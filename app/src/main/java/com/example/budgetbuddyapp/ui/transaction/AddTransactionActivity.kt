package com.example.budgetbuddyapp.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.data.model.Transaction
import com.example.budgetbuddyapp.databinding.ActivityAddTransactionBinding
import com.example.budgetbuddyapp.ui.viewmodel.CategoryViewModel
import com.example.budgetbuddyapp.ui.viewmodel.TransactionViewModel
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private var selectedType = "EXPENSE"
    private var categoryList: List<Category> = emptyList()
    private var selectedDate: Long = System.currentTimeMillis()
    private var isFormatting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTypeToggle()
        setupDatePicker()
        setupAmountFormatter()
        setupObservers()
        setupClickListeners()
        updateDateDisplay()
    }

    private fun setupTypeToggle() {
        updateTypeUI("EXPENSE")
        binding.btnIncome.setOnClickListener {
            selectedType = "INCOME"
            updateTypeUI("INCOME")
        }
        binding.btnExpense.setOnClickListener {
            selectedType = "EXPENSE"
            updateTypeUI("EXPENSE")
        }
    }

    private fun updateTypeUI(type: String) {
        selectedType = type
        val isIncome = type == "INCOME"
        binding.btnIncome.isSelected = isIncome
        binding.btnExpense.isSelected = !isIncome
        filterCategories()
    }

    private fun setupDatePicker() {
        binding.tvDate.setOnClickListener { showDatePicker() }
        binding.ivCalendar.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        DatePickerDialog(
            this,
            R.style.DatePickerTheme,
            { _, year, month, day ->
                val newCal = Calendar.getInstance()
                newCal.set(year, month, day)
                selectedDate = newCal.timeInMillis
                updateDateDisplay()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        binding.tvDate.text = DateUtils.formatDate(selectedDate)
    }

    private fun setupAmountFormatter() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val raw = s.toString().replace("[^0-9]".toRegex(), "")
                val amount = raw.toDoubleOrNull() ?: 0.0
                val formatted = if (amount > 0) CurrencyFormatter.formatShort(amount) else ""
                binding.etAmount.setText(formatted)
                binding.etAmount.setSelection(formatted.length)
                isFormatting = false
            }
        })
    }

    private fun setupObservers() {
        categoryViewModel.allCategories.observe(this) { categories ->
            categoryList = categories
            filterCategories()
        }
    }

    private fun filterCategories() {
        val filtered = categoryList.filter { it.type == selectedType }
        val labels = filtered.map { "${it.iconEmoji} ${it.name}" }
        val adapter = ArrayAdapter(this, R.layout.item_spinner, labels)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSave.setOnClickListener { saveTransaction() }
    }

    private fun saveTransaction() {
        val raw = binding.etAmount.text.toString().replace("[^0-9]".toRegex(), "")
        val amount = raw.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Masukkan jumlah yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etName.text.toString().trim()
        if (name.isEmpty()) {
            binding.etName.error = "Nama transaksi wajib diisi"
            return
        }

        val filtered = categoryList.filter { it.type == selectedType }
        if (filtered.isEmpty()) {
            Toast.makeText(this, "Belum ada kategori, tambah dulu di Kelola Kategori", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = filtered[binding.spinnerCategory.selectedItemPosition]
        val description = binding.etDescription.text.toString().trim()

        val transaction = Transaction(
            categoryId = selectedCategory.id,
            amount = amount,
            name = name,
            description = description,
            date = selectedDate,
            type = selectedType
        )
        transactionViewModel.insert(transaction)
        Toast.makeText(this, "Transaksi disimpan", Toast.LENGTH_SHORT).show()
        finish()
    }
}