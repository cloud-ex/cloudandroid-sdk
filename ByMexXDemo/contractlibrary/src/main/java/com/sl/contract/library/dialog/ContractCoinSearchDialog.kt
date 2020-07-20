package com.sl.contract.library.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.impl.ContractTickerListener
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractSearchAdapter
import com.sl.contract.library.data.ContractSearchInfo
import com.sl.ui.library.base.BaseDialogFragment
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.DisplayUtils
import kotlinx.android.synthetic.main.sl_dialog_contract_coin_search.*

/**
 * 合约搜索侧边栏
 * https://www.jianshu.com/p/01001a233c3c
 */
class ContractCoinSearchDialog : BaseDialogFragment() {
    override fun setContentView(): Int {
        return R.layout.sl_dialog_contract_coin_search
    }

    private val sectionEntityList = ArrayList<ContractSearchInfo>()
    var adapter : ContractSearchAdapter?= null
    var mContractId = 0
    override fun initView() {
        alpha_ll.setOnClickListener(this)
        lv_view.layoutManager = LinearLayoutManager(activity)
        adapter = ContractSearchAdapter(R.layout.view_contact_search_header_layout,R.layout.view_contact_search_item_layout,sectionEntityList)
        adapter?.mContractId = mContractId
        lv_view.adapter = adapter
        adapter?.setEmptyView(View.inflate(activity, R.layout.view_empty_layout, null))

        adapter?.setOnItemClickListener { _, _, position ->
            if(DisplayUtils.isFastClick){
                return@setOnItemClickListener
            }
            var info = sectionEntityList[position]
            if(!info.isHeader){
                EventBusUtil.post(MessageEvent(MessageEvent.sl_contract_left_coin_type,info.contractTicker))
                dismissDialog()
            }
        }

        ContractPublicDataAgent.registerTickerWsListener(this,object :ContractTickerListener(){
            override fun onWsContractTicker(ticker: ContractTicker) {
                for (item in sectionEntityList){
                    if(!item.isHeader && item.contractTicker.instrument_id == ticker.instrument_id){
                        item.contractTicker = ticker
                        break
                    }
                }
                adapter?.notifyDataSetChanged()
            }

        })
    }

    override fun loadData() {
        val usdtList = ArrayList<ContractSearchInfo>()
        val inverseList = ArrayList<ContractSearchInfo>()
        val simulationList = ArrayList<ContractSearchInfo>()
        for(item in ContractPublicDataAgent.getContractTickers()){
            if(item.block == Contract.CONTRACT_BLOCK_USDT){
                usdtList.add(ContractSearchInfo(false,item))
            }else if(item.block == Contract.CONTRACT_BLOCK_MAIN || item.block == Contract.CONTRACT_BLOCK_INNOVATION){
                inverseList.add(ContractSearchInfo(false,item))
            }else if(item.block == Contract.CONTRACT_BLOCK_SIMULATION){
                simulationList.add(ContractSearchInfo(false,item))
            }
        }

        if(usdtList.size > 0){
            sectionEntityList.add(ContractSearchInfo(true, ContractTicker(getString(R.string.str_coin_usdt))))
            sectionEntityList.addAll(usdtList)
        }

        if(inverseList.size > 0){
            sectionEntityList.add(ContractSearchInfo(true,ContractTicker(getString(R.string.str_coin_inverse))))
            sectionEntityList.addAll(inverseList)
        }

        if(simulationList.size > 0){
            sectionEntityList.add(ContractSearchInfo(true,ContractTicker(getString(R.string.str_coin_simulation))))
            sectionEntityList.addAll(simulationList)
        }
    }


    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftin_rightout_DialogFg_style)
    }


    public  fun showDialog(manager: FragmentManager, tag: String,contractId:Int) {
        mContractId = contractId
        showDialog(manager, tag)
    }

    public override fun showDialog(manager: FragmentManager, tag: String) {
        super.showDialog(manager, tag)
    }

}