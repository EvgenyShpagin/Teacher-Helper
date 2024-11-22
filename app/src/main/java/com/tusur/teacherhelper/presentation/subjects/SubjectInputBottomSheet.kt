package com.tusur.teacherhelper.presentation.subjects

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.dialog.InputBottomSheet
import com.tusur.teacherhelper.presentation.core.dialog.InputViewModel
import kotlinx.coroutines.launch

class SubjectInputBottomSheet : InputBottomSheet() {
    override val viewModel: SubjectInputViewModel by viewModels { SubjectInputViewModel.factory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.setText(R.string.dialog_add_subject_title)
        binding.textInputLayout.setHint(R.string.subject_name_hint)
        binding.textInputText.filters += viewModel.inputFilter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { display(it) }
            }
        }
    }

    private fun display(uiState: InputViewModel.UiState) {
        if (uiState.isSavedSuccessfully) {
            dismiss()
            return
        }
        binding.textInputLayout.error = uiState.error?.toString(requireContext())
    }
}