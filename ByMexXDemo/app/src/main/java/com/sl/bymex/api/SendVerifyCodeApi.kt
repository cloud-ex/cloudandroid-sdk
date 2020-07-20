package com.sl.bymex.api

import android.text.TextUtils
import com.hjq.http.annotation.HttpIgnore
import com.hjq.http.config.IRequestApi
import java.net.URLEncoder

/**
 * 发送短信验证码或者邮箱验证码
 */
class SendVerifyCodeApi : IRequestApi {
    @HttpIgnore
    var phone : String? = ""
    @HttpIgnore
    var email : String? = ""
    @HttpIgnore
    var type : String? = ""

    override fun getApi(): String {
        if(TextUtils.isEmpty(phone)){
            return PublicApiConstant.sSendVerifyCode+"?email="+email+"&type="+type
        }else{
            return PublicApiConstant.sSendVerifyCode+"?phone="+ URLEncoder.encode("+86 $phone")+"&type="+type
        }
    }

    var validate: String? = null

}