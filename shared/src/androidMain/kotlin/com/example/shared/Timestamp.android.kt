package com.example.shared

actual fun currentTimestamp(): Long {
    return System.currentTimeMillis()
}
