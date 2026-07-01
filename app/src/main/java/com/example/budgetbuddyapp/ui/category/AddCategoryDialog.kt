package com.example.budgetbuddyapp.ui.category

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.budgetbuddyapp.R
import com.example.budgetbuddyapp.data.model.Category
import com.example.budgetbuddyapp.databinding.DialogAddCategoryBinding
import com.example.budgetbuddyapp.ui.viewmodel.CategoryViewModel

object AddCategoryDialog {
    fun show(context: Context, category: Category?, viewModel: CategoryViewModel) {
        android.util.Log.e("CATEGORY_DEBUG", "Show dipanggil")
        try {
            val dialog = Dialog(context)
            val binding = DialogAddCategoryBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(binding.root)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(
                (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )

            val isEdit = category != null
            if (isEdit) {
                binding.tvTitle.text = "Edit Kategori"
                binding.etName.setText(category!!.name)
                binding.etEmoji.setText(category.iconEmoji)
                if (category.type == "INCOME") binding.rbIncome.isChecked = true
                else binding.rbExpense.isChecked = true
            } else {
                binding.tvTitle.text = "Tambah Kategori"
                binding.rbExpense.isChecked = true
            }

            binding.btnClose.setOnClickListener { dialog.dismiss() }
            binding.btnSave.setOnClickListener {
                val name = binding.etName.text.toString().trim()
                val emoji = binding.etEmoji.text.toString().trim()
                val type = if (binding.rbIncome.isChecked) "INCOME" else "EXPENSE"
                if (name.isEmpty()) {
                    binding.etName.error = "Nama tidak boleh kosong"
                    return@setOnClickListener
                }
                if (isEdit) {
                    viewModel.update(category!!.copy(name = name, iconEmoji = emoji, type = type))
                } else {
                    viewModel.insert(Category(name = name, iconEmoji = emoji, type = type))
                }
                dialog.dismiss()
            }

            dialog.show()
            android.util.Log.e("CATEGORY_DEBUG", "Dialog berhasil show")
        } catch (e: Exception) {
            android.util.Log.e("CATEGORY_DEBUG", "ERROR saat show dialog: ${e.message}")
            e.printStackTrace()
        }
    }
}