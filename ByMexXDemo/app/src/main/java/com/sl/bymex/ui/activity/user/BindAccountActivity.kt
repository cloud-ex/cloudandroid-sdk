package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.InputType
import android.view.View
import com.contract.sdk.ContractSDKAgent
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.BindAccountApi
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.api.UserCheckApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.ui.activity.AreaActivity
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.DisplayUtils
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.delayFinish
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_bind_account.*
import kotlinx.android.synthetic.main.activity_bind_account.bt_continue
import kotlinx.android.synthetic.main.activity_bind_account.input_phone_layout
import kotlinx.android.synthetic.main.activity_bind_account.rl_countries_container_layout
import kotlinx.android.synthetic.main.activity_bind_account.rl_countries_layout
import kotlinx.android.synthetic.main.activity_bind_account.title_layout
import kotlinx.android.synthetic.main.activity_bind_account.tv_city_code
import java.lang.Exception
import java.net.URLEncoder

/**
 * 绑定手机或者邮箱
 */
class BindAccountActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_bind_account
    }

    private val inputTypeChangedListener = object : CommonInputLayout.EditTextChangedListener {
        override fun onTextChanged(view: CommonInputLayout) {
            updateBtnStatusUi()
        }
    }

    /**
     * 注册类型  0 手机号 1 邮箱
     */
    private var bindType = 0
    private var verifyCodeHelper = VerifyCodeHelper()

    private var cityCode = "86"
    private var cityName = "中国"
    override fun loadData() {
        bindType = intent.getIntExtra("bindType", 0)
        val areaData = UserHelper.areaData
        cityCode = areaData.code
        cityName = if (ContractSDKAgent.isZhEnv) areaData.cn_name else areaData.us_name
    }

    override fun initView() {
        tv_city_code.text = "$cityName +$cityCode"
        initListener()
        updateRegisterTypeUI()
    }

    /**
     * 更新注册类型UI
     */
    private fun updateRegisterTypeUI() {
        if (bindType == 0) {
            rl_countries_container_layout.visibility = View.VISIBLE
            input_phone_layout.inputHint = getString(R.string.str_phone)
            input_verify_layout.inputHint = getString(R.string.str_sms_verify_label)
            title_layout.title = getString(R.string.str_bind_phone)
        } else {
            rl_countries_container_layout.visibility = View.GONE
            input_phone_layout.inputHint = getString(R.string.str_email)
            input_verify_layout.inputHint = getString(R.string.str_email_verify_label)
            title_layout.title = getString(R.string.str_bind_email)
        }
    }

    private fun initListener() {
        input_phone_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_verify_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_verify_layout.inputType =  InputType.TYPE_CLASS_NUMBER
        /**
         * 选择区域
         */
        rl_countries_layout.setOnClickListener {
            AreaActivity.show(this@BindAccountActivity, "86")
        }
        /**
         * 获取验证码
         */
        input_verify_layout.setRightListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if (DisplayUtils.isFastClick)
                    return
                val name = input_phone_layout.inputText
                if(name.isNullOrEmpty()){
                    TopToastUtils.showTopSuccessToast(this@BindAccountActivity,"账户不能为空!")
                    return
                }
                checkUserExit(false)
            }

        })

        /**
         * 点击确认
         */
        bt_continue.isEnable(false)
        bt_continue.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                bt_continue.showLoading()
                val name = input_phone_layout.inputText
                checkUserExit(true)
            }
        }
    }

    /**
     * 检测用户是否存在
     */
    private fun checkUserExit(isShowVerifyDialog : Boolean){
        val name = input_phone_layout.inputText
        //校验用户是否存在
        val userCheckApi = UserCheckApi()
        if (bindType == 0) {//手机
            //TODO
            userCheckApi.phone = URLEncoder.encode("+86 $name", "utf-8")
        } else {
            userCheckApi.email = name
        }

        NetHelper.doHttpGet(this@BindAccountActivity, userCheckApi,
            object : HttpCallback<HttpData<String>>(this@BindAccountActivity) {
                override fun onSucceed(result: HttpData<String>) {
                    val exist = result.getBooleanDataByKey("exist")
                    if (exist) {
                        //用户存在
                        bt_continue.hideLoading()
                        TopToastUtils.showTopSuccessToast(
                            this@BindAccountActivity,
                            getString(R.string.str_account_exist)
                        )
                    } else {
                        doGraphicCodeCheck(isShowVerifyDialog)
                    }
                }

                override fun onFail(e: Exception) {
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@BindAccountActivity, e.message)
                }
            })

    }

    private val verifyCodeListener = object : VerifyCodeHelper.VerifyCodeListener {
        override fun reSendCode() {
            doGraphicCodeCheck(true)
        }

        override fun doCancel() {
            bt_continue.hideLoading()
        }

        override fun sendFail(msg: String) {
            bt_continue.hideLoading()
            TopToastUtils.showTopSuccessToast(this@BindAccountActivity, msg)
        }

        override fun doCommit(code: String) {
            val name = input_phone_layout.inputText
            val bindApi = BindAccountApi(bindType)

            if (bindType == 0) {//手机
                bindApi.phone = "+86 $name"
                bindApi.sms_code = input_verify_layout.inputText
                bindApi.email_code = code
            } else {
                bindApi.email = name
                bindApi.sms_code = code
                bindApi.email_code = input_verify_layout.inputText
            }

            NetHelper.doHttpPost(this@BindAccountActivity, bindApi,
                object : HttpCallback<HttpData<String>>(this@BindAccountActivity) {
                    override fun onSucceed(result: HttpData<String>) {
                        super.onSucceed(result)
                        TopToastUtils.showSuccess(
                            this@BindAccountActivity,
                            getString(R.string.str_bind_success)
                        )
                        verifyCodeHelper.dismissVerifyCodeDialog()
                        if(bindType == 0){
                            UserHelper.user.phone = name
                        }else{
                            UserHelper.user.email = name
                        }
                        EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                        this@BindAccountActivity.delayFinish(input_verify_layout,1500)
                    }

                    override fun onFail(e: Exception) {
                        verifyCodeHelper.dialogBtnStopLoading()
                        TopToastUtils.showFail(this@BindAccountActivity, e.message)
                    }
                })
        }

    }


    private val verifyCodeNoDialogListener = object:VerifyCodeHelper.VerifyCodeNoDialogListener{

        override fun sendFail(msg: String) {
            TopToastUtils.showTopSuccessToast(this@BindAccountActivity, msg)
        }
    }


    private fun doGraphicCodeCheck(isShowVerifyDialog : Boolean) {
        VerifyCodeHelper.doGraphicCodeCheckApi(if(bindType == 0 )VerifyCodeHelper.VERIFY_TYPE_BIND_PHONE else VerifyCodeHelper.VERIFY_TYPE_BIND_EMAIL,
            this@BindAccountActivity,
            object : VerifyCodeHelper.GraphicCodeListener {
                override fun doVerifySuccess(validate: String) {
                    //图形验证码成功，则发送短信
                    val codeApi = SendVerifyCodeApi()
                    val name = input_phone_layout.inputText
                    if (bindType == 0) {
                        codeApi.phone = name
                    } else {
                        codeApi.email = name
                    }
                    codeApi.type = if(bindType == 0 )VerifyCodeHelper.VERIFY_TYPE_BIND_PHONE else VerifyCodeHelper.VERIFY_TYPE_BIND_EMAIL
                    codeApi.validate = validate
                    if(isShowVerifyDialog){
                        //需要弹出弹窗，
                        verifyCodeHelper.sendVerifyCode(
                            this@BindAccountActivity,
                            codeApi,
                            verifyCodeListener
                        )
                    }else{
                        verifyCodeHelper.sendVerifyCodeNoDialog(
                            this@BindAccountActivity,
                            codeApi,
                            input_verify_layout.getRightTextView(),
                            verifyCodeNoDialogListener
                        )

                    }
                }

                override fun doVerifyFail(msg: String) {
                    bt_continue.hideLoading()
                    TopToastUtils.showTopSuccessToast(this@BindAccountActivity, msg)
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
        val code = input_verify_layout.inputText

        if (code.length !=6) {
            bt_continue.isEnable(false)
            return
        }
        bt_continue.isEnable(true)
    }

    companion object {
        /**
         * bindType 0 手机号  1 邮箱
         */
        fun show(activity: Activity, bindType: Int? = 0) {
            val intent = Intent(activity, BindAccountActivity::class.java)
            intent.putExtra("bindType", bindType)
            activity.startActivity(intent)
        }
    }
}