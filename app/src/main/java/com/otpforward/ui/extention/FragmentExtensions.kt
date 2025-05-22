package com.otpforward.ui.extention

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.replaceFragmentIfNeeded(fragment: Fragment, containerId: Int) {
    val currentFragment = parentFragmentManager.findFragmentById(containerId)
    if (currentFragment?.javaClass != fragment.javaClass) {
        replaceFragment(fragment, containerId, true)
    }
}

fun Fragment.replaceFragment(fragment: Fragment, containerId: Int, addToBackStack: Boolean) {
    val transaction = parentFragmentManager.beginTransaction()
    transaction.replace(containerId, fragment)
    if (addToBackStack) {
        transaction.addToBackStack(fragment.javaClass.name)
    }
    transaction.commit()
}

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.copyToClipboard(text: String) {
    val clipboardManager = requireContext().getSystemService(ClipboardManager::class.java)
    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, text))
}