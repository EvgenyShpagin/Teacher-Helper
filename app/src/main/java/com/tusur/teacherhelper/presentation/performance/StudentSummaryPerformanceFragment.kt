package com.tusur.teacherhelper.presentation.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentStudentSummaryPerformanceBinding
import com.tusur.teacherhelper.presentation.performance.StudentPerformanceViewModel.Event
import com.tusur.teacherhelper.presentation.topic.PerformanceType
import com.tusur.teacherhelper.presentation.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.util.getListItemAt
import com.tusur.teacherhelper.presentation.util.primaryLocale
import com.tusur.teacherhelper.presentation.view.ListItemView
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


class StudentSummaryPerformanceFragment : Fragment() {

    private var _binding: FragmentStudentSummaryPerformanceBinding? = null
    private val binding get() = _binding!!

    private val args: StudentSummaryPerformanceFragmentArgs by navArgs()
    private val viewModel: StudentPerformanceViewModel by viewModels {
        StudentPerformanceViewModel.factory(
            locale = resources.primaryLocale,
            subjectId = args.subjectId,
            studentId = args.studentId,
            groupId = args.groupId
        )
    }

    private var wasStudentPerformanceShown = false
    private lateinit var studentSwapEffectAnimation: Animation


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentSummaryPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
        studentSwapEffectAnimation = AnimationUtils.loadAnimation(
            requireContext(), R.anim.performance_student_swap
        )
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.studentName }.collect {
                        updateUiOnStudentSwitch(it)
                        view.doOnPreDraw { startPostponedEnterTransition() }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.topicTypesUiItems }.collect {
                        updateListItems(it.topicTypesUiItems)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.topicUiItems }.collect {
                        updateTopicChips(it.topicUiItems)
                    }
                }
            }
        }
        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()
        binding.switchButtons.nextButton.setOnClickListener {
            viewModel.send(Event.SetNextStudent)
        }
        binding.switchButtons.prevButton.setOnClickListener {
            viewModel.send(Event.SetPrevStudent)
        }
        binding.setResultGradeButton.setOnClickListener {
            navigateToSetFinalGrade()
        }

        doOnBackPressed(binding.topAppBar) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTopicChips(topicUiItems: List<TopicUiItem>) {
        if (binding.chipGroup.isEmpty()) {
            createTopicChips(topicUiItems)
        }
        for (i in 0 until binding.chipGroup.childCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            chip.isChecked = topicUiItems[i].isTakenInAccount
        }
    }

    private fun updateListItems(topicTypesUiItems: List<TopicTypeUiItem>) {
        if (!binding.topicsAttendanceListLayout.hasVisibleItems &&
            !binding.topicsProgressListLayout.hasVisibleItems
        ) {
            createListItems(topicTypesUiItems)
        }

        var progressListItemIndex = 0
        var attendanceListItemIndex = 0
        topicTypesUiItems.forEach { uiItem ->
            if (uiItem.hasAttendance) {
                binding.topicsAttendanceListLayout.getListItemAt(attendanceListItemIndex)?.apply {
                    title = uiItem.name.toString(requireContext())
                    trailingSupportText = uiItem.totalAttendance.toString(requireContext())
                    ++attendanceListItemIndex
                }
            }
            if (uiItem.hasProgress) {
                binding.topicsProgressListLayout.getListItemAt(progressListItemIndex)?.apply {
                    title = uiItem.name.toString(requireContext())
                    trailingSupportText = uiItem.totalProgress.toString(requireContext())
                    ++progressListItemIndex
                }
            }
        }
    }

    private fun createListItem(onClickListener: View.OnClickListener): ListItemView {
        return ListItemView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                resources.getDimension(R.dimen.default_list_item_height).toInt()
            )
            leadingComponent = ListItemView.Component.NOTHING
            trailingComponent = ListItemView.Component.SUPPORT_TEXT
            setOnClickListener(onClickListener)
        }
    }

    private fun createListItems(topicTypesUiItems: List<TopicTypeUiItem>) {
        topicTypesUiItems.forEach { typeItem ->
            if (typeItem.hasProgress) {
                binding.topicsProgressListLayout.addView(
                    createListItem {
                        navigateToOneTypeTopicsResults(
                            args.studentId,
                            typeItem.typeId,
                            PerformanceType.OTHER_PERFORMANCE
                        )
                    }
                )
            }
            if (typeItem.hasAttendance) {
                binding.topicsAttendanceListLayout.addView(
                    createListItem {
                        navigateToOneTypeTopicsResults(
                            args.studentId,
                            typeItem.typeId,
                            PerformanceType.ATTENDANCE
                        )
                    }
                )
            }
        }
    }

    private fun navigateToOneTypeTopicsResults(
        studentId: Int,
        typeId: Int,
        performanceType: PerformanceType
    ) {
        val action = StudentSummaryPerformanceFragmentDirections
            .actionToStudentOneTypeTopicsResultsBottomSheet(
                performanceType = performanceType,
                studentId = studentId,
                topicTypeId = typeId,
                subjectId = args.subjectId
            )
        findNavController().navigate(action)
    }

    private fun navigateToSetFinalGrade() {
        val action = StudentSummaryPerformanceFragmentDirections
            .actionToGlobalTopicsToFinalGradeBottomSheet(args.studentId)
        findNavController().navigate(action)
    }

    private fun createTopicChips(topicUiItems: List<TopicUiItem>) {
        topicUiItems.forEach { topicItem ->
            val chip = View.inflate(context, R.layout.filter_chip, null) as Chip
            chip.text = topicItem.name.toString(requireContext())
            chip.isChecked = topicItem.isTakenInAccount
            chip.setOnClickListener { topicItem.onClick() }
            binding.chipGroup.addView(chip)
        }
    }

    private fun updateUiOnStudentSwitch(state: StudentPerformanceViewModel.UiState) {
        if (!wasStudentPerformanceShown) {
            wasStudentPerformanceShown = true
        } else {
            binding.root.startAnimation(studentSwapEffectAnimation)
        }
        binding.headline.text = state.studentName.toString(requireContext())
        binding.switchButtons.nextButton.isEnabled = state.hasNextStudent
        binding.switchButtons.prevButton.isEnabled = state.hasPrevStudent
    }
}