package dev.haqim.storyapp.ui.add_story

import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.helper.util.InputValidation
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AddStoryViewModelTest{
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyUseCase: StoryUseCase
    private lateinit var viewModel: AddStoryViewModel
    
    @Before
    fun setup(){
        `when`(storyUseCase.getUser()).thenReturn(
            flowOf(DataDummy.user())
        )
        viewModel = AddStoryViewModel(storyUseCase)
    }
    
    @Test
    fun `When init Should do GetUserData And update userData state`() = runTest { 
        viewModel.uiState.test { 
            val state = awaitItem()
            assertEquals(DataDummy.user(), state.userData)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToStories state is false When NavigateToStories Should navigateToStories state to true`() = runTest{

        //When: set navigateToStories to false
        viewModel.processAction(AddStoryUiAction.NavigateToStories)

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.navigateToStories)

            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `Given navigateToStories state is true When NavigateToStories Should navigateToStories state to false`() = runTest{

        //Given: set navigateToStories to true
        viewModel.processAction(AddStoryUiAction.NavigateToStories)

        //When: set navigateToStories to false
        viewModel.processAction(AddStoryUiAction.NavigateToStories)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.navigateToStories)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When OpenCamera Should openCamera state to true`() = runTest{
        
        //When: set navigateToStories to false
        viewModel.processAction(AddStoryUiAction.OpenCamera)

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.openCamera)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given openCamera state is true When CloseCamera Should openCamera state to false`() = runTest{

        //Given: set openCamera to true
        viewModel.processAction(AddStoryUiAction.OpenCamera)
        
        //When: set openCamera to false
        viewModel.processAction(AddStoryUiAction.CloseCamera)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.openCamera)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given openGallery state is true When CloseGallery Should openGallery state to false`() = runTest{

        //Given: set openGallery to true
        viewModel.processAction(AddStoryUiAction.OpenGallery)
        
        //When: set openGallery to false
        viewModel.processAction(AddStoryUiAction.CloseGallery)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.openGallery)

            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `When SetFile should update file state and allInputValid state to false`() = runTest {
        val dummyFile = DataDummy.file()    
        
        viewModel.processAction(AddStoryUiAction.SetFile(dummyFile))
        
        viewModel.uiState.test { 
            val state = awaitItem()
            
            assertEquals(dummyFile, state.file)
            assertFalse(state.allInputValid)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When ShareLocation should update lon, lat state and allInputValid state still false`() = runTest {
        viewModel.processAction(AddStoryUiAction.ShareLocation(true, 11.0, 12.0))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.shareLocation)
            assertEquals(11.0, state.lon)
            assertEquals(12.0, state.lat)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When SetDescription should update description state and allInputValid state to false`() = runTest {
        val expectedDescription = "ini deskripsi"

        viewModel.processAction(AddStoryUiAction.SetDescription(expectedDescription))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.description.validation == InputValidation.Valid)
            assertEquals(expectedDescription, state.description.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When UploadStory Should return Loading then Success`() = runTest{
        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedSuccess = Resource.Success(DataDummy.basicMessageSuccess())

        `when`(storyUseCase.addStory(expectedFile, expectedDescription)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedSuccess)
            }
        )

        val myViewModel = AddStoryViewModel(storyUseCase)

        myViewModel.processAction(AddStoryUiAction.SetFile(expectedFile))
        myViewModel.processAction(AddStoryUiAction.SetDescription(expectedDescription))
        myViewModel.processAction(AddStoryUiAction.UploadStory)

        myViewModel.uiState.test {
            val loadingEmission = awaitItem()

            assertEquals(expectedLoading, loadingEmission.uploadResult)

            val successEmission = awaitItem()

            assertEquals(expectedSuccess, successEmission.uploadResult)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When UploadStory Should return Loading then Error`() = runTest{
        val expectedFile = DataDummy.file()
        val expectedDescription = "ini deskripsi"
        val expectedErrorMsg = "Ini error"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedError = Resource.Error<BasicMessage>(expectedErrorMsg)

        `when`(storyUseCase.addStory(expectedFile, expectedDescription)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedError)
            }
        )

        val myViewModel = AddStoryViewModel(storyUseCase)

        myViewModel.processAction(AddStoryUiAction.SetFile(expectedFile))
        myViewModel.processAction(AddStoryUiAction.SetDescription(expectedDescription))
        myViewModel.processAction(AddStoryUiAction.UploadStory)

        myViewModel.uiState.test {
            val loadingEmission = awaitItem()

            assertEquals(expectedLoading, loadingEmission.uploadResult)

            val errorEmission = awaitItem()

            assertEquals(expectedError, errorEmission.uploadResult)

            cancelAndIgnoreRemainingEvents()
        }
    }
}