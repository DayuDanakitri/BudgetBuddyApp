package com.example.budgetbuddyapp.ui.category

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.databinding.ActivityCategoryBinding
import com.example.budgetbuddyapp.ui.dialog.DeleteDialog
import com.example.budgetbuddyapp.ui.viewmodel.CategoryViewModel

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        android.util.Log.e("CATEGORY_DEBUG", "=== CategoryActivity onCreate ===")
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onEditClick = { category -> AddCategoryDialog.show(this, category, viewModel) },
            onDeleteClick = { category ->
                DeleteDialog.show(this, onConfirm = { viewModel.delete(category) })
            }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allCategories.observe(this) { categories ->
            android.util.Log.e("CATEGORY_DEBUG", "Jumlah kategori: ${categories.size}")
            adapter.submitList(categories)
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAdd.setOnClickListener {
            AddCategoryDialog.show(this, null, viewModel)
        }
    }
}