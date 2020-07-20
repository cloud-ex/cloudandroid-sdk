package com.sl.ui.library.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.util.DisplayMetrics
import com.sl.ui.library.data.LanguageType
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import java.util.*


object MultiLanguageUtil {
    private var mContext: Context? = null

    fun init(context: Context){
        this.mContext = context
    }

    /**
     * 设置语言
     */
    fun setConfiguration() {
        val targetLocale: Locale = getLanguageLocale()
        val configuration: Configuration = mContext!!.resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale)
        } else {
            configuration.locale = targetLocale
        }
        val resources: Resources = mContext!!.resources
        val dm: DisplayMetrics = resources.getDisplayMetrics()
        resources.updateConfiguration(configuration, dm) //语言更换生效的代码!
    }

    /**
     * 如果不是英文、简体中文、繁体中文，默认返回简体中文
     *
     * @return
     */
    private fun getLanguageLocale(): Locale {
        val languageType =
            LanguageType.values()[PreferenceManager.getInstance(mContext).getSharedInt(PreferenceManager.PREF_LANGUAGE, 0)]
        return when (languageType) {
            LanguageType.SYSTEM -> {
                getSysLocale()
            }
            LanguageType.ENGLISH -> {
                Locale.ENGLISH
            }
            LanguageType.CHINESE_SIMPLIFIED -> {
                Locale.SIMPLIFIED_CHINESE
            }
            else -> {
                getSystemLanguage(getSysLocale())
                Locale.ENGLISH
            }
        }
    }

    private fun getSystemLanguage(locale: Locale): String? {
        return locale.language.toString() + "_" + locale.country
    }

    //以上获取方式需要特殊处理一下
    fun getSysLocale(): Locale {
        val locale: Locale
        locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        return locale
    }

    /**
     * 更新语言
     *
     * @param languageType
     */
    fun updateLanguage(languageType: LanguageType) {
        PreferenceManager.getInstance(mContext).putSharedInt(PreferenceManager.PREF_LANGUAGE,languageType.ordinal)
        setConfiguration()
        //通知语音切换
        EventBusUtil.post(MessageEvent(MessageEvent.SWITCH_APP_LANG_notify))
    }

    /**
     * 得到显示语音名称
     */
    fun getShowLanguageName(context: Context?): String? {
        val languageType =
            LanguageType.values()[PreferenceManager.getInstance(mContext).getSharedInt(PreferenceManager.PREF_LANGUAGE, 0)]
        if (languageType == LanguageType.ENGLISH) {
            return "英文"
        } else if (languageType == LanguageType.CHINESE_SIMPLIFIED) {
            return "中文简体"
        }
        return "英语"
    }

    /**
     * 获取到用户保存的语言类型
     * @return
     */
    fun getLanguageType(): LanguageType {
        return LanguageType.values()[PreferenceManager.getInstance(mContext).getSharedInt(PreferenceManager.PREF_LANGUAGE, 0)]
    }

    fun attachBaseContext(context: Context): Context? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationResources(context)
        } else {
           setConfiguration()
            context
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun createConfigurationResources(context: Context): Context? {
        val resources = context.resources
        val configuration = resources.configuration
        val locale: Locale = getLanguageLocale()
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

}