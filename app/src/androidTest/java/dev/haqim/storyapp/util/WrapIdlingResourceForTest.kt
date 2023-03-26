package dev.haqim.storyapp.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource

fun wrapEspressoIdlingResourceForTest(countIdlingResource: CountingIdlingResource, block: () -> Unit){
    IdlingRegistry.getInstance()
        .register(countIdlingResource)

    block()

    IdlingRegistry.getInstance()
        .unregister(countIdlingResource)

}