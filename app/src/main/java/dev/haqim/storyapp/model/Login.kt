package dev.haqim.storyapp.model

data class Login(
    val name: String,
    val userId: String,
    val token: String,
    override val error: Boolean,
    override val message: String?
): ABasicModel()