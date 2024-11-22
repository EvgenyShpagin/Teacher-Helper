package com.tusur.teacherhelper.presentation.core.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.databinding.BottomSheetInputBinding
import com.tusur.teacherhelper.presentation.core.util.doOnActionDone

abstract class InputBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetInputBinding? = null
    protected val binding get() = _binding!!

    protected abstract val viewModel: InputViewModel


    open fun onActionDone(name: String) {
        viewModel.send(InputViewModel.Event.TryAdd(name))
    }

    open fun onTextChanged(text: String) {}


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    @CallSuper
    override fun onStart() {
        super.onStart()

        binding.textInputText.doOnActionDone {
            onActionDone(it.text?.toString() ?: "")
        }

        binding.textInputText.doAfterTextChanged {
            onTextChanged(it?.toString() ?: "")
        }
    }

    override fun onStop() {
        super.onStop()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}