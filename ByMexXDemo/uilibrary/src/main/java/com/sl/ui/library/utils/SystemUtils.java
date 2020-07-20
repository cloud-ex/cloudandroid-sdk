package com.sl.ui.library.utils;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.security.MessageDigest;
import java.util.Locale;

import static android.content.Context.CLIPBOARD_SERVICE;

public class SystemUtils {

    /**
     * 获取当前手机系统语言。
     *
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN”
     */
    public static String getSystemLanguage() {
        //todo 先写死返回中文
        return "zh_CN";
    }


    public static boolean isZh() {
        if (getSystemLanguage().equals("zh")) {
            return true;
        } else {
            return getSystemLanguage().equals("zh_CN");
        }
    }

    public static boolean isMn() {
        return getSystemLanguage().equals("mn_MN");
    }

    public static boolean isRussia() {
        return getSystemLanguage().equals("ru_RU");
    }

    public static boolean isKorea() {
        return getSystemLanguage().equals("ko_KR");
    }

    public static boolean isJapanese() {
        return getSystemLanguage().equals("ja_JP");
    }

    public static boolean isSpanish() {
        return getSystemLanguage().equals("es_ES");
    }
    public static boolean isVietNam() {
        return getSystemLanguage().equals("vi_VN");
    }

    public static boolean isTW() {
        return getSystemLanguage().equals("el_GR");
    }


    /**
     * 获取当前系统上的语言列表(Locale列表)
     *
     * @return 语言列表
     */
    public static Locale[] getSystemLanguageList() {
        return Locale.getAvailableLocales();
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取 当前网络状态
     *
     * @param context
     * @return
     */
    public static String getAPNType(Context context) {
        String netType = "4g";
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";// wifi
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager mTelephony = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE) {
                netType = "4G";// 4G
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    && !mTelephony.isNetworkRoaming()) {
                netType = "3G";// 3G
            } else {
                netType = "2G";// 2G
            }

        }
        return netType;
    }

    public static String toMD5(String strText)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update( strText.getBytes() );
            StringBuffer buf=new StringBuffer();
            for(byte b:md.digest())
            {
                buf.append(String.format("%02x", b&0xff) );
            }

            return buf.toString();
        } catch( Exception e ) {
            e.printStackTrace(); return "";
        }
    }

    /**
     * 复制到剪贴版
     * @param context
     * @param text
     */
    public static void copyToClipboard(Context context,String text){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("address", text);
        clipboardManager.setPrimaryClip(clipData);
    }

}
