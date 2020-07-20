package com.sl.ui.library.utils

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import java.util.regex.Pattern

class CashierInputFilter : InputFilter {
    var mPattern: Pattern
    //小数点后的位数
    var POINTER_LENGTH = 8

    /**
     * @param source    新输入的字符串
     * @param start     新输入的字符串起始下标，一般为0
     * @param end       新输入的字符串终点下标，一般为source长度-1
     * @param dest      输入之前文本框内容
     * @param dstart    原内容起始坐标，一般为0
     * @param dend      原内容终点坐标，一般为dest长度-1
     * @return          输入内容
     */
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence {
        val sourceText = source.toString()
        val destText = dest.toString()

        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {
            return ""
        }
        val matcher = mPattern.matcher(source)
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(POINTER)) {
            if (!matcher.matches()) {
                return ""
            } else {
                if (POINTER == source.toString()) {  //只能输入一个小数点
                    return ""
                }
            }
            /**
             * 验证小数点精度，保证小数点后只能输入[POINTER_LENGTH]位
             */
            val index = destText.indexOf(POINTER)
            val length = dend - index
            if (length > POINTER_LENGTH) {
                return dest.subSequence(dstart, dend)
            }
        } else {
            /**
             * 没有输入小数点的情况下，只能输入小数点和数字
             * 1. 首位不能输入小数点
             * 2. 如果首位输入0，则接下来只能输入小数点了
             */
            if (!matcher.matches()) {
                return ""
            } else {
                if (POINTER == source.toString() && TextUtils.isEmpty(
                        destText
                    )
                ) {  //首位不能输入小数点
                    return ""
                } else if (POINTER != source.toString() && ZERO == destText) { //如果首位输入0，接下来只能输入小数点
                    return ""
                }
            }
        }

        //验证输入金额的大小
        val sumText = (destText + sourceText).toDouble()
        /**
         * 这里判断最大输入的值
         */
//        if (sumText > MAX_VALUE) {
//            return dest.subSequence(dstart, dend);
//        }
        return dest.subSequence(dstart, dend).toString() + sourceText
    }

    companion object {
        //输入的最大金额
        /**
         * java int 类整数的最大值是 2 的 31 次方 - 1 = 2147483648 - 1 = 2147483647
         * 可以用 Integer.MAX_VALUE 表示它，即 int value = Integer.MAX_VALUE;
         */
        private const val MAX_VALUE = Int.MAX_VALUE

        private const val POINTER = "."
        private const val ZERO = "0"
    }

    init {
        /**
         * 正则表达式，表示匹配0-9的数字和.
         */
        mPattern = Pattern.compile("([0-9]|\\.)*")
    }
}