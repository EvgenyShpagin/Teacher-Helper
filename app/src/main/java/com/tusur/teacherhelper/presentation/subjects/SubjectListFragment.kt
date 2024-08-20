package com.tusur.teacherhelper.presentation.subjects

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentSubjectListBinding
import com.tusur.teacherhelper.presentation.subjects.SubjectListViewModel.Event
import com.tusur.teacherhelper.presentation.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.view.recycler.checkNestedScrollState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class SubjectListFragment : Fragment() {

    private var _binding: FragmentSubjectListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubjectAdapter

    private val viewModel: SubjectListViewModel by viewModels { SubjectListViewModel.factory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }

        adapter = SubjectAdapter()
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupEmptyLabelGravity()
        postponeEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    binding.progressCircular.isVisible = uiState.isFetching
                    binding.emptyListLabel.isVisible = uiState.listIsEmpty
                    adapter.submitList(uiState.itemsUiState)
                    view.doOnPreDraw {
                        binding.subjectList.checkNestedScrollState()
                        val needToExpandAppBar = !binding.subjectList.isNestedScrollingEnabled
                        binding.appBarLayout.setExpanded(needToExpandAppBar)
                        startPostponedEnterTransition()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setupMenu()

        binding.addButton.setOnClickListener {
            SubjectInputBottomSheet().show(childFragmentManager, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupMenu() {
        binding.topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.topic_types -> navigateToTopicTypes()
                R.id.global_topics -> navigateToGlobalTopics()
                R.id.groups -> navigateToGroupList()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }
    }

    private fun navigateToGroupList() {
        val action = SubjectListFragmentDirections.actionToAllGroupsFragment()
        findNavController().navigate(action)
    }

    private fun navigateToGlobalTopics() {
        val action = SubjectListFragmentDirections.actionToGlobalTopicsBottomSheet()
        findNavController().navigate(action)
    }

    private fun navigateToTopicTypes() {
        val action = SubjectListFragmentDirections.actionToTopicTypesBottomSheet()
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        binding.subjectList.adapter = adapter
        binding.subjectList.addItemDecoration(getDefaultListItemDecoration(resources))
        adapter.onClickListener = SubjectAdapter.OnClickListener(::navigateToDetails)
    }

    private fun navigateToDetails(subjectId: Int) {
        SubjectListFragmentDirections.actionToSubjectDetailsFragment(subjectId).also {
            findNavController().navigate(it)
        }
    }

    private fun setupEmptyLabelGravity() {
        binding.appBarLayout.post {
            binding.emptyListLabel.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                gravity = Gravity.CENTER
                topMargin = binding.appBarLayout.height
            }
        }
    }
}