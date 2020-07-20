package com.sl.bymex.adapter

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.bymex.R
import com.sl.bymex.data.DwRecord
import com.sl.bymex.service.PublicInfoHelper
import com.sl.bymex.ui.activity.asset.DwRecordDetailActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 现货资金流水
 */
class SpotFundsFlowAdapter(data: ArrayList<DwRecord>) :
    BaseQuickAdapter<DwRecord, BaseViewHolder>(R.layout.view_spot_funds_flow_item_layout, data),
    LoadMoreModule {
    var decimalFormat = DecimalFormat(
        "###################.###########",
        DecimalFormatSymbols(Locale.ENGLISH)
    )

    override fun convert(holder: BaseViewHolder, item: DwRecord) {
        holder.apply {
            var volIndex = PublicInfoHelper.getSpotCoinByName(item.coin_code)?.vol_index ?: 4
            //日期
            setText(R.id.tv_time,TimeFormatUtils.convertZTime(item.updated_at,TimeFormatUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND))
            var typeString = "--"
            var symbol = ""
            var ivRightArrow = getView<ImageView>(R.id.iv_right_arrow)
            ivRightArrow.visibility = View.GONE
            //类型
            when(item.type){
                1,5 -> {//充值
                    ivRightArrow.visibility = View.VISIBLE
                    typeString = item.coin_code+context.getString(R.string.str_deposit)
                    symbol = "+"
                    setText(R.id.tv_vol,"+"+decimalFormat.format( MathHelper.round(item.vol, volIndex)))
                }
                2,6 -> {//提现 6:内部提现
                    ivRightArrow.visibility = View.VISIBLE
                    typeString = item.coin_code+context.getString(R.string.str_withdraw)
                    symbol = "-"
                }
                3 -> {//空投
                    typeString = item.coin_code+context.getString(R.string.str_withdraw)
                    symbol = "+"
                }
                4 -> {//奖励
                    typeString = item.coin_code+context.getString(R.string.str_transfer_in)
                    symbol = "+"
                }
                7 -> {//币币转合约
                    typeString = item.coin_code+context.getString(R.string.str_transfer_out)
                    symbol = "-"
                }
                8 -> {//合约转币币
                    typeString = item.coin_code+context.getString(R.string.str_transfer_in)
                    symbol = "+"
                }
                9 -> {//C2C转入
                    typeString = item.coin_code+context.getString(R.string.str_transfer_in)
                    symbol = "+"
                }
                10 -> {//C2C转出
                    typeString = item.coin_code+context.getString(R.string.str_transfer_in)
                    symbol = "-"
                }
            }
            //量
            val tvVol = getView<TextView>(R.id.tv_vol)
            if(TextUtils.equals(symbol,"+")){
                tvVol.setTextColor(context.resources.getColor(R.color.main_green))
                getView<TextView>(R.id.tv_account_label).text =  context.getString(R.string.str_transfer_in_account)
            }else{
                tvVol.setTextColor(context.resources.getColor(R.color.main_red))
                getView<TextView>(R.id.tv_account_label).text =  context.getString(R.string.str_transfer_out_account)
            }
            tvVol.text = symbol+decimalFormat.format( MathHelper.round(item.vol, volIndex) )
            setText(R.id.tv_name, typeString)

            if( ivRightArrow.visibility == View.VISIBLE){
                itemView.setOnClickListener {
                    DwRecordDetailActivity.show(context as Activity,item)
                }
            }else{
                itemView.setOnClickListener(null)
            }
        }
    }
}