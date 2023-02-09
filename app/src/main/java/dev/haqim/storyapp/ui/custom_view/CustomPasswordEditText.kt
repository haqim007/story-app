package dev.haqim.storyapp.ui.custom_view

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dev.haqim.storyapp.R


class CustomPasswordEditText: TextInputLayout{
    private lateinit var textInputEditText: TextInputEditText

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
        endIconMode = END_ICON_PASSWORD_TOGGLE
        hint = context.getString(R.string.password)
        contentDescription = context.getString(R.string.type_password_here)
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
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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
            setErrorMessage(context.getString(R.string.password_is_required))
            return false
        }

        if(this.textInputEditText.text != null && this.textInputEditText.text!!.length < 8){
            setErrorMessage(
                context.getString(R.string.password_length_min_8_chars)
            )
            return false
        }

        setErrorMessage()
        return true
    }

    fun isValid(): Boolean{

        if(this.textInputEditText.text != null && this.textInputEditText.text!!.isBlank()){
            return false
        }

        if(this.textInputEditText.text != null && this.textInputEditText.text!!.length < 8){
            return false
        }

        return true
    }

    fun setText(text: String?){
        textInputEditText.setText(text)
    }

    fun setErrorMessage(message: String? = null){
        isErrorEnabled = !message.isNullOrBlank()
        error = message
    }


}