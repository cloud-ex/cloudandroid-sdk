package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.data.ContractWsKlineType
import com.contract.sdk.data.DepthData
import com.contract.sdk.extra.dispense.DataDepthHelper
import com.contract.sdk.impl.ContractDepthListener
import com.contract.sdk.impl.ContractTickerListener
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.fragment.detail.ContractDepthFragment
import com.sl.contract.library.fragment.detail.ContractDishFragment
import com.sl.contract.library.fragment.detail.ContractTradeRecordFragment
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.utils.ContractSettingUtils.KLineTimeLiveData
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.utils.showUnderLine
import kotlinx.android.synthetic.main.activity_contract_kline.*
import kotlinx.android.synthetic.main.activity_contract_kline.kline_layout
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor
import java.text.DecimalFormat
import java.util.*

/**
 * 合约K腺
 */
class ContractKlineActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_contract_kline
    }

    private var contract: Contract? = null

    private var volDecimal = NumberUtil.getDecimal(-1)
    private var pxDecimal = NumberUtil.getDecimal(-1)
    private val dfRate: DecimalFormat = NumberUtil.getDecimal(2)
    private val mainRiseFullColor by lazy {
        R.drawable.sl_border_green_fill
    }
    private val mainFallFullColor by lazy {
        R.drawable.sl_border_red_fill
    }

    private val contractDishFragment = ContractDishFragment()
    private val contractDepthFragment = ContractDepthFragment()
    private val contractTradeRecordFragment = ContractTradeRecordFragment()

    override fun loadData() {
        contract = ContractPublicDataAgent.getContract(intent.getIntExtra("contractId", 0))
        if (contract == null) {
            finish()
            return
        }
        contractDishFragment.bindContract(contract!!)
        contractDepthFragment?.bindContract(contract!!)
        contractTradeRecordFragment?.bindContract(contract!!)
        volDecimal = NumberUtil.getDecimal(contract!!.value_index)
        pxDecimal = NumberUtil.getDecimal(contract!!.price_index)
        //订阅深度
        ContractPublicDataAgent.subscribeDepthWs(contract!!.instrument_id)

    }

    override fun initView() {
        title_layout.title = contract!!.symbol
        var ticker = ContractPublicDataAgent.getContractTicker(contract!!.instrument_id)
        ticker?.let {
            updateTicker(it)
        }
        tv_index_price.showUnderLine(true)
        tv_fair_price.showUnderLine(true)
        registerListener()
        kline_layout?.isQuickMode = false
        kline_layout?.bindContract(contract!!)
        //监听K线
        ContractPublicDataAgent.registerKlineWsListener(this, kline_layout.contractKlineListener)
        //深度
        ContractPublicDataAgent.registerDepthWsListener(this, 15, object : ContractDepthListener() {
            override fun onWsContractDepth(
                contractId: Int,
                buyList: ArrayList<DepthData>,
                sellList: ArrayList<DepthData>
            ) {
                if (contract?.instrument_id == contractId) {
                    updateDepthUI(buyList, sellList)
                }
            }

        })
        //K线时间切换监听
        KLineTimeLiveData.observe(this, object : Observer<ContractWsKlineType> {
            override fun onChanged(t: ContractWsKlineType?) {
                t?.let {
                    if (!TextUtils.equals(kline_layout?.getCurTime()?.name, it.name)) {
                        kline_layout?.selectTab(it)
                    }
                }
            }

        })
        title_layout.setRightOnClickListener(View.OnClickListener {
            contract?.let {
                //取消当前订阅
                ContractPublicDataAgent.unSubscribeKlineWs(
                    it.instrument_id,
                    kline_layout!!.getCurTime()
                )
                ContractKlineHActivity.show(this@ContractKlineActivity, it.instrument_id)
            }
        })
        tv_index_price.setOnClickListener(this)
        tv_fair_price.setOnClickListener(this)
        tv_open_long.setOnClickListener(this)
        tv_open_short.setOnClickListener(this)
        initTabLayout()
    }

    private fun updateDepthUI(
        buyList: ArrayList<DepthData>,
        sellList: ArrayList<DepthData>
    ) {
        if (contractDishFragment.isVisible) {
            contractDishFragment.bindDepthData(buyList, sellList)
        }
        if (contractDepthFragment.isVisible) {
            contractDepthFragment.bindDepthData(buyList, sellList)
        }
    }

    private fun initTabLayout() {
        val tabText = arrayOf(
            getString(R.string.str_dish),
            getString(R.string.str_depth),
            getString(R.string.str_deal)
        )
        val arrayFragment = ArrayList<Fragment>()
        arrayFragment.add(contractDishFragment)
        arrayFragment.add(contractDepthFragment)
        arrayFragment.add(contractTradeRecordFragment)
        vp_layout.offscreenPageLimit = 3
        tab_layout.setViewPager(vp_layout, tabText, this@ContractKlineActivity, arrayFragment)
    }

    fun registerListener() {
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            override fun onWsContractTicker(ticker: ContractTicker) {
                if (ticker.instrument_id == contract?.instrument_id) {
                    updateTicker(ticker)
                }
            }

        })
    }


    private fun updateTicker(ticker: ContractTicker) {
        //最新价格
        tv_last_price.text = pxDecimal.format(MathHelper.round(ticker.last_px))
        //指数价格
        tv_index_price.text = pxDecimal.format(MathHelper.round(ticker.index_px))
        //合理价格
        tv_fair_price.text = pxDecimal.format(MathHelper.round(ticker.fair_px))
        //涨跌幅
        val riseFallRate: Double = MathHelper.round(ticker.change_rate.toDouble() * 100, 2)
        val sRate =
            if (riseFallRate >= 0) "+" + dfRate.format(riseFallRate) + "%" else dfRate.format(
                riseFallRate
            ) + "%"
        tv_rose_rate.text = sRate
        val mainColor = if (riseFallRate >= 0) mainRiseColor else mainFallColor
        tv_rose_rate.textColor = mainColor
        tv_rose_rate.backgroundResource =
            if (riseFallRate >= 0) mainRiseFullColor else mainFallFullColor
        tv_last_price.textColor = mainColor
        //24小时量
        val amount24: Double = MathHelper.round(ticker.qty24, contract!!.vol_index)
        tv_24h_vol.text = NumberUtil.getBigVolum(this@ContractKlineActivity, volDecimal, amount24)
        //最高价
        tv_high_px.text = pxDecimal.format(MathHelper.round(ticker.high))
        //最低价
        tv_low_px.text = pxDecimal.format(MathHelper.round(ticker.low))
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_index_price -> {
                ContractOrderHelper.showIndexPriceDialog(mActivity)
            }
            R.id.tv_fair_price -> {
                ContractOrderHelper.showFairPriceDialog(mActivity)
            }
            R.id.tv_open_long, R.id.tv_open_short -> {
                this@ContractKlineActivity.finish()
            }
        }
    }

    companion object {
        fun show(activity: Activity, contractId: Int) {
            val intent = Intent(activity, ContractKlineActivity::class.java)
            intent.putExtra("contractId", contractId)
            activity.startActivity(intent)
        }
    }

}