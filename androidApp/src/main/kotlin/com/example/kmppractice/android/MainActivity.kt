package com.example.kmppractice.android

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.example.shared.Greeting
import com.example.shared.currentTimestamp
import com.example.shared.getPlatform

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val greeting = Greeting()
        val text = greeting.greet() + "\n\n" +
                greeting.greetWithPlatform() + "\n\n" +
                "Platform: " + getPlatform().name + "\n" +
                "Timestamp: " + currentTimestamp()

        val textView = TextView(this)
        textView.textSize = 20f
        textView.setPadding(48, 120, 48, 48)
        textView.text = text
        setContentView(textView)
    }
}
