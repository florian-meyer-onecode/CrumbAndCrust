package net.onefivefour.crumbandcrust.recipe

import kotlin.test.*

class RecipeDraftTest {
    private fun validDraft() = RecipeDraft(
        name = "  Sauerteigbrot  ", description = "  Mit Roggen  ",
        ingredients = listOf(IngredientDraft(name = " Mehl ", grams = "500"), IngredientDraft(name = "Hefe", grams = "0,5")),
        steps = listOf(StepDraft(title = "Mischen", instruction = " Alles mischen. ", minutes = "15"), StepDraft(title = "Ruhen", hours = "24", minutes = "30")),
    )

    @Test fun decimalCommaAndWhitespaceProduceNormalizedRecipe() {
        val recipe = validDraft().toRecipe()
        assertEquals("Sauerteigbrot", recipe.name)
        assertEquals("Mehl", recipe.ingredients.first().name)
        assertEquals(0.5, recipe.ingredients.last().grams)
        assertEquals("Alles mischen.", recipe.steps.first().instruction)
        assertEquals(1485, recipe.durationMinutes)
    }
    @Test fun rejectsZeroNegativeNonFiniteAndAmbiguousAmounts() {
        listOf("", "0", "-1", "NaN", "Infinity", "1e3", "1.000,5", "1,2,3", "1000001").forEach {
            assertNull(parseGrams(it), it)
        }
        assertEquals(0.25, parseGrams(" 0.25 "))
        assertEquals(1_000_000.0, parseGrams("1000000"))
    }
    @Test fun emptyCollectionsCannotBeSaved() {
        val draft = validDraft().copy(ingredients = emptyList(), steps = emptyList())
        assertTrue("ingredients" in draft.validate())
        assertTrue("steps" in draft.validate())
        assertFailsWith<IllegalArgumentException> { draft.toRecipe() }
    }
    @Test fun blankNamesAndOutOfRangeDurationAreReportedByStableFieldId() {
        val ingredient = IngredientDraft(id = "flour", name = " ", grams = "500")
        val step = StepDraft(id = "mix", title = "", hours = "-1", minutes = "60")
        val errors = validDraft().copy(name = " ", ingredients = listOf(ingredient), steps = listOf(step)).validate()
        assertEquals(setOf("name", "flour.name", "mix.title", "mix.hours", "mix.minutes"), errors.keys)
    }
    @Test fun durationRequiresWholeNumbersButAllowsZeroMilestone() {
        val valid = validDraft()
        val zero = valid.copy(steps = listOf(StepDraft(title = "Fertig")))
        assertEquals(0, zero.toRecipe().durationMinutes)
        listOf("", "1.5", "-1", "8761", "99999999999999").forEach { hours ->
            assertTrue(valid.copy(steps = listOf(StepDraft(id = "step", title = "Ruhen", hours = hours))).validate().containsKey("step.hours"))
        }
    }
    @Test fun reorderingPreservesIdentityInstructionsAndDurations() {
        val initial = validDraft()
        val moved = initial.moveStep(initial.steps.last().id, -1)
        assertEquals(initial.steps.reversed(), moved.steps)
        assertEquals(initial.id, moved.id)
        assertEquals(initial.steps, moved.moveStep(moved.steps.first().id, 1).steps)
        assertEquals(initial, initial.moveStep(initial.steps.first().id, -1))
    }
    @Test fun deletingAndAddingDoesNotReuseStepIdentity() {
        val draft = validDraft()
        val removedId = draft.steps.first().id
        val replaced = draft.copy(steps = draft.steps.drop(1) + StepDraft(title = "Backen"))
        assertFalse(replaced.steps.any { it.id == removedId })
        assertEquals(draft.steps.last().id, replaced.steps.first().id)
    }
    @Test fun codecRoundTripPreservesGermanTextDecimalsAndOrder() {
        val recipe = validDraft().copy(description = "Öl & Körner <script>\nZweite Zeile").toRecipe()
        assertEquals(listOf(recipe), RecipeCodec.decodeList("[${RecipeCodec.encode(recipe)}]"))
    }
    @Test fun unknownSchemaAndCorruptDataAreRejectedInsteadOfBecomingEmptyList() {
        val recipe = validDraft().toRecipe()
        assertFails { RecipeCodec.decodeList("[${RecipeCodec.encode(recipe.copy(schemaVersion = 99))}]") }
        assertFails { RecipeCodec.decodeList("broken") }
        assertFails { RecipeCodec.decodeList("[${RecipeCodec.encode(recipe)},${RecipeCodec.encode(recipe)}]") }
    }
    @Test fun recipeNamesDoNotDefineIdentity() {
        val first = validDraft().toRecipe()
        val second = validDraft().toRecipe()
        assertEquals(first.name, second.name)
        assertNotEquals(first.id, second.id)
    }
    @Test fun existingRecipeRoundTripKeepsAllIdsValuesAndOrder() {
        val recipe = validDraft().toRecipe()
        val draft = recipe.toDraft()
        assertEquals(recipe.id, draft.id)
        assertEquals("0,5", draft.ingredients.last().grams)
        assertEquals("24", draft.steps.last().hours)
        assertEquals("30", draft.steps.last().minutes)
        assertEquals(recipe, draft.toRecipe())
    }
    @Test fun editingDraftDoesNotMutateStoredRecipe() {
        val recipe = validDraft().toRecipe()
        val draft = recipe.toDraft().copy(name = "Anderes Brot", ingredients = emptyList())
        assertEquals("Sauerteigbrot", recipe.name)
        assertEquals(2, recipe.ingredients.size)
        assertTrue(draft.validate().containsKey("ingredients"))
    }
    @Test fun smallAndLargeAmountsStayEditableWithoutRounding() {
        listOf(Double.MIN_VALUE, 0.0000001, 0.000123456789, 0.1234567890123456, 500.0, 1_000_000.0).forEach { grams ->
            val input = gramsForInput(grams)
            assertFalse(input.contains('e', ignoreCase = true), input)
            assertEquals(grams, parseGrams(input), input)
        }
        assertEquals("500", gramsForInput(500.0))
    }
    @Test fun editingRetainsUnchangedItemIdentitiesAfterReorderAndAdd() {
        val recipe = validDraft().toRecipe()
        val draft = recipe.toDraft().moveStep(recipe.steps.last().id, -1)
        val updated = draft.copy(ingredients = draft.ingredients + IngredientDraft(name = "Salz", grams = "10")).toRecipe()
        assertEquals(recipe.id, updated.id)
        assertEquals(recipe.steps.reversed(), updated.steps)
        assertEquals(recipe.ingredients, updated.ingredients.take(2))
        assertFalse(recipe.ingredients.any { it.id == updated.ingredients.last().id })
    }
    @Test fun editRoundTripPreservesZeroAndMaximumStepDuration() {
        val original = validDraft().copy(steps = listOf(StepDraft(title = "Ruhen", hours = "8760", minutes = "59"), StepDraft(title = "Fertig"))).toRecipe()
        assertEquals(original, original.toDraft().toRecipe())
    }

}
