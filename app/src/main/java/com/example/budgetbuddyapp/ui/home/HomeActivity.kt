package com.example.budgetbuddyapp.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.relation.TransactionWithCategory
import com.example.budgetbuddyapp.databinding.ActivityHomeBinding
import com.example.budgetbuddyapp.ui.category.CategoryActivity
import com.example.budgetbuddyapp.ui.insight.InsightActivity
import com.example.budgetbuddyapp.ui.transaction.AddTransactionActivity
import com.example.budgetbuddyapp.ui.transaction.DetailTransactionActivity
import com.example.budgetbuddyapp.ui.viewmodel.HomeViewModel
import com.example.budgetbuddyapp.utils.Constants
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
        setupObservers()
        setupBottomNav()
    }

    private fun setupRecyclerView() {
        adapter = HomeAdapter { twc ->
            val intent = Intent(this, DetailTransactionActivity::class.java)
            intent.putExtra(Constants.EXTRA_TRANSACTION_ID, twc.transaction.id)
            startActivity(intent)
        }
        binding.rvTransactions.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.transactionsWithCategory.observe(this) { list ->
            val items = buildListItems(list)
            adapter.submitList(items)
            binding.tvEmpty.visibility =
                if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.totalIncome.observe(this) { income ->
            binding.tvTotalIncome.text = CurrencyFormatter.formatShort(income ?: 0.0)
        }

        viewModel.totalExpense.observe(this) { expense ->
            binding.tvTotalExpense.text = CurrencyFormatter.formatShort(expense ?: 0.0)
            updateBalance()
        }

        viewModel.totalIncome.observe(this) { updateBalance() }
    }

    private fun updateBalance() {
        val income = viewModel.totalIncome.value ?: 0.0
        val expense = viewModel.totalExpense.value ?: 0.0
        binding.tvBalance.text = CurrencyFormatter.formatShort(income - expense)
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

    private fun setupBottomNav() {
        binding.btnNavHome.setOnClickListener { /* already here */ }
        binding.btnNavAdd.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }
        binding.btnNavCategory.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }
        binding.btnNavInsight.setOnClickListener {
            startActivity(Intent(this, InsightActivity::class.java))
        }
    }
}