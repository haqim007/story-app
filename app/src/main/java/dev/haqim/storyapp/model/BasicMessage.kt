package dev.haqim.storyapp.model

data class BasicMessage(
    override val error: Boolean,
    override val message: String?
): ABasicModel()