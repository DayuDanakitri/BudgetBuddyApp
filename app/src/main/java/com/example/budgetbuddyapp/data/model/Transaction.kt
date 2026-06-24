package com.example.budgetbuddyapp.data.model

import androidx.room.*

@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("categoryId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoryId: Int?,
    val amount: Double,
    val name: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: String  // "INCOME" atau "EXPENSE"
)