package com.tusur.teacherhelper.presentation.subjectdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentSubjectDetailsBinding
import com.tusur.teacherhelper.domain.util.NO_ID
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import com.tusur.teacherhelper.presentation.core.util.doOnNavigationRequest
import com.tusur.teacherhelper.presentation.core.util.primaryLocale
import com.tusur.teacherhelper.presentation.core.util.setTextColor
import com.tusur.teacherhelper.presentation.subjectdetails.SubjectDetailsViewModel.Companion.GROUPS_FRAGMENT_POSITION
import com.tusur.teacherhelper.presentation.subjectdetails.SubjectDetailsViewModel.Companion.TOPICS_FRAGMENT_POSITION
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SubjectDetailsFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding get() = _binding!!
    private var _binding: FragmentSubjectDetailsBinding? = null

    private val args: SubjectDetailsFragmentArgs by navArgs()
    private val viewModel: SubjectDetailsViewModel by viewModels(extrasProducer = {
        creationCallback<SubjectDetailsViewModel.Factory> { factory ->
            factory.create(subjectId = args.subjectId)
        }
    })

    private val currentShownFragment get() = binding.pager.currentItem

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            when (currentShownFragment) {
                GROUPS_FRAGMENT_POSITION -> {
                    menuInflater.inflate(R.menu.groups_tab, menu)
                    menu.findItem(R.id.remove).setErrorColor()
                }

                TOPICS_FRAGMENT_POSITION -> {
                    // Use shared searchView only (no menu)
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return handleMenuItemClick(menuItem)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubjectDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pager.adapter = ViewPagerAdapter(this)
        binding.pager.isUserInputEnabled = false

        postponeEnterTransition()

        binding.topAppBar.addMenuProvider(menuProvider)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.subjectName }.collect {
                        binding.topAppBar.title = it.subjectName.toString(requireContext())
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isFetching }.collectLatest {
                        binding.progressIndicator.isVisible = it.isFetching
                        if (!it.isFetching) {
                            view.doOnPreDraw { startPostponedEnterTransition() }
                        }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isDeleting }
                        .collectLatest {
                            if (!binding.searchView.isIconified) {
                                setSearchingState()
                            } else {
                                if (it.isDeleting) {
                                    setDeleteState()
                                } else {
                                    setDefaultState(true)
                                }
                            }
                        }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.groupsUiState }.collectLatest {
                        if (it.groupsUiState.isEmpty()) {
                            disableMenu()
                        } else {
                            enableMenu()
                        }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.currentFragmentIndex }
                        .collectLatest {
                            setDefaultState(true)
                            binding.pager.currentItem = it.currentFragmentIndex
                            binding.tabLayout.getTabAt(it.currentFragmentIndex)!!.select()
                            recreateMenu()
                        }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setSelectedTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        doOnNavigationRequest(topAppBar = binding.topAppBar) {
            if (!binding.searchView.isIconified) {
                setDefaultState(true)
            } else if (binding.pager.currentItem == TOPICS_FRAGMENT_POSITION) {
                viewModel.setSelectedTab(GROUPS_FRAGMENT_POSITION)
            } else {
                findNavController().navigateUp()
            }
        }

        setupSearch()

        binding.addButton.setOnClickListener {
            if (currentShownFragment == GROUPS_FRAGMENT_POSITION) {
                showGroupAddDialog()
            } else {
                navigateToCreateTopic()
            }
        }
    }

    private fun setupSearch() = with(binding.searchView) {
        setOnSearchClickListener { setSearchingState() }

        setOnCloseListener {
            viewModel.uiState.value.also {
                if (it.isDeleting) {
                    setDeleteState()
                } else {
                    setDefaultState(closeSearch = false)
                }
            }
            false
        }

        setOnQueryTextListener(this@SubjectDetailsFragment)
    }

    private fun recreateMenu() {
        binding.topAppBar.invalidateMenu()
    }

    private fun showGroupAddDialog() {
        val action = SubjectDetailsFragmentDirections
            .actionToAddSubjectGroupDialog(subjectId = args.subjectId)
        findNavController().navigate(action)
    }

    private fun navigateToCreateTopic() {
        val extras = FragmentNavigatorExtras(
            binding.addButton to requireContext().getString(R.string.transition_topic_name)
        )
        val action = SubjectDetailsFragmentDirections.actionToTopicNameFragment(
            topicId = NO_ID,
            subjectId = args.subjectId
        )
        findNavController().navigate(action, extras)
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
        if (currentShownFragment == GROUPS_FRAGMENT_POSITION) {
            viewModel.searchGroup(searchQuery)
        } else {
            viewModel.searchTopic(searchQuery)
        }
        return true
    }

    private fun disableMenu() {
        if (currentShownFragment == TOPICS_FRAGMENT_POSITION) return
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isEnabled = false
            findItem(R.id.remove).also {
                it.isEnabled = false
                it.setErrorColor()
            }
        }
    }

    private fun enableMenu() {
        if (currentShownFragment == TOPICS_FRAGMENT_POSITION) return
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isEnabled = true
            findItem(R.id.remove).also {
                it.isEnabled = true
                it.setErrorColor()
            }
        }
    }

    private fun setDeleteState() {
        binding.addButton.isVisible = false
        setDeleteStateMenu()
    }

    private fun setDeleteStateMenu() {
        if (currentShownFragment == TOPICS_FRAGMENT_POSITION) return
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = true
            findItem(R.id.remove).isVisible = false
        }
    }

    private fun setDefaultState(closeSearch: Boolean) {
        viewModel.stopDelete()
        setDefaultStateMenu(closeSearch)
        binding.addButton.isVisible = true
    }

    private fun setDefaultStateMenu(closeSearch: Boolean) {
        if (currentShownFragment == GROUPS_FRAGMENT_POSITION) {
            binding.topAppBar.menu.apply {
                findItem(R.id.cancel).isVisible = false
                findItem(R.id.remove).isVisible = true
            }
        }
        if (closeSearch && !binding.searchView.isIconified) {
            binding.searchView.setQuery("", true)
            binding.searchView.isIconified = true
        }
    }

    private fun setSearchingState() {
        binding.addButton.isVisible = false
        setSearchingStateMenu()
    }

    private fun setSearchingStateMenu() {
        if (currentShownFragment == TOPICS_FRAGMENT_POSITION) return
        binding.topAppBar.menu.apply {
            findItem(R.id.cancel).isVisible = false
            findItem(R.id.remove).isVisible = false
        }
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            // Only GroupsTabFragment
            R.id.cancel -> viewModel.stopDelete()
            R.id.remove -> viewModel.startDelete()
            else -> return false
        }
        return true
    }

    private fun MenuItem.setErrorColor() {
        setTextColor(
            MaterialColors.getColor(
                requireView(),
                if (isEnabled) {
                    com.google.android.material.R.attr.colorError
                } else {
                    com.google.android.material.R.attr.colorErrorContainer
                }
            )
        )
    }

    private class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int) =
            if (position == GROUPS_FRAGMENT_POSITION) {
                GroupsTabFragment()
            } else {
                TopicsTabFragment()
            }
    }
}