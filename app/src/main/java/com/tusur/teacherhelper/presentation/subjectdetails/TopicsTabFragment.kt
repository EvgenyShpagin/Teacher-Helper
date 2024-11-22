package com.tusur.teacherhelper.presentation.subjectdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopicsTabBinding
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


class TopicsTabFragment : Fragment() {

    private var _binding: FragmentTopicsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubjectDetailsViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private lateinit var adapter: TopicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = TopicAdapter(resources.primaryLocale) { view, topicItem ->
            when (topicItem) {
                is TopicItemUiState.Label -> {}
                is TopicItemUiState.Topic -> navigateToTopicDetails(view, topicItem.itemId)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.distinctUntilChangedBy { it.topicsUiState }
                    .collectLatest { uiState ->
                        adapter.submitList(uiState.topicsUiState)
                        binding.emptyListLabel.isVisible = uiState.topicsUiState.isEmpty()
                    }
            }
        }
    }

    private fun navigateToTopicDetails(view: View, topicId: Int) {
        val extras =
            FragmentNavigatorExtras(view to requireContext().getString(R.string.transition_topic_details))
        val action = SubjectDetailsFragmentDirections.actionToTopicFragment(
            topicId,
            viewModel.subjectId,
        )
        findNavController().navigate(action, extras)
    }

    private fun setupRecyclerView() {
        binding.topics.adapter = adapter
        binding.topics.addItemDecoration(getDefaultListItemDecoration(resources))
    }
}