package dev.haqim.storyapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.helper.util.TimeAgo

const val TABLE_STORIES = "stories"
@Entity(tableName = TABLE_STORIES)
data class StoryEntity(
    @PrimaryKey val id: String,
    val photoUrl: String,
    val createdAt: String,
    val name: String,
    val description: String,
    val lon: Double? = null,
    val lat: Double? = null
)

fun StoryEntity.toModel() = 
    Story(
        photoUrl, TimeAgo().getTimeAgo(createdAt) ?: "", name, description, lon, id, lat
    )

fun List<StoryEntity>.toModel(): List<Story> {
    return this.map { it.toModel() }
}