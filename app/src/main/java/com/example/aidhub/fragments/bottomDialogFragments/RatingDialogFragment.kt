package com.example.aidhub.fragments.bottomDialogFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.DialogRatingBinding
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.RequestViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RatingDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogRatingBinding? = null
    private val binding get() = _binding!!
    private val requestViewModel: RequestViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRatingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var selectedRequest: Request? = null

        requestViewModel.selectedRequest.observe(viewLifecycleOwner) {
            selectedRequest = it
        }


        binding.btnSubmitRating.setOnClickListener {
            val rating = binding.ratingBar.rating
            val review = binding.editReview.text.toString().trim()
            if (rating == 0f)
                return@setOnClickListener

            binding.btnSubmitRating.isEnabled = false

            profileViewModel.completeRequest(
                selectedRequest,
                rating,
                review,
                selectedRequest!!.urgent
            )
            requestViewModel.completeRequest(selectedRequest.requestId, true)

        }

        profileViewModel.success.observe(viewLifecycleOwner){ success ->
            if (success) {
                ToastHelper.getInstance().showSuccess(ToastType.REVIEW_ADDED.message)
                profileViewModel.resetSuccess()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}