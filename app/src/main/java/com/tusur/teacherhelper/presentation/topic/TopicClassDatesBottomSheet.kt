package com.tusur.teacherhelper.presentation.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetSubjectClassDatesBinding
import com.tusur.teacherhelper.domain.util.fromEpochMillis
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.view.recycler.decorations.MarginItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate


@AndroidEntryPoint
class TopicClassDatesBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSubjectClassDatesBinding? = null
    private val binding get() = _binding!!

    private val args: TopicClassDatesBottomSheetArgs by navArgs()

    private val viewModel: TopicClassDatesViewModel by viewModels(extrasProducer = {
        creationCallback<TopicClassDatesViewModel.Factory> { factory ->
            factory.create(topicId = args.topicId)
        }
    })

    private lateinit var adapter: EditableDateAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
        adapter = EditableDateAdapter { datetimeUiState ->
            showDatePickerDialog(datetimeUiState.datetimeMillis) { newDateMillis ->
                showClassTimeSelectDialog(
                    newClassDate = LocalDate.fromEpochMillis(newDateMillis)
                ) { newTimeMillis ->
                    viewModel.editClassDay(
                        oldDatetimeMs = datetimeUiState.datetimeMillis,
                        newDatetimeMs = newDateMillis + newTimeMillis
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSubjectClassDatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.classDays }.collectLatest {
                        adapter.submitList(it.classDays)
                    }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect { event ->
                        when (event) {
                            TopicClassDatesViewModel.Event.DatetimeUpdated -> dismiss()
                            is TopicClassDatesViewModel.Event.UpdateError -> showDatetimeChangeError(
                                message = event.reason.toString(requireContext())
                            )
                        }
                    }
                }
            }
        }
    }

    private fun showDatetimeChangeError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.ok_button, null)
            .show()
    }

    override fun onStart() {
        super.onStart()
        doOnBackPressed {
            closeDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showClassTimeSelectDialog(
        newClassDate: LocalDate,
        onConfirm: (timeMillis: Long) -> Unit
    ) {
        ClassTimeBottomSheet(
            topicId = args.topicId,
            classDate = newClassDate
        ) { startTimeMillis ->
            onConfirm(startTimeMillis)
        }.show(childFragmentManager, null)
    }

    private fun showDatePickerDialog(datetimeMillis: Long, onConfirm: (dateMillis: Long) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.dialog_edit_class_date_title)
            .setSelection(datetimeMillis)
            .build().also { picker ->
                picker.addOnPositiveButtonClickListener { dateMillis ->
                    onConfirm(dateMillis)
                }
            }.show(childFragmentManager, null)
    }

    private fun closeDialog() {
        findNavController().navigateUp()
    }

    private fun setupRecyclerView() {
        binding.dateList.adapter = adapter
        val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
        val itemDecorator = MarginItemDecoration(verticalSpace = verticalMargin)
        binding.dateList.addItemDecoration(itemDecorator)
    }
}