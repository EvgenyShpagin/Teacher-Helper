package com.tusur.teacherhelper.presentation.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tusur.teacherhelper.R
import com.tusur.teacherhelper.databinding.BottomSheetClassTimeBinding
import com.tusur.teacherhelper.domain.model.Date
import com.tusur.teacherhelper.presentation.core.util.doOnBackPressed
import com.tusur.teacherhelper.presentation.core.util.getGroupListItemDecoration
import kotlinx.coroutines.launch

/**
 * Dialog with select of time range class.
 * @constructor is used to determine which class time list items are containing set performance.
 */
class ClassTimeBottomSheet(
    private var topicId: Int,
    private var groupListIds: List<Int>? = null,
    private val classDate: Date,
    private val doOnTimeConfirm: (initTimeMs: Long) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetClassTimeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClassTimeViewModel by viewModels {
        ClassTimeViewModel.factory(topicId, groupListIds, classDate)
    }

    private val adapter = ClassTimeAdapter()


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
        _binding = BottomSheetClassTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        requireDialog().window!!.setWindowAnimations(R.style.Animation_App_BottomSheet)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    if (it.chosenTimeMillis != null) {
                        doOnTimeConfirm(it.chosenTimeMillis)
                        dismiss()
                    } else {
                        adapter.submitList(it.itemsUiState)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        doOnBackPressed { dismiss() }
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.classTimesList.adapter = adapter
        binding.classTimesList.addItemDecoration(getGroupListItemDecoration(resources, false))
    }
}