package com.otpforward.ui.viewmodel

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.otpforward.data.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ContactViewModel @Inject constructor(@ApplicationContext private val context: Context) : ViewModel() {

    private val _contactList = MutableLiveData<ArrayList<Contact>>()
    val contactList: LiveData<ArrayList<Contact>> get() = _contactList

    fun fetchAndDisplayContacts() {
        try {
            val contactsList = ArrayList<Contact>()
            val phoneNumbersSet = HashSet<String>()

            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use { phoneCursor ->
                while (phoneCursor.moveToNext()) {
                    val contactName = phoneCursor.getString(0)
                    val phoneNumber =
                        phoneCursor.getString(1).replace("^\\+91|\\s+|\\D+".toRegex(), "")
                    if (phoneNumber.length == 10 && phoneNumbersSet.add(phoneNumber)) {
                        contactsList.add(
                            Contact(
                                contactName, phoneNumber,
                            )
                        )
                    }
                }
            }

            _contactList.postValue(contactsList)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}