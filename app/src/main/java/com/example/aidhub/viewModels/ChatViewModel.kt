package com.example.aidhub.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.data.dataStractures.ChatInboxItem
import com.example.data.dataStractures.ChatRoom
import com.example.data.dataStractures.Message
import com.example.data.dataStractures.Profile
import com.example.data.interfaces.IChatRepository
import com.example.data.interfaces.IProfileRepository
import com.example.aidhub.utilities.Constants


class ChatViewModel(
    private val chatRepository: IChatRepository,
    private val profileRepository: IProfileRepository
) : ViewModel() {

    private val _currentChatRoom = MutableLiveData<ChatRoom?>()
    val currentChatRoom: LiveData<ChatRoom?> get() = _currentChatRoom

    private val _inboxItems = MutableLiveData<List<ChatInboxItem>>()
    val inboxItems: LiveData<List<ChatInboxItem>> get() = _inboxItems

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages
    private var currentChatRoomId: String? = null

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: MutableLiveData<Boolean> get() = _isLoading
    private val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success


    private val _numOfUnreadChats = MutableLiveData<Int>()
    val numOfUnreadChats: LiveData<Int> get() = _numOfUnreadChats


    fun startChat(chatRoomId: String) {
        if (chatRoomId == currentChatRoomId) return
        _isLoading.value = true
        _messages.postValue(listOf())
        currentChatRoomId = chatRoomId

        chatRepository.listenForMessages(chatRoomId) { messageList ->
            _messages.postValue(messageList)
            _isLoading.value = false
            _success.value = true
        }
    }

    fun sendTextMessage(text: String, senderId: String, senderName: String, recipientId: String) {
        val roomId = currentChatRoomId ?: return
        if (text.isBlank()) return

        val newMessage = Message(
            senderId = senderId,
            senderName = senderName,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        chatRepository.sendMessage(roomId, newMessage, recipientId)
    }

    fun loadInbox(myUid: String) {
        chatRepository.getUserChatInboxItems(myUid) { rooms ->
            val tempItems = mutableListOf<ChatInboxItem>()

            if (rooms.isEmpty()) {
                _inboxItems.postValue(listOf())
                return@getUserChatInboxItems
            }

            rooms.forEach { room ->
                val otherUserId = room.participants.first { it != myUid }

                profileRepository.getUserProfile(otherUserId) { user ->
                    if (user == null) {
                        val deletedUser = Profile(
                            uid = Constants.DELETED_ACCOUNT_KEY,
                            fullName = Constants.DELETED_ACCOUNT_KEY
                        )
                        tempItems.add(ChatInboxItem(room, deletedUser))
                    } else
                        tempItems.add(ChatInboxItem(room, user))

                    if (tempItems.size == rooms.size) {
                        _inboxItems.postValue(tempItems.sortedByDescending { it.chatRoom.lastTimestamp })
                    }
                }
            }
        }
    }


    fun getChatRoom(participants: List<String>) {
        _isLoading.value = true
        val chatId = participants.sorted().joinToString("_")
        chatRepository.getChatRoom(chatId) { chatRoom ->
            if (chatRoom == null) {
                chatRepository.createChatRoom(chatId, participants) { chatRoom ->
                    _currentChatRoom.value = chatRoom
                    _isLoading.value = false
                    _success.value = true
                }
            } else {
                _currentChatRoom.value = chatRoom
                _isLoading.value = false
                _success.value = true
            }
        }
    }

    fun setCurrentChatRoom(chatRoom: ChatRoom) {
        _currentChatRoom.value = chatRoom
    }

    fun updateChatRead(chatId: String, userId: String) {
        chatRepository.updateChatRead(chatId, userId)
    }

    fun getNumberOfUnreadChats(userId: String) {
        chatRepository.listenToUnreadChats(userId) { unreadCount ->
            _numOfUnreadChats.postValue(unreadCount)
        }
    }
}



