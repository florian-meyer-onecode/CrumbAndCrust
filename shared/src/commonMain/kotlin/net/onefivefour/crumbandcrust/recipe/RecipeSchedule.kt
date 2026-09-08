package net.onefivefour.crumbandcrust.recipe

import kotlinx.datetime.*
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes

enum class PlanAnchor { START, FINISH }

data class TimedStep(val step: RecipeStep, val start: Instant, val end: Instant)
data class RecipeSchedule(val steps: List<TimedStep>, val start: Instant, val end: Instant, val zone: TimeZone)

fun parsePlanDate(text: String): LocalDate? {
    val match = Regex("([0-9]{2})\\.([0-9]{2})\\.([0-9]{4})").matchEntire(text.trim()) ?: return null
    val (day, month, year) = match.destructured
    if (year.toInt() !in 1..9999) return null
    return try { LocalDate(year.toInt(), month.toInt(), day.toInt()) } catch (_: IllegalArgumentException) { null }
}

fun parsePlanTime(text: String): LocalTime? {
    val match = Regex("([0-9]{2}):([0-9]{2})").matchEntire(text.trim()) ?: return null
    val (hour, minute) = match.destructured
    return try { LocalTime(hour.toInt(), minute.toInt()) } catch (_: IllegalArgumentException) { null }
}

fun resolvePlanAnchor(date: LocalDate, time: LocalTime, zone: TimeZone): Instant {
    val local = LocalDateTime(date, time)
    val instant = local.toInstant(zone)
    // A clock-forward gap must not silently move the user's requested time.
    require(instant.toLocalDateTime(zone) == local) { "Diese Uhrzeit gibt es wegen der Zeitumstellung nicht. Bitte wähle eine andere Uhrzeit." }
    return instant
}

fun Recipe.schedule(anchor: Instant, mode: PlanAnchor, zone: TimeZone): RecipeSchedule {
    require(steps.isNotEmpty() && steps.size <= 100 && steps.all { it.durationMinutes in 0..525659 })
    val total = steps.sumOf { it.durationMinutes.toLong() }.minutes
    val start = if (mode == PlanAnchor.START) anchor else anchor - total
    var cursor = start
    val timed = steps.map { step ->
        val end = cursor + step.durationMinutes.minutes
        TimedStep(step, cursor, end).also { cursor = end }
    }
    require(start.toLocalDateTime(zone).year in 1..9999 && cursor.toLocalDateTime(zone).year in 1..9999) {
        "Der berechnete Ablauf liegt außerhalb der Jahre 0001 bis 9999. Bitte wähle ein anderes Datum."
    }
    return RecipeSchedule(timed, start, cursor, zone)
}

fun formatPlanDate(date: LocalDate): String = "${date.day.toString().padStart(2, '0')}.${date.month.number.toString().padStart(2, '0')}.${date.year.toString().padStart(4, '0')}"
fun formatPlanTime(time: LocalTime): String = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
fun formatPlannedMoment(instant: Instant, zone: TimeZone): String {
    val local = instant.toLocalDateTime(zone)
    return "${formatPlanDate(local.date)} · ${formatPlanTime(local.time)}"
}
fun formatPlanOffset(instant: Instant, zone: TimeZone): String = "UTC${zone.offsetAt(instant).toString().let { if (it == "Z") "+00:00" else it }}"

// These controls edit local calendar fields; schedule calculation still validates DST gaps.
fun shiftPlanDate(date: LocalDate, days: Int): LocalDate? {
    require(days == -1 || days == 1)
    return date.plus(days, DateTimeUnit.DAY).takeIf { it.year in 1..9999 }
}

fun shiftPlanHour(date: LocalDate, time: LocalTime, hours: Int): LocalDateTime? {
    require(hours == -1 || hours == 1)
    val hour = time.hour + hours
    val shiftedDate = when {
        hour < 0 -> shiftPlanDate(date, -1)
        hour > 23 -> shiftPlanDate(date, 1)
        else -> date
    } ?: return null
    return LocalDateTime(shiftedDate, LocalTime((hour + 24) % 24, time.minute))
}
