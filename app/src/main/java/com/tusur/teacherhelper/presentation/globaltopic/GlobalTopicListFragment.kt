package com.tusur.teacherhelper.presentation.globaltopic

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialFadeThrough
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentGlobalTopicListBinding
import com.tusur.teacherhelper.domain.util.GLOBAL_TOPICS_SUBJECT_ID
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.util.setTextColor
import com.tusur.teacherhelper.presentation.core.util.setupTopLevelAppBarConfiguration
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListViewModel.Event
import com.tusur.teacherhelper.presentation.globaltopic.GlobalTopicListViewModel.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GlobalTopicListFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentGlobalTopicListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GlobalTopicListViewModel by viewModels(extrasProducer = {
        creationCallback<GlobalTopicListViewModel.Factory> { factory ->
            factory.create(resources.primaryLocale)
        }
    })

    private val adapter: GlobalTopicAdapter by lazy {
        GlobalTopicAdapter(object : BaseDeletableAdapter.Listener<GlobalTopicUiState> {
            override fun onClick(item: GlobalTopicUiState) {
                navigateToTopic(topicId = item.topicId, create = false)
            }

            override fun onDelete(item: GlobalTopicUiState) {
                showDeleteTopicDialog(
                    onConfirm = {
                        viewModel.send(Event.TryDelete(item.topicId))
                    }
                )
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()

        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlobalTopicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopLevelAppBarConfiguration(binding.topAppBar)
        setupRecyclerView()
        setupEmptyLabelGravity()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { uiState -> uiState.isFetching }
                        .collect { uiState ->
                            if (!uiState.isFetching) {
                                view.doOnPreDraw {
                                    startPostponedEnterTransition()
                                }
                            }
                        }
                }
                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { uiState -> uiState.topicsUiState }
                        .collectLatest { uiState ->
                            doOnListUpdate(uiState)
                        }
                }
                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { uiState -> uiState.isDeleting }
                        .collect { uiState ->
                            doOnDeleting(uiState)
                        }
                }
                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect { onetimeEvent ->
                        when (onetimeEvent) {
                            GlobalTopicListViewModel.OnetimeEvent.FailedToDeleteDeadline -> {
                                showDeleteErrorDialog()
                            }
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
        setupMenu()

        binding.addButton.setOnClickListener {
            navigateToTopic(topicId = NO_ID, create = true)
        }

        doOnBackPressed {
            if (!binding.searchView.isIconified) {
                setDefaultState(true)
            }
            if (viewModel.uiState.value.isDeleting) {
                viewModel.send(Event.StopDelete)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
        viewModel.send(Event.Search(searchQuery))
        return true
    }

    private fun showDeleteTopicDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun setupMenu() {
        binding.topAppBar.apply {
            menu.findItem(R.id.remove).setTextColor(
                MaterialColors.getColor(
                    binding.root,
                    com.google.android.material.R.attr.colorError
                ) // TODO: not working
            )
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.remove -> viewModel.send(Event.BeginDelete)
                    R.id.cancel -> viewModel.send(Event.StopDelete)
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun showDeleteErrorDialog() {
        TopicDeleteErrorDialog.show(requireContext())
    }

    private fun setupSearch() = with(binding.searchView) {
        setOnSearchClickListener {
            setSearchingStateMenu()
        }
        setOnCloseListener {
            viewModel.uiState.value.also {
                if (it.isDeleting) {
                    setDeleteStateMenu()
                } else {
                    setDefaultState(closeSearch = false)
                }
            }
            false
        }

        setOnQueryTextListener(this@GlobalTopicListFragment)
    }

    private fun setDeleteState() {
        binding.addButton.isVisible = false
        adapter.isDeleting = true
        setDeleteStateMenu()
    }

    private fun setDeleteStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = true
            findItem(R.id.remove).isVisible = false
        }
    }

    private fun setSearchingStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = false
            findItem(R.id.remove).isVisible = false
        }
    }

    private fun setDefaultState(closeSearch: Boolean) {
        binding.addButton.isVisible = true
        viewModel.send(Event.StopDelete)
        adapter.isDeleting = false
        setDefaultStateMenu(closeSearch)
    }

    private fun setDefaultStateMenu(closeSearch: Boolean) {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = false
            findItem(R.id.remove).isVisible = true
        }
        if (closeSearch && !binding.searchView.isIconified) {
            binding.searchView.setQuery("", true)
            binding.searchView.isIconified = true
        }
    }

    private fun doOnDeleting(uiState: UiState) {
        if (!binding.searchView.isIconified) {
            setSearchingStateMenu()
        } else {
            if (uiState.isDeleting) {
                setDeleteState()
            } else {
                setDefaultState(true)
            }
        }
    }

    private fun navigateToTopic(topicId: Int, create: Boolean) {
        val action = if (create) {
            GlobalTopicListFragmentDirections.actionToTopicNameFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID
            )
        } else {
            GlobalTopicListFragmentDirections.actionToTopicFragment(
                topicId = topicId,
                subjectId = GLOBAL_TOPICS_SUBJECT_ID,
                isJustCreated = false
            )
        }
        findNavController().navigate(action)
    }

    private fun doOnListUpdate(uiState: UiState) {
        adapter.submitList(uiState.topicsUiState)
        binding.emptyListLabel.isVisible = uiState.topicsUiState.isEmpty()
        binding.topAppBar.menu.findItem(R.id.remove).isVisible = uiState.topicsUiState.isNotEmpty()
        binding.topics.doOnPreDraw {
            binding.appBarLayout.fixCollapsing(binding.topics)
        }
    }

    private fun setupRecyclerView() {
        binding.topics.adapter = adapter
        binding.topics.addItemDecoration(getDefaultListItemDecoration(resources))
    }

    private fun setupEmptyLabelGravity() {
        binding.appBarLayout.post {
            binding.emptyListLabel.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                gravity = Gravity.CENTER
                topMargin = binding.appBarLayout.height
            }
        }
    }
}