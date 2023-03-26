package dev.haqim.storyapp.util

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import dev.haqim.storyapp.data.local.entity.StoryEntity
import dev.haqim.storyapp.data.remote.response.StoryResponse
import dev.haqim.storyapp.data.remote.response.toEntity
import kotlinx.coroutines.flow.Flow


class StoryRemoteMediatorMock: PagingSource<Int, Flow<List<StoryEntity>>>(){

    override fun getRefreshKey(state: PagingState<Int, Flow<List<StoryEntity>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Flow<List<StoryEntity>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

    companion object{
        fun snapshot(items:  List<StoryResponse>): PagingData<StoryEntity> {
            return PagingData.from(items.toEntity())
        }
    }

}