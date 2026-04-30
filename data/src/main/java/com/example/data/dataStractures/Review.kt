package com.example.data.dataStractures

data class Review (
    val raterUid: String = "",
    val raterName: String = "",
    val raterImageUrl: String = "",
    val rating: Double = 0.0,
    val review: String = "",
    val timestamp: Long = 0,
    val tag: String = ""
)