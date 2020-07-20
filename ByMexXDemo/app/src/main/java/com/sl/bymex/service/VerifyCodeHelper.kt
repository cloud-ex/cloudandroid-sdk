package com.sl.bymex.service

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.GraphicCodeCheckApi
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.service.timer.Countdown
import com.sl.ui.library.service.vcedittext.VerificationAction
import com.sl.ui.library.service.vcedittext.VerificationCodeEditText
import com.sl.ui.library.utils.DisplayUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.CommonlyUsedButton
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder


/**
 * 验证码Helper
 */
class VerifyCodeHelper {
    /**
     * 倒计时
     */
    var mCountdown: Countdown? = null
    var mTDialog: TDialog? = null
    var baseActivity: BaseEasyActivity? = null
    var requestApi: SendVerifyCodeApi? = null
    var codeListener : VerifyCodeListener? = null
    var codeNoDialogListener : VerifyCodeNoDialogListener? = null

    /**
     * 消失对话框
     */
    fun dismissVerifyCodeDialog(){
        if(mTDialog!=null){
            dialogBtnStopLoading()
            mTDialog?.dismiss()
        }
    }

    /**
     * 停止按钮进度
     */
    fun dialogBtnStopLoading(){
        if(mTDialog!=null){
          val submitBtm =   mTDialog?.dialog?.findViewById<CommonlyUsedButton>(R.id.bt_commit)
            submitBtm?.hideLoading()
        }
    }

    /**
     * 打开短信/邮箱验证码对话框
     */
    fun openVerifyCodeDialog(context: Context) {
        if (mCountdown?.isRunning == true) {
            mCountdown?.stop()
        }
        mTDialog = TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dalog_user_code_layout)
            .setScreenWidthAspect(context, 1.0f)
            .setGravity(Gravity.BOTTOM)
            .setDimAmount(0.8f)
            .setCancelableOutside(false)
            .setDialogAnimationRes(R.style.animate_dialog)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {
                    val tvSendCode = getView<TextView>(R.id.tv_send_code)
                    val etCode = getView<VerificationCodeEditText>(R.id.et_code)
                    val btCommit = getView<CommonlyUsedButton>(R.id.bt_commit)
                    var name = if(requestApi?.phone.isNullOrEmpty()) requestApi?.email else requestApi?.phone
                    getView<TextView>(R.id.tv_sms_code_tips).text = String.format(context.getString(R.string.str_sms_code_tips),name)
                    //code
                    initCodeListener(etCode, btCommit)
                    //倒计时
                    initCountDown(tvSendCode, context)
                    tvSendCode.setOnClickListener {
                        if (DisplayUtils.isFastClick)
                            return@setOnClickListener
                        codeListener?.reSendCode()
                    }
                    btCommit.listener = object : CommonlyUsedButton.OnBottomListener {
                        override fun bottomOnClick() {
                            btCommit.showLoading()
                            codeListener?.doCommit(etCode.text.toString())
                        }

                    }
                }
            }
            .addOnClickListener(R.id.iv_close, R.id.bt_commit)
            .setOnDismissListener {
                mCountdown?.stop()
                mTDialog = null
                codeListener?.doCancel()
            }
            .setOnViewClickListener { viewHolder, view, tDialog ->
                when (view.id) {
                    R.id.iv_close -> {
                        tDialog.dismiss()
                    }
                    R.id.bt_commit -> {
                        tDialog.dismiss()
                    }
                }
            }
            .create().show()
    }

    /**
     * 打开资金密码对话框
     */
    fun openAssetPwdCodeDialog(context: BaseEasyActivity,btnListener : DialogConfirmListener) {
        if (mCountdown?.isRunning == true) {
            mCountdown?.stop()
        }
        mTDialog = TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dalog_asset_pwd_layout)
            .setScreenWidthAspect(context, 1.0f)
            .setGravity(Gravity.BOTTOM)
            .setDimAmount(0.8f)
            .setCancelableOutside(false)
            .setDialogAnimationRes(R.style.animate_dialog)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {
                    val etCode = getView<VerificationCodeEditText>(R.id.et_code)
                    val btCommit = getView<CommonlyUsedButton>(R.id.bt_commit)

                    initCodeListener(etCode, btCommit)

                    btCommit.listener = object : CommonlyUsedButton.OnBottomListener {
                        override fun bottomOnClick() {
                            btCommit.showLoading()
                            btnListener.doConfirm( etCode.text.toString())
                        }

                    }
                }
            }
            .addOnClickListener(R.id.iv_close, R.id.bt_commit)
            .setOnDismissListener {
                mCountdown?.stop()
                mTDialog = null
                codeListener?.doCancel()
            }
            .setOnViewClickListener { viewHolder, view, tDialog ->
                when (view.id) {
                    R.id.iv_close -> {
                        tDialog.dismiss()
                    }
                    R.id.bt_commit -> {
                        if(btnListener == null){
                            tDialog.dismiss()
                        }
                    }
                }
            }
            .create().show()
    }
    /**
     * 打开谷歌验证码对话框
     */
    fun openGoogleVerifyCodeDialog(context: BaseEasyActivity,btnListener : DialogConfirmListener):TDialog? {
        if (mCountdown?.isRunning == true) {
            mCountdown?.stop()
        }
        mTDialog = TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dalog_google_code_layout)
            .setScreenWidthAspect(context, 1.0f)
            .setGravity(Gravity.BOTTOM)
            .setDimAmount(0.8f)
            .setCancelableOutside(false)
            .setDialogAnimationRes(R.style.animate_dialog)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {
                    val etCode = getView<VerificationCodeEditText>(R.id.et_code)
                    val btCommit = getView<CommonlyUsedButton>(R.id.bt_commit)

                    initCodeListener(etCode, btCommit)

                    btCommit.listener = object : CommonlyUsedButton.OnBottomListener {
                        override fun bottomOnClick() {
                            btCommit.showLoading()
                            btnListener.doConfirm(etCode.text.toString())
                        }

                    }
                }
            }
            .addOnClickListener(R.id.iv_close, R.id.bt_commit)
            .setOnDismissListener {
                mCountdown?.stop()
                mTDialog = null
                codeListener?.doCancel()
            }
            .setOnViewClickListener { viewHolder, view, tDialog ->
                when (view.id) {
                    R.id.iv_close -> {
                        tDialog.dismiss()
                    }
                }
            }
            .create().show()

        return mTDialog
    }

    private fun initCodeListener(
        etCode: VerificationCodeEditText,
        btCommit: CommonlyUsedButton
    ) {
        btCommit.isEnable(false)
        etCode.setOnVerificationCodeChangedListener(object :
            VerificationAction.OnVerificationCodeChangedListener {
            override fun onInputCompleted(s: CharSequence?) {
            }

            override fun onVerCodeChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                btCommit.isEnable(s.toString().length == 6)
            }

        })
    }

    private fun initCountDown(tvSendCode: TextView, context: Context) {
        mCountdown = Countdown(
            tvSendCode,
            context.getString(R.string.str_count_down_time),
            60
        )
        mCountdown?.setCountdownListener(object : Countdown.CountdownListener {
            @SuppressLint("NewApi")
            override fun onFinish() {
                tvSendCode.isEnabled = true
                tvSendCode.setTextColor(context.getColor(R.color.main_yellow))
            }

            override fun onUpdate(currentRemainingSeconds: Int) {
            }

            @SuppressLint("NewApi")
            override fun onStart() {
                tvSendCode.isEnabled = false
                tvSendCode.setTextColor(context.getColor(R.color.hint_color))
            }

        })
        mCountdown?.start()
    }

    interface DialogConfirmListener {
        fun doConfirm(code: String)
    }

    interface GraphicCodeListener {
        fun doVerifySuccess(validate: String)
        fun doVerifyFail(msg: String)
    }


    interface VerifyCodeListener{
        /**
         * 重新发送
         */
        fun reSendCode()
        fun doCancel()
        fun sendFail(msg:String)
        fun doCommit(code:String)
    }


    interface VerifyCodeNoDialogListener{
        fun sendFail(msg:String)
    }

    /**
     * 发送验证码
     */
    fun sendVerifyCode(baseActivity: BaseEasyActivity, api: SendVerifyCodeApi,codeListener:VerifyCodeListener) {
        this.baseActivity = baseActivity
        this.codeListener = codeListener
        requestApi = api
        EasyHttp.post(baseActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(baseActivity) {
                override fun onSucceed(result: HttpData<String>) {
                    //打开验证码对话框
                    if(mTDialog == null){
                        openVerifyCodeDialog(baseActivity)
                    }else{
                        if (mCountdown?.isRunning == false) {
                            mCountdown?.start()
                        }
                    }
                }

                override fun onFail(e: Exception) {
                    super.onFail(e)
                    codeListener.sendFail(e.message?:"发送失败")
                }
            })
    }

    /**
     * 发送验证码 无弹窗
     */
    fun  sendVerifyCodeNoDialog(baseActivity: BaseEasyActivity, api: SendVerifyCodeApi,tvSendCode: TextView,codeListener:VerifyCodeNoDialogListener){
        this.baseActivity = baseActivity
        this.codeNoDialogListener = codeListener
        requestApi = api
        EasyHttp.post(baseActivity)
            .api(api)
            .request(object : HttpCallback<HttpData<String>>(baseActivity) {
                override fun onSucceed(result: HttpData<String>) {
                    initCountDown(tvSendCode,baseActivity)
                }

                override fun onFail(e: Exception) {
                    super.onFail(e)
                    codeListener.sendFail(e.message?:"发送失败")
                }
            })
    }

    companion object {
        const val VERIFY_TYPE_ACTIVE = "ActiveVerifyCode"
        const val VERIFY_TYPE_RESET_PWD = "ResetPasswordVerifyCode"
        const val VERIFY_TYPE_REGISTER = "RegisterVerifyCode"
        const val VERIFY_TYPE_WITHDRAW = "WithdrawVerifyCode"
        const val VERIFY_TYPE_BIND_EMAIL = "BindEmailVerifyCode"
        const val VERIFY_TYPE_BIND_PHONE = "BindPhoneVerifyCode"
        const val VERIFY_TYPE_RESET_FUND_PWD = "ResetAssetPasswordVerifyCode"
        const val VERIFY_TYPE_OTC_ACCOUNT = "OTCAccountVerifyCode"

        /**
         * 是否需要校验图形验证码
         */
        fun doGraphicCodeCheckApi(
            action: String,
            baseActivity: BaseEasyActivity,
            listener: GraphicCodeListener
        ) {
            EasyHttp.get(baseActivity)
                .api(
                    GraphicCodeCheckApi()
                        .setAction(action)
                )
                .request(object : HttpCallback<HttpData<String>>(baseActivity) {
                    override fun onSucceed(result: HttpData<String>) {
                        val need = result.getBooleanDataByKey("need")
                        if (need) {//如果需要图形验证码
                            doRealGraphCodeVerify(baseActivity, listener)
                        } else {
                            listener.doVerifySuccess("")
                        }
                    }
                })
        }

        /**
         * 进行图片验证
         */
        private fun doRealGraphCodeVerify(
            baseActivity: BaseActivity,
            listener: GraphicCodeListener
        ) {
            LogicCaptcha.getInstance().start(
                baseActivity
            ) { succeed, validate ->
                if (succeed) {
                    ToastUtil.shortToast(
                        baseActivity,
                        baseActivity.getString(R.string.str_captcha_verify_succeed)
                    )
                    listener.doVerifySuccess(validate)
                } else {
                    listener.doVerifyFail(validate)
                }
            }
        }
    }
}