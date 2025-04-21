package com.tusur.teacherhelper.presentation.topictype

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.base.TopLevelListFragment
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TopicTypeListFragment :
    TopLevelListFragment<TopicTypeItemUiState, TopicTypeListUiState, TopicTypeListUiEffect>() {

    override lateinit var mainAdapter: BaseDeletableAdapter<TopicTypeItemUiState>
    override lateinit var searchAdapter: BaseDeletableAdapter<TopicTypeItemUiState>

    override val viewModel: TopicTypeListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemClickListener = object : BaseDeletableAdapter.Listener<TopicTypeItemUiState> {
            override fun onClick(item: TopicTypeItemUiState) {

                navigateToType(typeId = item.typeId, create = false)
            }

            override fun onDelete(item: TopicTypeItemUiState) {
                showDeleteTypeDialog(onConfirm = {
                    viewModel.onEvent(Event.TryDelete(item.typeId))
                    updateDeleteState(delete = false)
                })
            }
        }

        mainAdapter = TopicTypeAdapter(itemClickListener)
        searchAdapter = TopicTypeAdapter(itemClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addButton.setOnClickListener {
            navigateToType(typeId = NO_ID, create = true)
        }

        binding.searchView.setHint(R.string.topic_type_hint)
    }

    override fun initCollectors(scope: CoroutineScope) {
        super.initCollectors(scope)

        scope.launch(Dispatchers.Main.immediate) {
            viewModel.uiEffect.collect { effect ->
                showDeleteErrorDialog()
            }
        }
    }

    private fun navigateToType(typeId: Int, create: Boolean) {
        val action = TopicTypeListFragmentDirections.actionToTopicTypeFragment(typeId, create)
        findNavController().navigate(action)
    }

    private fun showDeleteTypeDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_type_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun showDeleteErrorDialog() {
        TopicDeleteErrorDialog.show(requireContext())
    }
}

// TODO: show dialog when reason differs