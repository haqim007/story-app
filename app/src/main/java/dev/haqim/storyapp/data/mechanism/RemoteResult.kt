package dev.haqim.storyapp.data.mechanism

import com.google.gson.Gson
import dev.haqim.storyapp.data.remote.response.BasicResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

fun <T> remoteResult(callback: suspend () -> T): Flow<HttpResult<T>> {
    return  flow {
        try {
            val response = callback()
            emit(HttpResult.Success(response))
        }catch (e: HttpException){
            val error = e.response()?.errorBody()?.string()
            error?.let {
                val response = parseError(error)
                emit(onFailure(response?.message ?: "", e.code()))
            } ?: emit(onFailure(e.localizedMessage ?: ""))
        }
    }
}

fun <T> onFailure(message: String, code: Int? = null): HttpResult<T> =
    if(code == 401) HttpResult.Unauthorized(message, code)
    else HttpResult.Error(message, code)

private fun parseError(error: String?): BasicResponse? {
    return Gson().fromJson(error, BasicResponse::class.java)
}