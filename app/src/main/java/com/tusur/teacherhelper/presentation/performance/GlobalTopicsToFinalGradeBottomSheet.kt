package com.tusur.teacherhelper.presentation.performance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetGlobalTopicsBinding
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.core.view.recycler.decorations.MarginItemDecoration
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicAdapter
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListViewModel
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListViewModel.Event
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

@AndroidEntryPoint // TODO: was not tested
class GlobalTopicsToFinalGradeBottomSheet : BottomSheetDialogFragment() {

    private val binding get() = _binding!!
    private var _binding: BottomSheetGlobalTopicsBinding? = null
    private val viewModel: GlobalTopicListViewModel by viewModels(
        extrasProducer = {
            creationCallback<GlobalTopicListViewModel.Factory> { factory ->
                factory.create(resources.primaryLocale)
            }
        }
    )

    private val args: GlobalTopicsToFinalGradeBottomSheetArgs by navArgs()

    private val adapter: GlobalTopicAdapter by lazy {
        GlobalTopicAdapter(object : BaseDeletableAdapter.Listener<GlobalTopicUiState> {
            override fun onClick(item: GlobalTopicUiState) {
                navigateToPerformance(item.topicId)
            }

            override fun onDelete(item: GlobalTopicUiState) {}
        })
    }

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
        _binding = BottomSheetGlobalTopicsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteButton.isVisible = false
        binding.addButton.isVisible = false
        setupRecyclerView()
        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.topicsUiState }
                        .collect { adapter.submitList(it.topicsUiState) }
                }
            }
        }
    }


    private fun navigateToPerformance(topicId: Int) {
        val action = GlobalTopicsToFinalGradeBottomSheetDirections.actionToFinalGradeBottomSheet(
            topicId = topicId,
            studentId = args.studentId,
            datetimeMillis = Date.current().toMillis()
        )
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        binding.topicList.adapter = adapter
        val verticalMargin = resources.getDimension(R.dimen.group_list_item_vertical_margin)
        val itemDecorator = MarginItemDecoration(verticalSpace = verticalMargin)
        binding.topicList.addItemDecoration(itemDecorator)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}