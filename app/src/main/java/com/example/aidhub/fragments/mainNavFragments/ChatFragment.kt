package com.example.aidhub.fragments.mainNavFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.R
import com.example.aidhub.adapters.ChatInboxAdapter
import com.example.aidhub.fragments.base.BaseFragment
import com.example.data.dataStractures.ChatInboxItem
import com.example.aidhub.databinding.FragmentChatBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.ProfileViewModel

class ChatFragment : BaseFragment<FragmentChatBinding>() {

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentChatBinding.inflate(inflater, container, false)

    override fun setupTopBar() {
        setTopBarTitle(Constants.CHAT_SCREEN_TITLE)
        showNotificationButton()
        showTopRequestButton()
    }

    private lateinit var chatAdapter: ChatInboxAdapter
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var chatList = listOf<ChatInboxItem>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadInbox()
    }

    private fun loadInbox() {
        chatViewModel.loadInbox(AuthManager.getUid()!!)
        chatViewModel.inboxItems.observe(viewLifecycleOwner) { chatList ->
            this.chatList = chatList
            setupRecyclerView()
        }
    }


    private fun setupRecyclerView() {
        chatAdapter = ChatInboxAdapter(chatList) { chatRoom, otherUser ->
            chatViewModel.setCurrentChatRoom(chatRoom)
            profileViewModel.setOtherUserData(otherUser)
            chatViewModel.updateChatRead(chatRoom.chatRoomId, AuthManager.getUid()!!)
            findNavController().navigate(R.id.action_chatFragment_to_chatRoomFragment)
        }

        binding.recyclerViewFeed.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

}