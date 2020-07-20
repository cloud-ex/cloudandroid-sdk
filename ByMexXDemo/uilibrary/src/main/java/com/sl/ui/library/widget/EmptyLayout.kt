package com.sl.ui.library.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.sl.ui.library.R
import kotlinx.android.synthetic.main.view_empty_layout.view.*

class EmptyLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var text = ""
        set(value) {
            field = value
            if (!TextUtils.isEmpty(value)) {
                tv_text.text = value
            }
        }

    var imageIcon: Drawable? = null
        set(value) {
            field = value
            if (value != null) {
                iv_icon.setImageDrawable(value)
            }
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_empty_layout, this, true)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyLayout)
        text = typedArray.getString(R.styleable.EmptyLayout_empty_text).orEmpty()
        imageIcon = typedArray.getDrawable(R.styleable.EmptyLayout_empty_icon)

        typedArray.recycle()
    }

}