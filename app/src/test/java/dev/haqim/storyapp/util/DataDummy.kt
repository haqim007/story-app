package dev.haqim.storyapp.util

import androidx.annotation.VisibleForTesting
import androidx.paging.PagingData
import androidx.paging.map
import dev.haqim.storyapp.data.local.entity.RemoteKeys
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.local.entity.toModel
import dev.haqim.storyapp.data.remote.response.*
import dev.haqim.storyapp.domain.model.BasicMessage
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.domain.model.User
import dev.haqim.storyapp.helper.util.TimeAgo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.File


@VisibleForTesting
object DataDummy {
    fun user(hasLogin: Boolean = true) = if(hasLogin){
        User(
            name = "haqim",
            password = "",
            email = "haqim@mail.com",
            hasLogin = hasLogin,
            token = "lorem ipsum",
            id = "212"
        )
    }else{
        User(
            name = "",
            password = "",
            email = "",
            hasLogin = hasLogin,
            token = "",
            id = ""
        )
    }

    fun stories(): List<Story> {
        return listOf(
            Story(
                id = "story-D6Fq_NtzIz1RoKiL",
                name = "kanae",
                description = "kuching",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677144192935_-rtbXhzI.jpg",
                createdAt = "2023-02-23T09:23:12.937Z",
                lon = null,
                lat = null
        ),

        Story(
            id =     "story-zMP2ZH9iYyaYkmIy",
            name = "kanae",
            description = "kkkk",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677142584594_djDEwicp.jpg",
            createdAt = "2023-02-23T08:56:24.595Z",
            lon = null,
            lat = null
        ),

        Story(
            id = "story-qKXkMJMAd3lKOVvV",
            name = "kanae",
            description = "lampu\n\n",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677132719125_xfPFuuic.jpg",
            createdAt = "2023-02-23T06:11:59.128Z",
            lon = null,
            lat = null
        ),
        Story(
            id = "story-x8JcCsi8cyAMxivB",
            name = "kanae",
            description = "asasdasd",
            photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677131648991_ci8hZv09.jpg",
            createdAt = "2023-02-23T05:54:08.998Z",
            lon = null,
            lat = null
        )

        )
    }

    fun listStoryResponse(): List<StoryResponse> {
        return listOf(
            StoryResponse(
                id = "story-D6Fq_NtzIz1RoKiL",
                name = "kanae",
                description = "kuching",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677144192935_-rtbXhzI.jpg",
                createdAt = "2023-02-23T09:23:12.937Z",
                lon = null,
                lat = null
            ),

            StoryResponse(
                id =     "story-zMP2ZH9iYyaYkmIy",
                name = "kanae",
                description = "kkkk",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677142584594_djDEwicp.jpg",
                createdAt = "2023-02-23T08:56:24.595Z",
                lon = -2323.0,
                lat = 23232.0
            ),

            StoryResponse(
                id = "story-qKXkMJMAd3lKOVvV",
                name = "kanae",
                description = "lampu\n\n",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677132719125_xfPFuuic.jpg",
                createdAt = "2023-02-23T06:11:59.128Z",
                lon = -2323.0,
                lat = 23232.0
            ),
            StoryResponse(
                id = "story-x8JcCsi8cyAMxivB",
                name = "kanae",
                description = "asasdasd",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677131648991_ci8hZv09.jpg",
                createdAt = "2023-02-23T05:54:08.998Z",
                lon = -2323.0,
                lat = 23232.0
            )

        )
    }
    
    fun List<StoryResponse>.toModel(): List<Story> {
        return this.map { 
            Story(
                photoUrl = it.photoUrl,
                id = it.id,
                description = it.description,
                name = it.name,
                createdAt = TimeAgo().getTimeAgo(it.createdAt) ?: "",
                lat = it.lat,
                lon = it.lon,
            )
        }
    }
    
    fun basicMessageSuccess() = BasicMessage(
        error = false,
        message = "Success"
    )

    fun login() = Login(
        name = "haqim",
        userId = "11111",
        token = "11111",
        error = false,
        message = null
    )
    
    fun file(extension: String = ".txt"): File = File.createTempFile("test", extension)
    
    fun pagingDataStoriesFlow(showEmpty: Boolean = false): Flow<PagingData<Story>> {
        val dummyStoriesResponse = if (!showEmpty) listStoryResponse() else listOf()
        val data: PagingData<StoryEntity> = StoryRemoteMediatorMock.snapshot(dummyStoriesResponse)
        return flowOf(data).map { it.map { storyEntity -> storyEntity.toModel() } }
    }

    fun pagingDataStories(showEmpty: Boolean = false): PagingData<StoryEntity> {
        val dummyStoriesResponse = if (!showEmpty) listStoryResponse() else listOf()
        return StoryRemoteMediatorMock.snapshot(dummyStoriesResponse)
    }
    
    fun basicResponseSuccess(message: String = "Success") = BasicResponse(
        error = false,
        message = message
    )

    fun basicResponseError(message: String = "Error") = Throwable(message)
    
    fun loginResponse() = LoginResponse(
        loginResult = LoginResultResponse(
            name = "Haqim",
            userId = "11",
            token = "1111"
        ),
        error = false,
        message = "Success"
    )
    
    fun storiesResponse() = StoriesResponse(
        listStory = listStoryResponse(),
        error = false,
        message = ""
    )
    
    fun remoteKeys() = listOf(
        RemoteKeys("1", null, 5),
        RemoteKeys("2", null, 2),
        RemoteKeys("3", null, 3),
        RemoteKeys("4", null, 4)
    )
    
}