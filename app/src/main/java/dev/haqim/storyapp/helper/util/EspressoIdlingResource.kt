package dev.haqim.storyapp.helper.util

import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    var RESOURCE = "GLOBAL"

    // CountingIdlingResource tracks whether the app is idle or busy 
    // while Espresso tests are running
    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    //Called at the beginning of a long-running operation 
    // (e.g. network call, database query, etc.) to indicate that the app is busy
    fun increment(){
        countingIdlingResource.increment()
    }

    fun decrement(){
        if(!countingIdlingResource.isIdleNow){
            countingIdlingResource.decrement()
        }
    }
    
    
}

/*
* It increments the EspressoIdlingResource before calling the function, 
* and decrements it after the function has completed. 
* This function can be used to wrap any long-running operation that needs to be tracked by 
* the CountingIdlingResource.
* */
inline fun <T> wrapEspressoIdlingResource(function: () -> T): T{
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function()
    }finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}