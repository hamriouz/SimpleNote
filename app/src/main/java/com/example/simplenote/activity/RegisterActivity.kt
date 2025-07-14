package com.example.simplenote.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.BuildConfig
import com.example.simplenote.compose.RegisterScreen
import com.example.simplenote.ui.theme.SimpleNoteTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import com.example.simplenote.core.util.showError

class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SimpleNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        onRegisterClick = { firstName, lastName, username, email, password ->
                            performRegister(firstName, lastName, username, email, password)
                        },
                        onLoginClick = {
                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun performRegister(firstName: String, lastName: String, username: String, email: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """{  
            "password": "$password",
            "email": "$email",
            "username": "$username",
            "first_name": "$firstName",
            "last_name": "$lastName"
        }""".trimMargin().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}/api/auth/register/")
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showError(this@RegisterActivity, e.message ?: "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 201) {
                    loginUser(username, password)
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
                    showError(this@RegisterActivity, errorMsg)
                }
            }
        })
    }

    private fun loginUser(username: String, password: String) {
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """{
            "username": "$username",
            "password": "$password"
        }""".trimIndent().toRequestBody(mediaType)
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
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200) {
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

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        })
    }
}
