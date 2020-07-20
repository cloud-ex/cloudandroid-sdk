package com.sl.contract.library.adapter

import android.content.Context
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.ui.library.utils.CommonUtils
import java.text.DecimalFormat

class SlContractAssetAdapter(context: Context,data:ArrayList<ContractAccount>) : BaseQuickAdapter<ContractAccount, BaseViewHolder>(
    R.layout.contract_item_asset_record,data) {

    private var isShowAssetEye = true

    override fun convert(helper: BaseViewHolder, item: ContractAccount) {
        item?.let {
            //币种名称
            helper.setText(R.id.tv_coin_name,it.coin_code)

            val contract: Contract? = ContractPublicDataAgent.getContract(it.contract_id)
            val dfDefault: DecimalFormat = NumberUtil.getDecimal(2)
            val freezeVol: Double = MathHelper.round(it.freeze_vol)
            val availableVol: Double = MathHelper.round(it.available_vol)

            var longProfitAmount = 0.0 //多仓位的未实现盈亏
            var shortProfitAmount = 0.0 //空仓位的未实现盈亏
            var positionMargin = 0.0
            val contractPositions: List<ContractPosition>? = ContractUserDataAgent.getCoinPositions(it.coin_code)
            if (contractPositions != null && contractPositions.isNotEmpty()) {
                for (i in contractPositions.indices){
                    val contractPosition = contractPositions[i]
                    val positionContract = ContractPublicDataAgent.getContract(contractPosition.instrument_id)
                    val contractTicker: ContractTicker? = ContractPublicDataAgent.getContractTicker(contractPosition.instrument_id)
                    if (positionContract == null || contractTicker == null) {
                        continue
                    }
                    positionMargin += MathHelper.round(contractPosition.im)
                    if (contractPosition.side === 1) { //开多
                        longProfitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                            contractPosition.cur_qty,
                            contractPosition.avg_cost_px,
                            contractTicker.fair_px,
                            positionContract.face_value,
                            positionContract.isReserve)
                    } else if (contractPosition.side === 2) { //开空
                        shortProfitAmount += ContractCalculate.CalculateCloseShortProfitAmount(
                            contractPosition.cur_qty,
                            contractPosition.avg_cost_px,
                            contractTicker.fair_px,
                            positionContract.face_value,
                            positionContract.isReserve)
                    }
                }
            }
            val balance = MathHelper.add(freezeVol, availableVol)
            val index = contract?.value_index?: 2
            //权益
            CommonUtils.showAssetEye(isShowAssetEye,dfDefault.format(MathHelper.round(balance + positionMargin + longProfitAmount + shortProfitAmount, index)),helper.getView(R.id.tv_normal_balance))
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