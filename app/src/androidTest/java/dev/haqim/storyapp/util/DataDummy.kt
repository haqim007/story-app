package dev.haqim.storyapp.util

import dev.haqim.storyapp.data.local.entity.RemoteKeys
import dev.haqim.storyapp.data.local.entity.StoryEntity

object DataDummy {
    fun storiesEntity(): List<StoryEntity> {
        return listOf(
            StoryEntity(
                id = "story-D6Fq_NtzIz1RoKiL",
                name = "kanae",
                description = "kuching",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677144192935_-rtbXhzI.jpg",
                createdAt = "2023-02-23T09:23:12.937Z",
                lon = null,
                lat = null
            ),

            StoryEntity(
                id =     "story-zMP2ZH9iYyaYkmIy",
                name = "kanae",
                description = "kkkk",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677142584594_djDEwicp.jpg",
                createdAt = "2023-02-23T08:56:24.595Z",
                lon = null,
                lat = null
            ),

            StoryEntity(
                id = "story-qKXkMJMAd3lKOVvV",
                name = "kanae",
                description = "lampu\n\n",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677132719125_xfPFuuic.jpg",
                createdAt = "2023-02-23T06:11:59.128Z",
                lon = null,
                lat = null
            ),
            StoryEntity(
                id = "story-x8JcCsi8cyAMxivB",
                name = "kanae",
                description = "asasdasd",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677131648991_ci8hZv09.jpg",
                createdAt = "2023-02-23T05:54:08.998Z",
                lon = null,
                lat = null
            ),
            
            StoryEntity(
                id = "story-x8JcCsi8cyAMx0vB",
                name = "kanae sip",
                description = "asasdasd",
                photoUrl = "https://story-api.dicoding.dev/images/stories/photos-1677131648991_ci8hZv09.jpg",
                createdAt = "2023-02-23T05:54:08.998Z",
                lon = null,
                lat = null
            )

        )
    }

    fun remoteKeys() = listOf(
        RemoteKeys("1", null, 5),
        RemoteKeys("2", null, 2),
        RemoteKeys("3", null, 3),
        RemoteKeys("4", null, 4)
    )
}