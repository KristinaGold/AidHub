package com.example.aidhub.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.aidhub.managers.AnimationManager
import com.example.aidhub.R
import com.example.data.dataStractures.Status
import com.example.aidhub.databinding.LayoutTopbarBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.managers.startBlinkAnimation
import com.example.aidhub.viewModels.TopBarViewModel
import com.google.android.material.chip.Chip

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!
    private val topBarViewModel: TopBarViewModel by activityViewModels()
    protected lateinit var topBarBinding: LayoutTopbarBinding
    private lateinit var statusChip: Chip



    abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    abstract fun setupTopBar()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = inflateBinding(inflater, container)
        val topBarView = binding.root.findViewById<View>(R.id.topBar)
        if (topBarView != null) {
            topBarBinding = LayoutTopbarBinding.bind(topBarView)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetTopBar()
        setupTopBar()
    }

    private fun resetTopBar() {
        if (!::topBarBinding.isInitialized) return
        topBarBinding.apply {
            btnBack.visibility = View.GONE
            layoutNotifications.visibility = View.GONE
            btnEditProfile.visibility = View.GONE
            searchViewSkills.visibility = View.GONE
            chipGroupStatus.visibility = View.GONE
            screenTitle.visibility = View.GONE
            btnSettings.visibility = View.GONE
        }
    }

    protected fun setTopBarTitle(title: String) {
        topBarBinding.screenTitle.visibility = View.VISIBLE
        topBarBinding.screenTitle.text = title

    }

    protected fun showBackButton() {
        topBarBinding.btnBack.apply {
            visibility = View.VISIBLE
            setOnClickListener { findNavController().navigateUp() }
        }
    }

    protected fun showNotificationButton() {
        topBarBinding.layoutNotifications.visibility = View.VISIBLE
        topBarBinding.btnNotification.setOnClickListener { notificationButtonClicked() }

        topBarViewModel.showNotificationBadge.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow)
                topBarBinding.notificationBadge.visibility = View.VISIBLE
            else
                topBarBinding.notificationBadge.visibility = View.GONE

        }
        topBarViewModel.numOfNotifications.observe(viewLifecycleOwner) { numOfNotifications ->
            topBarBinding.notificationBadge.text = "$numOfNotifications"
        }
    }
    private fun notificationButtonClicked() {
        val navOptions1 = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left).setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right).build()
        findNavController().navigate(R.id.notificationsFragment, null, navOptions1)
    }

    protected fun showSearchView() {
        topBarBinding.searchViewSkills.visibility = View.VISIBLE

    }
    protected fun toggleSearchAnimation(isCollapsed: Boolean) {
        AnimationManager.transformSearchToIcon(topBarBinding.searchViewSkills, topBarBinding.searchContainer, isCollapsed)
    }
    protected fun showEditProfileButton(onClick: () -> Unit) {
        topBarBinding.btnEditProfile.apply {
            visibility = View.VISIBLE
            setOnClickListener { onClick() }
        }
    }

    protected fun showSettingsButton() {
        topBarBinding.btnSettings.apply {
            visibility = View.VISIBLE
            setOnClickListener { topBarViewModel.onMenuClickEvent() }
        }
    }

    protected fun showTopRequestButton() {

        topBarViewModel.showRequestButton.observe(viewLifecycleOwner) { shouldShow ->
            if (shouldShow) {
                topBarBinding.topHelpRequest.visibility = View.VISIBLE
                topBarBinding.topHelpRequest.startBlinkAnimation()
            } else {
                topBarBinding.topHelpRequest.clearAnimation()
                topBarBinding.topHelpRequest.visibility = View.GONE
            }
        }
        topBarBinding.topHelpRequest.setOnClickListener {
            topBarViewModel.onRequestClickEvent()
        }
    }

    protected fun showStatusChip(status: String) {
        topBarBinding.chipGroupStatus.visibility = View.VISIBLE
        if (::statusChip.isInitialized)
            statusChip.clearAnimation()
        statusChip = ChipBuilder.createStatusChip(requireContext(), status)
       // val chipGroup = binding.topBar.chipGroupStatus
        topBarBinding.chipGroupStatus.removeAllViews()
        if (status == Status.IN_PROGRESS.displayName)
            statusChip.startBlinkAnimation()
        topBarBinding.chipGroupStatus.addView(statusChip)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}