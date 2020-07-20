package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.PreferenceManager
import com.timmy.tdialog.TDialog
import kotlinx.android.synthetic.main.activity_contract_setting.*

/**
 * 合约设置
 */
class ContractSettingActivity:BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_contract_setting
    }


    //仓位展示单位
    private val unitList = ArrayList<TabInfo>()
    private var currUnitInfo: TabInfo? = null
    private var unitDialog: TDialog? = null
    private var originUnitIndex:Int? = 0
    //未实现盈亏
    private val pnlList = ArrayList<TabInfo>()
    private var currPnlInfo: TabInfo? = null
    private var pnlDialog: TDialog? = null
    private var originPnlIndex:Int? = 0
    //有效时间
    private val timeList = ArrayList<TabInfo>()
    private var currTimeInfo: TabInfo? = null
    private var timeDialog: TDialog? = null
    //下单二次确认
    private var tradeConfirm = true

    override fun loadData() {
        unitList.add(TabInfo(getString(R.string.str_vol_unit), 0))
        unitList.add(TabInfo(getString(R.string.str_coin_unit), 1))
        currUnitInfo = findTabInfo(unitList, LogicContractSetting.getContractUint(mActivity))
        originUnitIndex = currUnitInfo?.index

        pnlList.add(TabInfo(getString(R.string.str_fair_price), 0))
        pnlList.add(TabInfo(getString(R.string.str_latest_price), 1))
        currPnlInfo = findTabInfo(pnlList, LogicContractSetting.getPnlCalculate(mActivity))
        originPnlIndex = currPnlInfo?.index

        timeList.add(TabInfo(getString(R.string.str_in_24_hours), 0))
        timeList.add(TabInfo(getString(R.string.str_in_7_days), 1))
        currTimeInfo = findTabInfo(timeList, LogicContractSetting.getStrategyEffectTime(mActivity))

        tradeConfirm =  PreferenceManager.getInstance(application).getSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, true)
    }

    override fun initView() {
        initListener()
        tv_contracts_unit_value.text = currUnitInfo?.name
        tv_pnl_calculator_value.text = currPnlInfo?.name
        tv_effective_time_value.text = currTimeInfo?.name
    }

    private fun initListener() {
        //有效时间
        rl_effective_time_layout.setOnClickListener {
            timeDialog = DialogUtils.showListDialog(mActivity, timeList, currTimeInfo!!.index, object : DialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currTimeInfo = timeList[index]
                    timeDialog?.dismiss()
                    tv_effective_time_value.text = currTimeInfo?.name
                    LogicContractSetting.setStrategyEffectTime(mActivity, currTimeInfo!!.index)
                }
            })
        }
        //仓位展示单位
        rl_display_unit_layout.setOnClickListener {
            unitDialog = DialogUtils.showListDialog(mActivity, unitList, currUnitInfo!!.index, object : DialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currUnitInfo = unitList[index]
                    unitDialog?.dismiss()
                    tv_contracts_unit_value.text = currUnitInfo?.name
                    LogicContractSetting.setContractUint(mActivity, currUnitInfo!!.index)
                }
            })
        }
        //未实现盈亏tv_price_hint
        rl_pnl_calculator_layout.setOnClickListener {
            pnlDialog = DialogUtils.showListDialog(mActivity, pnlList, currPnlInfo!!.index, object : DialogUtils.DialogOnItemClickListener {
                override fun clickItem(index: Int) {
                    currPnlInfo = pnlList[index]
                    pnlDialog?.dismiss()
                    tv_pnl_calculator_value.text = currPnlInfo?.name
                    LogicContractSetting.setPnlCalculate(mActivity, currPnlInfo!!.index)
                }
            })
        }
        //下单二次确认
        switch_book_again.setOnCheckedChangeListener { _, isChecked ->
            PreferenceManager.getInstance(application).putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, isChecked)
            switch_book_again.isChecked = isChecked
        }
        switch_book_again.isChecked = tradeConfirm
    }



    override fun finish() {
        super.finish()
        if (currUnitInfo?.index != originUnitIndex  || currPnlInfo?.index  != originPnlIndex) {
            LogicContractSetting.getInstance().refresh()
        }
    }
    private fun findTabInfo(list: ArrayList<TabInfo>, index: Int = 0): TabInfo {
        for (i in list.indices) {
            if (list[i].index == index) {
                return list[i]
            }
        }
        return list[0]
    }
    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity, ContractSettingActivity::class.java)
            activity.startActivity(intent)
        }
    }
}