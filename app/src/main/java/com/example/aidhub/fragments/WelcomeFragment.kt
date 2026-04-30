package com.example.aidhub.fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.aidhub.R
import com.example.aidhub.managers.SettingsManager
import com.example.aidhub.activities.MainActivity
import com.example.aidhub.adapters.WelcomeAdapter
import com.example.data.dataStractures.DialogType
import com.example.aidhub.databinding.FragmentWelcomeBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.google.android.material.tabs.TabLayoutMediator


class WelcomeFragment : Fragment(R.layout.fragment_welcome) {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPager: ViewPager2
    private val settingsManager = SettingsManager.getInstance()
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
    private var currentItem = 0

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) settingsManager.openAppSettings(requireActivity())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        currentItem = viewPager.currentItem
        setupAdapter()
        setupTabLayout()
    }

    private fun setupAdapter() {
        viewPager.adapter = WelcomeAdapter(onPermissionRequest = { page ->
            when (page) {
                0 -> requestLocationPermission()
                1 -> requestNotificationPermission()
                else -> finishOnboarding()
            }
        }, onSkipPressed = { page ->
            if (page == 0) showSettingsDialog(Constants.LOCATION_PERMISSION)
            else if (page == 1) showSettingsDialog(Constants.NOTIFICATION_PERMISSION)
        })
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, _ ->
            tab.setIcon(R.drawable.ic_dot_inactive)
        }.attach()
    }

    private fun requestLocationPermission() {
        if (settingsManager.hasLocationPermission()) {
            viewPagerProgress()
        } else {
            permissionLauncher.launch(locationPermission)
        }
    }

    private fun requestNotificationPermission() {
        if (settingsManager.areNotificationsEnabled()) {
            viewPagerProgress()
        } else {
            permissionLauncher.launch(notificationPermission)
        }
    }


    private fun showSettingsDialog(permission: Int) {
        val permissionType =
            if (permission == Constants.NOTIFICATION_PERMISSION) DialogType.SKIP_NOTIFICATION else DialogType.SKIP_LOCATION

        DialogHelper.showAlertDialog(requireContext(), permissionType) { buttonId, _ ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                if (permission == Constants.NOTIFICATION_PERMISSION) requestNotificationPermission()
                else requestLocationPermission()
            } else {
                viewPagerProgress()
            }
        }
    }

    private fun viewPagerProgress() {
        currentItem = viewPager.currentItem
        if (currentItem < (binding.viewPager.adapter?.itemCount ?: 0) - 1) {
            binding.viewPager.setCurrentItem(currentItem + 1, true)
        } else {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}