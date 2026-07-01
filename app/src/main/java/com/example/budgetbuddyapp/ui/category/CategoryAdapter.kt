package com.example.budgetbuddyapp.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: Category) {
            android.util.Log.e("CATEGORY_DEBUG", "Binding kategori: ${category.name}")
            binding.tvEmoji.text = category.iconEmoji
            binding.tvName.text = category.name
            binding.tvType.text = if (category.type == "INCOME") "Pemasukan" else "Pengeluaran"
            binding.tvType.setTextColor(
                binding.root.context.getColor(
                    if (category.type == "INCOME") R.color.income_green else R.color.expense_red
                )
            )
            binding.btnEdit.setOnClickListener {
                android.util.Log.e("CATEGORY_DEBUG", "EDIT DIKLIK: ${category.name}")
                onEditClick(category)
            }
            binding.btnDelete.setOnClickListener {
                android.util.Log.e("CATEGORY_DEBUG", "DELETE DIKLIK: ${category.name}")
                onDeleteClick(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        binding.root.isClickable = false
        binding.root.isFocusable = false
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}