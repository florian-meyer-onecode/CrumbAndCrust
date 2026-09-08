package net.onefivefour.crumbandcrust.recipe

interface RecipeRepository {
    val isAvailable: Boolean get() = true
    suspend fun load(): List<Recipe>
    suspend fun add(recipe: Recipe)
    suspend fun update(recipe: Recipe)
}

// Android persistence is deliberately deferred. Never report an in-memory write as saved.
object BrowserOnlyRecipeRepository : RecipeRepository {
    override val isAvailable = false
    override suspend fun load(): List<Recipe> = emptyList()
    override suspend fun update(recipe: Recipe): Nothing = error("Recipe storage is only available in the browser.")
    override suspend fun add(recipe: Recipe): Nothing = error("Recipe storage is only available in the browser.")
}
