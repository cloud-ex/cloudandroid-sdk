package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.GoogleCodeApi
import com.sl.bymex.api.PublicApiConstant
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.utils.QRCodeUtil
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.service.vcedittext.VerificationAction
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.delayFinish
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_bind_google.*
import kotlinx.android.synthetic.main.activity_bind_google.bt_continue

/**
 * 绑定谷歌验证码
 */
class BindGoogleActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_bind_google
    }

    var gaKey = ""
    var loginName = ""

    override fun loadData() {
        loadGoogleCode()
    }

    override fun initView() {
       val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        /**
         * 拷贝
         */
        tv_copy.setOnClickListener {
            val clipData = ClipData.newPlainText("Address", tv_qr_text.text)
            clipboardManager.setPrimaryClip(clipData)
            ToastUtil.shortToast(this@BindGoogleActivity,getString(R.string.str_copy_succeed))
        }
        /**
         * 输入谷歌验证码
         */
        et_input.setOnVerificationCodeChangedListener(object:
            VerificationAction.OnVerificationCodeChangedListener{
            override fun onInputCompleted(s: CharSequence?) {
                bt_continue.isEnable(true)
            }

            override fun onVerCodeChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(count < 6){
                    bt_continue.isEnable(false)
                }
            }

        })
        bt_continue.isEnable(false)
        bt_continue.listener = object: CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                bt_continue.showLoading()
                val googleApi = GoogleCodeApi(PublicApiConstant.sGoogleCodeAdd)
                googleApi.ga_code = et_input.text.toString().toLong()
                googleApi.ga_key = gaKey
                googleApi.login_name = loginName

                NetHelper.doHttpPost(this@BindGoogleActivity,googleApi,object : HttpCallback<HttpData<String>>(this@BindGoogleActivity){
                    override fun onFail(e: Exception) {
                        bt_continue.hideLoading()
                        TopToastUtils.showTopSuccessToast(this@BindGoogleActivity, e.message)
                    }

                    override fun onSucceed(result: HttpData<String>) {
                        TopToastUtils.showSuccess(this@BindGoogleActivity,getString(R.string.str_bind_success))
                        UserHelper.user.ga_key = "bind"
                        EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
                        this@BindGoogleActivity.delayFinish(bt_continue,1500)
                    }
                })
            }

        }
    }

    private fun loadGoogleCode() {
        val googleApi = GoogleCodeApi(PublicApiConstant.sGoogleCodeQuery)
        NetHelper.doHttpPost(
            this@BindGoogleActivity,
            googleApi,
            object : HttpCallback<HttpData<String>>(this@BindGoogleActivity) {
                override fun onFail(e: Exception) {
                    TopToastUtils.showTopSuccessToast(this@BindGoogleActivity, e.message)
                }

                override fun onSucceed(result: HttpData<String>) {
                     gaKey = result.getStringDataByKey("ga_key")
                     loginName = result.getStringDataByKey("login_name")

                    tv_qr_text.text = gaKey

                    val qrCode = "otpauth://totp/" + loginName.replace(" ", "")+ "?secret=" + gaKey + "&issuer=byMex"
                    val bitmap: Bitmap? = QRCodeUtil.createQRCodeBitmap(qrCode, 480, 480)
                    iv_qr_code.setImageBitmap(bitmap);

                }
            })

    }

    companion object {
        fun show(activity: Activity) {
            val intent = Intent(activity, BindGoogleActivity::class.java)
            activity.startActivity(intent)
        }
    }
}