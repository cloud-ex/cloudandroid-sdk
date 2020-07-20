package com.sl.contract.library.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractCashBook
import com.contract.sdk.impl.IResponse
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractFundsFlowAdapter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.widget.CustomLoadMoreView
import kotlinx.android.synthetic.main.fragment_contract_funds_flow.*

/**
 * 合约资金流水
 */
class ContractFundsFlowFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_funds_flow
    }

    private var mList = ArrayList<ContractCashBook>()
    private var fundsFlowAdapter : ContractFundsFlowAdapter?=null
    private var loadMoreModule: BaseLoadMoreModule? = null

    /**
     * 合约 筛选类型/币种
     */
    private var contractFilterType: TabInfo? = null
    private var contractCoin = ""
    private val mLimit = 10
    private var mOffset = 0


    override fun initView() {
        fundsFlowAdapter = ContractFundsFlowAdapter(mList)
        loadMoreModule = fundsFlowAdapter?.loadMoreModule
        loadMoreModule?.loadMoreView = CustomLoadMoreView()
        lv_list.layoutManager = LinearLayoutManager(context!!)
        fundsFlowAdapter?.setEmptyView(View.inflate(context!!, R.layout.view_empty_layout, null))
        lv_list.adapter = fundsFlowAdapter

        loadDataFromNet()
    }

    /**
     * 筛选
     */
    fun doFilter(contractCoin: String, contractFilterType: TabInfo) {
        this.contractCoin = contractCoin
        this.contractFilterType = contractFilterType
        loadDataFromNet()
    }

    private fun loadDataFromNet() {
        var action: IntArray? = getTypeAction()
        ContractUserDataAgent.loadCashBooks(
            0,
            getTypeAction(),
            contractCoin,
            mLimit,
            mOffset,
            object : IResponse<List<ContractCashBook>>() {
                override fun onSuccess(data: List<ContractCashBook>) {
                    if (mOffset == 0) {
                        mList.clear()
                    }
                    mList.addAll(data!!)

                    fundsFlowAdapter?.notifyDataSetChanged()
                }

            })
    }

    private fun getTypeAction(): IntArray? {
        var action1: IntArray? = null
        when (contractFilterType?.index) {
            1 -> {
                action1 = intArrayOf(1)
            }
            2 -> {
                action1 = intArrayOf(2)
            }
            3 -> {
                action1 = intArrayOf(3)
            }
            4 -> {
                action1 = intArrayOf(4)
            }
            5, 7 -> {
                action1 = intArrayOf(5, 7)
            }
            6, 8 -> {
                action1 = intArrayOf(6, 8)
            }
            9 -> {
                action1 = intArrayOf(9)
            }
            10 -> {
                action1 = intArrayOf(10)
            }
            11 -> {
                action1 = intArrayOf(11)
            }
            22 -> {
                action1 = intArrayOf(22)
            }
        }
        return action1
    }

    companion object {
        fun newInstance(): ContractFundsFlowFragment {
            return ContractFundsFlowFragment()
        }
    }
}