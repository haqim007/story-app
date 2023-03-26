package dev.haqim.storyapp.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import dev.haqim.storyapp.util.DataDummy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class StoryDaoTest{
    
    private lateinit var database: StoryDatabase
    private lateinit var dao: StoryDao
    private val stories = DataDummy.storiesEntity()
    
    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoryDatabase::class.java
        ).build()
        dao = database.storyDao()
    }
    
    @After
    fun tearDown(){
        database.close()
    }
    
    @Test
    fun save_stories() = runTest { 
        dao.insertAll(stories)
        val insertedStories = dao.getAllStories().first()
        assertEquals(stories.size, insertedStories.size)
    }
    
    @Test
    fun clear_stories() = runTest {
        dao.insertAll(stories)
        val insertedStories = dao.getAllStories().first()
        assertEquals(stories.size, insertedStories.size)
        dao.clearStory()
        val afterClear = dao.getAllStories().first()
        assertTrue(afterClear.isEmpty())
    }
    
}