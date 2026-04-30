package com.example.aidhub.fragments.mainNavFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.factories.MyViewModelFactory
import com.example.aidhub.adapters.ProfilePagerAdapter
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.Profile
import com.example.aidhub.databinding.FragmentProfileBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.TopBarViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentProfileBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        showTopRequestButton()
        showNotificationButton()
    }

    private val profileViewModel: ProfileViewModel by activityViewModels { MyViewModelFactory() }
    private val mainViewModel: TopBarViewModel by activityViewModels()
    private var profileMode = Constants.MODE_MY_PROFILE
    private var userProfile: Profile? = null
    private var targetUserId: String? = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        targetUserId = arguments?.getString(Constants.USER_ID_KEY) ?: AuthManager.getUid()
        val adapter = ProfilePagerAdapter(this, targetUserId!!)
        binding.profileViewPager.adapter = adapter
        observeLoadingState()
        configureTabLayout()
        configureAppbarLayout()

        if (!targetUserId.isNullOrEmpty()) {
            val isMyOwnProfile = targetUserId == AuthManager.getUid()
            if (isMyOwnProfile) {
                profileMode = Constants.MODE_MY_PROFILE
                showSettingsButton()
                setupLayoutMyProfile()
            } else {
                profileMode = Constants.MODE_OTHER_PROFILE
                showBackButton()
                setupLayoutAnotherProfile()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (profileMode == Constants.MODE_OTHER_PROFILE)
            profileViewModel.setProfilePressedUid(targetUserId!!)
    }

    private fun configureAppbarLayout() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val maxScroll = appBarLayout.totalScrollRange
            if (maxScroll == 0) return@addOnOffsetChangedListener

            val percentage = abs(verticalOffset).toFloat() / maxScroll
            val scale = 1f - (percentage * 0.5f)

            binding.imgProfilePicture.scaleX = scale
            binding.imgProfilePicture.scaleY = scale
            val toolbarHeight = binding.topBar.root.height
            val finalTranslationY = -(toolbarHeight / 2f)

            binding.imgProfilePicture.translationY = percentage * finalTranslationY
        }
    }


    private fun configureTabLayout() {
        TabLayoutMediator(binding.profileTabLayout, binding.profileViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> Constants.REQUESTS_TAB_TITLE
                1 -> Constants.POSTS_TAB_TITLE
                2 -> Constants.REVIEWS_TAB_TITLE
                else -> null
            }
        }.attach()

    }

    private fun observeLoadingState() {
        profileViewModel.isLoadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBarLayout.visibility = View.VISIBLE
            } else if (!isLoading) {
                binding.progressBarLayout.visibility = View.GONE
            }
        }
    }

    private fun setupLayoutMyProfile() {
        binding.btnEdit.visibility = View.VISIBLE
        binding.btnEdit.setOnClickListener {
            mainViewModel.openEditProfile()
        }
        profileViewModel.currentUserData.observe(viewLifecycleOwner) { userData ->
            if (userData == null) return@observe
            userProfile = userData
            updateUI(userData)

        }
    }

    private fun setupLayoutAnotherProfile() {
        binding.btnEdit.visibility = View.GONE
        profileViewModel.profilePressedUserData.observe(viewLifecycleOwner) { userData ->
            if (userData == null) return@observe
            userProfile = userData
            updateUI(userData)

        }
    }

    private fun updateUI(profile: Profile) {
        binding.txtFullName.text = profile.fullName
        binding.txtBio.text = profile.bio
        // binding.txtMemberSince.text = "Member since: ${profile.memberSince}"
        binding.imgProfilePicture.loadProfileImage(profile.profileImageUrl)
        binding.txtPoints.text = profile.points.toString()
        binding.txtHelpsGiven.text = profile.helpsGiven.toString()
        binding.txtRating.text = String.format("%.1f", profile.rating)

        if (profile.skills.isNullOrEmpty())
            binding.txtNoSkills.visibility = View.VISIBLE
        else
            binding.txtNoSkills.visibility = View.GONE


        displayUserSkills(profile.skills)
    }


    private fun displayUserSkills(skills: List<String>?) {
        val skillsChip = binding.chipGroupSkills
        skillsChip.removeAllViews()
        skills?.forEach { skillName ->
            val chip = ChipBuilder.createSkillChip(
                requireContext(),
                skillName,
                isCheckable = false,
                isClickable = false
            )
            skillsChip.addView(chip)
        }
    }
}