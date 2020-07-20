package com.sl.bymex.app

import com.hjq.http.listener.OnHttpListener
import com.sl.ui.library.base.BaseActivity

abstract class BaseEasyActivity : BaseActivity(), OnHttpListener<Any>
    {
    override fun onSucceed(result: Any?) {
    }

    override fun onFail(e: Exception?) {
    }


}