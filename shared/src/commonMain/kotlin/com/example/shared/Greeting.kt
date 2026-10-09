package com.example.shared

class Greeting {
    fun greet(): String {
        return "Hello, Kotlin Multiplatform!"
    }

    fun greetWithPlatform(): String {
        return "Hello from " + getPlatform().name
    }
}
