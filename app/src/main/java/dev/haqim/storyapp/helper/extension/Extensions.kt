package dev.haqim.storyapp.helper.extension

import android.content.Context
import androidx.annotation.StringRes
import com.google.android.material.textfield.TextInputEditText
import dev.haqim.storyapp.R

fun TextInputEditText.isValidEmail(): Boolean{
    if(this.text != null && this.text!!.isBlank()){
        return false
    }
    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(this.text.toString()).matches()){
        return false
    }
    if(this.error != null){
        return false
    }
    return true
}

fun TextInputEditText.isRequiredFieldValid(): Boolean{
    if(this.text != null && this.text!!.isBlank()){
        return false
    }
    if(this.error != null){
        return false
    }
    return true
}

