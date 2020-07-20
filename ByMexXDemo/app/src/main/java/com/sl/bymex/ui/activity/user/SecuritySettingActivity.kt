package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.GoogleCodeApi
import com.sl.bymex.api.PublicApiConstant
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.ui.activity.asset.AssetPwdActivity
import com.sl.bymex.ui.activity.asset.AssetPwdEffectTimeActivity
import com.sl.bymex.utils.AppUtils
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.PreferenceManager
import kotlinx.android.synthetic.main.activity_security_setting.*

/**
 * 安全设置
 */
class SecuritySettingActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_security_setting
    }



    override fun loadData() {


    }

    override fun initView() {
        updateItemStatus()
        initClickListener()
    }


    private fun updateItemStatus() {
        val user = UserHelper.user
        //手机
        var phone = user.phone ?: ""
        if (phone.isEmpty()) {
            item_phone.itemRightText = getString(R.string.str_unbind)
            item_phone.rightWarn = true
        } else {
            item_phone.itemRightText = phone
            item_phone.rightWarn = false
        }
        //邮箱
        var email = user.email ?: "unbound"
        if (email.isEmpty()) {
            item_email.itemRightText = getString(R.string.str_unbind)
            item_email.rightWarn = true
        } else {
            item_email.itemRightText = email
            item_email.rightWarn = false
        }
        //谷歌验证码
        var gaKey = user.ga_key ?: ""
        if (TextUtils.equals(gaKey, "unbound")) {
            item_google.itemRightText = getString(R.string.str_unbind)
            item_google.rightWarn = true
        } else {
            item_google.itemRightText = getString(R.string.str_unbind_more)
            item_google.rightWarn = false
        }
        //资金密码
        val fundPwdSetted = user.asset_password_effective_time != -2
        if (fundPwdSetted) {
            item_fund_pwd.itemRightText = getString(R.string.str_update)
            item_fund_pwd.rightWarn = false
            item_fund_pwd_effect_time.visibility = View.VISIBLE
        } else {
            item_fund_pwd.itemRightText = getString(R.string.str_un_setting)
            item_fund_pwd.rightWarn = true
            item_fund_pwd_effect_time.visibility = View.GONE
        }
        //资金密码有效期
         item_fund_pwd_effect_time.itemRightText = AppUtils.getAsset_password_effective_time_string( UserHelper.user.asset_password_effective_time.toLong())
        //交易确认
        val treadConfirm = PreferenceManager.getBoolean(
            this@SecuritySettingActivity,
            PreferenceManager.PREF_TRADE_CONFIRM,
            false
        )
        item_tread_confirm.itemChecked = treadConfirm
        item_tread_confirm.setItemOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _, isChecked ->
            PreferenceManager.putBoolean(
                this@SecuritySettingActivity,
                PreferenceManager.PREF_TRADE_CONFIRM,
                isChecked
            )
            item_tread_confirm.itemChecked = isChecked
        })

    }

    override fun onResume() {
        super.onResume()
        //手势密码
        item_gesture_pwd.itemChecked = LoginHelper.openGestureLogin
        //指纹密码
        updateFingerUI()
    }

    private fun updateFingerUI() {
        val isSupportFinger = LoginHelper.fingerprintIdentify?.isFingerprintEnable ?: false
        if (isSupportFinger) {
            item_finger_pwd.visibility = View.VISIBLE
            item_finger_pwd.itemChecked = LoginHelper.openFingerLogin
        } else {
            item_finger_pwd.visibility = View.GONE
        }
    }

    private fun initClickListener() {
        //手机
        item_phone.setItemOnClickListener(View.OnClickListener {
            val user = UserHelper.user
            if (user.phone.isNullOrEmpty()) {
                BindAccountActivity.show(this@SecuritySettingActivity, 0)
            }
        })
        //邮箱
        item_email.setItemOnClickListener(View.OnClickListener {
            val user = UserHelper.user
            if (user.email.isNullOrEmpty()) {
                BindAccountActivity.show(this@SecuritySettingActivity, 1)
            }
        })
        //谷歌验证码
        item_google.setItemOnClickListener(View.OnClickListener {
            val user = UserHelper.user
            var gaKey = user.ga_key ?: "unbound"
            if (TextUtils.equals(gaKey, "unbound")) {
                BindGoogleActivity.show(this@SecuritySettingActivity)
            } else {
                //去解绑
                val codeHelper = VerifyCodeHelper()
                codeHelper.openGoogleVerifyCodeDialog(this@SecuritySettingActivity,object:VerifyCodeHelper.DialogConfirmListener{
                    override fun doConfirm(code: String) {
                        val googleApi = GoogleCodeApi(PublicApiConstant.sGoogleCodeDelete)
                        googleApi.ga_code = code.toLong()

                        NetHelper.doHttpPost(this@SecuritySettingActivity,googleApi,object : HttpCallback<HttpData<String>>(this@SecuritySettingActivity){
                            override fun onFail(e: Exception) {
                                TopToastUtils.showTopSuccessToast(this@SecuritySettingActivity, e.message)
                                codeHelper.dialogBtnStopLoading()
                            }

                            override fun onSucceed(result: HttpData<String>) {
                                codeHelper.dismissVerifyCodeDialog()
                                TopToastUtils.showSuccess(this@SecuritySettingActivity,this@SecuritySettingActivity.getString(R.string.str_bind_success))
                                UserHelper.user.ga_key = "unbound"
                                EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                            }
                        })
                    }

                } )
            }
        })
        //资金密码
        item_fund_pwd.setItemOnClickListener(View.OnClickListener {
            val user = UserHelper.user
            val fundPwdSetted = user.asset_password_effective_time != -2
            if (fundPwdSetted) {
                //修改
                AssetPwdActivity.show(this@SecuritySettingActivity, 1)
            } else {
                //设置
                AssetPwdActivity.show(this@SecuritySettingActivity, 0)
            }
        })
        //资金密码有效期
        item_fund_pwd_effect_time.setItemOnClickListener(View.OnClickListener {
            AssetPwdEffectTimeActivity.show(
                this@SecuritySettingActivity
            )
        })
        //手势
        item_gesture_pwd.setItemOnCheckedClickListener(View.OnClickListener {
            if (!item_gesture_pwd.isCheck()) {
                //解绑验证
                GestureVerifyActivity.show(this@SecuritySettingActivity)
            } else {
                //设置验证
                PwdSettingTipsActivity.show(this@SecuritySettingActivity, 0)
            }
        })
        //指纹
        item_finger_pwd.setItemOnCheckedClickListener(View.OnClickListener {
            if (!item_finger_pwd.isCheck()) {
                //解绑指纹
                DialogUtils.showCenterDialog(this@SecuritySettingActivity,
                    getString(R.string.str_tips),
                    getString(
                        R.string.str_confirm_close_finger_login
                    ),
                    getString(R.string.common_text_btnCancel),
                    getString(R.string.str_confirm),
                    object : DialogUtils.DialogBottomListener {
                        override fun clickTab(tabType: Int) {
                            if (tabType == 1) {
                                //关闭指纹登录,设置为正常登录
                                LoginHelper.openFingerLogin = false
                            }
                            updateFingerUI()
                        }

                    });
            } else {
                //设置验证
                PwdSettingTipsActivity.show(this@SecuritySettingActivity, 1)
            }
        })
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.account_info_change_notify -> {
                updateItemStatus()
            }
        }
    }

    companion object {
        fun show(activity: Activity) {
            val intent = Intent(activity, SecuritySettingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}