package com.sl.contract.library.dialog

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatRadioButton
import com.contract.sdk.data.ContractWsKlineType
import com.sl.contract.library.R
import com.sl.ui.library.widget.kline.view.MainKlineViewStatus
import com.sl.ui.library.widget.kline.view.vice.ViceViewStatus

/**
 * K线指标window
 */
class DropKlineIndicatorsWindow(private val context: Context) : PopupWindow(),
    View.OnClickListener {
    private var mListener: OnDropKlineIndicatorsClickedListener? = null
    private var rgMain: RadioGroup? = null
    private var rgVice: RadioGroup? = null

    private var mainTabEye: AppCompatRadioButton? = null
    private var viceTabEye: AppCompatRadioButton? = null

    interface OnDropKlineIndicatorsClickedListener {
        fun onKlineMainDropClick(mainStatus: MainKlineViewStatus)
        fun onKlineViceDropClick(viceStatus: ViceViewStatus)
    }

    private fun initView(view: View) {
        rgMain = view.findViewById(R.id.rg_main)
        view.findViewById<AppCompatRadioButton>(R.id.main_tab_ma).setOnClickListener(this)
        view.findViewById<AppCompatRadioButton>(R.id.main_tab_boll).setOnClickListener(this)
        mainTabEye = view.findViewById(R.id.main_tab_eye)
        mainTabEye?.setOnClickListener(this)


        rgVice = view.findViewById(R.id.rg_vice)
        view.findViewById<AppCompatRadioButton>(R.id.vice_tab_macd).setOnClickListener(this)
        view.findViewById<AppCompatRadioButton>(R.id.vice_tab_kdj).setOnClickListener(this)
        view.findViewById<AppCompatRadioButton>(R.id.vice_tab_rsi).setOnClickListener(this)
        viceTabEye = view.findViewById(R.id.vice_tab_eye)
        viceTabEye?.setOnClickListener(this)
    }

    fun setOnKlineIndicatorsDropClick(listener: OnDropKlineIndicatorsClickedListener?) {
        mListener = listener
    }

    init {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView: View = inflater.inflate(R.layout.view_dropdown_kline_indicators, null, true)
        this.contentView = rootView
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.isFocusable = true
        this.isOutsideTouchable = true
        this.update()
        animationStyle = R.style.TopPopStyle
        setBackgroundDrawable(BitmapDrawable())
        initView(rootView)
    }

    fun clearMainSelect() {
        rgMain?.clearCheck()
        mainTabEye?.isChecked = false
    }

    fun clearViceSelect() {
        rgVice?.clearCheck()
        viceTabEye?.isChecked = false
    }

    override fun onClick(v: View) {
        val view = v as AppCompatRadioButton
        when (v.id) {
            R.id.main_tab_ma -> {
                mListener?.onKlineMainDropClick(MainKlineViewStatus.MA)
                mainTabEye?.isChecked = true
            }
            R.id.main_tab_boll -> {
                mListener?.onKlineMainDropClick(MainKlineViewStatus.BOLL)
                mainTabEye?.isChecked = true
            }
            R.id.main_tab_eye -> {
                clearMainSelect()
                mListener?.onKlineMainDropClick(MainKlineViewStatus.NONE)
            }
            R.id.vice_tab_macd -> {
                viceTabEye?.isChecked = true
                mListener?.onKlineViceDropClick(ViceViewStatus.MACD)
            }
            R.id.vice_tab_kdj -> {
                viceTabEye?.isChecked = true
                mListener?.onKlineViceDropClick(ViceViewStatus.KDJ)
            }
            R.id.vice_tab_rsi -> {
                viceTabEye?.isChecked = true
                mListener?.onKlineViceDropClick(ViceViewStatus.RSI)
            }
            R.id.vice_tab_eye -> {
                mListener?.onKlineViceDropClick(ViceViewStatus.NONE)
                clearViceSelect()
            }
        }
        dismiss()
    }
}