package com.tusur.teacherhelper.presentation.subjects

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


@AndroidEntryPoint
class SubjectListFragment :
    TopLevelListFragment<SubjectItemUiState, SubjectListUiState, Nothing>() {

    override lateinit var mainAdapter: SubjectAdapter
    override lateinit var searchAdapter: SubjectAdapter

    override val viewModel: SubjectListViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemClickListener = object : BaseDeletableAdapter.Listener<SubjectItemUiState> {
            override fun onClick(item: SubjectItemUiState) {
                navigateToDetails(item.id)
            }

            override fun onDelete(item: SubjectItemUiState) {
                showDeleteSubjectDialog(onConfirm = {
                    viewModel.onEvent(Event.TryDelete(item.id))
                    updateDeleteState(delete = false)
                })
            }
        }

        mainAdapter = SubjectAdapter(itemClickListener)
        searchAdapter = SubjectAdapter(itemClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addButton.setOnClickListener {
            SubjectInputBottomSheet().show(childFragmentManager, null)
        }

        binding.searchView.setHint(R.string.subject_hint)
    }

    private fun navigateToDetails(subjectId: Int) {
        SubjectListFragmentDirections.actionToSubjectDetailsFragment(subjectId).also {
            findNavController().navigate(it)
        }
    }

    private fun showDeleteSubjectDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_subject_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }
}