package com.sl.contract.library.fragment.detail

import com.contract.sdk.data.Contract
import com.contract.sdk.data.DepthData
import com.contract.sdk.extra.dispense.DataDepthHelper
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_contract_dish.*

/**
 * 合约盘口
 */
class ContractDishFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_dish
    }

    private var contract : Contract ? = null

    override fun initView() {
        updateHeaderView()
        DataDepthHelper.instance?.getDepthSource(count = 15) { buyList, sellList ->
            bindDepthData(buyList, sellList)
        }
    }

    fun bindContract(contract:Contract){
        this.contract = contract
    }

    private fun updateHeaderView(){
        contract?.let {
            val unit: Int = LogicContractSetting.getContractUint(context)
            tv_buy_label?.text = getString(R.string.str_buy_dish) + " "+getString(R.string.str_amount) +"(${if (unit == 0) getString(R.string.str_vol_unit) else it.base_coin})"
            tv_sell_label?.text = getString(R.string.str_sell_dish) + " "+getString(R.string.str_amount) +"(${if (unit == 0) getString(R.string.str_vol_unit) else it.base_coin})"
        }
    }

    fun bindDepthData(buyList : ArrayList<DepthData> , sellList: ArrayList<DepthData>){
        contract?.let {
            lv_depth_layout?.setData(sellList ,buyList,it)
        }
    }

}