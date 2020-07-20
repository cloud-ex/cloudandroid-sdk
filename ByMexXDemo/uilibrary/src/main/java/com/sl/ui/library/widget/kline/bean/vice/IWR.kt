package com.sl.ui.library.widget.kline.bean.vice

import com.sl.ui.library.widget.kline.bean.Index


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:43 AM
 * @Description: WR指标
 *
 * IWR，属摆动类指标、又称威廉超买超卖指数(Williams Overbought/Oversold Index)、威廉氏超买超卖指标，为分析市场短线买卖走势的技术指标。
 *
 * https://baike.baidu.com/item/wR%E6%8C%87%E6%A0%87
 */
interface IWR : Index {
    var R: Float
}