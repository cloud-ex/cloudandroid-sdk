package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.view.View
import com.sl.bymex.R
import com.sl.bymex.service.UserHelper
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.utils.DialogUtils
import kotlinx.android.synthetic.main.activity_personal_info.*

/**
 * 个人信息
 */
class PersonaInfoActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_personal_info
    }

    var nickName : String?=""
    override fun loadData() {
    }

    override fun initView() {
        initListener()
    }

    override fun onResume() {
        super.onResume()
        nickName = UserHelper.user.account_name
        item_nick_layout.itemRightText = nickName?:getString(R.string.str_un_setting)
    }
    private fun initListener() {
        rl_photo_layout.setOnClickListener {
            DialogUtils.showCenterDialog(this@PersonaInfoActivity,getString(R.string.str_tips),getString(
                            R.string.str_un_sopport_photo),"",getString(R.string.common_text_btn_i_see),null)
        }

        item_nick_layout.setItemOnClickListener(View.OnClickListener {
            UpdateNickNameActivity.show(this@PersonaInfoActivity)
//            SelectCommonActivity.show(
//                this@PersonaInfoActivity,
//                languageList,
//                getString(R.string.str_language),
//                1001
//            )
        })
    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if((resultCode == Activity.RESULT_OK)){
//            when(requestCode){
//                1001 ->{
//                    //语言
//                    var tabInfo = data?.getParcelableExtra(SelectCommonActivity.sSelectResponseKey) as TabInfo
//                    for(item in languageList){
//                        if(item.index == tabInfo.index){
//                            item.selected = true
//                            languageInfo = item
//                            //保存
//                            ColorUtils.setColorType(item.index)
//                        }else{
//                            item.selected = false
//                        }
//                    }
//                    updateLanguageUI()
//                }
//                1002 ->{
//                    var tabInfo = data?.getParcelableExtra(SelectCommonActivity.sSelectResponseKey) as TabInfo
//                    for(item in riseList){
//                        if(item.index == tabInfo.index){
//                            item.selected = true
//                            riseInfo = item
//                        }else{
//                            item.selected = false
//                        }
//                    }
//                    updateRiseUI()
//                }
//            }
//
//        }
//    }


    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,PersonaInfoActivity::class.java)
            activity.startActivity(intent)
        }
    }
}