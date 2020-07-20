package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.sl.bymex.R
import com.sl.bymex.api.LoginApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.User
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.UserHelper
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.RegexUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_login
    }

    private var name = ""

    private val inputTypeChangedListener =object: CommonInputLayout.EditTextChangedListener{
        override fun onTextChanged(view: CommonInputLayout) {
            updateBtnStatusUi()
        }

    }
    override fun loadData() {//
        name = UserHelper.user.phone?:""
    }

    override fun initView() {
        input_pwd_layout.setPwdType()
        input_phone_layout.inputText = name
        initListener()

        val signUpBtnAnimY =
            SpringAnimation(ll_warp_layout, SpringAnimation.TRANSLATION_Y, 0.0f)
        signUpBtnAnimY.spring.stiffness = SpringForce.STIFFNESS_VERY_LOW
        signUpBtnAnimY.spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        signUpBtnAnimY.setStartVelocity(2000f)
        signUpBtnAnimY.start()
    }

    private fun initListener() {
        input_pwd_layout.setRightListener(View.OnClickListener { input_pwd_layout.openEyeEnable = !input_pwd_layout.openEyeEnable })
        input_phone_layout.setEditTextChangedListener(inputTypeChangedListener)
        input_pwd_layout.setEditTextChangedListener(inputTypeChangedListener)
        /**
         * 忘记密码
         */
        tv_forget.setOnClickListener {
            ForgetPwdActivity.showFirstStep(this@LoginActivity)
        }
        /**
         * 注册
         */
        title_layout.setRightOnClickListener(View.OnClickListener { RegisterActivity.show(this@LoginActivity) })
        /**
         * 点击确认
         */
        bt_commit.isEnable(false)
        bt_commit.listener = object : CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                val name = input_phone_layout.inputText
                val pwd = input_pwd_layout.inputText

                if(pwd.length < 8){
                    TopToastUtils.showTopSuccessToast(this@LoginActivity,getString(R.string.str_password_too_short))
                    return
                }
                val loginApi = LoginApi()
                //是否是邮箱
                val isEmail = RegexUtils.isEmail(name)
                val nonce = System.currentTimeMillis() * 1000
                val savePwd = SystemUtils.toMD5(pwd)
                val md5 = SystemUtils.toMD5(savePwd + nonce.toString())

                loginApi.password = md5
                loginApi.nonce = nonce

                if(isEmail){
                    loginApi.email = name
                }else{
                    //TODO 接口改造之前 先默认写死+86
                    loginApi.phone = "+86 $name"
                }

                bt_commit.showLoading()
                LoginHelper.doRequestLoginToSever(this@LoginActivity,loginApi,object:
                    LoginHelper.LoginListener{
                    override fun loginSuccess(user: User) {
                        bt_commit.hideLoading()
                        UserHelper.doLoginSuccess(user)
                        LoginHelper.loginPwd = savePwd
                        TopToastUtils.showSuccess(this@LoginActivity,getString(R.string.str_login_success))
                        this@LoginActivity.finish()
                    }

                    override fun loginFail(msg: String) {
                        TopToastUtils.showTopSuccessToast(this@LoginActivity,msg)
                        bt_commit.hideLoading()
                    }
                })
            }

        }
    }

    fun updateBtnStatusUi(){
        bt_commit.isEnable(input_phone_layout.inputText.isNotEmpty() and input_pwd_layout.inputText.isNotEmpty())
    }

    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity, LoginActivity::class.java)
            activity.startActivity(intent)
        }
    }
}