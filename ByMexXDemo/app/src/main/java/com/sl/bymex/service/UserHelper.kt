package com.sl.bymex.service

import android.app.Activity
import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSON
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.ContractUser
import com.hjq.http.EasyLog
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.api.SpotAccountApi
import com.sl.bymex.app.ByMexApp
import com.sl.bymex.data.AreaData
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.User
import com.sl.bymex.data.UserAsset
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.PreferenceManager

object UserHelper {
    /**
     * 国家代码
     */
    var areaData = AreaData("Chain", "中国", "86")
    set(value) {
        field = value
        PreferenceManager.getInstance(ContractSDKAgent.context).putSharedString(PreferenceManager.REGISTER_CITY_CODE,JSON.toJSONString(value))
    }
    /**
     * 用户信息
     */
    var user = User()
        set(value) {
            field = value
            saveLocalUser()
        }

    /**
     * 是否登录
     */
    fun isLogin():Boolean{
        return !TextUtils.isEmpty(user.token)
    }

    /**
     * 登录成功，加载更多用户信息
     */
    fun doLoginSuccess(loginUser: User){
        user = loginUser
        //登录成功后，加载资产
        if(!TextUtils.isEmpty(user.token)){
            //通知合约SDK登录
            bindContractUser()
            EventBusUtil.post(MessageEvent(MessageEvent.sl_login_token_change_event))
            loadUserInfoFromNet()
        }else{
            EventBusUtil.post(MessageEvent(MessageEvent.sl_login_token_change_event))
            ContractSDKAgent.exitLogin()
        }
    }

    private fun bindContractUser(){
        if (!TextUtils.isEmpty(user.token)){
            val contractUser = ContractUser()
            contractUser.token = user.token
            contractUser.uid = user.account_id.toString()
            ContractSDKAgent.user = contractUser
        }
    }

    /**
     * 退出登录
     */
    fun exitLogin(){
        user.token = ""
        ContractSDKAgent.exitLogin()
        EventBusUtil.post(MessageEvent(MessageEvent.sl_login_token_change_event))
        saveLocalUser()
    }

     fun loadUserInfoFromNet(){
         if(!isLogin()){
             return
         }
        NetHelper.doHttpGet(ByMexApp.appContext,SpotAccountApi(),object:HttpCallback<HttpData<User>>(ByMexApp.appContext){
            override fun onSucceed(result: HttpData<User>) {
                result.data.token = user.token
                user =  result.data
                EventBusUtil.post(MessageEvent(MessageEvent.sl_spot_assets_change_event))
            }
        })

    }

    /**
     * 得到用户资产
     */
    fun getUserAsset(coin:String):UserAsset?{
       val userAssets =  user.user_assets
        for (item in userAssets){
            if(TextUtils.equals(coin,item.coin_code)){
                return item
            }
        }
        return null
    }

    fun isPhoneVerify():Boolean{
        if(user.phone.isNullOrEmpty() && user.email.isNullOrEmpty() && user.status != 1){
           return true
        } else if(user.phone.isNullOrEmpty()){
            return true
        }else if(user.email.isNullOrEmpty() &&  user.status != 1){
            return false
        }
        return false
    }


    private fun loadLocalUserJson():String{
        return PreferenceManager.getInstance(ContractSDKAgent.context)
            .getSharedString(PreferenceManager.PREF_APP_USER, "");
    }
    private fun saveLocalUser(){
        PreferenceManager.getInstance(ContractSDKAgent.context).putSharedString(PreferenceManager.PREF_APP_USER,JSON.toJSONString(user))
    }

    init {
        //国家代码
        val codeCache = PreferenceManager.getInstance(ContractSDKAgent.context)
            .getSharedString(PreferenceManager.REGISTER_CITY_CODE, "")
        if (!TextUtils.isEmpty(codeCache)) {
            areaData = JSON.parseObject(codeCache, AreaData::class.java)
        }
        //加载User
        val userCache = loadLocalUserJson()
        if (!TextUtils.isEmpty(userCache)) {
            user = JSON.parseObject(userCache, User::class.java)
            bindContractUser()
            loadUserInfoFromNet()
        }
    }


}