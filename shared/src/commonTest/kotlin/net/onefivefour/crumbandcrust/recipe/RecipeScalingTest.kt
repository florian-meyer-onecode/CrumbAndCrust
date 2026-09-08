package net.onefivefour.crumbandcrust.recipe

import kotlin.test.*

class RecipeScalingTest {
    private val recipe = Recipe("bread", "Brot", "", listOf(
        Ingredient("flour", "Mehl", 500.0), Ingredient("water", "Wasser", 350.0), Ingredient("salt", "Salz", 10.0)
    ), listOf(RecipeStep("mix", "Mischen", "", 15), RecipeStep("rest", "Ruhen", "", 1500), RecipeStep("done", "Fertig", "", 0)))

    @Test fun scalesProportionallyWithoutChangingOriginal() {
        val copy = recipe.copy()
        val doubled = recipe.scaledIngredients(1720.0)
        assertEquals(listOf(1000.0, 700.0, 20.0), doubled.map { it.grams })
        assertEquals(recipe.ingredients.map { it.id }, doubled.map { it.id })
        assertEquals(copy, recipe)
        assertEquals(1515, recipe.durationMinutes)
    }

    @Test fun customTargetAndRepeatedCalculationsUseOriginalPrecision() {
        val scaled = recipe.scaledIngredients(1234.5)
        assertEquals(1234.5, scaled.sumOf { it.grams }, 0.000000001)
        assertEquals(500.0 / 350.0, scaled[0].grams / scaled[1].grams, 0.000000001)
        repeat(50) { recipe.scaledIngredients(0.5); recipe.scaledIngredients(100_000_000.0) }
        recipe.scaledIngredients(860.0).zip(recipe.ingredients).forEach { (a, b) -> assertEquals(b.grams, a.grams, 0.000000001) }
    }

    @Test fun targetAcceptsDecimalCommaAndRejectsInvalidWeights() {
        assertEquals(1234.5, parseTargetGrams(" 1234,5 "))
        assertEquals(0.5, parseTargetGrams("0.5"))
        assertEquals(100_000_000.0, parseTargetGrams("100000000"))
        listOf("", "0", "-1", "NaN", "Infinity", "1e3", "1.000,5", "100000001", "1,").forEach { assertNull(parseTargetGrams(it), it) }
        listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach { assertFailsWith<IllegalArgumentException> { recipe.scaledIngredients(it) } }
    }

    @Test fun displayRoundsWithoutHidingSmallIngredients() {
        assertEquals("500 g", formatGrams(500.0))
        assertEquals("0,5 g", formatGrams(0.5))
        assertEquals("12,35 g", formatGrams(12.3451))
        assertEquals("< 0,01 g", formatGrams(0.0000001))
        assertEquals("0 g", formatGrams(0.0))
    }

    @Test fun maximumAndTinyRecipesProduceFiniteAmounts() {
        val large = recipe.copy(ingredients = (1..100).map { Ingredient("$it", "Mehl", 1_000_000.0) })
        assertEquals(100_000_000.0, large.totalGrams)
        assertTrue(large.scaledIngredients(200_000_000.0).all { it.grams == 2_000_000.0 })
        val tiny = recipe.copy(ingredients = listOf(Ingredient("tiny", "Hefe", Double.MIN_VALUE)))
        assertEquals(1.0, tiny.scaledIngredients(1.0).single().grams)
    }
}
