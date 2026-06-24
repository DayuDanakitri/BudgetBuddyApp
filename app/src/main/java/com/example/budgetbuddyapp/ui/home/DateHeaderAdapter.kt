package com.example.budgetbuddyapp.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.budgetbuddyapp.databinding.ItemDateHeaderBinding
import com.example.budgetbuddyapp.utils.DateUtils

class DateHeaderAdapter(private val dates: List<Long>) :
    RecyclerView.Adapter<DateHeaderAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemDateHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dateMillis: Long) {
            binding.tvDateHeader.text = DateUtils.formatDateHeader(dateMillis)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount(): Int = dates.size
}