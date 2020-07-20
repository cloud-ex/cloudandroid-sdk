package com.sl.contract.library.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.edit
import com.sl.ui.library.utils.setDrawableLeft
import com.sl.ui.library.widget.CommonBorderInputLayout
import kotlinx.android.synthetic.main.view_stop_win_loss_layout.view.*
import org.jetbrains.anko.textColor
import kotlin.math.max
import kotlin.math.min

/**
 * 止盈止损item
 */
class StopWinLossItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * 标题
     */
    var title = ""
        set(value) {
            field = value
            tv_check?.text = title
        }

    /**
     * 是否开启
     */
    var checkTab = false
        set(value) {
            field = value
            if (value) {
                tv_check.setDrawableLeft(R.drawable.icon_common_select)
                tv_check.textColor = context.resources.getColor(R.color.main_yellow)
                et_ex_price.visibility = View.VISIBLE
            } else {
                tv_check.setDrawableLeft(R.drawable.icon_common_normal)
                tv_check.textColor = context.resources.getColor(R.color.normal_text_color)
                et_ex_price.visibility = View.GONE
            }
            tv_check.isSelected = value
            et_trigger_price.getEtInputView().edit(value)
            updateInputHelperHint()
        }
        get() {
            return tv_check.isSelected
        }

    /**
     * 是否是止盈
     */
    var isStopWin = true
        set(value) {
            field = value
            if (value) {
                tv_check.text = context.getString(R.string.str_stop_win_label)
            } else {
                tv_check.text = context.getString(R.string.str_stop_loss_label)
            }
        }

    /**
     * 触发价格
     */
    var triggerPrice = ""
        set(value) {
            field = value
            value.let {
                et_trigger_price.inputText = value
                // et_trigger_price.setSelection(value.length)
            }
        }
        get() {
            return et_trigger_price.inputText
        }

    /**
     * 执行价格
     */
    var exPrice = ""
        set(value) {
            field = value
            value.let {
                et_ex_price.inputText = value
                // et_trigger_price.setSelection(value.length)
            }
        }
        get() {
            return et_ex_price.inputText
        }

    /**
     * 强平价格
     */
    var liqPrice ="0.00"

    /**
     * 是否是市价
     */
    var isMarker = true
        set(value) {
            field = value
            if(field){
                et_ex_price.setTextType()
                et_ex_price.rightTextColor = ContextCompat.getColor(context,R.color.text_color)
                et_ex_price.getEtInputView().edit(false)
                et_ex_price.inputText = context.getString(R.string.str_market_price_label)
                updateInputHelperHint()
            }else{
                et_ex_price.setMoneyType(contract?.price_index?:4)
                et_ex_price.rightTextColor = ContextCompat.getColor(context,R.color.main_yellow)
                et_ex_price.inputText = ""
                et_ex_price.getEtInputView().edit(true)
            }
        }

    private fun updateInputHelperHint() {
        if (order == null) {
            return
        }
        var profitAmount = 0.0 //回报率 = 盈亏额/委托保证金
        //仓位价值
        val value = ContractCalculate.CalculateContractValue(
            order!!.qty,
            order!!.px,
            contract
        )
        // var profitRate = MathHelper.div(profitAmount, MathHelper.add(MathHelper.round(item.im), plus)) * 100
        if (checkTab) {
            var triggerPrice = et_trigger_price.inputText
            var exPrice = et_ex_price.inputText
            if (isStopWin) {
                order!!.profit_price = triggerPrice
                order!!.profit_ex_price = if(isMarker) null else exPrice
                //盈亏
                var profit = max(
                    0.0,

                    ContractCalculate.calculateOrderProfitLossValue(
                        contract!!,
                        order!!.side,
                        order!!.qty,
                        order!!.px,
                        if(isMarker) triggerPrice else exPrice,
                        isStopWin
                    )

                )
                profitAmount = rateDecimal.format(MathHelper.div(profit, value) * 100).toDouble()
                et_ex_price.inputHelperHint = String.format(
                    context.getString(R.string.str_stop_win_estimate_profit),
                    "${pxDecimal.format(profit)} ${contract!!.margin_coin}",
                    "$profitAmount%"
                )
            } else {
                order!!.loss_price = triggerPrice
                order!!.loss_ex_price = if(isMarker) null else exPrice
                var profit = min(
                    0.0,
                    ContractCalculate.calculateOrderProfitLossValue(
                        contract!!,
                        order!!.side,
                        order!!.qty,
                        order!!.px,
                        if(isMarker) triggerPrice else exPrice,
                        isStopWin
                    )
                )
                profitAmount = rateDecimal.format(MathHelper.div(profit, value) * 100).toDouble()
                et_ex_price.inputHelperHint = String.format(
                    context.getString(R.string.str_stop_loss_estimate_profit),
                    "${pxDecimal.format(profit)} ${contract!!.margin_coin}",
                    "$profitAmount%"
                )
            }
        } else {
            et_ex_price.inputHelperHint = ""
        }
    }

    fun inputBaseVerify(): Boolean {
        if (MathHelper.round(triggerPrice) <= 0) {
            ToastUtil.shortToast(context, R.string.str_price_too_low)
            return false
        }
        if (!isMarker && MathHelper.round(exPrice) <= 0) {
            ToastUtil.shortToast(context, R.string.str_price_too_low)
            return false
        }
        contractTicker = ContractPublicDataAgent.getContractTicker(contract!!.instrument_id)
        var triggerPrice = MathHelper.round(et_trigger_price.inputText)
        var exPx = if (isMarker) 0.0 else MathHelper.round(exPrice)
        var lastPx = MathHelper.round(contractTicker!!.last_px)
        if (isStopWin) {
            order!!.profit_category = if (isMarker) 2 else 1
            if (order!!.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG || order!!.side == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG) {
                //多头止盈委托触发价格需要 大于等于 当前价格，若不符合则提示“止盈价格需要高于当前最新价格”；
                if (triggerPrice < lastPx || (!isMarker&&exPx < lastPx)) {
                    ToastUtil.shortToast(
                        context,
                        String.format(
                            context.getString(R.string.str_stop_rate_high_tips),
                            context.getString(R.string.str_latest_price)
                        )
                    )
                    return false
                }
                order!!.trend = 1
            } else {
                //空头止盈委托触发价格需要 小于等于 当前价格，若不符合则提示“止盈价格需要低于当前最新价格”；
                if (triggerPrice > lastPx || (!isMarker&&exPx > lastPx)){
                    ToastUtil.shortToast(
                        context,
                        String.format(
                            context.getString(R.string.str_stop_rate_low_tips),
                            context.getString(R.string.str_latest_price)
                        )
                    )
                    return false
                }
                order!!.trend = 2
            }
        } else {
            val liqPrice = MathHelper.round(liqPrice)
            order!!.loss_category = if (isMarker) 2 else 1
            if (order!!.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG || order!!.side == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG) {
                //多头止损委托触发价格需要 小于等于 当前价格，若不符合则提示“止损价格需要低于当前最新价格”；
                if (triggerPrice > lastPx) {
                    ToastUtil.shortToast(
                        context,
                        String.format(
                            context.getString(R.string.str_stop_loss_low_tips),
                            context.getString(R.string.str_latest_price)
                        )
                    )
                    return false
                }
                if (triggerPrice < liqPrice || (!isMarker&&exPx < liqPrice)){
                    ToastUtil.shortToast(
                        context,
                        context.getString(R.string.str_stop_loss_px_big_liq)
                    )
                    return false
                }
                order!!.trend = 2
            } else {
                // 空头止损委托触发价格需要 大于等于 当前价格，若不符合则提示“止损价格需要高于当前最新价格”；
                if (triggerPrice < lastPx) {
                    ToastUtil.shortToast(
                        context,
                        String.format(
                            context.getString(R.string.str_stop_loss_high_tips),
                            context.getString(R.string.str_latest_price)
                        )
                    )
                    return false
                }
                if (triggerPrice > liqPrice || (!isMarker&&exPx > liqPrice)){
                    ToastUtil.shortToast(
                        context,
                        context.getString(R.string.str_stop_loss_px_small_liq)
                    )
                    return false
                }
                order!!.trend = 1
            }
        }

        return true
    }

    /**
     * 交易止损警告价格
     */
    fun verifyStopLossWarnPx(): String {
        val liqPrice = ContractCalculate.CalculateOrderLiquidatePrice(
            order!!,
            if (order!!.position_type == 2) ContractUserDataAgent.getContractAccount(contract!!.margin_coin) else null,
            contract!!
        )
        var warnText = ""
        val warnPx =
            ContractCalculate.calculateStopLostWarnPx(contract!!, order!!.px.toDouble(), liqPrice)
        val triggerFairPx = MathHelper.round(triggerPrice)
        if (order!!.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) {
            //多仓限价止损时限制若触发价格或者执行价格低于仓位预警价格
            if (triggerFairPx < warnPx) {
                warnText = "止损触发价格低于预警价格 $warnPx，可能会导致止损失败，是否继续提交？"
            }
        } else {
            //空仓限价止损时限制若触发价格或者执行价格高于于仓位预警价格
            if (triggerFairPx > warnPx) {
                warnText = "止损触发价格高于预警价格 $warnPx，可能会导致止损失败，是否继续提交？"
            }
        }
        return warnText
    }



    private var contract: Contract? = null
    private var order: ContractOrder? = null
    private var contractTicker: ContractTicker? = null
    private var pxDecimal = NumberUtil.getDecimal(-1)
    private var rateDecimal = NumberUtil.getDecimal(2)

    fun bindContract(contract: Contract, order: ContractOrder) {
        this.contract = contract
        this.order = order
        pxDecimal = NumberUtil.getDecimal(contract.price_index)
        contractTicker = ContractPublicDataAgent.getContractTicker(contract!!.instrument_id)

        //设置触发价格精度
        et_trigger_price.setMoneyType(contract.price_index)
        et_ex_price.setMoneyType(contract.price_index)
    }


    init {
        layoutInflater.inflate(R.layout.view_stop_win_loss_layout, this)
        isMarker = true
        checkTab = false
        initListener()
    }

    private fun initListener() {
        //选中tab
        tv_check.setOnClickListener {
            checkTab = !tv_check.isSelected
        }

        et_trigger_price.setEditTextChangedListener(object :
            CommonBorderInputLayout.EditTextChangedListener {
            override fun onTextChanged(view: CommonBorderInputLayout) {
                if(isMarker){
                    updateInputHelperHint()
                }
            }

        })
        et_ex_price.setEditTextChangedListener(object : CommonBorderInputLayout.EditTextChangedListener{
            override fun onTextChanged(view: CommonBorderInputLayout) {
                if(!isMarker){
                    updateInputHelperHint()
                }
            }

        })
        /**
         * 市场点击事件
         */
        et_ex_price.setRightListener(object :OnClickListener{
            override fun onClick(v: View?) {
                isMarker = !isMarker
            }

        } )
    }

}