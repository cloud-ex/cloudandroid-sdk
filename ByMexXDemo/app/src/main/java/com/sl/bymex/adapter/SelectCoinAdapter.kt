package com.sl.bymex.adapter

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.sl.bymex.R
import com.sl.bymex.data.SpotCoin
import com.sl.bymex.ui.activity.asset.SelectCoinActivity
import com.sl.bymex.ui.activity.asset.SelectCommonActivity
import com.sl.ui.library.widget.material.MaterialRelativeLayout

/**
 * 选择币种
 */
class SelectCoinAdapter(val activity: SelectCoinActivity, data:ArrayList<SpotCoin>):
    BaseQuickAdapter<SpotCoin, BaseViewHolder>(R.layout.view_coin_select_layout,data){
    override fun convert(helper: BaseViewHolder, item: SpotCoin) {
        item?.let {
            helper.setVisible(R.id.iv_select,it.selected)
            val tvLeftText = helper.getView<TextView>(R.id.tv_left_text);
            tvLeftText.text = it.name
            val leftImageView = helper.getView<ImageView>(R.id.iv_left_icon)
            val options = RequestOptions()
            options.placeholder(R.drawable.icon_coin_default)
            Glide.with(activity).load(it.small).apply(options).into(leftImageView)

            helper.getView<MaterialRelativeLayout>(R.id.item_layout).setOnClickListener {
                val intent = Intent()
                intent.putExtra(SelectCoinActivity.sSelectResponseKey,item)
                activity.setResult(RESULT_OK,intent)
                activity.finish()
            }
        }
    }
}