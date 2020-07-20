package com.sl.contract.library.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.InsuranceFund
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.ui.library.utils.DateUtils

class InsuranceFundAdapter(data: ArrayList<InsuranceFund>) : BaseQuickAdapter<InsuranceFund, BaseViewHolder>(
    R.layout.item_insturance_funding_layout, data) {
    override fun convert(holder: BaseViewHolder, item: InsuranceFund) {
        holder?.run {
            val contract: Contract = ContractPublicDataAgent.getContract(item.instrument_id)
                ?: return

            val vol: Double = MathHelper.round(item.vol, contract.value_index)
            getView<TextView>(R.id.tv_remain_value).text = NumberUtil.getDecimal(-1).format(vol) +" " + contract.margin_coin

            getView<TextView>(R.id.tv_time_value).text = DateUtils.longToString(DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND,item.timestamp)
        }
    }
}