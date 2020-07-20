package com.sl.contract.library.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import java.util.*

/**
 *  仓位资金明细
 */
class PositionTaxDetailAdapter(val contract:Contract,data: ArrayList<ContractPosition>) : BaseQuickAdapter<ContractPosition, BaseViewHolder>(
    R.layout.item_position_tax_detail_layout, data) {

    var pxDecimal = NumberUtil.getDecimal(-1)
    init {
        pxDecimal = NumberUtil.getDecimal(contract.price_index)
    }

    override fun convert(helper: BaseViewHolder, item: ContractPosition) {
        helper?.run {
            //资金费用
            setText(R.id.tv_tax_fee,"${ NumberUtil.getDecimal(8).format(item.tax.toDouble())} ${contract.quote_coin}")
            //仓位数量
            setText(R.id.tv_position_vol,"${item.qty} ${context.getString(R.string.str_vol_unit)}")
            //合理价格
            setText(R.id.tv_fair_price,"${pxDecimal.format(item.fair_px.toDouble())} ${contract.getQuote_coin()}")
            //时间
            setText(R.id.tv_time,TimeFormatUtils.timeStampToDate(TimeFormatUtils.getUtcTimeToMillis(item.created_at),"yyyy-MM-dd  HH:mm:ss"))
        }
    }

}