package com.example.data.dataStractures

data class Request(
    val requestId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val requestImageUrl: String = "",
    val title: String = "",
    val content: String = "",
    val locationInfo: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val timestamp: Long = 0,
    val urgent: Boolean = false,
    val tag: String = "",
    val status: String = "",
    val helperId: String = "",
    val rating: Double = 0.0,
    val points: Int = 0,
    val review: String = "",
    val reviewTimestamp: Long = 0
)


enum class Status(val displayName: String) {
    OPEN("OPEN"),
    IN_PROGRESS("IN PROGRESS"),
    COMPLETED("COMPLETED"),
    CANCELLED("CANCELLED")
}


sealed class RequestScreenState {
    object AuthorWaiting : RequestScreenState()
    object CancelledRequestAuthor : RequestScreenState()
    object ClosedRequestAuthor : RequestScreenState()
    object CancelledRequestHelper : RequestScreenState()
    object ClosedRequestHelper : RequestScreenState()
    object AuthorWithHelper : RequestScreenState()
    object ViewerCanHelp : RequestScreenState()
    object ViewerRequestBusy : RequestScreenState()
    object IAmTheHelper : RequestScreenState()
}


