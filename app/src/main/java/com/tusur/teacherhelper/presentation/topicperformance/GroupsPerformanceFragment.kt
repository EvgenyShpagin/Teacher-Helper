package com.tusur.teacherhelper.presentation.topicperformance

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.Slide
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentGroupsPerformanceBinding
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnNavigationRequest
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.util.toNativeArray
import com.tusur.teacherhelper.presentation.topic.PerformanceType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GroupsPerformanceFragment : Fragment(), OnQueryTextListener {

    private var _binding: FragmentGroupsPerformanceBinding? = null
    private val binding get() = _binding!!
    private val args: GroupsPerformanceFragmentArgs by navArgs()

    private val viewModel: GroupsPerformanceViewModel by viewModels(extrasProducer = {
        creationCallback<GroupsPerformanceViewModel.Factory> { factory ->
            factory.create(
                locale = resources.primaryLocale,
                topicId = args.topicId,
                performanceType = args.performanceType,
                datetimeMillis = args.datetimeMillis,
                groupIdList = args.groupListIds.toList()
            )
        }
    })

    private val adapter = StudentPerformanceAdapter { studentId ->
        showPerformanceDialog(studentId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
        enterTransition = Slide(Gravity.END)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupsPerformanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isFetching }.collect {
                        if (it.isFetching) return@collect
                        binding.topAppBar.setTitle(it.topicName)
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.groupItemsUiState }.collect {
                        adapter.submitList(it.groupItemsUiState)
                        view.doOnPreDraw {
                            startPostponedEnterTransition()
                        }
                    }
                }
            }
        }

        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()

        setupSearch()

        binding.topAppBar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.name_display_type) {
                viewModel.changeNameDisplayType()
                true
            } else {
                false
            }
        }

        binding.acceptButton.setOnClickListener {
            navigateToTopic()
        }

        doOnNavigationRequest(topAppBar = binding.topAppBar) {
            if (!binding.searchView.isIconified) {
                viewModel.stopSearch()
                binding.searchView.setQuery("", true)
                binding.searchView.isIconified = true
            } else {
                navigateBack()
            }
        }
    }

    private fun navigateToTopic() {
        navigateBack(R.id.topicFragment)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        binding.searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query == null) {
            return true
        }
        val searchQuery = "%$query%"
        viewModel.search(searchQuery)
        return true
    }

    private fun showPerformanceDialog(studentId: Int) {
        val navController = findNavController()
        val action = when (args.performanceType) {
            PerformanceType.ATTENDANCE ->
                GroupsPerformanceFragmentDirections.actionToAttendanceBottomSheet(
                    topicId = args.topicId,
                    studentId = studentId,
                    datetimeMillis = args.datetimeMillis,
                    allStudentIds = viewModel.getAllStudentIds().toNativeArray()
                )

            PerformanceType.OTHER_PERFORMANCE ->
                GroupsPerformanceFragmentDirections.actionToPerformanceBottomSheet(
                    topicId = args.topicId,
                    studentId = studentId,
                    datetimeMillis = args.datetimeMillis,
                    allStudentIds = viewModel.getAllStudentIds().toNativeArray()
                )
        }
        navController.navigate(action)
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    private fun navigateBack(destinationId: Int) {
        findNavController().popBackStack(destinationId, false)
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(this)
        binding.searchView.setOnCloseListener {
            viewModel.stopSearch()
            false
        }
    }

    private fun setupRecyclerView() {
        binding.studentList.adapter = adapter
        binding.studentList.addItemDecoration(getDefaultListItemDecoration(resources))
    }
}