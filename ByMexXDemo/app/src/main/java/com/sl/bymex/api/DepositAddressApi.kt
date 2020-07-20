package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 充值地址
 */
class DepositAddressApi() : IRequestApi {

    override fun getApi(): String {
        return  PublicApiConstant.sDepositAddress
    }

    var coin = ""

}