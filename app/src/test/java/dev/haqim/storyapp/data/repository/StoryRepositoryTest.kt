package dev.haqim.storyapp.data.repository

import androidx.paging.*
import app.cash.turbine.test
import dev.haqim.storyapp.data.local.LocalDataSource
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.response.toEntity
import dev.haqim.storyapp.data.remote.response.toModel
import dev.haqim.storyapp.data.remoteMediator.StoryRemoteMediator
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.DataDummy.toModel
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StoryRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var remoteDataSource: RemoteDataSource
    @Mock
    private lateinit var userPreference: UserPreference
    @Mock
    private lateinit var localDataSource: LocalDataSource
    @Mock
    private lateinit var remoteMediator: StoryRemoteMediator
    
    private lateinit var repository: StoryRepository
    
    fun sutSuccess(
        name:String = "",
        email: String = "",
        password: String = ""
    ): StoryRepository {
        
        `when`(remoteDataSource.register(name, email, password)).thenReturn(
            flow {
                emit(Result.success(DataDummy.basicResponseSuccess()))
            }
        )

        `when`(remoteDataSource.login(email, password)).thenReturn(
            flow {
                emit(Result.success(DataDummy.loginResponse()))
            }
        )

        `when`(userPreference.getUser()).thenReturn(
            flowOf(DataDummy.user())
        )

        `when`(localDataSource.getAllStoriesWithLocation()).thenReturn(
            flowOf(DataDummy.listStoryResponse().toEntity())
        )

        runTest {
            `when`(localDataSource.insertAllStories(DataDummy.listStoryResponse().toEntity()))
                .thenReturn(Unit)
        }
        
        `when`(remoteDataSource.getStories(1, 4, 1)).thenReturn(
            flowOf(Result.success(DataDummy.storiesResponse()))
        )
        
        return StoryRepository(remoteDataSource, userPreference, localDataSource, remoteMediator)
    }

    fun sutError(
        name:String = "",
        email: String = "",
        password: String = ""
    ): StoryRepository {
        
        `when`(remoteDataSource.register(name, email, password)).thenReturn(
            flow {
                emit(Result.failure(DataDummy.basicResponseError()))
            }
        )

        `when`(remoteDataSource.login(email, password)).thenReturn(
            flow {
                emit(Result.failure(DataDummy.basicResponseError()))
            }
        )

        `when`(remoteDataSource.getStories(1, 4, 1)).thenReturn(
            flowOf(Result.failure(DataDummy.basicResponseError()))
        )

        return StoryRepository(remoteDataSource, userPreference, localDataSource, remoteMediator)
    }


    
    @Test
    fun `When register() Should return Loading and Success`() = runTest{
        val email = "malih@mail.com"
        val name = "malih"
        val password = "!2345678"

        repository = sutSuccess(name, email, password)
        

        repository.register(name, email, password).test {
            verify(remoteDataSource).register(name, email, password)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionSuccess = awaitItem()
            assertTrue(emissionSuccess is Resource.Success)
            assertEquals(DataDummy.basicMessageSuccess(), emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }
        

    }

    @Test
    fun `When register() Should return Loading and Error`() = runTest{
        val email = "haqim@mail.com"
        val name = "haqim"
        val password = "!2345678"
        val expectedErrorMsg = "Ini error"

        repository = sutError(name, email, password)

        repository.register(name, email, password).test {
            verify(remoteDataSource).register(name, email, password)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionError = awaitItem()
            assertTrue(emissionError is Resource.Error)
            assertEquals(DataDummy.basicResponseError().message, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When login() Should return Loading and Success`() = runTest{
        val email = "haqim@mail.com"
        val password = "!2345678"
        
        repository = sutSuccess("", email, password)

        repository.login(email, password).test {
            verify(remoteDataSource).login(email, password)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionSuccess = awaitItem()
            assertTrue(emissionSuccess is Resource.Success)
            assertEquals(DataDummy.loginResponse().toModel(), emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When login() Should return Loading and Error`() = runTest{
        val email = "haqim@mail.com"
        val password = "!2345678"
        
        repository = sutError("", email, password)

        repository.login(email, password).test {
            verify(remoteDataSource).login(email, password)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionError = awaitItem()
            assertTrue(emissionError is Resource.Error)
            assertEquals(DataDummy.basicResponseError().message, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun getUser() = runTest{
        repository = sutSuccess("", "", "")
        repository.getUser().test {
            verify(userPreference).getUser()
            val emission = awaitItem()

            assertEquals(DataDummy.user(), emission)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When getStoriesWithLocation() Should return Loading Then Success`() = runTest {
        
        repository = sutSuccess("", "", "")
        
        repository.getStoriesWithLocation(4, 1).test {
            // first emission is loading
            val loadingEmission = awaitItem()
            assertTrue(loadingEmission is Resource.Loading)
            verify(remoteDataSource).getStories(1, 4, 1)
            val storyEntities = DataDummy.listStoryResponse().toEntity()
            verify(localDataSource).insertAllStories(storyEntities)
            verify(localDataSource).getAllStoriesWithLocation()

            // second emission is success
            val successEmission = awaitItem()
            assertTrue(successEmission is Resource.Success)
            assertEquals(DataDummy.listStoryResponse().size, successEmission.data?.size ?: 0)
            assertEquals(DataDummy.listStoryResponse().toModel()[0], successEmission.data?.get(0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When getStoriesWithLocation() Should return Loading Then Error`() = runTest {

        repository = sutError("", "", "")

        repository.getStoriesWithLocation(4, 1).test {
            // first emission is loading
            assertTrue(awaitItem() is Resource.Loading)
            verify(remoteDataSource).getStories(1, 4, 1)

            // second emission is success
            val errorEmission = awaitItem()
            assertTrue(errorEmission is Resource.Error)
            assertNotNull(errorEmission.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When getStories() Should return Not Null PagingData_Story`() = runTest{

        // Create a MemoryPagingSource with the mock data
        val stories = DataDummy.listStoryResponse().toEntity()
        
        // Set up the mock behavior of the local data source
        `when`(localDataSource.getAllStories()).thenReturn(
            MemoryPagingSource(stories)
        )
        
        repository = StoryRepository(remoteDataSource, userPreference, localDataSource, remoteMediator)
        val actual = repository.getStories().first()
        assertNotNull(actual)
        

    }

    @Test
    fun `When addStory Should return Loading Then Success`() = runTest{

        val file = DataDummy.file(".png")
        val description = "Ini deskripsi"
        val lon = -11F
        val lat = 111F
        
        
        `when`(remoteDataSource.addStory(file, description, lon, lat))
            .thenReturn(
                flow{
                    emit(Result.success(DataDummy.basicResponseSuccess()))
                }
            )
        
        val myRepo = StoryRepository(remoteDataSource, userPreference, localDataSource, remoteMediator)
        
        myRepo.addStory(file, description, lon, lat).test { 
            assertTrue(awaitItem() is Resource.Loading)
            verify(remoteDataSource)
                .addStory(
                    file, description, lon, lat
                )
            
            val successEmission = awaitItem()
            assertTrue(successEmission is Resource.Success)
            assertEquals(DataDummy.basicMessageSuccess(), successEmission.data)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When addStory Should return Loading Then Error`() = runTest{

        val file = DataDummy.file(".png")
        val description = "Ini deskripsi"
        val lon = -11F
        val lat = 111F


        `when`(remoteDataSource.addStory(file, description, lon, lat))
            .thenReturn(
                flow{
                    emit(Result.failure(DataDummy.basicResponseError()))
                }
            )

        val myRepo = StoryRepository(remoteDataSource, userPreference, localDataSource, remoteMediator)

        myRepo.addStory(file, description, lon, lat).test {
            assertTrue(awaitItem() is Resource.Loading)
            verify(remoteDataSource)
                .addStory(
                    file, description, lon, lat
                )

            val successEmission = awaitItem()
            assertTrue(successEmission is Resource.Error)
            assertEquals(DataDummy.basicResponseError().message, successEmission.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun logout() = runTest{
        repository = sutSuccess("", "", "")
        repository.logout()

        verify(userPreference).logout()
    }
}

class MemoryPagingSource<T : Any>(
    private val data: List<T>,
    private val pageSize: Int = 10
) : PagingSource<Int, T>() {

    override fun getRefreshKey(state: PagingState<Int, T>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val pageNumber = params.key ?: 0
        val start = pageNumber * pageSize
        val end = start + pageSize

        return try {
            val items = data.subList(start.coerceAtMost(data.size), end.coerceAtMost(data.size))
            LoadResult.Page(
                data = items,
                prevKey = if (pageNumber > 0) pageNumber - 1 else null,
                nextKey = if (items.size == pageSize) pageNumber + 1 else null
            ) 
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}

