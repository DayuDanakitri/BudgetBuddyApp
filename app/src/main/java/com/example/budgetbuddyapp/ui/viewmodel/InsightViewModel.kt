package com.example.budgetbuddyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.example.budgetbuddyapp.data.database.AppDatabase
import com.example.budgetbuddyapp.data.repository.TransactionRepository
import com.example.budgetbuddyapp.utils.DateUtils

class InsightViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )

    val currentYearMonth = MutableLiveData(DateUtils.getCurrentYearMonth())

    val monthlyTransactions = currentYearMonth.switchMap { ym ->
        repo.getMonthlyTransactionsWithCategory(ym).asLiveData()
    }

    val monthlyIncome = currentYearMonth.switchMap { ym ->
        repo.getMonthlyIncome(ym).asLiveData()
    }

    val monthlyExpense = currentYearMonth.switchMap { ym ->
        repo.getMonthlyExpense(ym).asLiveData()
    }

    fun previousMonth() {
        val current = currentYearMonth.value ?: DateUtils.getCurrentYearMonth()
        val parts = current.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val newMonth = if (month == 1) 12 else month - 1
        val newYear = if (month == 1) year - 1 else year
        currentYearMonth.value = "%04d-%02d".format(newYear, newMonth)
    }

    fun nextMonth() {
        val current = currentYearMonth.value ?: DateUtils.getCurrentYearMonth()
        val parts = current.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val newMonth = if (month == 12) 1 else month + 1
        val newYear = if (month == 12) year + 1 else year
        currentYearMonth.value = "%04d-%02d".format(newYear, newMonth)
    }
}