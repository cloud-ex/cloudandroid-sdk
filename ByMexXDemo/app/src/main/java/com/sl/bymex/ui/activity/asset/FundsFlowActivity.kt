package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.bymex.R
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.ui.fragment.asset.SpotFundsFlowFragment
import com.sl.bymex.utils.LogUtil
import com.sl.bymex.widget.SpotFundsFlowDialog
import com.sl.contract.library.fragment.ContractFundsFlowFragment
import com.sl.contract.library.widget.ContractFundsFlowDialog
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.MessageEvent
import kotlinx.android.synthetic.main.activity_dw_record.*


/**
 * 资金流水
 */
class FundsFlowActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_funds_flow
    }

    /**
     * 0 钱包账户 1 合约账户
     */
    private var tabIndex = 0
    private var tabTextList = arrayOfNulls<String>(2)

    private var spotAssetFragment = SpotFundsFlowFragment.newInstance()
    private var contractAssetFragment = ContractFundsFlowFragment.newInstance()
    private var currFragment = Fragment()


    /**
     * 现货 筛选类型/币种
     */
    private var spotFilterType: TabInfo? = null
    private var spotCoin = ""

    /**
     * 合约 筛选类型/币种
     */
    private var contractFilterType: TabInfo? = null
    private var contractCoin = ""

    override fun loadData() {
        tabTextList[0] = getString(R.string.str_wallet_account)
        tabTextList[1] = getString(R.string.str_contract_account)

        spotFilterType = TabInfo(getString(R.string.str_all), 0)
        contractFilterType = TabInfo(getString(R.string.str_all), 0)
    }


    override fun initView() {
        tab_layout.setTabData(tabTextList)
        showFragment()
        initListener()
    }


    private fun initListener() {
        /**
         * 筛选
         */
        title_layout.setRightOnClickListener(View.OnClickListener {
            if (tabIndex == 0) {
                val spotFilterDialog = SpotFundsFlowDialog()
                spotFilterDialog.showDialog(supportFragmentManager, "filter", spotCoin, spotFilterType)
            } else {
                val contractFilterDialog = ContractFundsFlowDialog()
                contractFilterDialog.showDialog(supportFragmentManager, "filter", contractCoin, contractFilterType)
            }
        })
        /**
         *  切换类型
         */
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                if (tabIndex != index) {
                    tabIndex = index
                    showFragment()
                }
            }

            override fun onTabReselect(p0: Int) {
            }

        })

    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.filter_coin_type_select_notify -> {
                val filter = event.msg_content as ArrayList<Any>
                if(tabIndex == 1){
                    spotCoin = filter[0] as String
                    spotFilterType = filter[1] as TabInfo
                }else{
                    contractCoin = filter[0] as String
                    contractFilterType = filter[1] as TabInfo
                }

                LogUtil.d("DEBUG", "筛选:coin:" + spotCoin + ";filterType:" + spotFilterType!!.name)
            }
        }
    }

    private fun showFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        currFragment = if (tabIndex == 0) {
            if (!spotAssetFragment.isAdded) {
                transaction.hide(currFragment).add(R.id.fl_asset_layout, spotAssetFragment)
            } else {
                transaction.hide(currFragment).show(spotAssetFragment)
            }
            spotAssetFragment
        } else {
            if (!contractAssetFragment.isAdded) {
                transaction.hide(currFragment).add(R.id.fl_asset_layout, contractAssetFragment)
            } else {
                transaction.hide(currFragment).show(contractAssetFragment)
            }
            contractAssetFragment
        }
        transaction.commitNowAllowingStateLoss()
    }


    companion object {
        fun show(activity: Activity) {
            val intent = Intent(activity, FundsFlowActivity::class.java)
            activity.startActivity(intent)
        }
    }
}