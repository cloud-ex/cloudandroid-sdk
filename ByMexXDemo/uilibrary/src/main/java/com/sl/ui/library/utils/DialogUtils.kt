package com.sl.ui.library.utils

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sl.ui.library.R
import com.sl.ui.library.adapter.BottomDialogAdapter
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.widget.ProgressDrawable
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import org.jetbrains.anko.support.v4.find

class DialogUtils {

    interface DialogOnItemClickListener {
        fun clickItem(position: Int)
    }

    interface DialogBottomListener {
        /**
         * 从左到右 0 1 2...
         */
        fun clickTab(tabType: Int)
    }

    companion object {
        var loadingDialog: TDialog? = null
        fun showLoadingDialog(context: Context): TDialog? {
            loadingDialog = TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_loading_layout)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.0f)
                .setCancelableOutside(true)
                .create()
                .show()
            return loadingDialog
        }

        /**
         * 中间对话框
         */
        fun showCenterDialog(
            context: Context,
            title: String  = context.getString(R.string.str_tips),
            content: String,
            cancelTitle: String = context.getString(R.string.common_text_btnCancel),
            sureTitle:  String = context.getString(R.string.str_confirm),
            clickListener: DialogBottomListener?
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_center_layout)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(R.style.animate_dialog_scale)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        //标题
                        val tvTitle = getView<TextView>(R.id.tv_title)
                        if (TextUtils.isEmpty(title)) {
                            tvTitle.visibility = View.GONE
                        } else {
                            tvTitle.visibility = View.VISIBLE
                            tvTitle.text = title
                        }
                        //内容
                        setText(R.id.tv_content, Html.fromHtml(content))
                        setGone(R.id.tv_cancel, !TextUtils.isEmpty(cancelTitle))
                        //确认按钮
                        if (!TextUtils.isEmpty(sureTitle)) {
                            setText(R.id.tv_sure, sureTitle)
                        }
                    }
                }
                .addOnClickListener(R.id.tv_cancel, R.id.tv_sure)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                            clickListener?.clickTab(0)
                        }
                        R.id.tv_sure -> {
                            tDialog.dismiss()
                            clickListener?.clickTab(1)
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 标题带有icon的弹窗
         */
        fun showBottomDialog(
            context: Context,
            title: String = "",
            titleIcon: Int = 0,
            content: String,
            cancelTitle: String = "",
            sureTitle: String = "",
            clickListener: DialogBottomListener?
        ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_normal_layout)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        //标题
                        val tvTitle = getView<TextView>(R.id.tv_title)
                        if (TextUtils.isEmpty(title)) {
                            tvTitle.visibility = View.GONE
                        } else {
                            tvTitle.visibility = View.VISIBLE
                            tvTitle.text = title
                            if (titleIcon > 0) {
                                tvTitle.setDrawableLeft(titleIcon, 70)
                            }
                        }
                        //内容
                        setText(R.id.tv_content,content.replace("\\n","\n"))
                        setGone(R.id.tv_cancel, !TextUtils.isEmpty(cancelTitle))
                        //确认按钮
                        if (!TextUtils.isEmpty(sureTitle)) {
                            setText(R.id.tv_sure, title)
                        } else {
                            setGone(R.id.tv_sure, false)
                        }
                    }
                }
                .addOnClickListener(R.id.tv_cancel, R.id.tv_sure, R.id.rl_title_close)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                            clickListener?.clickTab(0)
                        }
                        R.id.rl_title_close -> {
                            tDialog.dismiss()
                        }
                        R.id.tv_sure -> {
                            tDialog.dismiss()
                            clickListener?.clickTab(1)
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 底部列表选择弹窗
         */
        fun showListDialog(
            context: Context,
            list: ArrayList<TabInfo>,
            position: Int, listener: DialogOnItemClickListener
        ): TDialog {

            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_content_list_layout)
                .setScreenWidthAspect(context, 1f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    var adapter = BottomDialogAdapter(list, position)
                    var listView = viewHolder?.getView<RecyclerView>(R.id.recycler_view)
                    listView?.layoutManager = LinearLayoutManager(context)
                    listView?.adapter = adapter
                    listView?.setHasFixedSize(true)
                    adapter.setOnItemClickListener { adapter, view, position ->
                        listener.clickItem(position)
                    }
                }
                .addOnClickListener(R.id.tv_cancel)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_cancel -> {
                            tDialog.dismiss()
                        }
                    }
                }
                .create()
                .show()
        }

    }
}