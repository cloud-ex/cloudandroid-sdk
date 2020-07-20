package com.sl.bymex.adapter

import android.app.Activity
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
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.SystemUtils
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 充提记录
 */
class DwRecordAdapter(val coin: String, data: ArrayList<DwRecord>) :
    BaseQuickAdapter<DwRecord, BaseViewHolder>(R.layout.view_dw_record_item_layout, data),
    LoadMoreModule {
    var decimalFormat = DecimalFormat(
        "###################.###########",
        DecimalFormatSymbols(Locale.ENGLISH)
    )
    var volIndex = 0

    init {
        volIndex = PublicInfoHelper.getSpotCoinByName(coin)?.vol_index ?: 4
    }

    override fun convert(holder: BaseViewHolder, item: DwRecord) {
        holder.apply {
            //名称
            setText(R.id.tv_name, item.coin_code ?: "--")
            //数量
            setText(R.id.tv_vol,decimalFormat.format( MathHelper.round(item.vol, volIndex)))
            //日期
            setText(R.id.tv_time,TimeFormatUtils.convertZTime(item.updated_at,TimeFormatUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND))
            //状态
           var statusString = "--"
            val tvCancel = getView<TextView>(R.id.tv_cancel)
            tvCancel.visibility = View.GONE
            when(item.status){
                DwRecord.SETTLE_STATUS_CREATED -> {//1申请成功(用户提交申请)
                    statusString = context.getString(R.string.str_settle_created)
                    tvCancel.visibility = View.VISIBLE
                    tvCancel.setOnClickListener {
                        //撤销
                    }
                }
                DwRecord.SETTLE_STATUS_PASSED ->{ //2 审核通过(运营审核通过)
                    statusString = context.getString(R.string.str_settle_passed)
                }
                DwRecord.SETTLE_STATUS_REJECTED ->{ //3 审核拒绝(运营审核拒绝)
                    statusString = context.getString(R.string.str_settle_rejected)
                }
                DwRecord.SETTLE_STATUS_SIGNED ->{ //4 签名完成(生成转账signstr完成)
                    statusString = context.getString(R.string.str_settle_signed)
                }
                DwRecord.SETTLE_STATUS_PENDING ->{ //5 打包中(待确认链上是否转账成功)
                    statusString = context.getString(R.string.str_settle_pending)
                }
                DwRecord.SETTLE_STATUS_SUCCESS ->{ //6 成功(转账成功)
                    statusString = context.getString(R.string.str_settle_success)
                }
                DwRecord.SETTLE_STATUS_FAILED ->{ //7 失败(转账失败)
                    statusString = context.getString(R.string.str_settle_failed)
                }
            }
            setText(R.id.tv_status,statusString)
            //复制地址
            getView<ImageView>(R.id.iv_copy).setOnClickListener {
                SystemUtils.copyToClipboard(context,getView<TextView>(R.id.tv_address).text.toString())
                TopToastUtils.showTopSuccessToast(context as Activity,context.getString(R.string.str_copy_success))
            }
            //类型
            when(item.type){
                1,5 -> {//充值
                    setText(R.id.tv_address,item.from_address ?: "--")
                }
                2,6 -> {//提现 6:内部提现
                    setText(R.id.tv_address,item.to_address ?: "--")
                }
                3 -> {//空投

                }
                4 -> {//奖励

                }
                7 -> {//币币转合约

                }
                8 -> {//合约转币币

                }
                9 -> {//C2C转入

                }
                10 -> {//C2C转出

                }
                else ->{

                }
            }
        }
    }
}