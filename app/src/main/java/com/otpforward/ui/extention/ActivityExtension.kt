package com.otpforward.ui.extention

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