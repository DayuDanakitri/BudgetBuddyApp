package com.example.budgetbuddyapp.ui.transaction

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.databinding.ActivityEditTransactionBinding
import com.example.budgetbuddyapp.ui.transaction.ExitWithoutSavingDialog
import com.example.budgetbuddyapp.ui.viewmodel.CategoryViewModel
import com.example.budgetbuddyapp.ui.viewmodel.TransactionViewModel
import com.example.budgetbuddyapp.utils.Constants
import com.example.budgetbuddyapp.utils.CurrencyFormatter
import com.example.budgetbuddyapp.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class EditTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTransactionBinding
    private val transactionViewModel: TransactionViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private var transactionId: Int = -1
    private var selectedType = "EXPENSE"
    private var categoryList: List<Category> = emptyList()
    private var selectedDate: Long = System.currentTimeMillis()
    private var isFormatting = false
    private var hasChanges = false
    private var isDataLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionId = intent.getIntExtra(Constants.EXTRA_TRANSACTION_ID, -1)
        if (transactionId == -1) { finish(); return }

        setupBackPress()
        setupDatePicker()
        setupClickListeners()
        loadTransaction()
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBack()
            }
        })
    }

    private fun handleBack() {
        if (hasChanges) {
            ExitWithoutSavingDialog.show(
                this,
                onDiscard = { finish() },
                onContinue = { /* tetap di halaman */ }
            )
        } else {
            finish()
        }
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
                if (isDataLoaded) hasChanges = true
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        binding.tvDate.text = DateUtils.formatDate(selectedDate)
    }

    private fun loadTransaction() {
        lifecycleScope.launch {
            val twc = transactionViewModel.getByIdWithCategory(transactionId) ?: return@launch
            val t = twc.transaction

            selectedType = t.type
            selectedDate = t.date

            // Tampilkan label tipe (tidak bisa diubah saat edit)
            val isIncome = t.type == "INCOME"
            binding.tvTypeLabel.text = if (isIncome) "Pemasukan" else "Pengeluaran"
            binding.tvTypeLabel.setTextColor(
                getColor(if (isIncome) R.color.income_green else R.color.expense_red)
            )

            // Isi semua field SEBELUM pasang watcher
            binding.etAmount.setText(CurrencyFormatter.formatShort(t.amount))
            binding.etName.setText(t.name)
            binding.etDescription.setText(t.description)
            updateDateDisplay()

            // Isi spinner kategori
            categoryViewModel.allCategories.observe(this@EditTransactionActivity) { categories ->
                categoryList = categories
                val filtered = categories.filter { it.type == t.type }
                val labels = filtered.map { "${it.iconEmoji} ${it.name}" }
                val spinnerAdapter = ArrayAdapter(
                    this@EditTransactionActivity,
                    R.layout.item_spinner,
                    labels
                )
                spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
                binding.spinnerCategory.adapter = spinnerAdapter

                val idx = filtered.indexOfFirst { it.id == t.categoryId }
                if (idx >= 0) binding.spinnerCategory.setSelection(idx)

                // Pasang watcher baru setelah data selesai diisi
                if (!isDataLoaded) {
                    isDataLoaded = true
                    hasChanges = false
                    setupChangeWatchers()
                }
            }
        }
    }

    private fun setupChangeWatchers() {
        // Watcher untuk nama
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDataLoaded) hasChanges = true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Watcher untuk deskripsi
        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isDataLoaded) hasChanges = true
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Watcher untuk nominal (dengan formatter Rupiah)
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
                if (isDataLoaded) hasChanges = true
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { handleBack() }
        binding.btnSave.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
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
            Toast.makeText(this, "Tidak ada kategori tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedCategory = filtered[binding.spinnerCategory.selectedItemPosition]
        val description = binding.etDescription.text.toString().trim()

        lifecycleScope.launch {
            val existing = transactionViewModel.getById(transactionId) ?: return@launch
            val updated = existing.copy(
                categoryId = selectedCategory.id,
                amount = amount,
                name = name,
                description = description,
                date = selectedDate
            )
            transactionViewModel.update(updated)
            hasChanges = false
            Toast.makeText(
                this@EditTransactionActivity,
                "Perubahan disimpan",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}