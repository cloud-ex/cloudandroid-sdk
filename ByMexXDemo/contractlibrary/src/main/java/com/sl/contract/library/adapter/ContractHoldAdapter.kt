package com.sl.contract.library.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.sl.contract.library.R
import com.sl.contract.library.widget.ContractUpDownItemLayout
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.showUnderLine
import org.jetbrains.anko.backgroundResource

/**
 *  合约仓位
 */
class ContractHoldAdapter(context: Context, data: ArrayList<ContractPosition>) :
    AbstractContractHoldAdapter(
        context,
        R.layout.item_contract_position_layout, data
    ) {


    override fun convert(helper: BaseViewHolder, item: ContractPosition) {
        if (!initContract(item)) {
            return
        }

        helper.apply {
            val positionType = item.position_type
            //币种名称
            setText(R.id.tv_name, contract!!.symbol)
            //杠杆
            if (positionType == 1) {
                getView<TextView>(R.id.tv_lever).text =
                    context.getString(R.string.sl_str_gradually_position) + getRealLever(item) + "X"
            } else if (positionType == 2) {
                getView<TextView>(R.id.tv_lever).text =
                    context.getString(R.string.sl_str_full_position) + getRealLever(item) + "X"
            }

            //可平量
            getView<TextView>(R.id.tv_close_vol).text = dfVol.format(MathHelper.round(item.cur_qty))
            //持仓量
            getView<TextView>(R.id.tv_hold_vol).text =
                dfVol.format(MathHelper.sub(item.cur_qty, item.freeze_qty))
            //持仓均价
            context.resources.getColor(R.color.main_red)
            getView<ContractUpDownItemLayout>(R.id.item_hold_px).content =
                " ${dfPrice.format(MathHelper.round(item.avg_cost_px))}<font  color='${ContextCompat.getColor(
                    context,
                    R.color.hint_color
                )}'><small> ≈ $0.00</small> </font>"
            //强平价格
            val itemLiqPrice = getView<ContractUpDownItemLayout>(R.id.item_liq_price)
            item.liquidate_price = dfPrice.format(getLiqPrice(item))
            itemLiqPrice.content = item.liquidate_price

            itemLiqPrice.setExplainListener(View.OnClickListener {
                DialogUtils.showBottomDialog(
                    context = context,
                    content = context.getString(R.string.str_liq_price_tips),
                    clickListener = null
                )
            }, true)
            //仓位类型
            val tvPositionType = getView<TextView>(R.id.tv_position_type)
            tvPositionType.text = showPositionType
            tvPositionType.backgroundResource = showPositionTypeColor
            //回报率
            val itemFloatingGains = getView<ContractUpDownItemLayout>(R.id.item_floating_gains)
            itemFloatingGains.setExplainListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    DialogUtils.showBottomDialog(
                        context = context,
                        content = context.getString(R.string.str_floating_gains_tips),
                        clickListener = null
                    )
                }
            }, true)
            itemFloatingGains.content = if (profitRate >= 0) {
                "+"
            } else {
                ""
            } +
                    rateDefault.format(profitRate).toString() + "%"
            //未实现盈亏
            val itemUnrealisedPnl = getView<ContractUpDownItemLayout>(R.id.item_unrealised_pnl)
            itemUnrealisedPnl.setExplainListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    DialogUtils.showBottomDialog(
                        context = context,
                        content = context.getString(R.string.str_unrealised_pnl_tips),
                        clickListener = null
                    )
                }
            }, true)
            itemUnrealisedPnl.content = if (profitAmount > 0) {
                "+"
            } else {
                ""
            } + dfVol.format(MathHelper.round(profitAmount, contract!!.value_index))
            if (profitAmount >= 0) {
                itemFloatingGains.contentTextColor = mainGreen
                itemUnrealisedPnl.contentTextColor = mainGreen
            } else {
                itemFloatingGains.contentTextColor = mainRed
                itemUnrealisedPnl.contentTextColor = mainRed
            }
            //保证金
            setText(
                R.id.tv_margins,
                dfDefault.format(MathHelper.round(item.im, contract!!.value_index))
            )
            //已实现盈亏
            val tvGainsBalance = getView<TextView>(R.id.tv_gains_balance)
            tvGainsBalance.text = dfDefault.format(
                MathHelper.round(
                    MathHelper.round(item.earnings),
                    contract!!.value_index
                )
            ) + " " + contract!!.margin_coin
            tvGainsBalance.showUnderLine()
            tvGainsBalance.setOnClickListener {
                setOnItemChildClick(tvGainsBalance, getItemPosition(item))
            }

            //调整保证金
            val tvAdjustMargins = getView<TextView>(R.id.tv_adjust_margins)
            tvAdjustMargins.setOnClickListener {
                setOnItemChildClick(tvAdjustMargins, getItemPosition(item))
            }
            //获得止盈止损订单
            ContractUserDataAgent.getContractStopPlanOrder(
                item.instrument_id,
                item.side
            ) { stopWinOrder: ContractOrder?, stopFairOrder: ContractOrder? ->
                val tvStopWinPxLabel = getView<TextView>(R.id.tv_stop_win_px_label)
                val tvStopWinPx = getView<TextView>(R.id.tv_stop_win_px)
                val tvStopLossPxLabel = getView<TextView>(R.id.tv_stop_loss_px_label)
                val tvStopLossPx = getView<TextView>(R.id.tv_stop_loss_px)
                tvStopWinPxLabel.visibility = if (stopWinOrder == null) View.GONE else View.VISIBLE
                tvStopWinPx.visibility = if (stopWinOrder == null) View.GONE else View.VISIBLE
                tvStopLossPxLabel.visibility =
                    if (stopFairOrder == null) View.GONE else View.VISIBLE
                tvStopLossPx.visibility = if (stopFairOrder == null) View.GONE else View.VISIBLE
                stopWinOrder?.let {
                    tvStopWinPx.text =
                        dfPrice.format(MathHelper.round(it.px)) + "/" + ContractCalculate.calculateOrderProfitLossRate(
                            contract!!,
                            it.side,
                            item.cur_qty,
                            item.avg_cost_px,
                            it.px,
                            true
                        ) + "%"
                }
                stopFairOrder?.let {
                    tvStopLossPx.text =
                        dfPrice.format(MathHelper.round(it.px)) + "/" + ContractCalculate.calculateOrderProfitLossRate(
                            contract!!,
                            it.side,
                            item.cur_qty,
                            item.avg_cost_px,
                            it.px,
                            true
                        ) + "%"
                }
            }
            //止盈止损
            val tvStopProfitLoss = getView<TextView>(R.id.tv_stop_profit_loss)
            tvStopProfitLoss.setOnClickListener {
                setOnItemChildClick(tvStopProfitLoss, getItemPosition(item))
            }
            //平仓
            val tvClosePosition = getView<TextView>(R.id.tv_close_position)
            tvClosePosition.setOnClickListener {
                setOnItemChildClick(tvClosePosition, getItemPosition(item))
            }
            //分享
            val tv_share = getView<TextView>(R.id.tv_share)
            tv_share.setOnClickListener {
                setOnItemChildClick(tv_share, getItemPosition(item))
            }
        }
    }

}