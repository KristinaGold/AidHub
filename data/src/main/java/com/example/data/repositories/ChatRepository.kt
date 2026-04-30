package com.example.data.repositories

import android.util.Log
import com.example.data.dataStractures.ChatRoom
import com.example.data.dataStractures.Message
import com.example.data.interfaces.IChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

internal class ChatRepository private constructor(): IChatRepository {


    private val db = FirebaseFirestore.getInstance()
    private val chatsCollection = db.collection("ChatRooms")

    internal companion object {

        @Volatile
        private var instance: ChatRepository? = null

        internal fun init(): ChatRepository {
            return instance ?: synchronized(this) {
                instance ?: ChatRepository().also { instance = it }
            }
        }

        internal fun getInstance(): ChatRepository {
            return instance ?: throw IllegalStateException(
                "ChatRepository must be initialized by calling init(context) before use."
            )
        }

    }

    override fun listenForMessages(chatRoomId: String, onMessagesUpdate: (List<Message>) -> Unit) {
        chatsCollection.document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val messages = snapshot?.toObjects(Message::class.java) ?: listOf()
                onMessagesUpdate(messages)
            }
    }

    override fun sendMessage(chatRoomId: String, message: Message, recipientId: String) {
        val chatRoomRef = chatsCollection.document(chatRoomId)
        val msgId = chatsCollection.document().id
        val finalMsg = message.copy(messageId = msgId)

        Log.d("TESTCHAT", "recipientId: $recipientId")

        chatRoomRef.collection("messages").document(msgId).set(finalMsg)

        chatRoomRef.update(
            "lastMessage", message.text,
            "lastTimestamp", message.timestamp,
            "read.${recipientId}", false
        )
    }


    override fun getChatRoom(chatId: String, callback: (ChatRoom?) -> Unit) {
        chatsCollection.document(chatId).get().addOnSuccessListener { chat ->
            val chatRoom = chat.toObject(ChatRoom::class.java)
            callback(chatRoom)
        }
            .addOnFailureListener {
                callback(null)
            }
    }

    override fun createChatRoom(
        chatId: String,
        participants: List<String>,
        callback: (ChatRoom) -> Unit
    ) {
        val chatRoom = ChatRoom(chatRoomId = chatId, participants = participants)
        chatsCollection.document(chatId).set(chatRoom).addOnSuccessListener {
            callback(chatRoom)
        }
    }

    override fun getUserChatInboxItems(myUid: String, callback: (List<ChatRoom>) -> Unit) {
        chatsCollection
            .whereArrayContains("participants", myUid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING).whereNotEqualTo("lastMessage", "")
            .addSnapshotListener { snapshot, _ ->
                val rooms = snapshot?.toObjects(ChatRoom::class.java) ?: listOf()
                callback(rooms)
            }
    }

    override fun updateChatRead(chatId: String, userId: String) {
        chatsCollection.document(chatId).update("read.${userId}", true)
    }

    override fun listenToUnreadChats(userId: String, callback: (Int) -> Unit) {
        chatsCollection.whereArrayContains("participants", userId)
            .whereEqualTo("read.${userId}", false)
            .addSnapshotListener { snapshot, _ ->
                val unreadCount = snapshot?.size() ?: 0
                callback(unreadCount)
            }
    }
}