package dev.haqim.storyapp.ui.main

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.gson.Gson
import dev.haqim.storyapp.R
import dev.haqim.storyapp.data.remote.network.ApiConfig
import dev.haqim.storyapp.data.remote.response.StoriesResponse
import dev.haqim.storyapp.helper.util.TimeAgo
import dev.haqim.storyapp.helper.util.wrapEspressoIdlingResource
import dev.haqim.storyapp.util.JsonConverter
import dev.haqim.storyapp.util.withDrawable
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest{

    @get:Rule
    val activity = ActivityScenarioRule(MainActivity::class.java)


    private val appContext = ApplicationProvider.getApplicationContext<Context>()
    private val mockWebServer = MockWebServer()

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

    @Test
    fun load_stories(){

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("stories_success_response.json"))
        mockWebServer.enqueue(mockResponse)
        

        onView(withId(R.id.srl_main))
            .check(matches(isDisplayed()))
        onView(withId(R.id.rv_stories))
            .check(matches(isDisplayed()))
            .perform(
                RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(10)
            )
    }

    @Test
    fun load_detail_story(){

        val response = JsonConverter.readStringFromFile("stories_success_response.json")
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(response)
        mockWebServer.enqueue(mockResponse)
        val stories = Gson().fromJson(response, StoriesResponse::class.java)
        val firstStory = stories.listStory[0]
        mockWebServer.enqueue(mockResponse)
        
        onView(withId(R.id.rv_stories))
            .check(matches(isDisplayed()))
            .perform(
                RecyclerViewActions
                    .actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0, click()
                    )
            )
        
        onView(withId(R.id.nslDetailStory))
            .check(matches(isDisplayed()))
        
        // check name
        onView(allOf(withId(R.id.tvFullName), isDescendantOfA(withId(R.id.nslDetailStory))))
            .check(matches(withText(firstStory.name)))
        // check created at
        onView(allOf(withId(R.id.tvCreatedAt), isDescendantOfA(withId(R.id.nslDetailStory))))
            .check(matches(withText(TimeAgo().getTimeAgo(firstStory.createdAt))))
        // check description
        onView(allOf(withId(R.id.tvDescription), isDescendantOfA(withId(R.id.nslDetailStory))))
            .check(matches(withText(firstStory.description)))
        //check image with glide
        wrapEspressoIdlingResource {
            onView(allOf(withId(R.id.imgPhoto), isDescendantOfA(withId(R.id.nslDetailStory))))
                .check(matches(isDisplayed()))
                .check(matches(not(withDrawable(R.drawable.outline_image_search_24))))
                .check(matches(not(withDrawable(R.drawable.outline_broken_image_24))))
        }
    }

}