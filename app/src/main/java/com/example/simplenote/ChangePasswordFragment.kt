package com.example.simplenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.OnBackPressedCallback

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
        view.findViewById<View>(R.id.backButton).setOnClickListener { goSettings() }
        view.findViewById<View>(R.id.backText).setOnClickListener { goSettings() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goSettings()
            }
        })
    }
}

