package com.sl.contract.library.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.data.ContractTicker
import com.sl.contract.library.R
import java.util.*

/**
 * 合约行情
 */
class ContractRankAdapter(ctx: Context, data: ArrayList<ContractTicker>) :
    BaseQuickAdapter<ContractTicker, BaseViewHolder>(
        R.layout.view_contact_rank_item_layout,
        data
    ) {
    override fun convert(helper: BaseViewHolder, item: ContractTicker) {
    }
}