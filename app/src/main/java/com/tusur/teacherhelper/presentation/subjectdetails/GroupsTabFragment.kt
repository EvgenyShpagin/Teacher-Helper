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
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentGroupsTabBinding
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


class GroupsTabFragment : Fragment() {

    private var _binding: FragmentGroupsTabBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SubjectDetailsViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    private lateinit var adapter: DeletableGroupAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DeletableGroupAdapter(object : DeletableGroupAdapter.OnClickListener {
            override fun onClick(groupId: Int) {
                navigateToGroupPerformance(groupId)
            }

            override fun onDeleteClick(groupId: Int) {
                showDeleteGroupDialog {
                    viewModel.stopDelete()
                    viewModel.deleteGroup(groupId)
                }
            }
        })
    }

    private fun navigateToGroupPerformance(groupId: Int) {
        val action = SubjectDetailsFragmentDirections
            .actionToPerformanceTableFragment(groupId, viewModel.subjectId)
        findNavController().navigate(action)
    }

    private fun showDeleteGroupDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_group_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.groupsUiState }
                        .collectLatest { uiState -> doOnListUpdate(uiState) }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }
                        .collectLatest { uiState -> doOnDeleting(uiState) }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun doOnDeleting(uiState: SubjectDetailsViewModel.UiState) {
        if (uiState.isDeleting) {
            adapter.showDeleteButtons()
        } else {
            adapter.hideDeleteButtons()
        }
    }

    private fun doOnListUpdate(uiState: SubjectDetailsViewModel.UiState) {
        adapter.submitList(uiState.groupsUiState)
        binding.emptyListLabel.isVisible = uiState.groupsUiState.isEmpty()
    }

    private fun setupRecyclerView() {
        binding.groups.adapter = adapter
        binding.groups.addItemDecoration(getDefaultListItemDecoration(resources))
    }
}