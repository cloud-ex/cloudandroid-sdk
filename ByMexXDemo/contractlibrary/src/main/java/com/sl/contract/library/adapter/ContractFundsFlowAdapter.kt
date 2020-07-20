package com.sl.contract.library.adapter

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractCashBook
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 合约资金流水
 */
class ContractFundsFlowAdapter(data: ArrayList<ContractCashBook>) :
    BaseQuickAdapter<ContractCashBook, BaseViewHolder>(R.layout.view_contract_funds_flow_item_layout, data),
    LoadMoreModule {
    var decimalFormat = DecimalFormat(
        "###################.###########",
        DecimalFormatSymbols(Locale.ENGLISH)
    )

    override fun convert(holder: BaseViewHolder, item: ContractCashBook) {
        holder.apply {
            var volIndex = ContractPublicDataAgent.getContract(item.coin_code)?.vol_index ?: 4
            //日期
            setText(R.id.tv_time,TimeFormatUtils.convertZTime(item.created_at,TimeFormatUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND))
            var typeString = "--"
            var symbol = ""
            //类型
            when(item.action){
                1 -> {//买入开多
                    typeString = item.coin_code+context.getString(R.string.str_buy_up)
                    symbol = "+ "
                }
                2 -> {//买入平空
                    typeString = item.coin_code+context.getString(R.string.str_sell_down)
                    symbol = "- "
                }
                3 -> {//卖出平多
                    typeString = item.coin_code+context.getString(R.string.str_sell_up)
                    symbol = "+ "
                }
                4 -> {//卖出开空
                    typeString = item.coin_code+context.getString(R.string.str_buy_down)
                    symbol = "+ "
                }
                5,7 -> {//转入
                    typeString = item.coin_code+context.getString(R.string.str_transfer_in)
                    symbol = "- "
                }
                6,8 -> {//转出
                    typeString = item.coin_code+context.getString(R.string.str_transfer_out)
                    symbol = "+ "
                }
                9 -> {//减少保证金
                    typeString = item.coin_code+context.getString(R.string.str_decrease_margin)
                    symbol = "+ "
                }
                10 -> {//增加保证金
                    typeString = item.coin_code+context.getString(R.string.str_add_margin)
                    symbol = "- "
                }
                11 -> {//资金费率
                    typeString = item.coin_code+context.getString(R.string.str_funds_rate)
                    symbol = "- "
                }
                20 -> {//手续费分成
                    typeString = item.coin_code+ context.getString(R.string.str_fee_share)
                    symbol = "- "
                }
                21 -> {//空投
                    typeString = item.coin_code+context.getString(R.string.str_air_drop)
                    symbol = "- "
                }
            }
            //量
            val tvVol = getView<TextView>(R.id.tv_vol)
            if(symbol.contains("+")){
                tvVol.setTextColor(context.resources.getColor(R.color.main_green))
            }else{
                tvVol.setTextColor(context.resources.getColor(R.color.main_red))
            }
            tvVol.text = symbol+decimalFormat.format( MathHelper.round(item.deal_count, volIndex) )+" "+item.coin_code
            setText(R.id.tv_name, typeString)
            //手续费
            getView<TextView>(R.id.tv_fee).text =  symbol+decimalFormat.format( MathHelper.round(item.fee, volIndex) )+" "+item.coin_code

        }
    }
}