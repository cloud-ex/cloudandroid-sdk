package com.sl.bymex.widget

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.sl.bymex.R
import com.sl.bymex.service.PublicInfoHelper
import com.sl.ui.library.adapter.FilterLabelAdapter
import com.sl.ui.library.base.BaseDialogFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.widget.GridDividerItemDecoration
import kotlinx.android.synthetic.main.dialog_dw_record_filter.*

/**
 * 充提记录筛选项
 */
class DwRecordFilterDialog : BaseDialogFragment() {
    override fun setContentView(): Int {
        return R.layout.dialog_dw_record_filter
    }

    private val tabList = ArrayList<TabInfo>()
    private var coin = ""
    var adapter : FilterLabelAdapter?= null
    override fun initView() {
        alpha_ll.setOnClickListener(this)
        lv_view.layoutManager = GridLayoutManager(activity,3)
        adapter = FilterLabelAdapter(tabList)
        lv_view.addItemDecoration(GridDividerItemDecoration(activity,30,25,true,false,resources.getColor(R.color.bg_card_color),0.82))
        lv_view.adapter = adapter

        bt_reset.setOnClickListener {
            tabList.forEach {
                it.selected = TextUtils.equals(coin,it.name)
            }
            adapter?.notifyDataSetChanged()
        }
        tv_confirm.setOnClickListener {
            for (item in tabList)
                if(item.selected){
                    coin = item.name!!
                }
            }
            EventBusUtil.post(MessageEvent(MessageEvent.filter_coin_type_select_notify,coin))
            dismissDialog()
        }

    override fun loadData() {
       val spotCoinList = PublicInfoHelper.getSpotCoin()
        for (index in spotCoinList.indices){
            val item = spotCoinList[index]
            tabList.add(TabInfo(item.name,index,TextUtils.equals(coin,item.name)))
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftOut_rightin_DialogFg_style)
    }


    public  fun showDialog(manager: FragmentManager, tag: String,coin:String) {
        this.coin = coin
        showDialog(manager, tag)
    }

    public override fun showDialog(manager: FragmentManager, tag: String) {
        super.showDialog(manager, tag)
    }

}