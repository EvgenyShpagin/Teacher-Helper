package com.tusur.teacherhelper.presentation.topictype

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.databinding.FragmentTopicTypeBinding
import com.tusur.teacherhelper.domain.util.TOPIC_TYPE_SHORT_NAME_MAX_LENGTH
import com.tusur.teacherhelper.presentation.core.util.clearFocusOnActionDone
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TopicTypeFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentTopicTypeBinding? = null
    private val args: TopicTypeFragmentArgs by navArgs()
    private val viewModel: TopicTypeViewModel by viewModels(extrasProducer = {
        creationCallback<TopicTypeViewModel.Factory> { factory ->
            factory.create(
                isCreating = args.isCreating,
                typeId = args.topicTypeId
            )
        }
    })


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
        _binding = FragmentTopicTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    updateUi(it)
                    view.doOnPreDraw { startPostponedEnterTransition() }
                }
            }
        }
        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()

        binding.apply {
            shortNameEditText.filters += InputFilter.LengthFilter(TOPIC_TYPE_SHORT_NAME_MAX_LENGTH)
            shortNameEditText

            assessmentItem.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckAssessment)
            }

            progressItem.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckProgress)
            }

            gradeItem.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckGrade)
            }

            attendanceItem.setOnClickListener {
                if (viewModel.uiState.value.attendanceAcceptable) {
                    if (attendanceVariants.isVisible) {
                        attendanceVariants.isVisible = false
                    } else {
                        viewModel.send(TopicTypeViewModel.Event.CheckAttendance)
                    }
                } else {
                    attendanceVariants.isVisible = !attendanceVariants.isVisible
                    viewModel.send(TopicTypeViewModel.Event.CheckAttendance)
                }
            }

            attendanceOneDay.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckOneDayAttendance)
            }
            deadline.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckDeadline)
            }

            attendanceMultipleDays.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.CheckMultipleDaysAttendance)
            }

            nameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.send(TopicTypeViewModel.Event.NameUpdate(text?.toString()))
            }

            shortNameEditText.doOnTextChanged { text, _, _, _ ->
                viewModel.send(TopicTypeViewModel.Event.ShortNameUpdate(text?.toString()))
            }

            nameEditText.clearFocusOnActionDone()
            shortNameEditText.clearFocusOnActionDone()

            confirmButton.setOnClickListener {
                viewModel.send(TopicTypeViewModel.Event.SaveChanges)
                navigateBack()
            }
        }

        doOnBackPressed(binding.topAppBar) {
            navigateBack()
        }
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUi(uiState: TopicTypeViewModel.UiState) = binding.apply {
        val context = requireContext()
        if (topAppBar.title.isNullOrEmpty()) {
            topAppBar.setTitle(uiState.title.toString(context))
            nameEditText.setText(uiState.typeName.toString(context))
            shortNameEditText.setText(uiState.typeShortName.toString(context))
        }
        shortNameTextInputLayout.isVisible = uiState.needToShowShortNameInput
        assessmentItem.isChecked = uiState.assessmentAcceptable
        gradeItem.isChecked = uiState.gradeAcceptable
        progressItem.isChecked = uiState.progressAcceptable
        attendanceItem.isChecked = uiState.attendanceAcceptable.also {
            if (!it) {
                attendanceVariants.isVisible = false
            }
        }
        attendanceOneDay.isChecked = uiState.attendanceOnlyOneDayAcceptable
        attendanceMultipleDays.isChecked = !uiState.attendanceOnlyOneDayAcceptable
        deadline.isChecked = uiState.deadlineAcceptable
        confirmButton.isEnabled = uiState.allowedToSave
    }
}