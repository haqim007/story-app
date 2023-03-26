package dev.haqim.storyapp.ui.main

import androidx.paging.*
import app.cash.turbine.test
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.DataDummy.toModel
import dev.haqim.storyapp.util.MainCoroutineRule
import dev.haqim.storyapp.util.noopListUpdateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyUseCase: StoryUseCase
    private lateinit var viewModel: MainViewModel
    

    private fun sutWhenUserHasNotLogin(): MainViewModel {
        `when`(storyUseCase.getUser()).thenReturn(
            flowOf(DataDummy.user(false))
        )

        return MainViewModel(storyUseCase)
    }

    private fun sutWhenUserHasLogin(emptyStories: Boolean = false): MainViewModel {
        val userFlow = MutableStateFlow<User>(DataDummy.user())
        `when`(storyUseCase.getUser())
            .thenReturn(
                userFlow
            )
        
        val expected = DataDummy.pagingDataStoriesFlow(emptyStories)
        `when`(storyUseCase.getStories()).thenReturn(expected)
        runTest {
            `when`(storyUseCase.logout()).then {
                userFlow.update{
                    DataDummy.user(false)
                }
            }
        }

        return MainViewModel(storyUseCase)
    }
    

    @Test
    fun `When init Expect to update userData state that has not login yet`() = runTest{
        viewModel = sutWhenUserHasNotLogin()

        viewModel.uiState.test {
            assertEquals(DataDummy.user(false), awaitItem().userData)
        }
    }
    
    @Test
    fun `When init Should invoke action GetUserData Expect to trigger getUser() in StoryUseCase`() = 
        runTest{
            viewModel = sutWhenUserHasLogin()
            verify(storyUseCase).getUser()
        }

    @Test
    fun `When init Expect to update userData state`() = runTest{
        viewModel = sutWhenUserHasLogin()
        viewModel.uiState.test { 
            assertEquals(DataDummy.user(), awaitItem().userData)
            cancelAndIgnoreRemainingEvents()
        }
    }
    
    @Test
    fun `When init Should call getStories() from StoryUseCase Expect return not empty`() =
        runTest{
            viewModel = sutWhenUserHasLogin()
            
            val expected = DataDummy.listStoryResponse().toModel()

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )
            
            viewModel.pagingDataFlow.test {

                differ.submitData(awaitItem())
                
                assertNotNull(differ.snapshot())
                assertEquals(expected.size, differ.snapshot().size)
                assertEquals(expected[0], differ.snapshot()[0])
                cancelAndIgnoreRemainingEvents()
                
                verify(storyUseCase).getStories()
            }
        }

    @Test
    fun `When init Should call getStories() from StoryUseCase Expect return empty`() =
        runTest{
            viewModel = sutWhenUserHasLogin(true)

            val differ = AsyncPagingDataDiffer(
                diffCallback = StoryAdapter.DIFF_CALLBACK,
                updateCallback = noopListUpdateCallback,
                workerDispatcher = Dispatchers.Main,
            )

            viewModel.pagingDataFlow.test {

                differ.submitData(awaitItem())

                assertNotNull(differ.snapshot())
                assertEquals(0, differ.snapshot().size)
                cancelAndIgnoreRemainingEvents()
                verify(storyUseCase).getStories()
            }
        }

    @Test
    fun `When processAction() NavigateToDetailStory Expect storyToBeOpened state to be updated`() = runTest{
        viewModel = sutWhenUserHasLogin()
        val expect = DataDummy.stories()[0]
        
        viewModel.processAction(MainUiAction.NavigateToDetailStory(expect))
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expect, state.storyToBeOpened.story)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() FinishNavigateToDetailStory Expect storyToBeOpened state to be reset`() = runTest{
        viewModel = sutWhenUserHasLogin()
        val expect = null

        viewModel.processAction(MainUiAction.FinishNavigateToDetailStory)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expect, state.storyToBeOpened.story)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToAddStory state is false When processAction() NavigateToAddStory Expect set the state to true`() = runTest{
        viewModel = sutWhenUserHasLogin()
        val expect = true

        viewModel.processAction(MainUiAction.NavigateToAddStory)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expect, state.navigateToAddStory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToAddStory state is true When processAction() NavigateToAddStory Expect set the state to false`() = runTest{
        viewModel = sutWhenUserHasLogin()
        val expect = false

        // given - set state to true
        viewModel.processAction(MainUiAction.NavigateToAddStory)

        // when - set state to false
        viewModel.processAction(MainUiAction.NavigateToAddStory)

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expect, state.navigateToAddStory)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() Logout Should call StoryUseCase Logout()`() = runTest{
        viewModel = sutWhenUserHasLogin()

        viewModel.processAction(MainUiAction.Logout)
        
        verify(storyUseCase).logout()
    }

    @Test
    fun `Given user has login When processAction() Logout Expect userData state to be null`() = runTest{
        viewModel = sutWhenUserHasLogin()

        viewModel.processAction(MainUiAction.Logout)

        viewModel.uiState.test { 
            val state = awaitItem()
            assertEquals("", state.userData?.token)
        }
    }
    
}