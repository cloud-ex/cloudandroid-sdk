package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 登录
 * https://github.com/getActivity/EasyHttp
 */
class LoginApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sLogin
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
     * 时间戳
     */
     var nonce: Long? = System.currentTimeMillis()*1000

    /**
     * 验证码
     */
    var validate: String? = ""
    /**
     * 邮箱
     */
    var email: String? = ""


}