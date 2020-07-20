package com.sl.contract.library.adapter

import android.content.Context
import android.text.TextUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractTrade
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.extra.Contract.ContractCalculate.getVolUnitNoSuffix
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import org.jetbrains.anko.textColor
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs

/**
 * 合约交易记录
 */
class ContractTradeAdapter(context: Context, data: ArrayList<ContractTrade>) :
    BaseQuickAdapter<ContractTrade, BaseViewHolder>(
        R.layout.item_contract_trade_record_layout, data
    ) {
    private var contract: Contract? = null
    private var dfVol = NumberUtil.getDecimal(-1)
    private var dfPrice = NumberUtil.getDecimal(-1)


    override fun convert(helper: BaseViewHolder, item: ContractTrade) {

        helper.apply {
            if (contract == null) {
                contract = ContractPublicDataAgent.getContract(item.instrument_id) ?: return
                dfVol = NumberUtil.getDecimal(contract!!.vol_index)
                dfPrice = NumberUtil.getDecimal(contract!!.price_index - 1);
            }

            //时间
            setText(
                R.id.tv_time,
                TimeFormatUtils.timeStampToDate(
                    TimeFormatUtils.getUtcTimeToMillis(item.created_at),
                    "HH:mm:ss"
                )
            )
            //方向
            val tvSide = getView<TextView>(R.id.tv_side)
            if(item.side <= 4){
                tvSide.text = context.getString(R.string.str_buy_label)
                tvSide.textColor = ContextCompat.getColor(context,R.color.main_green)
            }else{
                tvSide.text = context.getString(R.string.str_sell_label)
                tvSide.textColor = ContextCompat.getColor(context,R.color.main_red)
            }
            //价格
            val dealPrice: Double = MathHelper.round(item.px, 8)
            getView<TextView>(R.id.tv_price).text = dfPrice.format(dealPrice)
            //数量
            getView<TextView>(R.id.tv_vol).text = getVolUnitNoSuffix(
                contract,
                item.qty.toDouble(),
                dealPrice,
                LogicContractSetting.getContractUint(context)
            )

        }
    }

}