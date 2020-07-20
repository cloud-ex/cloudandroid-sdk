package com.sl.contract.library.adapter

import android.content.Context
import android.widget.TextView
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import com.sl.contract.library.widget.ContractUpDownItemLayout
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.textColor

/**
 *  合约历史仓位
 */
class ContractHoldHistoryAdapter(context: Context, data: ArrayList<ContractPosition>) :
    AbstractContractHoldAdapter(
        context,
        R.layout.item_contract_history_position_layout, data
    ) , LoadMoreModule {


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
            //时间
            setText(
                R.id.tv_time,
                TimeFormatUtils.timeStampToDate(
                    TimeFormatUtils.getUtcTimeToMillis(item.updated_at),
                    "yyyy-MM-dd  HH:mm:ss"
                )
            )
            //开仓均价
            var itemOpenPrice = getView<ContractUpDownItemLayout>(R.id.item_open_price)
            itemOpenPrice.content = dfDefault.format(MathHelper.round(item.avg_open_px, contract!!.price_index))
            itemOpenPrice.title = context.getString(R.string.str_open_avg_price)+" (${contract!!.quote_coin})"

            //平仓均价
            var itemAvgClosePx = getView<ContractUpDownItemLayout>(R.id.item_avg_close_px)
            itemAvgClosePx.content = dfDefault.format(MathHelper.round(item.avg_close_px, contract!!.price_index))
            itemAvgClosePx.title = context.getString(R.string.str_avg_close_px)+" (${contract!!.quote_coin})"

            //仓位类型
            val tvPositionType = getView<TextView>(R.id.tv_position_type)
            tvPositionType.text = showPositionType
            tvPositionType.backgroundResource = showPositionTypeColor
            //已实现盈亏
            getView<ContractUpDownItemLayout>(R.id.item_gains_balance).content = dfDefault.format(
                MathHelper.round(
                    MathHelper.round(item.earnings),
                    contract!!.value_index
                )
            ) + " " + contract!!.margin_coin
        }
    }

}