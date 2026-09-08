package net.onefivefour.crumbandcrust.recipe

import kotlin.math.round

val Recipe.totalGrams: Double get() = ingredients.sumOf { it.grams }

fun parseTargetGrams(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    if (!Regex("[0-9]+(?:\\.[0-9]+)?").matches(normalized)) return null
    return normalized.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 && it <= 100_000_000 }
}

fun Recipe.scaledIngredients(targetGrams: Double): List<Ingredient> {
    require(targetGrams.isFinite() && targetGrams > 0)
    val total = totalGrams
    require(total.isFinite() && total > 0)
    val factor = targetGrams / total
    return ingredients.map { it.copy(grams = if (factor.isFinite()) it.grams * factor else (it.grams / total) * targetGrams) }
}

// Round only for presentation; retain full precision for every calculation.
fun formatGrams(grams: Double): String {
    require(grams.isFinite() && grams >= 0)
    if (grams == 0.0) return "0 g"
    if (grams < 0.01) return "< 0,01 g"
    return gramsForInput(round(grams * 100) / 100) + " g"
}

