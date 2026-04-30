package com.example.aidhub.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TopBarViewModel : ViewModel() {

    private val _showRequestButton = MutableLiveData<Boolean>()
    val showRequestButton: LiveData<Boolean> get() = _showRequestButton
    private val _requestClickEvent = MutableLiveData<Boolean>()
    val requestClickEvent: LiveData<Boolean> get() = _requestClickEvent
    private val _menuClickEvent = MutableLiveData<Boolean>()
    val menuClickEvent: LiveData<Boolean> get() = _menuClickEvent

    private val _showNotificationBadge = MutableLiveData<Boolean>()
    val showNotificationBadge: LiveData<Boolean> get() = _showNotificationBadge

    private val _numOfNotifications = MutableLiveData<Int>()
    val numOfNotifications: LiveData<Int> get() = _numOfNotifications

    private val _openEditProfile = MutableLiveData<Boolean>()
    val openEditProfile: LiveData<Boolean> get() = _openEditProfile

    fun showRequestButton() {
        _showRequestButton.value = true
    }
    fun doNotShowRequestButton() {
        _showRequestButton.value = false
    }
    fun showNotificationBadge(numOfNotifications: Int) {
        _showNotificationBadge.value = numOfNotifications > 0
        _numOfNotifications.value = numOfNotifications
    }
    fun onRequestClickEvent() {
        _requestClickEvent.value = true
    }
    fun onRequestClickEventHandled() {
        _requestClickEvent.value = false
    }
    fun onMenuClickEvent() {
        _menuClickEvent.value = true
    }
    fun onMenuClickEventHandled() {
        _menuClickEvent.value = false
    }

    fun openEditProfile() {
        _openEditProfile.value = true
    }

    fun openEditProfileHandled() {
        _openEditProfile.value = false
    }
}