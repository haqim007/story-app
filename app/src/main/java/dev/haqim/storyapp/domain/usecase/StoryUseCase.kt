package dev.haqim.storyapp.domain.usecase

import androidx.paging.PagingData
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface StoryUseCase {
    fun register(
        name: String, email: String, password: String
    ): Flow<Resource<BasicMessage>> 

    fun login(email: String, password: String): Flow<Resource<Login>> 

    fun getUser(): Flow<User>

    fun getStories(): Flow<PagingData<Story>> 
    
    fun getStoriesWithLocation(pageSize: Int = 30): Flow<Resource<List<Story>>>

    fun addStory(
        file: File,
        description: String,
        lon: Float? = null,
        lat: Float? = null
    ): Flow<Resource<BasicMessage>>

    suspend fun logout()
}