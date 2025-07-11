package com.example.simplenote

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.simplenote.databinding.ActivityMainBinding
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}