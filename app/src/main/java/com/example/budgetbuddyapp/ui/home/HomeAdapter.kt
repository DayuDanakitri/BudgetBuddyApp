package com.example.budgetbuddyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import com.example.budgetbuddyapp.databinding.ItemDateHeaderBinding
import com.example.budgetbuddyapp.databinding.ItemTransactionBinding
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils

sealed class HomeListItem {
    data class Header(val dateMillis: Long) : HomeListItem()
    data class TransactionItem(val data: TransactionWithCategory) : HomeListItem()
}

class HomeAdapter(
    private val onItemClick: (TransactionWithCategory) -> Unit
) : ListAdapter<HomeListItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is HomeListItem.Header -> TYPE_HEADER
            is HomeListItem.TransactionItem -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TransactionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is HomeListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is HomeListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item.data)
        }
    }

    inner class HeaderViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HomeListItem.Header) {
            binding.tvDateHeader.text = DateUtils.formatDateHeader(item.dateMillis)
        }
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionWithCategory) {
            val t = item.transaction
            val cat = item.category

            binding.tvEmoji.text = cat?.iconEmoji ?: "💸"
            binding.tvName.text = t.name.ifEmpty { cat?.name ?: "-" }
            binding.tvCategory.text = cat?.name ?: "-"

            val isIncome = t.type == "INCOME"
            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = "$prefix${CurrencyFormatter.formatShort(t.amount)}"
            binding.tvAmount.setTextColor(
                binding.root.context.getColor(
                    if (isIncome) R.color.income_green else R.color.expense_red
                )
            )

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HomeListItem>() {
        override fun areItemsTheSame(oldItem: HomeListItem, newItem: HomeListItem): Boolean {
            return when {
                oldItem is HomeListItem.Header && newItem is HomeListItem.Header ->
                    oldItem.dateMillis == newItem.dateMillis
                oldItem is HomeListItem.TransactionItem && newItem is HomeListItem.TransactionItem ->
                    oldItem.data.transaction.id == newItem.data.transaction.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: HomeListItem, newItem: HomeListItem) =
            oldItem == newItem
    }
}