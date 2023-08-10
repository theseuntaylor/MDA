package com.theseuntaylor.mda

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SetUpPersonalInformationActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var savePersonalInformationButton: Button

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up_personal_information)

        nameEditText = findViewById(R.id.editText_userName)
        ageEditText = findViewById(R.id.editText_userAge)
        savePersonalInformationButton = findViewById(R.id.button_saveUserInformation)

        sharedPreferences = this.getSharedPreferences(
            getString(R.string.shared_pref_file_name),
            Context.MODE_PRIVATE
        )

        val name = sharedPreferences.getString(getString(R.string.user_name_key), null)
        val age = sharedPreferences.getString(getString(R.string.age_key), null)

        val needToAddData: Boolean = name.isNullOrEmpty() && age.isNullOrEmpty()

        if (!needToAddData) {
            nameEditText.apply {
                isEnabled = false
                setText(name)
            }

            ageEditText.apply {
                isEnabled = false
                setText(age)
            }

            savePersonalInformationButton.visibility = GONE
        }

        savePersonalInformationButton.setOnClickListener {
            saveUserInformation(
                name = nameEditText.text.toString(),
                age = ageEditText.text.toString()
            )
        }
    }

    private fun saveUserInformation(name: String, age: String) {
        if (name.isNotEmpty() && age.isNotEmpty()) {
            with(sharedPreferences.edit()) {
                putString(getString(R.string.user_name_key), name)
                putString(getString(R.string.age_key), age)
                apply()
            }
        } else Toast.makeText(this, "Please put your name and age", Toast.LENGTH_LONG).show()
    }
}