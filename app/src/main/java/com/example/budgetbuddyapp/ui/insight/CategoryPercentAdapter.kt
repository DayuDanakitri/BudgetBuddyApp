package com.example.budgetbuddyapp.ui.insight

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddyapp.databinding.ItemCategoryPercentBinding
import com.example.budgetbuddyapp.utils.CurrencyFormatter

data class CategoryPercentItem(
    val emoji: String,
    val name: String,
    val percent: Int,
    val amount: Double
)

class CategoryPercentAdapter :
    ListAdapter<CategoryPercentItem, CategoryPercentAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCategoryPercentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryPercentItem) {
            binding.tvEmoji.text = item.emoji
            binding.tvName.text = item.name
            binding.tvPercent.text = "${item.percent}%"
            binding.tvAmount.text = CurrencyFormatter.formatShort(item.amount)
            binding.progressBar.progress = item.percent
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryPercentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<CategoryPercentItem>() {
        override fun areItemsTheSame(oldItem: CategoryPercentItem, newItem: CategoryPercentItem) = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: CategoryPercentItem, newItem: CategoryPercentItem) = oldItem == newItem
    }
}