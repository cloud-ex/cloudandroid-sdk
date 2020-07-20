package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 资金密码操作
 */
class AssetPwdApi(private val path:String) : IRequestApi {


    override fun getApi(): String {
       return path;
    }

    var asset_password: String? = null
    var sms_code: String? = null
    var ga_code:Long?=0
}