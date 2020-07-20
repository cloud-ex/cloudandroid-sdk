package com.sl.bymex.utils;


import android.text.TextUtils;
import android.util.Log;

import com.sl.bymex.app.AppConfig;


//日志工具
public class LogUtil {

    public static void v(String tag, String msg) {
        if (AppConfig.Companion.getIS_DEBUG()) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (AppConfig.Companion.getIS_DEBUG()) {
            if(!TextUtils.isEmpty(msg) && msg.length() > 1024){
                Log.d(tag, "spit show:"+msg.substring(0,200));
            }else {
                Log.d(tag, msg);
            }
        }
    }

    public static void i(String tag, String msg) {
        if (AppConfig.Companion.getIS_DEBUG()) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (AppConfig.Companion.getIS_DEBUG()) {
            Log.i(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (AppConfig.Companion.getIS_DEBUG()) {
            Log.i(tag, msg);
        }
    }
}
