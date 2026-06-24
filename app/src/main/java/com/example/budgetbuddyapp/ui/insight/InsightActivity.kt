package com.example.budgetbuddyapp.ui.insight

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbuddyapp.databinding.ActivityInsightBinding
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import com.example.budgetbuddyapp.ui.viewmodel.InsightViewModel
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

class InsightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsightBinding
    private val viewModel: InsightViewModel by viewModels()
    private lateinit var adapter: CategoryPercentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = CategoryPercentAdapter()
        binding.rvCategoryPercent.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryPercent.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.currentYearMonth.observe(this) { ym ->
            val parts = ym.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
            val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
            binding.tvMonth.text = monthName
        }

        viewModel.monthlyIncome.observe(this) { income ->
            binding.tvTotalIncome.text = CurrencyFormatter.formatShort(income ?: 0.0)
        }

        viewModel.monthlyExpense.observe(this) { expense ->
            binding.tvTotalExpense.text = CurrencyFormatter.formatShort(expense ?: 0.0)
        }

        viewModel.monthlyTransactions.observe(this) { list ->
            updateCategoryPercents(list)
        }
    }

    private fun updateCategoryPercents(list: List<TransactionWithCategory>) {
        val isIncome = binding.btnIncome.isSelected
        val type = if (isIncome) "INCOME" else "EXPENSE"
        val filtered = list.filter { it.transaction.type == type }
        val total = filtered.sumOf { it.transaction.amount }

        val grouped = filtered.groupBy { it.category?.name ?: "Lainnya" }
        val items = grouped.map { (catName, txList) ->
            val catTotal = txList.sumOf { it.transaction.amount }
            val percent = if (total > 0) (catTotal / total * 100).toInt() else 0
            val emoji = txList.firstOrNull()?.category?.iconEmoji ?: "💸"
            CategoryPercentItem(emoji, catName, percent, catTotal)
        }.sortedByDescending { it.percent }

        adapter.submitList(items)

        val totalLabel = if (type == "INCOME") "Total Pemasukan" else "Total Pengeluaran"
        binding.tvTotalLabel.text = totalLabel
        binding.tvGrandTotal.text = CurrencyFormatter.formatShort(total)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPrev.setOnClickListener { viewModel.previousMonth() }
        binding.btnNext.setOnClickListener { viewModel.nextMonth() }

        binding.btnIncome.setOnClickListener {
            binding.btnIncome.isSelected = true
            binding.btnExpense.isSelected = false
            viewModel.monthlyTransactions.value?.let { updateCategoryPercents(it) }
        }

        binding.btnExpense.setOnClickListener {
            binding.btnIncome.isSelected = false
            binding.btnExpense.isSelected = true
            viewModel.monthlyTransactions.value?.let { updateCategoryPercents(it) }
        }

        // Default: expense
        binding.btnExpense.isSelected = true
        binding.btnIncome.isSelected = false
    }
}