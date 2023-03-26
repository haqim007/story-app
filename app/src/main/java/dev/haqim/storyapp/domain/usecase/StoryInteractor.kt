package dev.haqim.storyapp.domain.usecase

import androidx.paging.PagingData
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.domain.repository.IStoryRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

class StoryInteractor(
    private val storyRepository: IStoryRepository
): StoryUseCase {
    override fun register(
        name: String,
        email: String,
        password: String,
    ): Flow<Resource<BasicMessage>> {
        return storyRepository.register(name, email, password)
    }

    override fun login(email: String, password: String): Flow<Resource<Login>> {
        return storyRepository.login(email, password)
    }

    override fun getUser(): Flow<User> {
        return storyRepository.getUser()
    }

    override fun getStories(): Flow<PagingData<Story>> {
        return storyRepository.getStories()
    }

    override fun getStoriesWithLocation(pageSize: Int): Flow<Resource<List<Story>>> {
        return storyRepository.getStoriesWithLocation(pageSize)
    }

    override fun addStory(
        file: File,
        description: String,
        lon: Float?,
        lat: Float?,
    ): Flow<Resource<BasicMessage>> {
        return storyRepository.addStory(file, description, lon, lat)
    }

    override suspend fun logout() {
        storyRepository.logout()
    }
}