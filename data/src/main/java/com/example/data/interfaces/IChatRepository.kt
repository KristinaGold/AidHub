package com.example.data.interfaces

import com.example.data.dataStractures.ChatRoom
import com.example.data.dataStractures.Message


interface IChatRepository {

    fun listenForMessages(chatRoomId: String, onMessagesUpdate: (List<Message>) -> Unit)
    fun sendMessage(chatRoomId: String, message: Message, recipientId: String)
    fun getChatRoom(chatId: String, callback: (ChatRoom?) -> Unit)
    fun createChatRoom(chatId: String, participants: List<String>, callback: (ChatRoom) -> Unit)
    fun getUserChatInboxItems(myUid: String, callback: (List<ChatRoom>) -> Unit)
    fun updateChatRead(chatId: String, userId: String)
    fun listenToUnreadChats(userId: String, callback: (Int) -> Unit)
}