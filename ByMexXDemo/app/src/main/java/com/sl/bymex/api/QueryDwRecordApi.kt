package com.sl.bymex.api

import com.hjq.http.config.IRequestApi

/**
 * 充提记录
 */
class QueryDwRecordApi() : IRequestApi {

    override fun getApi(): String {
        return  PublicApiConstant.sDWithdrawRecord
    }

    /**
     * type 0 all, 1 deposit, 2 withdraw
     */
    var type = "all"
    var coin =""
    var offset = 0
    var limit = 20

}