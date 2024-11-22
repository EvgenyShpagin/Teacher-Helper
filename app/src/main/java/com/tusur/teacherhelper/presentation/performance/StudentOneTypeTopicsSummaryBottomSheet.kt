package com.tusur.teacherhelper.presentation.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.databinding.BottomSheetStudentOneTypeTopicsSummaryBinding
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import kotlinx.coroutines.launch


class StudentOneTypeTopicsSummaryBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetStudentOneTypeTopicsSummaryBinding? = null
    private val binding get() = _binding!!

    private val args: StudentOneTypeTopicsSummaryBottomSheetArgs by navArgs()

    private val viewModel: StudentOneTypeTopicsResultsViewModel by viewModels {
        StudentOneTypeTopicsResultsViewModel.factory(
            locale = resources.primaryLocale,
            performanceType = args.performanceType,
            subjectId = args.subjectId,
            studentId = args.studentId,
            topicTypeId = args.topicTypeId
        )
    }

    private val adapter = TopicSummaryAdapter()


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
        _binding = BottomSheetStudentOneTypeTopicsSummaryBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    binding.title.text = it.title.toString(requireContext())
                    adapter.submitList(it.topicUiItems)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.topicsList.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}