package dev.haqim.storyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.ui.add_story.AddStoryViewModel
import dev.haqim.storyapp.ui.login.LoginViewModel
import dev.haqim.storyapp.ui.main.MainViewModel
import dev.haqim.storyapp.ui.map.StoryMapViewModel
import dev.haqim.storyapp.ui.registration.RegistrationViewModel

class StoryViewModelProvider(private val storyUseCase: StoryUseCase) :
    ViewModelProvider.NewInstanceFactory() {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RegistrationViewModel::class.java) -> {
                RegistrationViewModel(storyUseCase) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(storyUseCase) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(storyUseCase) as T
            }
            modelClass.isAssignableFrom(AddStoryViewModel::class.java) -> {
                AddStoryViewModel(storyUseCase) as T
            }
            modelClass.isAssignableFrom(StoryMapViewModel::class.java) -> {
                StoryMapViewModel(storyUseCase) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}