package com.sl.bymex.ui.fragment.asset

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.sl.bymex.R
import com.sl.bymex.adapter.SpotFundsFlowAdapter
import com.sl.bymex.data.DwRecord
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.CommonUtils
import com.sl.ui.library.widget.CustomLoadMoreView
import kotlinx.android.synthetic.main.fragment_spot_funds_flow.*

/**
 * 现货资金流水
 */
class SpotFundsFlowFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_funds_flow
    }

    var fundsFlowAdapter: SpotFundsFlowAdapter?=null
    private var loadMoreModule: BaseLoadMoreModule? = null

    private var mList = ArrayList<DwRecord>()
    override fun initView() {
        fundsFlowAdapter = SpotFundsFlowAdapter(mList)

        loadMoreModule = fundsFlowAdapter?.loadMoreModule
        loadMoreModule?.loadMoreView = CustomLoadMoreView()
        lv_list.layoutManager = LinearLayoutManager(context!!)
        fundsFlowAdapter?.setEmptyView(View.inflate(context!!, R.layout.view_empty_layout, null))
        lv_list.adapter = fundsFlowAdapter

        addData()
    }

    fun addData() {
        //TODO 假数据
        var depositString = CommonUtils.readJsonFromAsset(context!!,"spot_deposit.json")
        mList.addAll(JSON.parseArray(depositString,DwRecord::class.java))
        fundsFlowAdapter?.notifyDataSetChanged()
    }
    companion object{
        fun newInstance() : SpotFundsFlowFragment{
            return SpotFundsFlowFragment()
        }
    }
}