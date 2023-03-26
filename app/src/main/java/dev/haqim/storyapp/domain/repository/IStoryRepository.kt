package dev.haqim.storyapp.domain.repository

import androidx.paging.PagingData
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface IStoryRepository{
    fun register(
        name: String, email: String, password: String
    ): Flow<Resource<BasicMessage>>

    fun login(email: String, password: String): Flow<Resource<Login>>

    fun getUser(): Flow<User>

    fun getStoriesWithLocation(
        pageSize: Int, page: Int = 1
    ): Flow<Resource<List<Story>>>
    
    fun getStories(): Flow<PagingData<Story>>

    fun addStory(
        file: File,
        description: String,
        lon: Float? = null,
        lat: Float? = null
    ): Flow<Resource<BasicMessage>>

    suspend fun logout()
}