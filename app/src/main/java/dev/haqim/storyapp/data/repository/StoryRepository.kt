package dev.haqim.storyapp.data.repository

import dev.haqim.storyapp.data.mechanism.NetworkBoundResource
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.response.*
import dev.haqim.storyapp.model.BasicMessage
import dev.haqim.storyapp.model.Login
import dev.haqim.storyapp.model.Story
import dev.haqim.storyapp.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class StoryRepository(
    private val remoteDataSource: RemoteDataSource,
    private val userPreference: UserPreference
) {
    fun register(
        name: String, email: String, password: String
    ): Flow<Resource<BasicMessage>> {
        return object : NetworkBoundResource<BasicMessage, BasicResponse>(){
            override fun createCall(): Flow<Result<BasicResponse>> {
                return remoteDataSource.register( name, email, password)
            }
    
            override fun loadFromNetwork(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(data.toModel())
            }
    
            override suspend fun saveCallResult(data: BasicResponse) {}
    
        }.asFlow()
    }

    fun login(email: String, password: String): Flow<Resource<Login>> {
        return object : NetworkBoundResource<Login, LoginResponse>(){
            override fun createCall(): Flow<Result<LoginResponse>> {
                return remoteDataSource.login(email, password)
            }

            override fun loadFromNetwork(data: LoginResponse): Flow<Login> {
                return flowOf(data.toModel())
            }

            override suspend fun saveCallResult(data: LoginResponse) {
                // save result to user preference
                userPreference.saveUser(
                    data.toUser(
                        email = email,
                        password = password,
                        hasLogin = true
                    )
                )
            }

        }.asFlow()
    }

    fun getUser(): Flow<User> {
        return userPreference.getUser()
    }

    fun getStories(page: Int, size: Int, location: Boolean, token: String): Flow<Resource<List<Story>?>> {
        return object: NetworkBoundResource<List<Story>?, StoriesResponse>(){

            override fun createCall(): Flow<Result<StoriesResponse>> {
                return remoteDataSource.getStories(page, size, location, token)
            }

            override suspend fun saveCallResult(data: StoriesResponse) {}

            override fun loadFromNetwork(data: StoriesResponse): Flow<List<Story>?> {
                return flowOf(data.toModel())
            }

        }.asFlow()
    }

    fun addStory(
        token: String,
        file: File,
        description: String,
        lon: Float? = null,
        lat: Float? = null
    ): Flow<Resource<BasicMessage>> {
        return object: NetworkBoundResource<BasicMessage, BasicResponse>(){

            override fun createCall(): Flow<Result<BasicResponse>> {
                val requestImageFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartFile = MultipartBody.Part.createFormData(
                    "photo",
                    file.name,
                    requestImageFile
                )
                val descriptionRequestBody =
                    description.toRequestBody("text/plain".toMediaType())
                return remoteDataSource.addStory(
                    token = token,
                    file = multipartFile,
                    description = descriptionRequestBody,
                    lon = lon?.toString()?.toRequestBody(),
                    lat = lat?.toString()?.toRequestBody()
                )
            }

            override suspend fun saveCallResult(data: BasicResponse) {}

            override fun loadFromNetwork(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(data.toModel())
            }

        }.asFlow()
    }

    suspend fun logout(){
        userPreference.logout()
    }
}