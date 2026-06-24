package com.example.budgetbuddyapp.data.repository

import com.example.budgetbuddyapp.data.database.CategoryDao
import com.example.budgetbuddyapp.data.model.Category

class CategoryRepository(private val dao: CategoryDao) {
    fun getAllCategories() = dao.getAllCategories()
    fun getCategoriesByType(type: String) = dao.getCategoriesByType(type)
    suspend fun getCategoryById(id: Int) = dao.getCategoryById(id)
    suspend fun insert(category: Category) = dao.insert(category)
    suspend fun update(category: Category) = dao.update(category)
    suspend fun delete(category: Category) = dao.delete(category)
}