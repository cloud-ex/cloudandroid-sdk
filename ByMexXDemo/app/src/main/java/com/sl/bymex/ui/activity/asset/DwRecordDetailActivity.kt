package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.view.View
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.bymex.R
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.DwRecord
import com.sl.bymex.service.PublicInfoHelper
import kotlinx.android.synthetic.main.activity_dw_record_detail.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 充提明细
 */
class DwRecordDetailActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_dw_record_detail
    }

    private var dwRecord: DwRecord? = null
    override fun loadData() {
        dwRecord = intent.getParcelableExtra("item")
    }

    override fun initView() {
        dwRecord?.let {
            //量
            var decimalFormat = DecimalFormat(
                "###################.###########",
                DecimalFormatSymbols(Locale.ENGLISH)
            )
            var volIndex = PublicInfoHelper.getSpotCoinByName(it.coin_code)?.vol_index ?: 4
            tv_vol.text =
                decimalFormat.format(MathHelper.round(it.vol, volIndex)) + " " + it.coin_code
            //手续费
            tv_miners_fee.text =
                decimalFormat.format(MathHelper.round(it.fee, volIndex)) + " " + it.coin_code
            //To 地址
            tv_to_address.text = it.to_address
            tv_tx_hash.text = it.tx_hash
            tv_from_address.text = it.from_address
            //时间
            tv_time.text = TimeFormatUtils.convertZTime(it.updated_at, TimeFormatUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND)

            when (it.type) {
                1, 5 -> {//充值
                    title_layout.title = it.coin_code + getString(R.string.str_deposit)
                    tv_to_address_label.text = getString(R.string.str_deposit_address)
                }
                2, 6 -> {//提现 6:内部提现
                    title_layout.title = it.coin_code + getString(R.string.str_withdraw)
                    tv_to_address_label.text = getString(R.string.str_withdraw_address)
                }
            }
            var statusString = "--"
            when (it.status) {
                DwRecord.SETTLE_STATUS_CREATED -> {//1申请成功(用户提交申请)
                    statusString = getString(R.string.str_settle_created)
                    iv_status.setImageResource(R.drawable.icon_wait)
                }
                DwRecord.SETTLE_STATUS_PASSED -> { //2 审核通过(运营审核通过)
                    statusString = getString(R.string.str_settle_passed)
                    iv_status.setImageResource(R.drawable.icon_status_withdraw_wait)
                }
                DwRecord.SETTLE_STATUS_REJECTED -> { //3 审核拒绝(运营审核拒绝)
                    statusString = getString(R.string.str_settle_rejected)
                    iv_status.setImageResource(R.drawable.icon_status_fail)
                }
                DwRecord.SETTLE_STATUS_SIGNED -> { //4 签名完成(生成转账signstr完成)
                    statusString = getString(R.string.str_settle_signed)
                    iv_status.setImageResource(R.drawable.icon_status_withdraw_wait)
                }
                DwRecord.SETTLE_STATUS_PENDING -> { //5 打包中(待确认链上是否转账成功)
                    statusString = getString(R.string.str_settle_pending)
                    iv_status.setImageResource(R.drawable.icon_status_withdraw_wait)
                }
                DwRecord.SETTLE_STATUS_SUCCESS -> { //6 成功(转账成功)
                    statusString = getString(R.string.str_settle_success)
                    iv_status.setImageResource(R.drawable.icon_status_success)
                }
                DwRecord.SETTLE_STATUS_FAILED -> { //7 失败(转账失败)
                    statusString = getString(R.string.str_settle_failed)
                    iv_status.setImageResource(R.drawable.icon_status_fail)
                }
            }
            tv_status.text = statusString
        }
    }

    companion object {
        fun show(activity: Activity, item: DwRecord) {
            val intent = Intent(activity, DwRecordDetailActivity::class.java)
            intent.putExtra("item", item)
            activity.startActivity(intent)
        }
    }
}