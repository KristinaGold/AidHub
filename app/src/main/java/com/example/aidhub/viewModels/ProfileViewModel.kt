package com.example.aidhub.viewModels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.ImageStorage
import com.example.data.dataStractures.Notification
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.Request
import com.example.data.dataStractures.Review
import com.example.data.interfaces.IProfileRepository
import com.example.data.interfaces.IRequestRepository
import com.google.firebase.firestore.FieldValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileViewModel(
    private val profileRepository: IProfileRepository,
    private val requestRepository: IRequestRepository
) : ViewModel() {

    private val _currentUserData = MutableLiveData<Profile?>()
    val currentUserData: LiveData<Profile?> get() = _currentUserData
    private val _otherUserData = MutableLiveData<Profile?>()
    val otherUserData: LiveData<Profile?> get() = _otherUserData
    private val _profilePressedUid = MutableLiveData<String?>()
    private val _profilePressedUserData = MutableLiveData<Profile?>()
    val profilePressedUserData: LiveData<Profile?> get() = _profilePressedUserData
    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications
    private val _numOfUnreadNotifications = MutableLiveData<Int>()
    val numOfUnreadNotifications: LiveData<Int> get() = _numOfUnreadNotifications
    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoadingLiveData: LiveData<Boolean> get() = _isLoading
    private val _userReviews = MutableLiveData<List<Review>>()
    val userReviews: LiveData<List<Review>> get() = _userReviews
    private val _signOutEvent = MutableLiveData<Boolean>()
    val signOutEvent: LiveData<Boolean> get() = _signOutEvent
    private val _deleteEvent = MutableLiveData<Boolean>()
    val deleteEvent: LiveData<Boolean> get() = _deleteEvent
    private val _currentMaxRadius = MutableLiveData<Double>()
    val currentMaxRadius: LiveData<Double> get() = _currentMaxRadius
    private val _tokenDeleteEvent = MutableLiveData<Boolean>()
    val tokenDeleteEvent: LiveData<Boolean> get() = _tokenDeleteEvent


    fun resetSuccess() {
        _success.value = false
    }


    fun requestSignOut() {
        _signOutEvent.value = true
    }

    fun onSignOutHandled() {
        _signOutEvent.value = false
    }

//    fun requestDelete() {
//        _deleteEvent.value = true
//    }
//
//    fun onDeleteHandled() {
//        _deleteEvent.value = false
//    }

    fun setProfilePressedUid(uid: String) {
        _profilePressedUid.value = uid
        _isLoading.value = true
        if (_profilePressedUid.value == _currentUserData.value?.uid) {
            _profilePressedUserData.value = _currentUserData.value
        } else {
            profileRepository.getUserProfile(_profilePressedUid.value!!) { userData ->
                _profilePressedUserData.postValue(userData)
                _isLoading.postValue(false)
            }
        }
    }


    fun setOtherUserData(userData: Profile) {
        _otherUserData.value = userData
    }


    fun createProfile(
        userId: String,
        name: String,
        bio: String,
        imageUrl: String,
        selectedSkills: MutableList<String>,
        onComplete: (Boolean) -> Unit
    ) {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateStr = formatter.format(Date())
        val newProfile = Profile(
            uid = userId,
            fullName = name,
            bio = bio,
            profileImageUrl = imageUrl,
            skills = selectedSkills,
            memberSince = dateStr
        )
        profileRepository.saveUserProfile(newProfile) { success ->
            if (success) onComplete(true)
            else onComplete(false)
        }
    }

    fun getProfile(uid: String) {
        profileRepository.getUserProfile(uid) { userData ->
            _otherUserData.postValue(userData)
        }
    }

    fun startListeningToCurrentProfile(uid: String) {
        profileRepository.listenToUserProfile(uid) { userData ->
            _currentUserData.postValue(userData)
        }
    }

    fun updateProfile(uid: String, name: String, bio: String, selectedSkills: MutableList<String>) {
        val updates = hashMapOf(
            "fullName" to name,
            "bio" to bio,
            "skills" to selectedSkills
        )
        profileRepository.updateFields(uid, updates) { success ->
            _success.value = success
        }
    }

    fun acceptRequest(uid: String, requestId: String) {
        profileRepository.updateFields(uid, mapOf("currentTakenRequest" to requestId)) { success ->
        }
    }

    fun rejectRequest(uid: String) {
        profileRepository.updateFields(uid, mapOf("currentTakenRequest" to "")) { success ->
        }
    }

    fun updateLastLocation(userId: String, lat: Double, lon: Double) {
        val userLocation = mapOf(
            "lat" to lat,
            "lon" to lon,
            "lastLocationUpdate" to FieldValue.serverTimestamp()
        )
        profileRepository.updateUserLastLocation(userId, userLocation)
    }


    fun completeRequest(request: Request?, rating: Float, review: String, urgent: Boolean) {
        addReview(request, rating, review)
        profileRepository.getUserProfile(request!!.helperId) { userData ->
            var points = userData!!.points
            var pointsScored = if (urgent) 10 else 5
            pointsScored += if (rating > 3) 5 else 0
            points += pointsScored
            var helpsGiven = userData.helpsGiven
            helpsGiven += 1
            var stars = userData.rating + rating
            stars /= if (helpsGiven > 1) 2 else 1
            requestRepository.rateRequest(
                userData.currentTakenRequest,
                rating.toDouble(),
                pointsScored, review
            )
            profileRepository.updateFields(
                request.helperId,
                mapOf(
                    "points" to points,
                    "helpsGiven" to helpsGiven,
                    "rating" to stars,
                    "currentTakenRequest" to ""
                )
            ) { success ->
                _success.value = success
            }

        }

    }

    fun addReview(request: Request?, rating: Float, review: String) {
        val review = Review(
            request!!.userId,
            request.userName,
            request.userImageUrl,
            rating.toDouble(),
            review,
            System.currentTimeMillis(),
            request.tag
        )
        profileRepository.addReview(request.helperId, review)
    }

    fun getUserReviews(userId: String) {
        profileRepository.getUserReviews(userId) { reviews ->
            _userReviews.postValue(reviews)
        }
    }


    fun assignToken(uid: String, token: String) {
        profileRepository.updateFields(uid, mapOf("fcmToken" to token)) { success ->
        }
    }

    fun deleteToken(uid: String) {
        _tokenDeleteEvent.value = false
        profileRepository.updateFields(uid, mapOf("fcmToken" to "")) { success ->
            _tokenDeleteEvent.value = success
        }
    }

    fun getUserNotifications(userId: String) {
        profileRepository.getUserNotifications(userId) { notifications ->
            _notifications.postValue(notifications)
        }
    }

    fun getNumberOfUnreadNotifications(userId: String) {
        profileRepository.listenToUnreadNotifications(userId) { unreadCount ->
            _numOfUnreadNotifications.postValue(unreadCount)
        }
    }


    fun updateNotificationRead(userId: String, notificationId: String) {
        profileRepository.updateNotificationRead(userId, notificationId)
    }

    fun getCurrentMaxRadius(userId: String) {
        profileRepository.getCurrentMaxRadius(userId) { radius ->
            _currentMaxRadius.postValue(radius)
        }
    }


    fun uploadProfileImage(
        uid: String,
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        ImageStorage.uploadProfileImage(uid, imageUri, onSuccess = { imageUrl ->
            onSuccess(imageUrl)
        }, onFailure = { e ->
            onFailure(e)
        })
    }


    fun updateProfileImage(uid: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        profileRepository.updateProfileImage(uid, imageUrl) { success ->
            onComplete(success)
        }
    }

    fun updateNotificationDistances(userId: String, distance: Double) {
        profileRepository.updateNotificationDistances(userId, distance) { success ->
            _success.value = success
        }
    }

    fun deleteProfile(uid: String) {
        _deleteEvent.value = true
        profileRepository.deleteProfile(uid) { success ->
            if (success)
                _deleteEvent.value = false
        }
    }
}
