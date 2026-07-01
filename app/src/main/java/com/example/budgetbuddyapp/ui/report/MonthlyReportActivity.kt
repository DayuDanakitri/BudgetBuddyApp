package com.example.budgetbuddyapp.ui.report

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.budgetbuddyapp.data.model.MonthlyReport
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import com.example.budgetbuddyapp.databinding.ActivityMonthlyReportBinding
import com.example.budgetbuddyapp.ui.home.HomeAdapter
import com.example.budgetbuddyapp.ui.home.HomeListItem
import com.example.budgetbuddyapp.ui.transaction.DetailTransactionActivity
import com.example.budgetbuddyapp.ui.viewmodel.MonthlyReportViewModel
import com.example.budgetbuddyapp.utils.Constants
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils
import com.example.budgetbuddyapp.utils.PdfReportGenerator
import java.text.SimpleDateFormat
import java.util.*

class MonthlyReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonthlyReportBinding
    private val viewModel: MonthlyReportViewModel by viewModels()
    private lateinit var adapter: HomeAdapter

    private var latestReport: MonthlyReport? = null
    private var latestTransactions: List<TransactionWithCategory> = emptyList()

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            exportPdf()
        } else {
            android.widget.Toast.makeText(
                this,
                "Izin penyimpanan diperlukan untuk export PDF",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonthlyReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        adapter = HomeAdapter { twc ->
            val intent = android.content.Intent(this, DetailTransactionActivity::class.java)
            intent.putExtra(Constants.EXTRA_TRANSACTION_ID, twc.transaction.id)
            startActivity(intent)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.currentYearMonth.observe(this) { ym ->
            val parts = ym.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, 1)
            val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(cal.time)
            binding.tvMonth.text = monthName
        }

        viewModel.report.observe(this) { report ->
            latestReport = report
            bindReport(report)
        }

        viewModel.monthlyTransactions.observe(this) { list ->
            latestTransactions = list
            val items = buildListItems(list)
            adapter.submitList(items)
            binding.rvTransactions.visibility =
                if (list.isEmpty()) android.view.View.GONE else android.view.View.VISIBLE
            binding.tvEmpty.visibility =
                if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun bindReport(report: MonthlyReport) {
        binding.tvBalance.text = CurrencyFormatter.formatShort(report.balance)
        binding.tvIncome.text = CurrencyFormatter.formatShort(report.totalIncome)
        binding.tvExpense.text = CurrencyFormatter.formatShort(report.totalExpense)
        binding.tvTransactionCount.text = report.transactionCount.toString()

        if (report.topExpenseCategoryName != null) {
            binding.tvTopCategory.text =
                "${report.topExpenseCategoryEmoji} ${report.topExpenseCategoryName}"
            binding.tvTopCategoryAmount.text =
                CurrencyFormatter.formatShort(report.topExpenseCategoryAmount ?: 0.0)
        } else {
            binding.tvTopCategory.text = "-"
            binding.tvTopCategoryAmount.text = ""
        }
    }

    private fun buildListItems(list: List<TransactionWithCategory>): List<HomeListItem> {
        val result = mutableListOf<HomeListItem>()
        var lastDate = -1L
        for (twc in list) {
            val date = twc.transaction.date
            if (lastDate == -1L || !DateUtils.isSameDay(date, lastDate)) {
                result.add(HomeListItem.Header(date))
                lastDate = date
            }
            result.add(HomeListItem.TransactionItem(twc))
        }
        return result
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnPrev.setOnClickListener { viewModel.previousMonth() }
        binding.btnNext.setOnClickListener { viewModel.nextMonth() }
        binding.btnExport.setOnClickListener { handleExportClick() }
    }

    private fun handleExportClick() {
        if (latestReport == null) {
            android.widget.Toast.makeText(
                this,
                "Data belum siap, coba lagi",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (latestTransactions.isEmpty()) {
            android.widget.Toast.makeText(
                this,
                "Tidak ada transaksi di bulan ini",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Android 10+ tidak butuh permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportPdf()
            return
        }

        // Android 9 ke bawah: cek & minta permission
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        when {
            ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                exportPdf()
            }
            else -> {
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    private fun exportPdf() {
        val report = latestReport ?: return

        binding.btnExport.isEnabled = false
        android.widget.Toast.makeText(
            this,
            "Membuat PDF...",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        val result = PdfReportGenerator.generate(
            context = this,
            report = report,
            transactions = latestTransactions
        )

        binding.btnExport.isEnabled = true

        when (result) {
            is PdfReportGenerator.ExportResult.Success -> {
                android.widget.Toast.makeText(
                    this,
                    "✅ PDF disimpan di ${result.displayPath}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            is PdfReportGenerator.ExportResult.Error -> {
                android.widget.Toast.makeText(
                    this,
                    "❌ Gagal: ${result.message}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}