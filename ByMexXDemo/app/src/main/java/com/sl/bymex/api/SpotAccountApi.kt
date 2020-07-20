package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 获取现货资产
 */
class SpotAccountApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sSpotAccount
    }

}