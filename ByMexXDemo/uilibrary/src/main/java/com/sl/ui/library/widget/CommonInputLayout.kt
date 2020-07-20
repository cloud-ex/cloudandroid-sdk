package com.sl.ui.library.widget

import android.content.Context
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.sl.ui.library.R
import com.sl.ui.library.utils.CashierInputFilter
import kotlinx.android.synthetic.main.item_commonly_border_input_layout.view.*
import kotlinx.android.synthetic.main.item_commonly_input_layout.view.*
import kotlinx.android.synthetic.main.item_commonly_input_layout.view.et_input
import kotlinx.android.synthetic.main.item_commonly_input_layout.view.tv_helper
import kotlinx.android.synthetic.main.item_commonly_input_layout.view.tv_right_text
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.singleLine
import org.jetbrains.anko.textColor

/**
 * 下劃綫輸入框
 */
class CommonInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    var inputHint = ""
        set(value) {
            field = value
            input_layout?.hint = value
        }
    var inputText = ""
        get() {
            return et_input.text.toString() ?: ""
        }
        set(value) {
            field = value
            et_input.setText(value)
            if(et_input.hasFocus() && !value.isNullOrEmpty()){
                et_input.setSelection(value.length)
            }
        }
    var inputHelperHint = ""
        set(value) {
            field = value
            tv_helper?.apply {
                if (TextUtils.isEmpty(value)) {
                    visibility = View.GONE
                } else {
                    visibility = View.VISIBLE
                }
                text = value
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
            if(rightTextColor!=0){
                tv_right_text.setTextColor(field)
            }
        }

    /**
     * 右边icon
     */
    var rightIcon = 0
        set(value) {
            field = value
            if (rightIcon != 0) {
                iv_right_icon.visibility = View.VISIBLE
                iv_right_icon.imageResource = rightIcon
            } else {
                iv_right_icon.visibility = View.GONE
            }
        }

    var inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        set(value) {
            field = value
            if (value > 0) {
                et_input.inputType = value
            }
        }


    /**
     * 设置金额类型
     */
    fun setMoneyType(pointer:Int = 8) {
        et_input.inputType =  InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        val inputFilter = CashierInputFilter()
        inputFilter.POINTER_LENGTH =pointer
        et_input.filters = arrayOf<InputFilter>(inputFilter)
    }


    fun getRightTextView():TextView{
        return tv_right_text
    }

    fun getInputView():EditText{
        return et_input
    }

    /**
     * 密码类型 是否打开眼睛
     */
    var openEyeEnable = false
        set(value) {
            field = value
            val pos: Int = et_input.selectionStart
            if (!value) {
                et_input.transformationMethod = PasswordTransformationMethod.getInstance()
                iv_right_icon.imageResource = R.drawable.icon_eye_off
            } else {
                et_input.transformationMethod = HideReturnsTransformationMethod.getInstance()
                iv_right_icon.imageResource = R.drawable.icon_eye_open
            }
            et_input.setSelection(pos);
        }

    /**
     * 是否是密码类型
     */
    fun setPwdType() {
        et_input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
    }

    /**
     * 设置数字类型
     */
    fun setNumberType() {
        et_input.inputType =  InputType.TYPE_CLASS_NUMBER
    }

    /**
     * 设置金额类型
     */
    fun setMoneyType() {
        et_input.inputType =  InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
    }


    /**
     * 设置数字密码类型
     */
    fun setNumberPwdType() {
        et_input.inputType =   InputType.TYPE_NUMBER_VARIATION_PASSWORD or InputType.TYPE_CLASS_NUMBER
    }

    /**
     * 右边点击事件
     */
    fun setRightListener(listener: OnClickListener) {
            iv_right_icon.setOnClickListener(listener)
        tv_right_text.setOnClickListener(listener)
    }

    /**
     * 设置输入监听
     */
    fun setEditTextChangedListener(listener: EditTextChangedListener) {
        et_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener.onTextChanged(this@CommonInputLayout)
            }

        })
    }

    interface EditTextChangedListener {
        fun onTextChanged(view: CommonInputLayout)
    }

    init {
        initView(context)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.inputLayout)
        inputHint = typedArray.getString(R.styleable.inputLayout_inputHint).toString()
        inputHelperHint = typedArray.getString(R.styleable.inputLayout_inputHelperHint).orEmpty()
        rightIcon = typedArray.getResourceId(R.styleable.inputLayout_rightIcon, 0)
        rightTextColor =  typedArray.getColor(R.styleable.inputLayout_rightTextColor, 0)
        rightText  = typedArray.getString(R.styleable.inputLayout_rightText).orEmpty()
        typedArray.recycle()
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_commonly_input_layout, this, true)
        et_input.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                view_cursor.backgroundColor = resources.getColor(R.color.line_focus_color)
            } else {
                view_cursor.backgroundColor = resources.getColor(R.color.line_color)
            }

        }
    }
}