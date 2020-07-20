package com.sl.ui.library.widget.kline.formatter

import com.sl.ui.library.utils.BigDecimalUtils
import com.sl.ui.library.widget.kline.base.IValueFormatter

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:16 AM
 * @Description:
 */
class ValueFormatter : IValueFormatter {
    override fun format(value: Float): String {
        return BigDecimalUtils.showSNormal(value.toString())
    }
}