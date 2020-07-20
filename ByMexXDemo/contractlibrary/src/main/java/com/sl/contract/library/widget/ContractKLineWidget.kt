package com.sl.contract.library.widget

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractWsKlineType
import com.contract.sdk.data.KLineData
import com.contract.sdk.extra.dispense.DataKLineHelper
import com.contract.sdk.impl.ContractKlineListener
import com.contract.sdk.utils.MathHelper
import com.sl.contract.library.R
import com.sl.contract.library.dialog.DropKlineIndicatorsWindow
import com.sl.contract.library.dialog.DropKlineWindow
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.ui.library.widget.kline.bean.KLineBean
import com.sl.ui.library.widget.kline.data.DataManager
import com.sl.ui.library.widget.kline.data.KLineChartAdapter
import com.sl.ui.library.widget.kline.view.KLineChartView
import com.sl.ui.library.widget.kline.view.MainKlineViewStatus
import com.sl.ui.library.widget.kline.view.vice.ViceViewStatus
import com.sl.ui.library.widget.material.MaterialRadioButton
import kotlinx.android.synthetic.main.view_kline_layout.view.*
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.backgroundResource

/**
 * 深度交易盘面
 */
class ContractKLineWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), View.OnClickListener {
    /**
     * 当前时间区间
     */
    private var curTime = ContractWsKlineType.WEBSOCKET_BINMTIME
    private var klineData: ArrayList<KLineBean> = arrayListOf()
    private val adapter by lazy { KLineChartAdapter() }
    private val dropKlineWindow: DropKlineWindow by lazy { DropKlineWindow(context) }
    private val dropKlineIndicatorsWindow: DropKlineIndicatorsWindow by lazy {
        DropKlineIndicatorsWindow(
            context
        )
    }
    private var contract: Contract? = null
    private var instrumentId = 0
    var isQuickMode = true
        set(value) {
            field = value
            if(value){
                setMainDrawLine(true)
            }
        }

    fun bindContract(contract: Contract) {
        this.contract = contract
        instrumentId = contract.instrument_id

        val timeType = ContractSettingUtils.getKlineCurTime(context)
        if(timeType == curTime){
            if (!isQuickMode) {
                line_layout?.setMainDrawLine(timeType == ContractWsKlineType.WEBSOCKET_BINMTIME)
            }
            loadKlineDataFromNet(true)
        }else{
            //触发tab点击
            selectTab(timeType)
        }
    }

    fun getCurTime(): ContractWsKlineType {
        return curTime
    }

    private fun loadKlineDataFromNet(forward: Boolean = true, cleanCache: Boolean = false) {
        contract ?: return
        if (forward) {
            line_layout.showLoading()
        }
        DataKLineHelper.loadKLineData(
            contract!!.instrument_id,
            curTime,
            forward,
            cleanCache,
            object : DataKLineHelper.KLineDataUpdateListener {
                /**
                 * 分屏加载数据
                 */
                override fun onLoadSplitData(
                    contractId: Int,
                    time: ContractWsKlineType,
                    moreData: ArrayList<KLineData>?
                ) {
                    if (instrumentId == contractId && TextUtils.equals(time.type, curTime.type)) {
                        if (moreData != null && moreData.isNotEmpty()) {
                            val newList = buildKLineBeanList(moreData);
                            klineData.addAll(0, newList)
                            DataManager.calculate(newList)
                            adapter.addItems(0, newList)
                        }
                        line_layout?.refreshComplete()
                    }
                }

                /**
                 * 初始化K线数据
                 */
                override fun onInitData(
                    contractId: Int,
                    time: ContractWsKlineType,
                    data: ArrayList<KLineData>?
                ) {
                    if (instrumentId == contractId && TextUtils.equals(time.type, curTime.type)) {
                        if (data != null && data.isNotEmpty()) {
                            val newList = buildKLineBeanList(data);
                            initKLineData(newList)
                        }
                        line_layout?.refreshComplete()
                    }
                }

                /**
                 * 加载失败
                 */
                override fun onLoadFail(errno: String?, message: String?) {
                    line_layout.refreshComplete()
                }

            })
    }

    private fun initKLineData(data: List<KLineBean>?) {
        if (data != null) {
            klineData.clear()
            klineData.addAll(data)
        }
        DataManager.calculate(klineData)
        adapter.addFooterData(klineData)
        line_layout?.startAnimation()
        if (line_layout?.minScrollX != null) {
            if (klineData.size < 30) {
                line_layout?.scrollX = 0
            } else {
                line_layout?.scrollX = line_layout!!.minScrollX
            }
        }
        //订阅K线
        ContractPublicDataAgent.subscribeKlineWs(instrumentId, curTime)
    }

    private fun buildKLineBean(originData: KLineData): KLineBean {
        val newData = KLineBean()
        newData.id = originData.timestamp
        newData.openPrice = originData.open.toFloat()
        newData.closePrice = originData.close.toFloat()
        newData.highPrice = originData.high.toFloat()
        newData.lowPrice = originData.low.toFloat()
        newData.volume = MathHelper.round(originData.qty, 2).toFloat()
        return newData
    }

    private fun buildKLineBeanList(originData: ArrayList<KLineData>): ArrayList<KLineBean> {
        val newList = ArrayList<KLineBean>()
        originData?.forEach { data ->
            newList.add(buildKLineBean(data))
        }
        return newList
    }

    var contractKlineListener: ContractKlineListener = object : ContractKlineListener() {
        /**
         * 更新某一条
         */
        override fun onWsContractKlineChange(
            type: ContractWsKlineType,
            contractId: Int,
            data: KLineData
        ) {
            if (instrumentId == contractId && TextUtils.equals(curTime.type, type.type)) {
                if (!klineData.isNullOrEmpty()) {
                    //if(klineData.last().id == data.timestamp){
                    val newItem = buildKLineBean(data)
                    klineData[klineData.lastIndex] = newItem
                    DataManager.calculate(klineData)
                    adapter.changeItem(klineData.lastIndex, newItem)
                    // }
                }

            }
        }

        /**
         * 新增
         */
        override fun onWsContractKlineAdd(
            type: ContractWsKlineType,
            contractId: Int,
            data: ArrayList<KLineData>
        ) {
            if (instrumentId == contractId && TextUtils.equals(curTime.type, type.type)) {
                val line = buildKLineBeanList(data)
                klineData.addAll(line)
                DataManager.calculate(klineData)
                adapter.addItems(line)
//                line_layout?.startAnimation()
                line_layout?.refreshComplete()
            }
        }

        /**
         * 可能K线数据有异常，需从新API接口走一次全量
         */
        override fun doKLineApiRequest() {
            loadKlineDataFromNet(forward = false, cleanCache = true)
        }

    }

    fun switchKLineScale(wsType: ContractWsKlineType, isClickMore: Boolean = false) {
        if (curTime != wsType) {
            //先取消旧的订阅
            ContractPublicDataAgent.unSubscribeKlineWs(instrumentId, curTime)
            curTime = wsType
            loadKlineDataFromNet()

            ContractSettingUtils.setKlineCurTime(context, wsType)
        }

        tv_more.isSelected = isClickMore
        if(isClickMore){
            iv_more.setImageResource(R.drawable.icon_tab_more_select)
            ll_more_layout.backgroundResource = R.drawable.border_bottom_yellow_bg
        }else{
            iv_more.setImageResource(R.drawable.icon_tab_more_normal)
            ll_more_layout.backgroundDrawable = null
        }

        if (!isClickMore) {
            tv_more.text = getContext().getString(R.string.str_more)
        }
        if (!isQuickMode) {
            line_layout?.setMainDrawLine(wsType == ContractWsKlineType.WEBSOCKET_BINMTIME)
        }
    }

    /**
     * 设置分时线
     */
    private fun setMainDrawLine(isLine: Boolean) {
        line_layout?.setMainDrawLine(isLine)
    }

    fun selectTab(wsType : ContractWsKlineType) {
        if (curTime != wsType) {
            tabPerformClick(wsType)
        }
    }

    private fun tabPerformClick(wsType: ContractWsKlineType) {
        val tabView =
            rg_time_layout.findViewWithTag<MaterialRadioButton?>(wsType)
        if (tabView != null) {
            tabView.performClick()
        } else {
            dropKlineWindow.selectTab(wsType)?.let {
                tv_more.text = it
            }
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_kline_layout, this, true)
        line_layout.adapter = adapter
        line_layout.isScrollEnable = true
        line_layout.startAnimation()

        line_layout?.setRefreshListener(object : KLineChartView.KChartRefreshListener {
            override fun onLoadMoreBegin(chart: KLineChartView) {
                //加载下一屏数据
                // loadKlineDataFromNet(forward = false, cleanCache = false)
            }

        })

        tab_min_time.setOnClickListener(this)
        tab_5min.setOnClickListener(this)
        tab_15min.setOnClickListener(this)
        tab_4hour.setOnClickListener(this)
        tv_indicators_label.setOnClickListener(this)
        tab_min_time.tag = ContractWsKlineType.WEBSOCKET_BIN1M
        tab_5min.tag = ContractWsKlineType.WEBSOCKET_BIN5M
        tab_15min.tag = ContractWsKlineType.WEBSOCKET_BIN15M
        tab_4hour.tag = ContractWsKlineType.WEBSOCKET_BIN4H
        /**
         * 点击更多分区
         */
        ll_more_layout.setOnClickListener {
            //如果更多没选中
            if (!tv_more.isSelected) {
                dropKlineWindow?.clearSelect()
            }
            dropKlineWindow?.isFocusable = true
            dropKlineWindow?.showAsDropDown(tab_5min, 0, 2)
        }

        dropKlineWindow.setOnKlineDropClick(object :
            DropKlineWindow.OnDropKlineClickedListener {
            override fun onKlineDropClick(wsType: ContractWsKlineType, showText: String) {
                switchKLineScale(wsType, true)
                tv_more.text = showText
                rg_time_layout.clearCheck()
            }
        })

        /**
         * 监听指标
         */
        dropKlineIndicatorsWindow.setOnKlineIndicatorsDropClick(object :
            DropKlineIndicatorsWindow.OnDropKlineIndicatorsClickedListener {
            override fun onKlineMainDropClick(mainStatus: MainKlineViewStatus) {
                line_layout?.changeMainDrawType(mainStatus)
            }

            override fun onKlineViceDropClick(viceStatus: ViceViewStatus) {
                when (viceStatus) {
                    ViceViewStatus.MACD -> {
                        line_layout?.setChildDraw(0)
                    }
                    ViceViewStatus.KDJ -> {
                        line_layout?.setChildDraw(1)
                    }
                    ViceViewStatus.WR -> {
                        line_layout?.setChildDraw(2)
                    }
                    ViceViewStatus.RSI -> {
                        line_layout?.setChildDraw(3)
                    }
                    else -> {
                        line_layout?.hideChildDraw()
                    }
                }
            }

        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tab_min_time -> {
                //分时
                switchKLineScale(ContractWsKlineType.WEBSOCKET_BINMTIME)
            }
            R.id.tab_5min -> {
                //5分
                switchKLineScale(ContractWsKlineType.WEBSOCKET_BIN5M)
            }
            R.id.tab_15min -> {
                //15分
                switchKLineScale(ContractWsKlineType.WEBSOCKET_BIN15M)
            }
            R.id.tab_4hour -> {
                //4小时
                switchKLineScale(ContractWsKlineType.WEBSOCKET_BIN4H)
            }
            R.id.tv_indicators_label -> {
                dropKlineIndicatorsWindow?.isFocusable = true
                dropKlineIndicatorsWindow?.showAsDropDown(tv_indicators_label, 0, 2)
            }
        }
    }
}