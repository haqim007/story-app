package dev.haqim.storyapp.data.mechanism

import dev.haqim.storyapp.helper.util.wrapEspressoIdlingResource
import kotlinx.coroutines.flow.*

/*
* 
* RequestType: Data type that used to catch network response a.k.a inserted data type
* ResultType: Data type that expected as return data a.k.a output data type
* */
abstract class NetworkBoundResource<ResultType, RequestType> {

    private val result: Flow<Resource<ResultType>> = flow{

        emit(Resource.Loading())
        wrapEspressoIdlingResource {
            try {
                when(val apiResponse = fetchFromRemote().first()){
                    is HttpResult.Success -> {
                        apiResponse.data?.let {
                            saveRemoteData(it)
                            emitAll(
                                loadResultData(it).map {res ->
                                    Resource.Success(res)
                                }
                            )
                        }
                    }
                    is HttpResult.Unauthorized -> {
                        onFetchFailed()
                        resetUserAuth()
                        emit(Resource.Error(
                            message = apiResponse.message ?: "Unknown error"
                        ))
                    }
                    else -> {
                        onFetchFailed()
                        emit(Resource.Error(
                            message = apiResponse.message ?: "Unknown error"
                        ))
                    }
                }
            }catch (e: Exception){
                onFetchFailed()
                emit(Resource.Error(
                    message = e.localizedMessage ?: "Unknown error"
                ))
            }
        }
    }
    
    protected abstract fun fetchFromRemote(): Flow<HttpResult<RequestType>>
    
    protected abstract fun loadResultData(data: RequestType): Flow<ResultType>
    
    protected abstract suspend fun saveRemoteData(data: RequestType)
    
    protected abstract suspend fun resetUserAuth()
    
    protected open fun onFetchFailed() {}
    fun asFlow(): Flow<Resource<ResultType>> = result
    
}