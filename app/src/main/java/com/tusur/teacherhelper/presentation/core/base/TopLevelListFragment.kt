package com.tusur.teacherhelper.presentation.core.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
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
        Effect : TopLevelListUiEffect> : Fragment() {

    private var _binding: FragmentTopLevelListBinding? = null
    protected val binding get() = _binding!!

    protected abstract val mainAdapter: BaseDeletableAdapter<ItemState>
    protected abstract val searchAdapter: BaseDeletableAdapter<ItemState>

    protected abstract val viewModel: TopLevelListViewModel<State, Effect>

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

        setupMenu()

        launchOnOwnerStart {
            initCollectors(this)
        }

        doOnBackPressed {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            }
        }

        handleSearchInput()

        if (savedInstanceState != null) {
            binding.searchView.isVisible = savedInstanceState.getBoolean(IS_SEARCH_SHOWN_KEY)
        }

        postponeEnterTransition()
    }

    protected open fun setupRecyclerView() {
        val itemDecoration = getDefaultListItemDecoration(resources)
        binding.mainList.apply {
            adapter = mainAdapter
            addItemDecoration(itemDecoration)
        }

        binding.searchList.apply {
            adapter = searchAdapter
            addItemDecoration(itemDecoration)
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

    private fun setupMenu() {
        binding.topAppBar.apply {
            menu.findItem(R.id.remove).setTextColor(
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            )
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.remove -> viewModel.onEvent(Event.BeginDelete)
                    R.id.cancel -> viewModel.onEvent(Event.StopDelete)
                    R.id.search -> {
                        // Show after first search as there is a bug
                        // where SearchView is not shown but its scrim is shown.
                        binding.searchView.isVisible = true
                        binding.searchView.show()
                    }

                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun handleSearchInput() {
        binding.searchView.editText.addTextChangedListener(
            onTextChanged = { charSequence, _, _, _ ->
                viewModel.onEvent(Event.Search(charSequence?.toString() ?: ""))
            }
        )
    }

    @CallSuper
    protected open fun initCollectors(scope: CoroutineScope) {
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.isFetching }
                .collect { uiState ->
                    if (!uiState.isFetching) {
                        doOnFetchFinish()
                    }
                }
        }
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.allItems }
                .collectLatest { uiState ->
                    doOnMainListUpdate(uiState.allItems)
                }
        }
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.searchedItems }
                .collectLatest { uiState -> doOnSearchListUpdate(uiState.searchedItems) }
        }
        scope.launch {
            viewModel.uiState
                .distinctUntilChangedBy { it.isDeleting }
                .collectLatest { uiState -> doOnDeleting(uiState.isDeleting) }
        }
    }

    private fun doOnFetchFinish() {
        requireView().doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    private fun doOnMainListUpdate(items: List<ItemState>) {
        mainAdapter.submitList(items)
        binding.emptyListLabel.isVisible = items.isEmpty()
        binding.mainList.doOnPreDraw { view ->
            binding.appBarLayout.fixCollapsing(view as RecyclerView)
        }
    }

    private fun doOnSearchListUpdate(searchedItems: List<ItemState>) {
        searchAdapter.submitList(searchedItems)
    }

    private fun doOnDeleting(isDeleting: Boolean) {
        binding.addButton.isVisible = !isDeleting
        mainAdapter.isDeleting = isDeleting

        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = isDeleting
            findItem(R.id.remove).isVisible = !isDeleting
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(IS_SEARCH_SHOWN_KEY, binding.searchView.isVisible)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val IS_SEARCH_SHOWN_KEY = "IS_SEARCH_SHOWN_KEY"
    }
}