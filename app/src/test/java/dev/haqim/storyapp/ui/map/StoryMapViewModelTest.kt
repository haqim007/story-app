package dev.haqim.storyapp.ui.map

import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class StoryMapViewModelTest{

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyUseCase: StoryUseCase
    private lateinit var viewModel: StoryMapViewModel

    private fun sutSuccess(): StoryMapViewModel {
        `when`(storyUseCase.getStoriesWithLocation()).thenReturn(
            flowOf(Resource.Success(DataDummy.stories()))
        )

        return StoryMapViewModel(storyUseCase)
    }

    private fun sutLoading(): StoryMapViewModel {
        `when`(storyUseCase.getStoriesWithLocation()).thenReturn(
            flowOf(Resource.Loading())
        )

        return StoryMapViewModel(storyUseCase)
    }
    
    private val errorMessage = "Error"
    private fun sutError(): StoryMapViewModel {
        `when`(storyUseCase.getStoriesWithLocation()).thenReturn(
            flowOf(Resource.Error(errorMessage))
        )

        return StoryMapViewModel(storyUseCase)
    }

    private fun sut(): StoryMapViewModel {
        return StoryMapViewModel(storyUseCase)
    }
    
    
    @Test
    fun `When init Should get stories And return Resource_Loading`() = runTest{
        viewModel = sutLoading()
        viewModel.uiState.test { 
            val state = awaitItem()
            
            verify(storyUseCase).getStoriesWithLocation()
            assertTrue(state.stories is Resource.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When init Should get stories And return Resource_Success`() = runTest{
        viewModel = sutSuccess()
        val expected = DataDummy.stories()
        viewModel.uiState.test {
            val state = awaitItem()

            verify(storyUseCase).getStoriesWithLocation()
            assertTrue(state.stories is Resource.Success)
            assertEquals(expected.size, state.stories.data?.size ?: 0)
            assertEquals(expected[0], state.stories.data?.get(0))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When init Should get stories And Return Resource_Error`() = runTest{
        viewModel = sutError()
        
        viewModel.uiState.test {
            val state = awaitItem()

            verify(storyUseCase).getStoriesWithLocation()
            assertTrue(state.stories is Resource.Error)
            assertEquals(errorMessage, state.stories.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() MapUiAction_NavigateToDetailStory Should update storyToBeOpened state And not null`() = runTest{
        viewModel = sut()

        val expected = DataDummy.stories()[0]
        viewModel.processAction(MapUiAction.NavigateToDetailStory(expected))
        
        viewModel.uiState.test {
            val actual = awaitItem().storyToBeOpened

            assertEquals(expected, actual)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() MapUiAction_FinishNavigateToDetailStory Should update storyToBeOpened state to null`() = runTest{
        viewModel = sut()

        viewModel.processAction(MapUiAction.FinishNavigateToDetailStory)

        viewModel.uiState.test {
            val actual = awaitItem().storyToBeOpened

            assertNull(actual)
            cancelAndIgnoreRemainingEvents()
        }
    }
}