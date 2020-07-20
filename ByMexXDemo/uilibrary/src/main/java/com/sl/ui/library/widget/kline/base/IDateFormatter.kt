package com.sl.ui.library.widget.kline.base

import java.util.*

/**
 * @Author: Bertking
 * @Date：2019/3/11-10:45 AM
 * @Description: 日期格式类
 */
interface IDateFormatter {
    fun format(date: Date): String
}