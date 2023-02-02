package dev.haqim.storyapp.ui.custom_view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.haqim.storyapp.R


class CustomEmailEditText: TextInputLayout{
    private lateinit var textInputEditText:  TextInputEditText

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        hint = context.getString(R.string.email)
        contentDescription = context.getString(R.string.type_email_here)
        initTextInputEditText(attrs)
    }

    private fun initTextInputEditText(attrs: AttributeSet?) {
        textInputEditText = TextInputEditText(context, attrs)
        textInputEditText.setEms(10)
        textInputEditText.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        textInputEditText.inputType =
            InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
        textInputEditText.setTextAppearance(R.style.TextInputEditAppearance)
        addView(textInputEditText)
    }

    fun doAfterTextChanged(callback: (text: Editable?, isValid: Boolean) -> Unit){
        textInputEditText.doAfterTextChanged { text ->
            callback(text, validate())
        }
    }

    fun validate(): Boolean{

        if(this.textInputEditText.text != null && this.textInputEditText.text!!.isBlank()){
            setErrorMessage(context.getString(R.string.email_is_required))
            return false
        }

        if(this.textInputEditText.text != null && !isEmailValid(this.textInputEditText.text!!.toString())){
            setErrorMessage(
                context.getString(R.string.email_format_is_invalid)
            )
            return false
        }

        setErrorMessage()
        return true
    }

    fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun setText(text: String?){
        textInputEditText.setText(text)
    }

    fun setErrorMessage(message: String? = null){
        isErrorEnabled = !message.isNullOrBlank()
        error = message
    }


}