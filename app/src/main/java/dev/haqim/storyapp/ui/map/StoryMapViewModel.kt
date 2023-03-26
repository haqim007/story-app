package dev.haqim.storyapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StoryMapViewModel(private val storyUseCase: StoryUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, MapUiState()
    )
    private val actionStateFlow = MutableSharedFlow<MapUiAction>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)

        processAction(MapUiAction.GetStories)
    }

    fun processAction(action: MapUiAction) = actionStateFlow.tryEmit(action)

    private fun MutableSharedFlow<MapUiAction>.updateStates() = onEach {
        when(it){
            is MapUiAction.GetStories -> {
                viewModelScope.launch {
                    storyUseCase.getStoriesWithLocation()
                        .collect{
                            _uiState.update { state ->
                                state.copy(stories = it)
                            }
                    }
                }
            }
            is MapUiAction.NavigateToDetailStory -> {
                _uiState.update { state ->
                    state.copy(
                        storyToBeOpened = it.story
                    )
                }
            }
            is MapUiAction.FinishNavigateToDetailStory -> {
                _uiState.update { state ->
                    state.copy(
                        storyToBeOpened = null
                    )
                }
            }
        }
    }
}

data class MapUiState(
    val stories: Resource<List<Story>> = Resource.Idle(),
    val storyToBeOpened: Story? = null
)

sealed class MapUiAction{
    object GetStories: MapUiAction()
    data class NavigateToDetailStory(val story: Story): MapUiAction()
    object FinishNavigateToDetailStory: MapUiAction()
}

