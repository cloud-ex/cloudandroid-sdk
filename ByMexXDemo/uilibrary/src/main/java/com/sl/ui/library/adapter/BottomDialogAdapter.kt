package com.sl.ui.library.adapter

import android.view.View
import androidx.core.content.ContextCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sl.ui.library.R
import com.sl.ui.library.data.TabInfo


/**
 * 底部弹出dailog适配器
 */
class BottomDialogAdapter(data: ArrayList<TabInfo>, var position: Int) : BaseQuickAdapter<TabInfo, BaseViewHolder>(
    R.layout.item_string_dialog_adapter, data) {

    override fun convert(helper: BaseViewHolder, item: TabInfo) {
        helper?.setText(R.id.tv_content, item?.name)
        if (position == item?.index) {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.main_yellow))
        } else {
            helper?.setTextColor(R.id.tv_content, ContextCompat.getColor(context, R.color.text_color))
        }

        val viewLine = helper.getView<View>(R.id.view_line)
        if(getItemPosition(item) == data.size-1){
            viewLine.visibility = View.GONE
        }else{
            viewLine.visibility = View.VISIBLE
        }
    }
}