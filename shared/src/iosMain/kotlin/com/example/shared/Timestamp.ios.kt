package com.example.shared

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun currentTimestamp(): Long {
    val seconds = NSDate().timeIntervalSince1970
    return (seconds * 1000).toLong()
}
