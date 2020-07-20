package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

class PublicInfoApi() : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sPublicInfo
    }
}