package dev.haqim.storyapp.data.remote

import com.google.gson.Gson
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.data.remote.response.BasicResponse
import dev.haqim.storyapp.data.remote.response.LoginResponse
import dev.haqim.storyapp.data.remote.response.StoriesResponse
import dev.haqim.storyapp.helper.util.RequestBodyUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.File

class RemoteDataSource(
    private val service: ApiService,
    private val userPreference: UserPreference,
    private val requestBodyUtil: RequestBodyUtil = RequestBodyUtil
) {
    
    fun register(
        name: String, email: String, password: String
    ): Flow<Result<BasicResponse>>{
        return flow {
            val response = service.register(name, email, password)
            emit(Result.success(response))
        }.catch {
            val error = (it as? HttpException)?.response()?.errorBody()?.string()
            error?.let {
                val response = parseError(error)
                emit(Result.failure(Throwable(message = response?.message)))
            }
                ?: emit(Result.failure(it))

        }
    }

    fun login(
        email: String, password: String
    ): Flow<Result<LoginResponse>>{
        return flow {
            val response = service.login(email, password)
            emit(Result.success(response))
        }.catch {
            val error = (it as? HttpException)?.response()?.errorBody()?.string()
            error?.let {
                val response = parseError(error)
                emit(Result.failure(Throwable(message = response?.message)))
            }
                ?: emit(Result.failure(it))

        }
    }

    fun getStories(page: Int, size: Int, location: Int = 0) : Flow<Result<StoriesResponse>> {
        return flow {
            val token = userPreference.getUserToken().first()
            val response = service.getAllStories(page, size, location, token)
            emit(Result.success(response))
        }.catch {
            val error = (it as? HttpException)?.response()?.errorBody()?.string()
            error?.let {
                val response = parseError(error)
                emit(Result.failure(Throwable(message = response?.message)))
            }
                ?: emit(Result.failure(it))
        }
    }

    fun addStory(
        file: File,
        description: String,
        lon: Float? = null,
        lat: Float? = null
    ): Flow<Result<BasicResponse>> {

        val multipartFile = requestBodyUtil.multipartRequestBody(file)
        val descriptionRequestBody = requestBodyUtil.textPlainRequestBody(description)
        val lonRequestBody = requestBodyUtil.textPlainRequestBodyNullable(lon?.toString())
        val latRequestBody = requestBodyUtil.textPlainRequestBodyNullable(lat?.toString())
        
        return flow {
            val token = userPreference.getUserToken().first()
            val response = service.addNewStory(
                file = multipartFile,
                description = descriptionRequestBody, 
                lon = lonRequestBody, 
                lat = latRequestBody, 
                token =  token
            )
            emit(Result.success(response))
        }.catch {
            val error = (it as? HttpException)?.response()?.errorBody()?.string()
            error?.let {
                val response = parseError(error)
                emit(Result.failure(Throwable(message = response?.message)))
            }
                ?: emit(Result.failure(it))
        }
    }

    private fun parseError(error: String?): BasicResponse? {
        return Gson().fromJson(error, BasicResponse::class.java)
    }

}