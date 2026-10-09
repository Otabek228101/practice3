package com.example.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreetingTest {

    @Test
    fun testGreeting() {
        assertEquals("Hello, Kotlin Multiplatform!", Greeting().greet())
    }

    @Test
    fun timestampIsNotZero() {
        assertTrue(currentTimestamp() > 0)
    }
}
