package com.tusur.teacherhelper.presentation.performance

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentPerformanceTableBinding
import com.tusur.teacherhelper.domain.util.currentMinute
import com.tusur.teacherhelper.domain.util.fromEpochMillis
import com.tusur.teacherhelper.domain.util.map
import com.tusur.teacherhelper.domain.util.toEpochMillis
import com.tusur.teacherhelper.presentation.core.dialog.EmptyGroupDialog
import com.tusur.teacherhelper.presentation.core.util.EXCEL_FILE_NEW_MIME_TYPE
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnNavigationRequest
import com.tusur.teacherhelper.presentation.topic.ClassTimeBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime


@AndroidEntryPoint
class PerformanceTableFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentPerformanceTableBinding? = null
    private val args: PerformanceTableFragmentArgs by navArgs()
    private val viewModel: PerformanceTableViewModel by viewModels(
        extrasProducer = {
            creationCallback<PerformanceTableViewModel.Factory> { factory ->
                factory.create(
                    subjectId = args.subjectId,
                    groupId = args.groupId
                )
            }
        })

    private val createExcelFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument(EXCEL_FILE_NEW_MIME_TYPE)
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        val outputStream = requireActivity().contentResolver.openOutputStream(uri)
        viewModel.savePerformanceToFile(outputStream!!, requireContext()) { mime ->
            openCreatedFile(uri, mime)
        }
    }

    private var emptyDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerformanceTableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val widthIsPrettyLarge = resources.getBoolean(R.bool.width_at_least_600dp)
        viewModel.showFullName = widthIsPrettyLarge
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.group }.collect {
                        binding.topAppBar.title = getString(
                            R.string.group_performance_title,
                            it.group.toString(requireContext())
                        )
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.tableContent }.collect { state ->
                        binding.tableView.set(
                            state.tableContent.map { it.toString(requireContext()) }
                        )
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.thereAreNoNonEmptyGroups || it.thereAreNoNotCancelledTopics }
                        .collect { state ->
                            if (state.thereAreNoNonEmptyGroups) {
                                showEmptyGroupListDialog {
                                    navigateBack()
                                }
                            } else if (state.thereAreNoNotCancelledTopics) {
                                showEmptyTopicListDialog {
                                    navigateBack()
                                }
                            } else {
                                view.doOnPreDraw { startPostponedEnterTransition() }
                            }
                        }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect {
                        when (it) {
                            is PerformanceTableViewModel.OnetimeEvent.SetTopicPerformance -> {
                                if (it.datetimeMs == null) {
                                    showDatePickerDialog(
                                        datetimeMillis = LocalDateTime
                                            .currentMinute()
                                            .toEpochMillis()
                                    ) { dateMillis ->
                                        showClassTimeDialog(
                                            topicId = it.topicId,
                                            groupId = it.groupId,
                                            classDate = LocalDate.fromEpochMillis(dateMillis)
                                        ) { datetimeMillis ->
                                            navigateToStudentTopicPerformance(
                                                studentId = it.studentId,
                                                topicId = it.topicId,
                                                classDayDatetimeMs = datetimeMillis
                                            )
                                        }
                                    }
                                } else {
                                    navigateToStudentTopicPerformance(
                                        studentId = it.studentId,
                                        topicId = it.topicId,
                                        classDayDatetimeMs = it.datetimeMs
                                    )
                                }
                            }

                            is PerformanceTableViewModel.OnetimeEvent.ShowStudentSummaryPerformance ->
                                navigateToStudentTotalPerformance(studentId = it.studentId)
                        }
                    }
                }
            }
        }
        postponeEnterTransition()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onStart() {
        super.onStart()

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.excel_export -> createFile()
                else -> return@setOnMenuItemClickListener false
            }
            true
        }

        binding.tableView.onClickListener = object : TableView.OnClickListener {
            override fun onCellClick(columnIndex: Int, rowIndex: Int) {
                viewModel.click(columnIndex, rowIndex)
            }

            override fun onLabelClick(ordinal: Int) {
                // TODO: implement
            }
        }

        doOnNavigationRequest(binding.topAppBar) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createFile() {
        createExcelFileLauncher.launch(viewModel.uiState.value.group.toString(requireContext()))
    }

    private fun openCreatedFile(uri: Uri, mime: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(uri, mime)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }

    private fun navigateToStudentTopicPerformance(
        studentId: Int,
        topicId: Int,
        classDayDatetimeMs: Long
    ) {
        val action = PerformanceTableFragmentDirections.actionToFinalGradeBottomSheet(
            topicId = topicId,
            studentId = studentId,
            datetimeMillis = classDayDatetimeMs
        )
        findNavController().navigate(action)
    }

    private fun navigateToStudentTotalPerformance(studentId: Int) {
        val action = PerformanceTableFragmentDirections
            .actionToStudentPerformanceFragment(
                studentId = studentId,
                groupId = args.groupId,
                subjectId = args.subjectId
            )
        findNavController().navigate(action)
    }

    private fun showEmptyGroupListDialog(onConfirm: () -> Unit) {
        if (emptyDialog?.isShowing == true) return
        emptyDialog = EmptyGroupDialog(requireContext()) {
            emptyDialog = null
            onConfirm.invoke()
        }.show()
    }

    private fun showEmptyTopicListDialog(onConfirm: () -> Unit) {
        if (emptyDialog?.isShowing == true) return
        emptyDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_no_not_cancelled_topics_title)
            .setMessage(R.string.dialog_no_not_cancelled_topics)
            .setCancelable(false)
            .setPositiveButton(R.string.back_button) { _, _ ->
                emptyDialog = null
                onConfirm()
            }.create()
        emptyDialog!!.show()
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

    private fun showClassTimeDialog(
        topicId: Int,
        groupId: Int,
        classDate: LocalDate,
        onSelect: (datetimeMillis: Long) -> Unit
    ) {
        ClassTimeBottomSheet(
            topicId = topicId,
            groupListIds = listOf(groupId),
            classDate = classDate
        ) { initTimeMs ->
            onSelect.invoke(classDate.toEpochMillis() + initTimeMs)
        }.show(childFragmentManager, null)
    }
}