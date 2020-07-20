package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.impl.ContractPlanOrderListener
import com.contract.sdk.utils.SDKLogUtil
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.contract.library.R
import com.sl.contract.library.fragment.ContractPlanEntrustFragment
import com.sl.contract.library.fragment.ContractPriceEntrustFragment
import com.sl.contract.library.widget.ContractOrderFilterDialog
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.MessageEvent
import kotlinx.android.synthetic.main.activity_contract_entrust.*

class ContractEntrustActivity:BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_contract_entrust
    }

    private var mContract: Contract?=null
    private var fragmentList = ArrayList<Fragment>()
    private var isCurrentEntrust = true
    //0 普通委托 1计划委托
    private var entrustIndex = 0

    private var priceEntrustFragment : ContractPriceEntrustFragment?=null
    private var planEntrustFragment : ContractPlanEntrustFragment?=null

    private var statusId = 0
    private var directionTabInfo : TabInfo? = null

    override fun loadData() {
        val  contractId = intent.getIntExtra("contractId",0)
        mContract = ContractPublicDataAgent.getContract(contractId)
        if(mContract == null){
            finish()
            return
        }

    }

    override fun initView() {
        iv_left.setOnClickListener(this)
        iv_filter.setOnClickListener(this)
        initTitleTabLayout()
        initTabLayout()
        priceEntrustFragment?.bindContract(mContract!!)
        planEntrustFragment?.bindContract(mContract!!)
    }
    private fun initTitleTabLayout() {
        var tabTextList = arrayOfNulls<String>(2)
        tabTextList[0] = getString(R.string.str_current_entrust)
        tabTextList[1] = getString(R.string.str_history_entrust)
        title_tab_layout.setTabData(tabTextList)
        title_tab_layout.setOnTabSelectListener(object: OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                isCurrentEntrust = index == 0
                priceEntrustFragment?.bindEntrustType(isCurrentEntrust)
                planEntrustFragment?.bindEntrustType(isCurrentEntrust)
            }

            override fun onTabReselect(p0: Int) {
            }

        })
    }

    private fun initTabLayout() {
        var tabTextList = arrayOfNulls<String>(2)
        tabTextList[0] = getString(R.string.str_limit_entrust)
        tabTextList[1] = getString(R.string.str_plan_entrust)

        priceEntrustFragment = ContractPriceEntrustFragment.newInstance(true)
        planEntrustFragment = ContractPlanEntrustFragment.newInstance(true)

        fragmentList.add(priceEntrustFragment!!)
        fragmentList.add(planEntrustFragment!!)
        tab_layout.setTabData(tabTextList,this,R.id.fl_layout,fragmentList)
    }


    override fun onClick(v: View) {
        when(v.id){
            R.id.iv_left -> {
                finish()
            }
            R.id.iv_filter -> {
                val filterDialog = ContractOrderFilterDialog()
                filterDialog.showDialog(supportFragmentManager,"dalog",statusId,directionTabInfo)
            }
        }

    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when(event.msg_type){
            MessageEvent.filter_order_type_select_notify -> {
              val array = event.msg_content as  ArrayList<Any>
                statusId = array[0] as Int
                directionTabInfo = array[1] as TabInfo

                priceEntrustFragment?.doFilter(statusId,directionTabInfo)
                planEntrustFragment?.doFilter(statusId,directionTabInfo)

            }
        }
    }

    companion object{
        fun show(activity: Activity,contractId:Int){
            val intent = Intent(activity,ContractEntrustActivity::class.java)
            intent.putExtra("contractId",contractId)
            activity.startActivity(intent)
        }
    }
}