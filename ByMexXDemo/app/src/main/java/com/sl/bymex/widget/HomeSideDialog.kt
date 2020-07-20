package com.sl.bymex.widget

import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.sl.bymex.R
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.ui.activity.asset.AssetTransferActivity
import com.sl.bymex.ui.activity.user.PersonaInfoActivity
import com.sl.bymex.ui.activity.user.SecuritySettingActivity
import com.sl.bymex.ui.activity.user.SettingActivity
import com.sl.bymex.utils.AppUtils
import com.sl.ui.library.base.BaseDialogFragment
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.setDrawableRight
import kotlinx.android.synthetic.main.view_home_left_layout.*

/**
 * 首页侧边栏
 */
class HomeSideDialog : BaseDialogFragment() {
    override fun setContentView(): Int {
        return R.layout.view_home_left_layout
    }

    override fun initView() {
        alpha_ll.setOnClickListener(this)
        item_asset_transfer.setOnClickListener(this)
        item_account_security.setOnClickListener(this)
        item_setting.setOnClickListener(this)
        rl_header_warp_layout.setOnClickListener(this)
        updateLoginUI()
    }

    override fun loadData() {
    }

    private fun updateLoginUI() {
        if (UserHelper.isLogin()) {
            tv_nick_name.text =
                String.format(getString(R.string.str_say_hi), UserHelper.user.showName)
            tv_uid.text = UserHelper.user.account_id.toString()
            tv_uid.setDrawableRight(R.drawable.icon_copy)
        } else {
            tv_nick_name.text = getString(R.string.str_login)
            tv_uid.text = getString(R.string.str_welcome_login)
            tv_uid.setDrawableRight(0)
        }
    }

    override fun onMessageEvent(event: MessageEvent) {
        when (event.msg_type) {
            MessageEvent.sl_login_token_change_event,MessageEvent.nick_name_change_notify -> {
                updateLoginUI()
            }
        }
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when(v.id){
            R.id.rl_header_warp_layout ->{
                if (UserHelper.isLogin()) {
                    PersonaInfoActivity.show(activity!!)
                }else{
                    LoginHelper.openLogin(activity!!)
                }
            }
            R.id.item_asset_transfer ->{
                //资金划转
                if (UserHelper.isLogin()) {
                    AssetTransferActivity.show(activity!!)
                }else{
                    LoginHelper.openLogin(activity!!)
                }
            }
            R.id.item_account_security ->{
                //账户安全
                if (UserHelper.isLogin()) {
                    SecuritySettingActivity.show(activity!!)
                }else{
                    LoginHelper.openLogin(activity!!)
                }
            }
            R.id.item_setting ->{
                //个人设置
                SettingActivity.show(activity!!)
            }
        }
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftin_rightout_DialogFg_style)
    }


    public override fun showDialog(manager: FragmentManager, tag: String) {
        super.showDialog(manager, tag)
    }

}