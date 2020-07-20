package com.sl.ui.library.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.sl.bymex.service.foreground.ForegroundCallbacksListener
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.service.foreground.ForegroundCallbacksObserver
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.DialogUtils
import com.timmy.tdialog.TDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


abstract class BaseActivity : AppCompatActivity(), View.OnClickListener {
    var mActivity: FragmentActivity = this

    var layoutView: View? = null
    var inflater: LayoutInflater? = null

    var loadingDialog: TDialog? = null

    protected val mainRiseColor by lazy {
        ColorUtils.getMainColorType(this@BaseActivity, true)
    }
    protected val mainFallColor by lazy {
        ColorUtils.getMainColorType(this@BaseActivity, false)
    }

    abstract fun setContentView(): Int
    abstract fun loadData()
    abstract fun initView()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            //被系统回收，重启APP
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            finish()
            return
        }
        mActivity = this
        EventBusUtil.register(this)
        inflater = LayoutInflater.from(this)
        layoutView = inflater?.inflate(setContentView(), null)
        setContentView(layoutView)

        ForegroundCallbacksObserver.instance?.addListener(listener)
        loadData()
        initView()

    }


    var listener = object : ForegroundCallbacksListener {

        override fun foregroundListener() {
            appForeground()
        }

        override fun backgroundListener() {
            appBackground()
        }
    }


    /*
    * 处理线程跟发消息线程一致
    * 子类重载
    */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun onMessageEvent(event: MessageEvent) {
    }


    /**
     * 进入后台
     */
    open fun appBackground() {

    }

    /**
     * 进入前台
     */
    open fun appForeground() {

    }

    override fun onClick(v: View) {
    }


    fun showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = DialogUtils.showLoadingDialog(this@BaseActivity)
        } else {
            loadingDialog?.show()
        }
    }

    fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }


    override fun onDestroy() {
        super.onDestroy()
        ForegroundCallbacksObserver.instance?.removeListener(listener)
        EventBusUtil.unregister(this)

        if (loadingDialog != null) {
            hideLoadingDialog()
        }
    }


}