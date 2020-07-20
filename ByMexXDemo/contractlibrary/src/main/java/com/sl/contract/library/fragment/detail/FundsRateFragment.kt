package com.sl.contract.library.fragment.detail

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.ContractFundingRate
import com.contract.sdk.impl.IResponse
import com.sl.contract.library.R
import com.sl.contract.library.adapter.FundsRateAdapter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.EmptyLayout
import kotlinx.android.synthetic.main.fragment_cotract_detail_other_recycler.*

/**
 * 资金费率
 */
class FundsRateFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_cotract_detail_other_recycler
    }

    var mList = ArrayList<ContractFundingRate>()
    var adapter: FundsRateAdapter? = null

    override fun loadData() {
        var contractId = mActivity!!.intent.getIntExtra("contractId", 0)

        loadDataFromNet(contractId)
    }

    private fun loadDataFromNet(contractId: Int) {
        showLoadingDialog()
        ContractPublicDataAgent.loadFundingRate(contractId,
            object : IResponse<MutableList<ContractFundingRate>>() {
                override fun onSuccess(data: MutableList<ContractFundingRate>) {
                    if (data != null) {
                        mList.addAll(data)
                        adapter?.notifyDataSetChanged()
                    }
                    hideLoadingDialog()
                }

                override fun onFail(code: String, msg: String) {
                    ToastUtil.shortToast(ContractSDKAgent.context!!, msg)
                    hideLoadingDialog()
                }

            })
    }

    override fun initView() {
        recycler_fund_layout.layoutManager = LinearLayoutManager(mActivity)
        adapter = FundsRateAdapter(mList)
        adapter?.setEmptyView(EmptyLayout(mActivity!!))
        adapter?.animationEnable = true
        recycler_fund_layout.adapter = adapter

        val headerView = LayoutInflater.from(mActivity)
            .inflate(R.layout.item_contract_detail_header_layout, recycler_fund_layout, false)
        adapter?.addHeaderView(headerView)
        val tv_header_center = headerView.findViewById<TextView>(R.id.tv_header_center)
        tv_header_center.visibility = View.VISIBLE
        tv_header_center.text = getString(R.string.str_funds_rate_interval)
        headerView.findViewById<TextView>(R.id.tv_header_right).text =
            getString(R.string.str_funds_rate)
    }
}