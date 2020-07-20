package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.impl.ContractUserStatusListener
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.contract.library.R
import com.sl.contract.library.fragment.ContractHistoryHoldFragment
import com.sl.contract.library.fragment.ContractHoldFragment
import com.sl.contract.library.helper.ContractService
import com.sl.ui.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_contract_entrust.*

/**
 * 合约仓位
 */
class ContractPositionActivity : BaseActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_contract_position
    }
    private var mContract: Contract?=null
    private var fragmentList = ArrayList<Fragment>()
    private var holdFragment = ContractHoldFragment.newInstance()
    private var holdHistoryFragment = ContractHistoryHoldFragment.newInstance()

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
        initTitleTabLayout()
        holdFragment?.bindContract(mContract!!)
        holdHistoryFragment?.bindContract(mContract!!)

        ContractSDKAgent.registerSDKUserStatusListener(this,object : ContractUserStatusListener(){
            /**
             * 合约SDK登录成功
             */
            override fun onContractLoginSuccess() {
                mContract?.let {
                    holdFragment?.bindContract(it)
                    holdHistoryFragment?.bindContract(it)
                }
            }

            /**
             * 合约SDK退出登录
             */
            override fun onContractExitLogin() {
               ContractService.contractService?.doOpenLoginPage(this@ContractPositionActivity)
            }

        })
    }

    private fun initTitleTabLayout() {
        var tabTextList = arrayOfNulls<String>(2)
        tabTextList[0] = getString(R.string.str_current_position)
        tabTextList[1] = getString(R.string.str_history_position)
        fragmentList.add(holdFragment)
        fragmentList.add(holdHistoryFragment)
        title_tab_layout.setTabData(tabTextList,this@ContractPositionActivity,R.id.fl_layout,fragmentList)
    }


    override fun onClick(v: View) {
       when(v.id){
           R.id.iv_left -> {
               finish()
           }
       }

    }

    companion object{
        fun show(activity: Activity, contractId:Int){
            val intent = Intent(activity,ContractPositionActivity::class.java)
            intent.putExtra("contractId",contractId)
            activity.startActivity(intent)
        }
    }
}