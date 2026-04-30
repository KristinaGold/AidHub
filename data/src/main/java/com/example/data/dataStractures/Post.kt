package com.example.data.dataStractures

data class Post(
    val postId: String ="",
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val postImageUrl: String = "",
    val content: String ="",
    val locationInfo: String = "",
    val timestamp: Long = 0,
    val tags: List<String> = listOf(),
    val likes: MutableList<String> = mutableListOf()
)