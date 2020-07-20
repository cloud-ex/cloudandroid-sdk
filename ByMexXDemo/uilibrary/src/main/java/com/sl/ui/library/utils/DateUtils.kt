package com.sl.ui.library.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * @Author: Bertking
 * @Date：2018/12/14-5:21 PM
 * @Description: 处理时间
 */

class DateUtils {
    companion object {
        /**
         *MM-dd
         * yyyy-MM
         * HH:mm
         */
        const val FORMAT_MONTH_DAY = "MM-dd"

        /**
         * 年月
         */
        const val FORMAT_YEAR_MONTH = "yyyy-MM"

        /**
         * 年月日
         */
        const val FORMAT_YEAR_MONTH_DAY = "yyyy-MM-dd"

        /**
         * 时分
         */
        const val FORMAT_HOUR_MIN = "HH:mm"

        /**
         * 月日时分
         */
        const val FORMAT_MONTH_DAY_HOUR_MIN = "MM-dd HH:mm"
        /**
         * 月日时分秒
         */
        const val FORMAT_MONTH_DAY_HOUR_MIN_SECOND = "MM-dd HH:mm:ss"

        /**
         * 年月日时分秒
         */
        const val FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND = "yyyy-MM-dd HH:mm:ss"

        const val FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND_SSS = "yyyy-MM-dd HH:mm:ss:SSS"


        fun dateToString(format: String, date: Date): String {
            val mFormat = SimpleDateFormat(format)
            return mFormat.format(date)
        }

        fun longToString(format: String, date: Long): String {
            return dateToString(format, Date(date * 1000L))
        }

        fun long2StringMS(format: String, ms: Long): String {
            return dateToString(format, Date(ms))
        }

        /**
         * 返回：月日时分秒
         * ms 毫秒数
         */
        fun getYearMonthDayHourMinSecondMS(ms: Long): String {
            return long2StringMS(FORMAT_MONTH_DAY_HOUR_MIN_SECOND, ms)
        }


        /**
         * 返回：年月日
         * @param ms 毫秒数
         */
        fun getYearMonthDayMS(ms: Long): String {
            return long2StringMS(FORMAT_YEAR_MONTH_DAY, ms)
        }


        /**
         * @return 月日
         */
        fun getMonthDay(date: Long): String {
            return longToString(FORMAT_MONTH_DAY, date)
        }

        /**
         * @return 时分
         */
        fun getHourMin(date: Long): String {
            return longToString(FORMAT_HOUR_MIN, date)
        }


        /**
         * 返回：年月日
         * @param seconds 秒数
         */
        fun getYearMonthDay(seconds: Long): String {
            return longToString(FORMAT_YEAR_MONTH_DAY, seconds)
        }

        /**
         * 返回：月日时分
         * @param seconds  秒数(ws返回的时间是以秒来计算的)
         */
        fun getYearMonthDayHourMin(seconds: Long): String {
            return longToString(FORMAT_MONTH_DAY_HOUR_MIN, seconds)
        }

        fun getLogTimeMS(seconds: Long): String {
            return long2StringMS(FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND_SSS, seconds)
        }
        fun getYearLongDayMS(): String {
            return longToString(FORMAT_YEAR_MONTH_DAY_HOUR_MIN_SECOND, Date().time)
        }



    }
}