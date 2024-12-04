package com.tusur.teacherhelper.presentation.topictype

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
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopicTypeListBinding
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.dialog.TopicDeleteErrorDialog
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.setTextColor
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import com.tusur.teacherhelper.presentation.topictype.TopicTypeListViewModel.Event
import com.tusur.teacherhelper.presentation.topictype.TopicTypeListViewModel.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class TopicTypeListFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentTopicTypeListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicTypeListViewModel by viewModels()

    private val adapter: TopicTypeAdapter by lazy {
        TopicTypeAdapter(object : BaseDeletableAdapter.Listener<TopicTypeItemUiState> {
            override fun onClick(item: TopicTypeItemUiState) {
                navigateToType(typeId = item.typeId, create = false)
            }

            override fun onDelete(item: TopicTypeItemUiState) {
                showDeleteTypeDialog(onConfirm = {
                    viewModel.send(Event.TryDelete(item.typeId))
                })
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

        if (savedInstanceState == null) {
            viewModel.send(Event.Fetch)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicTypeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupEmptyLabelGravity()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { uiState -> uiState.typesUiState }
                        .collect { uiState ->
                            doOnListUpdate(uiState)
                            if (uiState.typesUiState.isNotEmpty()) {
                                requireView().doOnPreDraw {
                                    binding.appBarLayout.fixCollapsing(binding.topicTypes)
                                    startPostponedEnterTransition()
                                }
                            }
                        }
                }

                launch {
                    viewModel.uiState
                        .distinctUntilChangedBy { it.isDeleting }
                        .collect { uiState ->
                            doOnDeleting(uiState)
                        }
                }

                launch(Dispatchers.Main.immediate) {
                    viewModel.onetimeEvent.collect { event ->
                        showDeleteErrorDialog()
                    }
                }

            }
        }

        postponeEnterTransition(300, TimeUnit.MILLISECONDS)
    }

    override fun onStart() {
        super.onStart()
        setupSearch()
        setupMenu()

        binding.addButton.setOnClickListener {
            navigateToType(typeId = NO_ID, create = true)
        }

        doOnBackPressed(binding.topAppBar) {
            if (!binding.searchView.isIconified) {
                setDefaultState(true)
            }
            if (viewModel.uiState.value.isDeleting) {
                viewModel.send(Event.StopDelete)
            } else {
                findNavController().navigateUp()
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

        setOnQueryTextListener(this@TopicTypeListFragment)
    }

    private fun setSearchingStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = false
            findItem(R.id.remove).isVisible = false
        }
    }

    private fun setDeleteStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = true
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

    private fun setDeleteState() {
        binding.addButton.isVisible = false
        adapter.isDeleting = true
        setDeleteStateMenu()
    }

    private fun navigateToType(typeId: Int, create: Boolean) {
        val action = TopicTypeListFragmentDirections.actionToTopicTypeFragment(typeId, create)
        findNavController().navigate(action)
    }

    private fun showDeleteTypeDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_topic_type_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun doOnListUpdate(uiState: UiState) {
        adapter.submitList(uiState.typesUiState)
        binding.emptyListLabel.isVisible = uiState.typesUiState.isEmpty()
        binding.topAppBar.menu.findItem(R.id.remove).isVisible = uiState.typesUiState.isNotEmpty()
    }

    private fun setupRecyclerView() {
        binding.topicTypes.adapter = adapter
        binding.topicTypes.addItemDecoration(getDefaultListItemDecoration(resources))
    }

    private fun setupEmptyLabelGravity() {
        binding.appBarLayout.post {
            binding.emptyListLabel.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                gravity = Gravity.CENTER
                topMargin = binding.appBarLayout.height
            }
        }
    }

    private fun showDeleteErrorDialog() {
        TopicDeleteErrorDialog.show(requireContext())
    }
}