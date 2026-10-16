package com.example

data class LoginValidationResult(
    val emailError: String?,
    val passwordError: String?
) {
    val isValid: Boolean
        get() = emailError == null && passwordError == null
}

class LoginValidator {
    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Email is required"
        !isValidEmail(email) -> "Invalid email"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.length < 8 -> "Password must be at least 8 characters"
        password.none { it.isDigit() } -> "Password must contain a digit"
        password.none { it.isUpperCase() } -> "Password must contain an uppercase letter"
        else -> null
    }

    fun validate(email: String, password: String): LoginValidationResult =
        LoginValidationResult(
            emailError = validateEmail(email),
            passwordError = validatePassword(password)
        )
}
