package com.example.aidhub.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aidhub.viewModels.ChatViewModel
import com.example.aidhub.viewModels.PostViewModel
import com.example.aidhub.viewModels.ProfileViewModel
import com.example.aidhub.viewModels.RequestViewModel
import com.example.data.DataModule

class MyViewModelFactory() :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        val chatRepository = DataModule.provideChatRepository()
        val requestRepository = DataModule.provideRequestRepository()
        val profileRepository = DataModule.provideProfileRepository()
        val postRepository = DataModule.providePostRepository()


        return when {
            modelClass.isAssignableFrom(RequestViewModel::class.java) -> {
                RequestViewModel(requestRepository) as T
            }

            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                PostViewModel(postRepository) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(profileRepository, requestRepository) as T
            }

            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(chatRepository, profileRepository) as T
            }


            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}