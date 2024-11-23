package com.tusur.teacherhelper.presentation.topictype

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
import com.tusur.teacherhelper.databinding.BottomSheetTopicTypesBinding
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.core.view.recycler.decorations.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TopicTypesBottomSheet : BottomSheetDialogFragment() {

    private val binding get() = _binding!!
    private var _binding: BottomSheetTopicTypesBinding? = null
    private val viewModel: TopicTypesViewModel by viewModels()

    private val adapter: TopicTypeAdapter by lazy {
        TopicTypeAdapter(object : BaseDeletableAdapter.Listener<TopicTypeItemUiState> {
            override fun onClick(item: TopicTypeItemUiState) =
                navigateToType(typeId = item.typeId, create = false)

            override fun onDelete(item: TopicTypeItemUiState) = showDeleteTypeDialog {
                viewModel.deleteType(item.typeId) { reason -> showDeleteErrorDialog(reason) }
            }
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
        _binding = BottomSheetTopicTypesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.topicTypeItemsUiState }.collect {
                        adapter.submitList(it.topicTypeItemsUiState)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }.collect {
                        adapter.isDeleting = it.isDeleting
                        binding.deleteButton.isVisible = !it.isDeleting
                        binding.cancelDeleteButton.isVisible = it.isDeleting
                        binding.addButton.isInvisible = it.isDeleting
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
            navigateToType(typeId = NO_ID, create = true)
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


    private fun showDeleteErrorDialog(messageText: UiText) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_type_error_title)
            .setMessage(messageText.toString(requireContext()))
            .setPositiveButton(R.string.ok_button, null)
            .show()
    }

    private fun navigateToType(typeId: Int, create: Boolean) {
        val action = TopicTypesBottomSheetDirections.actionToTopicTypeFragment(typeId, create)
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        binding.topicTypesList.adapter = adapter
        val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
        val itemDecorator = MarginItemDecoration(verticalSpace = verticalMargin)
        binding.topicTypesList.addItemDecoration(itemDecorator)
    }
}