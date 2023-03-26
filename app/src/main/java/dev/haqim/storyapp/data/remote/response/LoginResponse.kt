package dev.haqim.storyapp.data.remote.response

import com.google.gson.annotations.SerializedName
import dev.haqim.storyapp.domain.model.Login
import dev.haqim.storyapp.domain.model.User

data class LoginResponse(

	@field:SerializedName("loginResult")
	val loginResult: LoginResultResponse,

	@field:SerializedName("error")
	val error: Boolean,

	@field:SerializedName("message")
	val message: String
)

data class LoginResultResponse(

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("userId")
	val userId: String,

	@field:SerializedName("token")
	val token: String
)

fun LoginResponse.toModel() =
	Login(
		error = this.error,
		message = this.message,
		name = this.loginResult.name,
		token = "Bearer ${this.loginResult.token}",
		userId = this.loginResult.userId
	)

fun LoginResponse.toUser(email: String, password: String, hasLogin: Boolean) = User(
	name = this.loginResult.name,
	password = password,
	email = email,
	hasLogin = hasLogin,
	token = "Bearer ${this.loginResult.token}",
	id = this.loginResult.userId
)