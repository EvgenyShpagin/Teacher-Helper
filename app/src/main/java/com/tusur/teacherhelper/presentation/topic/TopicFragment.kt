package com.tusur.teacherhelper.presentation.topic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopicBinding
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.model.UiText
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.topic.TopicViewModel.OnetimeEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TopicFragment : Fragment() {

    private var _binding: FragmentTopicBinding? = null
    private val binding get() = _binding!!

    private val args: TopicFragmentArgs by navArgs()

    private val viewModel: TopicViewModel by viewModels(extrasProducer = {
        creationCallback<TopicViewModel.Factory> { factory ->
            factory.create(
                subjectId = args.subjectId,
                topicId = args.topicId,
                isJustCreated = args.isJustCreated,
                locale = resources.primaryLocale
            )
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.fetch()
        }

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_content_main
            scrimColor = Color.TRANSPARENT
            startContainerColor = MaterialColors.getColor(
                requireContext(),
                com.google.android.material.R.attr.colorSurfaceContainerLowest,
                Color.TRANSPARENT
            )
            endContainerColor =
                MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorSurface,
                    Color.TRANSPARENT
                )
        }
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest {
                        display(it)
                        view.doOnPreDraw { startPostponedEnterTransition() }
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

    private fun display(uiState: TopicViewModel.UiState) = binding.apply {
        editText.setText(uiState.topicName)
        topAppBar.setTitle(
            if (uiState.isJustCreated) {
                R.string.topic_creating_title
            } else {
                R.string.topic_editing_title
            }
        )
        performanceItem.isVisible = uiState.let {
            it.supportsGrades || it.supportsAssessment || it.supportsProgress
        }
        attendanceItem.isVisible = uiState.supportsAttendance

        deadlineItem.isVisible = uiState.supportsDeadline
        if (uiState.deadlineText == UiText.empty) {
            deadlineItem.setTrailingDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_arrow_right_24
                )
            )
        } else {
            deadlineItem.trailingSupportText =
                uiState.deadlineText.toString(requireContext())
        }

        classDaysItem.isVisible = uiState.hasClassDays
        dateGroup.isVisible = binding.dateGroup.hasVisibleItems
    }

    override fun onStart() {
        super.onStart()

        binding.performanceItem.setOnClickListener {
            navigateToPerformance()
        }

        binding.attendanceItem.setOnClickListener {
            navigateToAttendance()
        }

        binding.deadlineItem.setOnClickListener {
            navigateToDeadline()
        }

        binding.classDaysItem.setOnClickListener {
            navigateToClassDays()
        }

        binding.cancelItem.setOnClickListener {
            showCancelDialog { viewModel.cancelTopic() }
        }

        binding.deleteItem.setOnClickListener {
            showDeleteDialog { viewModel.deleteTopic() }
        }

        doOnBackPressed(binding.topAppBar) {
            navigateBack()
        }

        binding.editText.setOnClickListener {
            navigateToEditName()
        }
    }

    private fun navigateToEditName() {
        val action = TopicFragmentDirections.actionToTopicNameFragment(
            topicId = args.topicId,
            subjectId = args.subjectId
        )
        findNavController().navigate(action)
    }

    private fun navigateToClassDays() {
        val action = TopicFragmentDirections
            .actionToSubjectClassDatesBottomSheet(topicId = args.topicId)
        findNavController().navigate(action)
    }

    private fun navigateToDeadline() {
        val action = TopicFragmentDirections.actionToDeadlineBottomSheet(topicId = args.topicId)
        findNavController().navigate(action)
    }

    private fun navigateToAttendance() {
        val action = TopicFragmentDirections
            .actionToSubjectGroupSelectFragment(
                subjectId = args.subjectId,
                shouldBeAllChecked = true,
                topicId = args.topicId,
                requiredPerformance = PerformanceType.ATTENDANCE
            )
        findNavController().navigate(action)
    }

    private fun navigateToPerformance() {
        val action = TopicFragmentDirections.actionToSubjectGroupSelectFragment(
            subjectId = args.subjectId,
            shouldBeAllChecked = false,
            topicId = args.topicId,
            requiredPerformance = PerformanceType.OTHER_PERFORMANCE
        )
        findNavController().navigate(action)
    }

    private fun navigateBack() {
        if (args.isJustCreated) {
            findNavController().popBackStack(R.id.topicNameFragment, inclusive = true)
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showCancelDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_cancel_topic_title)
            .setNegativeButton(R.string.back_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun showDeleteDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_title)
            .setNegativeButton(R.string.back_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun handleOnetimeEvent(event: OnetimeEvent) {
        when (event) {
            OnetimeEvent.FailedToDeleteDeadline -> TopicDeleteErrorDialog.show(requireContext())
            OnetimeEvent.NavigateBack -> navigateBack()
        }
    }
}