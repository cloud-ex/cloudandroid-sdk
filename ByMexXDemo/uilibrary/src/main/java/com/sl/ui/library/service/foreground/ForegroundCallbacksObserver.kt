package com.sl.ui.library.service.foreground

import com.sl.bymex.service.foreground.ForegroundCallbacksListener
import java.util.*

class ForegroundCallbacksObserver {
    private val listeners: MutableList<ForegroundCallbacksListener>

    @Synchronized
    fun addListener(listener: ForegroundCallbacksListener?) {
        if (null == listener || listeners.contains(listener)) {
            return
        }
        listeners.add(listener)
    }

    fun ForegroundListener() {
        for (i in listeners.indices) {
            listeners[i].foregroundListener()
        }
    }

    fun CallBacksListener() {
        for (i in listeners.indices) {
            listeners[i].backgroundListener()
        }
    }

    fun removeListener(listener: ForegroundCallbacksListener?) {
        if (null == listener || !listeners.contains(listener)) {
            return
        }
        listeners.remove(listener)
    }

    companion object {
        private var foregroundCallbacksObserver: ForegroundCallbacksObserver? = null
        val instance: ForegroundCallbacksObserver?
            get() {
                if (null == foregroundCallbacksObserver) {
                    foregroundCallbacksObserver =
                        ForegroundCallbacksObserver()
                }
                return foregroundCallbacksObserver
            }
    }

    init {
        listeners = ArrayList()
    }
}