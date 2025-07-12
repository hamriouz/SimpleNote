package com.example.simplenote.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplenote.core.data.local.AppDatabase
import com.example.simplenote.adapter.NoteAdapter
import com.example.simplenote.core.repository.NoteRepository
import com.example.simplenote.R
import com.example.simplenote.core.util.UserManager
import com.example.simplenote.databinding.FragmentFirstBinding
import com.example.simplenote.core.data.local.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val username by lazy { UserManager.getCurrentUsername(requireContext()) }

    private val pageSize = 8
    private var currentPage = 0
    private var isEndPage = false

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
        loadNotes(page = currentPage++)

        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment)
        }
        binding.navSettings.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SettingsFragment)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            val bundle = Bundle().apply { putInt("noteId", note.id.toInt()) }
            findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment, bundle)
        }
        binding.recyclerView.adapter = noteAdapter
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                synchronized("page_load") {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= pageSize
                    ) {
                        loadNotes(currentPage++)
                    }
                }
            }
        })

    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                noteAdapter.clear()
                val query = newText?.trim() ?: ""
                if (query.isEmpty()) {
                    currentPage = 0
                    loadNotes(currentPage++)
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        val list = repo.searchNotes(username = username, query = query)
                        noteAdapter.updateNotes(list)
                    }
                }
                return true
            }
        })
    }

    private fun loadNotes(page: Int) {
        if (isEndPage) return
        lifecycleScope.launch(Dispatchers.Default) {
            delay(300)

            notes = repo.getAllNotes(
                username = username,
                limit = pageSize,
                offset = (page * pageSize)
            )
            isEndPage = notes.isEmpty()
            lifecycleScope.launch(Dispatchers.Main) {
                noteAdapter.updateNotes(notes)
                if (page == 0) setVisibilities(notes.isEmpty())
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