package com.example.simplenote

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.simplenote.databinding.FragmentNoteEditorBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        val goHome = {
            saveNoteToDb()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.btnBack.setOnClickListener { goHome() }
        binding.tvBack.setOnClickListener { goHome() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })
        binding.btnDeleteBottom.setOnClickListener {
            deleteNote()
        }

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

    private fun saveNoteToDb() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()
        if (title.isNotBlank() || content.isNotBlank()) {
            val note = Note(title = title, content = content, lastEdited = System.currentTimeMillis())
            val db = AppDatabase.getDatabase(requireContext())
            val repo = NoteRepository(db)
            CoroutineScope(Dispatchers.IO).launch {
                repo.insert(note)
            }
        }
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