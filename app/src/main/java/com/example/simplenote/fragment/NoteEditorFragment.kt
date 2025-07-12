package com.example.simplenote.fragment

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
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.example.simplenote.core.data.local.AppDatabase
import com.example.simplenote.core.repository.NoteRepository
import com.example.simplenote.R
import com.example.simplenote.core.util.UserManager
import com.example.simplenote.activity.MainActivity
import com.example.simplenote.bottomsheet.DeleteBottomSheet
import com.example.simplenote.databinding.FragmentNoteEditorBinding
import com.example.simplenote.core.data.local.model.Note
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
    private var currentNote: Note? = null
    private var noteId: Int = -1

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

        // Get note ID from arguments if editing existing note
        noteId = arguments?.getInt("noteId", -1) ?: -1
        if (noteId != -1) {
            loadExistingNote()
        }

        val goHome = {
            saveNoteToDb()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.backButton.setOnClickListener { goHome() }
        binding.backText.setOnClickListener { goHome() }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goHome()
                }
            })
        binding.btnDeleteBottom.setOnClickListener {
            showDeleteConfirmationDialog()
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

    private fun loadExistingNote() {
        val db = AppDatabase.getDatabase(requireContext())
        val repo = NoteRepository(db)
        CoroutineScope(Dispatchers.IO).launch {
            val username = UserManager.getCurrentUsername(requireContext())
            val note = repo.getNoteById(noteId.toLong(), username)
            note?.let {
                currentNote = it
                CoroutineScope(Dispatchers.Main).launch {
                    binding.editTitle.setText(it.title)
                    binding.editContent.setText(it.content)
                    lastEdited = Date(it.lastEdited)
                    updateLastEdited()
                }
            }
        }
    }

    private fun updateLastEdited() {
        binding.lastEditedText.text = "Last edited on ${dateFormat.format(lastEdited)}"
    }

    private fun saveNoteToDb() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()
        if (title.isNotBlank() || content.isNotBlank()) {
            val username = UserManager.getCurrentUsername(requireContext())
            val db = AppDatabase.getDatabase(requireContext())
            val repo = NoteRepository(db)

            CoroutineScope(Dispatchers.IO).launch {
                if (currentNote != null) {
                    // Update existing note
                    currentNote!!.title = title
                    currentNote!!.content = content
                    currentNote!!.lastEdited = System.currentTimeMillis()
                    repo.update(currentNote!!)
                } else {
                    // Create new note
                    val note = Note(
                        title = title,
                        content = content,
                        lastEdited = System.currentTimeMillis(),
                        username = username
                    )
                    repo.insert(note)
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        setFragmentResultListener(
            requestKey = DeleteBottomSheet.DELETE_RESULT,
            listener = { _, _ ->
                deleteNote()
            }
        )
        findNavController().navigate(
            R.id.noteEditorFragment_delete_dialog
        )
    }

    private fun deleteNote() {
        currentNote?.let { note ->
            val db = AppDatabase.getDatabase(requireContext())
            val repo = NoteRepository(db)
            CoroutineScope(Dispatchers.IO).launch {
                repo.delete(note)
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 