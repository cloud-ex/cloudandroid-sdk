package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 绑定邮箱或者手机号
 */
class BindAccountApi(private val bindType:Int) : IRequestApi {

    override fun getApi(): String {
        return if(bindType == 0){
            PublicApiConstant.sSpotBindPhone
        }else{
            PublicApiConstant.sSpotBindEmail
        }
    }

    var phone: String? = null
    var email: String? = null
    var email_code: String? = null
    var sms_code: String? = null

}