package com.tusur.teacherhelper.presentation.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetDeadlineBinding
import com.tusur.teacherhelper.domain.util.fromEpochMillis
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.util.SingleChoiceAlertAdapter
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.util.setSingleChoiceItems
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


@AndroidEntryPoint
class DeadlineBottomSheet : BottomSheetDialogFragment() {

    private val binding get() = _binding!!
    private var _binding: BottomSheetDeadlineBinding? = null
    private val args: DeadlineBottomSheetArgs by navArgs()
    private val viewModel: DeadlineViewModel by viewModels(extrasProducer = {
        creationCallback<DeadlineViewModel.Factory> { factory ->
            factory.create(
                locale = resources.primaryLocale,
                topicId = args.topicId
            )
        }
    })


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
        _binding = BottomSheetDeadlineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { setupVisibility(it) }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect { onetimeEvent ->
                        handleOnetimeEvent(onetimeEvent)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.existDeadlineItem.setOnClickListener {
            showTopicsDeadlineDialog { close() }
        }
        binding.newDeadlineItem.setOnClickListener {
            showNewDateDialog { date ->
                viewModel.setDeadline(date)
                close()
            }
        }
        binding.noDeadlineItem.setOnClickListener {
            viewModel.removeDeadline()
            close()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun close() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            delay(100)
            dismiss()
        }
    }

    private fun showTopicsDeadlineDialog(onConfirm: () -> Unit) {
        val uiState = viewModel.uiState.value
        val adapter = SingleChoiceAlertAdapter(
            uiState.deadlineItems.map {
                SingleChoiceAlertAdapter.Item(it.isSelected, it.text.toString(requireContext()))
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_topics_deadline_title)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                uiState.deadlineItems[adapter.checkedPosition].select()
                onConfirm()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .setSingleChoiceItems(adapter)
            .show()
    }

    private fun showNewDateDialog(onConfirm: (date: LocalDate) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.dialog_new_deadline_title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build().also { picker ->
                picker.addOnPositiveButtonClickListener { dateMillis ->
                    onConfirm(LocalDate.fromEpochMillis(dateMillis))
                }
            }.show(childFragmentManager, null)
    }

    private fun setupVisibility(uiState: DeadlineViewModel.UiState) {
        if (!uiState.isDeadlineSet) {
            binding.noDeadlineItem.isVisible = false
        }
        if (!uiState.anyDeadlineExists) {
            binding.existDeadlineItem.isVisible = false
        }
    }

    private fun handleOnetimeEvent(onetimeEvent: DeadlineViewModel.OnetimeEvent) {
        when (onetimeEvent) {
            DeadlineViewModel.OnetimeEvent.FailedToDeleteDeadline -> {
                TopicDeleteErrorDialog.show(requireContext())
            }
        }
    }
}