package com.example.aidhub.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.utilities.Constants
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.R
import com.example.aidhub.fragments.WelcomeFragment
import com.example.data.dataStractures.Profile
import com.example.aidhub.databinding.ActivityEditProfileBinding
import com.example.data.dataStractures.Skill
import com.example.data.dataStractures.ToastType
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.hideSystemBars
import com.example.aidhub.utilities.loadProfileImage


class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: ProfileViewModel by viewModels { MyViewModelFactory() }
    private val toastManager = ToastHelper.getInstance()
    private val selectedSkills = mutableListOf<String>()
    private var layoutMode: Int = Constants.MODE_EDIT
    private var imageUri: Uri? = null
    private var imageUrl: String = ""
    private lateinit var nameEdit: com.google.android.material.textfield.TextInputEditText
    private lateinit var bioEdit: com.google.android.material.textfield.TextInputEditText
    private lateinit var profilePic: com.google.android.material.imageview.ShapeableImageView


    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                profilePic.setImageURI(it)
                uploadProfileImage()
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemBars(binding.root)
        val buttonImage = binding.btnEditImage
        val buttonSave = binding.btnSave
        val buttonCancel = binding.topBar.btnCancel
        profilePic = binding.imgProfilePicture
        nameEdit = binding.editName
        bioEdit = binding.editBio




        val bundle = intent.getBundleExtra(Constants.BUNDLE_KEY)
        layoutMode = bundle!!.getInt(Constants.MODE_KEY)


        if (layoutMode == Constants.MODE_SETUP) {
            setupModeUI()
        } else {
            viewModel.getProfile(AuthManager.getUid()!!)
            viewModel.otherUserData.observe(this) { user ->
                editModeUI(user!!)
            }
        }

        buttonSave.setOnClickListener {
            saveButtonPressed()
        }
        buttonImage.setOnClickListener {
            pickImageLauncher.launch(Constants.IMAGE_KEY)
        }
        buttonCancel.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.from_bottom, R.anim.to_bottom)
        }
    }


    private fun saveButtonPressed() {
        val name = nameEdit.text.toString().trim()
        val bio = bioEdit.text.toString().trim()

        if (name.isEmpty()) {
            toastManager.showError(ToastType.EMPTY_NAME.message)
            return
        }

        if (layoutMode == Constants.MODE_SETUP) {
            saveUserProfile(name, bio)
        } else {
            updateProfile(name, bio)
        }
    }


    private fun editModeUI(user: Profile) {
        binding.topBar.txtTitle.text = Constants.EDIT_PROFILE_SCREEN_TITLE
        profilePic.loadProfileImage(user.profileImageUrl)
        nameEdit.setText(user.fullName)
        bioEdit.setText(user.bio)
        setupSkillsChips(user.skills ?: emptyList())
    }


    private fun setupModeUI() {
        binding.topBar.btnCancel.visibility = View.GONE
        binding.btnEditImage.setIconResource(R.drawable.ic_camera)
        binding.btnSave.text = "Start"
        binding.topBar.txtTitle.text = Constants.START_PROFILE_SCREEN_TITLE
        binding.txtSkills.text = "Select your skills:"
        binding.secondTitle.visibility = View.VISIBLE

        val displayName = AuthManager.getCurrentUser()!!.displayName
        nameEdit.setText(displayName)

        setupSkillsChips(emptyList())
    }

    private fun setupSkillsChips(currentSkills: List<String>) {
        val skillChipGroup = binding.cgEditSkills
        var initiallyChecked = false

        Skill.entries.sortedBy{ it.displayName }.forEach { skill ->
            if (layoutMode == Constants.MODE_EDIT) {
                initiallyChecked = currentSkills.contains(skill.displayName)
                if (initiallyChecked) selectedSkills.add(skill.displayName)
            }
            val chip =
                ChipBuilder.createSkillChip(
                    this,
                    skill.displayName,
                    initiallyChecked = initiallyChecked
                ) { name, isChecked ->
                    if (isChecked) selectedSkills.add(name)
                    else selectedSkills.remove(name)
                }
            skillChipGroup.addView(chip)
        }
    }

    private fun updateProfile(name: String, bio: String) {
        viewModel.updateProfile(AuthManager.getUid()!!, name, bio, selectedSkills)
        viewModel.success.observe(this) { success ->
            if (success) {
                toastManager.showSuccess(ToastType.PROFILE_UPDATED.message)
                finish()
            }
        }
    }


    private fun saveUserProfile(name: String, bio: String) {
        viewModel.createProfile(AuthManager.getUid()!!, name, bio, imageUrl, selectedSkills) { success ->
            if (success) {
                toastManager.showSuccess(ToastType.PROFILE_SAVED.message)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerWelcome, WelcomeFragment()).addToBackStack(null)
                    .commit()
            } else {
                toastManager.showError(ToastType.PROFILE_NOT_SAVED.message)
            }
        }
    }


    private fun uploadProfileImage() {
        AuthManager.getUid() ?: return

        if (imageUri != null) {
            viewModel.uploadProfileImage(AuthManager.getUid()!!, imageUri!!, onSuccess = { imageUrl ->
                this.imageUrl = imageUrl
                if (layoutMode == Constants.MODE_EDIT) {
                    viewModel.updateProfileImage(AuthManager.getUid()!!, imageUrl) { _ -> }
                }
            }, onFailure = { _ ->
                toastManager.showError(ToastType.IMAGE_NOT_UPLOADED.message)
            })
        }
    }
}
