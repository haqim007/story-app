package dev.haqim.storyapp.ui.registration

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.databinding.ActivityRegistrationBinding
import dev.haqim.storyapp.di.Injection.provideViewModelProvider
import dev.haqim.storyapp.helper.util.InputValidation
import dev.haqim.storyapp.helper.util.ResultInput
import dev.haqim.storyapp.ui.base.BaseActivity
import dev.haqim.storyapp.ui.login.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class RegistrationActivity : BaseActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val viewModel: RegistrationViewModel by viewModels {
        provideViewModelProvider(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.registration_title)

        val uiState = viewModel.uiState
        val uiAction = {action: RegistrationUiAction -> viewModel.processAction(action)}

        bindInputEmail(uiState, uiAction)

        bindInputName(uiState, uiAction)

        bindInputPassword(uiAction)

        bindRegisterButton(uiState, uiAction)

        val navigateToLoginFlow = uiState.map { it.navigateToLogin }.distinctUntilChanged()
        lifecycleScope.launch {
            navigateToLoginFlow.collect {
                if(it){
                    toLoginScreen(uiAction)
                }
            }
        }
    }

    private fun bindRegisterButton(
        uiState: StateFlow<RegistrationUiState>,
        uiAction: (RegistrationUiAction) -> Boolean
    ) {

        binding.registerBtn.apply {
            setText(getString(R.string.sign_up))
            setEnable(false)
        }

        val validateInputsFlow = uiState.map { it.allInputValid }.distinctUntilChanged()

        lifecycleScope.launch {
            validateInputsFlow.collect {
                binding.registerBtn.setEnable(it)
            }
        }

        binding.registerBtn.setOnClickListener {
            uiAction(RegistrationUiAction.Submit)
        }
        val submitFlow = uiState.map { it.submitResult }.distinctUntilChanged()
        lifecycleScope.launch {
            submitFlow.collectLatest {
                
                binding.registerBtn.setLoading(false)
                binding.registerBtn.setEnable(uiState.value.allInputValid)

                when(it){
                    is Resource.Loading -> {
                        binding.registerBtn.apply{
                            setLoading(true)
                            setEnable(false)
                        }.also {
                            binding.edRegisterEmail.clearFocus()
                            binding.edRegisterNameLayout.clearFocus()
                            binding.edRegisterPassword.clearFocus()
                        }
                    }
                    is Resource.Success -> {
                        val mySnackBar = Snackbar.make(binding.clRegistration,
                            getString(R.string.registered_successfully),
                            Snackbar.LENGTH_INDEFINITE
                        )
                        mySnackBar.show()
                        lifecycleScope.launch { 
                            delay(3000)
                            uiAction(RegistrationUiAction.NavigateToLogin())
                        }
                    }
                    is Resource.Error -> {
                        val mySnackBar = Snackbar.make(binding.clRegistration,
                            it.message ?: getString(R.string.failed_to_register),
                            Snackbar.LENGTH_LONG
                        )
                        mySnackBar.show()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun bindInputEmail(
        uiState: StateFlow<RegistrationUiState>,
        uiAction: (RegistrationUiAction) -> Boolean,
    ) {

        binding.edRegisterEmail.doAfterTextChanged { value, isValid ->
            uiAction(RegistrationUiAction.SetEmail(value.toString(), isValid))
        }
    }

    private fun bindInputName(
        uiState: StateFlow<RegistrationUiState>,
        uiAction: (RegistrationUiAction) -> Boolean,
    ) {

        val nameInputFlow = uiState.map { it.name }.distinctUntilChanged()
        lifecycleScope.launch {
            nameInputFlow.collect {
                if(it is ResultInput.Invalid){
                    when(it.validation){
                        InputValidation.RequiredFieldInvalid -> {
                            binding.edRegisterNameLayout.isErrorEnabled = true
                            binding.edRegisterNameLayout.error =
                                getString(R.string.name_is_required)
                        }
                        else -> {}
                    }
                }else{
                    binding.edRegisterNameLayout.error = null
                    binding.edRegisterNameLayout.isErrorEnabled = false
                }
            }
        }
        binding.edRegisterName.doOnTextChanged { value, _, _, _ ->
            uiAction(RegistrationUiAction.SetName(value.toString()))
        }
    }

    private fun bindInputPassword(
        uiAction: (RegistrationUiAction) -> Boolean,
    ) {
        binding.edRegisterPassword.doAfterTextChanged { value, isValid ->
            uiAction(RegistrationUiAction.SetPassword(value.toString(), isValid))
        }
    }

    private fun toLoginScreen(uiAction: (RegistrationUiAction) -> Boolean){

        val optionsCompat: ActivityOptionsCompat =
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair(binding.edRegisterEmail, "email"),
                Pair(binding.edRegisterPassword, "password"),
            )

        val intent = Intent(
            this@RegistrationActivity,
            LoginActivity::class.java
        )
        startActivity(intent, optionsCompat.toBundle())
        uiAction(RegistrationUiAction.NavigateToLogin(false))
        finish()

    }


}