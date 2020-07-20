package com.sl.ui.library.widget.kline.formatter

import com.sl.ui.library.widget.kline.base.IValueFormatter
import java.util.*

/**
 * @Author: Bertking
 * @Date：2019/3/12-4:16 PM
 * @Description:
 */
class BigValueFormatter : IValueFormatter {
    private val values = intArrayOf(1000, 1000000, 1000000000)
    private val units = arrayOf("K", "M", "B")

    override fun format(value: Float): String {
        var value = value
        var unit = ""
        var i = values.size - 1
        while (i >= 0) {
            if (value > values[i]) {
                value /= values[i].toFloat()
                unit = units[i]
                break
            }
            i--
        }
        return String.format(Locale.getDefault(), "%.2f", value) + unit
    }


}