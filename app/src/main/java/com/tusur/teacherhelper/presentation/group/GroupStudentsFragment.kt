package com.tusur.teacherhelper.presentation.group

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialSharedAxis
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.FragmentGroupStudentsBinding
import com.tusur.teacherhelper.presentation.util.EXCEL_FILE_MIME_TYPES
import com.tusur.teacherhelper.presentation.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.util.getDefaultListItemDecoration
import com.tusur.teacherhelper.presentation.util.getExcelFileFromUri
import com.tusur.teacherhelper.presentation.view.recycler.checkNestedScrollState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch
import java.io.File


class GroupStudentsFragment : Fragment() {

    private val binding get() = _binding!!
    private var _binding: FragmentGroupStudentsBinding? = null

    private val args: GroupStudentsFragmentArgs by navArgs()
    private val viewModel: GroupStudentsViewModel by viewModels {
        GroupStudentsViewModel.factory(args.groupId)
    }

    private val adapter = GroupStudentAdapter { studentId ->
        showDeleteGroupDialog {
            viewModel.deleteStudent(studentId)
            viewModel.stopDelete()
        }
    }

    private val openDocumentsLauncher = registerForActivityResult(
        object : ActivityResultContracts.OpenDocument() {
            override fun createIntent(context: Context, input: Array<String>): Intent {
                return super.createIntent(context, input).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
            }
        }
    ) { uri ->
        if (uri != null) {
            val file = requireActivity().getExcelFileFromUri(uri)
            navigateToImportDialog(file)
        }
    }

    private val permissionDialogLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showExcelFilePicker()
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupStudentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupEmptyLabelGravity()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.studentItemsUiState }
                        .collectLatest { uiState ->
                            binding.emptyGroupLabel.isVisible =
                                uiState.studentItemsUiState.isEmpty()
                            adapter.submitList(uiState.studentItemsUiState)
                            view.doOnPreDraw {
                                binding.studentList.checkNestedScrollState()
                                val needToExpandAppBar =
                                    !binding.studentList.isNestedScrollingEnabled
                                binding.appBarLayout.setExpanded(needToExpandAppBar)
                                startPostponedEnterTransition()
                            }
                        }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.isEditing || it.isDeleting }
                        .collectLatest { uiState ->
                            when {
                                uiState.isDeleting -> setRemoveUiState()
                                uiState.isEditing -> setEditUiState()
                                else -> setDefaultUiState()
                            }
                        }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.groupNumber }.collect {
                        binding.topAppBar.title =
                            resources.getString(R.string.group_students_headline, it.groupNumber)
                    }
                }

            }
        }

        postponeEnterTransition()
    }

    override fun onStart() {
        super.onStart()
        binding.addButton.setOnClickListener {
            viewModel.addStudentWithoutName()
        }

        binding.topAppBar.apply {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.remove -> viewModel.startDelete()
                    R.id.cancel -> if (viewModel.uiState.value.isDeleting) {
                        viewModel.stopDelete()
                    } else {
                        viewModel.stopEditing()
                    }

                    R.id.excel_import -> checkPermissionsAndShowExcelFilePicker()

                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
        }

        doOnBackPressed(binding.topAppBar) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRemoveUiState() {
        binding.topAppBar.menu.findItem(R.id.cancel).isVisible = true
        binding.topAppBar.menu.findItem(R.id.remove).isVisible = false
        adapter.isDeleting = true
        viewModel.stopEditing()
        binding.addButton.isVisible = false
    }

    private fun setEditUiState() {
        binding.topAppBar.menu.findItem(R.id.cancel).isVisible = true
        binding.topAppBar.menu.findItem(R.id.remove).isVisible = false
        adapter.isDeleting = false
        binding.addButton.isVisible = false
    }

    private fun setDefaultUiState() {
        binding.topAppBar.menu.findItem(R.id.cancel).isVisible = false
        binding.topAppBar.menu.findItem(R.id.remove).isVisible = true
        adapter.isDeleting = false
        binding.addButton.isVisible = true
    }

    private fun setupRecyclerView() {
        binding.studentList.adapter = adapter
        binding.studentList.addItemDecoration(getDefaultListItemDecoration(resources))
    }

    private fun showDeleteGroupDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_student_title)
            .setNegativeButton(R.string.cancel_button, null)
            .setPositiveButton(R.string.confirm_button) { _, _ ->
                onConfirm.invoke()
            }.show()
    }

    private fun checkPermissionsAndShowExcelFilePicker() {
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ).also { permissionCode ->
            if (permissionCode != PackageManager.PERMISSION_GRANTED) {
                permissionDialogLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                showExcelFilePicker()
            }
        }
    }

    private fun showExcelFilePicker() {
        openDocumentsLauncher.launch(EXCEL_FILE_MIME_TYPES)
    }

    private fun navigateToImportDialog(file: File) {
        val action = GroupStudentsFragmentDirections
            .actionToGroupImportBottomSheet(args.groupId, file)
        findNavController().navigate(action)
    }

    private fun setupEmptyLabelGravity() {
        binding.appBarLayout.post {
            binding.emptyGroupLabel.updateLayoutParams<CoordinatorLayout.LayoutParams> {
                gravity = Gravity.CENTER
                topMargin = binding.appBarLayout.height
            }
        }
    }
}