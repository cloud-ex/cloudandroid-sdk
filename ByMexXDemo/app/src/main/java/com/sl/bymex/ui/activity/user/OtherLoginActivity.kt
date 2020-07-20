package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.sl.bymex.R
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.User
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.widget.gesture.LockPatternUtil
import com.sl.bymex.widget.gesture.LockPatternView
import com.sl.ui.library.utils.*
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.activity_other_login.*
import kotlinx.android.synthetic.main.activity_other_login.lpv_gesture
import kotlinx.android.synthetic.main.activity_other_login.tv_login_info

/**
 * 手势/指纹登录
 */
class OtherLoginActivity : BaseEasyActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_other_login
    }
    var user = UserHelper.user
    var openGestureLogin = false
    var openFingerLogin = false

    /**
     * 处理手势逻辑
     */
    private val mPatternListener  = object: LockPatternView.OnPatternListener{
        override fun onPatternStart() {
            lpv_gesture.removePostClearPatternRunnable()
            lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT);
        }

        override fun onPatternComplete(pattern: MutableList<LockPatternView.Cell>) {
            val localPwd  = LoginHelper.gesturePwd
            // EasyLog.print("localPwd:$localPwd"+"="+localPwd.toByteArray(Charsets.ISO_8859_1))
            if (pattern != null && !TextUtils.isEmpty(localPwd)){
                if( LockPatternUtil.checkPattern(pattern, localPwd.toByteArray(Charsets.ISO_8859_1))){
                    lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
                    onVerifyCorrect()
                }else{
                    lpv_gesture.setPattern(LockPatternView.DisplayMode.ERROR)
                    lpv_gesture.postClearPatternRunnable(500)
                    TopToastUtils.showTopSuccessToast(this@OtherLoginActivity, getString(R.string.str_gesture_error))
                }
            }
        }

    }

    override fun loadData() {
        openGestureLogin = LoginHelper.openGestureLogin
        openFingerLogin = LoginHelper.openFingerLogin
    }

    override fun initView() {
        tv_login_info.text = user.loginNum
        //手势登录
        if(openGestureLogin){
            lpv_gesture.visibility = View.VISIBLE
            iv_finger_icon.visibility = View.GONE
            tv_finger_unlock.visibility = View.GONE
            lpv_gesture.setOnPatternListener(mPatternListener)
            if(openFingerLogin){
                tv_finger_login.visibility = View.VISIBLE
                view_spit_line.visibility = View.VISIBLE
            }
        }else{
            lpv_gesture.visibility = View.GONE
            iv_finger_icon.visibility = View.VISIBLE
            tv_finger_unlock.visibility = View.VISIBLE
        }
        //指纹打开。默认进行指纹解锁
        if(openFingerLogin){
            doFingerVerify()
            //点击指纹解锁
            iv_finger_icon.setOnClickListener {
                doFingerVerify()
            }

            tv_finger_login.setOnClickListener {
                doFingerVerify()
            }
        }
        //密码登录
        tv_pwd_login.setOnClickListener {
            LoginActivity.show(this@OtherLoginActivity)
            finish()
        }
    }

    private fun doFingerVerify() {
        LoginHelper.showFingerVerifyDialog(this@OtherLoginActivity)
        //指纹
        LoginHelper.fingerprintIdentify?.startIdentify(5,
            object : BaseFingerprint.IdentifyListener {
                override fun onSucceed() {
                    LoginHelper.hideFingerVerifyDialog()
                    onVerifyCorrect()
                }

                override fun onFailed(isDeviceLocked: Boolean) {
                    ToastUtil.shortToast(
                        this@OtherLoginActivity,
                        getString(R.string.str_captcha_verify_failed)
                    )
                }

                override fun onNotMatch(availableTimes: Int) {
                    ToastUtil.shortToast(
                        this@OtherLoginActivity,
                        getString(R.string.str_captcha_verify_failed)
                    )
                }

                override fun onStartFailedByDeviceLocked() {
                    ToastUtil.shortToast(
                        this@OtherLoginActivity,
                        getString(R.string.str_captcha_verify_failed)
                    )
                }

            })
    }

    private fun onVerifyCorrect() {
        LoginHelper.doRequestLoginToSever(this@OtherLoginActivity,LoginHelper.generateAutoLoginApi(),object:
            LoginHelper.LoginListener{
            override fun loginSuccess(user: User) {
                UserHelper.doLoginSuccess(user)
                TopToastUtils.showSuccess(this@OtherLoginActivity,getString(R.string.str_login_success))
                this@OtherLoginActivity.delayFinish(iv_finger_icon,1500)
            }

            override fun loginFail(msg: String) {
                TopToastUtils.showTopSuccessToast(this@OtherLoginActivity,msg)
            }
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        LoginHelper.fingerprintIdentify?.cancelIdentify()
    }


    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,OtherLoginActivity::class.java)
            activity.startActivity(intent)
        }
    }
}