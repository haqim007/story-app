package dev.haqim.storyapp.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.helper.util.InputValidation
import dev.haqim.storyapp.helper.util.ResultInput
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

class LoginViewModel(
    private val storyUseCase: StoryUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, LoginUiState()
    )
    private val actionStateFlow = MutableSharedFlow<LoginUiAction>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST)

    init {
        actionStateFlow.updateStates().launchIn(viewModelScope)
    }

    fun processAction(action: LoginUiAction): Boolean {
//        EspressoIdlingResource.increment()
        return actionStateFlow.tryEmit(action)
    }

    private fun MutableSharedFlow<LoginUiAction>.updateStates() = onEach {
        when(it){
            is LoginUiAction.SetEmail -> {
                val emailValue = if(it.isValid) ResultInput.Valid(data = it.value)
                else ResultInput.Invalid(InputValidation.Invalid)
                _uiState.update { state ->
                    state.copy(
                        email = emailValue,
                        allInputValid =
                            uiState.value.password is ResultInput.Valid &&
                            emailValue is ResultInput.Valid
                    )
                }
            }
            is LoginUiAction.SetPassword -> {
                val passwordValue = if(it.isValid) ResultInput.Valid(data = it.value)
                    else ResultInput.Invalid(InputValidation.Invalid)
                _uiState.update { state ->
                    state.copy(
                        password = passwordValue,
                        allInputValid =
                        passwordValue is ResultInput.Valid &&
                        uiState.value.email is ResultInput.Valid
                    )
                }
            }
            is LoginUiAction.Submit -> {
                if(uiState.value.allInputValid){
                    storyUseCase.login(
                        email = uiState.value.email.data!!,
                        password = uiState.value.password.data!!
                    ).collect{
                        _uiState.update { state ->
                            state.copy(submitResult = it)
                        }
                    }
                }
            }
            is LoginUiAction.NavigateToHome -> {
                _uiState.update { state -> state.copy(navigateToHome = !state.navigateToHome) }
            }
            is LoginUiAction.NavigateToRegistration -> {
                _uiState.update { state -> state.copy(navigateToRegistration = !state.navigateToRegistration) }
//                EspressoIdlingResource.decrement()
            }
        }
    }

}

data class LoginUiState(
    val email: ResultInput<String> = ResultInput.Idle(),
    val password: ResultInput<String> = ResultInput.Idle(),
    val allInputValid: Boolean = false,
    val submitResult: Resource<Login> = Resource.Idle(),
    val navigateToHome: Boolean = false,
    val navigateToRegistration: Boolean = false
)

sealed class LoginUiAction{
    data class SetEmail(val value: String, val isValid: Boolean = false): LoginUiAction()
    data class SetPassword(val value: String, val isValid: Boolean = false): LoginUiAction()
    object Submit: LoginUiAction()
    object NavigateToHome: LoginUiAction()
    object NavigateToRegistration: LoginUiAction()
}