package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.contract.library.R
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.finishActivityDialogAnim
import com.sl.ui.library.utils.startActivityDialogAnim
import com.sl.ui.library.widget.CommonInputLayout
import kotlinx.android.synthetic.main.activity_contract_calculate.*
import kotlinx.android.synthetic.main.activity_contract_calculate.iv_close
import kotlinx.android.synthetic.main.activity_contract_calculate.tab_layout
import java.text.DecimalFormat

/**
 * 合约计算器
 */
class ContractCalculateActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_contract_calculate
    }

    private var contractId = 0
    private var contract: Contract? = null

    private var tabTextList = arrayOfNulls<String>(3)
    private var tabIndex = 0

    private var tabPositionTextList = arrayOfNulls<String>(2)
    private var tabPositionIndex = 0

    private var tabOpneTextList = arrayOfNulls<String>(2)
    private var tabOpenIndex = 0

    override fun loadData() {
        contractId = intent.getIntExtra("contractId", 0);
        contract = ContractPublicDataAgent.getContract(contractId)
        if (contract == null) {
            finish()
            return
        }

        tabTextList[0] = getString(R.string.str_pl_calculator)
        tabTextList[1] = getString(R.string.str_profit_rate)
        tabTextList[2] = getString(R.string.str_liq_price)

        tabPositionTextList[0] = getString(R.string.sl_str_gradually_position)
        tabPositionTextList[1] = getString(R.string.sl_str_full_position)

        tabOpneTextList[0] = getString(R.string.str_open_long)
        tabOpneTextList[1] = getString(R.string.str_open_short)
    }

    override fun initView() {
        initOnClickListener()
        initTabView()
        initTabPositionView()
        initTabOpenView()
        doSwitchTabUi()

        item_lever_layout.setNumberType()
        item_lever_layout.inputText =
            ContractOrderHelper.getDefaultLever(this@ContractCalculateActivity, contract!!)
                .toString()
        item_open_vol_layout.setNumberType()
        item_open_price_layout.setMoneyType(contract!!.price_index)
        item_open_price_layout.rightText = contract!!.quote_coin
        item_close_price_layout.setMoneyType(contract!!.price_index)
        item_close_price_layout.rightText = contract!!.quote_coin
        item_floating_gains_layout.setNumberType()
        item_profit_value_layout.setNumberType()
        item_profit_value_layout.rightText = contract!!.quote_coin

        //杠杆输入范围校验
        item_lever_layout.setEditTextChangedListener(object :
            CommonInputLayout.EditTextChangedListener {
            override fun onTextChanged(view: CommonInputLayout) {
                var inputText = view.inputText
                if (TextUtils.isEmpty(inputText)) {
                    return
                }
                if (inputText.toDouble() > contract!!.max_leverage.toDouble()) {
                    view.inputText = contract!!.max_leverage
                }
                if (inputText.toDouble() < contract!!.min_leverage.toDouble()) {
                    view.inputText = contract!!.min_leverage
                }
            }

        })
    }

    /**
     * 多/空
     */
    private fun initTabOpenView() {
        tab_open_layout.setTabData(tabOpneTextList)
        tab_open_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                tabOpenIndex = index
            }

            override fun onTabReselect(p0: Int) {
            }

        })
    }

    /**
     * 全仓/逐仓
     */
    private fun initTabPositionView() {
        tab_position_layout.setTabData(tabPositionTextList)
        tab_position_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                tabPositionIndex = index
            }

            override fun onTabReselect(p0: Int) {
            }

        })
    }

    private fun initTabView() {
        tab_layout.setTabData(tabTextList)
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                tabIndex = index
                doSwitchTabUi()
            }

            override fun onTabReselect(p0: Int) {
            }

        })
    }

    private fun initOnClickListener() {
        bt_sure.setOnClickListener(this)
        iv_close.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_close -> {
                finishActivityDialogAnim()
            }
            R.id.bt_sure -> {
                doCalculator(true)
            }

        }
    }

    private fun doCalculator(showToast: Boolean) {
        val currLeverage = item_lever_layout.inputText
        val vol = item_open_vol_layout.inputText
        val openPrice = item_open_price_layout.inputText
        val closePx = item_close_price_layout.inputText
        val profitValue = item_profit_value_layout.inputText

        if (tabIndex == 0 && (TextUtils.isEmpty(currLeverage) || TextUtils.isEmpty(vol) || TextUtils.isEmpty(
                openPrice
            ) || TextUtils.isEmpty(closePx))
        ) {
            if (showToast) {
                ToastUtil.shortToast(
                    this@ContractCalculateActivity,
                    getString(R.string.str_miss_param)
                )
            }
            return
        }

        if (tabIndex == 1 && (TextUtils.isEmpty(currLeverage) || TextUtils.isEmpty(vol) || TextUtils.isEmpty(
                openPrice
            ) || TextUtils.isEmpty(profitValue))
        ) {
            if (showToast) {
                ToastUtil.shortToast(
                    this@ContractCalculateActivity,
                    getString(R.string.str_miss_param)
                )
            }
            return
        }
        if (tabIndex == 2 && (TextUtils.isEmpty(currLeverage) || TextUtils.isEmpty(vol) || TextUtils.isEmpty(
                openPrice
            ))
        ) {
            if (showToast) {
                ToastUtil.shortToast(
                    this@ContractCalculateActivity,
                    getString(R.string.str_miss_param)
                )
            }
            return
        }

        val dfNormal: DecimalFormat = NumberUtil.getDecimal(-1)
        val value: Double = ContractCalculate.CalculateContractValue(
            vol,
            openPrice,
            contract
        )
        val mDirection = tabPositionIndex
        val margin =
            ContractCalculate.CalculateIM(dfNormal.format(value), currLeverage.toInt(), contract!!)
        val contractOrder = ContractOrder()
        contractOrder.instrument_id = contract!!.instrument_id
        contractOrder.leverage = currLeverage.toInt()
        contractOrder.qty = vol
        contractOrder.position_type = 1
        contractOrder.px = openPrice
        contractOrder.category = ContractOrder.ORDER_CATEGORY_NORMAL
        if (mDirection == 0) {
            contractOrder.side = ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG
        } else if (mDirection == 1) {
            contractOrder.side = ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT
        }

        when (tabIndex) {
            0 -> {
                //盈亏计算
                var profitRate = 0.0 //未实现盈亏
                var profitAmount = 0.0 //未实现盈亏额
                if (mDirection == 0) {
                    profitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                        vol,
                        openPrice,
                        closePx,
                        contract!!.face_value,
                        contract!!.isReserve
                    )

                    profitRate = MathHelper.div(profitAmount, margin) * 100
                } else {
                    profitAmount += ContractCalculate.CalculateCloseShortProfitAmount(
                        vol,
                        openPrice,
                        closePx,
                        contract!!.face_value,
                        contract!!.isReserve
                    )
                    profitRate = MathHelper.div(profitAmount, margin) * 100
                }

                //保证金
                tv_show_value1.text = dfNormal.format(margin) + contract?.margin_coin
                //收益
                tv_show_value2.text = dfNormal.format(
                    MathHelper.round(
                        value,
                        contract!!.value_index
                    )
                ) + contract!!.margin_coin
                //收益率
                tv_show_value3.text =
                    dfNormal.format(MathHelper.round(profitRate, 2)).toString() + "%"
            }
            1 -> {
                //目标价格
                val targetPrice = ContractCalculate.CalculateOrderTargetPriceValue(
                    contractOrder,
                    profitValue,
                    0,
                    contract!!
                )
                tv_show_value1.text = dfNormal.format(
                    MathHelper.round(
                        targetPrice,
                        contract!!.price_index
                    )
                ) + contract!!.quote_coin
                //占用保证金
                tv_show_value2.text = dfNormal.format(margin) + contract?.margin_coin
            }
            2 -> {
                //强平价格
                val liquidationPrice = ContractCalculate.CalculateOrderLiquidatePrice(
                    contractOrder,
                    if (tabPositionIndex == 0) null else ContractUserDataAgent.getContractAccount(
                        contract!!.margin_coin
                    ),
                    contract!!
                )
                tv_show_value1.text = dfNormal.format(
                    MathHelper.round(
                        liquidationPrice,
                        contract!!.price_index
                    )
                ) + contract!!.quote_coin
            }
        }

    }

    /**
     * 切换计算类型
     */
    fun doSwitchTabUi() {
        tv_show_value1.text = "--"
        tv_show_value2.text = "--"
        tv_show_value3.text = "--"
        when (tabIndex) {
            0 -> {
                //盈亏计算
                tab_position_layout.visibility = View.GONE
                tab_position_layout.visibility = View.GONE
                item_floating_gains_layout.visibility = View.GONE
                item_profit_value_layout.visibility = View.GONE
                item_close_price_layout.visibility = View.VISIBLE
                rl_show_layout3.visibility = View.VISIBLE
                rl_show_layout2.visibility = View.VISIBLE
                //保证金，收益，收益率
                tv_show_label1.text = getString(R.string.sl_str_margins)
                tv_show_label2.text = getString(R.string.sl_str_earnings)
                tv_show_label3.text = getString(R.string.sl_str_earnings_rate)
            }
            1 -> {
                //目标收益率
                tab_position_layout.visibility = View.GONE
                item_close_price_layout.visibility = View.GONE
                item_profit_value_layout.visibility = View.VISIBLE
                rl_show_layout3.visibility = View.GONE
                rl_show_layout2.visibility = View.VISIBLE
                //目标平仓价格，占用保证金
                tv_show_label1.text = getString(R.string.str_target_price)
                tv_show_label2.text = getString(R.string.str_take_margin)
            }
            2 -> {
                //强平价格
                rl_show_layout2.visibility = View.GONE
                rl_show_layout3.visibility = View.GONE
                tab_position_layout.visibility = View.VISIBLE
                item_profit_value_layout.visibility = View.GONE
                item_close_price_layout.visibility = View.GONE
            }
        }
        doCalculator(false)
    }

    companion object {
        fun show(activity: Activity, contractId: Int) {
            val intent = Intent(activity, ContractCalculateActivity::class.java)
            intent.putExtra("contractId", contractId)
            activity.startActivityDialogAnim(intent)
        }
    }
}