package com.sl.bymex.service

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.contract.sdk.ContractSDKAgent
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.LoginApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.app.ByMexApp
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.User
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.ui.activity.user.OtherLoginActivity
import com.sl.bymex.ui.activity.user.LoginActivity
import com.sl.ui.library.utils.*
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import java.lang.Exception

object LoginHelper {
    var fingerprintIdentify: FingerprintIdentify? = null
    var fingerDialog: TDialog? = null

    /**
     * 是否打开指纹登录
     */
    var openFingerLogin = false
        get() {
            return PreferenceManager.getInstance(ContractSDKAgent.context)
                .getSharedBoolean(PreferenceManager.PREF_LOGIN_TYPE_FINGER_KEY, false) and (fingerprintIdentify?.isFingerprintEnable == true)
        }
        set(value) {
            field = value
            PreferenceManager.getInstance(ContractSDKAgent.context)
                .putSharedBoolean(PreferenceManager.PREF_LOGIN_TYPE_FINGER_KEY, value)
        }


    /**
     * 是否打开手势登录
     */
    var openGestureLogin = false
        get() {
            return PreferenceManager.getInstance(ContractSDKAgent.context)
                .getSharedBoolean(PreferenceManager.PREF_LOGIN_TYPE_GESTURE_KEY, false)  and (gesturePwd.isNotEmpty())
        }
        set(value) {
            field = value
            PreferenceManager.getInstance(ContractSDKAgent.context)
                .putSharedBoolean(PreferenceManager.PREF_LOGIN_TYPE_GESTURE_KEY, value)
        }

    /**
     * 手势密码
     */
    var gesturePwd = ""
        set(value) {
            field = value
            PreferenceManager.getInstance(ContractSDKAgent.context)
                .putSharedString(PreferenceManager.PREF_LOGIN_TYPE_KEY + "gesturePwd", value)
        }
        get() {
            return PreferenceManager.getInstance(ContractSDKAgent.context)
                .getSharedString(PreferenceManager.PREF_LOGIN_TYPE_KEY + "gesturePwd", "")
        }

    /**
     * 登录密码(已加密存储)
     */
    var loginPwd = ""
        set(value) {
            field = value
            PreferenceManager.getInstance(ContractSDKAgent.context)
                .putSharedString(PreferenceManager.PREF_LOGIN_TYPE_KEY + "loginPwd", value)
        }
        get() {
            return PreferenceManager.getInstance(ContractSDKAgent.context)
                .getSharedString(PreferenceManager.PREF_LOGIN_TYPE_KEY + "loginPwd", "")
        }


    /**
     * 打开登录页面
     */
    fun openLogin(activity: Activity) {
        val openGesture = openGestureLogin
        val openFinger = openFingerLogin
        if(openFinger || openGesture){
            OtherLoginActivity.show(activity)
        }else{
            LoginActivity.show(activity)
        }
    }

    interface LoginListener {
        fun loginSuccess(user: User)
        fun loginFail(msg: String)
    }

    /**
     * 生成自动登录API
     */
    fun generateAutoLoginApi(): LoginApi {
        val name = UserHelper.user.loginNum
        val pwd = loginPwd

        val loginApi = LoginApi()
        //是否是邮箱
        val isEmail = RegexUtils.isEmail(name)
        val nonce = System.currentTimeMillis() * 1000
        val md5 = SystemUtils.toMD5(pwd + nonce.toString())
        loginApi.password = md5
        loginApi.nonce = nonce
        if (isEmail) {
            loginApi.email = name
        } else {
            if (name.contains("+")) {
                loginApi.phone = name
            } else {
                loginApi.phone = "+86 $name"
            }

        }
        return loginApi
    }

    /**
     * 执行登录
     */
    fun doRequestLoginToSever(
        activity: BaseEasyActivity,
        loginApi: LoginApi,
        loginListener: LoginListener
    ) {
        VerifyCodeHelper.doGraphicCodeCheckApi("login", activity, object :
            VerifyCodeHelper.GraphicCodeListener {
            override fun doVerifySuccess(validate: String) {
                loginApi.validate = validate
                NetHelper.doHttpPost(activity, loginApi, object :
                    HttpCallback<HttpData<User>>(activity) {
                    override fun onSucceed(result: HttpData<User>) {
                        loginListener.loginSuccess(result.data)
                    }

                    override fun onFail(e: Exception) {
                        loginListener.loginFail(
                            e.message ?: activity.getString(R.string.login_failed)
                        )
                    }
                })
            }

            override fun doVerifyFail(msg: String) {
                loginListener.loginFail(msg)
            }

        })
    }

    /**
     * 展示指纹验证弹窗
     */
    fun showFingerVerifyDialog(activity: Activity) {
        fingerDialog = TDialog.Builder((activity as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dialog_finger_verify)
            .setScreenWidthAspect(activity, 0.7f)
            .setGravity(Gravity.CENTER)
            .setDimAmount(0.7f)
            .setCancelableOutside(true)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {

                }
            }
            .addOnClickListener(R.id.tv_cancel, R.id.tv_sure)
            .setOnViewClickListener { viewHolder, view, tDialog ->
                fingerprintIdentify?.cancelIdentify()
                when (view.id) {
                    R.id.tv_cancel -> {
                        tDialog.dismiss()
                    }
                    R.id.tv_sure -> {
                        tDialog.dismiss()
                    }
                }
            }
            .create()
            .show()
    }

    fun hideFingerVerifyDialog() {
        fingerDialog?.dismiss()
    }

    init {
        //指纹
        fingerprintIdentify = FingerprintIdentify(ByMexApp.appContext)
        fingerprintIdentify?.setSupportAndroidL(true)
        fingerprintIdentify?.init()
    }
}