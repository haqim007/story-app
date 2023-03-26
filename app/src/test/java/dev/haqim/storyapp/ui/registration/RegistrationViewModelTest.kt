package dev.haqim.storyapp.ui.registration

import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.domain.model.BasicMessage
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
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationViewModelTest{
    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var storyUseCase: StoryUseCase
    private lateinit var viewModel: RegistrationViewModel
    
    @Before
    fun setup(){
        viewModel = RegistrationViewModel(storyUseCase)
    }
    
    @Test
    fun `Given all input is empty When processAction() SetEmail with valid true Should update email state and allInputValid state set to false`() = runTest{
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))
        
        viewModel.uiState.test { 
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            
            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetEmail with valid false Should update email state and allInputValid state set to false`() = runTest{
        val expectedEmail = "haqimail.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()
            
            assertTrue(state.email is ResultInput.Invalid)
            assertNull(state.email.data)
            assertEquals(InputValidation.Invalid, state.email.validation)
            
            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetName Should update name state and allInputValid state set to false`() = runTest{
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetName empty String Should update name state and allInputValid state set to false`() = runTest{
        val expectedName = ""

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()
            
            assertTrue(state.name is ResultInput.Invalid)
            assertEquals(InputValidation.RequiredFieldInvalid, state.name.validation)
            assertNull(state.name.data)
            
            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetPassword with valid param as true Should update password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()
            
            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            
            assertFalse(state.allInputValid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given all input is empty When processAction() SetPassword with valid param as false Should update password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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
    fun `When processAction() SetEmail with valid param as false And SetName Should update email and name state and allInputValid state set to false`() = runTest{
        val expectedName = "haqim"
        val expectedEmail = "haqim.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            
            
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as true And SetName Should update email and name state and allInputValid state set to false`() = runTest{
        val expectedName = "haqim"
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)


            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }
    

    @Test
    fun `When processAction() SetEmail with valid param as true And SetPassword with valid param as true Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as true And SetPassword with valid param as false Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"
        val expectedEmail = "haqim@mail.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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
    fun `When processAction() SetEmail with valid param as false And SetPassword with valid param as true Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim.com"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetName And SetPassword with valid param as false Should update name and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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
    fun `When processAction() SetName And SetPassword with valid param as true Should update email and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "123456"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as true, SetName and SetPassword with valid param as true Should update email, name, password and allInputValid state set to true`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertTrue(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as false, SetName and SetPassword with valid param as true Should update email, name and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.password is ResultInput.Valid)
            assertEquals(expectedPassword, state.password.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When processAction() SetEmail with valid param as false, SetName and SetPassword with valid param as false Should update email, name and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Invalid)
            assertEquals(InputValidation.Invalid, state.email.validation)
            assertNull(state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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
    fun `When processAction() SetEmail with valid param as true, SetName and SetPassword with valid param as false Should update email, name and password state and allInputValid state set to false`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"

        viewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.email is ResultInput.Valid)
            assertEquals(expectedEmail, state.email.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetName(expectedName))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.name is ResultInput.Valid)
            assertEquals(expectedName, state.name.data)
            assertFalse(state.allInputValid)

            cancelAndIgnoreRemainingEvents()
        }

        viewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, false))

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
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedSuccess = Resource.Success(DataDummy.basicMessageSuccess())
        
        `when`(storyUseCase.register(expectedName, expectedEmail, expectedPassword)).thenReturn(
            flow { 
                emit(expectedLoading)
                delay(100)
                emit(expectedSuccess)
            }
        )
        
        val myViewModel = RegistrationViewModel(storyUseCase)

        myViewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))
        myViewModel.processAction(RegistrationUiAction.SetName(expectedName))
        myViewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))
        myViewModel.processAction(RegistrationUiAction.Submit)

        myViewModel.uiState.test {
            val loadingState = awaitItem()

            assertEquals(expectedLoading, loadingState.submitResult)

            val successState = awaitItem()

            assertEquals(expectedSuccess, successState.submitResult)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When Submit Should return Loading then Error`() = runTest{
        val expectedPassword = "12345678"
        val expectedEmail = "haqim@mail.com"
        val expectedName = "haqim"
        val expectedLoading = Resource.Loading<BasicMessage>()
        val expectedError = Resource.Error<BasicMessage>("Error")

        `when`(storyUseCase.register(expectedName, expectedEmail, expectedPassword)).thenReturn(
            flow {
                emit(expectedLoading)
                delay(100)
                emit(expectedError)
            }
        )

        val myViewModel = RegistrationViewModel(storyUseCase)

        myViewModel.processAction(RegistrationUiAction.SetEmail(expectedEmail, true))
        myViewModel.processAction(RegistrationUiAction.SetName(expectedName))
        myViewModel.processAction(RegistrationUiAction.SetPassword(expectedPassword, true))
        myViewModel.processAction(RegistrationUiAction.Submit)

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
    fun `When NavigateToLogin with navigate value true Should update navigateToLogin state to true`() = runTest{
        

        viewModel.processAction(RegistrationUiAction.NavigateToLogin(true))

        viewModel.uiState.test {
            val state = awaitItem()

            assertTrue(state.navigateToLogin)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When NavigateToLogin with navigate value false Should update navigateToLogin state to false`() = runTest{
        
        viewModel.processAction(RegistrationUiAction.NavigateToLogin(false))

        viewModel.uiState.test {
            val state = awaitItem()

            assertFalse(state.navigateToLogin)

            cancelAndIgnoreRemainingEvents()
        }
    }
}