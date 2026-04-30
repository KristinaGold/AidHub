package com.example.aidhub.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aidhub.R
import com.example.data.dataStractures.ChatInboxItem
import com.example.data.dataStractures.ChatRoom
import com.example.data.dataStractures.Profile
import com.example.aidhub.databinding.ItemChatBinding
import com.example.aidhub.utilities.loadProfileImage


class ChatInboxAdapter(
    private val chats: List<ChatInboxItem>,
    private val onChatClicked: (ChatRoom, Profile) -> Unit
) :
    RecyclerView.Adapter<ChatInboxAdapter.ChatViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chat: ChatInboxItem) {

            binding.userName.text = chat.otherUser.fullName
            binding.userImage.loadProfileImage(chat.otherUser.profileImageUrl)
            binding.txtLastMessage.text = chat.chatRoom.lastMessage
            binding.txtTime.text = DateUtils.getRelativeTimeSpanString(chat.chatRoom.lastTimestamp)
            binding.root.setOnClickListener { onChatClicked(chat.chatRoom, chat.otherUser) }


            val myUid = chat.chatRoom.participants.first { it != chat.otherUser.uid }

            val unreadColor =
                ContextCompat.getColor(binding.root.context, R.color.unread_background)
            val readColor = ContextCompat.getColor(binding.root.context, R.color.read_background)


            if (chat.chatRoom.read[myUid] == true) {
                binding.holderView.setCardBackgroundColor(readColor)
                binding.holderView.cardElevation = 6f
                binding.txtLastMessage.alpha = 1f
                binding.unreadBadge.visibility = ViewGroup.GONE
            } else {
                binding.holderView.setCardBackgroundColor(unreadColor)
                binding.holderView.cardElevation = 2f
                binding.unreadBadge.visibility = ViewGroup.VISIBLE
            }
        }
    }
}