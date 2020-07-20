package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.sl.bymex.R
import com.sl.bymex.adapter.SelectCommonAdapter
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.data.TabInfo
import kotlinx.android.synthetic.main.activity_select_common.*

/**
 * 通用选择器
 */
class SelectCommonActivity : BaseActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_select_common
    }

    var tabInfoList = ArrayList<TabInfo>()
    var title = ""
    var requestCode = 0
    override fun loadData() {
        tabInfoList = intent.getParcelableArrayListExtra<TabInfo>("tabInfo")?: ArrayList<TabInfo>()
        title = intent.getStringExtra("title")?:""
        requestCode = intent.getIntExtra("requestCode",0)
    }

    override fun initView() {
        val adapter = SelectCommonAdapter(this@SelectCommonActivity,tabInfoList)
        view_layout.layoutManager = LinearLayoutManager(this@SelectCommonActivity)
        view_layout.adapter = adapter

        title_layout.title = title
    }


    companion object{
        const val sSelectResponseKey = "select_info_key"
        fun show(activity: Activity, tabInfoList : ArrayList<TabInfo>,title:String,requestCode:Int){
            val intent = Intent(activity,SelectCommonActivity::class.java)
            intent.putExtra("tabInfo",tabInfoList)
            intent.putExtra("title",title)
            intent.putExtra("requestCode",requestCode)
            activity.startActivityForResult(intent,requestCode)
        }
    }
}