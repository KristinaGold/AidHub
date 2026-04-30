package com.example.aidhub.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.managers.SettingsManager
import com.example.data.dataStractures.DialogType
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentSettingsBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.DialogHelper
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.TopBarViewModel
import com.google.android.material.slider.Slider
import kotlin.getValue

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val mainViewModel: TopBarViewModel by activityViewModels()
    private val toastManager = ToastHelper.getInstance()
    private val settingsManager = SettingsManager.getInstance()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private var currentMaxRadius: Int = 10
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        binding.switchNotification.isChecked = isGranted
        if (!isGranted) {
            showSettingsDialog(Constants.NOTIFICATION_PERMISSION)
            binding.layoutDistance.visibility = View.GONE
        } else binding.layoutDistance.visibility = View.VISIBLE
    }
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        binding.switchLocation.isChecked = isGranted
        if (!isGranted) showSettingsDialog(Constants.LOCATION_PERMISSION)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.topBar.txtTitle.text = Constants.SETTINGS_SCREEN_TITLE
        profileViewModel.getCurrentMaxRadius(AuthManager.getUid()!!)

        binding.switchNotification.setOnClickListener {
            handleNotificationSwitch()
        }

        binding.switchLocation.setOnClickListener {
            handleLocationSwitch()
        }

        binding.topBar.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEditProfile.setOnClickListener {
            mainViewModel.openEditProfile()
        }

        binding.btnLogout.setOnClickListener {
            profileViewModel.requestSignOut()
        }

        binding.btnDeleteProfile.setOnClickListener {
            showDeleteDialog()
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.toggleDarkMode(isChecked)
        }

        binding.switchDarkMode.isChecked = settingsManager.isDarkModeEnabled()

        binding.sliderDistance.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                val newValue = slider.value.toDouble()
                profileViewModel.updateNotificationDistances(
                    AuthManager.getUid()!!,
                    newValue
                )
            }
        })
        binding.sliderDistance.addOnChangeListener { _, value, _ ->
            binding.txtDistanceLabel.text = "Notification Radius: ${value.toInt()} km"
        }
    }

    override fun onResume() {
        super.onResume()
        checkLocationPermission()
        checkNotificationPermission()
        observeCurrentMaxRadius()
    }

    private fun observeCurrentMaxRadius() {
        profileViewModel.currentMaxRadius.observe(viewLifecycleOwner) { radius ->
            currentMaxRadius = radius.toInt()
            binding.sliderDistance.value = currentMaxRadius.toFloat()
            binding.txtDistanceLabel.text = "Notification Radius: ${currentMaxRadius} km"
        }
        profileViewModel.success.observe(viewLifecycleOwner) { success ->
            if (success) {
                toastManager.showSuccess(ToastType.DISTANCE_UPDATED.message)
                profileViewModel.resetSuccess()
            }
        }
    }

    private fun handleNotificationSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (settingsManager.areNotificationsEnabled()) {
                binding.switchNotification.isChecked = true
                showDisablePermissionDialog()
            } else {
                binding.switchNotification.isChecked = false
                notificationPermissionLauncher.launch(notificationPermission)
            }
        } else {
            settingsManager.openAppSettings(requireActivity())
        }
    }

    private fun handleLocationSwitch() {
        if (settingsManager.hasLocationPermission()) {
            binding.switchLocation.isChecked = true
            showDisablePermissionDialog()
        } else {
            binding.switchLocation.isChecked = false
            locationPermissionLauncher.launch(locationPermission)
        }
    }

    private fun checkLocationPermission() {
        binding.switchLocation.isChecked = settingsManager.hasLocationPermission()
    }

    private fun checkNotificationPermission() {
        val notificationEnabled = settingsManager.areNotificationsEnabled()
        binding.switchNotification.isChecked = notificationEnabled
        binding.layoutDistance.visibility = if (notificationEnabled) View.VISIBLE else View.GONE
    }


    private fun showDisablePermissionDialog() {
        DialogHelper.showAlertDialog(
            requireContext(),
            DialogType.TURN_OFF_PERMISSION
        ) { buttonId, _ ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                settingsManager.openAppSettings(requireActivity())
            }
        }
    }

    private fun showSettingsDialog(permission: Int) {
        val permissionType =
            if (permission == Constants.NOTIFICATION_PERMISSION) DialogType.PERMISSION_NOTIFICATIONS else DialogType.PERMISSION_LOCATION
        DialogHelper.showAlertDialog(requireContext(), permissionType) { buttonId, _ ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                settingsManager.openAppSettings(requireActivity())
            }
            else
                binding.switchNotification.isChecked = false
        }
    }

    private fun reauthenticateAndDelete(password: String) {
        val userId = AuthManager.getUid() ?: return
        AuthManager.reauthenticateAndDelete(password, onResult = { success ->
            if (success) profileViewModel.deleteProfile(userId)
            else showDeleteConfirmationDialog()
        })
    }

    private fun showDeleteDialog() {
        DialogHelper.showAlertDialog(
            requireContext(),
            DialogType.DELETE_ACCOUNT
        ) { buttonId, _ ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                showDeleteConfirmationDialog()
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        DialogHelper.showAlertDialog(
            requireContext(),
            DialogType.CONFIRM_DELETE
        ) { buttonId, password ->
            if (buttonId == Constants.BUTTON_POSITIVE_KEY) {
                reauthenticateAndDelete(password)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
