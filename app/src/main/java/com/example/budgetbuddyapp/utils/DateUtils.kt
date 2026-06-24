package com.example.budgetbuddyapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val locale = Locale("id", "ID")

    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", locale)
        return sdf.format(Date(millis))
    }

    fun formatDateFull(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", locale)
        return sdf.format(Date(millis))
    }

    fun formatDateHeader(millis: Long): String {
        val sdf = SimpleDateFormat("d MMM yyyy", locale)
        return sdf.format(Date(millis))
    }

    fun formatMonthYear(millis: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", locale)
        return sdf.format(Date(millis))
    }

    fun getYearMonth(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM", locale)
        return sdf.format(Date(millis))
    }

    fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", locale)
        return sdf.format(Date(millis1)) == sdf.format(Date(millis2))
    }

    fun getCurrentYearMonth(): String {
        return SimpleDateFormat("yyyy-MM", locale).format(Date())
    }
}