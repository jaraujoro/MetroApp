package com.metroapp.lima.domain.engine

import com.metroapp.lima.domain.model.CardType
import com.metroapp.lima.domain.model.FareRule
import com.metroapp.lima.domain.model.Holiday
import com.metroapp.lima.util.CurrencyUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

data class FareCalculationResult(
    val fareCents: Long,
    val explanation: String,
    val isHalfFareApplied: Boolean
)

object TariffEngine {

    fun calculateFare(
        cardType: CardType,
        dateTime: LocalDateTime,
        holiday: Holiday?,
        fareRule: FareRule?
    ): FareCalculationResult {
        return calculateFare(cardType.name, dateTime, holiday, fareRule)
    }

    fun calculateFare(
        cardType: String,
        dateTime: LocalDateTime,
        holiday: Holiday?,
        fareRule: FareRule?
    ): FareCalculationResult {
        val dayOfWeek = dateTime.dayOfWeek
        val isSunday = dayOfWeek == DayOfWeek.SUNDAY
        val isSaturday = dayOfWeek == DayOfWeek.SATURDAY
        val isWeekday = !isSunday && !isSaturday
        val isHoliday = holiday != null

        val upperType = cardType.uppercase()
        // Default fares if rule is not configured
        val defaultWeekday = if (upperType == "ADULT") 150L else 75L
        val defaultSaturday = if (upperType == "UNIVERSITY") 75L else 150L
        val defaultSunday = 150L
        val defaultHoliday = 150L

        val weekdayFare = fareRule?.weekdayFareCents ?: defaultWeekday
        val saturdayFare = fareRule?.saturdayFareCents ?: defaultSaturday
        val sundayFare = fareRule?.sundayFareCents ?: defaultSunday
        val holidayFare = fareRule?.holidayFareCents ?: defaultHoliday
        val ruleName = fareRule?.displayName ?: when (upperType) {
            "ADULT" -> "Adulto"
            "SCHOOL" -> "Escolar"
            "UNIVERSITY" -> "Universitario / Instituto"
            else -> cardType
        }

        return when (upperType) {
            "ADULT" -> {
                val fare = if (isHoliday) holidayFare else if (isSunday) sundayFare else if (isSaturday) saturdayFare else weekdayFare
                val explanation = if (isHoliday) {
                    "Tarifa adulto en feriado (${holiday!!.name}): ${CurrencyUtils.formatCents(fare)}"
                } else if (isSunday) {
                    "Tarifa adulto en domingo: ${CurrencyUtils.formatCents(fare)}"
                } else {
                    "Tarifa regular adulto: ${CurrencyUtils.formatCents(fare)}"
                }
                FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
            }

            "SCHOOL" -> {
                // Check validity period for school half-fare
                val localDate = dateTime.toLocalDate()
                val isWithinValidity = isSchoolBenefitValid(localDate, fareRule?.validityStartMonthDay, fareRule?.validityEndMonthDay)

                if (!isWithinValidity) {
                    val fare = sundayFare
                    val explanation = "Tarifa completa: fuera del periodo escolar oficial (${CurrencyUtils.formatCents(fare)})"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isHoliday) {
                    val fare = holidayFare
                    val explanation = "Tarifa completa escolar en feriado (${holiday!!.name}): ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSunday) {
                    val fare = sundayFare
                    val explanation = "Tarifa completa escolar en domingo: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSaturday) {
                    val fare = saturdayFare
                    val explanation = "Tarifa completa escolar en sábado: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else {
                    val fare = weekdayFare
                    val explanation = "Medio pasaje escolar vigente (Lunes a Viernes): ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = true)
                }
            }

            "UNIVERSITY" -> {
                if (isHoliday) {
                    val fare = holidayFare
                    val explanation = "Tarifa completa universitario en feriado (${holiday!!.name}): ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSunday) {
                    val fare = sundayFare
                    val explanation = "Tarifa completa universitario en domingo: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSaturday) {
                    val fare = saturdayFare
                    val explanation = "Medio pasaje universitario en sábado: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = true)
                } else {
                    val fare = weekdayFare
                    val explanation = "Medio pasaje universitario de Lunes a Viernes: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = true)
                }
            }

            else -> {
                // Custom fare rule!
                val localDate = dateTime.toLocalDate()
                val hasValidity = !fareRule?.validityStartMonthDay.isNullOrBlank() && !fareRule?.validityEndMonthDay.isNullOrBlank()
                val isWithinValidity = !hasValidity || isSchoolBenefitValid(localDate, fareRule?.validityStartMonthDay, fareRule?.validityEndMonthDay)

                if (!isWithinValidity) {
                    val fare = sundayFare
                    val explanation = "Tarifa completa ($ruleName): fuera del periodo de vigencia (${CurrencyUtils.formatCents(fare)})"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isHoliday) {
                    val fare = holidayFare
                    val explanation = "Tarifa $ruleName en feriado (${holiday!!.name}): ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSunday) {
                    val fare = sundayFare
                    val explanation = "Tarifa $ruleName en domingo: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = false)
                } else if (isSaturday) {
                    val fare = saturdayFare
                    val explanation = "Tarifa $ruleName en sábado: ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = fare < sundayFare)
                } else {
                    val fare = weekdayFare
                    val explanation = "Tarifa $ruleName (Lunes a Viernes): ${CurrencyUtils.formatCents(fare)}"
                    FareCalculationResult(fareCents = fare, explanation = explanation, isHalfFareApplied = fare < sundayFare)
                }
            }
        }
    }

    private fun isSchoolBenefitValid(
        date: LocalDate,
        startMonthDayStr: String?,
        endMonthDayStr: String?
    ): Boolean {
        // Default range: 02-15 to 12-31
        val startStr = startMonthDayStr ?: "02-15"
        val endStr = endMonthDayStr ?: "12-31"

        return try {
            val startParts = startStr.split("-").map { it.toInt() }
            val endParts = endStr.split("-").map { it.toInt() }

            val year = date.year
            val startDate = LocalDate.of(year, startParts[0], startParts[1])
            val endDate = LocalDate.of(year, endParts[0], endParts[1])

            !date.isBefore(startDate) && !date.isAfter(endDate)
        } catch (_: Exception) {
            true
        }
    }
}
