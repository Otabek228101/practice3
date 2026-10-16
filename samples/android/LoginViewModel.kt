package com.example.kmppractice.android

import androidx.lifecycle.ViewModel
import com.example.LoginValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val canSubmit: Boolean = false
)

class LoginViewModel(
    private val validator: LoginValidator = LoginValidator()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(email: String) = update(email, _state.value.password)

    fun onPasswordChange(password: String) = update(_state.value.email, password)

    private fun update(email: String, password: String) {
        val result = validator.validate(email, password)
        _state.value = LoginUiState(
            email = email,
            password = password,
            emailError = result.emailError,
            passwordError = result.passwordError,
            canSubmit = result.isValid
        )
    }
}
