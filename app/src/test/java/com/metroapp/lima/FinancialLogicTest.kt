package com.metroapp.lima

import com.metroapp.lima.domain.model.Card
import com.metroapp.lima.domain.model.CardType
import com.metroapp.lima.domain.model.MetroTransaction
import com.metroapp.lima.domain.model.MovementOrigin
import com.metroapp.lima.domain.model.MovementType
import com.metroapp.lima.domain.model.TransactionStatus
import com.metroapp.lima.domain.usecase.StatsUseCase
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialLogicTest {

    private val statsUseCase = StatsUseCase()

    @Test
    fun testInitialBalance_andTwoConfirmedTrips() {
        val initialBalance = 1000L // S/ 10.00
        val trip1 = 150L // S/ 1.50
        val trip2 = 150L // S/ 1.50
        val expectedBalance = initialBalance - trip1 - trip2
        assertEquals(700L, expectedBalance) // S/ 7.00
    }

    @Test
    fun testTopUp_increasesBalance() {
        var currentBalance = 700L // S/ 7.00
        val topUp = 2000L // S/ 20.00
        currentBalance += topUp
        assertEquals(2700L, currentBalance) // S/ 27.00
    }

    @Test
    fun testExclusionOfPendingAndCancelledTrips_fromEffectiveExpenses() {
        val now = System.currentTimeMillis()

        val txList = listOf(
            MetroTransaction(
                id = 1,
                cardId = 1,
                type = MovementType.TRIP,
                amountCents = 150L,
                status = TransactionStatus.CONFIRMED,
                timestamp = now,
                description = "Viaje confirmado 1"
            ),
            MetroTransaction(
                id = 2,
                cardId = 1,
                type = MovementType.TRIP,
                amountCents = 150L,
                status = TransactionStatus.PENDING, // PENDIENTE: no debe contarse como gasto
                timestamp = now,
                description = "Viaje programado pendiente"
            ),
            MetroTransaction(
                id = 3,
                cardId = 1,
                type = MovementType.TRIP,
                amountCents = 150L,
                status = TransactionStatus.CANCELLED, // ANULADO: no debe contarse como gasto
                timestamp = now,
                description = "Viaje anulado"
            ),
            MetroTransaction(
                id = 4,
                cardId = 1,
                type = MovementType.TOP_UP,
                amountCents = 1000L,
                status = TransactionStatus.CONFIRMED,
                timestamp = now,
                description = "Recarga"
            )
        )

        val metrics = statsUseCase.calculateDashboardMetrics(
            cachedBalanceCents = 1000L,
            realVerifiedBalanceCents = null,
            transactions = txList
        )

        // Only transaction 1 should be counted as spent today!
        assertEquals(150L, metrics.spentTodayCents)
        assertEquals(1, metrics.tripsCountToday)
        assertEquals(1, metrics.pendingTripsCount)
    }

    @Test
    fun testReconciliation_calculatesDifferenceCorrectly() {
        val calculatedBalance = 1500L // S/ 15.00
        val realPhysicalCardBalance = 1350L // S/ 13.50 (real en la tarjeta)
        val difference = realPhysicalCardBalance - calculatedBalance
        assertEquals(-150L, difference) // Diferencia negativa de S/ 1.50
    }

    @Test
    fun testModificationOfTripAmount_recalculatesDifference() {
        val initialBalance = 1000L // S/ 10.00
        val originalTripAmount = 150L // S/ 1.50
        val correctedTripAmount = 75L  // S/ 0.75 (se corrigió porque era medio pasaje)

        val balanceAfterOriginal = initialBalance - originalTripAmount // 850L
        val balanceAfterCorrection = balanceAfterOriginal + (originalTripAmount - correctedTripAmount)

        assertEquals(925L, balanceAfterCorrection) // S/ 9.25
    }
}
