package com.sl.bymex.service.net

import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.net.AESUtil
import com.contract.sdk.net.ContractApiConstants
import com.hjq.http.EasyConfig
import com.hjq.http.EasyHttp
import com.hjq.http.EasyLog
import com.hjq.http.config.IRequestApi
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.data.HttpData

object NetHelper {
    fun <T> doHttpGet(
        lifecycleOwner: LifecycleOwner,
        api: IRequestApi,
        listener: HttpCallback<HttpData<T>>
    ) {
        buildHeader(api.api)
        EasyHttp.get(lifecycleOwner)
            .api(api)
            .request(listener)
    }


    fun <T> doHttpPost(
        lifecycleOwner: LifecycleOwner,
        api: IRequestApi,
        listener: HttpCallback<HttpData<T>>,pwd:String = ""
    ) {
        buildHeader(api.api)
       val d =  EasyHttp.post(lifecycleOwner)
            .api(api)
            .request(listener)
    }


   private fun buildHeader(url:String,pwd:String = ""){
        val language =
            if (ContractSDKAgent.isZhEnv) "zh-cn" else "en"
        val timestamp = System.currentTimeMillis() * 1000

        val easyConfig =  EasyConfig.getInstance()
        easyConfig.addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_LANGUAGE),language)
        easyConfig.addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_TS),timestamp.toString())

       if(ContractSDKAgent.isLogin){
           var sign = ""
           sign = AESUtil.getInstance().encrypt(
               ContractSDKAgent.contractToken,
               ContractSDKAgent.httpConfig!!.aesSecret,
               timestamp.toString())
           sign = sign.substring(0, 64)
           easyConfig.addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_SIGN),sign)
           easyConfig.addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_UID),ContractSDKAgent.user.uid)
           if(!TextUtils.isEmpty(pwd)){
               easyConfig.addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_ASSET_PASSWORD),pwd)
           }
       }

       EasyLog.print("url:"+url+";header:"+easyConfig.headers.toString())
    }
}