package com.sl.ui.library.utils;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.hjq.xtoast.XToast;
import com.sl.ui.library.R;

public class TopToastUtils {

    /**
     * 展示顶部Toast
     * @param activity
     * @param text
     */
    public static void showTopSuccessToast(Activity activity, CharSequence text) {
        new XToast(activity)
                .setDuration(2000)
                .setView(R.layout.view_top_toast_success_layout)
                .setAnimStyle(R.style.TopToastStyle)
                .setGravity(Gravity.TOP)
                .setText(R.id.tv_toast, text)
                .show();
    }

    public static void showTopToast(Activity activity, CharSequence text) {
        new XToast(activity)
                .setDuration(2000)
                .setView(R.layout.view_top_toast_normal_layout)
                .setGravity(Gravity.TOP)
                .setAnimStyle(R.style.TopToastStyle)
                .setText(R.id.tv_toast, text)
                .show();
    }


    public static void showTopFailToast(Activity activity, CharSequence text) {
        new XToast(activity)
                .setDuration(2000)
                .setView(R.layout.view_top_toast_fail_layout)
                .setGravity(Gravity.TOP)
                .setAnimStyle(R.style.TopToastStyle)
                .setText(R.id.tv_toast, text)
                .show();
    }

    /**
     * 失败提示
     * @param activity
     * @param text
     */
    public static void showFail(Activity activity, CharSequence text) {
        new XToast(activity)
                .setDuration(2000)
                .setView(R.layout.view_toast_layout)
                .setGravity(Gravity.BOTTOM)
                .setYOffset(300)
                .setAnimStyle(android.R.style.Animation_Dialog)
               .setImageDrawable(R.id.iv_icon, R.drawable.icon_toast_fail)
                .setText(R.id.tv_toast, text)
                .show();
    }



    /**
     * 成功提示
     * @param activity
     * @param text
     */
    public static void showSuccess(Activity activity, CharSequence text) {
        new XToast(activity)
                .setDuration(2000)
                .setView(R.layout.view_toast_layout)
                .setGravity(Gravity.BOTTOM)
                .setYOffset(300)
                .setAnimStyle(android.R.style.Animation_Dialog)
                .setImageDrawable(R.id.iv_icon, R.drawable.icon_toast_success)
                .setText(R.id.tv_toast, text)
                .show();
    }
}
