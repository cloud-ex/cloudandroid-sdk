package com.sl.contract.library.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.impl.IResponse
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractHoldHistoryAdapter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.CustomLoadMoreView
import com.sl.ui.library.widget.EmptyLayout
import kotlinx.android.synthetic.main.fragment_contract_hold.*


/**
 * 合约历史持仓
 */
class ContractHistoryHoldFragment : BaseFragment() {

    override fun setContentView(): Int {
        return R.layout.fragment_contract_hold
    }

    private var mContract: Contract? = null
    private var historyHoldAdapter : ContractHoldHistoryAdapter?=null
    private var loadMoreModule: BaseLoadMoreModule? = null
    private var mList = ArrayList<ContractPosition>()
    private val mLimit = 10
    private var mOffset = 0

    override fun initView() {
        historyHoldAdapter = ContractHoldHistoryAdapter(activity!!,mList)
        loadMoreModule = historyHoldAdapter?.loadMoreModule
        loadMoreModule?.loadMoreView = CustomLoadMoreView()
        rv_position.layoutManager = LinearLayoutManager(activity!!)
        val emptyLayout = EmptyLayout(activity!!)
        emptyLayout.text = getString(R.string.str_empty_position)
        historyHoldAdapter?.setEmptyView(emptyLayout)
        rv_position.adapter = historyHoldAdapter
        historyHoldAdapter?.animationEnable = true
        historyHoldAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        loadMoreModule?.setOnLoadMoreListener {
            mOffset = mList.size
           loadDataFromNet()
        }

    }

    fun bindContract(contract: Contract) {
        mContract = contract
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!isHidden && isVisible){
            mOffset = 0
            loadDataFromNet()
        }
    }

    private fun loadDataFromNet() {
        if(mContract == null){
            return
        }
        if(mOffset == 0){
            showLoadingDialog()
        }

        ContractUserDataAgent.loadContractPosition(mContract!!.instrument_id,4,mOffset,mLimit,object : IResponse<MutableList<ContractPosition>>(){
            override fun onSuccess(data: MutableList<ContractPosition>) {
                hideLoadingDialog()
                if (mOffset == 0) {
                    mList.clear()
                }

                if (data != null && data.isNotEmpty()) {
                    mList.addAll(data)
                    mOffset = mList.size
                    historyHoldAdapter?.notifyDataSetChanged()
                    loadMoreModule?.isEnableLoadMore = true
                    loadMoreModule?.loadMoreComplete()
                }else{
                    loadMoreModule?.loadMoreEnd()
                }
                historyHoldAdapter?.notifyDataSetChanged()
                loadMoreModule?.isEnableLoadMoreIfNotFullPage = false

            }

            override fun onFail(code: String, msg: String) {
               hideLoadingDialog()
                loadMoreModule?.loadMoreEnd()
                loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
                ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
            }

        })
    }

    companion object {
        fun newInstance(): ContractHistoryHoldFragment {
            return ContractHistoryHoldFragment()
        }
    }
}