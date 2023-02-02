package dev.haqim.storyapp.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.databinding.ActivityLoginBinding
import dev.haqim.storyapp.di.Injection.provideViewModelProvider
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.main.MainActivity
import dev.haqim.storyapp.ui.registration.RegistrationActivity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel : LoginViewModel by viewModels {
        provideViewModelProvider(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.log_in)

        val uiState = viewModel.uiState
        val uiAction = {action: LoginUiAction -> viewModel.processAction(action)}

        //bindInputEmail
        bindInputEmail(uiState, uiAction)

        // bindPasswordInput
        bindInputPassword(uiState, uiAction)

        //bindSubmitButton
        bindSubmitButton(uiAction, uiState)

        //bindSubmitButton
        bindNavigateToRegistration(uiState, uiAction)

        //bindNavigateToHome
        bindNavigateToHome(uiState, uiAction)

    }

    private fun bindSubmitButton(
        uiAction: (LoginUiAction) -> Boolean,
        uiState: StateFlow<LoginUiState>,
    ) {
        binding.btnLogin.apply {
            setText(getString(R.string.log_in))
            setEnable(false)
        }
        binding.btnLogin.setOnClickListener {
            uiAction(LoginUiAction.Submit)
        }
        val submitResultFlow = uiState.map { it.submitResult }.distinctUntilChanged()
        lifecycleScope.launch {
            submitResultFlow.collectLatest {
                binding.btnLogin.apply {
                    setLoading(false)
                    setEnable(uiState.value.allInputValid)
                }
                when (it) {
                    is Resource.Loading -> {
                        binding.btnLogin.apply {
                            setLoading(true)
                            setEnable(false)
                        }.also {
                            binding.edLoginEmail.clearFocus()
                            binding.edLoginPassword.clearFocus()
                        }
                    }
                    is Resource.Success -> {
                        uiAction(LoginUiAction.NavigateToHome)
                    }
                    is Resource.Error -> {
                        val mySnackBar = Snackbar.make(
                            binding.coordinator,
                            it.message ?: getString(R.string.failed_to_login),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        mySnackBar.setAction(R.string.close) {
                            mySnackBar.dismiss()
                        }
                        mySnackBar.show()
                    }
                    else -> {}
                }
            }
        }

        val allInputValidFlow = uiState.map { it.allInputValid }.distinctUntilChanged()
        lifecycleScope.launch {
            allInputValidFlow.collect {
                binding.btnLogin.setEnable(it)
            }
        }
    }

    private fun bindNavigateToHome(
        uiState: StateFlow<LoginUiState>,
        uiAction: (LoginUiAction) -> Boolean,
    ) {
        val navigateToHomeFlow = uiState.map { it.navigateToHome }.distinctUntilChanged()
        lifecycleScope.launch {
            navigateToHomeFlow.collectLatest {
                if (it) {
                    toHomeScreen(uiAction)
                }
            }
        }
    }

    private fun bindNavigateToRegistration(
        uiState: StateFlow<LoginUiState>,
        uiAction: (LoginUiAction) -> Boolean,
    ) {
        val navigateToRegistrationFlow = uiState.map { it.navigateToRegistration }.distinctUntilChanged()
        lifecycleScope.launch {
            navigateToRegistrationFlow.collectLatest {
                if (it) {
                    toRegistrationScreen(uiAction)
                }
            }
        }

        binding.btnSignup.setOnClickListener{
            uiAction(LoginUiAction.NavigateToRegistration)
        }
    }

    private fun bindInputEmail(
        uiState: StateFlow<LoginUiState>,
        uiAction: (LoginUiAction) -> Boolean,
    ) {
//        val emailInputFlow = uiState.map { it.email }.distinctUntilChanged()
//        lifecycleScope.launch {
//            emailInputFlow.collectLatest {
//                if (it is ResultInput.Invalid) {
//                    binding.edLoginEmailLayout.isErrorEnabled = true
//                    when (it.validation) {
//                        InputValidation.EmailInvalid -> {
//                            binding.edLoginEmailLayout.error =
//                                getString(R.string.email_format_is_invalid)
//                        }
//                        InputValidation.RequiredFieldInvalid -> {
//                            binding.edLoginEmailLayout.error =
//                                getString(R.string.email_is_required)
//                        }
//                        else -> {}
//                    }
//                } else {
//                    binding.edLoginEmailLayout.error = null
//                    binding.edLoginEmailLayout.isErrorEnabled = false
//                }
//            }
//        }

        binding.edLoginEmail.doAfterTextChanged { value, isValid ->
            uiAction(LoginUiAction.SetEmail(value.toString(), isValid))
        }
    }

    private fun bindInputPassword(
        uiState: StateFlow<LoginUiState>,
        uiAction: (LoginUiAction) -> Boolean,
    ) {

        binding.edLoginPassword.doAfterTextChanged { value, isValid ->
            uiAction(LoginUiAction.SetPassword(value.toString(), isValid))
        }
    }

    private fun toHomeScreen(uiAction: (LoginUiAction) -> Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        uiAction(LoginUiAction.NavigateToHome)
        finish()
    }

    private fun toRegistrationScreen(uiAction: (LoginUiAction) -> Boolean) {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
        uiAction(LoginUiAction.NavigateToRegistration)
        finish()
    }


}
