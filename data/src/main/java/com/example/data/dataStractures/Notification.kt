package com.example.data.dataStractures

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Notification(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean = false,
    val relatedId: String = "",
    val type: String = ""
)

enum class NotificationType(val displayName: String) {
    NEW_REQUEST("NEW_REQUEST"),
    HELPER_FOUND("HELPER_FOUND"),
    REQUEST_CLOSED("REQUEST_CLOSED"),
    CHAT("CHAT"),
    POST_LIKE("POST_LIKE")

}