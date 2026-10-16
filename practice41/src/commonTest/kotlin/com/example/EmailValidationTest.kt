package com.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidationTest {
    @Test
    fun validEmail() {
        assertTrue(isValidEmail("student@ttpu.uz"))
    }

    @Test
    fun invalidEmail() {
        assertFalse(isValidEmail("student-at-ttpu.uz"))
    }
}
