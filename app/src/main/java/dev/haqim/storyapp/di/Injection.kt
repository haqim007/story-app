package dev.haqim.storyapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dev.haqim.storyapp.data.local.LocalDataSource
import dev.haqim.storyapp.data.local.room.StoryDatabase
import dev.haqim.storyapp.data.preferences.UserPreference
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.network.ApiConfig
import dev.haqim.storyapp.data.remote.network.ApiService
import dev.haqim.storyapp.data.remoteMediator.StoryRemoteMediator
import dev.haqim.storyapp.data.repository.StoryRepository
import dev.haqim.storyapp.domain.repository.IStoryRepository
import dev.haqim.storyapp.domain.usecase.StoryInteractor
import dev.haqim.storyapp.domain.usecase.StoryUseCase
import dev.haqim.storyapp.ui.StoryViewModelProvider

object Injection {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    fun provideViewModelProvider(context: Context): StoryViewModelProvider {
        val remoteDataSource = provideRemoteDataSource(context)
        val userPreference = provideUserPreference(context)
        val localDataSource = provideLocalDataSource(context)
        val remoteMediator: StoryRemoteMediator = provideStoryRemoteMediator(
            localDataSource, remoteDataSource
        )
        val repository = provideRepository(
            remoteDataSource, userPreference, localDataSource, remoteMediator
        )
        return StoryViewModelProvider(
            provideUseCase(repository)   
        )
    }

    private fun provideStoryRemoteMediator(
        localDataSource: LocalDataSource,
        remoteDataSource: RemoteDataSource
    ) = StoryRemoteMediator(localDataSource, remoteDataSource)

    private fun provideRepository(
        remoteDataSource: RemoteDataSource,
        userPreference: UserPreference,
        localDataSource: LocalDataSource,
        remoteMediator: StoryRemoteMediator
    ): IStoryRepository {
        return StoryRepository(
            remoteDataSource,
            userPreference,
            localDataSource,
            remoteMediator
        )
    }
    
    private fun provideDatabase(context: Context): StoryDatabase {
        return StoryDatabase.getInstance(context)
    }
    private fun provideLocalDataSource(context: Context): LocalDataSource{
        return LocalDataSource.getInstance(provideDatabase(context))
    }
    
    private fun provideUseCase(repository: IStoryRepository): StoryUseCase{
        return StoryInteractor(repository)
    }

    private fun provideRemoteDataSource(context: Context): RemoteDataSource {
        val service = ApiConfig.createService(ApiService::class.java)
        return RemoteDataSource(service = service, userPreference = provideUserPreference(context))
    }

    @Volatile private var userPreference: UserPreference? = null
    private fun provideUserPreference(context: Context): UserPreference{
        return userPreference ?: synchronized(this) {
            userPreference ?: UserPreference.getInstance(context.dataStore)
        }
    }
}