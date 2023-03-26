package dev.haqim.storyapp.data.repository

import androidx.paging.*
import dev.haqim.storyapp.data.local.LocalDataSource
import dev.haqim.storyapp.data.local.entity.toModel
import dev.haqim.storyapp.data.mechanism.NetworkBoundResource
import dev.haqim.storyapp.data.mechanism.Resource
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.response.*
import dev.haqim.storyapp.data.remoteMediator.StoryRemoteMediator
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.domain.repository.IStoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.File

class StoryRepository(
    private val remoteDataSource: RemoteDataSource,
    private val userPreference: UserPreference,
    private val localDataSource: LocalDataSource,
    private val remoteMediator: StoryRemoteMediator
): IStoryRepository {
    override fun register(
        name: String, email: String, password: String
    ): Flow<Resource<BasicMessage>> {
        return object : NetworkBoundResource<BasicMessage, BasicResponse>(){
            override fun fetchFromRemote(): Flow<Result<BasicResponse>> {
                return remoteDataSource.register( name, email, password)
            }
    
            override fun loadResultData(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(data.toModel())
            }
    
            override suspend fun saveRemoteData(data: BasicResponse) {}
    
        }.asFlow()
    }

    override fun login(email: String, password: String): Flow<Resource<Login>> {
        return object : NetworkBoundResource<Login, LoginResponse>(){
            override fun fetchFromRemote(): Flow<Result<LoginResponse>> {
                return remoteDataSource.login(email, password)
            }

            override fun loadResultData(data: LoginResponse): Flow<Login> {
                return flowOf(data.toModel())
            }

            override suspend fun saveRemoteData(data: LoginResponse) {
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

    override fun getUser(): Flow<User> {
        return userPreference.getUser()
    }

    override fun getStoriesWithLocation(pageSize: Int, page: Int): Flow<Resource<List<Story>>> {
        return object: NetworkBoundResource<List<Story>, StoriesResponse>(){

            override fun fetchFromRemote(): Flow<Result<StoriesResponse>> {
                return remoteDataSource.getStories(page, pageSize, 1)
            }

            override suspend fun saveRemoteData(data: StoriesResponse) {
                val storyEntities = data.listStory.toEntity()
                localDataSource.insertAllStories(storyEntities)
            }

            override fun loadResultData(data: StoriesResponse): Flow<List<Story>> {
                return localDataSource.getAllStoriesWithLocation().map { it.toModel() }
            }

        }.asFlow()
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getStories(): Flow<PagingData<Story>> {
        return Pager(
            config = PagingConfig(
                pageSize = 30
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = {
                localDataSource.getAllStories()
            }
        ).flow.map { pagingData ->
            pagingData.map { storyEntity ->
                storyEntity.toModel()
            }
        }
    }

    override fun addStory(
        file: File,
        description: String,
        lon: Float?,
        lat: Float?
    ): Flow<Resource<BasicMessage>> {
        return object: NetworkBoundResource<BasicMessage, BasicResponse>(){

            override fun fetchFromRemote(): Flow<Result<BasicResponse>> {
                return remoteDataSource.addStory(
                    file = file,
                    description = description,
                    lon = lon,
                    lat = lat
                )
            }

            override suspend fun saveRemoteData(data: BasicResponse) {}

            override fun loadResultData(data: BasicResponse): Flow<BasicMessage> {
                return flowOf(data.toModel())
            }

        }.asFlow()
    }

    override suspend fun logout(){
        userPreference.logout()
    }
}