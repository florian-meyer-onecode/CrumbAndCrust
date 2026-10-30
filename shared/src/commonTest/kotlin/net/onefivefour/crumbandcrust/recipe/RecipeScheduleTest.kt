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

    @Test fun dayControlsCrossLeapDaysAndYearBoundaries() {
        assertEquals(LocalDate(2028, 2, 29), shiftPlanDate(LocalDate(2028, 3, 1), -1))
        assertEquals(LocalDate(2027, 1, 1), shiftPlanDate(LocalDate(2026, 12, 31), 1))
        assertNull(shiftPlanDate(LocalDate(1, 1, 1), -1))
        assertNull(shiftPlanDate(LocalDate(9999, 12, 31), 1))
    }

    @Test fun hourControlsKeepMinutesAndAdjustDate() {
        assertEquals(LocalDateTime(2027, 1, 1, 0, 45), shiftPlanHour(LocalDate(2026, 12, 31), LocalTime(23, 45), 1))
        assertEquals(LocalDateTime(2028, 2, 29, 23, 15), shiftPlanHour(LocalDate(2028, 3, 1), LocalTime(0, 15), -1))
        assertEquals(LocalDateTime(2026, 9, 8, 13, 30), shiftPlanHour(LocalDate(2026, 9, 8), LocalTime(12, 30), 1))
        assertNull(shiftPlanHour(LocalDate(1, 1, 1), LocalTime(0, 30), -1))
        assertNull(shiftPlanHour(LocalDate(9999, 12, 31), LocalTime(23, 30), 1))
    }

    @Test fun hourControlsEditWallClockWhileCalculationRejectsSpringGap() {
        val local = checkNotNull(shiftPlanHour(LocalDate(2026, 3, 29), LocalTime(1, 30), 1))
        assertEquals(LocalTime(2, 30), local.time)
        assertFailsWith<IllegalArgumentException> { resolvePlanAnchor(local.date, local.time, zone) }
    }
    @Test fun timelineLabelsUseCalendarDaysAcrossYearBoundary() {
        val today = LocalDate(2026, 12, 31)
        listOf(
            Triple("31.12.2026", "23:59", "23:59"),
            Triple("01.01.2027", "00:01", "morgen · 00:01"),
            Triple("02.01.2027", "08:00", "Übermorgen · 08:00"),
            Triple("03.01.2027", "09:00", "in 3 Tagen · 09:00"),
            Triple("10.01.2027", "09:00", "in 10 Tagen · 09:00"),
            Triple("30.12.2026", "12:00", "30.12.2026 · 12:00")
        ).forEach { (date, time, expected) ->
            assertEquals(expected, formatTimelineMoment(anchor(date, time), zone, today))
        }
    }

    @Test fun timelineLabelsFollowBothPlanningDirectionsAndLocalDates() {
        val today = LocalDate(2026, 12, 31)
        val forward = recipe.schedule(anchor("31.12.2026", "23:30"), PlanAnchor.START, zone)
        val backward = recipe.schedule(forward.end, PlanAnchor.FINISH, zone)
        for (plan in listOf(forward, backward)) {
            assertEquals("23:30", formatTimelineMoment(plan.steps.first().start, zone, today))
            assertEquals("Übermorgen · 02:30", formatTimelineMoment(plan.end, zone, today))
        }
        assertEquals("morgen · 00:30", formatTimelineMoment(
            Instant.parse("2026-12-31T23:30:00Z"), zone, today))
        assertEquals("Übermorgen · 00:00", formatTimelineMoment(
            anchor("30.03.2026", "00:00"), zone, LocalDate(2026, 3, 28)))
        assertEquals("Übermorgen · 00:00", formatTimelineMoment(
            anchor("26.10.2026", "00:00"), zone, LocalDate(2026, 10, 24)))
    }
}
