package dev.haqim.storyapp.domain.model

data class BasicMessage(
    override val error: Boolean,
    override val message: String?
): ABasicModel()