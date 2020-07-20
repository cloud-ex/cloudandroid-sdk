package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 资金划转
 */
class AssetTransferApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sSpotAssetTransfer
    }

    /**
     * c2u  u2c  // 1 交易所=>合约  2 合约=>交易所   u2c/c2u
     */
    var type = 1

    var coin_code = ""

    var vol = ""


}