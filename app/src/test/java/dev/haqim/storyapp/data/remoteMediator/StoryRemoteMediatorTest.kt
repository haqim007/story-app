package dev.haqim.storyapp.data.remoteMediator

import androidx.paging.*
import dev.haqim.storyapp.data.local.LocalDataSource
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.response.StoriesResponse
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
class StoryRemoteMediatorTest {
    
    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var localDataSource: LocalDataSource
    @Mock
    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var storyRemoteMediator: StoryRemoteMediator

    @Before
    fun setup() {
        storyRemoteMediator = StoryRemoteMediator(localDataSource, remoteDataSource)
    }

    
    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun `When Load() with response data not empty Should return Success And EndOfPagination false`() = runTest {
        val pagingState = PagingState<Int, StoryEntity>(
            listOf(),
            null,
            PagingConfig(pageSize = 4),
            0
        )

        val response = Result.success(DataDummy.storiesResponse())
        val flow = flowOf(response)
        `when`(remoteDataSource.getStories(1, 4)).thenReturn(flow)
        
        val result = storyRemoteMediator.load(LoadType.REFRESH, pagingState)

        verify(remoteDataSource).getStories(1, 4)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun `When Load() with response data empty Should return Success And EndOfPagination true`() = runTest {
        val pagingState = PagingState<Int, StoryEntity>(
            listOf(),
            null,
            PagingConfig(pageSize = 4),
            0
        )

        val response = Result.success(
            StoriesResponse(
                listStory = listOf(),
                error = false,
                message = ""
            )
        )
        val flow = flowOf(response)
        `when`(remoteDataSource.getStories(1, 4)).thenReturn(flow)

        val result = storyRemoteMediator.load(LoadType.REFRESH, pagingState)

        verify(remoteDataSource).getStories(1, 4)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @OptIn(ExperimentalPagingApi::class)
    @Test
    fun `When Load() Should return Error And EndOfPagination true`() = runTest {
        val pagingState = PagingState<Int, StoryEntity>(
            listOf(),
            null,
            PagingConfig(pageSize = 4),
            0
        )

        val exception = RuntimeException("Something went wrong!")
        val flow = flow<Result<StoriesResponse>> { throw exception }
        `when`(remoteDataSource.getStories(1, 4)).thenReturn(flow)
        
        val myStoryRemoteMediator = StoryRemoteMediator(localDataSource, remoteDataSource)
        

        val result = myStoryRemoteMediator.load(LoadType.REFRESH, pagingState)

        verify(remoteDataSource).getStories(1, 4)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        assertEquals("Something went wrong!", (result as RemoteMediator.MediatorResult.Error).throwable.localizedMessage)
    }
}