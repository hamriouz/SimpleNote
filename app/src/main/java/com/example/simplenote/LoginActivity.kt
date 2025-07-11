package com.example.simplenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import okhttp3.MediaType.Companion.toMediaType

import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback

import java.io.IOException
import android.util.Log

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.emailEditText)
        val passwordInput = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerLink = findViewById<TextView>(R.id.registerLink)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
        }

        loginButton.isEnabled = false

        emailInput.doAfterTextChanged {
            updateLoginButtonState(emailInput.text.toString(), passwordInput.text.toString(), loginButton)
        }

        passwordInput.doAfterTextChanged {
            updateLoginButtonState(emailInput.text.toString(), passwordInput.text.toString(), loginButton)
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            val client = OkHttpClient()
            val mediaType = "application/json".toMediaType()
            val body = "{\n  \"password\": \"${password}\",\n  \"username\": \"${email}\"\n}".toRequestBody(mediaType)
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

                    if (response.code == 200 && response.body != null) {
                        val jsonResponse = response.body!!.string()
                        val jsonObject = JSONObject(jsonResponse)
                        val accessToken = jsonObject.getString("access")
                        val refreshToken = jsonObject.getString("refresh")
                        val masterKey = MasterKey.Builder(this@LoginActivity)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build()

                        val sharedPreferences = EncryptedSharedPreferences.create(
                            this@LoginActivity,
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
            // TODO: Add real authentication logic here
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun updateLoginButtonState(email: String, password: String, loginButton: Button) {
        loginButton.isEnabled = email.isNotEmpty() && password.isNotEmpty()
    }
}
