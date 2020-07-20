package com.sl.contract.library.fragment.detail

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTrade
import com.contract.sdk.extra.dispense.DataTradeHelper
import com.contract.sdk.impl.ContractTradeListener
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractTradeAdapter
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contract_trade.*

/**
 * 合约交易记录
 */
class ContractTradeRecordFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_trade
    }

    private var contract: Contract? = null
    private val mList = ArrayList<ContractTrade>()
    private var adapter : ContractTradeAdapter ? = null
    private var headerView: View? = null

    override fun initView() {
        lv_trade.layoutManager = LinearLayoutManager(context)
        adapter = ContractTradeAdapter(context!!,mList)
        headerView = LayoutInflater.from(context).inflate(R.layout.header_contract_trade_record_layout,null)
        adapter?.addHeaderView(headerView!!)
        lv_trade.adapter = adapter

        ContractPublicDataAgent.registerTradeWsListener(this,object : ContractTradeListener(){
            override fun onWsContractTrade(contractId: Int, allData: ArrayList<ContractTrade>) {
                if(contractId == contract?.instrument_id ){
                    if(isVisible){
                        mList.clear()
                        mList.addAll(allData)
                        adapter?.notifyDataSetChanged()
                    }
                }
            }

        },20)

        updateHeaderView()
    }

    fun bindContract(contract: Contract) {
        this.contract = contract
        ContractPublicDataAgent.subscribeTradeWs(contract.instrument_id)
        updateHeaderView()
    }


    private fun updateHeaderView(){
        headerView?.let {
            val tvPriceLabel = it.findViewById<TextView>(R.id.tv_price_label)
            tvPriceLabel?.text = getString(R.string.str_price)+"("+contract!!.base_coin+")"

            val unit: Int = LogicContractSetting.getContractUint(context)
            val tvVolLabel = it.findViewById<TextView>(R.id.tv_vol_label)
            tvVolLabel?.text = getString(R.string.str_amount) +"(${if (unit == 0) getString(R.string.str_vol_unit) else contract!!.base_coin})"
        }
    }

}