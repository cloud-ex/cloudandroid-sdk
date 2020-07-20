package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 重置密码
 */
class ResetPwdApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sResetPassword
    }

    var phone: String? = ""
    var email: String? = ""
    var code: String? = ""
    var password: String? = ""
}