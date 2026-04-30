package com.example.aidhub.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aidhub.managers.AuthManager
import com.example.aidhub.R
import com.example.aidhub.adapters.ChatRoomAdapter
import com.example.data.dataStractures.Message
import com.example.data.dataStractures.ToastType
import com.example.aidhub.databinding.FragmentChatroomBinding
import com.example.aidhub.utilities.Constants
import com.example.aidhub.utilities.ToastHelper
import com.example.aidhub.utilities.loadProfileImage
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.ProfileViewModel

class ChatRoomFragment : Fragment() {


    private var _binding: FragmentChatroomBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ChatRoomAdapter
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private var currentUserName = ""
    private var messageList = listOf<Message>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatroomBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var chatRoomId = ""
        var userId = ""
        profileViewModel.currentUserData.observe(viewLifecycleOwner) { user ->
            currentUserName = user?.fullName ?: ""
        }
        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }

        chatViewModel.currentChatRoom.observe(requireActivity()) { chatRoom ->
            chatRoomId = chatRoom!!.chatRoomId
            chatViewModel.startChat(chatRoomId)
        }
        setupRecyclerView()
        observeMessages()


        if (messageList.isNotEmpty()) {
            binding.rvMessages.smoothScrollToPosition(messageList.size - 1)
        }


        profileViewModel.otherUserData.observe(requireActivity()) {
            binding.txtChatName.text = it?.fullName
            binding.chatImagePreview.loadProfileImage(it?.profileImageUrl)
            userId = it?.uid!!
        }


        binding.btnSend.setOnClickListener {
            val text = binding.etMessageInput.text.toString()
            chatViewModel.sendTextMessage(text, AuthManager.getUid()!!, currentUserName, userId)
            binding.etMessageInput.text?.clear()
        }


        binding.chatImagePreview.setOnClickListener {
            if (userId != Constants.DELETED_ACCOUNT_KEY) {
                profileViewModel.setProfilePressedUid(userId)
                val bundle = Bundle()
                bundle.putString(Constants.USER_ID_KEY, userId)
                findNavController().navigate(
                    R.id.action_chatRoomFragment_to_profileFragment,
                    bundle
                )
            } else
                ToastHelper.getInstance().showError(ToastType.DELETED_ACCOUNT.message)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.progressBarLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = ChatRoomAdapter(AuthManager.getUid()!!)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMessages.adapter = adapter
    }

    private fun observeMessages() {
        chatViewModel.messages.observe(requireActivity()) { messageList ->
            adapter.submitList(messageList) {
                if (messageList.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messageList.size - 1)
                }
            }
        }
    }
}