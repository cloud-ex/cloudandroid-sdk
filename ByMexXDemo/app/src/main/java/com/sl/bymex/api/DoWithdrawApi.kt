package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 提现
 */
class DoWithdrawApi() : IRequestApi {

    override fun getApi(): String {
        return  PublicApiConstant.sDoWithdraw
    }

    var coin_code = ""
    var nonce = System.currentTimeMillis()
    var vol = ""
    var to_address = ""
    var email_code = ""
    var sms_code = ""
    var ga_code = ""
    var memo = ""
    var type = 2

}