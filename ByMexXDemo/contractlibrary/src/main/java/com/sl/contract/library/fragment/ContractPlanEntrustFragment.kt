package com.sl.contract.library.fragment

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.data.ContractOrders
import com.contract.sdk.impl.ContractOrderListener
import com.contract.sdk.impl.ContractPlanOrderListener
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractPlanEntrustAdapter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.fragment_sl_contract_hold.*

/**
 * 合约计划委托
 */
class ContractPlanEntrustFragment:BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_sl_contract_hold
    }

    private var entrustAdapter: ContractPlanEntrustAdapter? = null
    private var mList = ArrayList<ContractOrder>()
    private var contract: Contract? = null

    private var mLimit = 50
    private var mOffset = 0

    private var statusId = 0
    private var directionTabInfo : TabInfo? = null

    var isCurrentEntrust = true //是否是当前委托

    override fun loadData() {
        isCurrentEntrust = arguments?.getBoolean("isCurrentEntrust")?:true
    }



    fun bindContract(contract: Contract){
        this.contract = contract
        updateData()
    }

    fun doFilter(statusId:Int,directionTabInfo:TabInfo?){
        this.statusId = statusId
        this.directionTabInfo = directionTabInfo
        if(!isHidden){
            updateData()
        }
    }


    fun bindEntrustType(isCurrentEntrust: Boolean){
        this.isCurrentEntrust = isCurrentEntrust
        entrustAdapter?.setIsCurrentEntrust(isCurrentEntrust!!)
        updateData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden){
            if (isCurrentEntrust){
                doFilterData(ContractUserDataAgent.getContractPlanOrder(contract!!.instrument_id,true))
                entrustAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun updateData(){
        if(!isAdded){
            return
        }
        mList.clear()
        if (isCurrentEntrust){
            doFilterData(ContractUserDataAgent.getContractPlanOrder(contract!!.instrument_id,true))
            entrustAdapter?.notifyDataSetChanged()
        }else{
            loadHistoryDataFromNet()
        }
    }

    private fun doFilterData(data: MutableList<ContractOrder>) {
        mList.clear()
        //方向
        mList.addAll(data.filter {
            if (directionTabInfo == null || directionTabInfo!!.index == 0)  {
                return@filter  true
            } else {
                return@filter it.side == directionTabInfo!!.index
            }
        }.filter {
            var typeFind = false
            if (statusId > 0) {
                val errno: Int = it.errno
                val doneVol: Double = MathHelper.round(it.cum_qty, 8)
                when (directionTabInfo!!.index) {
                    1 -> {//已成交
                        typeFind = ContractOrder.ORDER_ERRNO_NOERR == errno
                    }
                    2 -> {//已撤销 包含用户取消 / 系统取消
                        typeFind =
                            (ContractOrder.ORDER_ERRNO_CANCEL == errno && doneVol <= 0) || errno > ContractOrder.ORDER_ERRNO_CANCEL
                    }
                    3 -> {//部分成交
                        typeFind = ContractOrder.ORDER_ERRNO_CANCEL == errno && doneVol > 0
                    }
                }
            } else {
                typeFind = true
            }
            return@filter typeFind
        })
    }

    private fun loadHistoryDataFromNet() {
        if(mOffset == 0){
            showLoadingDialog()
        }
        ContractUserDataAgent.loadContractPlanOrder(contract!!.instrument_id,ContractOrder.ORDER_STATE_FINISH,mOffset,mLimit,object:
            IResponse<MutableList<ContractOrder>>(){
            override fun onSuccess(data: MutableList<ContractOrder>) {
                doFilterData(data)
                entrustAdapter?.notifyDataSetChanged()
                hideLoadingDialog()
            }

            override fun onFail(code: String, msg: String) {
                super.onFail(code, msg)
                hideLoadingDialog()
                ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
                mList.clear()
                entrustAdapter?.notifyDataSetChanged()
            }
        })
    }

    override fun initView() {
        rv_order_contract.layoutManager = LinearLayoutManager(context)
        entrustAdapter = ContractPlanEntrustAdapter(context!!,mList)
        entrustAdapter?.setIsCurrentEntrust(isCurrentEntrust!!)
        entrustAdapter?.animationEnable = true
        entrustAdapter?.setEmptyView(View.inflate(activity, R.layout.view_empty_layout, null))
        entrustAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        rv_order_contract.adapter = entrustAdapter
        initItemChildClickListener()
        registerContractOrderWsListener()

        updateData()
    }

    private fun initItemChildClickListener() {
        entrustAdapter?.setOnItemChildClickListener(object : OnItemChildClickListener {
            override fun onItemChildClick(
                adapter: BaseQuickAdapter<*, *>,
                view: View,
                position: Int
            ) {
                when (view.id) {
                    R.id.tv_cancel -> {
                        //撤单
                        val order = mList[position]
                        DialogUtils.showCenterDialog(mActivity!!,
                            getString(R.string.str_tips),
                            getString(
                                R.string.str_cancel_order_tips
                            ),
                            getString(R.string.common_text_btnCancel),
                            getString(R.string.str_confirm),
                            object : DialogUtils.DialogBottomListener {
                                override fun clickTab(tabType: Int) {
                                    if (tabType == 1) {
                                        doCancelOrder(order)
                                    }
                                }
                            })
                    }
                }
            }
        })
    }

    private fun registerContractOrderWsListener() {
        ContractUserDataAgent.registerContractPlanOrderWsListener(this,
            object : ContractPlanOrderListener() {
                override fun onWsContractPlanOrder(contractId: Int) {
                    contract ?: return
                    if (!isCurrentEntrust) {
                        return
                    }
                    if (contractId == 0 || contractId == contract!!.instrument_id) {
                        doFilterData(ContractUserDataAgent.getContractPlanOrder(contract!!.instrument_id))
                        mActivity?.runOnUiThread {
                            entrustAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            })
    }

    private fun doCancelOrder(order: ContractOrder) {
        val orders = ContractOrders()
        orders.contract_id = order.instrument_id
        orders.orders?.add(order)
        showLoadingDialog()
        ContractUserDataAgent.doCancelPlanOrders(orders, object : IResponse<MutableList<Long>>() {
            override fun onSuccess(data: MutableList<Long>) {
                hideLoadingDialog()
                if (data != null && data.isNotEmpty()) {
                    ToastUtil.shortToast(
                        mActivity!!,
                        getString(R.string.str_some_orders_cancel_failed)
                    )
                } else {
                    ToastUtil.shortToast(mActivity!!, getString(R.string.str_cancel_success))
                }
            }

            override fun onFail(code: String, msg: String) {
                super.onFail(code, msg)
                hideLoadingDialog()
                ToastUtil.shortToast(mActivity!!, msg)
            }

        })
    }

    companion object {
        fun newInstance(isCurrentEntrust: Boolean = true): ContractPlanEntrustFragment {
            val fg = ContractPlanEntrustFragment()
            val bundle = Bundle()
            bundle.putBoolean("isCurrentEntrust", isCurrentEntrust)
            fg.arguments = bundle
            return fg
        }
    }
}