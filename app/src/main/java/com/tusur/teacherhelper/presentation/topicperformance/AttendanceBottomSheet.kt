package com.tusur.teacherhelper.presentation.topicperformance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetAttendanceBinding
import com.tusur.teacherhelper.presentation.topicperformance.AttendanceViewModel.Event
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


class AttendanceBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAttendanceBinding? = null
    private val binding get() = _binding!!

    private val args: AttendanceBottomSheetArgs by navArgs()

    private val viewModel: AttendanceViewModel by viewModels {
        AttendanceViewModel.factory(
            topicId = args.topicId,
            currentStudentId = args.studentId,
            datetimeMillis = args.datetimeMillis,
            allStudentIds = args.allStudentIds.toList()
        )
    }

    private var wasStudentPerformanceShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window?.setWindowAnimations(R.style.Animation_App_BottomSheet)

        val studentSwapEffectAnimation =
            AnimationUtils.loadAnimation(requireContext(), R.anim.performance_student_swap)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.studentName }.collect {
                        if (!wasStudentPerformanceShown) {
                            wasStudentPerformanceShown = true
                        } else {
                            binding.root.startAnimation(studentSwapEffectAnimation)
                        }

                        binding.headline.text = it.studentName
                        binding.prevButton.isEnabled = it.hasPrevStudent
                        binding.nextButton.isEnabled = it.hasNextStudent
                    }
                }

                launch {
                    viewModel.uiState.distinctUntilChanged { old, new ->
                        old.isAbsent == new.isAbsent
                                && old.isExcused == new.isExcused
                                && old.isPresent == new.isPresent
                    }.collect {
                        binding.presentItem.isChecked = it.isPresent
                        binding.absentItem.isChecked = it.isAbsent
                        binding.excusedItem.isChecked = it.isExcused
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.nextButton.setOnClickListener {
            viewModel.send(Event.SetNextStudent)
        }

        binding.prevButton.setOnClickListener {
            viewModel.send(Event.SetPrevStudent)
        }

        binding.presentItem.setOnClickListener {
            viewModel.send(Event.SetStudentPresent)
        }

        binding.absentItem.setOnClickListener {
            viewModel.send(Event.SetStudentAbsent)
        }

        binding.excusedItem.setOnClickListener {
            viewModel.send(Event.SetStudentExcused)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}