package com.sl.ui.library.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import androidx.core.content.ContextCompat
import com.sl.ui.library.R
import java.io.ByteArrayOutputStream
import java.io.IOException

class ColorUtils {
    companion object {
        /**
         * 涨跌色 0 绿涨红跌  1 红涨绿跌
         */
        fun getColorType(context: Context): Int {
            return PreferenceManager.getInstance(context)
                .getSharedInt(PreferenceManager.PREF_RISE_FALL_TYPE, 0)
        }

        fun setColorType(context: Context, type: Int) {
            PreferenceManager.getInstance(context)
                .putSharedInt(PreferenceManager.PREF_RISE_FALL_TYPE, type)
        }

        /**
         *获取主要颜色(红绿)
         * @param isRise 是否是上涨状态
         */
        fun getMainColorType(context: Context, isRise: Boolean = true): Int {
            var colorSelect = getColorType(context)
            val mainGreen = context.resources.getColor(R.color.main_green)
            val mainRed = context.resources.getColor(R.color.main_red)

            if (colorSelect == 0) {
                if (isRise) {
                    return mainGreen
                }
                return mainRed
            } else {
                if (isRise) {
                    return mainRed
                }
                return mainGreen
            }

        }

        fun getMainFullColorType(context: Context, isRise: Boolean = true): Int {
            var colorSelect = getColorType(context)
            val mainGreen = ContextCompat.getColor(context, R.color.main_green_light)
            val mainRed = ContextCompat.getColor(context, R.color.main_red_light)

            if (colorSelect == 0) {
                if (isRise) {
                    return mainGreen
                }
                return mainRed
            } else {
                if (isRise) {
                    return mainRed
                }
                return mainGreen
            }

        }

        /**
         * Get the value of color with specified alpha.
         *
         * @param color
         * @param alpha between 0 to 255.
         * @return Return the color with specified alpha.
         */
        fun getColorAtAlpha(color: Int, alpha: Int): Int {
            require(!(alpha < 0 || alpha > 255)) { "The alpha should be 0 - 255." }
            return Color.argb(
                alpha,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

        /**
         * bitmap转为base64
         * @param bitmap
         * @return
         */
        fun bitmapToBase64(bitmap: Bitmap?): String? {
            var result: String? = null
            var baos: ByteArrayOutputStream? = null
            try {
                if (bitmap != null) {
                    baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    baos.flush()
                    baos.close()
                    val bitmapBytes = baos.toByteArray()
                    result =
                        Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (baos != null) {
                        baos.flush()
                        baos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return result
        }


        /**
         * base64转为bitmap
         * @param base64Data
         * @return
         */
        fun base64ToBitmap(base64Data: String?): Bitmap? {
            val bytes =
                Base64.decode(base64Data, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}