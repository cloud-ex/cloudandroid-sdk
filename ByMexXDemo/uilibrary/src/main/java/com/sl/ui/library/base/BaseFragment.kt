package com.sl.ui.library.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sl.ui.library.R
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.DialogUtils
import com.timmy.tdialog.TDialog
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseFragment : Fragment() {
    protected var layoutView: View? = null
    protected var mActivity: Activity? = null

    val mainColorRise: Int by lazy {
        ColorUtils.getMainColorType(context!!,true)
    }
    val mainColorDown: Int by lazy {
        ColorUtils.getMainColorType(context!!,false)
    }

    var loadingDialog: TDialog? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as Activity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (null == layoutView) {
            layoutView = inflater.inflate(setContentView(), container, false)
            loadData()
        } else {
            var viewGroup = layoutView?.parent as ViewGroup?
            viewGroup?.removeView(layoutView)
        }
        return layoutView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBusUtil.register(this)
        initView()
    }

    override fun onDestroyView() {
        hideLoadingDialog()
        super.onDestroyView()
        EventBusUtil.unregister(this)
    }


    fun showLoadingDialog(){
        mActivity?.let {
            if(loadingDialog == null){
                loadingDialog =  DialogUtils.showLoadingDialog(it)
            }else{
                loadingDialog?.show()
            }
        }
    }

    fun hideLoadingDialog(){
        loadingDialog?.let {
            if(it.isAdded && it.isVisible){
                it.dismiss()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    open fun onMessageEvent(event: MessageEvent) {
    }

    /*
    * 加载布局文件
    */
    protected abstract fun setContentView(): Int

    /*
     * 此方法处理view显示，或view绑定数据
     */
    abstract fun initView()

    /*
     * 数据请求，建议重载保持代码风格统一
     */
    open fun loadData() {}


}