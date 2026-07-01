package com.example.budgetbuddyapp.data.model

data class MonthlyReport(
    val yearMonth: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val transactionCount: Int,
    val topExpenseCategoryName: String?,
    val topExpenseCategoryEmoji: String?,
    val topExpenseCategoryAmount: Double?
) {
    val balance: Double
        get() = totalIncome - totalExpense
}