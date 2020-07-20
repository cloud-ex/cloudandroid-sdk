package com.sl.ui.library.widget

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.sl.ui.library.R
import kotlinx.android.synthetic.main.view_title_bar_layout.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.textColor

/**
 * 通用标题栏
 */
class TitleBarLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var title = ""
        set(value) {
            field = value
            if(TextUtils.isEmpty(value)){
                tv_title.visibility = View.GONE
            }else{
                tv_title.visibility = View.VISIBLE
                tv_title.text = value
            }
        }

    /**
     * 右边文本
     */
    var rightText = ""
    set(value) {
        field = value
        if(TextUtils.isEmpty(value)){
            tv_right_text.visibility = View.GONE
        }else{
            tv_right_text.visibility = View.VISIBLE
            tv_right_text.text = value
        }
    }
    /**
     * 右边文本颜色
     */
    var rightTextColor = 0
        set(value) {
            field = value
            if(rightTextColor !=0){
                tv_right_text.setTextColor(rightTextColor)
            }
        }

    /**
     * 右边icon
     */
    var rightIcon : Drawable?=null
        set(value) {
            field = value
            if(value != null){
                iv_right_icon.setImageDrawable(value)
            }
        }
    /**
     * 左边按钮点击事件
     */
    fun setLeftOnClickListener(listener: OnClickListener){
        iv_left?.setOnClickListener(listener)
    }

    /**
     * 设置右边区域按钮点击事件
     */
    fun setRightOnClickListener(listener: OnClickListener){
        tv_right_text?.setOnClickListener(listener)
        iv_right_icon?.setOnClickListener(listener)
    }

    init {
        initView(context)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.titleBarLayout)
        title = typedArray.getString(R.styleable.titleBarLayout_bar_title).orEmpty()
        rightText = typedArray.getString(R.styleable.titleBarLayout_bar_right_text).orEmpty()
        rightTextColor = typedArray.getColor(R.styleable.titleBarLayout_bar_right_color,
            ContextCompat.getColor(context,R.color.main_yellow))
        rightIcon = typedArray.getDrawable(R.styleable.titleBarLayout_bar_right_icon)

        typedArray.recycle()
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_title_bar_layout, this, true)

        setLeftOnClickListener(OnClickListener {
            var activity = context as Activity
            activity.finish()
        })
    }
}