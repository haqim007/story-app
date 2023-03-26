package dev.haqim.storyapp.data.local

import androidx.paging.PagingSource
import app.cash.turbine.test
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.local.room.StoryDatabase
import dev.haqim.storyapp.data.remote.response.toEntity
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LocalDataSourceTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var database: StoryDatabase
    private val storyDao = FakeStoryDao()
    private val remoteKeysDao = FakeRemoteKeysDao()
    private lateinit var localDataSource: LocalDataSource
    
    @Before
    fun setup(){
        `when`(database.storyDao()).thenReturn(storyDao)
        `when`(database.remoteKeysDao()).thenReturn(remoteKeysDao)
        
        localDataSource = LocalDataSource.getInstance(database)
    }

    @Test
    fun clearRemoteKeys() = runTest{
        // Given - insert remote keys
        localDataSource.insertRemoteKeys(DataDummy.remoteKeys())
        assertEquals(DataDummy.remoteKeys()[0], localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id))
        
//         Assert
        localDataSource.clearRemoteKeys()
        assertNull(localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id))
        
    }

    @Test
    fun insertRemoteKeys() = runTest{
        localDataSource.insertRemoteKeys(DataDummy.remoteKeys())
        assertEquals(DataDummy.remoteKeys()[0], localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id))
    }

    @Test
    fun `When getRemoteKeysById() Should return not null`() = runTest{
        // Given - insert remote keys
        localDataSource.insertRemoteKeys(DataDummy.remoteKeys())
        assertEquals(DataDummy.remoteKeys()[0], localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id))
        
        val getById = localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id)
        assertNotNull(getById)
        assertEquals(DataDummy.remoteKeys()[0], getById)
    }

    @Test
    fun `When getRemoteKeysById() Should return null`() = runTest{

        val getById = localDataSource.getRemoteKeysById(DataDummy.remoteKeys()[0].id)
        assertNull(getById)
    }

    @Test
    fun getAllStories() = runTest{
        //Given - insert stories
        val sampleStories = DataDummy.listStoryResponse().toEntity()
        localDataSource.insertAllStories(sampleStories)
        
        //Assert
        val actual = localDataSource.getAllStories().load(
            PagingSource.LoadParams.Refresh(
                key = null, loadSize = 4, placeholdersEnabled = false
            )
        )
        assertEquals(sampleStories, (actual as PagingSource.LoadResult.Page).data)
        
    }

    @Test
    fun getAllStoriesWithLocation() = runTest{
        //Given - insert stories
        val sampleStories = DataDummy.listStoryResponse().toEntity()
        localDataSource.insertAllStories(sampleStories)
        
        //Assert
        localDataSource.getAllStoriesWithLocation().test { 
            val actual = awaitItem()
            val expected = sampleStories.filter { it.lat != null && it.lon != null }
            
            assertEquals(expected.size, actual.size)
            assertEquals(expected[0], actual[0])
            
            awaitComplete()
        }
    }

    @Test
    fun getStoryById()= runTest {
        //Given - insert stories
        val sampleStories = DataDummy.listStoryResponse().toEntity()
        localDataSource.insertAllStories(sampleStories)

        //Assert
        
        val actual = localDataSource.getStoryById(DataDummy.listStoryResponse()[0].id)
        val expected = DataDummy.listStoryResponse()[0].toEntity()

        assertEquals(expected, actual)
        assertEquals(expected.id, actual?.id)
    }

    @Test
    fun clearAllStories() = runTest {
        //Given - insert stories
        val sampleStories = DataDummy.listStoryResponse().toEntity()
        localDataSource.insertAllStories(sampleStories)

        //Assert
        localDataSource.clearAllStories()
        val getById = localDataSource.getStoryById(DataDummy.listStoryResponse()[0].id)
        assertNull(getById)
        val allStories =  localDataSource.getAllStories().load(
            PagingSource.LoadParams.Refresh(
                key = null, loadSize = 4, placeholdersEnabled = false
            )
        )
        assertTrue((allStories as PagingSource.LoadResult.Page).data.isEmpty())
    }

    @Test
    fun insertAllStories() = runTest {
        //Given - check stories that should be empty at first
        val firstStories =  loadResultStories()
        assertTrue((firstStories as PagingSource.LoadResult.Page).data.isEmpty())
        
        //Assert
        val sampleStories = DataDummy.listStoryResponse().toEntity()
        localDataSource.insertAllStories(sampleStories)
        val afterInsertStories = loadResultStories()
        val actual = (afterInsertStories as PagingSource.LoadResult.Page).data
        assertTrue(actual.isNotEmpty())
        assertEquals(sampleStories, actual)
    }
    
    private suspend fun loadResultStories(): PagingSource.LoadResult<Int, StoryEntity> {
        return localDataSource.getAllStories().load(
            PagingSource.LoadParams.Refresh(
                key = null, loadSize = 4, placeholdersEnabled = false
            )
        )
    }
}