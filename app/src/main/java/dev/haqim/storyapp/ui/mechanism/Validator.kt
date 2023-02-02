package dev.haqim.storyapp.ui.mechanism

import android.text.TextUtils
import android.util.Patterns

fun isValidEmail(target: CharSequence, isRequired: Boolean = true): ResultInput<String> {
    return if (isRequired && TextUtils.isEmpty(target)) {
        ResultInput.Invalid(validation = InputValidation.RequiredFieldInvalid)
    } else if(!Patterns.EMAIL_ADDRESS.matcher(target).matches()){
        ResultInput.Invalid(validation = InputValidation.EmailInvalid)
    }else{
        ResultInput.Valid(data = target.toString())
    }
}

fun isValidRequiredField(target: CharSequence): ResultInput<String> {
    return if (TextUtils.isEmpty(target)) {
        ResultInput.Invalid(validation = InputValidation.RequiredFieldInvalid)
    }else{
        ResultInput.Valid(data = target.toString())
    }
}

fun isValidPassword(target: CharSequence): ResultInput<String> {
    return if (TextUtils.isEmpty(target)) {
        ResultInput.Invalid(validation = InputValidation.RequiredFieldInvalid)
    } else if(target.toString().length < 8){
        ResultInput.Invalid(validation = InputValidation.PasswordLessThanEight)
    }else{
        ResultInput.Valid(data = target.toString())
    }
}