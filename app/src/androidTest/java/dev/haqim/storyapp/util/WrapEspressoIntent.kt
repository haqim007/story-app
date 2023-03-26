package dev.haqim.storyapp.util

import androidx.test.espresso.intent.Intents

fun <T> wrapEspressoIntent(block: () -> T){
    // Initialize Intents API
    Intents.init()

    block()
    
    // Reset Intents API
    Intents.release()
}