package com.example

fun sharedJvmAndJsGreeting(): String =
    "${commonHello()} + jvmAndJsMain (running on ${runtimeName()}, ${commonSecret()})"

expect fun runtimeName(): String
