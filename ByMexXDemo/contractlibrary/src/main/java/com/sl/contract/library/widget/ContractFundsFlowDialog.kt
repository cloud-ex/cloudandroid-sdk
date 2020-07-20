package com.sl.contract.library.widget

import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.contract.sdk.ContractUserDataAgent
import com.sl.contract.library.R
import com.sl.ui.library.adapter.FilterLabelAdapter
import com.sl.ui.library.base.BaseDialogFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.widget.GridDividerItemDecoration
import kotlinx.android.synthetic.main.dialog_contract_funds_flow_filter.*

/**
 * 现货资金流水筛选项
 */
class ContractFundsFlowDialog : BaseDialogFragment() {
    override fun setContentView(): Int {
        return R.layout.dialog_contract_funds_flow_filter
    }

    private val typeTabList = ArrayList<TabInfo>()
    private var typeTabInfo : TabInfo? = null
    private var typeAdapter: FilterLabelAdapter? = null

    private val coinTabList = ArrayList<TabInfo>()
    private var coin = ""
    private var coinAdapter: FilterLabelAdapter? = null
    override fun initView() {
        alpha_ll.setOnClickListener(this)

        initTypeAdapter()

        initCoinAdapter()

        bt_reset.setOnClickListener {
            typeTabList.forEach {
                it.selected = typeTabInfo!!.index == it.index
            }
            typeAdapter?.notifyDataSetChanged()

            coinTabList.forEach {
                it.selected = TextUtils.equals(coin, it.name)
            }
            coinAdapter?.notifyDataSetChanged()

        }
        tv_confirm.setOnClickListener {
            for (item in typeTabList){
                if (item.selected) {
                    coin = item.name!!
                    break
                }
            }

            for (item in coinTabList){
                if (item.selected) {
                    typeTabInfo = item
                }
            }

            val filter = ArrayList<Any>(2)
            filter[0] = coin
            filter[1] = typeTabInfo!!
            EventBusUtil.post(MessageEvent(MessageEvent.filter_coin_type_select_notify, coin))
            dismissDialog()
        }
    }

    private fun initTypeAdapter() {
        lv_type_view.layoutManager = GridLayoutManager(activity, 3)
        typeAdapter = FilterLabelAdapter(typeTabList)
        lv_type_view.addItemDecoration(
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
        lv_type_view.adapter = typeAdapter
    }

    private fun initCoinAdapter() {
        lv_coin_view.layoutManager = GridLayoutManager(activity, 3)
        coinAdapter = FilterLabelAdapter(coinTabList)
        lv_coin_view.addItemDecoration(
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
        lv_coin_view.adapter = coinAdapter

    }

    override fun loadData() {
        val contractAccountList = ContractUserDataAgent.getContractAccounts()
        for (index in contractAccountList.indices){
            val item = contractAccountList[index]
            coinTabList.add(TabInfo(item.coin_code, index, TextUtils.equals(coin, item.coin_code)))
        }

        if(typeTabInfo == null){
            typeTabInfo = TabInfo(getString(R.string.str_all),0)
        }
        typeTabList.add(TabInfo(getString(R.string.str_all),0,typeTabInfo!!.index == 0))
        typeTabList.add(TabInfo(getString(R.string.str_transfer_in),5,typeTabInfo!!.index == 5))
        typeTabList.add(TabInfo(getString(R.string.str_transfer_out),6,typeTabInfo!!.index == 6))
        typeTabList.add(TabInfo(getString(R.string.str_air_drop),21,typeTabInfo!!.index == 21))
        typeTabList.add(TabInfo(getString(R.string.str_buy_up),1,typeTabInfo!!.index == 1))
        typeTabList.add(TabInfo(getString(R.string.str_buy_down),4,typeTabInfo!!.index == 4))
        typeTabList.add(TabInfo(getString(R.string.str_sell_up),3,typeTabInfo!!.index == 3))
        typeTabList.add(TabInfo(getString(R.string.str_sell_down),2,typeTabInfo!!.index == 2))
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.leftOut_rightin_DialogFg_style)
    }


    fun showDialog(manager: FragmentManager, tag: String, coin: String,typeTabInfo: TabInfo?) {
        this.coin = coin
        this.typeTabInfo = typeTabInfo
        showDialog(manager, tag)
    }

    public override fun showDialog(manager: FragmentManager, tag: String) {
        super.showDialog(manager, tag)
    }

}