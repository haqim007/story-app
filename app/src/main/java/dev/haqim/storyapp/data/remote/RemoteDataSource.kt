package dev.haqim.storyapp.data.remote

import dev.haqim.storyapp.data.mechanism.HttpResult
import dev.haqim.storyapp.data.mechanism.remoteResult
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.data.remote.response.BasicResponse
import dev.haqim.storyapp.data.remote.response.LoginResponse
import dev.haqim.storyapp.data.remote.response.StoriesResponse
import dev.haqim.storyapp.helper.util.RequestBodyUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File

class RemoteDataSource(
    private val service: ApiService,
    private val userPreference: UserPreference,
    private val requestBodyUtil: RequestBodyUtil = RequestBodyUtil
) {
    
    fun register(
        name: String, email: String, password: String
    ): Flow<HttpResult<BasicResponse>> {
        return remoteResult {
            service.register(name, email, password)
        }
    }

    fun login(
        email: String, password: String
    ): Flow<HttpResult<LoginResponse>>{
        return remoteResult {
            service.login(email, password)
        }
    }

    fun getStories(page: Int, size: Int, location: Int = 0) : Flow<HttpResult<StoriesResponse>> {
        return remoteResult {
            val token = userPreference.getUserToken().first()
            service.getAllStories(page, size, location, token)
        }
    }

    fun addStory(
        file: File,
        description: String,
        lon: Float? = null,
        lat: Float? = null
    ): Flow<HttpResult<BasicResponse>> {

        val multipartFile = requestBodyUtil.multipartRequestBody(file)
        val descriptionRequestBody = requestBodyUtil.textPlainRequestBody(description)
        val lonRequestBody = requestBodyUtil.textPlainRequestBodyNullable(lon?.toString())
        val latRequestBody = requestBodyUtil.textPlainRequestBodyNullable(lat?.toString())
        
        return remoteResult { 
            val token = userPreference.getUserToken().first()
            service.addNewStory(
                file = multipartFile,
                description = descriptionRequestBody, 
                lon = lonRequestBody, 
                lat = latRequestBody, 
                token =  token
            )
        }
        
    }

}