package com.example.shared

import kotlin.test.Test
import kotlin.test.assertEquals

class HelloTest {

    @Test
    fun testHello() {
        assertEquals("Hello from commonMain", hello())
    }
}
