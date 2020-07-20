package com.sl.bymex.adapter

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sl.bymex.R
import com.sl.bymex.ui.activity.asset.SelectCommonActivity
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.setDrawableLeft
import com.sl.ui.library.widget.material.MaterialRelativeLayout

/**
 * 通用选择
 */
class SelectCommonAdapter(val activity: SelectCommonActivity, data:ArrayList<TabInfo>):
    BaseQuickAdapter<TabInfo, BaseViewHolder>(R.layout.view_common_select_layout,data){
    override fun convert(helper: BaseViewHolder, item: TabInfo) {
        item?.let {
            helper.setVisible(R.id.iv_select,it.selected)
            val tvLeftText = helper.getView<TextView>(R.id.tv_left_text);
            tvLeftText.text = it.name
            if(it.leftIcon!=0){
                tvLeftText.setDrawableLeft(it.leftIcon)
            }
            helper.getView<MaterialRelativeLayout>(R.id.item_layout).setOnClickListener {
                val intent = Intent()
                intent.putExtra(SelectCommonActivity.sSelectResponseKey,item)
                activity.setResult(RESULT_OK,intent)
                activity.finish()
            }
        }
    }
}