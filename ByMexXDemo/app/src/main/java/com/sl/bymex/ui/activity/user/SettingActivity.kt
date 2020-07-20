package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.view.View
import com.sl.bymex.R
import com.sl.bymex.service.UserHelper
import com.sl.bymex.ui.activity.asset.SelectCommonActivity
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_setting.*

/**
 * 设置
 */
class SettingActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_setting
    }
    //语言
    var languageList = ArrayList<TabInfo>()
    var languageInfo : TabInfo?=null
    //涨跌色
    var riseList = ArrayList<TabInfo>()
    var riseInfo : TabInfo?=null

    override fun loadData() {
        languageList.add(TabInfo(getString(R.string.str_simplified_chinese),0,true))
        languageList.add(TabInfo("English",1,false))
        languageInfo = languageList[0]

        val colorType = ColorUtils.getColorType(this@SettingActivity)
        riseList.add(TabInfo(getString(R.string.str_green_up_red_down),0,colorType==0))
        riseList.add(TabInfo(getString(R.string.str_red_up_green_down),1,colorType==1))
        riseInfo =findInfo(riseList,colorType)
    }

    override fun initView() {
        initListener()
        updateLanguageUI()
        updateRiseUI()
    }

    private fun initListener() {
        item_language_layout.setItemOnClickListener(View.OnClickListener {
            SelectCommonActivity.show(
                this@SettingActivity,
                languageList,
                getString(R.string.str_language),
                1001
            )
        })
        item_rise_layout.setItemOnClickListener(View.OnClickListener {
            SelectCommonActivity.show(
                this@SettingActivity,
                riseList,
                getString(R.string.str_rise_fall_color),
                1002
            )
        })
        if(!UserHelper.isLogin()){
            bt_exit.visibility = View.GONE
        }
        bt_exit.isEnable(true)
        bt_exit.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                DialogUtils.showCenterDialog(this@SettingActivity,"提示","确认退出登录?","","",
                object: DialogUtils.DialogBottomListener{
                    override fun clickTab(tabType: Int) {
                        if(tabType == 1){
                            UserHelper.exitLogin()
                            this@SettingActivity.finish()
                        }
                    }

                })
            }

        }
    }

    private fun updateLanguageUI(){
        item_language_layout.itemRightText = languageInfo?.name?:""
    }
    private fun updateRiseUI(){
        item_rise_layout.itemRightText = riseInfo?.name?:""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((resultCode == Activity.RESULT_OK)){
            when(requestCode){
                1001 ->{
                    //语言
                    var tabInfo = data?.getParcelableExtra(SelectCommonActivity.sSelectResponseKey) as TabInfo
                    for(item in languageList){
                        if(item.index == tabInfo.index){
                            item.selected = true
                            languageInfo = item
                        }else{
                            item.selected = false
                        }
                    }
                    updateLanguageUI()
                }
                1002 ->{
                    var tabInfo = data?.getParcelableExtra(SelectCommonActivity.sSelectResponseKey) as TabInfo
                    for(item in riseList){
                        if(item.index == tabInfo.index){
                            item.selected = true
                            riseInfo = item
                            //保存
                            ColorUtils.setColorType(this@SettingActivity,item.index)
                        }else{
                            item.selected = false
                        }
                    }
                    updateRiseUI()
                }
            }

        }
    }

    fun findInfo(list : ArrayList<TabInfo>,index : Int):TabInfo?{
        for(item in riseList){
            if(item.index == index){
                    return item
            }
        }
        return null
    }

    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,SettingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}