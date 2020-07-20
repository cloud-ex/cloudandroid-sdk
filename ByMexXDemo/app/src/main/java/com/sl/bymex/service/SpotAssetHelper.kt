package com.sl.bymex.service

import android.text.TextUtils
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.utils.MathHelper
import com.sl.bymex.app.ByMexApp
import com.sl.ui.library.utils.PreferenceManager

/**
 * 现货资产计算
 */
class SpotAssetHelper {
    companion object {
        /**
         * 现货总资产
         */
        fun getBalanceTotal(showCoin: String, baseCoin: String = "BTC"): Double {
            return getBalanceAvailable(showCoin, baseCoin) + getBalanceFreeze(showCoin, baseCoin)
        }

        /**
         * 获得可用总资产
         */
        fun getBalanceAvailable(showCoin: String, baseCoin: String = "BTC"): Double {
            val userAssets = UserHelper.user.user_assets
            var balanceTotal = 0.0
            val contractUIImpl = ContractUIImpl()
            for (item in userAssets) {
                balanceTotal += if (TextUtils.equals(showCoin, item.coin_code)) {
                    item.available_vol.toDouble()
                } else {
                    //根据费率转换成coin币种
                    val btcRate =
                        contractUIImpl.getCoinFeeRate(baseCoin, showCoin) ?: 0.00
                    //当前币种费率
                    val coinRate =
                        contractUIImpl.getCoinFeeRate(baseCoin, item.coin_code)
                            ?: 0.0
                    MathHelper.div(MathHelper.mul(item.available_vol.toDouble(), coinRate), btcRate)
                }
            }

            return balanceTotal
        }

        /**
         * 获得冻结总资产
         */
        fun getBalanceFreeze(showCoin: String, baseCoin: String = "BTC"): Double {
            val userAssets = UserHelper.user.user_assets
            var balanceTotal = 0.0
            val contractUIImpl = ContractUIImpl()
            for (item in userAssets) {
                balanceTotal += if (TextUtils.equals(showCoin, item.coin_code)) {
                    item.freeze_vol.toDouble()
                } else {
                    //根据费率转换成coin币种
                    val btcRate =
                        contractUIImpl.getCoinFeeRate(baseCoin, showCoin) ?: 0.00
                    //当前币种费率
                    val coinRate =
                        contractUIImpl.getCoinFeeRate(baseCoin, item.coin_code)
                            ?: 0.0
                    MathHelper.div(MathHelper.mul(item.freeze_vol.toDouble(), coinRate), btcRate)
                }
            }

            return balanceTotal
        }

        /**
         * 是否展示资产眼睛
         */
        var isShowAssetEye = true
            set(value) {
                field = value
                PreferenceManager.getInstance(ByMexApp.appContext)
                    .putSharedBoolean(PreferenceManager.PREF_SHOW_ASSET_EYE_OPEN, value)
            }
            get() {
                return PreferenceManager.getInstance(ByMexApp.appContext)
                    .getSharedBoolean(PreferenceManager.PREF_SHOW_ASSET_EYE_OPEN, true)
            }
    }
}