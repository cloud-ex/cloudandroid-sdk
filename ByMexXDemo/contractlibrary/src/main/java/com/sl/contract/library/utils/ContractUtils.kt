package com.sl.contract.library.utils

import android.content.Context
import android.text.TextUtils
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.helper.ContractService
import com.sl.contract.library.helper.LogicContractSetting

class ContractUtils {
    companion object{
        /**
         * 获得cny
         */
        fun calculateCnyByBtcBalance(btcVol:Double): Double {
            val cnyRate = ContractService.contractService?.getUsdToCoinRate("CNY")?:0.0
            //btc 转 usd 在转cny
            return calculateBtcToUsdBalance(btcVol) *cnyRate
        }

        /**
         * btc 转 usd
         */
        private fun calculateBtcToUsdBalance(btcVol:Double): Double {
            val usdRate = ContractService.contractService?.getCoinFeeRate("USD","BTC")?:0.0
            return btcVol*usdRate
        }
        /**
         * 计算合约总资产
         * @param coinType 当前资产展示单位 如：BTC USDT ETH等
         *  @param baseCoin 以基本币种进行转换，目前只支持BTC 和USD
         * @return
         */
        fun calculateTotalBalance(coinType: String,baseCoin:String="BTC"): Double {
            val contractAccount = ContractUserDataAgent.getContractAccounts() ?: return 0.0
            var totalBalance = 0.0
            for (i in contractAccount.indices) {
                val account = contractAccount[i] ?: continue
                val freezeVol = MathHelper.round(account.freeze_vol)
                val availableVol = MathHelper.round(account.available_vol)
                var longProfitAmount = 0.0 //多仓位的未实现盈亏
                var shortProfitAmount = 0.0 //空仓位的未实现盈亏
                var positionMargin = 0.0
                val contractPositions = ContractUserDataAgent.getCoinPositions(account.coin_code)
                if (contractPositions != null && contractPositions.isNotEmpty()) {
                    for (j in contractPositions.indices) {
                        val contractPosition = contractPositions[j]
                        val positionContract = ContractPublicDataAgent.getContract(contractPosition.instrument_id)
                        val contractTicker = ContractPublicDataAgent.getContractTicker(contractPosition.instrument_id)
                        if (positionContract == null || contractTicker == null) {
                            continue
                        }
                        positionMargin += MathHelper.round(contractPosition.im)
                        if (contractPosition.side == 1) { //开多
                            longProfitAmount += ContractCalculate.CalculateCloseLongProfitAmount(
                                contractPosition.cur_qty,
                                contractPosition.avg_cost_px,
                                contractTicker.fair_px,
                                positionContract.face_value,
                                positionContract.isReserve)
                        } else if (contractPosition.side == 2) { //开空
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
                val total = balance + positionMargin + longProfitAmount + shortProfitAmount
                totalBalance += if (TextUtils.equals(coinType, account.coin_code)) {
                    total
                } else {
                    //其他币种 需通过baseCoin费率进行一次中转
                    val btcRate =  ContractService.contractService?.getCoinFeeRate(baseCoin,coinType)?:0.0
                    //当前币种费率
                    val coinRate =  ContractService.contractService?.getCoinFeeRate(baseCoin,account.coin_code)?:0.0
                    MathHelper.div(MathHelper.mul(total, coinRate), btcRate)
                }
            }
            return totalBalance
        }
        /**
         * 获取版本名
         */
        fun getVersionName(context: Context): String {
            return try {
                context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                return ""
            }
        }

        fun CalculateContractBasicValue(
            vol: String?,
            price: String?,
            contract: Contract?): String {
            val unit = LogicContractSetting.getContractUint(ContractSDKAgent.context)
            if (MathHelper.round(vol) <= 0.0 || MathHelper.round(price) <= 0.0 || contract == null) {
                return "0" + if (unit == 0) contract?.base_coin else ContractSDKAgent.context!!.getString(
                    R.string.str_vol_unit)
            }
            var amount: Double
            amount = if (contract.isReserve) {
                MathHelper.div(vol, price)
            } else {
                MathHelper.round(vol)
            }
            val dfVol = NumberUtil.getDecimal(contract.vol_index)
            val dfValue = NumberUtil.getDecimal(-1)
            return if (unit == 0) {
                amount = MathHelper.mul(amount, MathHelper.round(contract.face_value))
                dfValue.format(amount) + contract.base_coin
            } else {
                if (contract.isReserve) {
                    dfVol.format(MathHelper.round(vol)) + ContractSDKAgent.context!!.getString(
                        R.string.str_vol_unit)
                } else {
                    dfVol.format(amount) + ContractSDKAgent.context!!.getString(
                        R.string.str_vol_unit)
                }
            }
        }


        fun getVolUnit(context: Context, contract: Contract?, vol: String?, price: String?): String? {
            if (contract == null) {
                return "0"
            }
            val dfVol0 = NumberUtil.getDecimal(contract.vol_index)
            val dfVol = NumberUtil.getDecimal(-1)
            val unit = LogicContractSetting.getContractUint(context)
            if (unit == 0) {
                return dfVol0.format(MathHelper.round(vol)) + context.getString(R.string.str_vol_unit)
            } else if (unit == 1) {
                return if (contract.isReserve) {
                    val bVol = MathHelper.div(
                        MathHelper.mul(vol, contract.face_value),
                        MathHelper.round(price)
                    )
                    dfVol.format(bVol) + contract.base_coin
                } else {
                    val bVol = MathHelper.mul(vol, contract.face_value)
                    dfVol.format(bVol) + contract.base_coin
                }
            }
            return "0"
        }

        fun getVolUnit(context: Context, contract: Contract?, vol: Double, price: Double): String {
            if (contract == null) {
                return "0"
            }
            val dfVol0 = NumberUtil.getDecimal(contract.vol_index)
            val dfVol = NumberUtil.getDecimal(-1)
            val unit = LogicContractSetting.getContractUint(context)
            if (unit == 0) {
                return dfVol0.format(vol) + context.getString(R.string.str_vol_unit)
            } else if (unit == 1) {
                return if (contract.isReserve) {
                    val bVol = MathHelper.div(
                        MathHelper.mul(vol, MathHelper.round(contract.face_value)),
                        price
                    )
                    dfVol.format(bVol) + contract.base_coin
                } else {
                    val bVol = MathHelper.mul(vol, MathHelper.round(contract.face_value))
                    dfVol.format(bVol) + contract.base_coin
                }
            }
            return "0"
        }

        fun getVolNoUnit(context: Context, contract: Contract?, vol: Double, price: Double): String {
            if (contract == null) {
                return "0"
            }
            val dfVol0 = NumberUtil.getDecimal(contract.vol_index)
            val dfVol = NumberUtil.getDecimal(-1)
            val unit = LogicContractSetting.getContractUint(context)
            if (unit == 0) {
                return dfVol0.format(vol)
            } else if (unit == 1) {
                return if (contract.isReserve) {
                    val bVol = MathHelper.div(
                        MathHelper.mul(vol, MathHelper.round(contract.face_value)),
                        price
                    )
                    dfVol.format(bVol)
                } else {
                    val bVol = MathHelper.mul(vol, MathHelper.round(contract.face_value))
                    dfVol.format(bVol)
                }
            }
            return "0"
        }

    }
}