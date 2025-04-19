package com.tusur.teacherhelper.presentation.groups

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.base.TopLevelListFragment
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GroupListFragment : TopLevelListFragment<GroupItemUiState, GroupsUiState, GroupsEffect>() {

    override val viewModel: GroupListViewModel by viewModels()
    override lateinit var mainAdapter: GroupAdapter
    override lateinit var searchAdapter: GroupAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemClickListener = object : BaseDeletableAdapter.Listener<GroupItemUiState> {
            override fun onClick(item: GroupItemUiState) {
                navigateToGroupPerformance(item.id)
            }

            override fun onDelete(item: GroupItemUiState) {
                showDeleteGroupDialog(onConfirm = {
                    viewModel.onEvent(Event.TryDelete(item.id))
                })
            }
        }

        mainAdapter = GroupAdapter(itemClickListener)
        searchAdapter = GroupAdapter(itemClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addButton.setOnClickListener {
            navigateToAddGroup()
        }

        binding.searchView.setHint(R.string.group_number_hint)
    }

    override fun initCollectors(scope: CoroutineScope) {
        super.initCollectors(scope)

        scope.launch(Dispatchers.Main.immediate) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    GroupsEffect.FailedToDeleteGroup -> showDeleteProblemDialog()
                }
            }
        }
    }

    private fun navigateToAddGroup() {
        val action = GroupListFragmentDirections
            .actionToNewGroupNumberInputBottomSheet()
        findNavController().navigate(action)
    }

    private fun showDeleteProblemDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_group_cannot_be_deleted_title)
            .setMessage(R.string.dialog_group_cannot_be_deleted_body)
            .setOnDismissListener { viewModel.onEvent(Event.StopDelete) }
            .setPositiveButton(R.string.ok_button) { _, _ ->
                viewModel.onEvent(Event.StopDelete)
            }.show()
    }

    private fun navigateToGroupPerformance(groupId: Int) {
        val action = GroupListFragmentDirections
            .actionToGroupStudentsFragment(groupId)
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
}