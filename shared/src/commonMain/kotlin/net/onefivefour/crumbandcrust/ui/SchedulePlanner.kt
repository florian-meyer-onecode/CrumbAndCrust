package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.datetime.*
import kotlin.time.Clock
import net.onefivefour.crumbandcrust.recipe.*

@Composable
internal fun SchedulePlanner(recipe: Recipe, plan: RecipeSchedule?, onPlan: (RecipeSchedule?) -> Unit) {
    val zone = remember { TimeZone.currentSystemDefault() }
    val now = remember(recipe) { Clock.System.now().toLocalDateTime(zone) }
    var mode by remember(recipe) { mutableStateOf(PlanAnchor.START) }
    var dateText by remember(recipe) { mutableStateOf(formatPlanDate(now.date)) }
    var timeText by remember(recipe) { mutableStateOf(formatPlanTime(now.time)) }
    var attempted by remember(recipe) { mutableStateOf(false) }
    var error by remember(recipe) { mutableStateOf<String?>(null) }
    val date = parsePlanDate(dateText)
    val time = parsePlanTime(timeText)
    fun invalidate() { onPlan(null); error = null; attempted = false }

    Heading("Wann möchtest du backen?")
    Spacer(Modifier.height(8.dp))
    Body("Wähle den Beginn oder das Ende des letzten Schritts.")
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BreadButton("Startzeit", { mode = PlanAnchor.START; invalidate() }, Modifier.weight(1f).semantics { selected = mode == PlanAnchor.START }, secondary = mode != PlanAnchor.START)
        BreadButton("Zielzeit", { mode = PlanAnchor.FINISH; invalidate() }, Modifier.weight(1f).semantics { selected = mode == PlanAnchor.FINISH }, secondary = mode != PlanAnchor.FINISH)
    }
    Spacer(Modifier.height(12.dp))
    RecipeField("Datum (TT.MM.JJJJ)", dateText, { dateText = it; invalidate() },
        if (attempted && date == null) "Gib ein gültiges Datum ein, z. B. 09.09.2026." else null, true)
    Spacer(Modifier.height(12.dp))
    RecipeField("Uhrzeit (HH:MM)", timeText, { timeText = it; invalidate() },
        if (attempted && time == null) "Gib eine Uhrzeit von 00:00 bis 23:59 ein." else null, true)
    Spacer(Modifier.height(12.dp))
    BreadButton("Planung berechnen", {
        attempted = true
        error = null
        if (date != null && time != null) {
            try { onPlan(recipe.schedule(resolvePlanAnchor(date, time, zone), mode, zone)) }
            catch (e: IllegalArgumentException) { error = e.message ?: "Diese Planung konnte nicht berechnet werden."; onPlan(null) }
        }
    }, Modifier.fillMaxWidth())
    error?.let { Spacer(Modifier.height(12.dp)); Notice(it) }
    Spacer(Modifier.height(10.dp))
    Body("Zeitzone: ${zone.id}. Bei doppelt vorkommenden Uhrzeiten gilt das erste Vorkommen.", small = true)
    if (plan != null) {
        Spacer(Modifier.height(16.dp))
        Notice("Start: ${formatPlannedMoment(plan.start, zone)} (${formatPlanOffset(plan.start, zone)})\nFertig: ${formatPlannedMoment(plan.end, zone)} (${formatPlanOffset(plan.end, zone)})")
        Spacer(Modifier.height(8.dp))
        BreadButton("Planung entfernen", { onPlan(null); attempted = false; error = null }, Modifier.fillMaxWidth(), secondary = true)
    }
    Spacer(Modifier.height(10.dp))
    Body("Die Planung gilt für diese Ansicht. Rezeptwechsel, Bearbeiten oder Neuladen setzen sie zurück.", small = true)
}
