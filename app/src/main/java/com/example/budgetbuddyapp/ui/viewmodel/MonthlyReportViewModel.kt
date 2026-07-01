package com.example.budgetbuddyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.budgetbuddyapp.data.database.AppDatabase
import com.example.budgetbuddyapp.data.model.MonthlyReport
import com.example.budgetbuddyapp.data.repository.CategoryRepository
import com.example.budgetbuddyapp.data.repository.TransactionRepository
import com.example.budgetbuddyapp.utils.DateUtils
import kotlinx.coroutines.launch

class MonthlyReportViewModel(application: Application) : AndroidViewModel(application) {
    private val transactionRepo = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )
    private val categoryRepo = CategoryRepository(
        AppDatabase.getInstance(application).categoryDao()
    )

    val currentYearMonth = MutableLiveData(DateUtils.getCurrentYearMonth())

    private val monthlyIncome = currentYearMonth.switchMap { ym ->
        transactionRepo.getMonthlyIncome(ym).asLiveData()
    }

    private val monthlyExpense = currentYearMonth.switchMap { ym ->
        transactionRepo.getMonthlyExpense(ym).asLiveData()
    }

    private val monthlyCount = currentYearMonth.switchMap { ym ->
        transactionRepo.getMonthlyTransactionCount(ym).asLiveData()
    }

    val monthlyTransactions = currentYearMonth.switchMap { ym ->
        transactionRepo.getMonthlyTransactionsWithCategory(ym).asLiveData()
    }

    // Report gabungan — direkomputasi setiap kali salah satu sumber berubah
    val report = MediatorLiveData<MonthlyReport>()

    init {
        val recompute = {
            viewModelScope.launch {
                recomputeReport()
            }
        }
        report.addSource(currentYearMonth) { recompute() }
        report.addSource(monthlyIncome) { recompute() }
        report.addSource(monthlyExpense) { recompute() }
        report.addSource(monthlyCount) { recompute() }
    }

    private suspend fun recomputeReport() {
        val ym = currentYearMonth.value ?: return
        val income = monthlyIncome.value ?: 0.0
        val expense = monthlyExpense.value ?: 0.0
        val count = monthlyCount.value ?: 0

        val topExpense = transactionRepo.getTopExpenseCategoryForMonth(ym)
        var topName: String? = null
        var topEmoji: String? = null
        var topAmount: Double? = null

        if (topExpense != null) {
            topAmount = topExpense.totalAmount
            if (topExpense.categoryId != null) {
                val category = categoryRepo.getCategoryById(topExpense.categoryId)
                topName = category?.name ?: "Lainnya"
                topEmoji = category?.iconEmoji ?: "💸"
            } else {
                topName = "Lainnya"
                topEmoji = "💸"
            }
        }

        report.postValue(
            MonthlyReport(
                yearMonth = ym,
                totalIncome = income,
                totalExpense = expense,
                transactionCount = count,
                topExpenseCategoryName = topName,
                topExpenseCategoryEmoji = topEmoji,
                topExpenseCategoryAmount = topAmount
            )
        )
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