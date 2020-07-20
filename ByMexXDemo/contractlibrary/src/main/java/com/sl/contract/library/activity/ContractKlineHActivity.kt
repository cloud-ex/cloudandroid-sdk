package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.RadioGroup
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.data.ContractWsKlineType
import com.contract.sdk.data.KLineData
import com.contract.sdk.extra.dispense.DataKLineHelper
import com.contract.sdk.impl.ContractKlineListener
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.widget.kline.bean.KLineBean
import com.sl.ui.library.widget.kline.data.DataManager
import com.sl.ui.library.widget.kline.data.KLineChartAdapter
import com.sl.ui.library.widget.kline.view.KLineChartView
import com.sl.ui.library.widget.kline.view.MainKlineViewStatus
import com.sl.ui.library.widget.material.MaterialRadioButton
import kotlinx.android.synthetic.main.activity_contract_kline_h.*
import kotlinx.android.synthetic.main.activity_contract_kline_h.tv_24h_vol
import kotlinx.android.synthetic.main.activity_contract_kline_h.tv_last_price
import kotlinx.android.synthetic.main.activity_contract_kline_h.tv_low_px
import kotlinx.android.synthetic.main.activity_contract_kline_h.tv_rose_rate
import kotlinx.android.synthetic.main.view_kline_layout.view.*
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.text.DecimalFormat

/**
 * K线横屏
 */
class ContractKlineHActivity : BaseActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_contract_kline_h
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        super.onCreate(savedInstanceState)
    }

    private var contract : Contract ?= null
    private var instrumentId = 0
    private var pxDecimal = NumberUtil.getDecimal(-1)
    private val dfRate: DecimalFormat = NumberUtil.getDecimal(2)
    private var volDecimal = NumberUtil.getDecimal(-1)

    /**
     * 当前时间区间
     */
    private var curTime = ContractWsKlineType.WEBSOCKET_BIN5M
    private var klineData: ArrayList<KLineBean> = arrayListOf()
    private val adapter by lazy { KLineChartAdapter() }

    private val mainRiseFullColor by lazy{
            R.drawable.sl_border_green_fill
    }
    private val mainFallFullColor by lazy{
        R.drawable.sl_border_red_fill
    }

    override fun loadData() {
        curTime = ContractSettingUtils.getKlineCurTime(this@ContractKlineHActivity)
        instrumentId = intent.getIntExtra("contractId",0)
        contract = ContractPublicDataAgent.getContract(instrumentId)
        if(contract == null){
            return
        }
        pxDecimal = NumberUtil.getDecimal(contract!!.price_index)
        volDecimal = NumberUtil.getDecimal(contract!!.value_index)

    }

    fun switchKLineScale(wsType: ContractWsKlineType) {
        if (!TextUtils.equals(curTime.name, wsType.name)) {
            //先取消旧的订阅
            ContractPublicDataAgent.unSubscribeKlineWs(instrumentId, curTime)
            curTime = wsType
            line_layout?.setMainDrawLine(curTime == ContractWsKlineType.WEBSOCKET_BINMTIME)
            ContractSettingUtils.setKlineCurTime(this@ContractKlineHActivity,wsType)
            ContractSettingUtils.KLineTimeLiveData.postValue(wsType)
            loadKlineDataFromNet()
        }
    }

    private fun loadKlineDataFromNet(forward: Boolean = true, cleanCache: Boolean = false) {
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

    override fun initView() {
        iv_close.setOnClickListener(this)
        ContractPublicDataAgent.getContractTicker(contract!!.instrument_id)?.let {
            updateTicker(it)
        }

        line_layout.adapter = adapter
        line_layout.isScrollEnable = true
        line_layout.startAnimation()

        line_layout?.setRefreshListener(object : KLineChartView.KChartRefreshListener {
            override fun onLoadMoreBegin(chart: KLineChartView) {
                //加载下一屏数据
                // loadKlineDataFromNet(forward = false, cleanCache = false)
            }

        })

        initClickListener()

        /**
         * 加载K线数据
         */
        loadKlineDataFromNet(true)
        selectTab()
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

    private fun selectTab(){
        line_layout?.setMainDrawLine(curTime == ContractWsKlineType.WEBSOCKET_BINMTIME)
        rg_time.findViewWithTag<MaterialRadioButton>(curTime).isChecked = true
    }


    private fun initClickListener() {
        //时间轴
        tab_min_time.tag = ContractWsKlineType.WEBSOCKET_BINMTIME
        tab_1min.tag = ContractWsKlineType.WEBSOCKET_BIN1M
        tab_5min.tag = ContractWsKlineType.WEBSOCKET_BIN5M
        tab_15min.tag = ContractWsKlineType.WEBSOCKET_BIN15M
        tab_30min.tag = ContractWsKlineType.WEBSOCKET_BIN30M
        tab_4hour.tag = ContractWsKlineType.WEBSOCKET_BIN4H
        tab_12hour.tag = ContractWsKlineType.WEBSOCKET_BIN12H
        tab_1day.tag = ContractWsKlineType.WEBSOCKET_BIN1D
        tab_1week.tag = ContractWsKlineType.WEBSOCKET_BIN1W
        tab_1month.tag = ContractWsKlineType.WEBSOCKET_BIN30D

        rg_time.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val wsType =  group.findViewById<View>(checkedId).tag as ContractWsKlineType
                switchKLineScale(wsType)
            }
        })
        main_tab_ma.tag = MainKlineViewStatus.MA
        main_tab_boll.tag = MainKlineViewStatus.BOLL
        main_tab_eye.tag = MainKlineViewStatus.NONE
        //主图
        rg_main.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
                val mainStatus =  group.findViewById<View>(checkedId).tag as MainKlineViewStatus
                line_layout?.changeMainDrawType(mainStatus)
            }
        })
        //副图
        rg_vice.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup, checkedId: Int) {
               when(checkedId){
                   R.id.vice_tab_macd -> {
                       line_layout?.setChildDraw(0)
                   }
                   R.id.vice_tab_kdj -> {
                       line_layout?.setChildDraw(1)
                   }
                   R.id.vice_tab_rsi -> {
                       line_layout?.setChildDraw(3)
                   }
                   R.id.vice_tab_eye -> {
                       line_layout?.hideChildDraw()
                   }
               }
            }
        })

        /**
         * 监听K线
         */
        ContractPublicDataAgent.registerKlineWsListener(this,object: ContractKlineListener(){
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

            override fun onWsContractKlineAdd(
                type: ContractWsKlineType,
                contractId: Int,
                data: ArrayList<KLineData>
            ) {
                if (instrumentId == contractId && TextUtils.equals(curTime.type, type.type)) {
                    val line = buildKLineBeanList(data)
                    klineData.addAll(line)
                    DataManager.calculate(klineData)
                    line_layout?.startAnimation()
                    adapter.addItems(line)
                    line_layout?.refreshComplete()
                }
            }

            override fun doKLineApiRequest() {
                loadKlineDataFromNet(forward = false, cleanCache = true)
            }

        })

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

    /**
     * 更新ticker
     */
    private fun updateTicker(ticker: ContractTicker){
        //币种名称
        tv_coin_name?.text = ticker.symbol
        //最新价格
        tv_last_price.text = pxDecimal.format(MathHelper.round(ticker.last_px))
        //涨跌幅
        val riseFallRate: Double = MathHelper.round(ticker.change_rate.toDouble() * 100, 2)
        val sRate =
            if (riseFallRate >= 0) "+" + dfRate.format(riseFallRate) + "%" else dfRate.format(
                riseFallRate
            ) + "%"
        tv_rose_rate.text = sRate
        val mainColor = if (riseFallRate >= 0) mainRiseColor else mainFallColor
        tv_rose_rate.textColor = mainColor
        tv_rose_rate.backgroundResource = if (riseFallRate >= 0) mainRiseFullColor else mainFallFullColor
        tv_last_price.textColor = mainColor
        //24小时量
        val amount24: Double = MathHelper.round(ticker.qty24, contract!!.vol_index)
        tv_24h_vol?.text = NumberUtil.getBigVolum(mActivity, volDecimal, amount24)
        //最高价
        tv_high_px.text = pxDecimal.format(MathHelper.round(ticker.high))
        //最低价
        tv_low_px.text = pxDecimal.format(MathHelper.round(ticker.low))
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when(v.id){
            R.id.iv_close -> {
                this@ContractKlineHActivity.finish()
            }
        }
    }

    companion object{
        fun show(activity: Activity,contractId:Int){
            val intent = Intent(activity,ContractKlineHActivity::class.java)
            intent.putExtra("contractId",contractId)
            activity.startActivity(intent)
        }
    }
}