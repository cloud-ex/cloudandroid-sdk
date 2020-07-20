package com.sl.contract.library.adapter

import android.content.Context
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.ContractFundingRate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import java.util.*

/**
 * 资金费率
 */
class FundsRateAdapter(data: ArrayList<ContractFundingRate>) : BaseQuickAdapter<ContractFundingRate, BaseViewHolder>(
    R.layout.item_funding_rate_layout, data) {

    override fun convert(helper: BaseViewHolder, item: ContractFundingRate) {
        helper?.run {

            //资金费率
            val rate = MathHelper.mul(item.rate, "100")
            getView<TextView>(R.id.tv_funding_rate).text = NumberUtil.getDecimal(4).format(rate).toString() + "%"
            //时间
            getView<TextView>(R.id.tv_time).text = TimeFormatUtils.timeStampToDate((item.timestamp)*1000,"yyyy-MM-dd  HH:mm:ss")
        }
    }

}