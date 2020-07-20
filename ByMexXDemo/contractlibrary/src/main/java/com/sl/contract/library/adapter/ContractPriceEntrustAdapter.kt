package com.sl.contract.library.adapter

import android.content.Context
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
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.widget.ContractUpDownItemLayout
import java.text.DecimalFormat
import java.util.*

/**
 * 合约限价委托
 */
class ContractPriceEntrustAdapter(context: Context, data: ArrayList<ContractOrder>) :
    BaseQuickAdapter<ContractOrder, BaseViewHolder>(
        R.layout.sl_item_contract_price_entrust, data
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
            ContractOrderHelper.updateOrderSideUi(context,item.side, tvType)
            //合约名称
            var tvContractName = getView<TextView>(R.id.tv_contract_name)
            tvContractName.text = contract!!.symbol

            //委托数量
            val itemEntrustVolume = getView<ContractUpDownItemLayout>(R.id.item_entrust_volume)
            itemEntrustVolume.title = context.getString(R.string.str_entrust_volume)+"("+context.getString(R.string.str_vol_unit)+")"
            itemEntrustVolume.content = dfVol.format(MathHelper.round(item.qty))

            //成交量
            var itemDealVolume = getView<ContractUpDownItemLayout>(R.id.item_deal_volume)
            itemDealVolume.content = dfVol.format(MathHelper.round(item.cum_qty))
            itemDealVolume.title = context.getString(R.string.str_deal_volume)+"("+context.getString(R.string.str_vol_unit)+")"

            val category: Int = item.category
            //是否是强平订单
            val isForceCloseOrder = item.isForceCloseOrder
            //委托价格
            var itemEntrustPrice = getView<ContractUpDownItemLayout>(R.id.item_entrust_price)
            itemEntrustPrice.title = context.getString(R.string.str_entrust_price) + " (" + contract!!.quote_coin + ")"
            itemEntrustPrice.content = item.px
            if (category and 127 == 2) {//市价
                itemEntrustPrice.content = context.getString(R.string.str_market_price)
            }
            //历史委托-强平订单 不显示委托价格
            if (!isCurrentEntrust && isForceCloseOrder) {
                itemEntrustPrice.content = "--"
            }

            //委托价值
            val itemEntrustValue = getView<ContractUpDownItemLayout>(R.id.item_entrust_value)
            itemEntrustValue.title = context.getString(R.string.str_entrust_value) + " (" + contract!!.quote_coin + ")"
            //历史委托-强平订单 不显示委托价值
            if (!isCurrentEntrust && isForceCloseOrder) {
                itemEntrustValue.content = "--"
            } else {
                val value: Double = ContractCalculate.CalculateContractValue(item.qty, item.px, contract)
                itemEntrustValue.content =
                    dfDefault.format(MathHelper.round(value, contract!!.value_index))
            }

            //时间
            setText(
                R.id.tv_time,
                TimeFormatUtils.timeStampToDate(
                    TimeFormatUtils.getUtcTimeToMillis(item.created_at),
                    "yyyy-MM-dd  HH:mm:ss"
                )
            )

            if(isCurrentEntrust){
                //撤单
                val tvCancel = getView<TextView>(R.id.tv_cancel)
                tvCancel.visibility = View.VISIBLE
                setVisible(R.id.tv_status,false)
                tvCancel.setOnClickListener {
                    setOnItemChildClick(tvCancel,position = getItemPosition(item))
                }
            }else{
                setVisible(R.id.tv_cancel,false)
                //状态
                val tvStatus = getView<TextView>(R.id.tv_status)
                tvStatus.visibility = View.VISIBLE
                val errno: Int = item.errno
                tvStatus.text  = if (errno == ContractOrder.ORDER_ERRNO_NOERR) {
                    context.getString(R.string.str_order_complete)
                } else if (errno == ContractOrder.ORDER_ERRNO_CANCEL) {
                    val doneVol: Double = MathHelper.round(item.cum_qty, 8)
                    if (doneVol > 0) {
                        context.getString(R.string.str_part_deal)
                    } else {
                        context.getString(R.string.str_user_canceled)
                    }
                } else {
                    context.getString(R.string.str_user_canceled)
                }
                //item点击
                val llItemWarpLayout = getView<View>(R.id.ll_item_warp_layout)
                llItemWarpLayout.setOnClickListener {
                    setOnItemChildClick(llItemWarpLayout,position = getItemPosition(item))
                }
            }
        }
    }




}