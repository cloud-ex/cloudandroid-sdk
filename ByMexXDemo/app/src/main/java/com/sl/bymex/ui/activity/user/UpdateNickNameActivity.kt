package com.sl.bymex.ui.activity.user

import android.app.Activity
import android.content.Intent
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.NickNameApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.delayFinish
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_update_nick_name.*
import java.lang.Exception

/**
 * 修改昵称
 */
class UpdateNickNameActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_update_nick_name
    }

    var nickname : String?=""

    override fun loadData() {
        nickname = UserHelper.user.account_name
    }

    override fun initView() {
        item_nick_layout.inputText = nickname?:""
        item_nick_layout.setEditTextChangedListener(object :CommonInputLayout.EditTextChangedListener{
            override fun onTextChanged(view: CommonInputLayout) {
                bt_save.isEnable(item_nick_layout.inputText.isNotEmpty())
            }

        })

        bt_save.isEnable(false)
        bt_save.listener = object: CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                bt_save.showLoading()
                val nickApi = NickNameApi()
                nickApi.account_name = item_nick_layout.inputText
                NetHelper.doHttpPost(this@UpdateNickNameActivity,nickApi,object:HttpCallback<HttpData<String>>(this@UpdateNickNameActivity){
                    override fun onFail(e: Exception?) {
                        bt_save.hideLoading()
                        TopToastUtils.showFail(this@UpdateNickNameActivity,getString(R.string.str_setting_fail))
                    }

                    override fun onSucceed(result: HttpData<String>?) {
                        TopToastUtils.showSuccess(this@UpdateNickNameActivity,getString(R.string.str_setting_success))
                        UserHelper.user.account_name = item_nick_layout.inputText
                        //发出昵称修改成功通知
                        EventBusUtil.post(MessageEvent(MessageEvent.nick_name_change_notify))
                        this@UpdateNickNameActivity.delayFinish(bt_save,1500)
                    }
                })
            }

        }
    }

    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,UpdateNickNameActivity::class.java)
            activity.startActivity(intent)
        }
    }
}