package com.metroapp.lima.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object CurrencyUtils {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ','
    }
    private val format = DecimalFormat("#,##0.00", symbols)

    fun formatCents(cents: Long): String {
        val soles = cents / 100.0
        return "S/ ${format.format(soles)}"
    }

    fun parseSolesToCents(input: String): Long {
        val sanitized = input.trim()
            .replace("S/", "", ignoreCase = true)
            .replace(" ", "")
            .replace(",", ".")
        if (sanitized.isEmpty()) return 0L
        val amount = sanitized.toDoubleOrNull() ?: 0.0
        return (amount * 100).toLong()
    }
}

object DateUtils {
    val LIMA_ZONE: ZoneId = ZoneId.of("America/Lima")

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "PE"))
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale("es", "PE"))
    private val isoDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthDayFormatter = DateTimeFormatter.ofPattern("MM-dd")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "PE"))

    fun toLimaLocalDateTime(epochMillis: Long): LocalDateTime {
        return Instant.ofEpochMilli(epochMillis).atZone(LIMA_ZONE).toLocalDateTime()
    }

    fun toLimaLocalDate(epochMillis: Long): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(LIMA_ZONE).toLocalDate()
    }

    fun toEpochMillis(localDateTime: LocalDateTime): Long {
        return localDateTime.atZone(LIMA_ZONE).toInstant().toEpochMilli()
    }

    fun formatDate(epochMillis: Long): String = toLimaLocalDateTime(epochMillis).format(dateFormatter)

    fun formatTime(epochMillis: Long): String = toLimaLocalDateTime(epochMillis).format(timeFormatter)

    fun formatDateTime(epochMillis: Long): String = toLimaLocalDateTime(epochMillis).format(dateTimeFormatter)

    fun formatIsoDate(epochMillis: Long): String = toLimaLocalDate(epochMillis).format(isoDateFormatter)

    fun formatMonthDay(localDate: LocalDate): String = localDate.format(monthDayFormatter)

    fun formatMonthYear(localDate: LocalDate): String = localDate.format(monthYearFormatter)

    fun startOfDay(epochMillis: Long): Long {
        return toLimaLocalDate(epochMillis).atStartOfDay(LIMA_ZONE).toInstant().toEpochMilli()
    }

    fun endOfDay(epochMillis: Long): Long {
        return toLimaLocalDate(epochMillis).atTime(23, 59, 59, 999_000_000).atZone(LIMA_ZONE).toInstant().toEpochMilli()
    }
}
