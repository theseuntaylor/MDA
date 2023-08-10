package com.theseuntaylor.mda

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SetUpTrustedContactActivity : AppCompatActivity() {

    private lateinit var trustedContactNameEditText: EditText
    private lateinit var trustedContactNumberEditText: EditText
    private lateinit var saveTrustedContactInformationButton: Button

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_trusted_contact)

        trustedContactNameEditText = findViewById(R.id.editText_trustedContactName)
        trustedContactNumberEditText = findViewById(R.id.editText_trustedContactNumber)
        saveTrustedContactInformationButton =
            findViewById(R.id.button_saveTrustedContactInformation)

        sharedPreferences = this.getSharedPreferences(
            getString(R.string.shared_pref_file_name),
            Context.MODE_PRIVATE
        )

        val name = sharedPreferences.getString(getString(R.string.trusted_contact_name_key), null)
        val phoneNumber =
            sharedPreferences.getString(getString(R.string.trusted_contact_number_key), null)

        val needToAddData: Boolean = name.isNullOrEmpty() && phoneNumber.isNullOrEmpty()

        if (!needToAddData) {
            trustedContactNameEditText.apply {
                isEnabled = false
                setText(name)
            }

            trustedContactNumberEditText.apply {
                isEnabled = false
                setText(phoneNumber)
            }

            saveTrustedContactInformationButton.visibility = View.GONE
        }

        saveTrustedContactInformationButton.setOnClickListener {
            saveTrustedContactInformation(
                name = trustedContactNameEditText.text.toString(),
                phoneNumber = trustedContactNumberEditText.text.toString()
            )
        }

    }

    private fun saveTrustedContactInformation(name: String, phoneNumber: String) {
        if (name.isNotEmpty() && phoneNumber.isNotEmpty()) {
            if (phoneNumber.length < 11) {
                Toast.makeText(
                    this,
                    "Please put in a valid phone number.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                with(sharedPreferences.edit()) {
                    putString(getString(R.string.trusted_contact_name_key), name)
                    putString(getString(R.string.trusted_contact_number_key), phoneNumber)
                    apply()
                }
            }
        } else Toast.makeText(
            this,
            "Please put the name and phone number of a trusted contact.",
            Toast.LENGTH_LONG
        ).show()

    }
}