package com.tusur.teacherhelper.presentation.subjectdetails

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.DialogAddSubjectGroupBinding
import com.tusur.teacherhelper.presentation.core.util.hideKeyboard
import com.tusur.teacherhelper.presentation.subjectdetails.AddSubjectGroupViewModel.Event
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

class AddSubjectGroupDialog : DialogFragment() {

    private var _binding: DialogAddSubjectGroupBinding? = null
    private val binding get() = _binding!!

    private val args: AddSubjectGroupDialogArgs by navArgs()

    private val viewModel: AddSubjectGroupViewModel by viewModels {
        AddSubjectGroupViewModel.factory(subjectId = args.subjectId)
    }

    private val adapter = SelectableGroupAdapter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddSubjectGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = adapter

        binding.textInputEditText.filters += viewModel.inputFilter

        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_AlertDialog)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.availableGroups }.collectLatest {
                        adapter.submitList(it.availableGroups)
                    }
                }
                launch {
                    viewModel.uiState.collect {
                        binding.nextButton.isEnabled = it.errorText == null || it.anyChecked
                        if (it.justCreatedGroupId != null) {
                            navigateToGroupCreation(it.justCreatedGroupId)
                        } else if (it.groupIsAdded) {
                            dismiss()
                        }
                    }
                }
                launch {
                    viewModel.uiState.distinctUntilChangedBy { it.errorText }.collect {
                        if (it.errorText == null) {
                            binding.textInputEditText.error = null
                            binding.textInputLayout.isErrorEnabled = false
                        } else {
                            binding.textInputLayout.isErrorEnabled = true
                            binding.textInputEditText.error =
                                it.errorText.toString(requireContext())
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        binding.textInputEditText.doAfterTextChanged { numberText ->
            viewModel.send(Event.Input(numberText?.toString() ?: ""))
        }

        binding.textInputEditText.setOnEditorActionListener { _, actionId, _ ->
            (actionId == EditorInfo.IME_ACTION_SEARCH).also {
                if (it) {
                    hideKeyboard()
                }
            }
        }

        binding.nextButton.setOnClickListener {
            viewModel.send(Event.TryAddNewGroup)
        }

        binding.cancelButton.setOnClickListener { dismiss() }
    }

    private fun navigateToGroupCreation(newGroupId: Int) {
        val action = AddSubjectGroupDialogDirections.actionToGroupStudentsFragment(newGroupId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}