package com.example.data

import com.example.data.repositories.*
import com.example.data.interfaces.*

object DataModule {
    fun initAll() {
        ChatRepository.init()
        RequestRepository.init()
        ProfileRepository.init()
        PostRepository.init()

    }
    fun provideChatRepository(): IChatRepository {
        return ChatRepository.getInstance()
    }

    fun provideRequestRepository(): IRequestRepository {
        return RequestRepository.getInstance()
    }

    fun provideProfileRepository(): IProfileRepository {
        return ProfileRepository.getInstance()
    }

    fun providePostRepository(): IPostRepository {
        return PostRepository.getInstance()
    }
}