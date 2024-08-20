package com.tusur.teacherhelper.presentation.topic

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.R.attr
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopicNameBinding
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.presentation.model.UiText
import com.tusur.teacherhelper.presentation.topic.TopicNameViewModel.Event
import com.tusur.teacherhelper.presentation.topic.TopicNameViewModel.OnetimeEvent
import com.tusur.teacherhelper.presentation.util.SingleChoiceAlertAdapter
import com.tusur.teacherhelper.presentation.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.util.getGroupListItemDecoration
import com.tusur.teacherhelper.presentation.util.primaryLocale
import com.tusur.teacherhelper.presentation.util.setDisabledItems
import com.tusur.teacherhelper.presentation.util.setSingleChoiceItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


class TopicNameFragment : Fragment() {

    private var _binding: FragmentTopicNameBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicNameViewModel by viewModels {
        TopicNameViewModel.factory(
            locale = resources.primaryLocale,
            subjectId = args.subjectId,
            topicId = args.topicId
        )
    }

    private val args: TopicNameFragmentArgs by navArgs()

    private val adapter = TopicTypeAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_content_main
            scrimColor = Color.TRANSPARENT
            startContainerColor = MaterialColors.getColor(
                requireContext(),
                attr.colorPrimaryContainer,
                Color.TRANSPARENT
            )
            endContainerColor =
                MaterialColors.getColor(requireContext(), attr.colorSurface, Color.TRANSPARENT)
        }
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.editText.requestFocus()
        setupRecyclerViewAdapter()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.checkedTopicName }.collect {
                        binding.confirmButton.isEnabled = it.checkedTopicName != UiText.empty
                        binding.nameAdditionOrdinal.isEnabled = it.checkedTopicName != UiText.empty
                        updateEditText(it)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChanged { old, new ->
                        old.ordinal == new.ordinal && old.date == new.date
                    }.collect {
                        updateNameAdditions(it)
                        updateEditText(it)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isCreating }.collect {
                        binding.topAppBar.setTitle(
                            if (it.isCreating) {
                                R.string.topic_name_creating_title
                            } else {
                                R.string.topic_name_editing_title
                            }
                        )
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.topicTypesItems }.collect {
                        displayTopicTypeItems(it)
                        view.doOnPreDraw { startPostponedEnterTransition() }
                    }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvents.collect { event ->
                        when (event) {
                            is OnetimeEvent.SaveFailed -> showError(event.message)

                            is OnetimeEvent.SaveSuccess -> {
                                if (event.isJustCreated) {
                                    navigateToCreateTopic(event.createdTopicId!!)
                                } else {
                                    navigateBack()
                                }
                            }

                            OnetimeEvent.OtherTypesClick -> showSecondaryTypesDialog()
                        }
                    }
                }
            }
        }

        postponeEnterTransition()
    }

    private fun updateNameAdditions(uiState: TopicNameViewModel.UiState) {
        binding.nameAdditionOrdinal.isChecked = uiState.ordinal != null
        binding.nameAdditionDate.isChecked = uiState.date != null
    }

    private fun displayTopicTypeItems(uiState: TopicNameViewModel.UiState) {
        adapter.submitList(uiState.topicTypesItems)
    }

    override fun onStart() {
        super.onStart()

        binding.nameAdditionOrdinal.setOnClickListener {
            viewModel.send(Event.AddOrRemoveOrdinal)
        }
        binding.nameAdditionDate.setOnClickListener {
            if (viewModel.uiState.value.date == null) {
                showDateSelectDialog { viewModel.send(Event.SetDate(it)) }
            } else {
                viewModel.send(Event.SetDate(null))
            }
        }

        binding.confirmButton.setOnClickListener {
            viewModel.send(Event.SetAddText(binding.editText.text?.toString()))
            viewModel.send(Event.Save)
        }

        doOnBackPressed(binding.topAppBar) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showError(uiText: UiText) {
        Toast.makeText(
            requireContext(),
            uiText.toString(
                requireContext()
            ), Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToCreateTopic(topicId: Int) {
        val action = TopicNameFragmentDirections.actionToTopicFragment(
            topicId = topicId,
            subjectId = args.subjectId,
            isJustCreated = true
        )
        findNavController().navigate(action)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun updateEditText(uiState: TopicNameViewModel.UiState) {
        val context = requireContext()
        binding.textInputLayout.prefixText = uiState.checkedTopicName.toString(context)

        binding.textInputLayout.suffixText = when {
            uiState.ordinal != null && uiState.date != null ->
                "${uiState.ordinal.toString(context)} ${uiState.date.toString(context)}"

            uiState.ordinal != null -> uiState.ordinal.toString(requireContext())
            uiState.date != null -> uiState.date.toString(context)
            else -> null
        }

        binding.editText.setText(uiState.addText?.toString(requireContext()))
    }

    private fun showDateSelectDialog(onConfirm: (date: Date) -> Unit) {
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.dialog_topic_date_title)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build().also { picker ->
                picker.addOnPositiveButtonClickListener { dateMillis ->
                    onConfirm(Date.fromMillis(dateMillis))
                }
            }.show(childFragmentManager, null)
    }

    private fun showSecondaryTypesDialog() {
        val topics = viewModel.uiState.value.secondaryTopicTypesItems
        val adapter = SingleChoiceAlertAdapter(
            topics.map {
                SingleChoiceAlertAdapter.Item(
                    isChecked = it.isSelected,
                    isEnabled = it.isEnabled,
                    text = it.name.toString(requireContext()),
                )
            }
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_other_topic_types_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setSingleChoiceItems(adapter)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                topics[adapter.checkedPosition].onSelect()
            }
            .create()
            .setDisabledItems(adapter)
            .show()
    }

    private fun setupRecyclerViewAdapter() {
        binding.topicTypeList.adapter = adapter
        binding.topicTypeList.addItemDecoration(
            getGroupListItemDecoration(resources = resources, addHorizontalSpace = false)
        )
        binding.topicTypeList.isNestedScrollingEnabled = false
    }
}
