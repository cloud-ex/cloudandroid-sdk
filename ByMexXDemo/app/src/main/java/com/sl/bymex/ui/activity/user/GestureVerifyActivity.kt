package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.hjq.http.EasyLog
import com.sl.bymex.R
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.widget.gesture.LockPatternUtil
import com.sl.bymex.widget.gesture.LockPatternView
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_gesture_verify.*

/**
 * 手势验证
 */
class GestureVerifyActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_gesture_verify
    }

    private val mPatternListener  = object: LockPatternView.OnPatternListener{
        override fun onPatternStart() {
            lpv_gesture.removePostClearPatternRunnable()
            lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT);
        }

        override fun onPatternComplete(pattern: MutableList<LockPatternView.Cell>) {
            val localPwd  = LoginHelper.gesturePwd
            EasyLog.print("localPwd:$localPwd"+"="+localPwd.toByteArray(Charsets.ISO_8859_1))
            if (pattern != null && !TextUtils.isEmpty(localPwd)){
                if( LockPatternUtil.checkPattern(pattern, localPwd.toByteArray(Charsets.ISO_8859_1))){
                    updateStatus(Status.CORRECT)
                }else{
                    updateStatus(Status.ERROR)
                }
            }
        }

    }


    override fun loadData() {
    }

    override fun initView() {
        lpv_gesture.setOnPatternListener(mPatternListener)
    }


    private fun updateStatus(status: Status) {
        tv_gesture_info.setText(status.strId)
        tv_gesture_info.setTextColor(resources.getColor(status.colorId))
        when (status) {
            Status.DEFAULT -> lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
            Status.ERROR -> {
                lpv_gesture.setPattern(LockPatternView.DisplayMode.ERROR)
                lpv_gesture.postClearPatternRunnable(500)
            }
            Status.CORRECT -> {
                lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
                ToastUtil.shortToast(this@GestureVerifyActivity, getString(R.string.str_gesture_correct))
                onVerifyCorrect()
            }
        }
    }
    private fun onVerifyCorrect() {
        //登录方式设置为普通
        LoginHelper.openGestureLogin = false
        EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
        finish()
    }

    private enum class Status( val strId: Int,  val colorId: Int) {
        //默认的状态
        DEFAULT(R.string.str_gesture_default, R.color.text_color),  //密码输入错误
        ERROR(R.string.str_gesture_error, R.color.main_red),  //密码输入正确
        CORRECT(R.string.str_gesture_correct, R.color.text_color);

    }

    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,GestureVerifyActivity::class.java)
            activity.startActivity(intent)
        }
    }
}