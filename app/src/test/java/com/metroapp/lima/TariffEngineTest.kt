package com.metroapp.lima

import com.metroapp.lima.domain.engine.TariffEngine
import com.metroapp.lima.domain.model.CardType
import com.metroapp.lima.domain.model.Holiday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class TariffEngineTest {

    @Test
    fun adultCard_alwaysPaysFullFare_weekday() {
        val wednesday = LocalDateTime.of(2026, 10, 21, 8, 0)
        val result = TariffEngine.calculateFare(CardType.ADULT, wednesday, null, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
    }

    @Test
    fun adultCard_alwaysPaysFullFare_saturday() {
        val saturday = LocalDateTime.of(2026, 10, 24, 10, 0)
        val result = TariffEngine.calculateFare(CardType.ADULT, saturday, null, null)
        assertEquals(150L, result.fareCents)
    }

    @Test
    fun adultCard_alwaysPaysFullFare_sunday() {
        val sunday = LocalDateTime.of(2026, 10, 25, 12, 0)
        val result = TariffEngine.calculateFare(CardType.ADULT, sunday, null, null)
        assertEquals(150L, result.fareCents)
    }

    @Test
    fun adultCard_alwaysPaysFullFare_holiday() {
        val holidayDate = LocalDateTime.of(2026, 7, 28, 9, 0)
        val holiday = Holiday(dateIso = "2026-07-28", name = "Fiestas Patrias")
        val result = TariffEngine.calculateFare(CardType.ADULT, holidayDate, holiday, null)
        assertEquals(150L, result.fareCents)
    }

    @Test
    fun schoolCard_paysHalfFare_onWeekday_duringSchoolPeriod() {
        // Wednesday in October (within 02-15 to 12-31)
        val wednesday = LocalDateTime.of(2026, 10, 21, 7, 30)
        val result = TariffEngine.calculateFare(CardType.SCHOOL, wednesday, null, null)
        assertEquals(75L, result.fareCents)
        assertTrue(result.isHalfFareApplied)
    }

    @Test
    fun schoolCard_paysFullFare_onSaturday() {
        val saturday = LocalDateTime.of(2026, 10, 24, 8, 0)
        val result = TariffEngine.calculateFare(CardType.SCHOOL, saturday, null, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
    }

    @Test
    fun schoolCard_paysFullFare_onSunday() {
        val sunday = LocalDateTime.of(2026, 10, 25, 9, 0)
        val result = TariffEngine.calculateFare(CardType.SCHOOL, sunday, null, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
    }

    @Test
    fun schoolCard_paysFullFare_onHoliday_evenIfWeekday() {
        // July 28 (Fiestas Patrias) is a Tuesday in 2026
        val tuesdayHoliday = LocalDateTime.of(2026, 7, 28, 8, 30)
        val holiday = Holiday(dateIso = "2026-07-28", name = "Fiestas Patrias")
        val result = TariffEngine.calculateFare(CardType.SCHOOL, tuesdayHoliday, holiday, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
        assertTrue(result.explanation.contains("Fiestas Patrias"))
    }

    @Test
    fun schoolCard_paysFullFare_outsideSchoolPeriod() {
        // January 15 (School vacation)
        val januaryDay = LocalDateTime.of(2026, 1, 15, 8, 0)
        val result = TariffEngine.calculateFare(CardType.SCHOOL, januaryDay, null, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
    }

    @Test
    fun universityCard_paysHalfFare_onWeekday() {
        val thursday = LocalDateTime.of(2026, 10, 22, 18, 0)
        val result = TariffEngine.calculateFare(CardType.UNIVERSITY, thursday, null, null)
        assertEquals(75L, result.fareCents)
        assertTrue(result.isHalfFareApplied)
    }

    @Test
    fun universityCard_paysHalfFare_onSaturday() {
        // University students have half-fare on Saturday by Peruvian law
        val saturday = LocalDateTime.of(2026, 10, 24, 14, 0)
        val result = TariffEngine.calculateFare(CardType.UNIVERSITY, saturday, null, null)
        assertEquals(75L, result.fareCents)
        assertTrue(result.isHalfFareApplied)
    }

    @Test
    fun universityCard_paysFullFare_onSunday() {
        val sunday = LocalDateTime.of(2026, 10, 25, 11, 0)
        val result = TariffEngine.calculateFare(CardType.UNIVERSITY, sunday, null, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
    }

    @Test
    fun universityCard_paysFullFare_onHoliday_evenIfWeekday() {
        val wednesdayHoliday = LocalDateTime.of(2026, 10, 8, 9, 0)
        val holiday = Holiday(dateIso = "2026-10-08", name = "Combate de Angamos")
        val result = TariffEngine.calculateFare(CardType.UNIVERSITY, wednesdayHoliday, holiday, null)
        assertEquals(150L, result.fareCents)
        assertFalse(result.isHalfFareApplied)
        assertTrue(result.explanation.contains("Combate de Angamos"))
    }

    @Test
    fun customFareRule_appliedCorrectly() {
        val customRule = com.metroapp.lima.domain.model.FareRule(
            cardType = "VIP_TRANSIT",
            weekdayFareCents = 100L,
            saturdayFareCents = 120L,
            sundayFareCents = 150L,
            holidayFareCents = 180L
        )
        val wednesday = LocalDateTime.of(2026, 10, 21, 10, 0)
        val result = TariffEngine.calculateFare("VIP_TRANSIT", wednesday, null, customRule)
        assertEquals(100L, result.fareCents)
        assertTrue(result.explanation.contains("VIP_TRANSIT"))
    }
}
