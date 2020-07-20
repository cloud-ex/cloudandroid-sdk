package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.contract.sdk.ContractSDKAgent
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.PublicApiConstant
import com.sl.bymex.api.RegisterApi
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.api.UserCheckApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.ui.activity.AreaActivity
import com.sl.bymex.utils.AppUtils
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.RegexUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.bt_continue
import kotlinx.android.synthetic.main.activity_register.input_phone_layout
import kotlinx.android.synthetic.main.activity_register.input_pwd_layout
import kotlinx.android.synthetic.main.activity_register.input_sure_pwd_layout
import kotlinx.android.synthetic.main.activity_register.title_layout
import kotlinx.android.synthetic.main.include_login_header_layout.*
import java.lang.Exception
import java.net.URLEncoder

class RegisterActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_register
    }

    private val inputTypeChangedListener = object : CommonInputLayout.EditTextChangedListener {
        override fun onTextChanged(view: CommonInputLayout) {
            updateBtnStatusUi()
        }
    }

    /**
     * 注册类型  0 手机号 1 邮箱
     */
    private var registerType = 0
    private var verifyCodeHelper = VerifyCodeHelper()

    private var cityCode = "86"
    private var cityName = "中国"
    override fun loadData() {
        registerType = intent.getIntExtra("registerType", 0)
        val areaData = UserHelper.areaData
        cityCode = areaData.code
        cityName = if (ContractSDKAgent.isZhEnv) areaData.cn_name else areaData.us_name
    }

    override fun initView() {
        title_layout.rightText = getString(R.string.str_login)
        tv_city_code.text = "$cityName +$cityCode"
        input_pwd_layout.setPwdType()
        input_sure_pwd_layout.setPwdType()
        initListener()
        updateRegisterTypeUI()
    }

    /**
     * 更新注册类型UI
     */
    private fun updateRegisterTypeUI() {
        if (registerType == 0) {
            rl_countries_container_layout.visibility = View.VISIBLE
            tv_page_type.text = getString(R.string.str_phone_register)
            input_phone_layout.inputHint = getString(R.string.str_phone)
            tv_switch_register.text = getString(R.string.str_email_register)
            input_phone_layout.inputHint = getString(R.string.str_phone)
        } else {
            rl_countries_container_layout.visibility = View.GONE
            tv_page_type.text = getString(R.string.str_email_register)
            input_phone_layout.inputHint = getString(R.string.str_email)
            tv_switch_register.text = getString(R.string.str_phone_register)
            input_phone_layout.inputHint = getString(R.string.str_email)
        }
    }

    private fun initListener() {
        input_pwd_layout.setOnClickListener {
            input_pwd_layout.openEyeEnable = !input_pwd_layout.openEyeEnable
        }
        input_sure_pwd_layout.setOnClickListener {
            input_sure_pwd_layout.openEyeEnable = !input_sure_pwd_layout.openEyeEnable
        }
        input_phone_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_pwd_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_sure_pwd_layout.setEditTextChangedListener(inputTypeChangedListener)
        /**
         * 选择区域
         */
        rl_countries_layout.setOnClickListener {
            AreaActivity.show(this@RegisterActivity, "86")
        }
        /**
         * 切换注册方式
         */
        tv_switch_register.setOnClickListener {
            registerType = if (registerType == 0) {
                1
            } else {
                0
            }
            updateRegisterTypeUI()
        }
        /**
         * 登录
         */
        title_layout.setRightOnClickListener(View.OnClickListener { this@RegisterActivity.finish() })
        /**
         * 用户协议
         */
        tv_user_agreement.setOnClickListener {
           var url =  if(SystemUtils.isZh()){
               PublicApiConstant.BTURL_TERMS_STATEMENT_CN
            }else{
               PublicApiConstant.BTURL_TERMS_STATEMENT_EN
            }
            AppUtils.openHtml(this@RegisterActivity,url,getString(R.string.str_user_agreement),false)
        }
        /**
         * 法律申明
         */
        tv_legal_explain.setOnClickListener {
            var url =  if(SystemUtils.isZh()){
                PublicApiConstant.BTURL_RISK_STATEMENT_CN
            }else{
                PublicApiConstant.BTURL_RISK_STATEMENT_EN
            }
            AppUtils.openHtml(this@RegisterActivity,url,getString(R.string.str_legal_explain),false)
        }
        /**
         * 点击确认
         */
        bt_continue.isEnable(false)
        bt_continue.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                bt_continue.showLoading()
                val name = input_phone_layout.inputText
                //校验用户是否存在
                val userCheckApi = UserCheckApi()
                if (registerType == 0) {//手机
                    //TODO 又要打补丁，写死+86了  蛋疼
                    userCheckApi.phone = URLEncoder.encode("+86 $name", "utf-8")
                } else {
                    userCheckApi.email = name
                }

                NetHelper.doHttpGet(this@RegisterActivity, userCheckApi,
                    object : HttpCallback<HttpData<String>>(this@RegisterActivity) {
                        override fun onSucceed(result: HttpData<String>) {
                            val exist = result.getBooleanDataByKey("exist")
                            if (exist) {
                                //用户存在
                                bt_continue.hideLoading()
                                TopToastUtils.showTopSuccessToast(
                                    this@RegisterActivity,
                                    getString(R.string.str_account_exist)
                                )
                            } else {
                                doGraphicCodeCheck()
                            }
                        }

                        override fun onFail(e: Exception) {
                            bt_continue.hideLoading()
                            TopToastUtils.showTopSuccessToast(this@RegisterActivity, e.message)
                        }
                    })
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
            TopToastUtils.showTopSuccessToast(this@RegisterActivity, msg)
        }

        override fun doCommit(code: String) {
            //获得验证码 开始走注册接口
            val name = input_phone_layout.inputText
            val registerApi = RegisterApi()

            registerApi.code = code
            if (registerType == 0) {//手机
                registerApi.account_type = 2
                registerApi.phone = "+86 $name"
            } else {
                registerApi.account_type = 1
                registerApi.email = name
            }
            registerApi.password = SystemUtils.toMD5(input_pwd_layout.inputText)

            NetHelper.doHttpPost(this@RegisterActivity, registerApi,
                object : HttpCallback<HttpData<String>>(this@RegisterActivity) {
                    override fun onSucceed(result: HttpData<String>) {
                        super.onSucceed(result)
                        TopToastUtils.showSuccess(
                            this@RegisterActivity,
                            getString(R.string.str_register_success)
                        )
                        verifyCodeHelper.dismissVerifyCodeDialog()
                        //注册成功 也可记住登录方式
                        this@RegisterActivity.finish()
                    }

                    override fun onFail(e: Exception) {
                        verifyCodeHelper.dialogBtnStopLoading()
                        TopToastUtils.showFail(this@RegisterActivity, e.message)
                    }
                })
        }

    }

    private fun doGraphicCodeCheck() {
        VerifyCodeHelper.doGraphicCodeCheckApi(VerifyCodeHelper.VERIFY_TYPE_REGISTER,
            this@RegisterActivity,
            object : VerifyCodeHelper.GraphicCodeListener {
                override fun doVerifySuccess(validate: String) {
                    //图形验证码成功，则发送短信
                    val codeApi = SendVerifyCodeApi()
                    val name = input_phone_layout.inputText
                    if (registerType == 0) {
                        codeApi.phone = name
                    } else {
                        codeApi.email = name
                    }
                    codeApi.type = VerifyCodeHelper.VERIFY_TYPE_REGISTER
                    codeApi.validate = validate
                    verifyCodeHelper.sendVerifyCode(
                        this@RegisterActivity,
                        codeApi,
                        verifyCodeListener
                    )
                }

                override fun doVerifyFail(msg: String) {
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@RegisterActivity, msg)
                }

            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AreaActivity.CHOOSE_COUNTRY_CODE && resultCode == Activity.RESULT_OK) {
            val areaData = UserHelper.areaData
            cityCode = areaData.code
            cityName = if (ContractSDKAgent.isZhEnv) areaData.cn_name else areaData.us_name
            tv_city_code.text = "$cityName +$cityCode"
        }
    }

    fun updateBtnStatusUi() {
        if (input_phone_layout.inputText.isEmpty()) {
            bt_continue.isEnable(false)
            return
        }
        val pwd = input_pwd_layout.inputText
        val surePwd = input_sure_pwd_layout.inputText

        if (!TextUtils.equals(pwd, surePwd)) {
            bt_continue.isEnable(false)
            return
        }

        if (!RegexUtils.checkPasWord(pwd)) {
            bt_continue.isEnable(false)
            return
        }
        bt_continue.isEnable(true)
    }

    companion object {
        /**
         * registerType 0 手机号  1 邮箱
         */
        fun show(activity: Activity, registerType: Int? = 0) {
            val intent = Intent(activity, RegisterActivity::class.java)
            intent.putExtra("registerType", registerType)
            activity.startActivity(intent)
        }
    }
}