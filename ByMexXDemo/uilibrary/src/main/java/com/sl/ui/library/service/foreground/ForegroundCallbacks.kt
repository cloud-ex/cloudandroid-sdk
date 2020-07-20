package com.sl.ui.library.service.foreground

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

class ForegroundCallbacks : ActivityLifecycleCallbacks {
    var isForeground = false
        private set
    private var paused = true
    private val handler = Handler()
    private val listeners: MutableList<Listener> =
        CopyOnWriteArrayList()
    private var check: Runnable? = null

    val isBackground: Boolean
        get() = !isForeground

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    override fun onActivityResumed(activity: Activity) {
        paused = false
        val wasBackground = !isForeground
        isForeground = true
        if (check != null) handler.removeCallbacks(check)
        if (wasBackground) {
            for (l in listeners) {
                try {
                    l.onBecameForeground()
                } catch (exc: Exception) {
                    Log.i("ForegroundCallbacks", exc.message)
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        paused = true
        if (check != null) handler.removeCallbacks(check)
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isForeground && paused) {
                    isForeground = false
                    for (l in listeners) {
                        try {
                            l.onBecameBackground()
                        } catch (exc: Exception) {
                            Log.i("ForegroundCallbacks", exc.message)
                        }
                    }
                }
            }
        }.also { check = it }, CHECK_DELAY)
    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle?
    ) {
    }

    override fun onActivityDestroyed(activity: Activity) {}
    interface Listener {
        fun onBecameForeground()
        fun onBecameBackground()
    }

    companion object {
        private const val CHECK_DELAY: Long = 600
        private var instance: ForegroundCallbacks? = null
        fun init(application: Application): ForegroundCallbacks? {
            if (instance == null) {
                instance = ForegroundCallbacks()
                application.registerActivityLifecycleCallbacks(instance)
            }
            return instance
        }

        operator fun get(application: Application): ForegroundCallbacks? {
            if (instance == null) {
                init(application)
            }
            return instance
        }

        operator fun get(ctx: Context): ForegroundCallbacks? {
            if (instance == null) {
                val appCtx = ctx.applicationContext
                if (appCtx is Application) {
                    init(appCtx)
                }
                throw IllegalStateException("Foreground is not initialised and " + "cannot obtain the Application object")
            }
            return instance
        }

        fun get(): ForegroundCallbacks? {
            return instance
        }
    }
}