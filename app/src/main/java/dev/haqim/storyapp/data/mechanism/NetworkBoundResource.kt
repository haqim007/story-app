package dev.haqim.storyapp.data.mechanism

import kotlinx.coroutines.flow.*

abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result: Flow<Resource<ResultType>> = flow{
        emit(Resource.Loading())
        val apiResponse = createCall().first()
        if(apiResponse.isSuccess){
            apiResponse.getOrNull()?.let { res ->
                saveCallResult(res)
                emitAll(
                    loadFromNetwork(res).map {
                        Resource.Success(it)
                    }
                )
            }

        }else{
            onFetchFailed()
            emit(Resource.Error(
                message = apiResponse
                    .exceptionOrNull()
                    ?.localizedMessage ?: "Unknown error"
            ))
        }
    }
    
    protected abstract fun createCall(): Flow<Result<RequestType>>
    
    protected abstract fun loadFromNetwork(data: RequestType): Flow<ResultType>
    
    protected abstract suspend fun saveCallResult(data: RequestType)
    
    protected open fun onFetchFailed() {}
    fun asFlow(): Flow<Resource<ResultType>> = result
    
}