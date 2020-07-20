package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 资金密码有效期
 */
class AssetPwdEffectTimeApi() : IRequestApi {


    override fun getApi(): String {
       return PublicApiConstant.sAssetPasswordResetEffectiveTime;
    }

    var asset_password: String? = null
    var asset_password_effective_time: Long? = 0
}