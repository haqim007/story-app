package dev.haqim.storyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.network.ApiConfig
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.data.repository.StoryRepository
import dev.haqim.storyapp.ui.StoryViewModelProvider

object Injection {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun provideViewModelProvider(context: Context): StoryViewModelProvider {
        val remoteDataSource = provideRemoteDataStore()
        val userPreference = provideUserPreference(context)
        return StoryViewModelProvider(
            provideRepository(
                remoteDataSource, userPreference
            )
        )
    }


    private fun provideRepository(
        remoteDataSource: RemoteDataSource,
        userPreference: UserPreference
    ): StoryRepository{
        return StoryRepository(
            remoteDataSource,
            userPreference
        )
    }

    private fun provideRemoteDataStore(): RemoteDataSource {
        val service = ApiConfig.createService(ApiService::class.java)
        return RemoteDataSource(service = service)
    }

    @Volatile private var userPreference: UserPreference? = null
    private fun provideUserPreference(context: Context): UserPreference{
        return userPreference ?: synchronized(this) {
            userPreference ?: UserPreference.getInstance(context.dataStore)
        }
    }
}