package dev.haqim.storyapp.data.local.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.local.entity.TABLE_STORIES
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<StoryEntity>)
    
    @Query("SELECT * FROM $TABLE_STORIES")
    fun getAllStoriesPaging(): PagingSource<Int, StoryEntity>

    @Query("SELECT * FROM $TABLE_STORIES")
    fun getAllStories(): Flow<List<StoryEntity>>

    @Query("SELECT * FROM $TABLE_STORIES WHERE lon IS NOT NULL AND lat IS NOT NULL")
    fun getAllStoriesWithLocation(): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM $TABLE_STORIES where id = :id")
    suspend fun getStoryById(id: String): StoryEntity?
    
    @Query("DELETE FROM $TABLE_STORIES")
    suspend fun clearStory()
}