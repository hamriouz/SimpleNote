package com.example.simplenote.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.BuildConfig
import com.example.simplenote.compose.MainNavigation
import com.example.simplenote.ui.theme.SimpleNoteTheme
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Token refresh logic
        lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000 * 60 * 5) // refresh token every 5 minutes
                val masterKey = MasterKey.Builder(this@MainActivity)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()

                val sharedPreferences = EncryptedSharedPreferences.create(
                    this@MainActivity,
                    "secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                val refresh = sharedPreferences.getString("refresh_token", "")!!
                if (refresh.isEmpty()) {
                    break
                }
                Log.e("TOKEN", "Refreshing Token")
                val client = OkHttpClient()
                val mediaType = "application/json".toMediaType()
                val body = """
                    {
                        "refresh": "$refresh"
                    }
                """.trimIndent().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("${BuildConfig.BASE_URL}/api/auth/token/refresh/")
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
                        if (response.code == 200) {
                            val jsonResponse = response.body!!.string()
                            val jsonObject = JSONObject(jsonResponse)
                            val accessToken = jsonObject.getString("access")
                            sharedPreferences.edit {
                                putString("access_token", accessToken)
                            }
                        }
                    }
                })
            }
        }

        setContent {
            SimpleNoteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUILeanBack()
        }
    }

    private fun hideSystemUILeanBack() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }
    }
}