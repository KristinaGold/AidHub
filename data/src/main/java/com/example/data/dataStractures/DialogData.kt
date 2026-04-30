package com.example.data.dataStractures

import com.example.data.R

data class DialogData(
    val title: String = "",
    val message: String = "",
    val positiveButtonText: String = "",
    val negativeButtonText: String = "",
    val icon: Int = 0
)

enum class DialogType(val data: DialogData) {
    LOG_OUT(DialogData(
        title = "Are you sure you want to log out?",
        message = "",
        positiveButtonText = "Log Out",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_logout
    )),
    DELETE_ACCOUNT(DialogData(
        title = "Are you sure you want to delete your account?",
        message = "All of your data will be permanently deleted.",
        positiveButtonText = "Delete",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_trash
    )),
    CONFIRM_DELETE(DialogData(
        title = "Delete Account",
        message = "For security, please enter your password to confirm deletion.",
        positiveButtonText = "Delete Forever",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_trash
    )),
    PERMISSION_NOTIFICATIONS(DialogData(
        title = "Permission Required",
        message = "You have previously denied notification permissions. \nTo use this feature, please enable notifications in the app settings.",
        positiveButtonText = "Go to Settings",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_notification
    )),
    PERMISSION_LOCATION(DialogData(
        title = "Permission Required",
        message = "You have previously denied location permissions. \nTo use this feature, please enable location in the app settings.",
        positiveButtonText = "Go to Settings",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_location
    )),
    SKIP_NOTIFICATION(DialogData(
        title = "Are you sure you want to skip?",
        message = "Notification permissions are needed. To use this feature, please enable it in the app settings.",
        positiveButtonText = "Go to Settings",
        negativeButtonText = "Skip",
        icon = R.drawable.ic_notification
    )),
    SKIP_LOCATION(DialogData(
        title = "Are you sure you want to skip?",
        message = "Location permissions are needed. To use this feature, please enable it in the app settings.",
        positiveButtonText = "Go to Settings",
        negativeButtonText = "Skip",
        icon = R.drawable.ic_location
    )),
    TURN_OFF_PERMISSION(DialogData(
        title = "Are you sure you want to disable the permission?",
        message = "Some features may not work without the permission.",
        positiveButtonText = "Go to Settings",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_settings
    )),
    POST_DELETE(DialogData(
        title = "Are you sure you want to delete this post?",
        message = "",
        positiveButtonText = "Delete",
        negativeButtonText = "Cancel",
        icon = R.drawable.ic_trash
    )),
    CONFIRM_CANCEL_REQUEST(DialogData(
        title = "Are you sure you want to cancel this request?",
        message = "",
        positiveButtonText = "Cancel",
        negativeButtonText = "Keep",
        icon = R.drawable.ic_cancel
    ))
}
