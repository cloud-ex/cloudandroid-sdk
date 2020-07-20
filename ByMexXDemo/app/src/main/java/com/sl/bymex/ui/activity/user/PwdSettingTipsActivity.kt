package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import com.hjq.http.EasyLog
import com.sl.bymex.R
import com.sl.bymex.service.LoginHelper
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.CommonlyUsedButton
import com.timmy.tdialog.TDialog
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.activity_pwd_setting_tips.*

/**
 * 手势/指纹密码设置提示
 */
class PwdSettingTipsActivity : BaseActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_pwd_setting_tips
    }

    var setType = 0
    override fun loadData() {
        setType = intent.getIntExtra("setType",0)
    }

    override fun initView() {
        if(setType == 0){
            iv_icon.setImageResource(R.drawable.icon_gesture_tips)
            tv_open_tips.text = getString(R.string.str_gesture_open_tips)
        }else{
            iv_icon.setImageResource(R.drawable.icon_finger_tips)
            tv_open_tips.text = getString(R.string.str_finger_open_tips)
        }

        bt_continue.isEnable(true)
        bt_continue.listener = object : CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                if(setType == 0){
                    GestureSettingActivity.show(this@PwdSettingTipsActivity)
                    this@PwdSettingTipsActivity.finish()
                }else if(setType == 1){
                    LoginHelper.showFingerVerifyDialog(this@PwdSettingTipsActivity)
                    //指纹
                    LoginHelper.fingerprintIdentify?.startIdentify(5,object : BaseFingerprint.IdentifyListener{
                        override fun onSucceed() {
                            LoginHelper.hideFingerVerifyDialog()
                            ToastUtil.shortToast(this@PwdSettingTipsActivity,getString(R.string.str_finger_setting_success))
                            LoginHelper.openFingerLogin = true
                            EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                            this@PwdSettingTipsActivity.finish()
                        }

                        override fun onFailed(isDeviceLocked: Boolean) {
                            ToastUtil.shortToast(this@PwdSettingTipsActivity,getString(R.string.str_setting_fail))
                        }

                        override fun onNotMatch(availableTimes: Int) {
                            ToastUtil.shortToast(this@PwdSettingTipsActivity,getString(R.string.str_setting_fail))
                        }

                        override fun onStartFailedByDeviceLocked() {
                            ToastUtil.shortToast(this@PwdSettingTipsActivity,getString(R.string.str_setting_fail))
                        }

                    })
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LoginHelper.fingerprintIdentify?.cancelIdentify()
    }

    companion object{
        /**
         * 0 手势密码 1 指纹密码
         */
        fun show(activity: Activity,setType : Int){
            val intent = Intent(activity,PwdSettingTipsActivity::class.java)
            intent.putExtra("setType",setType)
            activity.startActivity(intent)
        }
    }
}