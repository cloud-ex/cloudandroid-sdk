package com.sl.ui.library.widget.kline.bean.vice

import com.sl.ui.library.widget.kline.bean.Index


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:37 AM
 * @Description:MACD指标（异同移动平均线）
 * https://baike.baidu.com/item/IMACD%E6%8C%87%E6%A0%87/6271283?fr=aladdin
 *
 * MACD指标是由两线一柱组合起来形成，快速线为DIF，慢速线为DEA，柱状图为MACD
 */
interface IMACD : Index {
    /**
     * DEA值
     */
    var DEA: Float
    /**
     * 计算离差值（DIF）
     */
    var DIF: Float
    var MACD: Float

}