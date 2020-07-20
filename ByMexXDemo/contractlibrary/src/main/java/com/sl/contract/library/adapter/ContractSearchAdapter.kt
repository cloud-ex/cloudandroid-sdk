package com.sl.contract.library.adapter

import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.data.ContractSearchInfo
import org.jetbrains.anko.textColor

class ContractSearchAdapter(
    layoutResId: Int,
    sectionHeadResId: Int,
    data: MutableList<ContractSearchInfo>
) : BaseSectionQuickAdapter<ContractSearchInfo, BaseViewHolder>(
    layoutResId,
    sectionHeadResId,
    data
) {

    private var dfRate = NumberUtil.getDecimal(2)
    var mContractId = 0


    override fun convertHeader(
        baseViewHolder: BaseViewHolder,
        contractSearchInfo: ContractSearchInfo
    ) {
        baseViewHolder?.let {
            it.setText(R.id.tv_tab_title, contractSearchInfo.contractTicker.symbol)
        }
    }


    override fun convert(baseViewHolder: BaseViewHolder, contractSearchInfo: ContractSearchInfo) {
        contractSearchInfo.contractTicker?.let {
            val tvName = baseViewHolder.getView<TextView>(R.id.tv_name)
            tvName.text = it.symbol
            baseViewHolder.setText(R.id.tv_price, dfRate.format(MathHelper.round(it.last_px)))

            val chg = MathHelper.round(it.change_rate.toDouble() * 100, 2)
            val tvRate = baseViewHolder?.getView<TextView>(R.id.tv_rate)

            tvRate?.run {
                text = if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                textColor =
                    if (chg >= 0) context.resources.getColor(R.color.main_green) else context.resources.getColor(
                        R.color.main_red
                    )
            }
            //选中背景
            val rlLayout = baseViewHolder?.getView<RelativeLayout>(R.id.rl_layout)
            rlLayout?.run {
                if (mContractId == it.instrument_id) {
                    setBackgroundColor(context.resources.getColor(R.color.bg_item_color))
                    tvName.setTextColor(ContextCompat.getColor(context,R.color.main_yellow))
                } else {
                    setBackgroundColor(context.resources.getColor(R.color.transparent))
                    tvName.setTextColor(ContextCompat.getColor(context,R.color.text_color))
                }
            }

        }
    }


}