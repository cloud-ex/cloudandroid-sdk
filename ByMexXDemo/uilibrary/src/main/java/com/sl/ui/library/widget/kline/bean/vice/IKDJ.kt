package com.sl.ui.library.widget.kline.bean.vice

import com.sl.ui.library.widget.kline.bean.Index


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:34 AM
 * @Description:KDJ指标(随机指标)接口
 * https://baike.baidu.com/item/IKDJ%E6%8C%87%E6%A0%87/6328421?fr=aladdin&fromid=3423560&fromtitle=kdj
 */
interface IKDJ : Index {
    var K: Float
    var D: Float
    var J: Float
}