package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.onefivefour.crumbandcrust.recipe.*

@Composable
internal fun IngredientsSection(recipe: Recipe) {
    var targetText by remember(recipe) { mutableStateOf(gramsForInput(recipe.totalGrams)) }
    var target by remember(recipe) { mutableStateOf(recipe.totalGrams) }
    var custom by remember(recipe) { mutableStateOf(false) }
    val parsed = parseTargetGrams(targetText)
    val invalid = custom && parsed == null
    val amounts = if (!custom && target == recipe.totalGrams) recipe.ingredients else recipe.scaledIngredients(target)
    Spacer(Modifier.height(28.dp))
    Label("DEINE ZUTATEN", Modifier.semantics { heading() })
    Spacer(Modifier.height(12.dp))
    Column(Modifier.fillMaxWidth().panel().padding(20.dp)) {
        Heading("Die richtige Menge")
        Spacer(Modifier.height(8.dp))
        Body("Grundrezept: ${formatGrams(recipe.totalGrams)} Gesamtteig")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BreadButton("1× Ansatz", { custom = false; target = recipe.totalGrams; targetText = gramsForInput(target) },
                Modifier.weight(1f).semantics { selected = !custom && target == recipe.totalGrams }, secondary = custom || target != recipe.totalGrams)
            BreadButton("2× Ansatz", { custom = false; target = recipe.totalGrams * 2; targetText = gramsForInput(target) },
                Modifier.weight(1f).semantics { selected = !custom && target == recipe.totalGrams * 2 }, secondary = custom || target != recipe.totalGrams * 2)
        }
        Spacer(Modifier.height(12.dp))
        RecipeField("Gewünschtes Gesamtteiggewicht in g", targetText, {
            custom = true
            targetText = it
            parseTargetGrams(it)?.let { value -> target = value }
        }, if (invalid) "Gib ein Gewicht über 0 bis 100.000.000 g ein." else null, true, keyboardType = KeyboardType.Decimal)
        Spacer(Modifier.height(12.dp))
        Body(if (invalid) "Die Zutaten zeigen weiterhin die letzte gültige Menge: ${formatGrams(target)}."
            else "Angezeigte Gesamtmenge: ${formatGrams(target)}", Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Spacer(Modifier.height(16.dp))
        Divider()
        amounts.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp).semantics(mergeDescendants = true) {},
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Body(item.name, Modifier.weight(1f))
                Body(formatGrams(item.grams), Modifier.widthIn(max = 120.dp), textAlign = TextAlign.End)
            }
            Divider()
        }
        Spacer(Modifier.height(12.dp))
        Body("Mengen auf 0,01 g gerundet. Das Grundrezept bleibt unverändert; die Dauern werden nicht skaliert.", small = true)
    }
}

@Composable
internal fun RecipeTimeline(recipe: Recipe, onEditRecipe: (Recipe) -> Unit) {
    var plan by remember(recipe) { mutableStateOf<RecipeSchedule?>(null) }
    Heading(recipe.name)
    if (recipe.description.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Body(recipe.description)
    }
    Spacer(Modifier.height(16.dp))
    BreadButton("Rezept bearbeiten", { onEditRecipe(recipe) }, Modifier.fillMaxWidth(), secondary = true)
    Spacer(Modifier.height(20.dp))
    Divider()
    Spacer(Modifier.height(20.dp))
    SchedulePlanner(recipe, plan) { plan = it }
    Spacer(Modifier.height(20.dp))
    Divider()
    Spacer(Modifier.height(20.dp))
    Label("GESAMTDAUER")
    Spacer(Modifier.height(6.dp))
    Heading(formatDuration(recipe.durationMinutes))
    Spacer(Modifier.height(8.dp))
    Body("${recipe.steps.size} Schritte · einschließlich aller eingetragenen Ruhezeiten")
    Spacer(Modifier.height(24.dp))
    recipe.steps.forEachIndexed { index, step ->
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.width(32.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(32.dp).background(BreadColors.tint, CircleShape), contentAlignment = Alignment.Center) {
                    BasicText("${index + 1}", Modifier.semantics { contentDescription = "Schritt ${index + 1}" }, style = LocalBreadTypography.current.label)
                }
                if (index < recipe.steps.lastIndex) Box(Modifier.width(2.dp).weight(1f).background(BreadColors.line))
            }
            Column(Modifier.weight(1f).padding(bottom = 28.dp)) {
                Heading(step.title)
                plan?.let { schedule ->
                    val timed = schedule.steps[index]
                    Spacer(Modifier.height(8.dp))
                    Body("Beginn: ${formatPlannedMoment(timed.start, schedule.zone)}")
                    Body("Ende: ${formatPlannedMoment(timed.end, schedule.zone)}")
                    Body("${formatPlanOffset(timed.start, schedule.zone)} → ${formatPlanOffset(timed.end, schedule.zone)}", small = true)
                }
                Spacer(Modifier.height(6.dp))
                Label("Dauer: ${formatDuration(step.durationMinutes)}")
                if (step.instruction.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Body(step.instruction)
                }
            }
        }
    }
    Divider()
    Spacer(Modifier.height(16.dp))
    Body("Die Dauer eines Schritts reicht bis zum nächsten Schritt – beim letzten bis zum Ende des Rezepts.", small = true)
}
