package dev.haqim.storyapp.data.remote.response

import com.google.gson.annotations.SerializedName
import dev.haqim.storyapp.model.BasicMessage

data class BasicResponse(

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String?
)

fun BasicResponse.toModel() =
	BasicMessage(
		error = this.error,
		message = this.message
	)
