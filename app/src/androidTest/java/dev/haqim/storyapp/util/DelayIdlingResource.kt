package dev.haqim.storyapp.util

import androidx.test.espresso.IdlingResource


class DelayIdlingResource(private val delayMillis: Long) : IdlingResource {
    private var startTime: Long = 0
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = DelayIdlingResource::class.java.name

    override fun isIdleNow(): Boolean {
        val idle = System.currentTimeMillis() >= startTime + delayMillis
        if (idle) {
            resourceCallback?.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

    fun start() {
        startTime = System.currentTimeMillis()
    }
}
