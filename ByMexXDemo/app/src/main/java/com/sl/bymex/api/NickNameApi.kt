package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 设置昵称
 */
class NickNameApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sSpotSetAccountName
    }

    var account_name: String? = ""


}