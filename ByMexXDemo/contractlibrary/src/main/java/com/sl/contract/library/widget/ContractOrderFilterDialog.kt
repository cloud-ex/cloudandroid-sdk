package com.sl.contract.library.widget

import android.os.Bundle
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.contract.sdk.data.ContractOrder
import com.sl.contract.library.R
import com.sl.ui.library.adapter.FilterLabelAdapter
import com.sl.ui.library.base.BaseDialogFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.widget.GridDividerItemDecoration
import kotlinx.android.synthetic.main.dialog_contract_entrust_order_filter.*

/**
 * 合约订单筛选项
 */
class ContractOrderFilterDialog : BaseDialogFragment() {
    override fun setContentView(): Int {
        return R.layout.dialog_contract_entrust_order_filter
    }

    private val directionTabList = ArrayList<TabInfo>()
    private var directionTabInfo : TabInfo? = null
    private var directionAdapter: FilterLabelAdapter? = null

    private val statusTabList = ArrayList<TabInfo>()
    private var statusId = 0
    private var statusAdapter: FilterLabelAdapter? = null
    override fun initView() {
        alpha_ll.setOnClickListener(this)

        initTypeAdapter()

        initCoinAdapter()

        bt_reset.setOnClickListener {
            directionTabList.forEach {
                it.selected = 0 == it.index
            }
            directionAdapter?.notifyDataSetChanged()

            statusTabList.forEach {
                it.selected = 0 == it.index
            }
            statusAdapter?.notifyDataSetChanged()

        }
        tv_confirm.setOnClickListener {
            for (item in statusTabList){
                if (item.selected) {
                    statusId = item.index
                    break
                }
            }

            for (item in directionTabList){
                if (item.selected) {
                    directionTabInfo = item
                    break
                }
            }

            val filter = ArrayList<Any>(2)
            filter.add(statusId)
            filter.add(directionTabInfo!!)
            EventBusUtil.post(MessageEvent(MessageEvent.filter_order_type_select_notify, filter))
            dismissDialog()
        }
    }

    private fun initTypeAdapter() {
        lv_direction_view.layoutManager = GridLayoutManager(activity, 3)
        directionAdapter = FilterLabelAdapter(directionTabList)
        lv_direction_view.addItemDecoration(
            GridDividerItemDecoration(
                activity,
                30,
                25,
                true,
                false,
                resources.getColor(R.color.bg_card_color),
                0.82
            )
        )
        lv_direction_view.adapter = directionAdapter
    }

    private fun initCoinAdapter() {
        lv_status_view.layoutManager = GridLayoutManager(activity, 3)
        statusAdapter = FilterLabelAdapter(statusTabList)
        lv_status_view.addItemDecoration(
            GridDividerItemDecoration(
                activity,
                30,
                25,
                true,
                false,
                resources.getColor(R.color.bg_card_color),
                0.82
            )
        )
        lv_status_view.adapter = statusAdapter

    }

    override fun loadData() {
        //状态
        statusTabList.add(TabInfo(getString(R.string.str_all),0,statusId == 0))
        statusTabList.add(TabInfo(getString(R.string.str_deal_done),1,statusId == 1))
        statusTabList.add(TabInfo(getString(R.string.str_has_cancel),2,statusId == 2))
        statusTabList.add(TabInfo(getString(R.string.str_part_deal),3,statusId == 3))

        //方向
        if(directionTabInfo == null){
            directionTabInfo = TabInfo(getString(R.string.str_all),0)
        }
        directionTabList.add(TabInfo(getString(R.string.str_all),0,directionTabInfo!!.index == 0))
        directionTabList.add(TabInfo(getString(R.string.str_buy_up), ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG,directionTabInfo!!.index == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG))
        directionTabList.add(TabInfo(getString(R.string.str_buy_down),ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT,directionTabInfo!!.index == ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT))
        directionTabList.add(TabInfo(getString(R.string.str_sell_up),ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG,directionTabInfo!!.index == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG))
        directionTabList.add(TabInfo(getString(R.string.str_sell_down),ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT,directionTabInfo!!.index == ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT))
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftOut_rightin_DialogFg_style)
    }


    fun showDialog(manager: FragmentManager, tag: String, statusId: Int,directionTabInfo: TabInfo?) {
        this.statusId = statusId
        this.directionTabInfo = directionTabInfo
        showDialog(manager, tag)
    }

    public override fun showDialog(manager: FragmentManager, tag: String) {
        super.showDialog(manager, tag)
    }

}