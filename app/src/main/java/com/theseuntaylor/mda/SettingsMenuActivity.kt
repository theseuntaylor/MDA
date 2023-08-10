package com.theseuntaylor.mda

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView

/** This class is the 'interface' to all the other possible settings that a user can do
 * This screen leads you to one of three options at any time.
 * 1. Setting up the personal information for the user.
 * 2. Setting up the information of the trusted contact.
 * 3. Deleting user information and data.
 */
class SettingsMenuActivity : AppCompatActivity() {

    private lateinit var setUpPersonalInformationCardView: CardView
    private lateinit var setUpTrustedContactCardView: CardView
    private lateinit var deletePersonalInformationCardView: CardView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_menu)

        setUpPersonalInformationCardView = findViewById(R.id.cardView_setUpPersonalInformation)
        setUpTrustedContactCardView = findViewById(R.id.cardView_setUpTrustedContact)
        deletePersonalInformationCardView = findViewById(R.id.cardView_deleteAllInformation)

        sharedPreferences = this.getSharedPreferences(
            getString(R.string.shared_pref_file_name),
            Context.MODE_PRIVATE
        )

        setUpPersonalInformationCardView.setOnClickListener {
            val intent = Intent(this, SetUpPersonalInformationActivity::class.java)
            startActivity(intent)
        }
        setUpTrustedContactCardView.setOnClickListener {
            val intent = Intent(this, SetUpTrustedContactActivity::class.java)
            startActivity(intent)
        }

        // this would show a dialog, to confirm if they are sure about deleting their information.
        deletePersonalInformationCardView.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(R.string.delete_information)
                setMessage(R.string.are_you_sure)
                setPositiveButton("Yes") { _, _ ->
                    with(sharedPreferences.edit()) {
                        remove(getString(R.string.user_name_key))
                        remove(getString(R.string.age_key))
                        remove(getString(R.string.trusted_contact_name_key))
                        remove(getString(R.string.trusted_contact_number_key))
                        apply()
                    }
                    Toast.makeText(
                        this@SettingsMenuActivity,
                        "All information has been successfully deleted!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }.create().show()
        }
    }
}