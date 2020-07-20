package com.sl.bymex.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.AsyncTask
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.sl.bymex.R
import com.sl.bymex.utils.LogUtil
import org.jetbrains.anko.doAsync
import java.io.IOException
import java.net.URL
import java.util.*

class NTabNavView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(context, attrs, defStyle) {
    private var views: ArrayList<View>? = null
    fun setData(
        imgIDs: ArrayList<Int>,
        titles: ArrayList<String>,
        l: OnClickListener?
    ) {
        if (null == imgIDs || null == titles || imgIDs.size != titles.size) {
            return
        }
        val count = imgIDs.size
        if (count <= 0) return
        removeAllViews()
        val inflater = LayoutInflater.from(getContext())
        views = ArrayList()
        post {
            val itemW = this@NTabNavView.measuredWidth / count
            val itemH = this@NTabNavView.measuredHeight
            LogUtil.d(
                TAG,
                "setData==itemW is $itemW,itemH is $itemH"
            )
            for (i in 0 until count) {
                val view =
                    inflater.inflate(R.layout.item_main_home_tab, null)
                val item_view_ll =
                    view.findViewById<View>(R.id.item_tab_view_ll)
                val params = LayoutParams(itemW, itemH)
                params.gravity = Gravity.CENTER
                item_view_ll.layoutParams = params
                val imageview =
                    item_view_ll.findViewById<ImageView>(R.id.iv_tab_icon)
                val textview = item_view_ll.findViewById<TextView>(R.id.tv_tab_text)
                imageview.isSelected = 0 == i
                textview.isSelected = 0 == i
                imageview.setBackgroundResource(imgIDs[i]!!)
                textview.text = titles[i]
                item_view_ll.tag = i
                item_view_ll.setOnClickListener(l)
                views!!.add(view)
                this@NTabNavView.addView(view)
            }
        }
    }

    fun showCurTabView(curIndex: Int) {
        if (null == views || views!!.size <= 0) return
        for (i in views!!.indices) {
            val view = views!![i]
            val imageview =
                view.findViewById<View>(R.id.iv_tab_icon)
            val textview = view.findViewById<View>(R.id.tv_tab_text)
            imageview.isSelected = i == curIndex
            textview.isSelected = i == curIndex
        }
    }

    companion object {
        private const val TAG = "NTabNavView"

        /**
         * 从网络获取图片 给 ImageView 设置 selector
         *
         * @param normalUrl 获取默认图片的链接
         * @param pressUrl  获取点击图片的链接
         * @param imageView 点击的 view
         */
        fun addSeletorFromNet(
            normalUrl: String,
            pressUrl: String,
            imageView: ImageView
        ) {
            object : AsyncTask<Void?, Void?, Drawable>() {
                protected override fun doInBackground(vararg params: Void?): Drawable? {
                    val drawable =
                        StateListDrawable()
                    val normal = loadImageFromNet(normalUrl)
                    val press = loadImageFromNet(pressUrl)
                    drawable.addState(intArrayOf(android.R.attr.state_selected), press)
                    drawable.addState(intArrayOf(-android.R.attr.state_selected), normal)
                    return drawable
                }

                override fun onPostExecute(drawable: Drawable) {
                    super.onPostExecute(drawable)
                    imageView.setBackgroundDrawable(drawable)
                }
            }.execute()
        }

        /**
         * 从网络获取图片
         *
         * @param netUrl 获取图片的链接
         * @return 返回一个 drawable 类型的图片
         */
        private fun loadImageFromNet(netUrl: String): Drawable? {
            var drawable: Drawable? = null
            try {
                drawable =
                    Drawable.createFromStream(URL(netUrl).openStream(), "netUrl.jpg")
            } catch (e: IOException) {
                Log.e("jinlong", e.message)
            }
            return drawable
        }
    }
}