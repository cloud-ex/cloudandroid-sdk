package com.sl.contract.library.dialog

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioGroup
import com.contract.sdk.data.ContractWsKlineType
import com.sl.contract.library.R
import com.sl.ui.library.widget.material.MaterialRadioButton

class DropKlineWindow(private val context: Context) : PopupWindow(),View.OnClickListener {
    private var mListener: OnDropKlineClickedListener? = null
    private var rgTab : RadioGroup?=null
    private var tab1min : MaterialRadioButton?= null
    private var tab30min : MaterialRadioButton?= null
    private var tab12hour : MaterialRadioButton?= null
    private var tab1day : MaterialRadioButton?= null
    private var tab1week : MaterialRadioButton?= null
    private var tab1month : MaterialRadioButton?= null

    interface OnDropKlineClickedListener {
        fun onKlineDropClick(wsType: ContractWsKlineType,showText:String)
    }

    private fun initView(view: View) {
        rgTab = view.findViewById<RadioGroup>(R.id.rg_tab)
        tab1min = view.findViewById(R.id.tab_1min)
        tab30min = view.findViewById(R.id.tab_30min)
        tab12hour = view.findViewById(R.id.tab_12hour)
        tab1day = view.findViewById(R.id.tab_1day)
        tab1week = view.findViewById(R.id.tab_1week)
        tab1month = view.findViewById(R.id.tab_1month)

        tab1min?.setOnClickListener(this)
        tab30min?.setOnClickListener(this)
        tab12hour?.setOnClickListener(this)
        tab1day?.setOnClickListener(this)
        tab1week?.setOnClickListener(this)
        tab1month?.setOnClickListener(this)

        tab1min?.tag = ContractWsKlineType.WEBSOCKET_BIN1M
        tab30min?.tag = ContractWsKlineType.WEBSOCKET_BIN30M
        tab12hour?.tag = ContractWsKlineType.WEBSOCKET_BIN12H
        tab1day?.tag = ContractWsKlineType.WEBSOCKET_BIN1D
        tab1week?.tag = ContractWsKlineType.WEBSOCKET_BIN1W
        tab1month?.tag = ContractWsKlineType.WEBSOCKET_BIN30M

    }

    fun selectTab(wsTime : ContractWsKlineType):String?{
        val tabView =  rgTab?.findViewWithTag<MaterialRadioButton?>(wsTime)
        if(tabView!=null){
            onClick(tabView)
            return tabView!!.text.toString()
        }
        return null
    }

    fun setOnKlineDropClick(listener: OnDropKlineClickedListener?) {
        mListener = listener
    }

    init {
        val inflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rootView: View = inflater.inflate(R.layout.view_dropdown_kline, null,true)
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

    fun clearSelect(){
        rgTab?.clearCheck()
    }

    override fun onClick(v: View) {
        val view = v as MaterialRadioButton
        val showText = view.text.toString()
       when(v.id){
           R.id.tab_1min -> {
               //1分
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN1M,showText)
           }
           R.id.tab_30min -> {
               //30分
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN30M,showText)
           }
           R.id.tab_12hour -> {
               //12小时
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN12H,showText)
           }
           R.id.tab_1day -> {
               //1天
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN1D,showText)
           }
           R.id.tab_1week -> {
               //1周
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN1W,showText)
           }
           R.id.tab_1month -> {
               //1月
               mListener?.onKlineDropClick(ContractWsKlineType.WEBSOCKET_BIN30D,showText)
           }
       }
        dismiss()
    }
}