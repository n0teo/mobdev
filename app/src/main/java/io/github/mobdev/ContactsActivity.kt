package io.github.mobdev

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContactsActivity : AppCompatActivity() {

    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var statusText: TextView
    private lateinit var contactsList: RecyclerView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showContacts()
        } else {
            showStatus(R.string.no_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        statusText = findViewById(R.id.status_text)
        contactsList = findViewById(R.id.contacts_list)
        contactsList.layoutManager = LinearLayoutManager(this)
        contactsList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        if (hasPermission()) {
            showContacts()
        } else if (savedInstanceState == null) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            showStatus(R.string.no_permission)
        }
    }

    private fun hasPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun showContacts() {
        viewModel.loadContacts(this)
        val contacts = viewModel.contacts ?: emptyList()

        if (contacts.isEmpty()) {
            showStatus(R.string.no_contacts)
        } else {
            statusText.visibility = View.GONE
            contactsList.adapter = ContactsAdapter(contacts) { contact ->
                showDetails(contact)
            }
        }
    }

    private fun showStatus(textId: Int) {
        statusText.setText(textId)
        statusText.visibility = View.VISIBLE
    }

    private fun showDetails(contact: Contact) {
        val notSpecified = getString(R.string.not_specified)
        val phone = contact.phone ?: notSpecified
        val email = contact.email ?: notSpecified

        AlertDialog.Builder(this)
            .setTitle(contact.name)
            .setMessage(getString(R.string.contact_details, phone, email))
            .setPositiveButton(R.string.ok, null)
            .show()
    }
}