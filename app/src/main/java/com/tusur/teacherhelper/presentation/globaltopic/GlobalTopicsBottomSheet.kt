package com.tusur.teacherhelper.presentation.globaltopic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetGlobalTopicsBinding
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.core.view.recycler.decorations.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GlobalTopicsBottomSheet : BottomSheetDialogFragment() {

    private val binding get() = _binding!!
    private var _binding: BottomSheetGlobalTopicsBinding? = null
    private val viewModel: GlobalTopicsViewModel by viewModels(extrasProducer = {
        creationCallback<GlobalTopicsViewModel.Factory> { factory ->
            factory.create(resources.primaryLocale)
        }
    })

    private val adapter: GlobalTopicAdapter by lazy {
        GlobalTopicAdapter(object : BaseDeletableAdapter.Listener<GlobalTopicUiState> {
            override fun onClick(item: GlobalTopicUiState) =
                navigateToTopic(topicId = item.topicId, create = false)

            override fun onDelete(item: GlobalTopicUiState) =
                showDeleteTypeDialog { viewModel.deleteTopic(item.topicId) }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetGlobalTopicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.globalTopicItemsUiState }
                        .collect {
                            adapter.submitList(it.globalTopicItemsUiState)
                            binding.deleteButton.isVisible = it.globalTopicItemsUiState.isNotEmpty()
                            binding.emptyListLabel.isVisible = it.globalTopicItemsUiState.isEmpty()
                        }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }.collect {
                        adapter.isDeleting = it.isDeleting
                        if (it.globalTopicItemsUiState.isEmpty()) return@collect
                        binding.deleteButton.isVisible = !it.isDeleting
                        binding.cancelDeleteButton.isVisible = it.isDeleting
                        binding.addButton.isInvisible = it.isDeleting
                    }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect { event ->
                        handleOnetimeEvent(event)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.deleteButton.setOnClickListener {
            viewModel.startDelete()
        }
        binding.cancelDeleteButton.setOnClickListener {
            viewModel.stopDelete()
        }
        binding.addButton.setOnClickListener {
            navigateToTopic(topicId = NO_ID, create = true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDeleteTypeDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_type_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun navigateToTopic(topicId: Int, create: Boolean) {
        val action = if (create) {
            GlobalTopicsBottomSheetDirections.actionToTopicNameFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID
            )
        } else {
            GlobalTopicsBottomSheetDirections.actionToTopicFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID,
                isJustCreated = false
            )
        }
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        binding.topicList.adapter = adapter
        val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
        val itemDecorator = MarginItemDecoration(verticalSpace = verticalMargin)
        binding.topicList.addItemDecoration(itemDecorator)
    }

    private fun handleOnetimeEvent(event: GlobalTopicsViewModel.OnetimeEvent) {
        when (event) {
            GlobalTopicsViewModel.OnetimeEvent.FailedToDeleteDeadline -> {
                TopicDeleteErrorDialog.show(requireContext())
            }
        }
    }
}