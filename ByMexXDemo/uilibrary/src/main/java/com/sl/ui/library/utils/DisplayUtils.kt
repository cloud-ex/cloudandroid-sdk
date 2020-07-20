package com.sl.ui.library.utils

import android.content.Context
import android.view.WindowManager

object DisplayUtils {
    fun getWidthHeight(context: Context): IntArray {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val width = wm.defaultDisplay.width
        val height = wm.defaultDisplay.height
        return intArrayOf(width, height)
    }

    fun getWidth(context: Context): Int {
        val wm = context
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
      return wm.defaultDisplay.width
    }

    fun sp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.scaledDensity
        return (dipValue * scale).toInt()
    }


    fun dip2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }
    /**
     * 用于判断是否快速点击
     *
     * @return
     */
    private const val FAST_CLICK_DELAY_TIME = 1000
    private var lastClickTime: Long = 0

    @get:Synchronized
    val isFastClick: Boolean
        get() {
            val flag = false
            val currentClickTime = System.currentTimeMillis()
            if (currentClickTime - lastClickTime <= FAST_CLICK_DELAY_TIME) {
                return true
            }
            lastClickTime = currentClickTime
            return flag
        }
}