package com.sl.contract.library.adapter

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.utils.MathHelper
import com.sl.contract.library.R
import com.sl.contract.library.widget.ContractUpDownItemLayout
import org.jetbrains.anko.backgroundResource

/**
 *  横向闪电 合约仓位
 */
class HQuickContractHoldAdapter(context: Context, data:ArrayList<ContractPosition>) :
    AbstractContractHoldAdapter(
        context,
        R.layout.item_contract_position_h_quick_layout, data
    ) {


    override fun convert(helper: BaseViewHolder, item: ContractPosition) {
        if (!initContract(item)){
            return
        }
        helper.apply {
            //Item 宽度
            val ll_item_warp_layout = getView<LinearLayout>(R.id.ll_item_warp_layout)
            adjustItemWidth(ll_item_warp_layout)
            //名称
            getView<TextView>(R.id.tv_name).text = contract!!.symbol
            //杠杆
            getView<TextView>(R.id.tv_lever).text = getRealLever(item)+"X"
            //持仓均价
            getView<ContractUpDownItemLayout>(R.id.item_hold_px).content =
                dfPrice.format(MathHelper.round(item.avg_cost_px))
            //强平价格
            getView<ContractUpDownItemLayout>(R.id.item_liq_price).content = dfPrice.format(getLiqPrice(item))
            val tvPositionType = getView<TextView>(R.id.tv_position_type)
            //仓位类型
            tvPositionType.text = showPositionType
            tvPositionType.backgroundResource =  showPositionTypeColor
            //回报率
            val itemFloatingGains = getView<ContractUpDownItemLayout>(R.id.item_floating_gains)
            itemFloatingGains.content = if (profitRate >= 0) {
                "+"
            } else {
                ""
            } +
                    rateDefault.format(profitRate).toString() + "%"
            itemFloatingGains.contentTextColor = if (profitAmount >= 0) {
                context.resources.getColor(R.color.main_green)
            } else {
                context.resources.getColor(R.color.main_red)
            }

            //未实现盈亏额
            val itemUnrealisedPnl =  getView<ContractUpDownItemLayout>(R.id.item_unrealised_pnl)
            itemUnrealisedPnl.content = if (profitAmount >= 0){"+"}else{""} + dfVol.format(MathHelper.round(profitAmount, contract!!.value_index))
            itemUnrealisedPnl.contentTextColor = if (profitAmount >= 0) {
                context.resources.getColor(R.color.main_green)
            } else {
                context.resources.getColor(R.color.main_red)
            }

            //闪电平仓
            val tvQuickClose = getView<TextView>(R.id.tv_quick_close)
            tvQuickClose.setOnClickListener {
                setOnItemChildClick(tvQuickClose,getItemPosition(item))
            }
            //item点击
            ll_item_warp_layout.setOnClickListener {
                setOnItemChildClick(ll_item_warp_layout,getItemPosition(item))
            }

        }
    }

}