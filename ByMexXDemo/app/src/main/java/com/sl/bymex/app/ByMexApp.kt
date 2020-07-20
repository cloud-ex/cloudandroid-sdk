package com.sl.bymex.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.multidex.MultiDexApplication
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.net.ContractApiConstants
import com.contract.sdk.net.ContractHttpConfig
import com.hjq.http.EasyConfig
import com.hjq.http.listener.OnHttpListener
import com.sl.bymex.service.ContractUIImpl
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.net.HttpLogStrategy
import com.sl.bymex.service.net.NetUrl
import com.sl.bymex.service.net.RequestHandler
import com.sl.bymex.service.net.RequestServer
import com.sl.contract.library.helper.ContractService
import com.sl.ui.library.service.foreground.ForegroundCallbacks
import com.sl.ui.library.service.foreground.ForegroundCallbacksObserver
import com.sl.ui.library.utils.SystemUtils
import okhttp3.OkHttpClient

class ByMexApp: MultiDexApplication(),OnHttpListener<Any>,
    LifecycleOwner {
    companion object {
        lateinit var appContext: ByMexApp
        var currEvn = NetUrl.Env.Producing
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        //APP生命周期监听
        initAppStatusListener()
        //合约SDK 相关配置
        initContractSdk()
        //网络配置
        initNetConfig()
        UserHelper
        ContractService.contractService = ContractUIImpl()

    }

    /**
     * 接入方需配置
     */
    private fun initContractSdk() {
        val contractHttpConfig = ContractHttpConfig()
        contractHttpConfig.aesSecret = NetUrl.aesSecret
        contractHttpConfig.prefixHeader = "ex"
        contractHttpConfig.contractUrl = NetUrl.contractUrl
        contractHttpConfig.contractWsUrl = NetUrl.contractSocketUrl
        //是否是合约云SDK
        ContractSDKAgent.isContractCloudSDK = false
        //合约SDK Http配置初始化
        ContractSDKAgent.httpConfig = contractHttpConfig
        //通知合约SDK语言环境
        ContractSDKAgent.isZhEnv = SystemUtils.isZh()
        //合约SDK 必须设置 在最后调用
        ContractSDKAgent.init(this)
    }



    private fun initNetConfig() {
        var okHttpClient = OkHttpClient()
        EasyConfig.with(okHttpClient)
            // 是否打印日志
            .setLogEnabled(true)
            // 设置服务器配置
            .setServer(RequestServer())
            // 设置请求处理策略
            .setHandler(RequestHandler(this))
            .setLogStrategy(HttpLogStrategy())
            // 设置请求重试次数
            .setRetryCount(2)
            // 添加全局请求参数
            //.addParam("token", "6666666")
            // 添加全局请求头
            .addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_VER), "1.0")
            .addHeader(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_HEADER_DEV), "Android")
            // 启用配置
            .into();
    }


    private fun initAppStatusListener() {
        ForegroundCallbacks.init(this)?.addListener(object : ForegroundCallbacks.Listener {
            override fun onBecameForeground() {
                ForegroundCallbacksObserver.instance?.ForegroundListener()
            }

            override fun onBecameBackground() {
                ForegroundCallbacksObserver.instance?.CallBacksListener()
            }
        })
    }


    val Java8Observer : Lifecycle = object : Lifecycle(){
        override fun addObserver(observer: LifecycleObserver) {
        }

        override fun removeObserver(observer: LifecycleObserver) {
        }

        override fun getCurrentState(): State {
                return State.RESUMED
        }

    }

    override fun onSucceed(result: Any?) {
    }

    override fun onFail(e: Exception?) {
    }

    override fun getLifecycle(): Lifecycle {
        return Java8Observer
    }




}