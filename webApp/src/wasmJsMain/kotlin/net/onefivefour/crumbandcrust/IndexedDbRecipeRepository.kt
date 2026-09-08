@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package net.onefivefour.crumbandcrust

import kotlin.js.JsString
import kotlin.js.Promise
import kotlin.js.js
import kotlinx.coroutines.await
import net.onefivefour.crumbandcrust.recipe.Recipe
import net.onefivefour.crumbandcrust.recipe.RecipeCodec
import net.onefivefour.crumbandcrust.recipe.RecipeRepository

private fun loadRecipes(): Promise<JsString> = js("globalThis.CrumbRecipeStorage.load()")
private fun addRecipe(payload: String): Promise<JsString> = js("globalThis.CrumbRecipeStorage.add(payload)")

private fun updateRecipe(payload: String): Promise<JsString> = js("globalThis.CrumbRecipeStorage.update(payload)")

class IndexedDbRecipeRepository : RecipeRepository {
    override suspend fun load(): List<Recipe> = RecipeCodec.decodeList(loadRecipes().await().toString())
    override suspend fun update(recipe: Recipe) {
        updateRecipe(RecipeCodec.encode(recipe)).await()
    }
    override suspend fun add(recipe: Recipe) {
        addRecipe(RecipeCodec.encode(recipe)).await()
    }
}
