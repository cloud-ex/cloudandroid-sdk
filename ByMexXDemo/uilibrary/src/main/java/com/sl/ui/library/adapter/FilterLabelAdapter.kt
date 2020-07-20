package com.sl.ui.library.adapter

import android.text.TextUtils
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sl.ui.library.R
import com.sl.ui.library.data.TabInfo

class FilterLabelAdapter (data: ArrayList<TabInfo>) : BaseQuickAdapter<TabInfo, BaseViewHolder>(
    R.layout.view_label_layout, data) {

    override fun convert(helper: BaseViewHolder, item: TabInfo) {
        var tvText = helper.getView<TextView>(R.id.tv_text)
        tvText.text = item.name
        tvText.isSelected = item.selected

        tvText.setOnClickListener {
            for (info in data){
                info.selected = TextUtils.equals(item.name,info.name)
            }
            notifyDataSetChanged()
        }
    }
}