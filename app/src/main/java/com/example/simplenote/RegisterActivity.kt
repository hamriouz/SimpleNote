package com.example.simplenote

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firstNameInput = findViewById<EditText>(R.id.editTextFirstName)
        val lastNameInput = findViewById<EditText>(R.id.editTextLastName)
        val usernameInput = findViewById<EditText>(R.id.editTextUsername)
        val emailInput = findViewById<EditText>(R.id.editTextEmail)
        val passwordInput = findViewById<EditText>(R.id.editTextPassword)
        val retypePasswordInput = findViewById<EditText>(R.id.editTextRetypePassword)
        val registerButton = findViewById<Button>(R.id.button_register)
        val loginLink = findViewById<TextView>(R.id.text_login)

        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val retypePassword = retypePasswordInput.text.toString()
            // TODO: Call your API to register here
        }

        loginLink.setOnClickListener {
            finish() // Go back to LoginActivity
        }
    }
}
