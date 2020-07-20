package com.sl.bymex.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Base64
import com.contract.sdk.ContractSDKAgent
import com.sl.bymex.R
import com.sl.bymex.app.ByMexApp
import com.sl.bymex.service.UserHelper
import com.sl.bymex.ui.activity.user.LoginActivity
import com.sl.ui.library.ui.activity.HtmlActivity
import com.sl.ui.library.utils.CommonUtils

class AppUtils {
    companion object{
        /**
         * 打开html
         */
        fun openHtml(
            context: Activity,
            url: String?,
            title: String?,
            needLogin: Boolean
        ) {
            val urlString = StringBuilder(url!!)
            val intent = Intent(context, HtmlActivity::class.java)
            if (needLogin) {
                val account = UserHelper.user
                if (UserHelper.isLogin()) {
                    var lang = "en-us"
                    if (ContractSDKAgent.isZhEnv) {
                        lang = "zh-cn"
                    }
                    urlString.append("?uid=").append(account.account_id)
                        .append("&lang=").append(lang)
                        .append("&token=").append(account.getToken())
                        .append("&options=Android&version=")
                        .append(CommonUtils.getVersionName(context))
                    intent.putExtra("url", urlString.toString())
                    intent.putExtra("title", title)
                    context.startActivity(intent)
                } else {
                   LoginActivity.show(context)
                }
            } else {
                intent.putExtra("url", urlString.toString())
                intent.putExtra("title", title)
                context.startActivity(intent)
            }
        }

        /**
         * 获得资金密码显示文案
         */
        fun getAsset_password_effective_time_string(asset_password_effective_time : Long): String {
            if (asset_password_effective_time == -1L || asset_password_effective_time == -2L) {
                return ByMexApp.appContext.getString(R.string.str_no_fund_pwd)
            } else if (asset_password_effective_time == 0L) {
                return ByMexApp.appContext.getString(R.string.str_effect_single)
            } else {
                val minutes: Long
                val hours: Long = asset_password_effective_time / 3600
                if (hours > 0) {
                    val leftSeconds: Long = asset_password_effective_time % 3600
                    minutes = leftSeconds / 60
                    return if (minutes > 0) {
                        hours.toString() + " " + ByMexApp.appContext.getString(R.string.str_hours) +
                                " " + minutes + ByMexApp.appContext.getString(R.string.str_minutes)
                    } else {
                        hours.toString() + " " + ByMexApp.appContext.getString(R.string.str_hours)
                    }
                } else {
                    minutes = asset_password_effective_time / 60
                    if (minutes > 0) {
                        return minutes.toString() + " " + ByMexApp.appContext.getString(R.string.str_minutes)
                    }
                }
            }
            return ""
        }

        /**
         * 打开系统游览器
         */
        fun safeOpenUrl(context: Context, url: String?) {
            LogUtil.d("DEBUG_UI", url)
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val content_url = Uri.parse(url)
            intent.data = content_url
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }

        /**
         * string 转图片
         */
        fun stringToBitmap(text: String?): Bitmap? {
            //将字符串转换成Bitmap类型
            var bitmap: Bitmap? = null
            try {
                val bitmapArray: ByteArray
                bitmapArray = Base64.decode(text, Base64.DEFAULT)
                bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }
    }
}