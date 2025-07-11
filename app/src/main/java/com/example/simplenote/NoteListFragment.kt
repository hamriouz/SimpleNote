package com.example.simplenote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class NoteListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var adapter: NoteAdapter
    val db = AppDatabase.getDatabase(requireContext())
    val repo = NoteRepository(db)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_note_list, container, false)
        recyclerView = view.findViewById(R.id.notesRecyclerView)
        fabAddNote = view.findViewById(R.id.fabAddNote)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NoteAdapter(emptyList()) { note ->
            // TODO: Navigate to note editor with note.id
        }
        recyclerView.adapter = adapter
        loadNotes()
        fabAddNote.setOnClickListener {
            // TODO: Navigate to note editor for new note
        }
    }

    private fun loadNotes() {
        viewLifecycleOwner.lifecycleScope.launch {
            val notes = repo.getAllNotes()
            adapter.updateNotes(notes)
        }
    }
} 