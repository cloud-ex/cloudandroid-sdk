package com.sl.ui.library.utils

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.sl.ui.library.R

/**
 * TextView显示下划线
 */
fun TextView.showUnderLine(showLine: Boolean = true) {
    if (showLine) {
        paint.flags = Paint.UNDERLINE_TEXT_FLAG
        paint.isAntiAlias = true
    } else {
        paint.isAntiAlias = true
    }
}

fun TextView.setTextUnderLine(textString: String) {
    val spannableString = SpannableString(textString);
    val underlineSpan = UnderlineSpan()
    spannableString.setSpan(
        underlineSpan,
        0,
        spannableString.length,
        Spanned.SPAN_INCLUSIVE_EXCLUSIVE
    )
    text = spannableString

}

fun TextView.setDrawableLeft(resId: Int) {
    setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
}

fun TextView.setDrawableRight(resId: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0)
}

fun TextView.setDrawableLeft(resId: Int, size: Int) {
    if (size > 0) {
        val drawable = resources.getDrawable(resId)
        drawable.setBounds(0, 0, size, size)
        setCompoundDrawables(drawable, null, null, null);
    } else {
        setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
    }

}

/**
 * EditText设置编辑状态
 */
fun EditText.edit(edit: Boolean = true) {
    if (edit) {
        isFocusableInTouchMode = true
        isFocusable = true
        isEnabled = true
        requestFocus()
        if (!TextUtils.isEmpty(text)) {
            setSelection(text.length)
        }

    } else {
        isFocusable = false
        isFocusableInTouchMode = false
        isEnabled = false
        clearFocus()
    }
}

/**
 * 延迟结束
 */
fun Activity.delayFinish(view: View, delayMillis: Long) {
    view.postDelayed({
        finish()
    }, delayMillis)
}

/**
 * activity 对话框动画
 */
fun Activity.startActivityDialogAnim(intent: Intent) {
    startActivity(intent)
    overridePendingTransition(
        R.anim.enter_translate_activity,
        R.anim.enter_translate_activity
    )
}

fun Activity.finishActivityDialogAnim() {
    finish()
    overridePendingTransition(R.anim.exit_translate_activity, R.anim.exit_translate_activity);
}







