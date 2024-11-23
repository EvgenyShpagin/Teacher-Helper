package com.tusur.teacherhelper.presentation.topicperformance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetPerformanceBinding
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.SingleChoiceAlertAdapter
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.formatProgress
import com.tusur.teacherhelper.presentation.core.util.setSingleChoiceItems
import com.tusur.teacherhelper.presentation.core.view.ListItemView
import com.tusur.teacherhelper.presentation.topicperformance.PerformanceViewModel.Event
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
open class PerformanceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetPerformanceBinding? = null
    protected val binding get() = _binding!!

    private val args: PerformanceBottomSheetArgs by navArgs()

    protected open val viewModel: PerformanceViewModel by viewModels(extrasProducer = {
        creationCallback<PerformanceViewModel.Factory> { factory ->
            factory.create(
                topicId = args.topicId,
                currentStudentId = args.studentId,
                datetimeMillis = args.datetimeMillis,
                allStudentIds = args.allStudentIds.toList()
            )
        }
    })

    private var wasStudentPerformanceShown = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window?.setWindowAnimations(R.style.Animation_App_BottomSheet)

        val studentSwapEffectAnimation = AnimationUtils.loadAnimation(
            requireContext(), R.anim.performance_student_swap
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isFetched }.collect {
                        setupPerformanceItemsVisibility(it)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.studentName }.collect {
                        if (!wasStudentPerformanceShown) {
                            wasStudentPerformanceShown = true
                        } else {
                            binding.root.startAnimation(studentSwapEffectAnimation)
                        }

                        binding.headline.text = it.studentName.toString(requireContext())
                        setupButtons(it)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChanged { old, new ->
                        (old.grade == new.grade
                                && old.assessmentIcon == new.assessmentIcon
                                && old.progress == new.progress)
                    }.collect { state ->
                        setupPerformanceItemsValues(state)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.gradeItem.setOnClickListener {
            showGradeDialog()
        }

        binding.progressItem.setOnClickListener {
            showProgressDialog(viewModel.uiState.value.progressPercent)
        }

        binding.assessmentItem.setOnClickListener {
            showAssessmentDialog()
        }

        binding.switchButtons.prevButton.setOnClickListener {
            viewModel.send(Event.SetPrevStudent)
        }

        binding.switchButtons.nextButton.setOnClickListener {
            viewModel.send(Event.SetNextStudent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupButtons(uiState: PerformanceViewModel.UiState) {
        binding.switchButtons.prevButton.isEnabled = uiState.hasPrevStudent
        binding.switchButtons.nextButton.isEnabled = uiState.hasNextStudent
    }

    private fun setupPerformanceItemsValues(uiState: PerformanceViewModel.UiState) {
        val context = requireContext()
        val noValueDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_right_24)

        fun setTextOrDrawable(item: ListItemView, text: UiText) {
            if (text == UiText.empty) {
                item.setTrailingDrawable(noValueDrawable)
            } else {
                item.trailingSupportText = text.toString(context)
            }
        }

        binding.apply {
            setTextOrDrawable(gradeItem, uiState.grade)
            setTextOrDrawable(progressItem, uiState.progress)
            val assessmentDrawable = uiState.assessmentIcon?.toDrawable(context)
            assessmentItem.setTrailingDrawable(assessmentDrawable, keepOriginColor = true)
        }
    }

    private fun showProgressDialog(progressPercent: Int) {
        var slider: Slider? = null
        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.progress_title)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                viewModel.send(Event.SetProgress(slider!!.value.toInt()))
            }
            .setNegativeButton(R.string.cancel_button, null)
            .setView(R.layout.progress_slider)
            .create()
        alertDialog.setOnShowListener {
            slider = alertDialog.findViewById(R.id.slider)
            slider!!.value = progressPercent.toFloat()
            slider!!.setLabelFormatter { formatProgress(it.toInt()) }
        }
        alertDialog.show()
    }

    private fun showGradeDialog() {
        val uiState = viewModel.uiState.value
        val adapter = SingleChoiceAlertAdapter(
            uiState.gradeItems.map {
                SingleChoiceAlertAdapter.Item(it.isSelected, it.text.toString(requireContext()))
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.grade_title)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                uiState.gradeItems[adapter.checkedPosition].onCheck()
                if (viewModel.shouldSetProgress) {
                    showProgressDialog(viewModel.suggestedProgressPercent)
                }
            }
            .setNegativeButton(R.string.cancel_button, null)
            .setSingleChoiceItems(adapter)
            .show()
    }

    private fun showAssessmentDialog() {
        val uiState = viewModel.uiState.value
        val adapter = SingleChoiceAlertAdapter(
            uiState.assessmentItems.map {
                SingleChoiceAlertAdapter.Item(it.isSelected, it.text.toString(requireContext()))
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.assessment_title)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                uiState.assessmentItems[adapter.checkedPosition].onCheck()
            }
            .setNegativeButton(R.string.cancel_button, null)
            .setSingleChoiceItems(adapter)
            .show()
    }

    private fun setupPerformanceItemsVisibility(uiState: PerformanceViewModel.UiState) {
        binding.progressItem.isVisible = uiState.isProgressAcceptable
        binding.gradeItem.isVisible = uiState.isGradeAcceptable
        binding.assessmentItem.isVisible = uiState.isAssessmentAcceptable
    }
}