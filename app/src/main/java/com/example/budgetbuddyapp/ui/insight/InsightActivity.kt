package com.example.budgetbuddyapp.ui.insight

import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbuddyapp.databinding.ActivityInsightBinding
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import com.example.budgetbuddyapp.ui.viewmodel.InsightViewModel
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.SimpleDateFormat
import java.util.*

class InsightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInsightBinding
    private val viewModel: InsightViewModel by viewModels()
    private lateinit var adapter: CategoryPercentAdapter

    private val pieColors = listOf(
        Color.parseColor("#518796"), // accent_blue
        Color.parseColor("#7DB38C"), // income_green
        Color.parseColor("#B37D7D"), // expense_red
        Color.parseColor("#8E7DBF"), // ungu
        Color.parseColor("#BFA97D"), // coklat emas
        Color.parseColor("#7DADB3"), // biru muda
        Color.parseColor("#B3977D"), // oranye muted
        Color.parseColor("#A0B37D"), // hijau muda
        Color.parseColor("#B37DAD"), // pink muted
        Color.parseColor("#7D8EB3"), // biru slate
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupPieChart()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = CategoryPercentAdapter()
        binding.rvCategoryPercent.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryPercent.adapter = adapter
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 52f
            transparentCircleRadius = 57f
            setHoleColor(Color.parseColor("#222222"))       // bg_card
            setTransparentCircleColor(Color.parseColor("#222222"))
            setTransparentCircleAlpha(80)
            setUsePercentValues(true)
            setDrawEntryLabels(false)                        // label di RecyclerView sudah cukup
            legend.isEnabled = false
            setNoDataText("Tidak ada transaksi")
            setNoDataTextColor(Color.parseColor("#555555"))
            animateY(800)
        }
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

        // Update label total
        binding.tvTotalLabel.text = if (type == "INCOME") "Total Pemasukan" else "Total Pengeluaran"
        binding.tvGrandTotal.text = CurrencyFormatter.formatShort(total)

        // Update pie chart
        updatePieChart(items, total)
    }

    private fun updatePieChart(items: List<CategoryPercentItem>, total: Double) {
        if (items.isEmpty() || total == 0.0) {
            binding.pieChart.visibility = android.view.View.GONE
            binding.tvChartEmpty.visibility = android.view.View.VISIBLE
            return
        }

        binding.pieChart.visibility = android.view.View.VISIBLE
        binding.tvChartEmpty.visibility = android.view.View.GONE

        val entries = items.mapIndexed { _, item ->
            PieEntry(item.amount.toFloat(), item.name)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = items.mapIndexed { index, _ ->
                pieColors[index % pieColors.size]
            }
            sliceSpace = 2f
            selectionShift = 6f
            valueFormatter = PercentFormatter(binding.pieChart)
            valueTextColor = Color.WHITE
            valueTextSize = 11f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(binding.pieChart))
        }

        binding.pieChart.apply {
            data = pieData
            // Center text: total
            centerText = CurrencyFormatter.formatShort(total)
            setCenterTextColor(Color.WHITE)
            setCenterTextSize(13f)
            invalidate()
            animateY(600)
        }
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
        
        binding.btnExpense.isSelected = true
        binding.btnIncome.isSelected = false

        binding.btnViewMonthlyReport.setOnClickListener {
            startActivity(android.content.Intent(this, com.example.budgetbuddyapp.ui.report.MonthlyReportActivity::class.java))
        }
    }
}