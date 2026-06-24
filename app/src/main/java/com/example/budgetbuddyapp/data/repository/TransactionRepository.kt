package com.example.budgetbuddyapp.data.repository

import com.example.budgetbuddyapp.data.database.TransactionDao
import com.example.budgetbuddyapp.data.model.Transaction

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactionsWithCategory() = dao.getAllTransactionsWithCategory()
    fun getAllTransactions() = dao.getAllTransactions()
    fun getTotalIncome() = dao.getTotalIncome()
    fun getTotalExpense() = dao.getTotalExpense()
    fun getMonthlyIncome(yearMonth: String) = dao.getMonthlyIncome(yearMonth)
    fun getMonthlyExpense(yearMonth: String) = dao.getMonthlyExpense(yearMonth)
    fun getMonthlyTransactionsWithCategory(yearMonth: String) = dao.getMonthlyTransactionsWithCategory(yearMonth)
    suspend fun getById(id: Int) = dao.getById(id)
    suspend fun getByIdWithCategory(id: Int) = dao.getByIdWithCategory(id)
    suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    suspend fun update(transaction: Transaction) = dao.update(transaction)
    suspend fun delete(transaction: Transaction) = dao.delete(transaction)
}