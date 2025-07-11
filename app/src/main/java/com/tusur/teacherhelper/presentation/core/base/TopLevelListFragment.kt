package com.tusur.teacherhelper.presentation.core.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import com.google.android.material.transition.MaterialFadeThrough
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentTopLevelListBinding
import com.tusur.teacherhelper.presentation.core.base.TopLevelListViewModel.Event
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.launchOnOwnerStart
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

    private var isDeleting: Boolean = false

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
            when {
                binding.searchView.isShowing -> binding.searchView.hide()
                isDeleting -> updateDeleteState(false)
                else -> findNavController().navigateUp()
            }
        }

        handleSearchInput()

        fixSearchViewVisibility()

        if (savedInstanceState != null) {
            binding.searchView.isVisible = savedInstanceState.getBoolean(IS_SEARCH_SHOWN_KEY)
            updateDeleteState(savedInstanceState.getBoolean(IS_DELETING_KEY))
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
        binding.topAppBar.setupMenuListeners()
        binding.searchView.inflateMenu(R.menu.top_level_fragment_search_menu)
        binding.searchView.toolbar.setupMenuListeners()
    }

    private fun Toolbar.setupMenuListeners() {
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.remove -> updateDeleteState(delete = true)
                R.id.cancel -> updateDeleteState(delete = false)
                R.id.search -> {
                    // Show after first search as there is a bug
                    // where SearchView is not shown but its scrim is shown.
                    binding.searchView.isVisible = true
                    binding.searchView.show()
                }

                else -> false
            }
            true
        }
    }

    private fun handleSearchInput() {
        binding.searchView.editText.addTextChangedListener(
            onTextChanged = { charSequence, _, _, _ ->
                viewModel.onEvent(Event.Search(charSequence?.toString() ?: ""))
            }
        )
    }

    private fun fixSearchViewVisibility() {
        // Make invisible by default to hide scrim when is SearchView is hidden
        binding.searchView.isVisible = false

        binding.searchView.addTransitionListener { _, _, newState ->
            if (newState == SearchView.TransitionState.HIDDEN) {
                binding.searchView.isVisible = false
            }
        }
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

    protected fun updateDeleteState(delete: Boolean) {
        isDeleting = delete

        binding.addButton.isVisible = !delete
        mainAdapter.isDeleting = delete
        searchAdapter.isDeleting = delete

        binding.topAppBar.updateMenuItemsVisibility(delete)
        binding.searchView.toolbar.updateMenuItemsVisibility(delete)
    }

    private fun Toolbar.updateMenuItemsVisibility(isDeleting: Boolean) {
        menu.apply {
            findItem(R.id.cancel).isVisible = isDeleting
            findItem(R.id.remove).isVisible = !isDeleting
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /** Save UI related data only if fragment is visible. **/
        if (_binding == null) return
        outState.putBoolean(IS_SEARCH_SHOWN_KEY, binding.searchView.isVisible)
        outState.putBoolean(IS_DELETING_KEY, isDeleting)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val IS_SEARCH_SHOWN_KEY = "IS_SEARCH_SHOWN_KEY"
        const val IS_DELETING_KEY = "IS_DELETING_KEY"
    }
}