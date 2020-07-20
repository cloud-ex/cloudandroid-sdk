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
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import com.sl.contract.library.widget.ContractUpDownItemLayout
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.showUnderLine
import org.jetbrains.anko.backgroundResource
import java.text.DecimalFormat
import java.util.*

/**
 * 合约计划委托
 */
class ContractPlanEntrustAdapter(context: Context, data: ArrayList<ContractOrder>) :
    BaseQuickAdapter<ContractOrder, BaseViewHolder>(
        R.layout.sl_item_contract_plan_entrust, data
    ) {
    //是否是当前委托
    private var isCurrentEntrust = true
    private val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)
    private var contract: Contract? = null
    private var dfVol = NumberUtil.getDecimal(-1)

    fun setIsCurrentEntrust(isCurrentEntrust: Boolean = true) {
        this.isCurrentEntrust = isCurrentEntrust
    }

    override fun convert(helper: BaseViewHolder, item: ContractOrder) {

        helper.apply {
            if (contract == null || contract!!.instrument_id != item.instrument_id) {
                contract = ContractPublicDataAgent.getContract(item.instrument_id) ?: return
                dfVol = NumberUtil.getDecimal(contract!!.vol_index)
            }
            //方向
            val tvType = getView<TextView>(R.id.tv_type)
            updateOrderSide(item, tvType)
            //合约名称
            var tvContractName = getView<TextView>(R.id.tv_contract_name)
            tvContractName.text = contract!!.symbol
            //仓位杠杆
            val  tvLever = getView<TextView>(R.id.tv_lever)
            when (item.position_type) {
                1 -> {
                    tvLever.text = context.getString(R.string.sl_str_gradually_position)+item.leverage+"X"
                }
                2 -> {
                    //全仓
                    tvLever.text = context.getString(R.string.sl_str_full_position)+item.leverage+"X"
                }
                else -> {
                    tvLever.text = "${item.leverage} X"
                }
            }

            //时间
            setText(
                R.id.tv_time,
                TimeFormatUtils.timeStampToDate(
                    TimeFormatUtils.getUtcTimeToMillis(item.created_at),
                    "yyyy-MM-dd  HH:mm:ss"
                )
            )
            //触发价格
            val itemTriggerPrice = getView<ContractUpDownItemLayout>(R.id.item_trigger_price)
            //委托价格类型
            var priceType = ""
            var triggerType = item.trigger_type
            when {
                triggerType === 1 -> {
                    priceType =  context.getString(R.string.str_last_price_simple)
                }
                triggerType === 2 -> {
                    priceType =  context.getString(R.string.str_fair_price_simple)
                }
                triggerType === 4 -> {
                    priceType =  context.getString(R.string.str_index_price_simple)
                }
            }
            itemTriggerPrice.title =  context.getString(R.string.str_trigger_price)+" (" +contract!!.quote_coin +")"
            itemTriggerPrice.content = priceType+ " " + dfDefault.format(MathHelper.round(item.px, contract!!.price_index))
            //执行价格
            val itemExecutionPrice = getView<ContractUpDownItemLayout>(R.id.item_execution_price)
            itemExecutionPrice.title =  context.getString(R.string.str_execution_price)+" (" +contract!!.quote_coin +")"
            val category: Int = item.category
            if (category and 127 == 2){
                itemExecutionPrice.content = context.getString(R.string.str_market_price)
            }else{
                itemExecutionPrice.content = item.exec_px
            }

            //执行数量
            var itemExecutionVolume = getView<ContractUpDownItemLayout>(R.id.item_execution_volume)
            itemExecutionVolume.title = context.getString(R.string.str_execution_volume)+"("+context.getString(R.string.str_vol_unit)+")"
            //止盈止损单 执行数量显示100%
            if(item.type == 1 || item.type == 2){
                itemExecutionVolume.content = "100%"
            }else{
                itemExecutionVolume.content = dfVol.format(MathHelper.round(item.qty))
            }
            //到期时间
            var itemDeadlineTime = getView<ContractUpDownItemLayout>(R.id.item_deadline_time)
            val createdTime = TimeFormatUtils.getUtcTimeToMillis(item.created_at)
            //原时间加上周期时间
            var expireTime =  if(item.life_cycle == 24){
                TimeFormatUtils.timeStampToDate(createdTime+1000*60*60*24,"yyyy/MM/dd  HH:mm")
            }else{
                TimeFormatUtils.timeStampToDate(createdTime+1000*60*60*24*7,"yyyy/MM/dd  HH:mm")
            }
            if(isCurrentEntrust){
                tvLever.visibility = if(item.leverage > 0) View.VISIBLE else View.GONE
                itemDeadlineTime.visibility = View.VISIBLE
                setVisible(R.id.tv_cancel,true)
                setVisible(R.id.view_dash,false)
                setGone(R.id.ll_bottom_layout,true)
                setVisible(R.id.tv_status,false)

                itemDeadlineTime.content = expireTime
                //撤单
                val tvCancel = getView<TextView>(R.id.tv_cancel)
                tvCancel.setOnClickListener {
                    setOnItemChildClick(tvCancel,position = getItemPosition(item))
                }
            }else{
                val tvStatus = getView<TextView>(R.id.tv_status)
                tvLever.visibility = View.GONE
                tvStatus.visibility = View.VISIBLE
                itemDeadlineTime.visibility = View.INVISIBLE
                setVisible(R.id.tv_cancel,false)
                setVisible(R.id.view_dash,true)
                setVisible(R.id.ll_bottom_layout,true)
                //到期时间
                setText(R.id.tv_deadline_time,expireTime)
                //触发时间
                setText(R.id.tv_trigger_time,TimeFormatUtils.timeStampToDate(TimeFormatUtils.getUtcTimeToMillis(item.finished_at),"yyyy/MM/dd  HH:mm"))
                //委托状态
                val errno: Int = item.errno
                when(errno){
                    ContractOrder.ORDER_ERRNO_NOERR ->{
                        tvStatus.text = context.getString(R.string.str_order_complete)
                    }
                    ContractOrder.ORDER_ERRNO_CANCEL ->{
                        tvStatus.text = context.getString(R.string.str_user_canceled)
                    }
                    ContractOrder.ORDER_ERRNO_TIMEOUT ->{
                        tvStatus.text = context.getString(R.string.str_order_timeout)
                    }
                    else ->{
                        tvStatus.text = context.getString(R.string.str_trigger_failed)
                    }
                }
                if (errno >= ContractOrder.ORDER_ERRNO_ASSETS) {
                    tvStatus.showUnderLine()
                    tvStatus.setOnClickListener {
                        queryDetail(item)
                    }
                }else{
                    tvStatus.showUnderLine(false)
                }
            }

        }
    }

    private fun queryDetail(order: ContractOrder) {
        var createAt = if (TextUtils.isEmpty(order.finished_at)) order.created_at else order.finished_at
        var time =TimeFormatUtils.timeStampToDate(TimeFormatUtils.getUtcTimeToMillis(createAt),"yyyy-MM-dd  HH:mm:ss")
        var price_type = ""
        when {
            order.trigger_type === 1 -> {
                price_type = context.getString(R.string.str_last_price_simple)
            }
            order.trigger_type === 2 -> {
                price_type =  context.getString(R.string.str_fair_price_simple)
            }
            order.trigger_type === 4 -> {
                price_type = context.getString(R.string.str_index_price_simple)
            }
        }

        var reason: String
        var content: String
        when (order.errno) {
            3 -> {
                reason =   context.getString(R.string.str_insufficient) 
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            4 -> {
                reason = context.getString(R.string.str_trigger_failed_reason4)
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            6 -> {
                reason = context.getString(R.string.str_trigger_failed_reason6)
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            7 -> {
                reason = context.getString(R.string.str_trigger_failed_reason7)
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            8 -> {
                reason = context.getString(R.string.str_trigger_failed_reason8)
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            9 -> {
                reason = context.getString(R.string.str_trigger_failed_reason9)
                content = java.lang.String.format(context.getString(R.string.str_trigger_failed_info),
                    time,
                    contract!!.symbol, price_type,
                    dfDefault.format(MathHelper.round(order.px, contract!!.price_index)) + contract!!.quote_coin,
                    reason)
            }
            5 -> content = context.getString(R.string.str_trigger_failed_reason5)
            10 -> content = context.getString(R.string.str_trigger_failed_reason10)
            11 -> content = context.getString(R.string.str_trigger_failed_reason11)
            12 -> content = context.getString(R.string.str_trigger_failed_reason12)
            13 -> content = context.getString(R.string.str_trigger_failed_reason13)
            else -> content = context.getString(R.string.str_trigger_failed_reason13)
        }

        DialogUtils.showCenterDialog(context,context.getString(R.string.str_tips),content,"",context.getString(R.string.common_text_btn_i_see),null)
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



}