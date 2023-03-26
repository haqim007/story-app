package dev.haqim.storyapp.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.helper.util.InputValidation
import dev.haqim.storyapp.helper.util.ResultInput
import dev.haqim.storyapp.helper.util.isValidRequiredField
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class RegistrationViewModel(
    private val storyUseCase: StoryUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, RegistrationUiState()
    )
    private val actionStateFlow = MutableSharedFlow<RegistrationUiAction>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)
    }

    fun processAction(action: RegistrationUiAction) = actionStateFlow.tryEmit(action)

    private fun MutableSharedFlow<RegistrationUiAction>.updateStates() = onEach {
        when(it){
            is RegistrationUiAction.SetEmail -> {
                val emailValue = if(it.isValid) ResultInput.Valid(data = it.value)
                else ResultInput.Invalid(InputValidation.Invalid)
                _uiState.update { state ->
                    state.copy(
                        email = emailValue,
                        allInputValid = uiState.value.name is ResultInput.Valid &&
                            uiState.value.password is ResultInput.Valid &&
                            emailValue is ResultInput.Valid
                    )
                }
            }
            is RegistrationUiAction.SetName -> {
                _uiState.update { state ->
                    state.copy(
                        name = isValidRequiredField(it.value),
                        allInputValid = isValidRequiredField(it.value) is ResultInput.Valid &&
                        uiState.value.password is ResultInput.Valid &&
                        uiState.value.email is ResultInput.Valid
                    )
                }
            }
            is RegistrationUiAction.SetPassword -> {
                val passwordValue = if(it.isValid) ResultInput.Valid(data = it.value)
                else ResultInput.Invalid(InputValidation.Invalid)
                _uiState.update { state ->
                    state.copy(
                        password =  passwordValue,
                        allInputValid = uiState.value.name is ResultInput.Valid &&
                        passwordValue is ResultInput.Valid &&
                        uiState.value.email is ResultInput.Valid
                    )
                }
            }
            is RegistrationUiAction.Submit -> {
                if(uiState.value.allInputValid){
                    storyUseCase.register(
                        name = uiState.value.name.data!!,
                        email = uiState.value.email.data!!,
                        password = uiState.value.password.data!!
                    ).collect{
                        _uiState.update { state ->
                            state.copy(submitResult = it)
                        }
                    }
                }
            }
            is RegistrationUiAction.NavigateToLogin -> {
                _uiState.update { state -> state.copy(navigateToLogin = it.navigate) }
            }
        }
    }

}

data class RegistrationUiState(
    val email: ResultInput<String> = ResultInput.Idle(),
    val name: ResultInput<String> = ResultInput.Idle(),
    val password: ResultInput<String> = ResultInput.Idle(),
    val allInputValid: Boolean = false,
    val submitResult: Resource<BasicMessage> = Resource.Idle(),
    val navigateToLogin: Boolean = false
)

sealed class RegistrationUiAction{
    data class SetEmail(val value: String, val isValid: Boolean): RegistrationUiAction()
    data class SetName(val value: String): RegistrationUiAction()
    data class SetPassword(val value: String, val isValid: Boolean = false): RegistrationUiAction()
    object Submit: RegistrationUiAction()
    data class NavigateToLogin(val navigate: Boolean = true): RegistrationUiAction()
}