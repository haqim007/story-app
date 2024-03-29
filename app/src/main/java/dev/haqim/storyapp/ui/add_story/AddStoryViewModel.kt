package dev.haqim.storyapp.ui.add_story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.helper.util.ResultInput
import dev.haqim.storyapp.helper.util.isValidRequiredField
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AddStoryViewModel(private val storyUseCase: StoryUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(AddStoryUiState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, AddStoryUiState()
    )
    private val actionStateFlow = MutableSharedFlow<AddStoryUiAction>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)
        processAction(AddStoryUiAction.GetUserData)
    }

    fun processAction(action: AddStoryUiAction) = actionStateFlow.tryEmit(action)

    private fun MutableSharedFlow<AddStoryUiAction>.updateStates() = onEach {
        when(it){
            is AddStoryUiAction.GetUserData -> {
                viewModelScope.launch {
                    storyUseCase.getUser().collectLatest {user ->
                        _uiState.update { state ->
                            state.copy(userData = user)
                        }
                    }
                }
            }
            is AddStoryUiAction.OpenCamera -> {
                _uiState.update { state ->
                    state.copy(openCamera = true)
                }
            }
            is AddStoryUiAction.CloseCamera -> {
                _uiState.update { state ->
                    state.copy(openCamera = false)
                }
            }
            is AddStoryUiAction.OpenGallery -> {
                _uiState.update { state ->
                    state.copy(openGallery = true)
                }
            }
            is AddStoryUiAction.CloseGallery -> {
                _uiState.update { state ->
                    state.copy(openGallery = false)
                }
            }
            is AddStoryUiAction.UploadStory -> {
                viewModelScope.launch {
                    storyUseCase.addStory(
                        file = uiState.value.file!!,
                        description = uiState.value.description.data!!,
                        lon = uiState.value.lon?.toFloat(),
                        lat = uiState.value.lat?.toFloat()
                    ).collect {
                        _uiState.update { state ->
                            state.copy(uploadResult = it)
                        }
                    }
                }
            }
            is AddStoryUiAction.SetFile -> {
                _uiState.update { state ->
                    state.copy(
                        file = it.file,
                        allInputValid = uiState.value.description is ResultInput.Valid
                    )
                }
            }
            is AddStoryUiAction.SetDescription -> {
                _uiState.update { state ->
                    state.copy(
                        description = isValidRequiredField(it.description),
                        allInputValid = isValidRequiredField(it.description) is ResultInput.Valid &&
                            uiState.value.file != null
                    )
                }
            }
            AddStoryUiAction.NavigateToStories -> {
                _uiState.update { state ->
                    state.copy(
                        navigateToStories = !uiState.value.navigateToStories
                    )
                }
            }
            is AddStoryUiAction.ShareLocation -> {
                _uiState.update { state -> 
                    state.copy(
                        lon = it.lon,
                        lat = it.lat,
                        shareLocation = it.shareLocation
                    )
                }
            }
        }
    }
    
}

data class AddStoryUiState(
    val userData: User? = null,
    val openCamera: Boolean = false,
    val openGallery: Boolean = false,
    val file: File? = null,
    val description: ResultInput<String> = ResultInput.Idle(),
    val uploadResult: Resource<BasicMessage> = Resource.Idle(),
    val allInputValid: Boolean = false,
    val navigateToStories: Boolean = false,
    val shareLocation: Boolean = false,
    val lon: Double? = null,
    val lat: Double? = null
)

sealed class AddStoryUiAction{
    object GetUserData: AddStoryUiAction()
    object OpenCamera: AddStoryUiAction()
    object CloseCamera: AddStoryUiAction()
    object OpenGallery: AddStoryUiAction()
    object CloseGallery: AddStoryUiAction()
    object UploadStory: AddStoryUiAction()
    data class SetFile(val file: File): AddStoryUiAction()
    data class SetDescription(val description: String): AddStoryUiAction()
    object NavigateToStories : AddStoryUiAction()
    data class ShareLocation(val shareLocation: Boolean, val lon: Double? = null, val lat: Double? = null): AddStoryUiAction()
}

