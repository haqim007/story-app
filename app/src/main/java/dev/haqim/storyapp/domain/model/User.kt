package dev.haqim.storyapp.domain.model

data class User(
    val name: String,
    val password: String,
    val email: String,
    val hasLogin: Boolean,
    val token: String,
    val id: String
)
