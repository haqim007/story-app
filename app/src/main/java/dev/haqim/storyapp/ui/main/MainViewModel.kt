package dev.haqim.storyapp.ui.main

import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val storyUseCase: StoryUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val pagingDataFlow: Flow<PagingData<Story>>
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, MainUiState()
    )
    private val actionStateFlow = MutableSharedFlow<MainUiAction>(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)
        processAction(MainUiAction.GetUserData)
        
        val getStoriesAction = actionStateFlow
            .filterIsInstance<MainUiAction.GetStories>()
            .distinctUntilChanged()
            .onStart { 
                emit(MainUiAction.GetStories)
            }
        
        /*
        * .flatMapLatest trigger to start collecting upstream flow immediately
        * */
        pagingDataFlow = getStoriesAction
            .flatMapLatest { onGetStories() }
            .cachedIn(viewModelScope)
    }

    fun processAction(action: MainUiAction) = actionStateFlow.tryEmit(action)

    private fun MutableSharedFlow<MainUiAction>.updateStates() = onEach {
        when(it){
            is MainUiAction.GetUserData -> {
                viewModelScope.launch {
                    storyUseCase.getUser().collectLatest {user ->
                        _uiState.update { state ->
                            state.copy(userData = user)
                        }
                    }
                }
            }
            is MainUiAction.NavigateToAddStory -> {
                _uiState.update { state ->
                    state.copy(
                        navigateToAddStory = !state.navigateToAddStory
                    )
                }
            }
            is MainUiAction.Logout -> {
                storyUseCase.logout()
            }
            is MainUiAction.NavigateToDetailStory -> {
                viewModelScope.launch { 
                    _uiState.update {state ->
                        state.copy(
                            storyToBeOpened = StoryToBeOpened(
                                story = it.story,
                                optionsCompat = it.optionsCompat
                            )
                        )
                    }
                }
            }
            is MainUiAction.FinishNavigateToDetailStory ->{
                viewModelScope.launch {
                    _uiState.update {state ->
                        state.copy(
                            storyToBeOpened = StoryToBeOpened()
                        )
                    }
                }
            }
            else -> {}
        }
    }
    
    private fun onGetStories(): Flow<PagingData<Story>> {
        return storyUseCase.getStories()
    }
}

data class MainUiState(
    val userData: User? = null,
    val navigateToAddStory: Boolean = false,
    val storyToBeOpened: StoryToBeOpened = StoryToBeOpened()
)

data class StoryToBeOpened(
    val story: Story? = null,
    val optionsCompat: ActivityOptionsCompat? = null
)

sealed class MainUiAction{
    object GetUserData: MainUiAction()
    object GetStories: MainUiAction()
    object NavigateToAddStory: MainUiAction()
    object Logout: MainUiAction()
    data class NavigateToDetailStory(
        val story: Story, 
        val optionsCompat: ActivityOptionsCompat? = null
    ): MainUiAction()
    object FinishNavigateToDetailStory: MainUiAction()
}

