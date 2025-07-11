package com.example.simplenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import android.content.Intent
import androidx.activity.OnBackPressedCallback
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val goHome = {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        view.findViewById<View>(R.id.backButton).setOnClickListener { goHome() }
        view.findViewById<View>(R.id.backText).setOnClickListener { goHome() }
        view.findViewById<View>(R.id.changePasswordRow)?.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_changePasswordFragment)
        }
        view.findViewById<View>(R.id.logoutRow)?.setOnClickListener {
            showLogoutDialog()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })
    }

    private fun showLogoutDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.setCancelable(true)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnYes.setOnClickListener {
            dialog.dismiss()
            requireActivity().finishAffinity()
        }
        dialog.show()
        val window = dialog.window
        window?.setLayout((resources.displayMetrics.widthPixels * 0.90).toInt(), android.view.WindowManager.LayoutParams.WRAP_CONTENT)
    }
} 