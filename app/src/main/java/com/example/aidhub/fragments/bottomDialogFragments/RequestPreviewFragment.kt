package com.example.aidhub.fragments.bottomDialogFragments

import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.R
import com.example.data.dataStractures.Request
import com.example.aidhub.databinding.DialogRequestPreviewBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.RequestViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RequestPreviewFragment : BottomSheetDialogFragment() {

    private var _binding: DialogRequestPreviewBinding? = null
    private val binding get() = _binding!!
    private val requestViewModel: RequestViewModel by activityViewModels()
    private var request: Request? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRequestPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestViewModel.selectedRequest.observe(viewLifecycleOwner) { req ->
            request = req
            setupUI(req)
        }
        binding.btnDetails.setOnClickListener {
            val navController = findNavController()
            val bundle = Bundle()
            bundle.putString(Constants.REQUEST_ID_KEY, request!!.requestId)
            bundle.putString(Constants.HELPER_ID_KEY, request!!.helperId)
            navController.navigate(R.id.action_requestPreviewFragment_to_requestFragment, bundle)
        }
    }

    private fun setupUI(request: Request) {
        binding.txtPreviewTitle.text = request.title
        binding.txtPreviewDescription.text = request.content
        binding.txtTime.text = DateUtils.getRelativeTimeSpanString(request.timestamp)
        binding.txtFullName.text = request.userName
        binding.txtLocation.text = request.locationInfo
        binding.imgProfilePicture.loadProfileImage(request.userImageUrl)
        binding.chipGroup.removeAllViews()
        val chip = ChipBuilder.createSkillChip(
            binding.root.context,
            request.tag,
            isCheckable = true,
            isClickable = false,
            isPost = true
        )
        binding.chipGroup.addView(chip)
        if (request.urgent) {
            binding.chipGroup.addView(
                ChipBuilder.createStatusChip(
                    requireContext(),
                    Constants.URGENT_KEY
                )
            )
        }
        LocationManager.getInstance().calculateDistanceToUser(request.latitude, request.longitude) {
            binding.txtDistance.text = it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}