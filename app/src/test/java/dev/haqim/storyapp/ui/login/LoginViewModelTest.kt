package dev.haqim.storyapp.ui.login

import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.helper.util.InputValidation
import dev.haqim.storyapp.helper.util.ResultInput
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest{
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyUseCase: StoryUseCase
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup(){
        viewModel = LoginViewModel(storyUseCase)
    }

    @Test
    fun `Given all input is empty When processAction() SetEmail with valid true Should update email state and allInputValid state set to false`() = runTest{
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(LoginUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)

            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetPassword with valid param as true Should update password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"

        viewModel.processAction(LoginUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)

            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as true And SetPassword with valid param as true Should update email and password state and allInputValid state set to true`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(LoginUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(LoginUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertTrue(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as true And SetPassword with valid param as false Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(LoginUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(LoginUiAction.SetPassword(expectedPassword, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.password.validation)
            assertNull(state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as false And SetPassword with valid param as false Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"
        val expectedEmail = "haqim.com"

        viewModel.processAction(LoginUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(LoginUiAction.SetPassword(expectedPassword, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.password.validation)
            assertNull(state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When Submit Should return Loading then Success`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"
        val expectedLoading = Resource.Loading<Login>()
        val expectedSuccess = Resource.Success(DataDummy.login())

        Mockito.`when`(storyUseCase.login(expectedEmail, expectedPassword)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedSuccess)
            }
        )

        val myViewModel = LoginViewModel(storyUseCase)

        myViewModel.processAction(LoginUiAction.SetEmail(expectedEmail, true))
        myViewModel.processAction(LoginUiAction.SetPassword(expectedPassword, true))
        myViewModel.processAction(LoginUiAction.Submit)

        myViewModel.uiState.test {
            val loadingEmission = awaitItem()

            assertEquals(expectedLoading, loadingEmission.submitResult)

            val successEmission = awaitItem()

            assertEquals(expectedSuccess, successEmission.submitResult)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When Submit Should return Loading then Error`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedLoading = Resource.Loading<Login>()
        val expectedError = Resource.Error<Login>("Error")

        Mockito.`when`(storyUseCase.login(expectedEmail, expectedPassword)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedError)
            }
        )

        val myViewModel = LoginViewModel(storyUseCase)

        myViewModel.processAction(LoginUiAction.SetEmail(expectedEmail, true))
        myViewModel.processAction(LoginUiAction.SetPassword(expectedPassword, true))
        myViewModel.processAction(LoginUiAction.Submit)

        myViewModel.uiState.test {
            val loadingEmission = awaitItem()

            assertEquals(expectedLoading, loadingEmission.submitResult)

            val errorEmission = awaitItem()

            assertEquals(expectedError, errorEmission.submitResult)
            assertEquals("Error", errorEmission.submitResult.message)
            assertNull(errorEmission.submitResult.data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToRegistration state is false When NavigateToRegistration Should navigateToRegistration state to true`() = runTest{

        viewModel.processAction(LoginUiAction.NavigateToRegistration)
        
        viewModel.uiState.test { 
            val state = awaitItem()
            
            assertTrue(state.navigateToRegistration)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToRegistration state is true When NavigateToRegistration Should navigateToRegistration state to false`() = runTest{

        //Given: set navigateToRegistration to true
        viewModel.processAction(LoginUiAction.NavigateToRegistration)
        
        //When: set navigateToRegistration to false
        viewModel.processAction(LoginUiAction.NavigateToRegistration)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.navigateToRegistration)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToHome state is false When NavigateToHome Should navigateToHome state to true`() = runTest{

        viewModel.processAction(LoginUiAction.NavigateToHome)

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.navigateToHome)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given navigateToHome state is true When NavigateToHome Should navigateToHome state to false`() = runTest{

        //Given: set navigateToHome to true
        viewModel.processAction(LoginUiAction.NavigateToHome)

        //When: set navigateToHome to false
        viewModel.processAction(LoginUiAction.NavigateToHome)

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.navigateToHome)

            cancelAndIgnoreRemainingEvents()
        }
    }
}