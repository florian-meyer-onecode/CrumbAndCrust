package net.onefivefour.crumbandcrust.ui

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
    val previousDay = date?.let { shiftPlanDate(it, -1) }
    val nextDay = date?.let { shiftPlanDate(it, 1) }
    val previousHour = if (date != null && time != null) shiftPlanHour(date, time, -1) else null
    val nextHour = if (date != null && time != null) shiftPlanHour(date, time, 1) else null
    fun invalidate() { onPlan(null); error = null; attempted = false }

    fun setMoment(moment: LocalDateTime) {
        dateText = formatPlanDate(moment.date)
        timeText = formatPlanTime(moment.time)
        invalidate()
    }

    Heading("Wann möchtest du backen?")
    Spacer(Modifier.height(8.dp))
    Body("Wähle den Beginn oder das Ende des letzten Schritts.")
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BreadButton("Startzeit", { mode = PlanAnchor.START; invalidate() }, Modifier.weight(1f).semantics { selected = mode == PlanAnchor.START }, secondary = mode != PlanAnchor.START)
        BreadButton("Zielzeit", { mode = PlanAnchor.FINISH; invalidate() }, Modifier.weight(1f).semantics { selected = mode == PlanAnchor.FINISH }, secondary = mode != PlanAnchor.FINISH)
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Body("Datum & Uhrzeit", Modifier.weight(1f))
        BreadButton("Jetzt", { setMoment(Clock.System.now().toLocalDateTime(zone)) }, secondary = true)
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BreadButton("−", { previousDay?.let { dateText = formatPlanDate(it); invalidate() } },
            Modifier.width(54.dp).semantics { contentDescription = "−1 Tag" }, secondary = true, enabled = previousDay != null)
        RecipeField("Datum (TT.MM.JJJJ)", dateText, { dateText = it; invalidate() },
            if (attempted && date == null) "Gib ein gültiges Datum ein, z. B. 09.09.2026." else null, true,
            Modifier.weight(1f), showLabel = false)
        BreadButton("+", { nextDay?.let { dateText = formatPlanDate(it); invalidate() } },
            Modifier.width(54.dp).semantics { contentDescription = "+1 Tag" }, secondary = true, enabled = nextDay != null)
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        BreadButton("−", { previousHour?.let { setMoment(it) } },
            Modifier.width(54.dp).semantics { contentDescription = "−1 Stunde" }, secondary = true, enabled = previousHour != null)
        RecipeField("Uhrzeit (HH:MM)", timeText, { timeText = it; invalidate() },
            if (attempted && time == null) "Gib eine Uhrzeit von 00:00 bis 23:59 ein." else null, true,
            Modifier.weight(1f), showLabel = false)
        BreadButton("+", { nextHour?.let { setMoment(it) } },
            Modifier.width(54.dp).semantics { contentDescription = "+1 Stunde" }, secondary = true, enabled = nextHour != null)
    }
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
