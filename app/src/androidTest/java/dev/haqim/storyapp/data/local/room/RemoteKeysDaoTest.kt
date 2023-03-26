package dev.haqim.storyapp.data.local.room

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.MediumTest
import dev.haqim.storyapp.util.DataDummy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
@MediumTest
class RemoteKeysDaoTest{
    
    private lateinit var database: StoryDatabase
    private lateinit var dao: RemoteKeysDao
    private val keys = DataDummy.remoteKeys()
    
    @Before
    fun setup(){
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoryDatabase::class.java
        ).build()
        dao = database.remoteKeysDao()
    }
    
    @After
    fun tearDown(){
        database.close()
    }
    
    @Test
    fun save_stories() = runTest { 
        dao.insertAll(keys)
        val insertedFirstKey = dao.getRemoteKeyById(keys[0].id)
        assertEquals(insertedFirstKey, keys[0])
    }
    
    @Test
    fun clear_stories() = runTest {
        dao.insertAll(keys)
        val insertedFirstKey = dao.getRemoteKeyById(keys[0].id)
        assertEquals(insertedFirstKey, keys[0])
        dao.clearRemoteKeys()
        val afterClearKeys = dao.getRemoteKeyById(keys[0].id)
        assertNull(afterClearKeys)
    }
    
}