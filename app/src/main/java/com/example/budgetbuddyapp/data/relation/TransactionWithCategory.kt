package com.example.budgetbuddyapp.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.data.model.Transaction

data class TransactionWithCategory(
    @Embedded val transaction: Transaction,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)