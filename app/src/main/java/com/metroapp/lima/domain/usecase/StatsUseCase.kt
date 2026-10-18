package com.metroapp.lima.domain.usecase

import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

data class DashboardMetrics(
    val calculatedBalanceCents: Long = 0,
    val realVerifiedBalanceCents: Long? = null,
    val balanceDifferenceCents: Long = 0,
    val spentTodayCents: Long = 0,
    val spentThisWeekCents: Long = 0,
    val spentThisMonthCents: Long = 0,
    val tripsCountToday: Int = 0,
    val topUpsCountMonth: Int = 0,
    val lastMovementDate: Long? = null,
    val pendingTripsCount: Int = 0,
    val dailyChartPoints: List<Pair<String, Long>> = emptyList() // "Día" to cents
)

data class ReportsData(
    val totalSpentCents: Long = 0,
    val totalTopUpCents: Long = 0,
    val totalTripsCount: Int = 0,
    val totalTopUpsCount: Int = 0,
    val averageDailySpentCents: Long = 0,
    val projectedMonthlySpentCents: Long = 0,
    val expensesByCardType: Map<String, Long> = emptyMap(),
    val monthlyComparison: List<Pair<String, Long>> = emptyList(), // "Mes Año" to cents
    val dailyExpenses: List<Pair<String, Long>> = emptyList()     // "dd/MM" to cents
)

class StatsUseCase {

    fun calculateDashboardMetrics(
        cachedBalanceCents: Long,
        realVerifiedBalanceCents: Long?,
        transactions: List<MetroTransaction>
    ): DashboardMetrics {
        val now = LocalDate.now(DateUtils.LIMA_ZONE)
        val todayStart = now.atStartOfDay(DateUtils.LIMA_ZONE).toInstant().toEpochMilli()
        val todayEnd = now.atTime(23, 59, 59, 999_000_000).atZone(DateUtils.LIMA_ZONE).toInstant().toEpochMilli()

        // Week start (Monday)
        val weekStart = now.minusDays((now.dayOfWeek.value - 1).toLong())
            .atStartOfDay(DateUtils.LIMA_ZONE).toInstant().toEpochMilli()

        // Month start
        val monthStart = now.withDayOfMonth(1)
            .atStartOfDay(DateUtils.LIMA_ZONE).toInstant().toEpochMilli()

        var spentToday = 0L
        var spentThisWeek = 0L
        var spentThisMonth = 0L
        var tripsCountToday = 0
        var topUpsCountMonth = 0
        var pendingCount = 0
        var lastMovement: Long? = null

        // Last 7 days chart points
        val last7DaysMap = mutableMapOf<String, Long>()
        for (i in 6 downTo 0) {
            val d = now.minusDays(i.toLong())
            val key = d.dayOfWeek.name.take(3)
            last7DaysMap[key] = 0L
        }

        for (tx in transactions) {
            if (tx.status == TransactionStatus.PENDING) {
                pendingCount++
                continue
            }
            if (tx.status != TransactionStatus.CONFIRMED) continue

            if (lastMovement == null || tx.timestamp > lastMovement) {
                lastMovement = tx.timestamp
            }

            if (tx.type == MovementType.TRIP) {
                if (tx.timestamp in todayStart..todayEnd) {
                    spentToday += tx.amountCents
                    tripsCountToday++
                }
                if (tx.timestamp >= weekStart) {
                    spentThisWeek += tx.amountCents
                }
                if (tx.timestamp >= monthStart) {
                    spentThisMonth += tx.amountCents
                }

                // Check last 7 days
                val txDate = DateUtils.toLimaLocalDate(tx.timestamp)
                if (txDate.isAfter(now.minusDays(7)) && !txDate.isAfter(now)) {
                    val key = txDate.dayOfWeek.name.take(3)
                    last7DaysMap[key] = (last7DaysMap[key] ?: 0L) + tx.amountCents
                }
            } else if (tx.type == MovementType.TOP_UP) {
                if (tx.timestamp >= monthStart) {
                    topUpsCountMonth++
                }
            }
        }

        val diff = if (realVerifiedBalanceCents != null) realVerifiedBalanceCents - cachedBalanceCents else 0L

        return DashboardMetrics(
            calculatedBalanceCents = cachedBalanceCents,
            realVerifiedBalanceCents = realVerifiedBalanceCents,
            balanceDifferenceCents = diff,
            spentTodayCents = spentToday,
            spentThisWeekCents = spentThisWeek,
            spentThisMonthCents = spentThisMonth,
            tripsCountToday = tripsCountToday,
            topUpsCountMonth = topUpsCountMonth,
            lastMovementDate = lastMovement,
            pendingTripsCount = pendingCount,
            dailyChartPoints = last7DaysMap.toList()
        )
    }

    fun calculateReports(
        transactions: List<MetroTransaction>,
        startDateEpoch: Long?,
        endDateEpoch: Long?
    ): ReportsData {
        val filtered = transactions.filter { tx ->
            tx.status == TransactionStatus.CONFIRMED &&
            (startDateEpoch == null || tx.timestamp >= startDateEpoch) &&
            (endDateEpoch == null || tx.timestamp <= endDateEpoch)
        }

        var totalSpent = 0L
        var totalTopUp = 0L
        var tripsCount = 0
        var topUpsCount = 0

        val dailyMap = mutableMapOf<String, Long>()
        val monthlyMap = mutableMapOf<String, Long>()
        val distinctDays = mutableSetOf<String>()

        for (tx in filtered) {
            val dateStr = DateUtils.formatDate(tx.timestamp)
            val monthYearStr = DateUtils.formatMonthYear(DateUtils.toLimaLocalDate(tx.timestamp))
            distinctDays.add(dateStr)

            when (tx.type) {
                MovementType.TRIP -> {
                    totalSpent += tx.amountCents
                    tripsCount++
                    dailyMap[dateStr] = (dailyMap[dateStr] ?: 0L) + tx.amountCents
                    monthlyMap[monthYearStr] = (monthlyMap[monthYearStr] ?: 0L) + tx.amountCents
                }
                MovementType.TOP_UP -> {
                    totalTopUp += tx.amountCents
                    topUpsCount++
                }
                else -> Unit
            }
        }

        val daysCount = if (distinctDays.isEmpty()) 1 else distinctDays.size
        val avgDaily = totalSpent / daysCount

        // Monthly projection based on current day of month
        val now = LocalDate.now(DateUtils.LIMA_ZONE)
        val currentDay = now.dayOfMonth
        val totalDaysInMonth = YearMonth.now(DateUtils.LIMA_ZONE).lengthOfMonth()
        val projectedMonthly = if (currentDay > 0) (totalSpent / currentDay) * totalDaysInMonth else totalSpent

        return ReportsData(
            totalSpentCents = totalSpent,
            totalTopUpCents = totalTopUp,
            totalTripsCount = tripsCount,
            totalTopUpsCount = topUpsCount,
            averageDailySpentCents = avgDaily,
            projectedMonthlySpentCents = projectedMonthly,
            monthlyComparison = monthlyMap.toList().takeLast(6),
            dailyExpenses = dailyMap.toList().takeLast(14)
        )
    }
}
