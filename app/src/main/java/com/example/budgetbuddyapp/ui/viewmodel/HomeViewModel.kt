package com.example.budgetbuddyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.example.budgetbuddyapp.data.database.AppDatabase
import com.example.budgetbuddyapp.data.repository.TransactionRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )

    val transactionsWithCategory = repo.getAllTransactionsWithCategory().asLiveData()
    val totalIncome = repo.getTotalIncome().asLiveData()
    val totalExpense = repo.getTotalExpense().asLiveData()
}