package com.tusur.teacherhelper.presentation.groups

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.presentation.core.dialog.InputBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class NewGroupNumberInputBottomSheet : InputBottomSheet() {

    override val viewModel: NewGroupNumberInputViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.title.setText(R.string.dialog_add_group_title)
        binding.textInputLayout.setHint(R.string.group_number_new_hint)
        binding.textInputText.filters += viewModel.inputFilter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    state.savedItemId?.let { navigateToGroupMembers(it) }
                        ?: if (state.error != null) {
                            binding.textInputLayout.isErrorEnabled = true
                            binding.textInputLayout.error = state.error.toString(requireContext())
                        } else {
                            binding.textInputLayout.isErrorEnabled = false
                            binding.textInputLayout.error = null
                        }
                }
            }
        }
    }

    private fun navigateToGroupMembers(createdGroupId: Int) {
        val action = NewGroupNumberInputBottomSheetDirections
            .actionToGroupStudentsFragment(createdGroupId)
        findNavController().navigate(action)
    }
}