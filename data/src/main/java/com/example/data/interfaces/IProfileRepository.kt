package com.example.data.interfaces

import com.example.data.dataStractures.Notification
import com.example.data.dataStractures.Profile
import com.example.data.dataStractures.Review

interface IProfileRepository {
    fun saveUserProfile(user: Profile, onComplete: (Boolean) -> Unit)
    fun getUserProfile(uid: String, onSuccess: (Profile?) -> Unit)
    fun listenToUserProfile(uid: String, onUpdate: (Profile?) -> Unit)
    fun updateFields(uid: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit)
    fun updateUserLastLocation(userId: String, userLocation: Map<String, Any>)
    fun getUserNotifications(userId: String, onComplete: (List<Notification>) -> Unit)
    fun getUserReviews(userId: String, onComplete: (List<Review>) -> Unit)
    fun listenToUnreadNotifications(userId: String, updateNotificationBadge: (Int) -> Unit)
    fun updateNotificationRead(userId: String, notificationId: String)
    fun addReview(uid: String, review: Review)
    fun updateProfileImage(uid: String, imageUrl: String, onComplete: (Boolean) -> Unit)
    fun updateNotificationDistances(userId: String, distance: Double, onComplete: (Boolean) -> Unit)
    fun getCurrentMaxRadius(userId: String, onComplete: (Double) -> Unit)
    fun deleteProfile(uid: String, onComplete: (Boolean) -> Unit)
}