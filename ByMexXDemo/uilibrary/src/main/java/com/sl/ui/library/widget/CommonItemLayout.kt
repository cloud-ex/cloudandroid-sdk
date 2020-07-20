package com.sl.ui.library.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sl.ui.library.R
import com.sl.ui.library.utils.setDrawableLeft
import kotlinx.android.synthetic.main.view_common_item_layout.view.*
import kotlinx.android.synthetic.main.view_title_bar_layout.view.*
import kotlinx.android.synthetic.main.view_title_bar_layout.view.tv_right_text
import org.jetbrains.anko.textColor

/**
 * 通用设置Item
 */
class CommonItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    /**
     * 左边文本
     */
    var itemLeftText = ""
        set(value) {
            field = value
            tv_left_text.text = value
        }

    /**
     * 右边文本
     */
    var itemRightText = ""
    set(value) {
        field = value
        tv_right_text.text = value
    }
    /**
     * 是否是警告状态
     */
    var rightWarn = false
        set(value) {
            field = value
            if(value){
                tv_right_text.textColor = resources.getColor(R.color.main_red)
                tv_right_text.setDrawableLeft(R.drawable.icon_red_warn)
            }else{
                tv_right_text.textColor = resources.getColor(R.color.hint_color)
                tv_right_text.setDrawableLeft(0)
            }
        }
    var itemChecked = false
        set(value) {
            field = value
            item_switch.isChecked = value
        }
    /**
     * 是否是选择类型
     */
    var isCheckWidget = false
        set(value) {
            field = value
            if(isCheckWidget){
                item_switch.visibility = View.VISIBLE
                ll_right_warp_layout.visibility = View.GONE
            }else{
                item_switch.visibility = View.GONE
                ll_right_warp_layout.visibility = View.VISIBLE
            }
        }

    /**
     * item的背景色
     */
    var itemBgColor = 0
        set(value) {
            field = value
            if(value != 0){
                rl_item_layout.setBackgroundColor(itemBgColor)
            }
        }

    /**
     * 点击事件
     */
    fun setItemOnClickListener(listener: OnClickListener){
        rl_item_layout?.setOnClickListener(listener)
    }

    /**
     * 左边icon
     */
    fun setLeftIcon(resId:Int){
        iv_left_icon.visibility = View.VISIBLE
        iv_left_icon.setImageResource(resId)
    }

    fun setLeftIcon(netImg:String,defaultIcon:Int){
        iv_left_icon.visibility = View.VISIBLE
        val options = RequestOptions()
        options.placeholder(defaultIcon)
        Glide.with(context).load(netImg).apply(options).into(iv_left_icon)
    }

    fun setItemOnCheckedChangeListener(listener: CompoundButton.OnCheckedChangeListener){
        item_switch?.setOnCheckedChangeListener(listener)
    }
    fun setItemOnCheckedClickListener(listener: OnClickListener){
        item_switch?.setOnClickListener(listener)
    }

    fun isCheck():Boolean{
        return item_switch.isChecked
    }

    init {
        initView(context)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.commonItemLayout)
        itemLeftText = typedArray.getString(R.styleable.commonItemLayout_itemLeftText).orEmpty()
        itemRightText = typedArray.getString(R.styleable.commonItemLayout_itemRightText).orEmpty()
        isCheckWidget = typedArray.getBoolean(R.styleable.commonItemLayout_isCheckWidget,false)
        itemBgColor = typedArray.getColor(R.styleable.commonItemLayout_itemBgColor,0)

        typedArray.recycle()
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.view_common_item_layout, this, true)

    }
}