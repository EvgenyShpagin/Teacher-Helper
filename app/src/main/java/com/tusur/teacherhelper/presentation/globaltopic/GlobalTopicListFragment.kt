package com.tusur.teacherhelper.presentation.globaltopic

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.base.TopLevelListFragment
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GlobalTopicListFragment :
    TopLevelListFragment<GlobalTopicUiState, GlobalTopicListUiState, GlobalTopicListEffect>() {

    override lateinit var mainAdapter: BaseDeletableAdapter<GlobalTopicUiState>
    override lateinit var searchAdapter: BaseDeletableAdapter<GlobalTopicUiState>


    override val viewModel: GlobalTopicListViewModel by viewModels(extrasProducer = {
        creationCallback<GlobalTopicListViewModel.Factory> { factory ->
            factory.create(resources.primaryLocale)
        }
    })

    override fun initCollectors(scope: CoroutineScope) {
        super.initCollectors(scope)

        scope.launch(Dispatchers.Main.immediate) {
            viewModel.uiEffect.collect { effect ->
                when (effect) {
                    GlobalTopicListEffect.FailedToDeleteDeadline -> {
                        showDeleteErrorDialog()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemClickListener = object : BaseDeletableAdapter.Listener<GlobalTopicUiState> {
            override fun onClick(item: GlobalTopicUiState) {
                navigateToTopic(topicId = item.topicId, create = false)
            }

            override fun onDelete(item: GlobalTopicUiState) {
                showDeleteTopicDialog(
                    onConfirm = {
                        viewModel.onEvent(Event.TryDelete(item.topicId))
                    }
                )
            }
        }

        mainAdapter = GlobalTopicAdapter(itemClickListener)
        searchAdapter = GlobalTopicAdapter(itemClickListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addButton.setOnClickListener {
            navigateToTopic(topicId = NO_ID, create = true)
        }

        binding.searchView.setHint(R.string.global_topic_hint)
    }

    private fun showDeleteTopicDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }


    private fun showDeleteErrorDialog() {
        TopicDeleteErrorDialog.show(requireContext())
    }

    private fun navigateToTopic(topicId: Int, create: Boolean) {
        val action = if (create) {
            GlobalTopicListFragmentDirections.actionToTopicNameFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID
            )
        } else {
            GlobalTopicListFragmentDirections.actionToTopicFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID,
                isJustCreated = false
            )
        }
        findNavController().navigate(action)
    }
}