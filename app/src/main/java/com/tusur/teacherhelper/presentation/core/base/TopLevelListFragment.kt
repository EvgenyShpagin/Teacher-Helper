package com.tusur.teacherhelper.presentation.core.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFadeThrough
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopLevelListBinding
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.launchOnOwnerStart
import com.tusur.teacherhelper.presentation.core.util.setTextColor
import com.tusur.teacherhelper.presentation.core.util.setupTopLevelAppBarConfiguration
import com.tusur.teacherhelper.presentation.core.view.recycler.BaseDeletableAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

/**
 * Base fragment for all top-level destinations which are being navigated through Drawer
 * and have same functionality:
 *  delete, add, and search items.
 *
 * Each fragment also has the same components:
 *  collapsing toolbar, recyclerview, FAB, and same menu.
 *
 * Supports top-level transitions (MaterialFadeThrough)
 *
 * @param State screen's UI state
 * @param ItemState each list item's UI state used by adapter
 * @param Effect effect to Fragment from ViewModel
 */
abstract class TopLevelListFragment<ItemState,
        State : TopLevelListUiState<ItemState>,
        Effect : TopLevelListUiEffect> : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentTopLevelListBinding? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: TopLevelListViewModel<State, Effect>
    protected abstract val adapter: BaseDeletableAdapter<ItemState>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            viewModel.onEvent(Event.Fetch)
        }

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopLevelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopLevelAppBarConfiguration(binding.topAppBar)
        setupRecyclerView()
        setupEmptyLabelGravity()

        setupSearch()
        setupMenu()

        launchOnOwnerStart {
            initCollectors(this)
        }

        binding.addButton.setOnClickListener {
            onAddButtonClick()
        }

        doOnBackPressed {
            if (!binding.searchView.isIconified) {
                setDefaultState(true)
            }
            if (viewModel.uiState.value.isDeleting) {
                viewModel.onEvent(Event.StopDelete)
            }
        }
    }

    protected open fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@TopLevelListFragment.adapter
            addItemDecoration(getDefaultListItemDecoration(resources))
        }
    }

    private fun setupEmptyLabelGravity() {
        binding.appBarLayout.post {
            binding.emptyListLabel.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                gravity = Gravity.CENTER
                topMargin = binding.appBarLayout.height
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

        setOnQueryTextListener(this@TopLevelListFragment)
    }

    private fun setupMenu() {
        binding.topAppBar.apply {
            menu.findItem(R.id.remove).setTextColor(
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            )
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.remove -> viewModel.onEvent(Event.BeginDelete)
                    R.id.cancel -> viewModel.onEvent(Event.StopDelete)
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun setSearchingStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = false
            findItem(R.id.remove).isVisible = false
        }
    }

    protected abstract fun onAddButtonClick()

    private fun setDefaultState(closeSearch: Boolean) {
        binding.addButton.isVisible = true
        viewModel.onEvent(Event.StopDelete)
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        binding.searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query == null) {
            return true
        }
        val searchQuery = "%$query%"
        viewModel.onEvent(Event.Search(searchQuery))
        return true
    }

    @CallSuper
    protected open fun initCollectors(scope: CoroutineScope) {
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { uiState -> uiState.isFetching }
                .collect { uiState ->
                    if (!uiState.isFetching) {
                        requireView().doOnPreDraw {
                            startPostponedEnterTransition()
                        }
                    }
                }
        }
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.items }
                .collectLatest { uiState ->
                    doOnListUpdate(uiState.items)
                }
        }
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.isDeleting }
                .collectLatest { uiState -> doOnDeleting(uiState) }
        }
    }

    protected open fun doOnListUpdate(items: List<ItemState>) {
        adapter.submitList(items)
        binding.emptyListLabel.isVisible = items.isEmpty()
        binding.recyclerView.doOnPreDraw { view ->
            binding.appBarLayout.fixCollapsing(view as RecyclerView)
        }
    }

    private fun doOnDeleting(uiState: State) {
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

    private fun setDeleteStateMenu() {
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = true
            findItem(R.id.remove).isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}