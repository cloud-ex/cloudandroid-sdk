package com.sl.contract.library.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import com.sl.contract.library.widget.ContractUpDownItemLayout
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs

/**
 * 合约委托明细
 */
class ContractEntrustDetailAdapter(context: Context, data: ArrayList<ContractOrder>) :
    BaseQuickAdapter<ContractOrder, BaseViewHolder>(
        R.layout.item_contract_entruse_detail_layout, data
    ) {
    private val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)
    private var contract: Contract? = null
    private var dfVol = NumberUtil.getDecimal(-1)

    var isForceCloseOrder = false

    override fun convert(helper: BaseViewHolder, item: ContractOrder) {

        helper.apply {
            if (contract == null) {
                contract = ContractPublicDataAgent.getContract(item.instrument_id) ?: return
                dfVol = NumberUtil.getDecimal(contract!!.vol_index)
            }

            //成交时间
            setText(
                R.id.tv_deal_time,
                TimeFormatUtils.timeStampToDate(
                    TimeFormatUtils.getUtcTimeToMillis(item.created_at),
                    "yyyy-MM-dd  HH:mm:ss"
                )
            )
            //成交价格
            if (isForceCloseOrder) {
                setText(R.id.tv_deal_price, "--")
            }else{
                setText(R.id.tv_deal_price, dfDefault.format(MathHelper.round(item.px, contract!!.price_index))+" "+contract!!.quote_coin)
            }
            //成交量
            setText(R.id.tv_deal_vol, dfVol.format(MathHelper.round(item.qty))+" "+context.getString(R.string.str_vol_unit))
            //成交金额
            val value: Double = ContractCalculate.CalculateContractValue(
                item.qty,
                item.px,
                contract)
            if(isForceCloseOrder){
                setText(R.id.tv_deal_px, "--")
            }else{
                setText(R.id.tv_deal_px, dfDefault.format(MathHelper.round(value, contract!!.price_index))+" "+contract!!.margin_coin)
            }
            //手续费
            if(!TextUtils.isEmpty(item.take_fee)&& item.take_fee.toDouble() > 0){
                setText(R.id.tv_fee, item.take_fee)
            }else  if(!TextUtils.isEmpty(item.make_fee)){
                var makeFee = abs(item.make_fee.toDouble())
                setText(R.id.tv_fee, makeFee.toString())
            }else{
                setText(R.id.tv_fee, item.take_fee)
            }
        }
    }

    /**
     * 设置方向
     */
    private fun updateOrderSide(
        item: ContractOrder,
        tvType: TextView
    ) {
        when (item.side) {
            ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG -> {
                tvType.text = context.getString(R.string.str_buy_up)
                tvType.setTextColor(ContextCompat.getColor(context, R.color.main_green))
            }
            ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT -> {
                tvType.text = context.getString(R.string.str_buy_down)
                tvType.setTextColor(ContextCompat.getColor(context, R.color.main_red))
            }
            ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT -> {
                tvType.text = context.getString(R.string.str_sell_down)
                tvType.setTextColor(ContextCompat.getColor(context, R.color.main_green))
            }
            ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG -> {
                tvType.text = context.getString(R.string.str_sell_up)
                tvType.setTextColor(ContextCompat.getColor(context, R.color.main_red))
            }
            else -> {
            }
        }
    }


}