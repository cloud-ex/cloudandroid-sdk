package com.sl.contract.library.fragment.detail

import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.impl.IResponse
import com.sl.contract.library.R
import com.sl.contract.library.adapter.PositionTaxDetailAdapter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.EmptyLayout
import kotlinx.android.synthetic.main.fragment_position_tax_detail.*

/**
 * 仓位资金费用明细
 */
class PositionTaxDetailFragment : BaseFragment(){
    override fun setContentView(): Int {
        return R.layout.fragment_position_tax_detail
    }

    private var taxAdapter : PositionTaxDetailAdapter?=null
    private val mList =  ArrayList<ContractPosition>()
    private var instrumentID = 0

    override fun loadData() {
        instrumentID = activity!!.intent.getIntExtra("instrumentID",0)
        val pid =  activity!!.intent.getLongExtra("pid",0)

        showLoadingDialog()
        ContractUserDataAgent.loadPositionTax(instrumentID,pid,object:
            IResponse<List<ContractPosition>>(){
            override fun onSuccess(data: List<ContractPosition>) {
                hideLoadingDialog()
                mList.addAll(data)
                taxAdapter?.notifyDataSetChanged()
            }

            override fun onFail(code: String, msg: String) {
                super.onFail(code, msg)
                hideLoadingDialog()
                ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
            }
        })
    }

    override fun initView() {
        rv_tax.layoutManager = LinearLayoutManager(context)
        val contract =  ContractPublicDataAgent.getContract(instrumentID)
        contract?.let {
            taxAdapter = PositionTaxDetailAdapter(it,mList)
            taxAdapter?.animationEnable = true
            taxAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.SlideInBottom)
            taxAdapter?.setEmptyView(EmptyLayout(activity!!))
            rv_tax.adapter = taxAdapter
        }
    }
}