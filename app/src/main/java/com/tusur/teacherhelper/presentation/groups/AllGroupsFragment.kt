package com.tusur.teacherhelper.presentation.groups

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
import com.tusur.teacherhelper.databinding.FragmentAllGroupListBinding
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.fixCollapsing
import com.tusur.teacherhelper.presentation.core.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.core.util.setTextColor
import com.tusur.teacherhelper.presentation.core.util.setupTopLevelAppBarConfiguration
import com.tusur.teacherhelper.presentation.subjectdetails.DeletableGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AllGroupsFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentAllGroupListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllGroupsViewModel by viewModels()
    private lateinit var adapter: DeletableGroupAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = DeletableGroupAdapter(object : DeletableGroupAdapter.OnClickListener {
            override fun onClick(groupId: Int) {
                navigateToGroupPerformance(groupId)
            }

            override fun onDeleteClick(groupId: Int) {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.canGroupBeDeleted(groupId)) {
                        showDeleteGroupDialog {
                            viewModel.stopDelete()
                            viewModel.deleteGroup(groupId)
                        }
                    } else {
                        showDeleteProblemDialog()
                    }
                }
            }
        })
        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllGroupListBinding.inflate(inflater, container, false)
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
                    viewModel.uiState.distinctUntilChangedBy { it.groupsUiState }
                        .collectLatest { uiState ->
                            doOnListUpdate(uiState)
                            requireView().doOnPreDraw {
                                binding.appBarLayout.fixCollapsing(binding.groups)
                                startPostponedEnterTransition()
                            }
                        }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }
                        .collectLatest { uiState -> doOnDeleting(uiState) }
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
            navigateToAddGroup()
        }

        doOnBackPressed {
            if (!binding.searchView.isIconified) {
                setDefaultState(true)
            }
            if (viewModel.uiState.value.isDeleting) {
                viewModel.stopDelete()
            }
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
        viewModel.searchGroup(searchQuery)
        return true
    }

    private fun setupMenu() {
        binding.topAppBar.apply {
            menu.findItem(R.id.remove).setTextColor(
                MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorError)
            )
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.remove -> viewModel.startDelete()
                    R.id.cancel -> viewModel.stopDelete()
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }
        }
    }

    private fun navigateToAddGroup() {
        val action = AllGroupsFragmentDirections
            .actionToNewGroupNumberInputBottomSheet()
        findNavController().navigate(action)
    }

    private fun showDeleteProblemDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_group_cannot_be_deleted_title)
            .setMessage(R.string.dialog_group_cannot_be_deleted_body)
            .setOnDismissListener { viewModel.stopDelete() }
            .setPositiveButton(R.string.ok_button) { _, _ ->
                viewModel.stopDelete()
            }.show()
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

        setOnQueryTextListener(this@AllGroupsFragment)
    }

    private fun setDeleteState() {
        binding.addButton.isVisible = false
        adapter.showDeleteButtons()
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
        viewModel.stopDelete()
        adapter.hideDeleteButtons()
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

    private fun navigateToGroupPerformance(groupId: Int) {
        val action = AllGroupsFragmentDirections
            .actionToGroupStudentsFragment(groupId)
        findNavController().navigate(action)
    }

    private fun showDeleteGroupDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_group_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun doOnDeleting(uiState: AllGroupsViewModel.UiState) {
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

    private fun doOnListUpdate(uiState: AllGroupsViewModel.UiState) {
        adapter.submitList(uiState.groupsUiState)
        binding.emptyListLabel.isVisible = uiState.groupsUiState.isEmpty()
    }

    private fun setupRecyclerView() {
        binding.groups.adapter = adapter
        binding.groups.addItemDecoration(getDefaultListItemDecoration(resources))
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