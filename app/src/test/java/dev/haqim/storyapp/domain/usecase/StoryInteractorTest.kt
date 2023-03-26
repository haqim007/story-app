package dev.haqim.storyapp.domain.usecase

import androidx.paging.AsyncPagingDataDiffer
import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.repository.IStoryRepository
import dev.haqim.storyapp.ui.main.StoryAdapter
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.DataDummy.toModel
import dev.haqim.storyapp.util.MainCoroutineRule
import dev.haqim.storyapp.util.noopListUpdateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class StoryInteractorTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyRepository: IStoryRepository
    private lateinit var storyInteractor: StoryInteractor
 
    @Test
    fun `When register() Should return Loading and Success`() = runTest{
        val email = "haqim@mail.com"
        val name = "haqim"
        val password = "!2345678"
        
        `when`(storyRepository.register(name, email, password)).thenReturn(
            flow { 
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Success(DataDummy.basicMessageSuccess()))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)
        
        storyInteractor.register(name, email, password).test {
            verify(storyRepository).register(name, email, password)
            
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

        `when`(storyRepository.register(name, email, password)).thenReturn(
            flow {
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Error<BasicMessage>(expectedErrorMsg))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.register(name, email, password).test {
            verify(storyRepository).register(name, email, password)
            
            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionError = awaitItem()
            assertTrue(emissionError is Resource.Error)
            assertEquals(expectedErrorMsg, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When login() Should return Loading and Success`() = runTest{
        val email = "haqim@mail.com"
        val password = "!2345678"

        `when`(storyRepository.login(email, password)).thenReturn(
            flow {
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Success(DataDummy.login()))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.login(email, password).test {
            verify(storyRepository).login(email, password)
            
            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionSuccess = awaitItem()
            assertTrue(emissionSuccess is Resource.Success)
            assertEquals(DataDummy.login(), emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When logic() Should return Loading and Error`() = runTest{
        val email = "haqim@mail.com"
        val password = "!2345678"
        val expectedErrorMsg = "Ini error"

        `when`(storyRepository.login(email, password)).thenReturn(
            flow {
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Error(expectedErrorMsg))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.login(email, password).test {
            verify(storyRepository).login(email, password)
            
            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionError = awaitItem()
            assertTrue(emissionError is Resource.Error)
            assertEquals(expectedErrorMsg, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun getUser() = runTest{

        `when`(storyRepository.getUser()).thenReturn(
            flowOf(DataDummy.user())
        )
        storyInteractor = StoryInteractor(storyRepository)
        storyInteractor.getUser().test {
            verify(storyRepository).getUser()    
            val emission = awaitItem()
            
            assertEquals(DataDummy.user(), emission)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When getStories() Should return PagingData_Story`() = runTest{

        `when`(storyRepository.getStories()).thenReturn(
            DataDummy.pagingDataStoriesFlow()
        )
        storyInteractor = StoryInteractor(storyRepository)

        val expected = DataDummy.listStoryResponse().toModel()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )

        storyInteractor.getStories().test {
            verify(storyRepository).getStories()
            val pagingData = awaitItem()
            differ.submitData(pagingData)

            assertNotNull(differ.snapshot())
            assertEquals(expected.size, differ.snapshot().size)
            assertEquals(expected[0], differ.snapshot()[0])
            cancelAndIgnoreRemainingEvents()

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When getStoriesWithLoacation() Should return Loading and Success`() = runTest{

        `when`(storyRepository.getStoriesWithLocation(4)).thenReturn(
            flow {
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Success(DataDummy.stories()))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.getStoriesWithLocation(4).test {
            verify(storyRepository).getStoriesWithLocation(4)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionSuccess = awaitItem()
            assertTrue(emissionSuccess is Resource.Success)
            assertEquals(DataDummy.stories(), emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When getStoriesWithLocation() Should return Loading and Error`() = runTest{
        val expectedErrorMsg = "Ini error"

        `when`(storyRepository.getStoriesWithLocation(4)).thenReturn(
            flow {
                emit(Resource.Loading())
                delay(100)
                emit(Resource.Error(expectedErrorMsg))
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.getStoriesWithLocation(4).test {
            verify(storyRepository).getStoriesWithLocation(4)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)

            val emissionError = awaitItem()
            assertTrue(emissionError is Resource.Error)
            assertEquals(expectedErrorMsg, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When addStory() without lng & lat Should return Loading and Success`() = runTest{

        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedSuccess = Resource.Success(DataDummy.basicMessageSuccess())

        `when`(storyRepository.addStory(expectedFile, expectedDescription)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedSuccess)
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.addStory(expectedFile, expectedDescription).test {
            verify(storyRepository).addStory(expectedFile, expectedDescription)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)
            assertEquals(expectedLoading, emissionLoading)

            val emissionSuccess = awaitItem()
            assertEquals(expectedSuccess, emissionSuccess)
            assertEquals(expectedSuccess.data, emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When addStory() without lng & lat Should return Loading and Error`() = runTest{
        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedErrorMsg = "Ini error"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedError = Resource.Error<BasicMessage>(expectedErrorMsg)

        `when`(storyRepository.addStory(expectedFile, expectedDescription)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedError)
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.addStory(expectedFile, expectedDescription).test {
            verify(storyRepository).addStory(expectedFile, expectedDescription)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)
            assertEquals(expectedLoading, emissionLoading)

            val emissionError = awaitItem()
            assertEquals(expectedError, emissionError)
            assertEquals(expectedErrorMsg, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When addStory() with lng & lat Should return Loading and Success`() = runTest{

        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedLng = -111F
        val expectedLat = 111F
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedSuccess = Resource.Success(DataDummy.basicMessageSuccess())

        `when`(storyRepository.addStory(expectedFile, expectedDescription, expectedLng, expectedLat)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedSuccess)
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.addStory(expectedFile, expectedDescription, expectedLng, expectedLat).test {
            verify(storyRepository).addStory(expectedFile, expectedDescription, expectedLng, expectedLat)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)
            assertEquals(expectedLoading, emissionLoading)

            val emissionSuccess = awaitItem()
            assertTrue(emissionSuccess is Resource.Success)
            assertEquals(expectedSuccess, emissionSuccess)
            assertEquals(expectedSuccess.data, emissionSuccess.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When addStory() with lng & lat Should return Loading and Error`() = runTest{
        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedLng = -111F
        val expectedLat = 111F
        val expectedErrorMsg = "Ini error"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedError = Resource.Error<BasicMessage>(expectedErrorMsg)

        `when`(storyRepository.addStory(expectedFile, expectedDescription, expectedLng, expectedLat)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedError)
            }
        )
        storyInteractor = StoryInteractor(storyRepository)

        storyInteractor.addStory(expectedFile, expectedDescription, expectedLng, expectedLat).test {
            verify(storyRepository).addStory(expectedFile, expectedDescription, expectedLng, expectedLat)

            val emissionLoading = awaitItem()
            assertTrue(emissionLoading is Resource.Loading)
            assertEquals(expectedLoading, emissionLoading)

            val emissionError = awaitItem()
            assertEquals(expectedError, emissionError)
            assertEquals(expectedErrorMsg, emissionError.message)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun logout() = runTest{
        
        val storyInteractor = StoryInteractor(storyRepository)
        storyInteractor.logout()
        
        verify(storyRepository).logout()
    }
}