package com.tusur.teacherhelper.presentation.topicperformance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetSharedClassDatesBinding
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.presentation.core.util.SingleChoiceAlertAdapter
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.util.setSingleChoiceItems
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.core.view.recycler.decorations.MarginItemDecoration
import com.tusur.teacherhelper.presentation.topicperformance.SharedClassDatesViewModel.OnetimeEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

@AndroidEntryPoint // TODO: inject
class SharedClassDatesBottomSheet(
    private val topicId: Int,
    private val groupListIds: List<Int>,
    private val doOnDateConfirm: (dateMillis: Long) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSharedClassDatesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedClassDatesViewModel by viewModels(extrasProducer = {
        creationCallback<SharedClassDatesViewModel.Factory> { factory ->
            factory.create(
                topicId = topicId,
                locale = resources.primaryLocale,
                groupListIds = groupListIds
            )
        }
    })

    private lateinit var adapter: DeletableDateAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
        adapter = DeletableDateAdapter(object : BaseDeletableAdapter.Listener<DateItemUiState> {
            override fun onClick(item: DateItemUiState) {
                doOnDateConfirm(item.dateMillis)
            }

            override fun onDelete(item: DateItemUiState) {
                showDeleteDateDialog(item) { timeItemUiState ->
                    timeItemUiState.onDelete()
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSharedClassDatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isFetched }.collect {
                        if (it.isFetched && it.sharedClassDates.isEmpty()) {
                            showDateSelectDialogAndNavigate()
                        }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.sharedClassDates }.collect {
                        adapter.submitList(it.sharedClassDates)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }.collect {
                        adapter.isDeleting = it.isDeleting
                        binding.deleteButton.isVisible = !it.isDeleting
                        binding.cancelDeleteButton.isVisible = it.isDeleting
                    }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect {
                        if (it is OnetimeEvent.ClassDateDeleted) {
                            dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDateDialog(
        dateItem: DateItemUiState,
        onConfirm: (timeItemUiState: TimeItemUiState) -> Unit
    ) {
        val classTimeItemsUiState = viewModel.uiState.value.classDateTimeOfEach[dateItem]!!
        val adapter = SingleChoiceAlertAdapter(
            items = classTimeItemsUiState.map {
                SingleChoiceAlertAdapter.Item(
                    isChecked = false,
                    text = it.timeText.toString(requireContext()),
                    isEnabled = true
                )
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_attendance_title)
            .setSingleChoiceItems(adapter)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke(classTimeItemsUiState[adapter.checkedPosition])
            }.show()
    }

    private fun showDateSelectDialogAndNavigate() {
        showDateSelectDialog { selectedDate ->
            doOnDateConfirm(selectedDate.toMillis())
        }
        dismiss()
    }

    private fun showDateSelectDialog(onConfirm: (date: Date) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.dialog_topic_date_title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build().also { picker ->
                picker.addOnPositiveButtonClickListener { dateMillis ->
                    onConfirm(Date.fromMillis(dateMillis))
                }
            }.show(parentFragmentManager, null)
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
            showDateSelectDialogAndNavigate()
        }

        doOnBackPressed { dismiss() }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.dateList.adapter = adapter
        val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
        val itemDecorator = MarginItemDecoration(verticalSpace = verticalMargin)
        binding.dateList.addItemDecoration(itemDecorator)
    }
}