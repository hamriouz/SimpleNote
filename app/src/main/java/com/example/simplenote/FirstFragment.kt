package com.example.simplenote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.simplenote.databinding.FragmentFirstBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    private lateinit var notes: List<Note>
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var repo: NoteRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        repo = NoteRepository(db)

        setupRecyclerView()
        setupSearchView()
        loadNotes()

        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment)
        }
        binding.navSettings.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SettingsFragment)
        }
    }
    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(mutableListOf()) { note ->
            val bundle = Bundle().apply {
                putInt("noteId", note.id.toInt())
            }
            findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment, bundle)
        }
        binding.recyclerView.adapter = noteAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText?.trim() ?: ""
                if (query.isEmpty()) {
                    noteAdapter.updateNotes(notes)
                } else {
                    val filteredNotes = notes.filter { note ->
                        note.title.contains(query, ignoreCase = true) || 
                        note.content.contains(query, ignoreCase = true)
                    }
                    noteAdapter.updateNotes(filteredNotes)
                }
                return true
            }
        })
    }

    private fun loadNotes() {
        lifecycleScope.launch(Dispatchers.Default) {
            val username = UserManager.getCurrentUsername(requireContext())
            notes = repo.getAllNotes(username)
            lifecycleScope.launch(Dispatchers.Main) {
                noteAdapter.updateNotes(notes)
                setVisibilities(notes.isEmpty())
            }
        }
    }

    private fun setVisibilities(isDataEmpty: Boolean) {
        binding.recyclerView.isVisible = !isDataEmpty
        binding.searchCard.isVisible = !isDataEmpty
        binding.notesTitle.isVisible = !isDataEmpty
        binding.emptyIllustration.isVisible = isDataEmpty
        binding.emptyTitle.isVisible = isDataEmpty
        binding.emptySubtitle.isVisible = isDataEmpty
        binding.arrowToPlus.isVisible = isDataEmpty
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}