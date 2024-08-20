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
import com.tusur.teacherhelper.presentation.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.util.primaryLocale
import com.tusur.teacherhelper.presentation.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.view.recycler.decorations.MarginItemDecoration
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class SharedClassDatesBottomSheet(
    private val topicId: Int,
    private val groupListIds: List<Int>,
    private val doOnDateConfirm: (dateMillis: Long) -> Unit,
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSharedClassDatesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedClassDatesViewModel by viewModels {
        SharedClassDatesViewModel.factory(
            topicId = topicId,
            locale = resources.primaryLocale,
            groupListIds = groupListIds
        )
    }

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
                showDeleteGroupDialog {
                    viewModel.deleteAttendance(item)
                    viewModel.stopDelete()
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
                        if (it.isFetched && it.sharedClassDays.isEmpty()) {
                            showDateSelectDialogAndNavigate()
                        }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.sharedClassDays }.collect {
                        adapter.submitList(it.sharedClassDays)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }.collect {
                        adapter.isDeleting = it.isDeleting
                        binding.deleteButton.isVisible = !it.isDeleting
                        binding.cancelDeleteButton.isVisible = it.isDeleting
                    }
                }
            }
        }
    }

    private fun showDeleteGroupDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_attendance_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
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