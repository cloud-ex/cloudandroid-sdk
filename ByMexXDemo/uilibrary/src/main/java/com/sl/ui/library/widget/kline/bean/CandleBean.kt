package com.sl.ui.library.widget.kline.bean


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:56 AM
 * @Description:
 */
interface CandleBean : Index {
    /**
     * 开盘价
     */
    var openPrice: Float
    /**
     * 收盘价
     */
    var closePrice: Float
    /**
     * 最高价
     */
    var highPrice: Float
    /**
     * 最低价
     */
    var lowPrice: Float


    /*************
     * MA数据(移动平均线指标)
     * https://baike.baidu.com/item/MA%E6%8C%87%E6%A0%87
     * *********************/
    /**
     * 五(月，日，时，分，5分等)均价
     */
    var price4MA5: Float
    /**
     * 十(月，日，时，分，5分等)均价
     */
    var price4MA10: Float
    /**
     * 二十(月，日，时，分，5分等)均价
     */
    var price4MA20: Float
    /**
     * 三十(月，日，时，分，5分等)均价
     */
    var price4MA30: Float
    /**
     * 六十(月，日，时，分，5分等)均价
     */
    var price4MA60: Float


    /*****************
     * Boll指标(布林线指标)
     * https://baike.baidu.com/item/%E5%B8%83%E6%9E%97%E7%BA%BF%E6%8C%87%E6%A0%87/3325894
     * **************************/

    /**
     * 上轨线
     */
    var up: Float
    /**
     * 中轨线
     */
    var mb: Float
    /**
     * 下轨线
     */
    var dn: Float
}