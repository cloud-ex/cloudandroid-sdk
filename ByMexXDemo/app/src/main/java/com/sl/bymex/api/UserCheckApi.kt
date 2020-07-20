package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 检查用户是否存在
 */
class UserCheckApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sUserCheck
    }

    var phone: String? = null
    var email: String? = null

}