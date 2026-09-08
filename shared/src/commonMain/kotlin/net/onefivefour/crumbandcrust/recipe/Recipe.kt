@file:OptIn(kotlin.uuid.ExperimentalUuidApi::class)

package net.onefivefour.crumbandcrust.recipe

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Ingredient(val id: String, val name: String, val grams: Double)

@Serializable
data class RecipeStep(val id: String, val title: String, val instruction: String, val durationMinutes: Int)

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val schemaVersion: Int = 1,
) {
    val durationMinutes: Int get() = steps.sumOf { it.durationMinutes }
}

data class IngredientDraft(val id: String = Uuid.random().toString(), val name: String = "", val grams: String = "")
data class StepDraft(
    val id: String = Uuid.random().toString(),
    val title: String = "",
    val instruction: String = "",
    val hours: String = "0",
    val minutes: String = "0",
)

data class RecipeDraft(
    val id: String = Uuid.random().toString(),
    val name: String = "",
    val description: String = "",
    val ingredients: List<IngredientDraft> = listOf(IngredientDraft()),
    val steps: List<StepDraft> = listOf(StepDraft()),
) {
    fun moveStep(id: String, direction: Int): RecipeDraft {
        require(direction == -1 || direction == 1)
        val index = steps.indexOfFirst { it.id == id }
        val destination = index + direction
        if (index == -1 || destination !in steps.indices) return this
        return copy(steps = steps.toMutableList().apply { add(destination, removeAt(index)) })
    }

    fun validate(): Map<String, String> = buildMap {
        if (name.isBlank()) put("name", "Gib deinem Rezept einen Namen.")
        else if (name.trim().length > 120) put("name", "Verwende höchstens 120 Zeichen.")
        if (description.length > 1000) put("description", "Verwende höchstens 1.000 Zeichen.")
        if (ingredients.isEmpty()) put("ingredients", "Füge mindestens eine Zutat hinzu.")
        if (ingredients.size > 100) put("ingredients", "Pro Rezept sind höchstens 100 Zutaten möglich.")
        ingredients.forEach { item ->
            if (item.name.isBlank()) put("${item.id}.name", "Wie heißt die Zutat?")
            else if (item.name.trim().length > 100) put("${item.id}.name", "Verwende höchstens 100 Zeichen.")
            val grams = parseGrams(item.grams)
            if (grams == null) put("${item.id}.grams", "Gib eine Menge über 0 bis 1.000.000 g ein, z. B. 0,5.")
        }
        if (steps.isEmpty()) put("steps", "Füge mindestens einen Arbeitsschritt hinzu.")
        if (steps.size > 100) put("steps", "Pro Rezept sind höchstens 100 Schritte möglich.")
        steps.forEach { step ->
            if (step.title.isBlank()) put("${step.id}.title", "Benenne diesen Arbeitsschritt.")
            else if (step.title.trim().length > 120) put("${step.id}.title", "Verwende höchstens 120 Zeichen.")
            if (step.instruction.length > 5000) put("${step.id}.instruction", "Verwende höchstens 5.000 Zeichen.")
            if (step.hours.trim().toIntOrNull() !in 0..8760) put("${step.id}.hours", "Gib ganze Stunden von 0 bis 8760 ein.")
            if (step.minutes.trim().toIntOrNull() !in 0..59) put("${step.id}.minutes", "Gib ganze Minuten von 0 bis 59 ein.")
        }
    }

    fun toRecipe(): Recipe {
        require(validate().isEmpty()) { "Invalid recipe draft" }
        return Recipe(
            id = id, name = name.trim(), description = description.trim(),
            ingredients = ingredients.map { Ingredient(it.id, it.name.trim(), checkNotNull(parseGrams(it.grams))) },
            steps = steps.map { RecipeStep(it.id, it.title.trim(), it.instruction.trim(), it.hours.trim().toInt() * 60 + it.minutes.trim().toInt()) },
        )
    }
}

internal fun parseGrams(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    if (!Regex("[0-9]+(?:\\.[0-9]+)?").matches(normalized)) return null
    return normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 && it <= 1_000_000 }
}

object RecipeCodec {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    fun encode(recipe: Recipe): String = json.encodeToString(recipe)
    fun decodeList(value: String): List<Recipe> = json.decodeFromString<List<Recipe>>(value).also { recipes ->
        require(recipes.map { it.id }.distinct().size == recipes.size)
        recipes.forEach { recipe ->
            require(recipe.schemaVersion == 1 && recipe.id.isNotBlank() && recipe.name.isNotBlank())
            require(recipe.ingredients.isNotEmpty() && recipe.steps.isNotEmpty())
            require(recipe.ingredients.all { it.name.isNotBlank() && it.grams.isFinite() && it.grams > 0 })
            require(recipe.steps.all { it.title.isNotBlank() && it.durationMinutes in 0..525659 })
            require(recipe.steps.size <= 100 && recipe.ingredients.size <= 100)
        }
    }
}

fun formatDuration(minutes: Int): String = when {
    minutes < 60 -> "$minutes Min."
    minutes % 60 == 0 -> "${minutes / 60} Std."
    else -> "${minutes / 60} Std. ${minutes % 60} Min."
}
