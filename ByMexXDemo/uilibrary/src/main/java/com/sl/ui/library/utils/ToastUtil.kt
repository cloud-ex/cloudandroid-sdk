package com.sl.ui.library.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.Toast
import com.hjq.xtoast.XToast
import com.sl.ui.library.R

/**
 * Created by ChenLiheng on 2016/3/31.
 * desc：
 */
object ToastUtil {
    fun shortToast(context: Context, resId: Int) {
        toast(context, context.getString(resId), null, Toast.LENGTH_SHORT)
    }

    fun shortToast(context: Context, resId: Int, icon: Drawable?) {
        toast(context, context.getString(resId), icon, Toast.LENGTH_SHORT)
    }

    fun shortToast(
        context: Context,
        text: CharSequence,
        icon: Drawable?
    ) {
        toast(context, text, icon, Toast.LENGTH_SHORT)
    }

    fun shortToast(context: Context, text: CharSequence) {
        toast(context, text, null, Toast.LENGTH_SHORT)
    }
    fun shortToast(context: Context, text: String) {
        toast(context, text, null, Toast.LENGTH_SHORT)
    }

    fun longToast(context: Context, resId: Int) {
        toast(context, context.getString(resId), null, Toast.LENGTH_LONG)
    }

    fun longToast(context: Context, resId: Int, icon: Drawable?) {
        toast(context, context.getString(resId), icon, Toast.LENGTH_LONG)
    }

    fun longToast(context: Context, text: CharSequence) {
        toast(context, text, null, Toast.LENGTH_LONG)
    }

    private fun toast(
        context: Context,
        text: CharSequence,
        icon: Drawable?,
        duration: Int
    ) {
        Toast.makeText(context, text, duration).show()
    }
}