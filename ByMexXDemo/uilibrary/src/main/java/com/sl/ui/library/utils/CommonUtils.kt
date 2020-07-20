package com.sl.ui.library.utils

import android.content.Context
import android.widget.TextView
import java.io.InputStream
import java.nio.charset.Charset

class CommonUtils {
    companion object{
        /**
         * 是否隐藏资产
         */
        fun showAssetEye(isShow:Boolean , text:String , textView:TextView){
            if(isShow){
                textView.text = text
            }else{
                textView.text = "*****"
            }
        }

        /**
         * 从asset中读取json String
         */
        fun readJsonFromAsset(context: Context,fileName:String): String {
            val stream: InputStream = context.assets.open(fileName)
            val size = stream.available()
            val byteArray = ByteArray(size)
            stream.read(byteArray)
            stream.close()
            return String(byteArray, Charset.defaultCharset())
        }

        /**
         * 获取版本名
         */
        fun getVersionName(context: Context): String {
            return try {
                context.packageManager
                    .getPackageInfo(context.packageName, 0).versionName
            } catch (e: Exception) {
                return ""
            }
        }
    }
}