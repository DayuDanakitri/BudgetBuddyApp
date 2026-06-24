package com.example.budgetbuddyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbuddyapp.data.database.AppDatabase
import com.example.budgetbuddyapp.data.model.Transaction
import com.example.budgetbuddyapp.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = TransactionRepository(
        AppDatabase.getInstance(application).transactionDao()
    )

    val allTransactions = repo.getAllTransactions().asLiveData()

    suspend fun getById(id: Int) = repo.getById(id)
    suspend fun getByIdWithCategory(id: Int) = repo.getByIdWithCategory(id)

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repo.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repo.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repo.delete(transaction)
    }
}