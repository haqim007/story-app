package dev.haqim.storyapp.data.remoteMediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import dev.haqim.storyapp.data.local.LocalDataSource
import dev.haqim.storyapp.data.local.entity.RemoteKeys
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.remote.RemoteDataSource
import dev.haqim.storyapp.data.remote.response.toEntity
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val localDataSource: LocalDataSource,
    private val remoteDataSource: RemoteDataSource
): RemoteMediator<Int, StoryEntity>() {
    override suspend fun load(loadType: LoadType, state: PagingState<Int, StoryEntity>): MediatorResult {
        
        val page = when(loadType){
            LoadType.REFRESH -> {
                val remoteKeys: RemoteKeys? = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys: RemoteKeys? = getRemoteKeyForFirstItem(state)
                val prevKey: Int = remoteKeys?.prevKey ?:
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys: RemoteKeys? = getRemoteKeyForLastItem(state)
                val nextKey: Int = remoteKeys?.nextKey ?:
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        
        return try {
            val response = remoteDataSource.getStories(page, state.config.pageSize).first()
            val endOfPaginationReached = response.getOrNull()?.listStory?.isEmpty() ?: true
            
            val prevKey = if (page == 1) null else page - 1
            val nextKey = if (endOfPaginationReached) null else page + 1
            val stories = response.getOrNull()?.listStory
            val keys = stories?.map {
                RemoteKeys(id = it.id, prevKey, nextKey)
            } ?: listOf()

            localDataSource.insertKeysAndStories(
                keys,
                stories.toEntity(),
                loadType == LoadType.REFRESH
            )
            
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        }catch (e: Exception){
            MediatorResult.Error(e)
        }
        
    }


    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position -> 
            state.closestItemToPosition(position)?.id?.let { id -> 
                localDataSource.getRemoteKeysById(id)
            }
        }
    }
    
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys?{
        return state.pages
            .firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { data -> 
                localDataSource.getRemoteKeysById(data.id)
            }
    }
    
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys?{
        return state.pages.lastOrNull{it.data.isNotEmpty()}?.data?.lastOrNull()?.let { data ->
            localDataSource.getRemoteKeysById(data.id)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

}