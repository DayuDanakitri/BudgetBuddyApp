package com.example.budgetbuddyapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.budgetbuddyapp.data.database.AppDatabase
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = CategoryRepository(
        AppDatabase.getInstance(application).categoryDao()
    )

    val allCategories = repo.getAllCategories().asLiveData()

    fun getCategoriesByType(type: String) = repo.getCategoriesByType(type).asLiveData()

    fun insert(category: Category) = viewModelScope.launch { repo.insert(category) }
    fun update(category: Category) = viewModelScope.launch { repo.update(category) }
    fun delete(category: Category) = viewModelScope.launch { repo.delete(category) }
}