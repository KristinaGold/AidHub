package com.example.aidhub.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentNewPostBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.PostViewModel
import com.example.aidhub.viewModels.ProfileViewModel
import kotlin.getValue

class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!
    private val postViewModel: PostViewModel by viewModels { MyViewModelFactory() }
    private val profileViewModel: ProfileViewModel by activityViewModels { MyViewModelFactory() }
    private var imageUri: Uri? = null
    private var selectedTags = mutableListOf<String>()
    private val toastManager = ToastHelper.getInstance()


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
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val buttonCancel = binding.topBar.btnCancel
        val buttonPublish = binding.btnPublish
        val buttonUploadPhoto = binding.btnUploadPhoto
        var currentProfile: Profile? = null

        profileViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                currentProfile = user
                updateUI(user)
            }
        }


        buttonUploadPhoto.setOnClickListener {
            pickImageLauncher.launch(Constants.IMAGE_KEY)
        }


        buttonPublish.setOnClickListener {
            publishPost(currentProfile)
        }

        buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        postViewModel.isPublishing.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        postViewModel.publishSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                toastManager.showSuccess(ToastType.POST_PUBLISHED.message)
                findNavController().navigateUp()
            } else {
                toastManager.showError(ToastType.ERROR.message)
                findNavController().navigateUp()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnPublish.isEnabled = !isLoading
    }

    private fun updateUI(user: Profile) {
        binding.topBar.txtTitle.text = Constants.NEW_POST_SCREEN_TITLE
        binding.imgProfilePicture.loadProfileImage(user.profileImageUrl)
        binding.txtFullName.text = user.fullName
        setupSkillsChips(user.skills!!)
    }

    private fun publishPost(profile: Profile?) {
        val content = binding.etDescription.text.toString().trim()
        if (content.isEmpty()) {
            binding.etDescription.error = Constants.EMPTY_KEY
            return
        }
        if (selectedTags.isEmpty()) {
            toastManager.showError(ToastType.EMPTY_TAG.message)
            return
        }


        postViewModel.createNewPost(
            AuthManager.getUid()!!,
            profile?.fullName!!,
            profile.profileImageUrl!!,
            content,
            selectedTags
        )
        postViewModel.newPostId.observe(viewLifecycleOwner)
        { postID ->
            if (imageUri != null)
                postViewModel.updatePostImage(postID, imageUri!!)
        }
    }

    private fun setupSkillsChips(userSkills: MutableList<String>) {
        val skillChipGroup = binding.cgEditSkills

        userSkills.forEach { skill ->
            val chip =
                ChipBuilder.createSkillChip(
                    requireContext(),
                    skill
                ) { name, isChecked ->
                    if (isChecked) selectedTags.add(name)
                    else selectedTags.remove(name)
                }
            skillChipGroup.addView(chip)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
