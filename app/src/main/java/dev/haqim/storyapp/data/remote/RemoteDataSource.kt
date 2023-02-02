package dev.haqim.storyapp.data.remote

import com.google.gson.Gson
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.data.remote.response.BasicResponse
import dev.haqim.storyapp.data.remote.response.LoginResponse
import dev.haqim.storyapp.data.remote.response.StoriesResponse
import dev.haqim.storyapp.data.remote.response.StoryResponse
import dev.haqim.storyapp.model.Story
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class RemoteDataSource(private val service: ApiService) {
    
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

    fun getStories(page: Int, size: Int, location: Boolean, token: String): Flow<Result<StoriesResponse>> {
        return flow {
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
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lon: RequestBody? = null,
        lat: RequestBody? = null
    ): Flow<Result<BasicResponse>> {
        return flow {
            val response = service.addNewStory(file, description, lon, lat, token)
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