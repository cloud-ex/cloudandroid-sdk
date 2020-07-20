package com.sl.contract.library.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.contract.sdk.data.Contract
import com.contract.sdk.data.DepthData
import com.sl.contract.library.adapter.BuySellContractAdapter
import java.util.*

/**
 * 深度交易盘面
 */
class DeepTreadDiskWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : RecyclerView(context, attrs, defStyle)
    {
    private val buySellContractAdapter: BuySellContractAdapter
    private var uiType = 1 //1买 2卖
    private var contract= Contract()
    private var showNum = 5
    fun bindContract(contract: Contract) {
        this.contract = contract
        buySellContractAdapter?.bindContract(contract)
    }

    fun initData(list: List<DepthData>?) {
        var list = list
        if (list == null) {
            list = ArrayList()
        }
        buySellContractAdapter.setData(list, uiType, showNum)
        buySellContractAdapter.notifyDataSetChanged()
    }



    /**
     * 更改盘面类型
     * @param type
     */
    fun updateDeepType(type: Int, list: List<DepthData>?) {
        var list = list
        when (type) {
//            AppConstant.DEFAULT_TAPE -> showNum = 5
//            AppConstant.SELL_TAPE -> showNum = if (uiType == 1) {
//                0
//            } else {
//                10
//            }
//            AppConstant.BUY_TAPE -> showNum = if (uiType == 1) {
//                10
//            } else {
//                0
//            }
        }
        if (showNum == 0) {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            }
        } else {
            if (visibility == View.GONE) {
                visibility = View.VISIBLE
            }
            if (list == null) {
                list = ArrayList()
            }
            buySellContractAdapter.setData(list, uiType, showNum)
            buySellContractAdapter.notifyDataSetChanged()
        }
    }

    fun getData() : ArrayList<DepthData>{
       return  buySellContractAdapter.getList()
    }


    init {
        uiType = tag.toString().toInt()
        buySellContractAdapter = BuySellContractAdapter(context)
        layoutManager = LinearLayoutManager(context)
        buySellContractAdapter.setData(ArrayList(), uiType, showNum)
        adapter = buySellContractAdapter
    }
}