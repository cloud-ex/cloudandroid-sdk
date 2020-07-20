package com.sl.ui.library.widget

import android.content.Context
import android.util.AttributeSet

class LoadingLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {
    private var progressDrawable : ProgressDrawable?=null

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressDrawable?.stop()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        progressDrawable?.start()
    }

    init {
         progressDrawable = ProgressDrawable()
         setImageDrawable(progressDrawable)
    }

}