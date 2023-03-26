package dev.haqim.storyapp.data.remote.response

import android.icu.text.SimpleDateFormat
import com.google.gson.annotations.SerializedName
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.domain.model.Story
import dev.haqim.storyapp.helper.util.TimeAgo
import java.util.*

data class StoriesResponse(

	@field:SerializedName("listStory")
	val listStory: List<StoryResponse>,

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class StoryResponse(

	@field:SerializedName("photoUrl")
	val photoUrl: String,

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("description")
	val description: String,

	@field:SerializedName("lon")
	val lon: Double? = null,

	@field:SerializedName("id")
	val id: String,

	@field:SerializedName("lat")
	val lat: Double? = null
)

fun StoriesResponse.toModel(): List<Story>?{
	return this.listStory.map {
		val createdAtDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
			it.createdAt
		)
		Story(
			photoUrl = it.photoUrl,
			description = it.description,
			createdAt = TimeAgo().getTimeAgo(createdAtDate.time) ?: "",
			name = it.name,
			id = it.id,
			lon = it.lon,
			lat = it.lat
		)
	}
}

fun StoryResponse.toEntity() =
	StoryEntity(
		id, photoUrl, createdAt, name, description, lon, lat
	)

fun List<StoryResponse>?.toEntity(): List<StoryEntity> {
	return this?.map {
		it.toEntity()
	} ?: listOf()
}

