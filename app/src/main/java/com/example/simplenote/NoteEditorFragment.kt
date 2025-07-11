package com.example.simplenote

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentNoteEditorBinding
import java.text.SimpleDateFormat
import java.util.*

class NoteEditorFragment : Fragment() {
    private var _binding: FragmentNoteEditorBinding? = null
    private val binding get() = _binding!!

    private var lastEdited: Date = Date()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNoteEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Bottom trash button
        binding.btnDeleteBottom.setOnClickListener {
            deleteNote()
        }

        // Update last edited on text change
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lastEdited = Date()
                updateLastEdited()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.editTitle.addTextChangedListener(watcher)
        binding.editContent.addTextChangedListener(watcher)

        updateLastEdited()
    }

    private fun updateLastEdited() {
        binding.lastEditedText.text = "Last edited on ${dateFormat.format(lastEdited)}"
    }

    private fun deleteNote() {
        // TODO: Implement actual note deletion logic
        Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 