package com.example.budgetbuddyapp.ui.transaction

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.databinding.ActivityDetailTransactionBinding
import com.example.budgetbuddyapp.ui.dialog.DeleteDialog
import com.example.budgetbuddyapp.ui.viewmodel.TransactionViewModel
import com.example.budgetbuddyapp.utils.Constants
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils
import kotlinx.coroutines.launch

class DetailTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTransactionBinding
    private val viewModel: TransactionViewModel by viewModels()
    private var transactionId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getIntExtra(Constants.EXTRA_TRANSACTION_ID, -1)
        if (transactionId == -1) { finish(); return }

        loadDetail()
        setupClickListeners()
    }

    private fun loadDetail() {
        lifecycleScope.launch {
            val twc = viewModel.getByIdWithCategory(transactionId) ?: return@launch
            val t = twc.transaction
            val cat = twc.category

            binding.tvEmoji.text = cat?.iconEmoji ?: "💸"
            binding.tvName.text = t.name.ifEmpty { cat?.name ?: "-" }

            val isIncome = t.type == "INCOME"
            binding.tvType.text = if (isIncome) "Pemasukan" else "Pengeluaran"
            binding.tvType.setTextColor(
                getColor(if (isIncome) R.color.income_green else R.color.expense_red)
            )
            binding.tvCategory.text = cat?.name ?: "-"

            val prefix = if (isIncome) "+" else "-"
            binding.tvAmount.text = "$prefix${CurrencyFormatter.formatShort(t.amount)}"
            binding.tvAmount.setTextColor(
                getColor(if (isIncome) R.color.income_green else R.color.expense_red)
            )

            binding.tvDate.text = DateUtils.formatDateFull(t.date)
            binding.tvDescription.text = t.description.ifEmpty { "-" }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditTransactionActivity::class.java)
            intent.putExtra(Constants.EXTRA_TRANSACTION_ID, transactionId)
            startActivity(intent)
            finish()
        }

        binding.btnDelete.setOnClickListener {
            DeleteDialog.show(this, onConfirm = {
                lifecycleScope.launch {
                    val t = viewModel.getById(transactionId)
                    t?.let { viewModel.delete(it) }
                    finish()
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        loadDetail()
    }
}