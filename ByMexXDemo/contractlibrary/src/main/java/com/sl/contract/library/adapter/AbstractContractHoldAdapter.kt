package com.sl.contract.library.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.utils.DisplayUtils
import com.sl.ui.library.widget.ViewWrapper
import java.text.DecimalFormat

/**
 * 合约仓位抽象类
 */
abstract class AbstractContractHoldAdapter(
    context: Context,
    resId: Int,
    data: ArrayList<ContractPosition>
) : BaseQuickAdapter<ContractPosition, BaseViewHolder>(
    resId, data
) {
    //0 合理价格 1 最新成交价
     var pnlCalculate = 0
     var contract: Contract? = null
     var contractTicker: ContractTicker?=null
     val rateDefault: DecimalFormat = NumberUtil.getDecimal(2)
     val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)
     var dfPrice: DecimalFormat = NumberUtil.getDecimal(-1)
     var dfVol: DecimalFormat = NumberUtil.getDecimal(-1)

    var profitRate = 0.0 //未实现盈亏
    var profitAmount = 0.0 //未实现盈亏额
    var showPositionType = "多头"
    var showPositionTypeColor = 0

    var itemWith =  0

    val  mainGreen by lazy{
         context.resources.getColor(R.color.main_green)
    }
    val  mainRed by lazy{
        context.resources.getColor(R.color.main_red)
    }

    init {
        itemWith = DisplayUtils.getWidth(context) -  DisplayUtils.dip2px(context,15f)*2
    }

    fun calculateItemWidth(){
        itemWith = if(data.size == 1){
            DisplayUtils.getWidth(context) -  DisplayUtils.dip2px(context,15f)*2
        }else{
            (DisplayUtils.getWidth(context)*0.8).toInt()
        }
    }

    fun notifyDataChanged(reSetWidth:Boolean){
        if(reSetWidth){
            calculateItemWidth()
        }
        notifyDataSetChanged()
    }

    /**
     * 调整item宽度
     */
    fun adjustItemWidth(itemView: View){
        SDKLogUtil.d("libin","itemWith:"+itemWith+";"+itemView.width)
        if(itemView.width != itemWith){
            val viewWrapper = ViewWrapper(itemView)
            ObjectAnimator.ofInt(viewWrapper, "width", itemWith).setDuration(300).start();
        }
    }

    fun initContract(item:ContractPosition):Boolean{
        if (contract == null || contract!!.instrument_id != item.instrument_id) {
            contract = ContractPublicDataAgent.getContract(item.instrument_id) ?: return false
            dfPrice = NumberUtil.getDecimal(contract!!.price_index)
            dfVol = NumberUtil.getDecimal(contract!!.vol_index)
        }
        contractTicker = ContractPublicDataAgent.getContractTicker(item.instrument_id)?:return false
        calculateProfitRateAndAmount(item)
        return true
    }

    /**
     * 计算未实现盈亏率和未实现盈亏额
     */
    fun  calculateProfitRateAndAmount(item:ContractPosition){
        when(item.side){
            ContractPosition.POSITION_TYPE_LONG -> {
                //多头
                showPositionType = context.getString(R.string.sl_str_hold_buy_open)
                showPositionTypeColor = R.drawable.contract_side_label_green_bg
                profitAmount = ContractCalculate.CalculateCloseLongProfitAmount(
                    item.cur_qty,
                    item.avg_cost_px,
                    if (pnlCalculate == 0) contractTicker?.fair_px else contractTicker?.last_px,
                    contract!!.face_value,
                    contract!!.isReserve)
                val p: Double = MathHelper.add(item.cur_qty,item.close_qty)
                val plus: Double = MathHelper.mul(
                    MathHelper.round(item.tax),
                    MathHelper.div(MathHelper.round(item.cur_qty), p))
                profitRate = MathHelper.div(profitAmount, MathHelper.add(MathHelper.round(item.im), plus)) * 100
            }
            ContractPosition.POSITION_TYPE_SHORT -> {
                //空头
                showPositionType = context.getString(R.string.sl_str_hold_sell_open)
                showPositionTypeColor =   R.drawable.contract_side_label_red_bg

                profitAmount = ContractCalculate.CalculateCloseShortProfitAmount(
                    item.cur_qty,
                    item.avg_cost_px,
                    if (pnlCalculate == 0) contractTicker?.fair_px else contractTicker?.last_px,
                    contract!!.face_value,
                    contract!!.isReserve)

                val p: Double = MathHelper.add(item.cur_qty, item.close_qty)
                val plus = MathHelper.mul(
                    MathHelper.round(item.tax),
                    MathHelper.div(MathHelper.round(item.cur_qty), p))
                profitRate = MathHelper.div(profitAmount, MathHelper.add(MathHelper.round(item.im), plus)) * 100
            }
        }

    }

    /**
     * 实际杠杆
     */
    fun getRealLever(item:ContractPosition):String{
//        return if (item.position_type == 1) {
//            ContractCalculate.CalculatePositionLeverage(item,if (pnlCalculate == 0) contractTicker!!.fair_px else contractTicker!!.last_px, contract!!).toString()
//        }else{
//            ContractCalculate.CalculatePositionLeverage(item,if (pnlCalculate == 0) contractTicker!!.fair_px else contractTicker!!.last_px, contract!!).toString()
//        }
        return MathHelper.round(item.avg_fixed_leverage).toInt().toString()
    }

    /**
     * 仓位名称
     */
    fun getPositionTypeName(item:ContractPosition):String{
        return return if (item.position_type == 1) {
            context.getString(R.string.sl_str_gradually_position)
        }else{
            context.getString(R.string.sl_str_full_position)
        }
    }
    /**
     * 强平价格
     */
    fun getLiqPrice(item:ContractPosition):Double{
        var liqPrice = 0.0
        if (item.position_type == 1) {
            liqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                item, null, contract!!)
        }else if (item.position_type == 2) {//全仓
            val contractAccount: ContractAccount? = ContractUserDataAgent.getContractAccount(contract!!.margin_coin)
            liqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                item, contractAccount, contract!!)
        }
        return liqPrice
    }

    init {
        pnlCalculate = LogicContractSetting.getPnlCalculate(context)
    }

}