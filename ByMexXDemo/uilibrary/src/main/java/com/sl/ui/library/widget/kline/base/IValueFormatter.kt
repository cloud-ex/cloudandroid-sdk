package com.sl.ui.library.widget.kline.base

/**
 * @Author: Bertking
 * @Date：2019/3/11-10:43 AM
 * @Description: 格式化KLine上的值
 */
interface IValueFormatter {
    /**
     * 格式化value
     *
     * @param value 传入的value值
     * @return 返回字符串
     */
    fun format(value: Float): String
}