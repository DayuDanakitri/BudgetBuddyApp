package com.example.budgetbuddyapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.budgetbuddyapp.data.model.Transaction as TransactionModel
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionModel)

    @Update
    suspend fun update(transaction: TransactionModel)

    @Delete
    suspend fun delete(transaction: TransactionModel)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactionsWithCategory(): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionModel>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Int): TransactionModel?

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getByIdWithCategory(id: Int): TransactionWithCategory?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND strftime('%Y-%m', date/1000, 'unixepoch') = :yearMonth")
    fun getMonthlyIncome(yearMonth: String): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND strftime('%Y-%m', date/1000, 'unixepoch') = :yearMonth")
    fun getMonthlyExpense(yearMonth: String): Flow<Double?>

    @Transaction
    @Query("SELECT * FROM transactions WHERE strftime('%Y-%m', date/1000, 'unixepoch') = :yearMonth ORDER BY date DESC")
    fun getMonthlyTransactionsWithCategory(yearMonth: String): Flow<List<TransactionWithCategory>>
}