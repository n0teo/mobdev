package io.github.mobdev

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    var contacts: List<Contact>? = null
        private set

    fun loadContacts(context: Context) {
        if (contacts != null) return
        contacts = readContacts(context)
    }

    private fun readContacts(context: Context): List<Contact> {
        val emails = readEmails(context)
        val result = mutableListOf<Contact>()

        val cursor = context.contentResolver.query(
            Phone.CONTENT_URI, null, null, null, Phone.DISPLAY_NAME
        ) ?: return result

        val idColumn = cursor.getColumnIndexOrThrow(Phone.CONTACT_ID)
        val nameColumn = cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME)
        val phoneColumn = cursor.getColumnIndexOrThrow(Phone.NUMBER)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val name = cursor.getString(nameColumn)
            val phone = cursor.getString(phoneColumn)
            if (name != null) {
                result.add(Contact(name, phone, emails[id]))
            }
        }
        cursor.close()
        return result
    }

    private fun readEmails(context: Context): Map<String, String> {
        val emails = mutableMapOf<String, String>()

        val cursor = context.contentResolver.query(
            Email.CONTENT_URI, null, null, null, null
        ) ?: return emails

        val idColumn = cursor.getColumnIndexOrThrow(Email.CONTACT_ID)
        val emailColumn = cursor.getColumnIndexOrThrow(Email.ADDRESS)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val email = cursor.getString(emailColumn)
            emails[id] = email
        }
        cursor.close()
        return emails
    }
}