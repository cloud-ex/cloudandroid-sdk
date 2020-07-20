package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.GlobalLeverage
import com.contract.sdk.impl.IResponse
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.contract.library.R
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.finishActivityDialogAnim
import com.sl.ui.library.utils.startActivityDialogAnim
import com.sl.ui.library.widget.bubble.BubbleSeekBar
import kotlinx.android.synthetic.main.activity_select_lever_layout.*
import kotlinx.android.synthetic.main.activity_select_lever_layout.tab_layout

/**
 * 选择杠杆
 */
class SelectLeverageActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_select_lever_layout
    }

    private var tabTextList = arrayOfNulls<String>(2)
    private var tabIndex = 0

    private var contract: Contract? = null
    private var selectLeverage = 10 // 选择杠杆   //全仓 杠杆为0
    private var leverageType = 1
    private var minLeverage = 1
    private var maxLeverage = 100

    private var contractId = 0

    override fun loadData() {
        contractId = intent.getIntExtra("contactId", 0)
        contract = ContractPublicDataAgent.getContract(contractId)
        if (contract == null) {
            finish()
            return
        }
        selectLeverage = intent.getIntExtra("selectLeverage", 10)
        leverageType = ContractSettingUtils.getLeverageType(this@SelectLeverageActivity, contractId)
        minLeverage = contract!!.min_leverage.toInt()
        maxLeverage = contract!!.max_leverage.toInt()
        tabTextList[0] = getString(R.string.sl_str_gradually_position)
        tabTextList[1] = getString(R.string.sl_str_full_position)
    }

    override fun initView() {
        iv_close.setOnClickListener(this)
        bt_sure.setOnClickListener(this)
        initTabView()
        initSeekBarUi()

        doTabSwitchUI()
        fullLeverUi()
    }

    private fun initSeekBarUi() {
        sb_seek_layout.configBuilder
            .min(minLeverage.toFloat())
            .max(maxLeverage.toFloat())
            .progress(selectLeverage.toFloat())
            .build()
        sb_seek_layout.onProgressChangedListener =
            object : BubbleSeekBar.OnProgressChangedListener {
                override fun onProgressChanged(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float
                ) {
                    selectLeverage = progress
                    fullLeverUi()
                }

                override fun getProgressOnActionUp(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float
                ) {
                }

                override fun getProgressOnFinally(
                    bubbleSeekBar: BubbleSeekBar?,
                    progress: Int,
                    progressFloat: Float
                ) {
                }

            }
    }

    private fun fullLeverUi() {
        if (tabIndex == 0) {
            tv_lever.text = "$selectLeverage" + "X"
            //选择杠杆超过一半提示警告
            if (selectLeverage >= maxLeverage / 2) {
                tv_leverage_warn.visibility = View.VISIBLE
            } else {
                tv_leverage_warn.visibility = View.INVISIBLE
            }
        } else {
            tv_lever.text = "$maxLeverage" + "X"
        }
    }

    private fun initTabView() {
        tab_layout.setTabData(tabTextList)
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                tabIndex = index
                doTabSwitchUI()
                fullLeverUi()
            }

            override fun onTabReselect(p0: Int) {
            }

        })
        if (leverageType == 2) {
            tabIndex = 1
            tab_layout.currentTab = tabIndex
        }
    }

    private fun doTabSwitchUI() {
        when (tabIndex) {
            0 -> {
                //逐仓
                tv_leverage_warn.text = getString(R.string.sl_select_lever_warn)
            }
            1 -> {
                tv_leverage_warn.visibility = View.VISIBLE
                //全仓
                tv_leverage_warn.text = getString(R.string.str_full_lever_warn)
            }
        }
        fullLeverUi()
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.iv_close -> {
                finishActivityDialogAnim()
            }
            R.id.bt_sure -> {
                leverageType = if(tabIndex == 0){
                    1
                }else{
                    2
                }
                showLoadingDialog()
                //保存全局杠杆
                ContractUserDataAgent.setGlobalLeverage(contractId, selectLeverage, leverageType,object :
                    IResponse<MutableList<GlobalLeverage>>(){
                    override fun onSuccess(data: MutableList<GlobalLeverage>) {
                        hideLoadingDialog()
                        ContractSettingUtils.setLeverage(this@SelectLeverageActivity, contractId,selectLeverage,leverageType)
                        finishActivityDialogAnim()
                    }

                    override fun onFail(code: String, msg: String) {
                         hideLoadingDialog()
                        ToastUtil.shortToast(this@SelectLeverageActivity,msg)
                    }
                })
            }
        }

    }


    companion object {
        fun show(activity: Activity, contactId: Int, selectLeverage: Int = 10) {
            val intent = Intent(activity, SelectLeverageActivity::class.java)
            intent.apply {
                putExtra("contactId", contactId)
                putExtra("selectLeverage", selectLeverage)
            }
            activity.startActivityDialogAnim(intent)
        }
    }
}