package com.sl.bymex.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.bymex.R
import com.sl.bymex.data.UserAsset
import com.sl.bymex.service.SpotAssetHelper
import com.sl.ui.library.utils.CommonUtils
import java.text.DecimalFormat

class SpotAssetAdapter(context: Context, data:ArrayList<UserAsset>) : BaseQuickAdapter<UserAsset, BaseViewHolder>(
    R.layout.spot_item_asset_record,data) {
    private var isShowAssetEye = SpotAssetHelper.isShowAssetEye

    override fun convert(helper: BaseViewHolder, item: UserAsset) {
        item?.let {
            //币种名称
            helper.setText(R.id.tv_coin_name,it.coin_code)

            val index = 2
            val dfDefault: DecimalFormat = NumberUtil.getDecimal(index)
            val freezeVol: Double = MathHelper.round(it.freeze_vol)
            val availableVol: Double = MathHelper.round(it.available_vol)
            val balance = MathHelper.add(freezeVol, availableVol)
            //权益
            CommonUtils.showAssetEye(isShowAssetEye,dfDefault.format(MathHelper.round(balance, index)),helper.getView(R.id.tv_normal_balance))
            //冻结
            CommonUtils.showAssetEye(isShowAssetEye,dfDefault.format(MathHelper.round(freezeVol, index)),helper.getView(R.id.tv_freeze))
            //可用
            CommonUtils.showAssetEye(isShowAssetEye,dfDefault.format(MathHelper.round(availableVol, index)),helper.getView(R.id.tv_available))
        }
    }

    fun notifyAssetDataRefresh(isShowAssetEye : Boolean){
        this.isShowAssetEye = isShowAssetEye
        notifyDataSetChanged()
    }
}