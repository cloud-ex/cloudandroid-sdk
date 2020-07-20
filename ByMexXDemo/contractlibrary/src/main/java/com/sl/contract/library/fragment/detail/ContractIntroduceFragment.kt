package com.sl.contract.library.fragment.detail

import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractIndex
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.activity.FragmentWarpActivity
import com.sl.contract.library.data.FragmentType
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.showUnderLine
import kotlinx.android.synthetic.main.fragment_contract_introduce.*

/**
 * 合约介绍
 */
class ContractIntroduceFragment : BaseFragment(){
    override fun setContentView(): Int {
        return R.layout.fragment_contract_introduce
    }
    private var contract : Contract? = null
    override fun loadData() {
        var contractId = mActivity!!.intent.getIntExtra("contractId",0)
        contract = ContractPublicDataAgent.getContract(contractId)
    }
    override fun initView() {
        updateUi()
    }

    private fun updateUi() {
        contract?.let {
            //合约基础币种
            tv_contract_base_coin.text = it.base_coin
            //保证金币种
            tv_margin_coin.text = it.margin_coin
            //合约属性
            tv_contract_property.text = if (it.isReserve) getString(R.string.str_reserve_contract) else getString(R.string.str_positive_contract)
            //合约大小
            tv_contract_size.text = "1"+getString(R.string.str_vol_unit)+"="+ it.face_value + it.price_coin
            //最高杠杆
            tv_max_leverage.text = it.max_leverage+"X"
            //指数信息
            loadIndexSource(it)
            val ticker = ContractPublicDataAgent.getContractTicker(it.instrument_id)
            ticker?.let { ticker ->
                //总持仓量
                tv_all_hold_vol.text = ticker.position_size + getString(R.string.str_vol_unit)
                //成交量
                tv_deal_vol.text = ticker.qty24
                //换手比
                tv_swap_value.text = NumberUtil.getDecimal(-1).format(MathHelper.div(ticker.qty24, ticker.position_size, 4))
                //资金费率
                tv_funds_rate.showUnderLine()
                tv_funds_rate.text = NumberUtil.getDecimal(4).format(MathHelper.mul(ticker.funding_rate,"100")) + " %"
                tv_funds_rate.setOnClickListener {
                    FragmentWarpActivity.show(mActivity!!, FragmentType.FUNDS_RATE,ticker.instrument_id)
                }
                //保险基金
                tv_insurance_fund.showUnderLine()
                tv_insurance_fund.text = NumberUtil.getDecimal(it.value_index).format(NumberUtil.createDouble(ticker.risk_revers_newest))+" "+it.margin_coin
                tv_insurance_fund.setOnClickListener {
                    FragmentWarpActivity.show(mActivity!!, FragmentType.INSURANCE_FUND,ticker.instrument_id)
                }
            }

        }
    }

    /**
     * 加载指数信息
     */
    private fun loadIndexSource(contract: Contract) {
        ContractPublicDataAgent.loadIndexes(object: IResponse<MutableList<ContractIndex>>(){
            override fun onSuccess(data: MutableList<ContractIndex>) {
                if (data != null) {
                    var indexSource = ""
                    for (i in data.indices) {
                        val index: ContractIndex = data[i]
                        if (index.index_id === contract.index_id) {
                            for (j in 0 until index.market.size) {
                                indexSource += index.market[j]
                                if (j < index.market.size - 1) {
                                    indexSource += " , "
                                }
                            }
                        }
                    }
                    tv_index_source?.text = indexSource
                }
            }

        })
    }

}