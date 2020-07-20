package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 获取谷歌验证二维码
 */
class GoogleCodeApi(private val path:String) : IRequestApi {


    override fun getApi(): String {
       return path;
    }

    var ga_key: String? = null
    var login_name: String? = null
    var ga_code: Long? = null

}