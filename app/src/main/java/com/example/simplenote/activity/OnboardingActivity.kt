package com.example.simplenote.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.BuildConfig
import com.example.simplenote.compose.OnboardingScreen
import com.example.simplenote.ui.theme.SimpleNoteTheme

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val masterKey = MasterKey.Builder(this@OnboardingActivity)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            this@OnboardingActivity,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val token = sharedPreferences.getString("access_token", "")!!
        sharedPreferences.edit {
            putBoolean("isUpdated", true)
        }

        var navigateToLogin by mutableStateOf(false)
        var navigateToMain by mutableStateOf(false)

        if (token.isEmpty()) {
            navigateToLogin = true
        } else {
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
                    navigateToLogin = true
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.e("HTTP", "${response.code}")
                    if (response.code == 200) {
                        navigateToMain = true
                    } else {
                        navigateToLogin = true
                    }
                }
            })
        }

        setContent {
            SimpleNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    OnboardingScreen(
                        onGetStartedClick = {
                            when {
                                navigateToMain -> {
                                    startActivity(Intent(this@OnboardingActivity, MainActivity::class.java))
                                    finish()
                                }
                                navigateToLogin -> {
                                    startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    startActivity(Intent(this@OnboardingActivity, LoginActivity::class.java))
                                    finish()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
