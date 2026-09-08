package net.onefivefour.crumbandcrust

import androidx.compose.runtime.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import net.onefivefour.crumbandcrust.recipe.*
import net.onefivefour.crumbandcrust.ui.BreadTheme
import net.onefivefour.crumbandcrust.ui.HomeScreen
import net.onefivefour.crumbandcrust.ui.RecipeEditor

@Composable
fun App(repository: RecipeRepository = BrowserOnlyRecipeRepository) {
    var recipes by remember { mutableStateOf(emptyList<Recipe>()) }
    var loading by remember { mutableStateOf(repository.isAvailable) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var retry by remember { mutableIntStateOf(0) }
    var recipeBeingEdited by remember { mutableStateOf<Recipe?>(null) }
    var selectedRecipeId by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var savedRecipe by remember { mutableStateOf<Recipe?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(repository, retry) {
        if (!repository.isAvailable) return@LaunchedEffect
        loading = true
        loadError = null
        try {
            recipes = repository.load().sortedBy { it.name.lowercase() }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            loadError = "Deine Rezepte konnten nicht geladen werden. Prüfe, ob dein Browser Websitedaten erlaubt, und schließe gegebenenfalls andere Tabs dieser App."
        } finally {
            loading = false
        }
    }

    BreadTheme {
        if (editing) {
            RecipeEditor(
                existingRecipe = recipeBeingEdited, saving = saving, saveError = saveError,
                onCancel = { if (!saving) { editing = false; recipeBeingEdited = null; saveError = null } },
                onSave = { recipe ->
                    if (!saving) {
                        saving = true
                        saveError = null
                        val isUpdate = recipeBeingEdited != null
                        scope.launch {
                            try {
                                if (isUpdate) repository.update(recipe) else repository.add(recipe)
                                recipes = (if (isUpdate) recipes.map { if (it.id == recipe.id) recipe else it } else recipes + recipe)
                                    .sortedBy { it.name.lowercase() }
                                selectedRecipeId = recipe.id
                                savedRecipe = recipe
                                editing = false
                                recipeBeingEdited = null
                            } catch (cancelled: CancellationException) {
                                throw cancelled
                            } catch (_: Exception) {
                                saveError = "Das Rezept konnte nicht gespeichert werden. Dein Entwurf bleibt erhalten. Prüfe den verfügbaren Browserspeicher und versuche es erneut."
                            } finally {
                                saving = false
                            }
                        }
                    }
                },
            )
        } else {
            HomeScreen(
                recipes = recipes, loading = loading, loadError = loadError, onRetry = { retry++ },
                onAddRecipe = { savedRecipe = null; saveError = null; recipeBeingEdited = null; editing = true },
                onEditRecipe = { recipe ->
                    savedRecipe = null; saveError = null; selectedRecipeId = recipe.id
                    recipeBeingEdited = recipe; editing = true
                },
                selectedRecipeId = selectedRecipeId, onSelectRecipe = { selectedRecipeId = it },
                storageAvailable = repository.isAvailable, savedRecipe = savedRecipe,
            )
        }
    }
}
