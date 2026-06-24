package com.example.budgetbuddyapp.ui.transaction

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.example.budgetbuddyapp.databinding.DialogExitWithoutSaveBinding

object ExitWithoutSavingDialog {
    fun show(context: Context, onDiscard: () -> Unit, onContinue: () -> Unit) {
        val dialog = Dialog(context)
        val binding = DialogExitWithoutSaveBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding.btnDiscard.setOnClickListener {
            dialog.dismiss()
            onDiscard()
        }
        binding.btnContinueEdit.setOnClickListener {
            dialog.dismiss()
            onContinue()
        }

        dialog.show()
    }
}