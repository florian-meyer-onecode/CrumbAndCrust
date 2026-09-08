package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.onefivefour.crumbandcrust.recipe.Recipe
import net.onefivefour.crumbandcrust.recipe.formatDuration

@Composable
internal fun HomeScreen(
    recipes: List<Recipe>, loading: Boolean, loadError: String?, onRetry: () -> Unit,
    onAddRecipe: () -> Unit, storageAvailable: Boolean, savedRecipe: Recipe?,
) {
    var selectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(savedRecipe?.id) { if (savedRecipe != null) selectedId = savedRecipe.id }
    val selectedRecipe = recipes.firstOrNull { it.id == selectedId } ?: recipes.firstOrNull()
    BoxWithConstraints(Modifier.fillMaxSize().background(BreadColors.background).safeDrawingPadding()) {
        val twoColumns = maxWidth >= 840.dp
        val compact = maxWidth < 600.dp
        val gutter = if (compact) 20.dp else 32.dp
        Column(Modifier.fillMaxSize()) {
            Header(compact)
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = gutter),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.widthIn(max = 1024.dp).fillMaxWidth().padding(top = if (compact) 28.dp else 42.dp, bottom = 24.dp)) {
                    Label("DEINE BACKSTUBE")
                    Spacer(Modifier.height(10.dp))
                    BasicText("Gutes Brot braucht Zeit.", Modifier.semantics { heading() },
                        style = LocalBreadTypography.current.heading.copy(fontSize = if (compact) 32.sp else 40.sp, lineHeight = 46.sp))
                    Spacer(Modifier.height(6.dp))
                    Body("Deine Rezepte und jeder Schritt bis zum fertigen Brot – an einem Ort.")
                    Spacer(Modifier.height(24.dp))
                    if (savedRecipe != null) {
                        Notice("„${savedRecipe.name}“ wurde in diesem Browser gespeichert.")
                        Spacer(Modifier.height(20.dp))
                    }
                    if (!storageAvailable) {
                        Notice("Rezepte lassen sich vorerst nur in der Browser-Version speichern.")
                        Spacer(Modifier.height(20.dp))
                    }
                    if (twoColumns) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.Top) {
                            RecipeSection(Modifier.weight(.4f), recipes, selectedRecipe?.id, loading, loadError, onRetry,
                                onAddRecipe, storageAvailable, onSelect = { selectedId = it })
                            ScheduleSection(Modifier.weight(.6f), selectedRecipe)
                        }
                    } else {
                        RecipeSection(Modifier.fillMaxWidth(), recipes, selectedRecipe?.id, loading, loadError, onRetry,
                            onAddRecipe, storageAvailable, onSelect = { selectedId = it })
                        Spacer(Modifier.height(28.dp))
                        ScheduleSection(Modifier.fillMaxWidth(), selectedRecipe)
                    }
                    Spacer(Modifier.height(28.dp))
                    Body("Mehl. Wasser. Zeit. Und ein guter Plan.", Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun RecipeSection(
    modifier: Modifier, recipes: List<Recipe>, selectedId: String?, loading: Boolean, loadError: String?,
    onRetry: () -> Unit, onAddRecipe: () -> Unit, storageAvailable: Boolean, onSelect: (String) -> Unit,
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Label("MEINE REZEPTE", Modifier.semantics { heading() })
            Spacer(Modifier.weight(1f))
            if (!loading && loadError == null) Body("${recipes.size} ${if (recipes.size == 1) "Rezept" else "Rezepte"}", small = true)
        }
        Spacer(Modifier.height(12.dp))
        when {
            loading -> Notice("Deine Rezepte werden geladen …")
            loadError != null -> Column(Modifier.fillMaxWidth().panel().padding(24.dp)) {
                Notice(loadError)
                Spacer(Modifier.height(12.dp))
                BreadButton("Erneut laden", onRetry, Modifier.fillMaxWidth())
            }
            recipes.isEmpty() -> Column(Modifier.fillMaxWidth().panel().padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(12.dp))
                Box(Modifier.size(88.dp).background(BreadColors.tint, CircleShape), contentAlignment = Alignment.Center) { BreadMark(Modifier.size(64.dp)) }
                Spacer(Modifier.height(24.dp))
                Heading("Dein erstes Brot wartet.", Modifier.fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Body("Sammle hier deine Lieblingsrezepte – vom schnellen Hefebrot bis zum Sauerteig.", Modifier.fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(28.dp))
                BreadButton("Rezept hinzufügen", onAddRecipe, Modifier.fillMaxWidth(), showPlus = true, enabled = storageAvailable)
                Spacer(Modifier.height(12.dp))
                Body("Deine Sammlung beginnt mit einem Rezept.", Modifier.fillMaxWidth(), TextAlign.Center, small = true)
                Spacer(Modifier.height(8.dp))
            }
            else -> {
                recipes.forEach { recipe ->
                    val shape = RoundedCornerShape(12.dp)
                    Column(Modifier.fillMaxWidth().clip(shape)
                        .background(if (recipe.id == selectedId) BreadColors.tint else BreadColors.paper)
                        .border(1.dp, if (recipe.id == selectedId) BreadColors.accent else BreadColors.line, shape)
                        .clickable(role = Role.Button) { onSelect(recipe.id) }
                        .semantics { selected = recipe.id == selectedId }
                        .padding(18.dp)) {
                        BasicText(recipe.name, style = LocalBreadTypography.current.heading.copy(fontSize = 22.sp, lineHeight = 28.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (recipe.description.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            BasicText(recipe.description, style = LocalBreadTypography.current.body, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                        Spacer(Modifier.height(10.dp))
                        Body("${recipe.ingredients.size} Zutaten · ${recipe.steps.size} Schritte · ${formatDuration(recipe.durationMinutes)}", small = true)
                    }
                    Spacer(Modifier.height(10.dp))
                }
                BreadButton("Rezept hinzufügen", onAddRecipe, Modifier.fillMaxWidth(), showPlus = true)
            }
        }
        Spacer(Modifier.height(16.dp))
        Body("Zutaten, Mengen und Arbeitsschritte bleiben bei deinem Rezept zusammen.", Modifier.padding(horizontal = 4.dp), small = true)
    }
}

@Composable
private fun ScheduleSection(modifier: Modifier, recipe: Recipe?) {
    Column(modifier) {
        Box(Modifier.height(32.dp), contentAlignment = Alignment.CenterStart) { Label("DEIN BACKPLAN", Modifier.semantics { heading() }) }
        Spacer(Modifier.height(12.dp))
        Column(Modifier.fillMaxWidth().panel().padding(26.dp)) {
            Heading(recipe?.name ?: "Schritt für Schritt zum Brot")
            Spacer(Modifier.height(6.dp))
            Body(if (recipe == null) "Dein Rezept gibt den Rhythmus vor." else "In deiner Rezeptsammlung gespeichert.", small = true)
            Spacer(Modifier.height(22.dp))
            Divider()
            Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ScheduleIllustration(Modifier.size(200.dp, 160.dp))
                Spacer(Modifier.height(20.dp))
                Heading(if (recipe == null) "Noch kein Rezept ausgewählt" else "Dein Rezept ist bereit.", Modifier.fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Body(if (recipe == null) "Wähle ein Rezept aus deiner Sammlung aus. Hier erscheint dann der Ablauf mit allen Arbeitsschritten und Ruhezeiten."
                    else "Zutaten und Arbeitsschritte sind gespeichert. Der Backplan ist noch nicht verfügbar.",
                    Modifier.widthIn(max = 340.dp).fillMaxWidth(), TextAlign.Center)
                Spacer(Modifier.height(16.dp))
            }
            Divider()
            Body("Vom ersten Mischen bis zum letzten Abkühlen.", Modifier.padding(top = 16.dp), small = true)
        }
    }
}

@Composable
internal fun Notice(text: String) {
    Box(Modifier.fillMaxWidth().background(BreadColors.tint, RoundedCornerShape(8.dp)).padding(16.dp)
        .semantics { liveRegion = LiveRegionMode.Polite }) { Body(text) }
}
