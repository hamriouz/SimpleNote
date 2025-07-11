package com.example.simplenote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
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
import java.security.KeyStore.TrustedCertificateEntry

class ChangePasswordFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val goSettings = {
            findNavController().navigate(R.id.action_changePasswordFragment_to_settingsFragment)
        }
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        view.findViewById<View>(R.id.backButton).setOnClickListener { goSettings() }
        view.findViewById<View>(R.id.backText).setOnClickListener { goSettings() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goSettings()
            }
        })
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        submitButton.isEnabled = true
        val currentPassword = view.findViewById<EditText>(R.id.currentPassword)
        val newPassword = view.findViewById<EditText>(R.id.newPassword)
        val retypeNewPassword = view.findViewById<EditText>(R.id.retypeNewPassword)

        currentPassword.setOnClickListener {
            updateButtonState(newPassword.text.toString(), retypeNewPassword.text.toString(), submitButton, currentPassword.text.toString())
        }

        newPassword.setOnClickListener {
            updateButtonState(newPassword.text.toString(), retypeNewPassword.text.toString(), submitButton, currentPassword.text.toString())
        }

        retypeNewPassword.setOnClickListener {
            updateButtonState(newPassword.text.toString(), retypeNewPassword.text.toString(), submitButton, currentPassword.text.toString())
        }

        submitButton.setOnClickListener {
            changePassword(newPassword.text.toString(), currentPassword.text.toString())
        }
    }

    private fun changePassword(newPassword: String, oldPassword: String) {
        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            requireContext(),
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val accessToken = sharedPreferences.getString("access_token", "")!!
        val client = OkHttpClient()
        val mediaType = "application/json".toMediaType()
        val body = """
                {
                    "old_password": "$oldPassword",
                    "new_password": "$newPassword"
                }
            """.trimIndent().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("${BuildConfig.BASE_URL}/api/auth/change-password/")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer ${accessToken}")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("HTTP", "Failed: ${e.message}")
                Log.e("HTTP ERROR", "Failed: ${Log.getStackTraceString(e)}")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("HTTP", "${response.code}-${response.message}")
                if (response.code == 200) {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        })
    }

    private fun updateButtonState(newPassword: String, retypeNewPassword: String, submitButton: Button, currentPassword: String) {
        submitButton.isEnabled = newPassword == retypeNewPassword && newPassword.isNotEmpty() && currentPassword.isNotEmpty()
    }
}

