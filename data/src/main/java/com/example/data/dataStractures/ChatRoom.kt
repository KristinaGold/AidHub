package com.example.data.dataStractures

data class ChatRoom(
    val chatRoomId: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0,
    val read: MutableMap<String, Boolean> = mutableMapOf()
)

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatInboxItem(
    val chatRoom: ChatRoom,
    val otherUser: Profile
)