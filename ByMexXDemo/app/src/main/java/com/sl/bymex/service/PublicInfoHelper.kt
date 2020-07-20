package com.sl.bymex.service

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.api.PublicInfoApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.app.ByMexApp
import com.sl.bymex.data.CoinScans
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.SpotCoin
import com.sl.bymex.data.WithdrawalConfig
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.data.CoinFeeRate
import com.sl.ui.library.data.UsdRates
import org.json.JSONObject


/**
 * PublicInfo
 */
object PublicInfoHelper {
    /**
     * 提现配置
     */
    private val withdrawalConfigList = ArrayList<WithdrawalConfig>()
    /**
     * 币种区块游览器
     */
    private val coinScanList = ArrayList<CoinScans>()

    /**
     * 现货币种
     */
    private val spotCoinList = ArrayList<SpotCoin>()

    /**
     * 币种费率
     */
    private val coinPricesRateList = ArrayList<CoinFeeRate>()

    /**
     * usd费率
     */
    private val usdRateList = ArrayList<UsdRates>()
    private var sharedPreferences: SharedPreferences? = null

    /**
     * 获取币种提现配置
     */
    fun getWithdrawalConfig(coin: String) : WithdrawalConfig?{
        for (item in withdrawalConfigList){
            if(TextUtils.equals(coin,item.coin_name)){
                return item
            }
        }
        return null
    }
    /**
     * 获得区块游览器信息
     */
    fun getCoinScan(coin_group: String): CoinScans? {
        for (item in coinScanList) {
            if (TextUtils.equals(coin_group, item.coin_group)) {
                return item
            }
        }
        return null
    }

    /**
     * 获得现货币种
     */
    fun getSpotCoin(): ArrayList<SpotCoin> {
        return spotCoinList
    }

    fun getSpotCoinByName(name:String): SpotCoin? {
        for (item in spotCoinList){
            if(TextUtils.equals(item.name,name)){
                return item
            }
        }
        return null
    }

    /**
     * 获取币种费率
     */
    fun getCoinFeeRate(coin: String): CoinFeeRate? {
        if (TextUtils.isEmpty(coin)) {
            return null
        }
        for (item in coinPricesRateList) {
            if (TextUtils.equals(item.name, coin)) {
                return item
            }
        }
        return null
    }

    /**
     * 获取usd对coin费率
     */
    fun getUsdToCoinRate(coin: String): UsdRates? {
        if (TextUtils.isEmpty(coin)) {
            return null
        }
        for (item in usdRateList) {
            if (TextUtils.equals(item.name, coin)) {
                return item
            }
        }
        return null
    }

    fun loadPublicInfoFromNet(activity: BaseEasyActivity) {
        NetHelper.doHttpGet(
            activity,
            PublicInfoApi(),
            object : HttpCallback<HttpData<String>>(activity) {
                override fun onSucceed(result: HttpData<String>) {
                    dealPublicInfo(result)
                    //存储数据
                    sharedPreferences?.edit()?.putString("public_info", result.data)?.commit()
                }
            })
    }


    private fun dealPublicInfo(result: HttpData<String>) {
        //提现配置
        withdrawalConfigList.clear()
        withdrawalConfigList.addAll(result.getBeanList(WithdrawalConfig::class.java, "withdrawal_configs"))
        //币种区块游览器
        val coinScans = result.getStringDataByKey("coin_scans")
        if (!TextUtils.isEmpty(coinScans)) {
            coinScanList.clear()
            val coinScansObject = JSONObject(coinScans)
            val it = coinScansObject.keys()
            while (it.hasNext()) {
                val key = it.next().toString()
                val itemObj = coinScansObject.optJSONObject(key)
                if (itemObj != null) {
                    val item = CoinScans()
                    item.coin_group = itemObj.optString("coin_group")
                    item.address = itemObj.optString("address")
                    item.tx = itemObj.optString("tx")
                    item.regex = itemObj.optString("regex")
                    coinScanList.add(item)
                }

            }
        }
        //现货币种
        spotCoinList.clear()
        spotCoinList.addAll(result.getBeanList(SpotCoin::class.java, "spot_coins"))
        //币种费率
        coinPricesRateList.clear()
        coinPricesRateList.addAll(
            result.getBeanList(
                CoinFeeRate::class.java,
                "coin_prices"
            )
        )
        //usd费率
        usdRateList.clear()
        usdRateList.addAll(result.getBeanList(UsdRates::class.java, "usd_rates"))
    }

    init {
        sharedPreferences =
            ByMexApp.appContext.getSharedPreferences("public_info_config", Context.MODE_PRIVATE)
        val cacheData = sharedPreferences?.getString("public_info", "")
        if (!TextUtils.isEmpty(cacheData)) {

            val data = HttpData<String>()
            data.data = cacheData
            dealPublicInfo(data)
        }
    }

}