package com.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LoginValidatorTest {
    private val validator = LoginValidator()

    @Test
    fun validLogin() {
        assertTrue(validator.validate("student@ttpu.uz", "Secret123").isValid)
    }

    @Test
    fun invalidLogin() {
        val result = validator.validate("wrong", "short")
        assertFalse(result.isValid)
        assertEquals("Invalid email", result.emailError)
        assertEquals("Password must be at least 8 characters", result.passwordError)
    }
}
