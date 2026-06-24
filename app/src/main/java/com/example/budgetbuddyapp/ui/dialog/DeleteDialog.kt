package com.example.budgetbuddyapp.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.budgetbuddyapp.databinding.DialogDeleteBinding

object DeleteDialog {
    fun show(context: Context, onConfirm: () -> Unit) {
        val dialog = Dialog(context)
        val binding = DialogDeleteBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.85).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )

        binding.btnCancel.setOnClickListener { dialog.dismiss() }
        binding.btnDelete.setOnClickListener {
            dialog.dismiss()
            onConfirm()
        }

        dialog.show()
    }
}