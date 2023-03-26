package dev.haqim.storyapp.helper.util

sealed class ResultInput<T> {
    open val data:T? = null
    open val validation: InputValidation = InputValidation.Valid

    class Idle<T>: ResultInput<T>()
    data class Valid<T>(override val data: T?): ResultInput<T>()
    data class Invalid<T>(
        override val validation: InputValidation,
        override val data: T? = null
    ): ResultInput<T>()
}