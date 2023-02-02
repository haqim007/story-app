package dev.haqim.storyapp.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.data.repository.StoryRepository
import dev.haqim.storyapp.model.Story
import dev.haqim.storyapp.model.User
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, MainUiState()
    )
    private val actionStateFlow = MutableSharedFlow<MainUiAction>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)
        processAction(MainUiAction.GetUserData)
    }

    fun processAction(action: MainUiAction) = actionStateFlow.tryEmit(action)

    private fun MutableSharedFlow<MainUiAction>.updateStates() = onEach {
        when(it){
            is MainUiAction.GetUserData -> {
                viewModelScope.launch {
                    repository.getUser().collectLatest {user ->
                        _uiState.update { state ->
                            state.copy(userData = user)
                        }
                    }
                }
            }
            is MainUiAction.GetStories -> {
                viewModelScope.launch {
                    repository.getStories(
                        1,
                        1000,
                        false,
                        uiState.value.userData?.token ?: "")
                        .collectLatest {
                            _uiState.update { state ->
                                state.copy(stories = it)
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
                repository.logout()
                processAction(MainUiAction.GetUserData)
            }
        }
    }
    
}

data class MainUiState(
    val userData: User? = null,
    val stories: Resource<List<Story>?> = Resource.Idle(),
    val navigateToAddStory: Boolean = false,
)

sealed class MainUiAction{
    object GetUserData: MainUiAction()
    object GetStories: MainUiAction()
    object NavigateToAddStory: MainUiAction()
    object Logout: MainUiAction()
}

