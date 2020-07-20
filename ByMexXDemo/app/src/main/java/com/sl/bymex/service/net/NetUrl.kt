package com.sl.bymex.service.net

import com.sl.bymex.app.ByMexApp
import com.sl.bymex.app.ByMexApp.Companion.currEvn

object NetUrl {

    val baseUrl  =  when(currEvn){
        Env.Producing -> {
             "https://api.lpmex.com/"
        }
        Env.Developing -> {
             "https://devapi.bymex.com/"
        }
    }

    val socketAddress  =  when(currEvn){
        Env.Producing -> {
            "wss://api.lpmex.com/wsswap/realTime"
        }
        Env.Developing -> {
            "wss://ws.mybts.info/kline-api/ws"
        }
    }


    val contractUrl =  when(currEvn){
        Env.Producing -> {
            "https://api.lpmex.com/swap/"
        }
        Env.Developing -> {
            "https://devapi.bymex.com/swap/"
        }
    }


    val contractSocketUrl  =  when(currEvn){
        Env.Producing -> {
            "wss://api.lpmex.com/wsswap/realTime"
        }
        Env.Developing -> {
            "wss://devapi.bymex.com/wsswap/realTime"
        }
    }

    val captchaId: String
        get() = "98a6399274f34c229b81fd4f81cb2f77"

    val aesSecret: String
        get() = "OZ1WNXAlbe84Kpq8"


    enum class Env(var flag: Boolean?) { Producing(false),  Developing(false) }
}