package com.sl.contract.library.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractRankAdapter
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contract_market.*
import kotlin.math.min

/**
 * 合约日盈利排行榜
 */
class ContractRankFragment : BaseFragment() {
    override fun setContentView(): Int {
       return R.layout.fragment_contract_rank
    }

    private val contractTickerList : ArrayList<ContractTicker> = ArrayList()
    var adapter : ContractRankAdapter?=null

    override fun loadData() {
        super.loadData()
        val list = ContractPublicDataAgent.getContractTickers()
        if(list.isNotEmpty()){
            contractTickerList.clear()
            contractTickerList.addAll(list.subList(0, min(4,list.size-1)))
        }
    }

    override fun initView() {
        market_layout.layoutManager = LinearLayoutManager(activity)
        adapter = ContractRankAdapter(activity!!,contractTickerList)
        market_layout.adapter = adapter
    }

}