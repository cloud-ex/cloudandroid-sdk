package com.sl.contract.library.widget

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.sl.contract.library.R
import com.sl.ui.library.utils.showUnderLine
import kotlinx.android.synthetic.main.sl_up_down_item_layout.view.*

/**
 * 合约通用组件封装(上下结构)
 */
class ContractUpDownItemLayout : LinearLayout {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initAttrs(attrs)
    }

    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            var typedArray = context.obtainStyledAttributes(it, R.styleable.SlUpDownItemStyle, 0, 0)
            title = typedArray.getString(R.styleable.SlUpDownItemStyle_sl_itemTitle) ?: ""
            contentGravity = typedArray.getInt(R.styleable.SlUpDownItemStyle_contentGravity, 1)
            showExplain = typedArray.getBoolean(R.styleable.SlUpDownItemStyle_showExplain, false)
            explainText = typedArray.getString(R.styleable.SlUpDownItemStyle_explainText) ?: ""
            contentTextColor = typedArray.getInt(
                R.styleable.SlUpDownItemStyle_contentTextColor,
                ContextCompat.getColor(context, R.color.text_color)
            )
            typedArray.recycle()
            if (showExplain) {
                //  setExplainListener(OnClickListener { NewDialogUtils.showDialog(context, explainText, true, null, context.getString(R.string.common_text_tip), context.getString(R.string.sl_str_isee)) })
            }
        }
    }

    /**
     * 标题
     */
    var title = ""
        set(value) {
            field = value
            tv_title?.text = title
        }

    /**
     * 内容
     */
    var content = ""
        set(value) {
            field = value
            tv_value?.text = Html.fromHtml(content)
        }

    /**
     * 内容颜色
     */
    var contentTextColor = ContextCompat.getColor(context, R.color.text_color)
        set(value) {
            field = value
            tv_value?.setTextColor(value)
        }

    /**
     * 内容的排版方向   1 左边 2 右边
     */
    var contentGravity = 1
        set(value) {
            field = value
            when (contentGravity) {
                0 -> {
                    ll_warp_layout?.gravity = Gravity.CENTER
                }
                1 -> {
                    ll_warp_layout?.gravity = Gravity.LEFT
                }
                2 -> {
                    ll_warp_layout?.gravity = Gravity.RIGHT
                }
            }

        }

    /**
     * 是否展示说明
     */
    var showExplain = false
        set(value) {
            field = value
            if (showExplain) {
                // tv_title?.setCompoundDrawablesWithIntrinsicBounds(null, null, resources.getDrawable(R.drawable.sl_contract_prompt), null)
            } else {
                tv_title?.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
            }
        }

    /**
     * 说明文本
     */
    var explainText = ""


    /**
     * 设置说明点击事件
     */
    fun setExplainListener(listener: OnClickListener?,isShowUnderLine:Boolean = false) {
        setOnClickListener(listener)
        tv_value.showUnderLine(isShowUnderLine)
    }

    init {
        layoutInflater.inflate(R.layout.sl_up_down_item_layout, this)
    }

}