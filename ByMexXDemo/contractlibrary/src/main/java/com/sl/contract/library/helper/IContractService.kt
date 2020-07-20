package com.sl.contract.library.helper

import android.app.Activity
import androidx.fragment.app.FragmentActivity


interface IContractService {

    /**
     * 打开登录界面
     */
    fun  doOpenLoginPage(activity: FragmentActivity)

    /**
     * 打开资金划转页面
     */
    fun  openAssetTransferActivity(activity: FragmentActivity, coin:String)

    /**根据原始币种 获取 目标币种费率,该接口目前不支持获取rmb费率，
     * getUsdToCoinRate
     * targetCoin 目标币种
     * originCoin 原始币种
     */
    fun getCoinFeeRate(targetCoin:String,originCoin:String):Double

    /**获取usd 对 coin(cny)费率
     */
    fun getUsdToCoinRate(coin:String):Double
}