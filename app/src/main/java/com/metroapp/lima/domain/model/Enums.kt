package com.metroapp.lima.domain.model

enum class CardType(val displayName: String) {
    ADULT("Adulto"),
    SCHOOL("Escolar"),
    UNIVERSITY("Universitario / Instituto");

    companion object {
        fun fromString(value: String): CardType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ADULT
        }
    }
}

enum class MovementType(val displayName: String, val isDeduction: Boolean) {
    TRIP("Viaje en Metro", true),
    TOP_UP("Recarga de Saldo", false),
    POSITIVE_ADJUSTMENT("Ajuste Positivo", false),
    NEGATIVE_ADJUSTMENT("Ajuste Negativo", true),
    REVERSAL("Anulación / Reversión", false);

    companion object {
        fun fromString(value: String): MovementType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TRIP
        }
    }
}

enum class TransactionStatus(val displayName: String) {
    CONFIRMED("Confirmado"),
    PENDING("Pendiente"),
    CANCELLED("Anulado");

    companion object {
        fun fromString(value: String): TransactionStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CONFIRMED
        }
    }
}

enum class MovementOrigin(val displayName: String) {
    MANUAL("Manual"),
    AUTOMATIC("Automático");

    companion object {
        fun fromString(value: String): MovementOrigin {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MANUAL
        }
    }
}
