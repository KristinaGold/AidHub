package com.example.data.dataStractures

import com.google.firebase.Timestamp

data class Profile(
    val uid: String? = null,
    val fullName: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null,
    val rating: Double = 0.0,
    val points: Int = 0,
    val helpsGiven: Int = 0,
    val skills: MutableList<String>? = null,
    val memberSince: String? = null,
    val currentTakenRequest: String = "",
    val fcmToken: String = "",
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val lastLocationUpdate: Timestamp? = null,
    val maxRadius: Int = 10
)
