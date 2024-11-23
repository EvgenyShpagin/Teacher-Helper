package com.tusur.teacherhelper.presentation.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetGroupImportBinding
import com.tusur.teacherhelper.presentation.core.util.creationCallback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class GroupImportBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetGroupImportBinding? = null
    private val binding get() = _binding!!

    private val args: GroupImportBottomSheetArgs by navArgs()

    private val viewModel: GroupImportViewModel by viewModels(
        extrasProducer = {
            creationCallback<GroupImportViewModel.Factory> { factory ->
                factory.create(args.groupId, args.excelFile)
            }
        }
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.fetch()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetGroupImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { update(it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.confirmButton.setOnClickListener {
            viewModel.send(GroupImportViewModel.Event.Confirm)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun update(uiState: GroupImportViewModel.UiState) = with(binding) {
        if (!uiState.hasMultipleSheets) {
            sheetInputLayout.isVisible = false
        } else {
            sheetInputEditText.setText(uiState.sheetIndex)
        }
        if (uiState.areNamesSeparated) {
            binding.columnInputLayout.setHint(R.string.import_excel_first_column_hint)
        }
        rowInputEditText.setText(uiState.firstRowIndex)
        columnInputEditText.setText(uiState.firstColumnIndex)
        confirmButton.isEnabled = uiState.isAllInputCorrect
        if (uiState.errorMessage != null) {
            Toast.makeText(
                requireContext(),
                uiState.errorMessage.toString(requireContext()),
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }
    }
}