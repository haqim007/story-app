package dev.haqim.storyapp.ui.login

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.remote.network.ApiConfig
import dev.haqim.storyapp.helper.util.EspressoIdlingResource
import dev.haqim.storyapp.ui.base.testCustomButtonVisibleLoading
import dev.haqim.storyapp.ui.base.testCustomButtonVisibleText
import dev.haqim.storyapp.ui.main.MainActivity
import dev.haqim.storyapp.util.*
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {
    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)
    
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockWebServer = MockWebServer()
    
    private val email = "haqim@mail.com"
    private val password = "hehehehe"

    init {
        ApiConfig.BASE_URL = mockWebServer.url("/").toString()
    }
    
    @Before
    fun setup(){
        mockWebServer.start(8080)
 }
    
    @After
    fun tearDown(){
        mockWebServer.shutdown()
    }
    
    private fun testBtnLoginInitState(){
        // button login should disabled
        onView(withId(R.id.btnLogin))
            .check(matches(not(isEnabled())))
            .check(matches(not(isClickable())))
            .check(matches(withAlpha(0.5F)))

        testCustomButtonVisibleText()
    }

    private fun testBtnLoginAfterValidInput(): ViewInteraction {
        testCustomButtonVisibleText()
        // button login should disabled
        return onView(withId(R.id.btnLogin))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
            .check(matches(withAlpha(1.0F)))
        
    }

    private fun testBtnLoginOnLoading(){
        // button login should disabled when loading
        onView(withId(R.id.btnLogin))
            .check(matches(not(isEnabled())))
            .check(matches(not(isClickable())))
            .check(matches(withAlpha(0.5F)))

        testCustomButtonVisibleLoading()
    }
    
    @Test
    fun login_with_valid_email_and_password(){
        
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("login_success_response.json"))


        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView(email), closeSoftKeyboard())
            .check(matches(not(textInputLayoutErrorText(appContext.getString(R.string.email_is_required)))))
            .check(matches(not(textInputLayoutErrorText(appContext.getString(R.string.email_format_is_invalid)))))

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())
            .check(matches(not(textInputLayoutErrorText(appContext.getString(R.string.password_is_required)))))
            .check(matches(not(textInputLayoutErrorText(appContext.getString(R.string.password_length_min_8_chars)))))

        // Test button login state after all input is valid
        testBtnLoginAfterValidInput()

        // perform click on button login
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
            .perform(click())
        
        Thread.sleep(5000)

        // Test button login in loading state
        testBtnLoginOnLoading()

        wrapEspressoIdlingResourceForTest(EspressoIdlingResource.countingIdlingResource){
            // mock web server
            mockWebServer.enqueue(mockResponse)

            wrapEspressoIntent {
                // Assert that the intent was sent with the correct component
                intended(hasComponent(MainActivity::class.java.name))
            }
        }

    }

    @Test
    fun login_with_valid_email_but_password_less_than_8_chars_long(){

 
        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView(email))

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView("12345"), closeSoftKeyboard())
        
        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun login_with_valid_password_but_invalid_email(){
        
        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView("email"), closeSoftKeyboard())

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun login_with_invalid_email_and_password(){


        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView("email"), closeSoftKeyboard())

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView("123456"), closeSoftKeyboard())

        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun login_with_valid_email_but_password_empty(){


        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView(email))

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun login_with_valid_password_but_empty_email(){


        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun login_with_empty_email_and_password(){


        // Test button login state at first state
        testBtnLoginInitState()

        // Input valid email
        onView(allOf(withId(R.id.ed_login_email), withHint(R.string.email)))
            .perform(click())
            .perform(typeTextIntoFocusedView(""))

        // Retest button login state after input email
        testBtnLoginInitState()

        onView(allOf(withId(R.id.ed_login_password), withHint(R.string.password)))
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button login should still not be clickable and disabled
        testBtnLoginInitState()

    }

    @Test
    fun navigate_to_register_activity(){
        onView(withId(R.id.btn_signup))
            .perform(click())
        
        onView(withId(R.id.clRegistration)).check(matches(isDisplayed()))
        onView(withId(R.id.tv_create_account_title))
            .check(
                matches(withText(R.string.create_account))
            )
        
    }
}