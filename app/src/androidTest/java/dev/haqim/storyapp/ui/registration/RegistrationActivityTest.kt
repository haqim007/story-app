package dev.haqim.storyapp.ui.registration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.gson.Gson
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.remote.network.ApiConfig
import dev.haqim.storyapp.data.remote.response.BasicResponse
import dev.haqim.storyapp.helper.util.EspressoIdlingResource
import dev.haqim.storyapp.ui.base.testCustomButtonVisibleLoading
import dev.haqim.storyapp.ui.base.testCustomButtonVisibleText
import dev.haqim.storyapp.util.DelayIdlingResource
import dev.haqim.storyapp.util.JsonConverter
import dev.haqim.storyapp.util.wrapEspressoIdlingResourceForTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@LargeTest
class RegistrationActivityTest{
    
    @get:Rule
    val activity = ActivityScenarioRule(RegistrationActivity::class.java)
    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockWebServer = MockWebServer()
    
    private val email = "halim@mail.com"
    private val name = "halim"
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

    private fun testBtnRegisterInitState(){
        // button register should disabled
        Espresso.onView(ViewMatchers.withId(R.id.register_btn))
            .check(matches(not(isEnabled())))
            .check(matches(not(isClickable())))
            .check(matches(withAlpha(0.5F)))

        testCustomButtonVisibleText()
    }

    private fun testBtnRegisterAfterValidInput(): ViewInteraction {
        testCustomButtonVisibleText()
        // button register should disabled
        return onView(withId(R.id.register_btn))
            .check(matches(isEnabled()))
            .check(matches(isClickable()))
            .check(matches(withAlpha(1.0F)))

    }

    private fun testBtnRegisterOnLoading(){
        // button register should disabled when loading
        onView(allOf(withId(R.id.register_btn)))
            .check(matches(not(isEnabled())))
            .check(matches(not(isClickable())))
            .check(matches(withAlpha(0.5F)))

        testCustomButtonVisibleLoading()
    }
    
    @Test
    fun register_with_valid_name_email_and_password(){
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBodyDelay(3, TimeUnit.SECONDS)
            .setBody(JsonConverter.readStringFromFile("registration_success_response.json"))
        mockWebServer.enqueue(mockResponse)
        
        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(name), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(email), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Test button register state after all input is valid
        testBtnRegisterAfterValidInput()
        
        // perform click on button register
        onView(withId(R.id.register_btn))
            .check(matches(isDisplayed()))
            .perform(click())

        // Test button register in loading state
        testBtnRegisterOnLoading()
        
        wrapEspressoIdlingResourceForTest(EspressoIdlingResource.countingIdlingResource){
            
            onView(withText(
                R.string.registered_successfully
            )).check(matches(isDisplayed()))

            runBlocking {
                val delayIdlingResource = DelayIdlingResource(3000L)
                IdlingRegistry.getInstance().register(delayIdlingResource)

                delayIdlingResource.start()
                // Delay for 3 seconds
                delay(3000)
                IdlingRegistry.getInstance().unregister(delayIdlingResource)

                onView(withId(R.id.clLogin)).check(matches(isDisplayed()))
                onView(withId(R.id.tv_login_subtitle))
                    .check(
                        matches(withText(R.string.sign_in_to_continue))
                    )
                onView(withId(R.id.tv_login_title))
                    .check(
                        matches(withText(R.string.welcome_back))
                    )
            }
        }
    }

    @Test
    fun register_with_valid_name_and_email_but_invalid_password(){

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(name), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(email), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView("123456"), closeSoftKeyboard())

        // Test button register state after all input has inserted
        testBtnRegisterInitState()
        
    }

    @Test
    fun register_with_valid_name_and_password_but_invalid_email(){

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(name), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView("email"), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Test button register state after all input has inserted
        testBtnRegisterInitState()

    }

    @Test
    fun register_with_valid_email_and_password_but_empty_name(){

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(email), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Test button register state after all input has inserted
        testBtnRegisterInitState()

    }

    @Test
    fun register_with_all_input_empty(){

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Test button register state after all input has inserted
        testBtnRegisterInitState()
    }

    @Test
    fun register_with_empty_name_and_email_and_password_are_invalid(){

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(""), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView("email"), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView("123456"), closeSoftKeyboard())

        // Test button register state after all input has inserted
        testBtnRegisterInitState()
    }

    @Test
    fun register_with_valid_name_email_and_password_but_email_is_already_taken(){
        val responseString = JsonConverter.readStringFromFile("registration_email_taken_response.json")
        val mockResponse = MockResponse()
            .setResponseCode(400)
            .setBodyDelay(3, TimeUnit.SECONDS)
            .setBody(responseString)
        val response = Gson().fromJson(responseString, BasicResponse::class.java)
        mockWebServer.enqueue(mockResponse)

        // Test button register state at first state
        testBtnRegisterInitState()

        // Input valid name
        onView(
            withId(R.id.ed_register_name)
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(name), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid email
        onView(
            allOf(
                withId(R.id.ed_register_email),
                withHint(R.string.email)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(email), closeSoftKeyboard())

        // Retest button register state after input email
        testBtnRegisterInitState()

        // Input valid password
        onView(
            allOf(
                withId(R.id.ed_register_password),
                withHint(R.string.password)
            )
        )
            .perform(click())
            .perform(typeTextIntoFocusedView(password), closeSoftKeyboard())

        // Test button register state after all input is valid
        testBtnRegisterAfterValidInput()

        // perform click on button register
        onView(withId(R.id.register_btn))
            .check(matches(isDisplayed()))
            .perform(click())

        // Test button register in loading state
        testBtnRegisterOnLoading()

        wrapEspressoIdlingResourceForTest(EspressoIdlingResource.countingIdlingResource){

            onView(withText(
                response.message
            )).check(matches(isDisplayed()))
            
        }
    }
}
