package com.sl.contract.library.helper

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.ContractUserDataAgent.getContractPosition
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.data.OrderPriceType
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.contract.library.widget.SlDialogHelper
import com.sl.contract.library.widget.StopWinLossItem
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.*
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.bubble.BubbleSeekBar
import com.sl.ui.library.widget.material.MaterialButton
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import org.jetbrains.anko.backgroundResource
import kotlin.math.max
import kotlin.math.min

class ContractOrderHelper {
    companion object {
        /**
         * 得到当前合约杠杆
         */
        fun getDefaultLever(activity: Activity, contract: Contract): Int {
            //读取本地保存的默认杠杆
            var localLeverage = ContractSettingUtils.getLeverage(activity, contract.instrument_id)
            val defaultLeverage = contract.default_leverage
            return when {
                localLeverage == 0 && defaultLeverage > 0 -> {//本地没保持上次记录，则取默认杠杆
                    defaultLeverage
                }
                localLeverage in contract.min_leverage.toInt()..contract.max_leverage.toInt() -> {//本地已保持，且在范围内
                    localLeverage
                }
                else -> {
                    contract.min_leverage.toInt()
                }
            }
        }

        /**
         * 对合约账户做预校验
         */
        fun doPreAccountVerify(activity: FragmentActivity, tradeType: Int, it: Contract): Boolean {
            //判断是否开通合约
            val contractAccount = ContractUserDataAgent.getContractAccount(it.margin_coin)
            if (contractAccount == null) {
                //ToastUtil.shortToast(activity, "需要开通合约")
                openCreateContractDialog(activity)
                return false
            }
            //开仓并且资金不足，跳转到划转页面
            if (tradeType == 0 && contractAccount.available_vol_real <= 0) {
                DialogUtils.showCenterDialog(activity!!,
                    activity.getString(R.string.str_tips),
                    activity.getString(R.string.str_contract_no_asset_tips)
                    ,
                    activity.getString(R.string.common_text_btnCancel),
                    activity.getString(R.string.str_confirm),
                    object : DialogUtils.DialogBottomListener {
                        override fun clickTab(tabType: Int) {
                            if (tabType == 1) {
                                ContractService.contractService?.openAssetTransferActivity(
                                    activity,
                                    it.margin_coin
                                )
                            }
                        }

                    })
                return false
            }
            return true
        }

        /**
         * 普通订单提交基本预校验
         */
        fun doCommitPreBaseVerify(
            activity: Activity,
            tradeType: Int,
            priceType: OrderPriceType,
            price: Double,
            triggerPrice: Double,
            vol: Double,
            it: Contract
        ): Boolean {
            var vol1 = vol
            if (price <= 0 && priceType == OrderPriceType.CONTRACT_ORDER_LIMIT) {
                TopToastUtils.showFail(activity!!, activity.getString(R.string.str_price_too_low))
                return false
            }
            if (triggerPrice <= 0 && priceType == OrderPriceType.CONTRACT_ORDER_PLAN) {
                TopToastUtils.showFail(activity!!, activity.getString(R.string.str_price_too_low))
                return false
            }
            val unit: Int = LogicContractSetting.getContractUint(activity)
            if (unit == 1) {
                vol1 =
                    ContractCalculate.trans2ContractVol(it, vol1.toString(), price.toString(), unit)
                        .toDouble()
            }
            if (vol1 <= 0) {
                TopToastUtils.showFail(activity!!, activity.getString(R.string.str_volume_too_low))
                return false
            }
            //开仓 校验下最小最大订单量
            if (tradeType == 0) {
                if (vol1 < it.min_qty.toDouble()) {
                    TopToastUtils.showFail(activity!!, activity.getString(R.string.str_volume_too_low))
                    return false
                }
                if (vol1 > it.max_qty.toDouble()) {
                    TopToastUtils.showFail(
                        activity!!,
                        String.format(activity.getString(R.string.str_book_order_tips), it.max_qty)
                    )
                    return false
                }
            }
            return true
        }


        /**
         * 构建订单实体
         * @param isAdvancedLimit 是否是高级委托
         * @param currAdvancedLimit 高级委托类型
         */
        fun buildContractOrder(
            activity: Activity,
            contract: Contract,
            tradeType: Int,
            priceType: OrderPriceType,
            currLeverage: Int,
            isAdvancedLimit: Boolean,
            currAdvancedLimit: Int,
            order: ContractOrder
        ): ContractOrder {
            contract?.let {
                order.instrument_id = it.instrument_id
                if (tradeType == 0) {//开仓
                    order.leverage =
                        if (currLeverage == 0) it.max_leverage.toInt() else currLeverage
                } else {//平仓
                    val shortPosition = ContractUserDataAgent?.getContractPosition(
                        order.instrument_id,
                        if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT) ContractPosition.POSITION_TYPE_SHORT else ContractPosition.POSITION_TYPE_LONG
                    )
                    if (shortPosition != null) {
                        order.pid = shortPosition.pid
                    }
                }


                order.category = ContractOrder.ORDER_CATEGORY_NORMAL
                var priceDisplay = when (priceType) {
                    OrderPriceType.CONTRACT_ORDER_LIMIT -> {//限价
                        order.px + " " + it.quote_coin
                    }
                    OrderPriceType.CONTRACT_ORDER_MARKET -> {//市价
                        order.category = ContractOrder.ORDER_CATEGORY_MARKET
                        activity.getString(R.string.str_market_price)
                    }
                    OrderPriceType.CONTRACT_ORDER_BID_PRICE -> {//买一
                        activity.getString(R.string.sl_str_buy1_price)
                    }
                    OrderPriceType.CONTRACT_ORDER_ASK_PRICE -> {//卖一
                        activity.getString(R.string.sl_str_sell1_price)
                    }
                    OrderPriceType.CONTRACT_ORDER_PLAN -> {//计划
                        activity.getString(R.string.sl_str_sell1_price)
                        val triggerType = LogicContractSetting.getTriggerPriceType(activity)
                        order.trend = 1
                        val ticker =
                            ContractPublicDataAgent.getContractTicker(contract.instrument_id)
                        ticker?.let {
                            val trendPx = when (triggerType) {
                                1 -> {
                                    MathHelper.round(ticker.last_px)
                                }
                                2 -> {
                                    MathHelper.round(ticker.fair_px)
                                }
                                4 -> {
                                    MathHelper.round(ticker.index_px)
                                }
                                else -> {
                                    0.0
                                }
                            }

                            if (trendPx > order.px.toDouble()) {
                                order.trend = 2
                            } else {
                                order.trend = 1
                            }
                        }
                        order.trigger_type = triggerType
                        val effect = LogicContractSetting.getStrategyEffectTime(activity)
                        order.life_cycle = if (effect == 0) 24 else 168

                        if (LogicContractSetting.getExecution(activity) == 0) {
                            order.category = ContractOrder.ORDER_CATEGORY_NORMAL
                        } else {
                            order.category = ContractOrder.ORDER_CATEGORY_MARKET
                            //市价单 不传执行价格
                            order.exec_px = null
                        }

                        activity.getString(R.string.sl_str_sell1_price)
                    }
                    else -> {
                        order.px + " " + it.quote_coin
                    }
                }
                //处理高级委托
                if (isAdvancedLimit) {
                    ///time_in_force若为1(post only) category 传7,其他情况category传1(限价)
                    if (currAdvancedLimit == 1) {
                        order.category = ContractOrder.ORDER_CATEGORY_ADVANCED_NORMAL
                    } else {
                        order.category = ContractOrder.ORDER_CATEGORY_NORMAL
                    }
                    order.time_in_force = currAdvancedLimit
                }
                order.priceDisplay = priceDisplay
                return order
            }

            return order
        }

        /**
         * 计算最大可开张数
         */
        fun calculateMaxOpenVolume(
            availableVol: String,
            order: ContractOrder,
            contract: Contract
        ): Double {
            val trendType = if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) {
                ContractPosition.POSITION_TYPE_LONG
            } else {
                ContractPosition.POSITION_TYPE_SHORT
            }
            return ContractCalculate.CalculateVolume(
                availableVol,
                order.leverage,
                ContractUserDataAgent?.getContractOrderSize(
                    order.instrument_id,
                    order.side
                ),
                getContractPosition(order.instrument_id, trendType),
                order.px,
                ContractPosition.POSITION_TYPE_LONG,
                contract
            )
        }

        /**
         * 打开闪电下单提示弹窗
         */
        fun openQuickOpenOrderConfirmDialog(
            context: FragmentActivity,
            order: ContractOrder,
            contract: Contract,
            callBack: (verifyLeverage: Boolean) -> Unit
        ): TDialog {

            val account = ContractUserDataAgent.getContractAccount(contract.margin_coin)
            //最大输入本金
            val maxInputVol = 5000
            val availableVol = NumberUtil.getDecimal(contract.value_index)
                .format(account?.available_vol_real ?: 0.00)
            val originLeverage = order.leverage
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_quick_open_order_confirm_layout)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        //标题
                        setText(
                            R.id.tv_title,
                            contract.symbol + if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) context.getString(
                                R.string.str_buy_up
                            ) else
                                context.getString(R.string.str_buy_down)
                        )
                        //杠杆倍数
                        val tvLever = getView<TextView>(R.id.tv_lever)
                        tvLever.text = "${order.leverage} X"
                        //可用余额
                        getView<TextView>(R.id.tv_available_vol).text =
                            availableVol + " " + contract.margin_coin

                        //预计成交数量
                        val tvDealVol = getView<TextView>(R.id.tv_deal_vol)
                        tvDealVol.setTextUnderLine(
                            calculateMaxOpenVolume(
                                "0",
                                order,
                                contract
                            ).toString() + " " + context.getString(R.string.str_vol_unit)
                        )
                        tvDealVol.setOnClickListener {
                            SlDialogHelper.showTipPopForView(
                                context,
                                tvDealVol,
                                context.getString(R.string.str_estimated_deal_price_intro),
                                vertGravity = YGravity.ABOVE,
                                horizGravity = XGravity.LEFT,
                                y = -10
                            )
                        }
                        //预估成交价
                        val tvEstimatedDealPrice = getView<TextView>(R.id.tv_estimated_deal_price)
                        tvEstimatedDealPrice.showUnderLine()
                        tvEstimatedDealPrice.text = order.px + contract.margin_coin
                        tvEstimatedDealPrice.setOnClickListener {
                            SlDialogHelper.showTipPopForView(
                                context,
                                getView(R.id.rl_warp_estimated_layout),
                                context.getString(R.string.str_estimated_deal_price_tips),
                                y = 20
                            )
                        }

                        //下单本金
                        val inputOrderVolLayout =
                            getView<CommonInputLayout>(R.id.input_order_vol_layout)
                        inputOrderVolLayout.rightText = contract.margin_coin
                        //inputOrderVolLayout.inputText = availableVol
                        inputOrderVolLayout.inputHelperHint = String.format(
                            context.getString(
                                R.string.str_max_order_vol,
                                maxInputVol.toString() + " " + contract.margin_coin
                            )
                        )
                        inputOrderVolLayout.setMoneyType(contract.value_index)
                        val minVol = min(availableVol.toDouble(), maxInputVol.toDouble())
                        inputOrderVolLayout.setEditTextChangedListener(object :
                            CommonInputLayout.EditTextChangedListener {
                            override fun onTextChanged(view: CommonInputLayout) {
                                val inputText = MathHelper.round(view.inputText)
                                if (inputText > minVol) {
                                    inputOrderVolLayout.inputText = minVol.toString()
                                }
                                tvDealVol.setTextUnderLine(
                                    calculateMaxOpenVolume(
                                        inputOrderVolLayout.inputText,
                                        order,
                                        contract
                                    ).toString() + " " + context.getString(R.string.str_vol_unit)
                                )
                            }

                        })
                        //杠杆提示
                        val hasPositionOrOrder =
                            ContractUserDataAgent.hasPositionOrOrder(contract.instrument_id)
                        //滑动杠杆
                        val sbSeekLayout = getView<BubbleSeekBar>(R.id.sb_seek_layout)
                        //有仓位/订单 禁止修改杠杆
                        if (hasPositionOrOrder) {
                            setVisible(R.id.tv_leverage_warn, true)
                            sbSeekLayout.setCanTouchMove(false)
                        }
                        var selectLeverage = order.leverage

                        sbSeekLayout.configBuilder
                            .min(contract.min_leverage.toFloat())
                            .max(contract.max_leverage.toFloat())
                            .progress(order.leverage.toFloat())
                            .build()
                        sbSeekLayout.onProgressChangedListener =
                            object : BubbleSeekBar.OnProgressChangedListener {
                                override fun onProgressChanged(
                                    bubbleSeekBar: BubbleSeekBar?,
                                    progress: Int,
                                    progressFloat: Float
                                ) {
                                    selectLeverage = progress
                                    tvLever.text = "$selectLeverage X"
                                    order.leverage = selectLeverage
                                    tvDealVol.setTextUnderLine(
                                        calculateMaxOpenVolume(
                                            inputOrderVolLayout.inputText,
                                            order,
                                            contract
                                        ).toString() + " " + context.getString(R.string.str_vol_unit)
                                    )
                                }

                                override fun getProgressOnActionUp(
                                    bubbleSeekBar: BubbleSeekBar?,
                                    progress: Int,
                                    progressFloat: Float
                                ) {
                                }

                                override fun getProgressOnFinally(
                                    bubbleSeekBar: BubbleSeekBar?,
                                    progress: Int,
                                    progressFloat: Float
                                ) {
                                }

                            }
                        //资金费率
                        val tvFundsRate = getView<TextView>(R.id.tv_funds_rate)
                        tvFundsRate.showUnderLine()
                        val ticker = ContractPublicDataAgent.getContractTicker(order.instrument_id)
                        ticker?.let {
                            tvFundsRate.text = NumberUtil.getDecimal(4)
                                .format(MathHelper.mul(it.funding_rate, "100")) + " %"
                        }

                        tvFundsRate.setOnClickListener {
                            SlDialogHelper.showTipPopForView(
                                context,
                                tvFundsRate,
                                context.getString(R.string.str_funds_rate_intro),
                                vertGravity = YGravity.ABOVE,
                                horizGravity = XGravity.LEFT,
                                y = -10
                            )
                        }
                    }
                }
                .addOnClickListener(R.id.iv_close, R.id.bt_sure)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.iv_close -> {
                            tDialog.dismiss()
                        }
                        R.id.bt_sure -> {
                            val inputOrderVolLayout =
                                viewHolder.getView<CommonInputLayout>(R.id.input_order_vol_layout)
                            val vol = calculateMaxOpenVolume(
                                inputOrderVolLayout.inputText,
                                order,
                                contract
                            )
                            if (vol > 0) {
                                order.qty = vol.toString()
                                order.nonce = System.currentTimeMillis() / 1000
                                tDialog.dismiss()
                                callBack.invoke(order.leverage != originLeverage)
                            }
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 打开下单提示弹窗
         */
        fun openOrderConfirmDialog(
            context: FragmentActivity,
            order: ContractOrder,
            isPlanOrder: Boolean,
            warning: String = "",
            callBack: (tradeConfirm: Boolean) -> Unit
        ): TDialog? {
            val contract = ContractPublicDataAgent.getContract(order.instrument_id)
                ?: return null
            if (!isPlanOrder && (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT || order.side == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG)) {
                //限价平仓  限价9170.1USDT买入/卖出10张BTCUSDT合约
                var tips = when (order.side) {
                    ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT -> {
                        String.format(
                            context.getString(R.string.str_limit_close_confirm_tips),
                            order.px + contract.margin_coin
                                    + context.getString(R.string.str_buy_label) + contract.symbol
                        )
                    }
                    else -> {
                        String.format(
                            context.getString(R.string.str_limit_close_confirm_tips),
                            order.px + contract.margin_coin
                                    + context.getString(R.string.str_sell_label) + contract.symbol
                        )
                    }
                }
                DialogUtils.showCenterDialog(
                    context,
                    title = context.getString(R.string.str_limit_px_close),
                    content = tips,
                    clickListener = object :
                        DialogUtils.DialogBottomListener {
                        override fun clickTab(tabType: Int) {
                            if (tabType == 1) {
                                callBack.invoke(true)
                            }
                        }
                    })
            } else {
                return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                    .setLayoutRes(R.layout.dialog_open_order_confirm_layout)
                    .setScreenWidthAspect(context, 1.0f)
                    .setGravity(Gravity.BOTTOM)
                    .setDimAmount(0.8f)
                    .setCancelableOutside(true)
                    .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
                    .setOnBindViewListener { viewHolder: BindViewHolder? ->
                        viewHolder?.apply {
                            val tvStopWinLossLabel = getView<TextView>(R.id.tv_stop_win_loss_label)
                            tvStopWinLossLabel.showUnderLine()
                            //标题
                            val tvTitle = getView<TextView>(R.id.tv_title)
                            val sideName =
                                if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG || order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT)
                                    context.getString(R.string.str_buy_label) else context.getString(
                                    R.string.str_sell_label
                                )
                            if (isPlanOrder) {
                                tvTitle.text =
                                    context.getString(R.string.str_plan_label) + contract.symbol + sideName
                            } else {
                                tvTitle.text =
                                    context.getString(R.string.str_limit_label) + contract.symbol + sideName
                            }
                            val volDecimal = NumberUtil.getDecimal(contract.value_index)
                            val lastPxDecimal = NumberUtil.getDecimal(contract.price_index - 1)
                            if (isPlanOrder) {
                                //计划单
                                setGone(R.id.rl_entrust_cost_layout, false)
                                setGone(R.id.rl_entrust_value_layout, false)
                                setGone(R.id.rl_stop_win_loss_warp_layout, false)
                                setGone(R.id.item_stop_win, false)
                                setGone(R.id.item_stop_loss, false)
                                setGone(R.id.rl_deal_position_layout, false)
                                setVisible(R.id.rl_trigger_price, true)
                                setVisible(R.id.rl_effective_time_layout, true)
                                setVisible(R.id.rl_trigger_price_type_layout, true)
                                //触发价格
                                setText(
                                    R.id.tv_trigger_price,
                                    order.px + " " + contract.margin_coin
                                )
                                //触发类型
                                setText(R.id.tv_trigger_price_type, getTriggerTypeName())
                                //执行价格
                                setText(
                                    R.id.tv_price_label,
                                    context.getString(R.string.str_execution_price)
                                )
                                setText(
                                    R.id.tv_price,
                                    context.getString(R.string.sl_str_market_order)
                                )
                                if (order.category == ContractOrder.ORDER_CATEGORY_MARKET) {
                                    setText(
                                        R.id.tv_price,
                                        context.getString(R.string.sl_str_market_order)
                                    )
                                } else {
                                    setText(
                                        R.id.tv_price,
                                        order.exec_px + " " + contract.margin_coin
                                    )
                                }
                                //有效时间
                                val effect =
                                    LogicContractSetting.getStrategyEffectTime(ContractSDKAgent.context)
                                setText(
                                    R.id.tv_effective_time,
                                    if (effect == 0) context.getString(R.string.str_in_24_hours) else context.getString(
                                        R.string.str_in_7_days
                                    )
                                )
                            } else {
                                //警告
                                if (!TextUtils.isEmpty(warning)) {
                                    val tvOpenCloseRiskTips =
                                        getView<TextView>(R.id.tv_open_close_risk_tips)
                                    tvOpenCloseRiskTips.visibility = View.VISIBLE
                                    tvOpenCloseRiskTips.text = warning
                                }
                                //最新价格
                                val ticker =
                                    ContractPublicDataAgent.getContractTicker(contract.instrument_id)
                                ticker?.let {
                                    setText(
                                        R.id.tv_last_price,
                                        lastPxDecimal.format(ticker.last_px.toDouble())
                                    )
                                }
                                //预估强平价格
                                val liqPx = NumberUtil.getDecimal(contract.price_index).format(
                                    ContractCalculate.CalculateOrderLiquidatePrice(
                                        order,
                                        if (order.position_type == 2) ContractUserDataAgent.getContractAccount(
                                            contract.margin_coin
                                        ) else null,
                                        contract
                                    )
                                )
                                setText(R.id.tv_liq_price, liqPx)
                                //价格
                                setText(R.id.tv_price, order.priceDisplay)

                                //委托价值
                                val contractValue =
                                    ContractCalculate.CalculateContractValue(
                                        order.qty,
                                        order.px,
                                        contract
                                    )
                                setText(
                                    R.id.tv_entrust_value,
                                    volDecimal.format(contractValue) + " " + contract.margin_coin
                                )
                                val position = getContractPosition(
                                    order.instrument_id,
                                    if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) ContractPosition.POSITION_TYPE_LONG else ContractPosition.POSITION_TYPE_SHORT
                                )
                                //成本
                                val longOpenCost =
                                    order.calculateAdvanceOpenCost(contract, position)
                                if (longOpenCost != null) {
                                    setText(
                                        R.id.tv_entrust_cost,
                                        volDecimal.format(longOpenCost.freezAssets.toDoubleOrNull()) + " " + contract.margin_coin
                                    )
                                } else {
                                    setText(R.id.tv_entrust_cost, "0.00 " + contract.margin_coin)
                                }
                                //成交后仓位
                                val holdVol = if (position == null) "0" else position.cur_qty
                                if (order.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) {
                                    setText(
                                        R.id.tv_deal_position,
                                        volDecimal.format(MathHelper.add(order.qty, holdVol))
                                    )
                                } else {
                                    setText(
                                        R.id.tv_deal_position,
                                        volDecimal.format(
                                            max(
                                                0.0,
                                                MathHelper.sub(holdVol, order.qty)
                                            )
                                        )
                                    )
                                }

                                //止盈
                                val itemStopWin = getView<StopWinLossItem>(R.id.item_stop_win)
                                itemStopWin.isStopWin = true
                                itemStopWin.liqPrice = liqPx
                                itemStopWin.bindContract(contract, order)
                                //止损
                                val itemStopLoss = getView<StopWinLossItem>(R.id.item_stop_loss)
                                itemStopLoss.isStopWin = false
                                itemStopLoss.liqPrice = liqPx
                                itemStopLoss.bindContract(contract, order)

                                //止盈止损点击事件
                                val tvStopWinLossLabel1 =
                                    getView<TextView>(R.id.tv_stop_win_loss_label)
                                tvStopWinLossLabel1.setOnClickListener {
                                    SlDialogHelper.showTipPopForView(
                                        context, tvStopWinLossLabel1, context.getString(
                                            R.string.str_stop_win_loss_tips
                                        )
                                    )
                                }

                            }
                            //数量
                            setText(
                                R.id.tv_vol,
                                order.qty + " " + context.getString(R.string.str_vol_unit)
                            )
                            //杠杆
                            val positionType = order.position_type
                            if (positionType == 1) {
                                setText(
                                    R.id.tv_leverage,
                                    context.getString(R.string.sl_str_gradually_position) + " " + order.leverage + "X"
                                )
                            } else {
                                setText(
                                    R.id.tv_leverage,
                                    context.getString(R.string.sl_str_full_position) + " " + order.leverage + "X"
                                )
                            }

                            //不在提示
                            val tvTipsAsk = getView<TextView>(R.id.tv_tips_ask)
                            tvTipsAsk.setOnClickListener {
                                if (tvTipsAsk.isSelected) {
                                    tvTipsAsk.setDrawableLeft(R.drawable.icon_common_normal)
                                } else {
                                    tvTipsAsk.setDrawableLeft(R.drawable.icon_common_select)
                                }
                                tvTipsAsk.isSelected = !tvTipsAsk.isSelected
                            }

                        }
                    }
                    .addOnClickListener(R.id.iv_close, R.id.bt_sure)
                    .setOnViewClickListener { viewHolder, view, tDialog ->
                        when (view.id) {
                            R.id.iv_close -> {
                                tDialog.dismiss()
                            }
                            R.id.bt_sure -> {
                                order.with_mission = 0
                                if (!isPlanOrder) {
                                    //止盈
                                    val itemStopWin =
                                        viewHolder.getView<StopWinLossItem>(R.id.item_stop_win)
                                    //止损
                                    val itemStopLoss =
                                        viewHolder.getView<StopWinLossItem>(R.id.item_stop_loss)
                                    if (itemStopWin.checkTab) {
                                        if (!itemStopWin.inputBaseVerify()) {
                                            return@setOnViewClickListener
                                        }
                                        order.with_mission = 1
                                    }
                                    if (itemStopLoss.checkTab) {
                                        if (!itemStopLoss.inputBaseVerify()) {
                                            return@setOnViewClickListener
                                        }
                                        order.with_mission = 2
                                        //校验止损预警价格
                                        val warnText = itemStopLoss.verifyStopLossWarnPx()
                                        if (!TextUtils.isEmpty(warnText)) {
                                            DialogUtils.showCenterDialog(
                                                context = context,
                                                content = warnText,
                                                sureTitle = context.getString(R.string.str_continue_confirm),
                                                clickListener = object :
                                                    DialogUtils.DialogBottomListener {
                                                    override fun clickTab(tabType: Int) {
                                                        if (itemStopWin.checkTab && itemStopLoss.checkTab) {
                                                            order.with_mission = 3
                                                        }
                                                        val tvTipsAsk =
                                                            viewHolder.getView<TextView>(R.id.tv_tips_ask)
                                                        callBack.invoke(!tvTipsAsk.isSelected)
                                                        tDialog.dismiss()
                                                    }
                                                })
                                            return@setOnViewClickListener
                                        }
                                    }
                                    if (itemStopWin.checkTab && itemStopLoss.checkTab) {
                                        order.with_mission = 3
                                    }
                                }
                                val tvTipsAsk = viewHolder.getView<TextView>(R.id.tv_tips_ask)
                                callBack.invoke(!tvTipsAsk.isSelected)
                                tDialog.dismiss()
                            }
                        }
                    }
                    .create()
                    .show()
            }
            return null
        }


        private fun getTriggerTypeName(): String {
            val triggerType = LogicContractSetting.getTriggerPriceType(ContractSDKAgent.context)
            var triggerTypeText = ""
            when (triggerType) {
                1 -> {
                    triggerTypeText =
                        ContractSDKAgent.context!!.getString(R.string.str_latest_price)
                }
                2 -> {
                    triggerTypeText = ContractSDKAgent.context!!.getString(R.string.str_fair_price)
                }
                4 -> {
                    triggerTypeText = ContractSDKAgent.context!!.getString(R.string.str_index_price)
                }
            }
            return triggerTypeText
        }

        /**
         * 高级委托模式选择对话框
         */
        fun showOrderAdvancedTypeDialog(
            context: FragmentActivity,
            list: ArrayList<TabInfo>,
            position: Int,
            callBack: (selectIndex: Int, showText: String) -> Unit
        ): TDialog {
            return DialogUtils.showListDialog(
                context,
                list,
                position,
                object : DialogUtils.DialogOnItemClickListener {
                    override fun clickItem(position: Int) {
                        val selectIndex = list[position].index
                        val showText = when (selectIndex) {
                            1 -> {
                                context.getString(R.string.str_tab_post_only)
                            }
                            2 -> {
                                context.getString(R.string.str_tab_fok)
                            }
                            3 -> {
                                context.getString(R.string.str_tab_ioc)
                            }
                            else -> {
                                context.getString(R.string.str_tab_post_only)
                            }
                        }
                        callBack.invoke(selectIndex, showText)
                    }
                })
        }

        /**
         * 设置方向
         */
        fun updateOrderSideUi(
            context: Context,
            side: Int,
            tvType: TextView
        ) {
            when (side) {
                ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG -> {
                    tvType.text = context.getString(R.string.str_buy_up)
                    tvType.backgroundResource = R.drawable.contract_side_label_green_bg
                }
                ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT -> {
                    tvType.text = context.getString(R.string.str_buy_down)
                    tvType.backgroundResource = R.drawable.contract_side_label_red_bg
                }
                ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT -> {
                    tvType.text = context.getString(R.string.str_sell_down)
                    tvType.backgroundResource = R.drawable.contract_side_label_green_bg
                }
                ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG -> {
                    tvType.text = context.getString(R.string.str_sell_up)
                    tvType.backgroundResource = R.drawable.contract_side_label_red_bg
                }
                else -> {
                }
            }
        }

        /**
         * 展示合理价格弹窗
         */
        fun showFairPriceDialog(activity: FragmentActivity) {
            DialogUtils.showBottomDialog(
                activity,
                activity.getString(R.string.str_fair_price),
                R.drawable.icon_fair_price_tag,
                activity.getString(R.string.str_fair_price_intro),
                "",
                "",
                null
            )
        }

        /**
         * 展示指数价格弹窗
         */
        fun showIndexPriceDialog(activity: FragmentActivity) {
            DialogUtils.showBottomDialog(
                activity,
                activity.getString(R.string.str_index_price),
                R.drawable.icon_index_price_tag,
                activity.getString(R.string.str_index_price_intro),
                "",
                "",
                null
            )
        }


        /**
         * 开通合约弹窗
         */
        fun openCreateContractDialog(
            context: FragmentActivity
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialo_open_contract_layout)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        getView<TextView>(R.id.tv_content).movementMethod =
                            ScrollingMovementMethod.getInstance()
                        val bt_sure = getView<MaterialButton>(R.id.bt_sure)
                        getView<CheckBox>(R.id.tv_tips_ask).setOnCheckedChangeListener { buttonView, isChecked ->
                            bt_sure.isEnabled = isChecked
                        }
                    }
                }
                .addOnClickListener(R.id.iv_close, R.id.bt_sure)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.iv_close -> {
                            tDialog.dismiss()
                        }
                        R.id.bt_sure -> {
                            val tvTipsAsk = viewHolder.getView<CheckBox>(R.id.tv_tips_ask)
                            if (tvTipsAsk.isChecked){
                                //开通所有合约账户
                                openAllContractAccount()
                                tDialog.dismiss()
                            }
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 开通所有合约
         */
        private fun openAllContractAccount(){
            val ids = ArrayList<Int>()
            val coinNames = ArrayList<String>()
            ContractPublicDataAgent.getContracts().forEach {
                val coin = it.margin_coin
                if(!coinNames.contains(coin) && ContractUserDataAgent.getContractAccount(coin) == null){
                    coinNames.add(coin)
                    ids.add(it.instrument_id)
                }
            }
            var count = 0
            ids.forEachIndexed { index, value  ->
                ContractUserDataAgent.doCreateContractAccount(value,object : IResponse<String>(){
                    override fun onSuccess(data: String) {
                        count++
                        if (count == ids.size){
                            ToastUtil.shortToast(ContractSDKAgent.context!!,ContractSDKAgent.context!!.getString(R.string.str_open_contract_success))
                        }
                    }

                    override fun onFail(code: String, msg: String) {
                        count++
                        ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
                    }
                },index == count)
            }
        }
    }



}