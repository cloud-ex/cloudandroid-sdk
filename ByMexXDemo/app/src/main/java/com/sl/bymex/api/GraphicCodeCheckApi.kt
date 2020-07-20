package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 检查是否需要图形验证码
 */
class GraphicCodeCheckApi : IRequestApi {
    override fun getApi(): String {
        return PublicApiConstant.sCaptchCheck
    }

    /**
     * 1:login
     */
    private var action: String? = null
    fun setAction(action: String?): GraphicCodeCheckApi {
        this.action = action
        return this
    }
}