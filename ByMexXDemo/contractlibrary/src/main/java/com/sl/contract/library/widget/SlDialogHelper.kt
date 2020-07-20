package com.sl.contract.library.widget

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.contract.sdk.ContractSDKAgent
import com.sl.contract.library.R
import com.sl.contract.library.activity.ContractEntrustActivity
import com.sl.contract.library.activity.ContractPositionActivity
import com.sl.contract.library.activity.ContractSettingActivity
import com.sl.contract.library.activity.FragmentWarpActivity
import com.sl.contract.library.data.FragmentType
import com.sl.contract.library.helper.ContractService
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.utils.setDrawableLeft
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity

object SlDialogHelper {
    /**
     * 打开合约设置弹窗
     *
     */
    fun openContractSetting(context: Context?, targetView: View, contractId: Int,callBack: (tabIndex: Int) -> Unit) {
        val cvcEasyPopup =
            EasyPopup.create().setContentView(context, R.layout.sl_view_dropdown_contract_menu)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
        cvcEasyPopup?.run {
            val llQuickMode = findViewById<View>(R.id.ll_quick_mode)
            val llEntrustInfo =  findViewById<View>(R.id.ll_entrust_info)
            val ll_position_info = findViewById<View>(R.id.ll_position_info)
            var mode = LogicContractSetting.getContractModeSetting(context)
            // 0 专业模式 1 闪电模式
            if (mode == 0) {
                llQuickMode.visibility = View.VISIBLE
                llEntrustInfo.visibility = View.GONE
               // ll_position_info.visibility = View.GONE
            } else {
                //闪电
                llQuickMode.visibility = View.GONE
                llEntrustInfo.visibility = View.VISIBLE
                ll_position_info.visibility = View.VISIBLE

            }
            //委托信息
            llEntrustInfo.setOnClickListener {
                cvcEasyPopup.dismiss()
                if (ContractSDKAgent.isLogin) {
                    ContractEntrustActivity.show(context as Activity,contractId)
                } else {
                    ContractService.contractService?.doOpenLoginPage(context as FragmentActivity)
                }
            }
            //仓位信息
            ll_position_info.setOnClickListener {
                cvcEasyPopup.dismiss()
                if (ContractSDKAgent.isLogin) {
                    ContractPositionActivity.show(context as Activity,contractId)
                } else {
                    ContractService.contractService?.doOpenLoginPage(context as FragmentActivity)
                }
            }
            //合约信息
            findViewById<View>(R.id.ll_contract_info).setOnClickListener {
                val activity = context as Activity
                FragmentWarpActivity.show(activity, FragmentType.CONTRACT_INTRODUCE,contractId)
                cvcEasyPopup.dismiss()
            }
            //合约设置
            findViewById<View>(R.id.ll_contract_setting).setOnClickListener {
                cvcEasyPopup.dismiss()
                ContractSettingActivity.show(context as Activity)
            }
            //资金划转
            findViewById<View>(R.id.ll_funds_transfer).setOnClickListener {
                cvcEasyPopup.dismiss()
                if (ContractSDKAgent.isLogin) {
                    ContractService.contractService?.openAssetTransferActivity(
                        context as FragmentActivity,
                        "USDT"
                    )
                } else {
                    ContractService.contractService?.doOpenLoginPage(context as FragmentActivity)
                }
            }
            //闪电模式
            llQuickMode.setOnClickListener {
                cvcEasyPopup.dismiss()
                callBack.invoke(6)
            }
        }
        cvcEasyPopup?.showAtAnchorView(
            targetView,
            YGravity.ALIGN_TOP,
            XGravity.ALIGN_RIGHT,
            0,
            targetView.height
        )
    }

    /**
     * 打开交易模式切换
     */
    fun openTreadModeSwitch(context: Context?, callBack: (isQuickMode: Boolean) -> Unit): TDialog {
        var mode = LogicContractSetting.getContractModeSetting(context)
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dialog_tread_mode_layout)
            .setScreenWidthAspect(context, 1.0f)
            .setGravity(Gravity.BOTTOM)
            .setDimAmount(0.8f)
            .setCancelableOutside(true)
            .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {
                    val tvProfessionalMode = getView<TextView>(R.id.tv_professional_mode)
                    val tvQuickMode = getView<TextView>(R.id.tv_quick_mode)

                    if (mode == 0) {
                        tvProfessionalMode.setDrawableLeft(R.drawable.icon_mode_select)
                        tvQuickMode.setDrawableLeft(R.drawable.icon_mode_normal)
                    } else {
                        tvProfessionalMode.setDrawableLeft(R.drawable.icon_mode_normal)
                        tvQuickMode.setDrawableLeft(R.drawable.icon_mode_select)
                    }

                    tvProfessionalMode.setOnClickListener {
                        mode = 0
                        tvProfessionalMode.setDrawableLeft(R.drawable.icon_mode_select)
                        tvQuickMode.setDrawableLeft(R.drawable.icon_mode_normal)
                    }

                    tvQuickMode.setOnClickListener {
                        mode = 1
                        tvProfessionalMode.setDrawableLeft(R.drawable.icon_mode_normal)
                        tvQuickMode.setDrawableLeft(R.drawable.icon_mode_select)
                    }
                }
            }
            .addOnClickListener(R.id.rl_title_close, R.id.bt_sure)
            .setOnViewClickListener { viewHolder, view, tDialog ->
                when (view.id) {
                    R.id.rl_title_close -> {
                        tDialog.dismiss()
                    }
                    R.id.bt_sure -> {
                        tDialog.dismiss()
                        LogicContractSetting.setContractModeSetting(context, mode)
                        callBack.invoke(mode == 1)
                    }
                }
            }
            .create()
            .show()
    }

    /**
     * 在指定view弹出pop
     */
    fun showTipPopForView(
        context: Context?,
        targetView: View,
        text: String,
        vertGravity: Int = YGravity.BELOW,
        horizGravity: Int = XGravity.ALIGN_LEFT,
        x: Int = 0,
        y: Int = 0
    ) {
        val cvcEasyPopup =
            EasyPopup.create().setContentView(context, R.layout.dialog_text_tips_layout)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(false)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
        cvcEasyPopup?.run {
            findViewById<TextView>(R.id.tv_text).text = text
        }
        cvcEasyPopup?.showAtAnchorView(targetView, vertGravity, horizGravity,x,y)
    }

    /**
     * 在指定view弹出带Arrow的pop
     */
    fun showTipArrowPopForView(
        context: Context?,
        targetView: View,
        text: String,
        vertGravity: Int = YGravity.BELOW,
        horizGravity: Int = XGravity.ALIGN_LEFT,
        x: Int = 0,
        y: Int = 0
    ) {
        val cvcEasyPopup =
            EasyPopup.create().setContentView(context, R.layout.dialog_text_arrow_tips_layout)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(false)
                .setWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                .setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply()
        cvcEasyPopup?.run {
            //合约设置
            findViewById<TextView>(R.id.tv_text).text = text
        }
        cvcEasyPopup?.showAtAnchorView(targetView, vertGravity, horizGravity,x,y)
    }
}