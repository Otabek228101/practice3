package com.example

actual fun runtimeName(): String = "JVM ${System.getProperty("java.version")}"

fun jvmGreeting(): String = "JVM says: ${sharedJvmAndJsGreeting()}"
