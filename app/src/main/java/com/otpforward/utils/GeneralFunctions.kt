package com.otpforward.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.otpforward.databinding.DialogDeleteConfirmationBinding

object GeneralFunctions {

    fun getAppVersion(context: Context): String {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return packageInfo.versionName
    }

    fun showDeleteConfirmation(context: Context, onAction: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        val binding = DialogDeleteConfirmationBinding.inflate(LayoutInflater.from(context))
        builder.setView(binding.root)

        val dialog = builder.create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        val margin = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._16sdp)
        dialog.window?.decorView?.setPadding(margin, 0, margin, 0)

        binding.confirmDelete.setOnClickListener {
            onAction()
            dialog.dismiss()
        }
        binding.cancelDelete.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}