package com.tusur.teacherhelper.presentation.subjectgroups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.databinding.FragmentSubjectGroupSelectBinding
import com.tusur.teacherhelper.domain.util.fromEpochMillis
import com.tusur.teacherhelper.presentation.core.dialog.EmptyGroupDialog
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnNavigationRequest
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.toNativeArray
import com.tusur.teacherhelper.presentation.topic.ClassTimeBottomSheet
import com.tusur.teacherhelper.presentation.topicperformance.SharedClassDatesBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


@AndroidEntryPoint
class SubjectGroupSelectFragment : Fragment(), OnQueryTextListener {

    private val binding get() = _binding!!
    private var _binding: FragmentSubjectGroupSelectBinding? = null
    private val args: SubjectGroupSelectFragmentArgs by navArgs()

    private val viewModel: SubjectGroupSelectViewModel by viewModels(extrasProducer = {
        creationCallback<SubjectGroupSelectViewModel.Factory> { factory ->
            factory.create(
                subjectId = args.subjectId,
                shouldBeAllChecked = args.shouldBeAllChecked
            )
        }
    })

    private val adapter = CheckableGroupAdapter()

    private var emptyDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetchGroups()
        }

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectGroupSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.nextButton.isEnabled = state.hasChecked
                    adapter.submitList(state.groupsUiState)
                    view.doOnPreDraw {
                        startPostponedEnterTransition()
                    }
                    if (state.wasFetched && state.groupsUiState.isEmpty()) {
                        showEmptyGroupListDialog(::navigateBack)
                    }
                }
            }
        }

        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()

        setupSearch()

        doOnNavigationRequest(topAppBar = binding.topAppBar) {
            if (!binding.searchView.isIconified) {
                viewModel.stopSearch()
                binding.searchView.setQuery("", true)
                binding.searchView.isIconified = true
            } else {
                navigateBack()
            }
        }

        binding.nextButton.setOnClickListener {
            showDatetimeSelectDialogs { datetimeMillis ->
                navigateToGroupPerformance(datetimeMillis)
            }
        }
    }

    private fun showDatetimeSelectDialogs(doOnSelectDatetime: (datetimeMillis: Long) -> Unit) {
        SharedClassDatesBottomSheet(
            topicId = args.topicId,
            groupListIds = viewModel.getCheckedItemIds()
        ) { dateMillis ->
            ClassTimeBottomSheet(
                topicId = args.topicId,
                groupListIds = viewModel.getCheckedItemIds(),
                classDate = LocalDate.fromEpochMillis(dateMillis)
            ) { initTimeMs ->
                doOnSelectDatetime.invoke(dateMillis + initTimeMs)
            }.show(childFragmentManager, null)
        }.show(childFragmentManager, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        binding.searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query == null) {
            return true
        }
        val searchQuery = "%$query%"
        viewModel.searchGroup(searchQuery)
        return true
    }

    private fun navigateToGroupPerformance(datetimeMillis: Long) {
        val action = SubjectGroupSelectFragmentDirections.actionToGroupsPerformanceFragment(
            topicId = args.topicId,
            performanceType = args.requiredPerformance,
            groupListIds = viewModel.getCheckedItemIds().toNativeArray(),
            datetimeMillis = datetimeMillis
        )
        findNavController().navigate(action)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.setOnCloseListener {
            viewModel.stopSearch()
            false
        }
    }

    private fun setupRecyclerView() {
        binding.groupsList.adapter = adapter
        binding.groupsList.addItemDecoration(getDefaultListItemDecoration(resources))
    }

    private fun showEmptyGroupListDialog(onConfirm: () -> Unit) {
        if (emptyDialog?.isShowing == true) return
        emptyDialog = EmptyGroupDialog(requireContext()) {
            emptyDialog = null
            onConfirm.invoke()
        }.show()
    }
}