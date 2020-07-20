package com.sl.bymex.service.foreground

interface ForegroundCallbacksListener {
    /**
     * 切换到前
     */
    fun foregroundListener()

    /**
     * 切换到后台
     */
    fun backgroundListener()
}