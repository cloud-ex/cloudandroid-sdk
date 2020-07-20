package com.sl.bymex.service

import androidx.fragment.app.FragmentActivity
import com.sl.bymex.ui.activity.asset.AssetTransferActivity
import com.sl.bymex.ui.activity.user.LoginActivity
import com.sl.contract.library.helper.IContractService

/**
 * 合约UI层代理类
 */
class ContractUIImpl : IContractService {

    /**
     * 打开登录界面
     */
    override fun doOpenLoginPage(activity: FragmentActivity) {
        LoginHelper.openLogin(activity)
    }

    /**
     * 打开资金划转页面
     */
    override fun openAssetTransferActivity(activity: FragmentActivity, coin: String) {
        AssetTransferActivity.show(activity,coin)
    }

    /**根据原始币种 获取 目标币种费率,该接口目前不支持获取rmb费率，
     * getUsdToCoinRate
     * targetCoin 目标币种
     * originCoin 原始币种
     */
    override fun getCoinFeeRate(targetCoin: String, originCoin: String): Double {
        val coinFeeRate =  PublicInfoHelper.getCoinFeeRate(originCoin)
        if(coinFeeRate!=null){
            when(targetCoin){
                "BTC" ->{
                    return coinFeeRate.price_btc.toDouble()
                }
                "USD" ->{
                    return coinFeeRate.price_usd.toDouble()
                }
            }
        }
        return 0.0
    }

    /**获取usd 对 coin(cny)费率
     */
    override fun getUsdToCoinRate(coin: String): Double {
        val coinFeeRate =  PublicInfoHelper.getUsdToCoinRate(coin)
        if(coinFeeRate!=null){
            return coinFeeRate.rate.toDouble()
        }
        return 0.0
    }

}