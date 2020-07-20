package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.hjq.http.EasyLog
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.ResetPwdApi
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.api.UserCheckApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.RegexUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_forget_pwd.*
import kotlinx.android.synthetic.main.include_login_header_layout.*
import java.lang.Exception
import java.net.URLEncoder

/**
 * 忘记密码
 */
class ForgetPwdActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_forget_pwd
    }

    private val inputTypeChangedListener = object : CommonInputLayout.EditTextChangedListener {
        override fun onTextChanged(view: CommonInputLayout) {
            updateBtnStatusUi()
        }
    }

    /**
     *  0 第一步  1 第二步
     */
    private var step = 0
    private var code = ""
    private var name = ""
    private var verifyCodeHelper = VerifyCodeHelper()

    override fun loadData() {
        step = intent.getIntExtra("step", 0)
        code = intent.getStringExtra("code")?:""
        name = intent.getStringExtra("name")?:""
    }

    override fun initView() {
        tv_page_type.text = getString(R.string.str_reset_login_pwd)
        input_pwd_layout.setPwdType()
        input_sure_pwd_layout.setPwdType()
        initListener()
        updateStepTypeUI()
    }

    /**
     * 更新注册类型UI
     */
    private fun updateStepTypeUI() {
        if (step == 0) {
            input_phone_layout.visibility = View.VISIBLE
            input_pwd_layout.visibility = View.GONE
            input_sure_pwd_layout.visibility = View.GONE
            tv_update_pwd_tips.visibility = View.GONE
            bt_continue.textContent = getString(R.string.str_continue)
            tv_user_label.visibility = View.GONE
        } else {
            input_phone_layout.visibility = View.GONE
            input_pwd_layout.visibility = View.VISIBLE
            tv_update_pwd_tips.visibility = View.VISIBLE
            input_sure_pwd_layout.visibility = View.VISIBLE
            bt_continue.textContent = getString(R.string.str_submit)
            tv_user_label.visibility = View.VISIBLE
            tv_user_label.text = tv_user_label.text.toString() + " " + name
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
         * 登录
         */
        title_layout.setRightOnClickListener(View.OnClickListener { this@ForgetPwdActivity.finish() })
        /**
         * 点击确认
         */
        bt_continue.isEnable(false)
        bt_continue.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                bt_continue.showLoading()
                val name = input_phone_layout.inputText
                //是否是邮箱
                val isEmail = RegexUtils.isEmail(name)
                if (step == 0) {
                    //第一步
                    doFirstStepCommit(isEmail, name)
                } else {
                    //第二步
                    doSecondStepCommit(isEmail)
                }
            }

        }
    }

    private fun doSecondStepCommit(isEmail: Boolean) {
        val pwd = input_pwd_layout.inputText
        val resetApi = ResetPwdApi()
        if (isEmail) {
            resetApi.email = name
        } else {
            //TODO
            resetApi.phone = "+86 $name"
        }
        resetApi.code = code
        resetApi.password = SystemUtils.toMD5(pwd)

        NetHelper.doHttpPost(
            this@ForgetPwdActivity,
            resetApi,
            object : HttpCallback<HttpData<String>>(this@ForgetPwdActivity) {
                override fun onFail(e: Exception) {
                    super.onFail(e)
                    bt_continue.hideLoading()
                    TopToastUtils.showFail(this@ForgetPwdActivity, e.message)
                }

                override fun onSucceed(result: HttpData<String>?) {
                    super.onSucceed(result)
                    TopToastUtils.showSuccess(
                        this@ForgetPwdActivity,
                        getString(R.string.str_resetpwd_succeed)
                    )
                    this@ForgetPwdActivity.finish()
                }
            })
    }

    private fun doFirstStepCommit(isEmail: Boolean, name: String) {
        //校验用户是否存在
        val userCheckApi = UserCheckApi()
        if (isEmail) {
            userCheckApi.email = name
        } else {
            //TODO 又要打补丁，写死+86了  蛋疼
            userCheckApi.phone = URLEncoder.encode("+86 $name", "utf-8")
        }
        NetHelper.doHttpGet(this@ForgetPwdActivity, userCheckApi,
            object : HttpCallback<HttpData<String>>(this@ForgetPwdActivity) {
                override fun onSucceed(result: HttpData<String>) {
                    val exist = result.getBooleanDataByKey("exist")
                    if (exist) {
                        //用户存在 进行图形验证码验证
                        doGraphicCodeCheck()
                    } else {
                        bt_continue.hideLoading()
                        TopToastUtils.showTopSuccessToast(
                            this@ForgetPwdActivity,
                            getString(R.string.str_account_not_exist)
                        )
                    }

                }

                override fun onFail(e: Exception) {
                    super.onFail(e)
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@ForgetPwdActivity, e.message)
                }
            })
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
            TopToastUtils.showTopSuccessToast(this@ForgetPwdActivity, msg)
        }

        override fun doCommit(code: String) {
            EasyLog.print("doCommit:$code")
            verifyCodeHelper.dismissVerifyCodeDialog()
            //TODO 获得用户输入的验证码，直接进入第二步，目前暂无接口校验，先默认校验成功
            this@ForgetPwdActivity.finish()
            showSecondStep(this@ForgetPwdActivity, code, input_phone_layout.inputText)
        }

    }

    /**
     * 图形码验证
     */
    private fun doGraphicCodeCheck() {
        VerifyCodeHelper.doGraphicCodeCheckApi(
            VerifyCodeHelper.VERIFY_TYPE_RESET_PWD,
            this@ForgetPwdActivity,
            object : VerifyCodeHelper.GraphicCodeListener {
                override fun doVerifySuccess(validate: String) {
                    //图形验证码成功，则发送短信
                    val name = input_phone_layout.inputText
                    //是否是邮箱
                    val isEmail = RegexUtils.isEmail(name)
                    val codeApi = SendVerifyCodeApi()
                    if (isEmail) {
                        codeApi.email = name
                    } else {
                        codeApi.phone = name
                    }
                    codeApi.type =VerifyCodeHelper.VERIFY_TYPE_RESET_PWD
                    codeApi.validate = validate
                    verifyCodeHelper.sendVerifyCode(
                        this@ForgetPwdActivity,
                        codeApi,
                        verifyCodeListener
                    )
                }

                override fun doVerifyFail(msg: String) {
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@ForgetPwdActivity, msg)
                }

            })
    }


    fun updateBtnStatusUi() {
        if (step == 0) {
            bt_continue.isEnable(input_phone_layout.inputText.isNotEmpty())
        } else {
            bt_continue.isEnable(false)
            val pwd = input_pwd_layout.inputText
            val surePwd = input_sure_pwd_layout.inputText
            if (pwd.isNotEmpty() and surePwd.isNotEmpty()) {
                if (TextUtils.equals(pwd, surePwd)) {
                    if ((pwd.length >= 8) and (pwd.length <= 20)) {
                        if (RegexUtils.checkPasWord(pwd)) {
                            bt_continue.isEnable(true)
                        }
                    }
                }
            }

        }
    }

    companion object {
        fun showFirstStep(activity: Activity) {
            val intent = Intent(activity, ForgetPwdActivity::class.java)
            intent.putExtra("step", 0)
            activity.startActivity(intent)
        }

        fun showSecondStep(activity: Activity, code: String? = "", name: String? = "") {
            val intent = Intent(activity, ForgetPwdActivity::class.java)
            intent.putExtra("step", 1)
            intent.putExtra("code", code)
            intent.putExtra("name", name)
            activity.startActivity(intent)
        }
    }
}