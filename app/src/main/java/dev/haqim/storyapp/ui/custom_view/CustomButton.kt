package dev.haqim.storyapp.ui.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import dev.haqim.storyapp.R

class CustomButton: FrameLayout {
    private lateinit var tvText: TextView
    private lateinit var loading: ProgressBar
    private lateinit var cl: ConstraintLayout
    private var isEnable: Boolean = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init(){
        inflate(context, R.layout.custom_button, this)

        tvText = findViewById(R.id.tvCustomButton)
        loading = findViewById(R.id.loadingCustomButton)
        cl = findViewById(R.id.clCustomButton)
    }
    fun setText(text: String) {
        tvText.text = text
    }

    fun setAllCaps(value: Boolean = true){
        tvText.isAllCaps = value
    }

    fun setEnable(enable:Boolean){
        isEnable = enable
        if (enable){
            alpha = 1.0F
            isClickable = true
            isEnabled = true
        }else{
            alpha = 0.5F
            isClickable = false
            isEnabled = false
        }
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            tvText.visibility = View.GONE
            loading.visibility = View.VISIBLE
        } else {
            tvText.visibility = View.VISIBLE
            loading.visibility = View.GONE
        }
    }


}