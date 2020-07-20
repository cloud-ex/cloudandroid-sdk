package com.sl.ui.library.widget

import android.content.Context
import android.text.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import com.sl.ui.library.R
import com.sl.ui.library.utils.CashierInputFilter
import kotlinx.android.synthetic.main.item_commonly_border_input_layout.view.*
import kotlinx.android.synthetic.main.item_commonly_border_input_layout.view.et_input
import kotlinx.android.synthetic.main.item_commonly_border_input_layout.view.tv_helper
import kotlinx.android.synthetic.main.item_commonly_border_input_layout.view.tv_right_text
import kotlinx.android.synthetic.main.item_commonly_input_layout.view.*
import org.jetbrains.anko.backgroundResource


/**
 * 圆角边框輸入框
 */
class CommonBorderInputLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var inputHint = ""
        set(value) {
            field = value
            et_input?.hint = value
        }
    var inputText = ""
        set(value) {
            field = value
            et_input?.let {
                it.setText(value)
                if(it.hasFocus()){
                    it.setSelection(it.text.length)
                }
            }
        }
        get() {
            field = et_input.text.toString()
            return field
        }
    var inputHelperHint = ""
        set(value) {
            field = value
            if(!TextUtils.isEmpty(value)){
                tv_helper?.apply {
                    visibility = View.VISIBLE
                    text = value
                }
            }else{
                tv_helper?.visibility = View.GONE
            }
        }
    var rightText = ""
        set(value) {
            field = value
            tv_right_text?.text = value
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
     * 设置数字类型
     */
    fun setNumberType() {
        et_input.inputType =  InputType.TYPE_CLASS_NUMBER
        et_input.filters =  arrayOf<InputFilter>()
    }

    fun setTextType() {
        et_input.inputType =  InputType.TYPE_CLASS_TEXT
        et_input.filters =  arrayOf<InputFilter>()
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

    fun getEtInputView():EditText{
        return et_input
    }

    /**
     * 是否获取焦点
     */
    fun requestInputView(requestFocus : Boolean,isEnabled:Boolean = true){
        if(requestFocus){
            et_input.requestFocus()
            et_input.setSelection(et_input.text.toString().length)
        }else{
            et_input.clearFocus();
        }
        et_input.isEnabled = isEnabled
    }
    /**
     * 右边点击事件
     */
    fun setRightListener(listener: OnClickListener) {
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
                listener.onTextChanged(this@CommonBorderInputLayout)
            }

        })
    }

    private var editTextFocusChangeListener :EditTextFocusChangeListener? = null
    fun setInputFocusChangeListener(listener: EditTextFocusChangeListener){
        this.editTextFocusChangeListener = listener
    }

    interface EditTextChangedListener {
        fun onTextChanged(view: CommonBorderInputLayout)
    }

    interface EditTextFocusChangeListener {
        fun onFocusChange(view: CommonBorderInputLayout,hasFocus:Boolean)
    }


    init {
        initView(context)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.inputLayout)
        inputHint = typedArray.getString(R.styleable.inputLayout_inputHint)?:""
        inputHelperHint = typedArray.getString(R.styleable.inputLayout_inputHelperHint)?:""
        rightText = typedArray.getString(R.styleable.inputLayout_rightText)?:""
        rightTextColor =  typedArray.getColor(R.styleable.inputLayout_rightTextColor, 0)
        typedArray.recycle()
    }

    fun initView(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.item_commonly_border_input_layout, this, true)
        et_input.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                rl_input_layout.backgroundResource = R.drawable.bg_et_focused
                et_input?.isCursorVisible = true
            } else {
                rl_input_layout.backgroundResource = R.drawable.bg_et_unfocused
                et_input?.isCursorVisible = false
            }
            editTextFocusChangeListener?.onFocusChange(this@CommonBorderInputLayout,hasFocus)

        }
    }
}