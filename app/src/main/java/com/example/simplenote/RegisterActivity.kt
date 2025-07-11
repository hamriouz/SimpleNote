package com.example.simplenote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val firstNameInput = findViewById<EditText>(R.id.firstName)
        val lastNameInput = findViewById<EditText>(R.id.lastName)
        val usernameInput = findViewById<EditText>(R.id.username)
        val emailInput = findViewById<EditText>(R.id.emailAddress)
        val passwordInput = findViewById<EditText>(R.id.password)
        val retypePasswordInput = findViewById<EditText>(R.id.retypePassword)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        registerButton.isEnabled = false

        firstNameInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }

        lastNameInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }

        usernameInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }

        emailInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }

        passwordInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }

        retypePasswordInput.doAfterTextChanged {
            updateRegisterButtonState(firstNameInput.text.toString(), lastNameInput.text.toString(), usernameInput.text.toString(), emailInput.text.toString(), passwordInput.text.toString(), retypePasswordInput.text.toString(), registerButton)
        }


        registerButton.setOnClickListener {
            val firstName = firstNameInput.text.toString()
            val lastName = lastNameInput.text.toString()
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val retypePassword = retypePasswordInput.text.toString()

            val client = OkHttpClient()
            val mediaType = "application/json".toMediaType()
            val body = """{  
                "password": "$password",
                "username": "$email",
                "username": "$username",
                "first_name": "$firstName",
                "last_name": "$lastName"
            }""".trimMargin().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}/api/auth/token/")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HTTP", "Failed: ${e.message}")
                    Log.e("HTTP ERROR", "Failed: ${Log.getStackTraceString(e)}")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d("HTTP", "Success: ${response.body?.string()}")
                    Log.d("HTTP", "Success: ${response.headers}")
                    Log.d("HTTP", "Success: ${response.code}")

                    if (response.code == 201 && response.body != null) {
                        val jsonResponse = response.body!!.string()
                        val jsonObject = JSONObject(jsonResponse)
                        val accessToken = jsonObject.getString("access")
                        val refreshToken = jsonObject.getString("refresh")
                        val masterKey = MasterKey.Builder(this@RegisterActivity)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()

                        val sharedPreferences = EncryptedSharedPreferences.create(
                            this@RegisterActivity,
                            "secure_prefs",
                            masterKey,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )

                        sharedPreferences.edit {
                            putString("access_token", accessToken)
                                .putString("refresh_token", refreshToken)
                        }

                        // todo: move to landing
                    } else {
                        // todo: login error handling
                    }
                }
            })
        }

        loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateRegisterButtonState(firstName: String, lastName: String, username: String, email: String, password: String, retypePassword: String, registerButton: Button) {
        registerButton.isEnabled = firstName.isNotEmpty() && lastName.isNotEmpty() && username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && password == retypePassword
    }
}
