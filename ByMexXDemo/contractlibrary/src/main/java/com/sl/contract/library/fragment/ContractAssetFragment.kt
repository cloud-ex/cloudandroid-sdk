package com.sl.contract.library.fragment

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.sl.contract.library.R
import com.sl.contract.library.adapter.SlContractAssetAdapter
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contract_asset.*

/**
 * 合约资产
 */
class ContractAssetFragment() : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_asset
    }
    var isShowAssetEye = true
    var adapter : SlContractAssetAdapter?=null
    val mList =  ArrayList<ContractAccount>()


    override fun initView() {
        adapter = SlContractAssetAdapter(mActivity!!,mList)
        lv_contract.layoutManager = LinearLayoutManager(context)
        lv_contract.adapter = adapter
        adapter?.setEmptyView(View.inflate(activity, R.layout.view_empty_layout, null))

        updateAccountData()
    }

    fun updateAccountData(){
       val accountList =  ContractUserDataAgent.getContractAccounts()
        mList.clear()
        mList.addAll(accountList)
        updateAssetEye(isShowAssetEye)
    }

    fun updateAssetEye(isShowAssetEye : Boolean){
        adapter?.notifyAssetDataRefresh(isShowAssetEye)
    }

    companion object{
        fun newInstance():ContractAssetFragment{
            return ContractAssetFragment()
        }
    }
}