package dev.haqim.storyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.haqim.storyapp.data.repository.StoryRepository
import dev.haqim.storyapp.ui.add_story.AddStoryViewModel
import dev.haqim.storyapp.ui.login.LoginViewModel
import dev.haqim.storyapp.ui.main.MainViewModel
import dev.haqim.storyapp.ui.registration.RegistrationViewModel

class StoryViewModelProvider(private val repository: StoryRepository) :
    ViewModelProvider.NewInstanceFactory() {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(repository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}