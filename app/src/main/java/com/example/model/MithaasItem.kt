package com.example.model

import java.util.UUID

enum class MithaasType(
    val emoji: String,
    val displayName: String,
    val hindiName: String,
    val description: String,
    val points: Int,
    val colorCode: Long
) {
    LADOO("🟡", "Ladoo", "लड्डू", "Golden saffron chickpea sweet ball", 10, 0xFFFFB300),
    KAJU_KATLI("🤍", "Kaju Katli", "काजू कतली", "Silver-coated cashew diamond delight", 15, 0xFFE0E0E0),
    JALEBI("🌀", "Jalebi", "जलेबी", "Crispy orange fried sugary hot spiral", 20, 0xFFF4511E),
    GULAB_JAMUN("🟤", "Gulab Jamun", "गुलाब जामुन", "Delectable fried dough in sweet syrup", 25, 0xFF6D4C41),
    BARFI("🟩", "Barfi", "बर्फी", "Pista green dense condensed milk cube", 12, 0xFF43A047),
    RASGULLA("⚪", "Rasgulla", "रसगुल्ला", "Spongy white cheese dumpling in rich syrup", 18, 0xFFEEEEEE)
}

data class MithaasSweet(
    val id: String = UUID.randomUUID().toString(),
    val type: MithaasType
) {
    companion object {
        fun random(): MithaasSweet = MithaasSweet(type = MithaasType.values().random())
    }
}

data class BoardPosition(val row: Int, val col: Int) {
    fun isAdjacent(other: BoardPosition): Boolean {
        val rDiff = kotlin.math.abs(row - other.row)
        val cDiff = kotlin.math.abs(col - other.col)
         return (rDiff == 1 && cDiff == 0) || (rDiff == 0 && cDiff == 1)
    }
}
