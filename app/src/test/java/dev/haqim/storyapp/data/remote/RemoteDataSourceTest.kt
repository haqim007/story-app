package dev.haqim.storyapp.data.remote

import app.cash.turbine.test
import dev.haqim.storyapp.data.mechanism.HttpResult
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.helper.util.RequestBodyUtil
import dev.haqim.storyapp.util.DataDummy
import dev.haqim.storyapp.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RemoteDataSourceTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    
    @Mock
    private lateinit var service: ApiService
    @Mock
    private lateinit var userPreference: UserPreference
    @Mock
    private lateinit var requestBody: RequestBodyUtil
    
    private lateinit var remoteDataSource: RemoteDataSource

    @Test
    fun `When register() should return Success`() = runTest{
        val name = DataDummy.user().name
        val email = DataDummy.user().email
        val password = DataDummy.user().password
        
        `when`(service.register(name, email, password)).thenReturn(
            DataDummy.basicResponseSuccess()
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.register(name, email, password).test { 
            verify(service).register(name, email, password)
            val emission = awaitItem()
            
            assertTrue(emission is HttpResult.Success)
            assertEquals(DataDummy.basicResponseSuccess(), emission.data)
            assertFalse(DataDummy.basicMessageSuccess().error)
            
            cancelAndIgnoreRemainingEvents()
        }
        
    }

    @Test
    fun `When register() should return Error`() = runTest{
        val name = DataDummy.user().name
        val email = DataDummy.user().email
        val password = DataDummy.user().password
        val exception = HttpException(Response.error<ResponseBody>(500, ("{\n" +
        "    \"error\": true,\n" +
        "    \"message\": \"token expired\"\n" +
        "}").toResponseBody("plain/text".toMediaTypeOrNull())
        ))

        `when`(service.register(name, email, password)).thenThrow(
            exception
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.register(name, email, password).test {
            verify(service).register(name, email, password)
            val emission = awaitItem()
            assertEquals("token expired", emission.message)
            assertTrue(emission is HttpResult.Error)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When login() should return Success`() = runTest{
        val email = DataDummy.user().email
        val password = DataDummy.user().password

        `when`(service.login(email, password)).thenReturn(
            DataDummy.loginResponse()
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.login(email, password).test {
            verify(service).login(email, password)
            val emission = awaitItem()

            assertTrue(emission is HttpResult.Success)
            assertEquals(DataDummy.loginResponse(), emission.data)
            assertFalse(DataDummy.basicMessageSuccess().error)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When login() should return Error`() = runTest{
        val email = ""
        val password = DataDummy.user().password
        val exception = HttpException(Response.error<ResponseBody>(500, ("{\n" +
        "    \"error\": true,\n" +
        "    \"message\": \"email empty\"\n" +
        "}").toResponseBody("plain/text".toMediaTypeOrNull())
        ))

        `when`(service.login(email, password)).thenThrow(
            exception
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.login(email, password).test {
            verify(service).login(email, password)
            val emission = awaitItem()
            assertEquals("email empty", emission.message)
            assertTrue(emission is HttpResult.Error)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When getStories() should return Success`() = runTest{
        
        val page = 1
        val size = 4
        val location = 1
        val token = "123"
        
        `when`(userPreference.getUserToken()).thenReturn(
            flowOf(token)
        )
        
        `when`(service.getAllStories(page, size, location, token)).thenReturn(
            DataDummy.storiesResponse()
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.getStories(page, size, location).test {
            verify(service).getAllStories(page, size, location, token)
            val emission = awaitItem()

            assertTrue(emission is HttpResult.Success)
            assertEquals(DataDummy.storiesResponse(), emission.data)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When getStories() should return Error`() = runTest{
        val page = 1
        val size = 4
        val location = 1
        val token = "123"
        val exception = HttpException(Response.error<ResponseBody>(500, ("{\n" +
        "    \"error\": true,\n" +
        "    \"message\": \"token expired\"\n" +
        "}").toResponseBody("plain/text".toMediaTypeOrNull())
        ))

        `when`(userPreference.getUserToken()).thenReturn(
            flowOf(token)
        )
        
        `when`(service.getAllStories(page, size, location, token)).thenThrow(
            exception
        )
        remoteDataSource = RemoteDataSource(service, userPreference)
        remoteDataSource.getStories(page, size, location).test {
            verify(service).getAllStories(page, size, location, token)
            val emission = awaitItem()
            assertEquals("token expired", emission.message)
            assertTrue(emission is HttpResult.Error)

            cancelAndIgnoreRemainingEvents()
        }

    }

    @Test
    fun `When addStory Should return Loading Then Success`() = runTest{

        val file = DataDummy.file(".png")
        val description = "Ini deskripsi"
        val lon = -11F
        val lat = 111F
        val token = DataDummy.user().token

        `when`(userPreference.getUserToken()).thenReturn(
            flowOf(token)
        )

        `when`(service.addNewStory(
            requestBody.multipartRequestBody(file), 
            requestBody.textPlainRequestBody(description),
            requestBody.textPlainRequestBody(lon.toString()), 
            requestBody.textPlainRequestBody(lat.toString()),
            token
            )
        )
            .thenReturn(
                DataDummy.basicResponseSuccess()
            )

        remoteDataSource = RemoteDataSource(service, userPreference, requestBody)

        remoteDataSource.addStory(file, description, lon, lat).test {
            verify(service).addNewStory(
                requestBody.multipartRequestBody(file),
                requestBody.textPlainRequestBody(description),
                requestBody.textPlainRequestBody(lon.toString()),
                requestBody.textPlainRequestBody(lat.toString()),
                token
            )

            val successEmission = awaitItem()
            assertTrue(successEmission is HttpResult.Success)
            assertEquals(DataDummy.basicResponseSuccess(), successEmission.data)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `When addStory Should return Loading Then Error`() = runTest{

        val file = DataDummy.file(".png")
        val description = "Ini deskripsi"
        val lon = -11F
        val lat = 111F
        val token = DataDummy.user().token
        val exception = HttpException(Response.error<ResponseBody>(500, ("{\n" +
        "    \"error\": true,\n" +
        "    \"message\": \"failed to add new story\"\n" +
        "}").toResponseBody("plain/text".toMediaTypeOrNull())
        ))

        `when`(userPreference.getUserToken()).thenReturn(
            flowOf(token)
        )

        `when`(service.addNewStory(
            requestBody.multipartRequestBody(file),
            requestBody.textPlainRequestBody(description),
            requestBody.textPlainRequestBody(lon.toString()),
            requestBody.textPlainRequestBody(lat.toString()),
            token
            )
        ).thenThrow(exception)

        remoteDataSource = RemoteDataSource(service, userPreference, requestBody)

        remoteDataSource.addStory(file, description, lon, lat).test {
            verify(service).addNewStory(
                requestBody.multipartRequestBody(file),
                requestBody.textPlainRequestBody(description),
                requestBody.textPlainRequestBody(lon.toString()),
                requestBody.textPlainRequestBody(lat.toString()),
                token
            )

            val emission = awaitItem()
            assertTrue(emission is HttpResult.Error)
            assertEquals("failed to add new story", emission.message)

            cancelAndIgnoreRemainingEvents()
        }
    }
}