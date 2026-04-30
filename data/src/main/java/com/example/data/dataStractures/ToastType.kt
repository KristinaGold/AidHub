package com.example.data.dataStractures

enum class ToastType(val message: String) {
    EMPTY_NAME("Please enter your name"),
    EMPTY_TAG("Please select at least one tag"),
    EMPTY_CATEGORY("Please select a category"),
    PROFILE_UPDATED("Profile updated!"),
    PROFILE_SAVED("Profile saved!"),
    REVIEW_ADDED("Review added!"),
    PROFILE_DELETED("Profile data deleted!"),
    PROFILE_NOT_SAVED("Failed to save profile"),
    IMAGE_UPLOADED("Image uploaded!"),
    IMAGE_NOT_UPLOADED("Failed to upload image"),
    POST_PUBLISHED("Post Published!"),
    POST_DELETED("Post deleted!"),
    REQUEST_PUBLISHED("Request Published!"),
    REQUEST_NOT_FOUND("Request not found"),
    HELPER_BUSY("You are already helping another request!"),
    ERROR("Error, Try again."),
    LOCATION_PERMISSION_DISABLED("Location permission is disabled"),
    WRONG_PASSWORD("Wrong password. Please try again."),
    DISTANCE_UPDATED("Distance updated!"),
    DISTANCE_NOT_UPDATED("Failed to update distance"),
    EMPTY_PASSWORD("Password cannot be empty"),
    DELETED_ACCOUNT("This account has been deleted!"),




}