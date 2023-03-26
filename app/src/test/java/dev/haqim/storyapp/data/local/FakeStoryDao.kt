package dev.haqim.storyapp.data.local

import androidx.paging.PagingSource
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.local.room.StoryDao
import dev.haqim.storyapp.data.repository.MemoryPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeStoryDao : StoryDao {
    private var stories = mutableListOf<StoryEntity>()

    override suspend fun insertAll(stories: List<StoryEntity>) {
        this.stories.addAll(stories)
    }

    override fun getAllStoriesPaging(): PagingSource<Int, StoryEntity> {
        return MemoryPagingSource(stories)
    }

    override fun getAllStories(): Flow<List<StoryEntity>> {
        return flowOf(this.stories)
    }

    override fun getAllStoriesWithLocation(): Flow<List<StoryEntity>> {
        return flowOf(
            this.stories.filter { it.lat != null && it.lon != null }
        )
    }

    override suspend fun getStoryById(id: String): StoryEntity? {
        return this.stories.find { it.id == id }
    }

    override suspend fun clearStory() {
        this.stories.clear()
    }

}