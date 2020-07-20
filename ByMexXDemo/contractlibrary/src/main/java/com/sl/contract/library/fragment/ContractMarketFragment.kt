package com.sl.contract.library.fragment

import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.impl.ContractTickerListener
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractMarketAdapter
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contract_market.*
import kotlin.math.min

/**
 * 合约行情
 */
class ContractMarketFragment : BaseFragment() {
    override fun setContentView(): Int {
       return R.layout.fragment_contract_market
    }

    private val contractTickerList : ArrayList<ContractTicker> = ArrayList()
    var adapter : ContractMarketAdapter?=null

    override fun loadData() {
        super.loadData()
        val list = ContractPublicDataAgent.getContractTickers()
        if(list.isNotEmpty()){
            contractTickerList.clear()
            contractTickerList.addAll(list)
            adapter?.notifyDataSetChanged()
        }
    }

    override fun initView() {
        market_layout.layoutManager = LinearLayoutManager(activity)
        adapter = ContractMarketAdapter(activity!!,contractTickerList)
        market_layout.adapter = adapter

        ContractPublicDataAgent.registerTickerWsListener(this,object:ContractTickerListener(){
            /**
             * 合约Ticker更新
             */
            override fun onWsContractTicker(ticker: ContractTicker) {
                if(!isVisible){
                    return
                }
                for (index in contractTickerList.indices){
                    if(contractTickerList[index].instrument_id == ticker.instrument_id){
                        contractTickerList[index] = ticker
                        adapter?.notifyDataSetChanged()
                        break
                    }
                }
            }

        })

    }

}