package com.otpforward.ui.extention

import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


fun AppCompatActivity.replaceFragment(
    fragment: Fragment,
    containerId: Int,
    addToBackStack: Boolean,
) {
    val transaction = supportFragmentManager.beginTransaction()
    transaction.replace(containerId, fragment)
    if (addToBackStack) {
        transaction.addToBackStack(fragment.javaClass.name)
    }
    transaction.commit()
}

fun AppCompatActivity.replaceFragmentIfNeeded(fragment: Fragment, containerId: Int) {
    val currentFragment = supportFragmentManager.findFragmentById(containerId)
    if (currentFragment?.javaClass != fragment.javaClass) {
        replaceFragment(fragment, containerId, true)
    }
}

fun AppCompatActivity.hideSystemUI() {
    window.decorView.apply {
        systemUiVisibility =
            (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }
    window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN
    )
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}