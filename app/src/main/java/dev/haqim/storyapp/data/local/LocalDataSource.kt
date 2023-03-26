package dev.haqim.storyapp.data.local

import androidx.room.withTransaction
import dev.haqim.storyapp.data.local.entity.RemoteKeys
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.local.room.StoryDatabase

class LocalDataSource private constructor(
    private val database: StoryDatabase
) {
    
    private val remoteKeysDao = database.remoteKeysDao()
    private val storyDao = database.storyDao()
    
    suspend fun clearRemoteKeys() = remoteKeysDao.clearRemoteKeys()
    
    suspend fun insertRemoteKeys(keys: List<RemoteKeys>) = remoteKeysDao.insertAll(keys)
    
    suspend fun getRemoteKeysById(id: String) = remoteKeysDao.getRemoteKeyById(id)
    
    fun getAllStories() = storyDao.getAllStoriesPaging()

    fun getAllStoriesWithLocation() = storyDao.getAllStoriesWithLocation()
    
    suspend fun getStoryById(id: String) = storyDao.getStoryById(id)
    
    suspend fun clearAllStories() = storyDao.clearStory()
    
    suspend fun insertAllStories(stories: List<StoryEntity>) = storyDao.insertAll(stories)
    
    suspend fun insertKeysAndStories(
        keys: List<RemoteKeys>, 
        stories: List<StoryEntity>, 
        isRefresh: Boolean = false
    ){
        database.withTransaction {
            if (isRefresh) {
                clearRemoteKeys()
                clearAllStories()
            }
            insertRemoteKeys(keys)
            insertAllStories(stories)
        }
    }
    
    suspend fun withTransaction(block: suspend () -> Unit){
        database.withTransaction { 
            block()
        }
    }
    
    companion object{
        private var INSTANCE: LocalDataSource? = null
        
        fun getInstance(database: StoryDatabase) = INSTANCE ?: synchronized(this){
            INSTANCE ?: LocalDataSource(database)
        }
    }
}