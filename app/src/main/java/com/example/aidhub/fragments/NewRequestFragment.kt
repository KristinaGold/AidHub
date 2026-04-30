package com.example.aidhub.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.aidhub.managers.LocationManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.R
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.Skill
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentNewRequestBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.RequestViewModel
import kotlin.getValue

class NewRequestFragment : Fragment() {

    private var _binding: FragmentNewRequestBinding? = null
    private val binding get() = _binding!!
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0
    private val profileViewModel: ProfileViewModel by activityViewModels { MyViewModelFactory() }
    private val requestViewModel: RequestViewModel by viewModels { MyViewModelFactory() }
    private var selectedCategory: String = ""
    private var imageUri: Uri? = null
    private val locationManager = LocationManager.getInstance()
    private val toastManager = ToastHelper.getInstance()
    private var currentProfile: Profile? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewRequestBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observers()
        setupRequestChips()

        binding.switchCurrentLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etAddress.isEnabled = false
                binding.etAddress.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_grey
                    )
                )
                getCurrentLocation()
            } else {
                binding.etAddress.isEnabled = true
            }
        }

        binding.btnUploadPhoto.setOnClickListener {
            pickImageLauncher.launch(Constants.IMAGE_KEY)
        }
        binding.topBar.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            createNewRequest(currentProfile)
        }


    }

    private fun observers() {
        profileViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentProfile = user
                updateUI(user)
            }
        }
        requestViewModel.isPublishing.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        requestViewModel.publishSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                toastManager.showSuccess(ToastType.REQUEST_PUBLISHED.message)
                findNavController().navigateUp()
            } else {
                toastManager.showError(ToastType.ERROR.message)
                findNavController().navigateUp()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
    }

    private fun updateUI(profile: Profile) {
        binding.imgProfilePicture.loadProfileImage(profile.profileImageUrl)
        binding.txtFullName.text = profile.fullName
        binding.topBar.txtTitle.text = Constants.NEW_REQUEST_SCREEN_TITLE
    }

    private fun createNewRequest(profile: Profile?) {
        val title = binding.etTitle.text.toString()
        val description = binding.etDescription.text.toString()
        val location = binding.etAddress.text.toString()

        if (!binding.switchCurrentLocation.isChecked) {
            val manualAddress = binding.etAddress.text.toString()
            locationManager.getCoordsFromAddress(manualAddress)?.let {
                selectedLat = it.first
                selectedLng = it.second
            }
        }
        if (title.isEmpty()) {
            binding.etTitle.error = Constants.EMPTY_KEY
            return
        }
        if (description.isEmpty()) {
            binding.etDescription.error = Constants.EMPTY_KEY
            return
        }
        if (location.isEmpty()) {
            binding.etAddress.error = Constants.EMPTY_KEY
            return
        }
        if (selectedCategory.isEmpty()) {
            toastManager.showError(ToastType.EMPTY_CATEGORY.message)
            return
        }
        showLoading(true)

        requestViewModel.createRequest(
            profile?.uid!!,
            profile.fullName!!,
            profile.profileImageUrl!!,
            title,
            description,
            location,
            selectedLat,
            selectedLng,
            selectedCategory,
            binding.switchUrgent.isChecked
        )
        requestViewModel.newRequestId.observe(viewLifecycleOwner)
        { reqID ->
            if (imageUri != null && reqID.isNotEmpty())
                requestViewModel.updateRequestImage(reqID, imageUri!!)
        }
    }


    private fun getCurrentLocation() {
        locationManager.getCurrentLocation({ lat, lng, address ->
            selectedLat = lat
            selectedLng = lng
            binding.etAddress.setText(address)
        }, {
            binding.switchCurrentLocation.isChecked = false
            binding.etAddress.isEnabled = true
            toastManager.showError(ToastType.LOCATION_PERMISSION_DISABLED.message)
        })
    }


    private fun setupRequestChips() {
        Skill.entries.forEach { skill ->
            val chip =
                ChipBuilder.createSkillChip(
                    requireContext(),
                    skill.displayName
                ) { name, isChecked ->
                    if (isChecked) {
                        selectedCategory = name
                    }
                }
            binding.cgEditSkills.addView(chip)
        }
    }
}