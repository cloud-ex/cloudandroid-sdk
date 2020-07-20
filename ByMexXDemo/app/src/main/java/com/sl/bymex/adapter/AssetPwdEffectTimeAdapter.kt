package com.sl.bymex.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sl.bymex.R
import com.sl.bymex.ui.activity.asset.AssetPwdEffectTimeActivity
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.widget.material.MaterialRelativeLayout
import com.sl.ui.library.utils.setDrawableLeft
/**
 * 通用选择
 */
class AssetPwdEffectTimeAdapter(val activity: AssetPwdEffectTimeActivity, data:ArrayList<TabInfo>):
    BaseQuickAdapter<TabInfo, BaseViewHolder>(R.layout.view_common_select_layout,data){

    private var itemClickListener:ItemClickListener?=null

    override fun convert(helper: BaseViewHolder, item: TabInfo) {
        item?.let {
            helper.setVisible(R.id.iv_select,it.selected)
            val tvLeftText = helper.getView<TextView>(R.id.tv_left_text);
            tvLeftText.text = it.name
            if(it.leftIcon!=0){
                tvLeftText.setDrawableLeft(it.leftIcon)
            }
            helper.getView<MaterialRelativeLayout>(R.id.item_layout).setOnClickListener {
                itemClickListener?.doItem(item)
            }
        }
    }

    fun bindItemClickListener(listener:ItemClickListener){
        this.itemClickListener = listener
    }

    interface ItemClickListener{
        fun doItem(item: TabInfo)
    }
}