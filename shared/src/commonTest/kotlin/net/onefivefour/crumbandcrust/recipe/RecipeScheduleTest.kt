package net.onefivefour.crumbandcrust.recipe

import kotlinx.datetime.*
import kotlin.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.test.*

class RecipeScheduleTest {
    private val zone = TimeZone.of("Europe/Berlin")
    private val recipe = Recipe("bread", "Brot", "", listOf(Ingredient("flour", "Mehl", 500.0)), listOf(
        RecipeStep("mix", "Mischen", "", 15), RecipeStep("rest", "Ruhen", "", 1500),
        RecipeStep("bake", "Backen", "", 45), RecipeStep("cool", "Abkühlen", "", 60), RecipeStep("done", "Fertig", "", 0)))
    private fun anchor(date: String, time: String) = resolvePlanAnchor(checkNotNull(parsePlanDate(date)), checkNotNull(parsePlanTime(time)), zone)

    @Test fun forwardAcrossYearIncludesCoolingAndZeroStep() {
        val plan = recipe.schedule(anchor("31.12.2026", "23:30"), PlanAnchor.START, zone)
        assertEquals("02.01.2027 · 02:30", formatPlannedMoment(plan.end, zone))
        assertEquals("31.12.2026 · 23:45", formatPlannedMoment(plan.steps[1].start, zone))
        assertEquals("02.01.2027 · 01:30", formatPlannedMoment(plan.steps[3].start, zone))
        assertEquals(plan.end, plan.steps.last().start)
        assertEquals(plan.end, plan.steps.last().end)
        assertEquals(recipe.steps, plan.steps.map { it.step })
    }

    @Test fun backwardMatchesForwardAndDoesNotChangeRecipe() {
        val original = recipe.copy()
        val forward = recipe.schedule(anchor("08.09.2026", "20:15"), PlanAnchor.START, zone)
        assertEquals(forward, recipe.schedule(forward.end, PlanAnchor.FINISH, zone))
        assertEquals(original, recipe)
        assertEquals(1620.minutes, forward.end - forward.start)
    }

    @Test fun leapDayAndMonthBoundaries() {
        val oneHour = recipe.copy(steps = listOf(RecipeStep("one", "Backen", "", 60)))
        val plan = oneHour.schedule(anchor("28.02.2028", "23:30"), PlanAnchor.START, zone)
        assertEquals("29.02.2028 · 00:30", formatPlannedMoment(plan.end, zone))
        assertEquals("29.02.2028 · 23:30", formatPlannedMoment(oneHour.schedule(anchor("01.03.2028", "00:30"), PlanAnchor.FINISH, zone).start, zone))
    }

    @Test fun dateAndTimeValidation() {
        assertNotNull(parsePlanDate(" 29.02.2000 "))
        listOf("29.02.2100", "29.02.2026", "31.04.2026", "00.01.2026", "01.13.2026", "01.01.0000", "2026-09-08", "", "8.9.2026").forEach { assertNull(parsePlanDate(it), it) }
        assertEquals(LocalTime(0, 0), parsePlanTime("00:00"))
        assertEquals(LocalTime(23, 59), parsePlanTime(" 23:59 "))
        listOf("24:00", "12:60", "-1:00", "9:00", "12:00:30", "").forEach { assertNull(parsePlanTime(it), it) }
    }

    @Test fun springForwardKeepsElapsedDuration() {
        val r = recipe.copy(steps = listOf(RecipeStep("rest", "Ruhen", "", 120)))
        val plan = r.schedule(anchor("29.03.2026", "01:30"), PlanAnchor.START, zone)
        assertEquals("29.03.2026 · 04:30", formatPlannedMoment(plan.end, zone))
        assertEquals(120.minutes, plan.end - plan.start)
        assertEquals(plan, r.schedule(plan.end, PlanAnchor.FINISH, zone))
        assertEquals("UTC+01:00", formatPlanOffset(plan.start, zone))
        assertEquals("UTC+02:00", formatPlanOffset(plan.end, zone))
    }

    @Test fun springGapIsRejected() {
        assertFailsWith<IllegalArgumentException> { anchor("29.03.2026", "02:30") }
    }

    @Test fun autumnOverlapChoosesFirstOccurrenceAndKeepsDuration() {
        val start = anchor("25.10.2026", "02:30")
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), start)
        val r = recipe.copy(steps = listOf(RecipeStep("rest", "Ruhen", "", 60)))
        val plan = r.schedule(start, PlanAnchor.START, zone)
        assertEquals("25.10.2026 · 02:30", formatPlannedMoment(plan.end, zone))
        assertEquals("UTC+01:00", formatPlanOffset(plan.end, zone))
        assertEquals("UTC+02:00", formatPlanOffset(plan.start, zone))
        assertEquals(60.minutes, plan.end - plan.start)
    }

    @Test fun timezoneChangesDisplayNotElapsedDuration() {
        val utc = recipe.schedule(Instant.parse("2026-09-08T20:00:00Z"), PlanAnchor.START, TimeZone.UTC)
        val berlin = recipe.schedule(utc.start, PlanAnchor.START, zone)
        assertEquals(utc.end, berlin.end)
        assertEquals("08.09.2026 · 20:00", formatPlannedMoment(utc.start, utc.zone))
        assertEquals("08.09.2026 · 22:00", formatPlannedMoment(berlin.start, zone))
        assertEquals("UTC+00:00", formatPlanOffset(utc.start, utc.zone))
    }

    @Test fun longPlansAndOutOfRangeResults() {
        val long = recipe.copy(steps = (1..100).map { RecipeStep("$it", "Ruhen", "", 525659) })
        val plan = long.schedule(anchor("01.01.2026", "00:00"), PlanAnchor.START, zone)
        assertEquals(52_565_900.minutes, plan.end - plan.start)
        assertFailsWith<IllegalArgumentException> { recipe.schedule(anchor("31.12.9999", "23:59"), PlanAnchor.START, zone) }
        assertFailsWith<IllegalArgumentException> { recipe.schedule(anchor("01.01.0001", "00:00"), PlanAnchor.FINISH, zone) }
    }
}
