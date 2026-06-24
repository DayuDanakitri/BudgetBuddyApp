package com.example.budgetbuddyapp.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val locale = Locale("id", "ID")

    fun format(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    fun formatShort(amount: Double): String {
        return "Rp ${NumberFormat.getNumberInstance(locale).apply { maximumFractionDigits = 0 }.format(amount)}"
    }

    fun parseAmount(text: String): Double {
        return text.replace("[^0-9]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }
}