package dev.haqim.storyapp.data.mechanism



sealed class HttpResult<T>(
    val data: T? = null, val message: String? = null, val code: Int? = 200
) {
    class Success<T>(data: T) : HttpResult<T>(data)
    class Error<T>(message: String, code: Int? = null) : HttpResult<T>(message = message, code = code)
    class Unauthorized<T>(message: String, code: Int) : HttpResult<T>(message = message, code = code)
}

