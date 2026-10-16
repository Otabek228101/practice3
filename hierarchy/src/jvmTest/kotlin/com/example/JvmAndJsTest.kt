package com.example

import kotlin.test.Test
import kotlin.test.assertTrue

class JvmAndJsTest {
    @Test
    fun sharedFunctionWorksOnJvm() {
        val text = jvmGreeting()
        println(text)
        assertTrue(text.contains("jvmAndJsMain"))
        assertTrue(text.contains("JVM"))
    }
}
