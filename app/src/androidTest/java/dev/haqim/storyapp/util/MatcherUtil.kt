package dev.haqim.storyapp.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.actionWithAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.textfield.TextInputLayout
import dev.haqim.storyapp.ui.custom_view.CustomPasswordEditText
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import org.hamcrest.TypeSafeMatcher


fun textInputLayoutHintText(expectedHintText: String): Matcher<View>{
    
    return object: TypeSafeMatcher<View>(){
        override fun describeTo(description: Description?) {
            
        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is TextInputLayout) {
                return false;
            }

            val hint: CharSequence = item.hint ?: return false

            val hintText = hint.toString()

            return expectedHintText == hintText
        }

    }
 
}
fun textInputLayoutErrorText(expectedErrorText: String): Matcher<View>{

    return object: TypeSafeMatcher<View>(){
        override fun describeTo(description: Description?) {

        }

        override fun matchesSafely(item: View?): Boolean {
            if (item !is CustomPasswordEditText) {
                Log.d("textInputLayoutErrorText", item.toString())
                Log.d("textInputLayoutErrorText", "not TextInputLayout")
                return false;
            }

            Log.d("textInputLayoutErrorText", "error: ${item.error}")

            val error: CharSequence = item.error ?: return false

            val errorText = error.toString()

            return expectedErrorText == errorText
        }

    }
    

}

fun withBitmap(expectedBitmap: Bitmap): Matcher<Bitmap> {
    return object : TypeSafeMatcher<Bitmap>() {
        override fun describeTo(description: Description?) {
            description?.appendText("Bitmap did not match expected bitmap.")
        }

        override fun matchesSafely(actualBitmap: Bitmap?): Boolean {
            return actualBitmap?.sameAs(expectedBitmap) ?: false
        }
    }
}

fun withDrawable(@DrawableRes expectedDrawableRes: Int): BoundedMatcher<View, ImageView> {
    return object : BoundedMatcher<View, ImageView>(ImageView::class.java){
        override fun describeTo(description: Description) {
            description.appendText("with drawable from resource id: $expectedDrawableRes")
        }

        override fun matchesSafely(item: ImageView): Boolean {
            val drawable = item.drawable
            Log.d("withDrawable", drawable.toBitmap().toString())
            Log.d("withDrawable", ContextCompat.getDrawable(item.context, expectedDrawableRes)?.toBitmap().toString())
            val context = item.context
            val expectedDrawable = ContextCompat.getDrawable(context, expectedDrawableRes)
            return drawable.toBitmap().sameAs(expectedDrawable?.toBitmap())
        }

        private fun Drawable.toBitmap(): Bitmap {
            if (this is BitmapDrawable) {
                return this.bitmap
            }
            val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            this.setBounds(0, 0, canvas.width, canvas.height)
            this.draw(canvas)
            return bitmap
        }

    }
}

fun recyclerViewSizeMatcher(matcherSize: Int): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with list size: $matcherSize")
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            return matcherSize == recyclerView.adapter!!.itemCount
        }
    }
}

fun recyclerViewIsNotEmptyMatcher(): Matcher<View?> {
    return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("list is not empty")
        }

        override fun matchesSafely(recyclerView: RecyclerView): Boolean {
            return recyclerView.adapter!!.itemCount != 0
        }
    }
}

fun waitUntil(matcher: Matcher<View?>): ViewAction? {
    return actionWithAssertions(object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun getDescription(): String {
            val description = StringDescription()
            matcher.describeTo(description)
            return java.lang.String.format("wait until: %s", description)
        }

        override fun perform(uiController: UiController, view: View) {
            if (!matcher.matches(view)) {
                val callback = LayoutChangeCallback(matcher)
                try {
                    IdlingRegistry.getInstance().register(callback)
                    view.addOnLayoutChangeListener(callback)
                    uiController.loopMainThreadUntilIdle()
                } finally {
                    view.removeOnLayoutChangeListener(callback)
                    IdlingRegistry.getInstance().unregister(callback)
                }
            }
        }
    })
}

private class LayoutChangeCallback(private val matcher: Matcher<View?>) :
    IdlingResource, OnLayoutChangeListener {
    private var callback: IdlingResource.ResourceCallback? = null
    private var matched = false
    override fun getName(): String {
        return "Layout change callback"
    }

    override fun isIdleNow(): Boolean {
        return matched
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }

    override fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int,
    ) {
        matched = matcher.matches(v)
        callback!!.onTransitionToIdle()
    }
}
