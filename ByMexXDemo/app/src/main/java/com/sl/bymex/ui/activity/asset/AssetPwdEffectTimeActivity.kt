package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.adapter.AssetPwdEffectTimeAdapter
import com.sl.bymex.api.AssetPwdEffectTimeApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.utils.AppUtils
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.utils.delayFinish
import kotlinx.android.synthetic.main.activity_select_common.*
import java.lang.Exception

/**
 * 资金密码有效期设置
 */
class AssetPwdEffectTimeActivity : BaseEasyActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_asset_effect_time
    }
    //密码有效时间
    var effectTimeList = ArrayList<TabInfo>()
    var effectTimeInfo : TabInfo?=null
    val verifyCodeHelper = VerifyCodeHelper()
    override fun loadData() {
        val assetPasswordEffectiveTime =com.sl.bymex.service.UserHelper.user.asset_password_effective_time
        effectTimeInfo = TabInfo(AppUtils.getAsset_password_effective_time_string(assetPasswordEffectiveTime.toLong()),assetPasswordEffectiveTime)
        effectTimeList.add(TabInfo(AppUtils.getAsset_password_effective_time_string(7200),7200,assetPasswordEffectiveTime == 7200))
        effectTimeList.add(TabInfo(AppUtils.getAsset_password_effective_time_string(900),900,assetPasswordEffectiveTime == 900))
        effectTimeList.add(TabInfo(AppUtils.getAsset_password_effective_time_string(0),0,assetPasswordEffectiveTime == 0))
        effectTimeList.add(TabInfo(AppUtils.getAsset_password_effective_time_string(-1),-1,assetPasswordEffectiveTime == -1||assetPasswordEffectiveTime == -2))
    }

    override fun initView() {
        val adapter = AssetPwdEffectTimeAdapter(this@AssetPwdEffectTimeActivity,effectTimeList)
        view_layout.layoutManager = LinearLayoutManager(this@AssetPwdEffectTimeActivity)
        view_layout.adapter = adapter

        adapter.bindItemClickListener(object:AssetPwdEffectTimeAdapter.ItemClickListener{
            override fun doItem(item: TabInfo) {

                verifyCodeHelper.openAssetPwdCodeDialog(this@AssetPwdEffectTimeActivity,object:
                   VerifyCodeHelper.DialogConfirmListener{
                    override fun doConfirm(code: String) {
                       val assetPwdApi = AssetPwdEffectTimeApi()
                        assetPwdApi.asset_password =SystemUtils.toMD5(code)
                        assetPwdApi.asset_password_effective_time = item.index.toLong()

                        NetHelper.doHttpPost(this@AssetPwdEffectTimeActivity,assetPwdApi,object:HttpCallback<HttpData<String>>(this@AssetPwdEffectTimeActivity){
                            override fun onFail(e: Exception?) {
                                TopToastUtils.showTopFailToast(this@AssetPwdEffectTimeActivity,e?.message?:getString(R.string.str_captcha_verify_failed))
                                verifyCodeHelper.dialogBtnStopLoading()
                            }

                            override fun onSucceed(result: HttpData<String>?) {
                                verifyCodeHelper.dismissVerifyCodeDialog()
                                UserHelper.user.asset_password_effective_time = item.index
                                EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                                this@AssetPwdEffectTimeActivity.delayFinish(view_layout,1500)
                            }
                        })
                    }

                })
            }

        })
    }


    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,AssetPwdEffectTimeActivity::class.java)
            activity.startActivity(intent)
        }
    }
}