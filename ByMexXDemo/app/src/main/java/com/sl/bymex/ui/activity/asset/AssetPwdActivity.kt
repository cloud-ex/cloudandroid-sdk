package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.AssetPwdApi
import com.sl.bymex.api.PublicApiConstant
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.utils.delayFinish
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_asset_pwd.*
import java.lang.Exception

/**
 * 资金密码
 */
class AssetPwdActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_asset_pwd
    }

    var type = 0
    var isPhoneVerify = true
    var showGoogleView = false
    var codeHelper = VerifyCodeHelper()

    override fun loadData() {
        type = intent.getIntExtra("type",0)
    }


    override fun initView() {
        if(type == 0){
            title_layout.title = getString(R.string.str_setting_asset_pwd)
            tv_update_asset_pwd_tips.visibility = View.GONE
        }else{
            tv_update_asset_pwd_tips.visibility = View.VISIBLE
            input_google_code_layout.visibility = View.VISIBLE
            title_layout.title = getString(R.string.str_update_asset_pwd)
        }

        input_sure_pwd_layout.openEyeEnable = false
        input_pwd_layout.openEyeEnable = false
        input_sure_pwd_layout.setRightListener(View.OnClickListener { input_sure_pwd_layout.openEyeEnable = !input_sure_pwd_layout.openEyeEnable })
        input_pwd_layout.setRightListener(View.OnClickListener { input_pwd_layout.openEyeEnable = !input_pwd_layout.openEyeEnable })
        input_sure_pwd_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_pwd_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_google_code_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_pwd_layout.setNumberPwdType()
        input_sure_pwd_layout.setNumberPwdType()
        input_google_code_layout.setNumberPwdType()

        if(TextUtils.equals(UserHelper.user.ga_key,"unbound")){
            showGoogleView  =  false
            input_google_code_layout.visibility = View.INVISIBLE
        }else{
            showGoogleView = true
            input_google_code_layout.visibility = View.VISIBLE
        }

        initClickListener()
    }

    private fun initClickListener() {
        bt_continue.isEnable(false)
        bt_continue.listener = object: CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                bt_continue.showLoading()
                if(type == 0){
                    //新增
                    doAddAssetPwd()
                }else{
                    //修改
                    doUpdateAssetPwd()
                }
            }

        }
    }

    private val verifyCodeListener = object : VerifyCodeHelper.VerifyCodeListener {
        override fun reSendCode() {
            doGraphicCodeCheck()
        }

        override fun doCancel() {
            bt_continue.hideLoading()
        }

        override fun sendFail(msg: String) {
            bt_continue.hideLoading()
            TopToastUtils.showTopSuccessToast(this@AssetPwdActivity, msg)
        }

        override fun doCommit(code: String) {
            val assetApi = AssetPwdApi(PublicApiConstant.sAssetPasswordReset)
            assetApi.asset_password = SystemUtils.toMD5(SystemUtils.toMD5(input_pwd_layout.inputText))
            assetApi.sms_code = code
            if(showGoogleView){
                val google_code = input_google_code_layout.inputText
                if(!TextUtils.isEmpty(google_code)){
                    assetApi.ga_code = google_code.toLong()
                }
            }
            NetHelper.doHttpPost(
                this@AssetPwdActivity,
                assetApi,
                object : HttpCallback<HttpData<String>>(this@AssetPwdActivity) {
                    override fun onSucceed(result: HttpData<String>?) {
                        TopToastUtils.showSuccess(
                            this@AssetPwdActivity,
                            getString(R.string.str_setting_success)
                        )
                        UserHelper.user.asset_password_effective_time = -1
                        EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                        this@AssetPwdActivity.delayFinish(bt_continue, 1500)
                    }

                    override fun onFail(e: Exception) {
                        bt_continue.hideLoading()
                        TopToastUtils.showTopSuccessToast(this@AssetPwdActivity, e.message)
                    }
                })
        }


    }

    /**
     * 图片验证
     */
    private fun doGraphicCodeCheck(){
        VerifyCodeHelper.doGraphicCodeCheckApi(VerifyCodeHelper.VERIFY_TYPE_RESET_FUND_PWD,this@AssetPwdActivity,object:
            VerifyCodeHelper.GraphicCodeListener{
            override fun doVerifySuccess(validate: String) {
                val codeApi = SendVerifyCodeApi()
               if(isPhoneVerify){
                   codeApi.phone = UserHelper.user.phone
               }else{
                   codeApi.email = UserHelper.user.email
               }
                codeApi.type =VerifyCodeHelper.VERIFY_TYPE_RESET_PWD
                codeApi.validate = validate
                codeHelper.sendVerifyCode(
                    this@AssetPwdActivity,
                    codeApi,
                    verifyCodeListener
                )
            }

            override fun doVerifyFail(msg: String) {
                bt_continue.hideLoading()
                TopToastUtils.showTopFailToast(this@AssetPwdActivity, msg)
            }

        })

    }
    private fun doUpdateAssetPwd() {
        //发送验证码
        val user = UserHelper.user
        val phone = user.phone?:""
        val email = user.email?:""
        val state = user.status
        isPhoneVerify = UserHelper.isPhoneVerify()
        doGraphicCodeCheck()
    }
    private fun doAddAssetPwd() {
        val assetApi = AssetPwdApi(PublicApiConstant.sAssetPasswordAdd)
        assetApi.asset_password = SystemUtils.toMD5(SystemUtils.toMD5(input_pwd_layout.inputText))
        NetHelper.doHttpPost(
            this@AssetPwdActivity,
            assetApi,
            object : HttpCallback<HttpData<String>>(this@AssetPwdActivity) {
                override fun onSucceed(result: HttpData<String>?) {
                    TopToastUtils.showSuccess(
                        this@AssetPwdActivity,
                        getString(R.string.str_setting_success)
                    )
                    UserHelper.user.asset_password_effective_time = -1
                    EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                    this@AssetPwdActivity.delayFinish(bt_continue, 1500)
                }

                override fun onFail(e: Exception) {
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@AssetPwdActivity, e.message)
                }
            })
    }

    private val inputTypeChangedListener = object : CommonInputLayout.EditTextChangedListener {
        override fun onTextChanged(view: CommonInputLayout) {
            updateBtnStatusUi()
        }
    }

    private fun updateBtnStatusUi() {
        val pwd = input_pwd_layout.inputText
        val surePwd = input_sure_pwd_layout.inputText
        if (pwd.length != 6) {
            bt_continue.isEnable(false)
            return
        }
        if (surePwd.length != 6) {
            bt_continue.isEnable(false)
            return
        }
        if(!TextUtils.equals(pwd,surePwd)){
            bt_continue.isEnable(false)
            return
        }

        if(showGoogleView){
            if(input_google_code_layout.inputText.length!=6){
                bt_continue.isEnable(false)
            }
        }

        bt_continue.isEnable(true)
    }

    companion object {
        /**
         * o 增加  1修改
         */
        fun show(activity: Activity,type:Int) {
            val intent = Intent(activity, AssetPwdActivity::class.java)
            intent.putExtra("type",type)
            activity.startActivity(intent)
        }
    }
}