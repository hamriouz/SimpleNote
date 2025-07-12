package com.example.simplenote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
        val repo = NoteRepository(db)

        lifecycleScope.launch(Dispatchers.Default) {
            notes = repo.getAllNotes()
            setVisibilities(notes.isEmpty())
            initRecyclerView(notes)
        }

        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        binding.fabAddNote.setOnClickListener {
            findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment)
        }
        binding.navSettings.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SettingsFragment)
        }
    }
    private fun initRecyclerView(list: List<Note>) {
        lifecycleScope.launch(Dispatchers.Main) {
            val adapter = NoteAdapter(list) { note ->
                // Handle note click - navigate to editor
                val bundle = Bundle().apply {
                    putInt("noteId", note.id.toInt())
                }
                findNavController().navigate(R.id.action_firstFragment_to_noteEditorFragment, bundle)
            }
            binding.recyclerView.adapter = adapter
            binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setVisibilities(isDataEmpty: Boolean) {
        binding.recyclerView.isVisible = !isDataEmpty
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