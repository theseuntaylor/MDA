package com.theseuntaylor.mda

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_menu)

        setUpPersonalInformationCardView = findViewById(R.id.cardView_setUpPersonalInformation)
        setUpTrustedContactCardView = findViewById(R.id.cardView_setUpTrustedContact)
        deletePersonalInformationCardView = findViewById(R.id.cardView_deleteAllInformation)

        setUpPersonalInformationCardView.setOnClickListener {
            val intent = Intent(this, SetUpPersonalInformationActivity::class.java)
            startActivity(intent)
        }
        setUpTrustedContactCardView.setOnClickListener {
            val intent = Intent(this, SetUpTrustedContantActivity::class.java)
            startActivity(intent)
        }

        // this would show a dialog, to confirm if they are sure about deleting their information.
        deletePersonalInformationCardView.setOnClickListener {
            val intent = Intent(this, SetUpPersonalInformationActivity::class.java)
            startActivity(intent)
        }
    }
}