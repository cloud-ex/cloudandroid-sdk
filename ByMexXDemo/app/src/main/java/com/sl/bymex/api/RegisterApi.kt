package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 注册
 */
class RegisterApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sRegister
    }

    /**
     * 手机号
     */
    var phone: String? = ""
    /**
     * 密码
     */
    var password: String? = ""

    /**
     * 验证码
     */
    var code = ""

    var account_type = 0

    var inviter_id = 0
    /**
     * 邮箱
     */
    var email: String? = ""


}