package dev.haqim.storyapp.ui.base

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import dev.haqim.storyapp.R
import org.hamcrest.CoreMatchers.allOf

fun testCustomButtonVisibleText(){
    // button login that use custom view should show text instead of loading
    onView(
        allOf(
            withId(R.id.tvCustomButton),
            isDescendantOfA(withId(R.id.clCustomButton))
        )
    )
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    onView(
        allOf(
            withId(R.id.loadingCustomButton),
            isDescendantOfA(withId(R.id.clCustomButton))
        )
    )
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
}

fun testCustomButtonVisibleLoading(){
    // button login that use custom view should show text instead of loading
    onView(
        allOf(
            withId(R.id.tvCustomButton),
            isDescendantOfA(withId(R.id.clCustomButton))
        )
    )
        .check(matches(withEffectiveVisibility(Visibility.GONE)))
    onView(
        allOf(
            withId(R.id.loadingCustomButton),
            isDescendantOfA(withId(R.id.clCustomButton))
        )
    )
        .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
}