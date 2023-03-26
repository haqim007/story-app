package dev.haqim.storyapp.helper.util

fun isValidRequiredField(target: CharSequence): ResultInput<String> {
    return if (target.isEmpty()) {
        ResultInput.Invalid(validation = InputValidation.RequiredFieldInvalid)
    }else{
        ResultInput.Valid(data = target.toString())
    }
}

