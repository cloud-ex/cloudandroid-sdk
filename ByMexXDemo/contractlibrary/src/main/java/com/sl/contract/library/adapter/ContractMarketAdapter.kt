package com.sl.contract.library.adapter

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import org.jetbrains.anko.backgroundResource
import java.util.*

/**
 * 合约行情
 */
class ContractMarketAdapter(ctx: Context, data: ArrayList<ContractTicker>) :
    BaseQuickAdapter<ContractTicker, BaseViewHolder>(
        R.layout.view_contact_market_item_layout,
        data
    ) {
    override fun convert(helper: BaseViewHolder, item: ContractTicker) {
        helper?.run {
            setText(R.id.tv_name, item.symbol)
            val contract = ContractPublicDataAgent.getContract(item.instrument_id) ?: return
            val dfVol = NumberUtil.getDecimal(contract.vol_index)
            val dfPrice = NumberUtil.getDecimal(contract.price_index)
            val dfRate = NumberUtil.getDecimal(2)
            //成交量
            val vol = MathHelper.round(item.qty24, contract.vol_index)
            setText(
                R.id.tv_vol,
                context.getString(R.string.str_deal_vol) + NumberUtil.getBigVolum(
                    context,
                    dfVol,
                    vol
                )
            )
            //最新价
            val lastPx: Double =
                MathHelper.round(item.last_px, contract.price_index)
            val sUsd = (if (TextUtils.equals(
                    contract.quote_coin,
                    "USDT"
                )
            ) "$" else "") + dfPrice.format(lastPx)
            setText(R.id.tv_price,sUsd)
            //换算价格  需要外部费率
            val currentCny: Double =
                MathHelper.round(MathHelper.mul(lastPx, 0.0), 2)
            setText(R.id.tv_cny_price, "￥" + dfRate.format(currentCny))
            //涨跌幅
            val chg: Double =
                MathHelper.round(item.change_rate.toDouble() * 100, 2)
            val tvRate = getView<TextView>(R.id.tv_rate);
            tvRate.run {
                text =
                    if (chg >= 0) "+" + dfRate.format(chg) + "%" else dfRate.format(chg) + "%"
                backgroundResource = if (chg >=0) R.drawable.btn_contract_market_green_fill else R.drawable.btn_contract_market_red_fill
            }
        }
    }
}