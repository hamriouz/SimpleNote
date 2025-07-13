package com.example.simplenote.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
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
import androidx.core.content.edit
import com.example.simplenote.BuildConfig
import com.example.simplenote.compose.LoginScreen
import com.example.simplenote.ui.theme.SimpleNoteTheme
import org.json.JSONObject
import com.example.simplenote.core.util.showError


class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val token = sharedPreferences.getString("access_token", "")!!

        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """{
            "token": "$token"
        }""".trimIndent().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}/api/auth/token/verify/")
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
                Log.e("HTTP", "${response.code}")
                if (response.code == 200) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })

        setContent {
            SimpleNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(
                        onLoginClick = { email, password ->
                            performLogin(email, password)
                        },
                        onRegisterClick = {
                            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """
            {
                "username": "$email",
                "password": "$password"
            }
        """.trimIndent().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}/api/auth/token/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showError(this@LoginActivity, e.message ?: "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    loginUser(response)
                } else {
                    val errorBody = response.body?.string()
                    val errorMsg = try {
                        var ret = ""
                        for (i in 0 until JSONObject(errorBody).getJSONArray("errors").length()) {
                            ret += "${JSONObject(errorBody).getJSONArray("errors").getJSONObject(i).getString("detail")}\n"
                        }
                        ret
                    } catch (e: Exception) {
                        errorBody ?: "Unknown error"
                    }
                    showError(this@LoginActivity, errorMsg)
                }
            }
        })
    }

    private fun loginUser(response: Response) {
        if (response.code == 200) {
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

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}/api/auth/userinfo/")
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer ${accessToken}")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("HTTP", "Failed: ${e.message}")
                    Log.e("HTTP ERROR", "Failed: ${Log.getStackTraceString(e)}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.code == 200) {
                        val jsonResponse = response.body!!.string()
                        val jsonObject = JSONObject(jsonResponse)

                        sharedPreferences.edit {
                            putString("username", jsonObject.getString("username"))
                                .putString("email", jsonObject.getString("email"))
                                .putInt("id", jsonObject.getInt("id"))
                        }
                    }
                }
            })

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // todo: login error handling
        }
    }
}
