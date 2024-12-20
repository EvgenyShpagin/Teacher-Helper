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
import com.google.android.material.transition.MaterialFadeThrough
import com.tusur.teacherhelper.databinding.FragmentSubjectListBinding
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.setupTopLevelAppBarConfiguration
import com.tusur.teacherhelper.presentation.subjects.SubjectListViewModel.Event
import com.tusur.teacherhelper.presentation.subjects.SubjectListViewModel.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SubjectListFragment : Fragment() {

    private var _binding: FragmentSubjectListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SubjectAdapter

    private val viewModel: SubjectListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }

        adapter = SubjectAdapter()

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
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

        setupTopLevelAppBarConfiguration(binding.topAppBar)
        setupRecyclerView()
        setupEmptyLabelGravity()
        postponeEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { uiState -> uiState.isFetching }
                        .collect { uiState ->
                            if (!uiState.isFetching) {
                                view.doOnPreDraw {
                                    startPostponedEnterTransition()
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState.collectLatest { uiState ->
                        doOnListUpdate(uiState)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.addButton.setOnClickListener {
            SubjectInputBottomSheet().show(childFragmentManager, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun doOnListUpdate(uiState: UiState) {
        binding.progressCircular.isVisible = uiState.isFetching
        binding.emptyListLabel.isVisible = uiState.listIsEmpty
        adapter.submitList(uiState.itemsUiState)
        binding.subjectList.doOnPreDraw {
            binding.appBarLayout.fixCollapsing(binding.subjectList)
        }
    }
}