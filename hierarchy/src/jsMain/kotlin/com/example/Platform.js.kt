package com.example

actual fun runtimeName(): String = "JavaScript"

fun jsGreeting(): String = "JS says: ${sharedJvmAndJsGreeting()}"
