package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import com.hjq.http.EasyLog
import com.sl.bymex.R
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.widget.gesture.LockPatternUtil
import com.sl.bymex.widget.gesture.LockPatternView
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_gesture_setting.*
import org.jetbrains.anko.textColor
import java.util.*

/**
 * 手势设置
 */
class GestureSettingActivity:BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_gesture_setting
    }

    private var mChosenPattern: List<LockPatternView.Cell>? = null
    private val mPatternListener  = object:LockPatternView.OnPatternListener{
        override fun onPatternStart() {
            lpv_gesture.removePostClearPatternRunnable()
            lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT);
        }

        override fun onPatternComplete(pattern: MutableList<LockPatternView.Cell>) {
            if (mChosenPattern == null && pattern.size >= 4) {
                mChosenPattern = ArrayList(pattern)
                updateStatus(Status.CORRECT, pattern)
            } else if (mChosenPattern == null && pattern.size < 4) {
                updateStatus(Status.LESSERROR, pattern)
            }else  if (mChosenPattern != null) {
                if (mChosenPattern == pattern) {
                    updateStatus(Status.CONFIRMCORRECT, pattern)
                } else {
                    updateStatus(Status.CONFIRMERROR, pattern)
                }
            }
        }

    }

    override fun loadData() {
    }

    override fun initView() {
        lpv_gesture.setOnPatternListener(mPatternListener)
    }
    private fun updateStatus(status:Status ,pattern: List<LockPatternView.Cell> ) {
        tv_gesture_info.textColor = resources.getColor(status.colorId)
        tv_gesture_info.setText(status.strId)

        when(status){
            Status.DEFAULT ->{
                lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
            }
            Status.CORRECT ->{
                updateLockPatternIndicator()
                lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
            }
            Status.LESSERROR ->{
                lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
            }
            Status.CONFIRMERROR ->{
                lpv_gesture.setPattern(LockPatternView.DisplayMode.ERROR)
                lpv_gesture.postClearPatternRunnable(500)
            }
            Status.CONFIRMCORRECT ->{
                //保存手势
                saveChosenPattern(pattern)
                ToastUtil.shortToast(
                    this@GestureSettingActivity,
                    getString(R.string.str_create_gesture_confirm_correct)
                )
                lpv_gesture.setPattern(LockPatternView.DisplayMode.DEFAULT)
            }
        }
    }

    private fun updateLockPatternIndicator() {
        if (mChosenPattern == null) return

        lpi_indicator.setIndicator(mChosenPattern)
    }

    /**
     * 保持手势密码
     */
    private fun saveChosenPattern(cells: List<LockPatternView.Cell>) {
        val bytes: ByteArray = LockPatternUtil.patternToHash(cells)
        val gesturePwd =    String(bytes, Charsets.ISO_8859_1)
        LoginHelper.openGestureLogin = true
        LoginHelper.gesturePwd = gesturePwd
       // EasyLog.print("gesturePwd:$gesturePwd")
        EventBusUtil.post(MessageEvent(MessageEvent.account_info_change_notify))
        this@GestureSettingActivity.finish()
    }

    private enum class Status( val strId: Int,  val colorId: Int) {
        //默认的状态，刚开始的时候（初始化状态）
        DEFAULT(R.string.str_create_gesture_default, R.color.text_color),  //第一次记录成功
        CORRECT(
            R.string.str_create_gesture_correct,
            R.color.text_color
        ),  //连接的点数小于4（二次确认的时候就不再提示连接的点数小于4，而是提示确认错误）
        LESSERROR(R.string.str_create_gesture_less_error, R.color.main_red),  //二次确认错误
        CONFIRMERROR(R.string.str_create_gesture_confirm_error, R.color.main_red),  //二次确认正确
        CONFIRMCORRECT(R.string.str_create_gesture_confirm_correct, R.color.text_color);

    }

    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,GestureSettingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}