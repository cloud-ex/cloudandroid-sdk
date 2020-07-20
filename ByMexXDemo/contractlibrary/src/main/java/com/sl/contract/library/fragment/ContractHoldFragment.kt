package com.sl.contract.library.fragment

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.*
import com.contract.sdk.impl.ContractPlanOrderListener
import com.contract.sdk.impl.ContractPositionListener
import com.contract.sdk.impl.ContractTickerListener
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.activity.AdjustMarginActivity
import com.sl.contract.library.activity.FragmentWarpActivity
import com.sl.contract.library.adapter.ContractHoldAdapter
import com.sl.contract.library.data.FragmentType
import com.sl.contract.library.helper.ContractPositionHelper
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.EmptyLayout
import kotlinx.android.synthetic.main.fragment_contract_hold.*
import org.jetbrains.anko.textColor


/**
 * 合约当前持仓
 */
class ContractHoldFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_hold
    }

    private var mContract: Contract? = null

    private var holdAdapter: ContractHoldAdapter? = null
    private var mList = ArrayList<ContractPosition>()

    private var headerView: View? = null
    private var tvLastPrice: TextView? = null
    private var tvFairPrice: TextView? = null

    private var pxDecimal = NumberUtil.getDecimal(-1)
    private var lastPxDecimal = NumberUtil.getDecimal(-1)

    override fun initView() {
        headerView =
            LayoutInflater.from(context!!).inflate(R.layout.header_contract_hold_layout, null)
        tvLastPrice = headerView?.findViewById(R.id.tv_last_price)
        tvFairPrice = headerView?.findViewById(R.id.tv_fair_price)

        holdAdapter = ContractHoldAdapter(mActivity!!, mList)
        rv_position.layoutManager = LinearLayoutManager(context)
        holdAdapter?.animationEnable = true
        holdAdapter?.addHeaderView(headerView!!)
        holdAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        val emptyLayout = EmptyLayout(activity!!)
        emptyLayout.text = getString(R.string.str_empty_position)
        holdAdapter?.setEmptyView(emptyLayout)
        rv_position.adapter = holdAdapter
        initItemChildClickListener()
        registerContractWsListener()

        mContract?.let {
            ContractPublicDataAgent.getContractTicker(it.instrument_id)?.let { ticker ->
                updateHeaderUi(ticker)
                //异步加载一次止盈止损订单
                ContractUserDataAgent.getContractPlanOrder(it.instrument_id,true)
            }
        }
    }

    private fun registerContractWsListener() {
        /**
         * 监听Ticker
         */
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            override fun onWsContractTicker(ticker: ContractTicker) {
                if (ticker.instrument_id == mContract!!.instrument_id) {
                    holdAdapter?.notifyDataSetChanged()
                    updateHeaderUi(ticker)
                }

            }

        })
        /**
         * 监听计划订单
         */
        ContractUserDataAgent.registerContractPlanOrderWsListener(this,object : ContractPlanOrderListener(){
            override fun onWsContractPlanOrder(contractId: Int) {
                holdAdapter?.notifyDataSetChanged()
            }
        })

        /**
         * 监听仓位
         */
        ContractUserDataAgent.registerContractPositionWsListener(this,
            object : ContractPositionListener() {
                override fun onWsContractPosition(instrument_id: Int?) {
                    mList.clear()
                    mList.addAll(ContractUserDataAgent.getCoinPositions(mContract!!.instrument_id))
                    holdAdapter?.notifyDataSetChanged()
                }

            })
    }

    fun bindContract(contract: Contract) {
        mContract = contract
        mList.clear()
        mList.addAll(ContractUserDataAgent.getCoinPositions(contract.instrument_id, true))
        holdAdapter?.notifyDataSetChanged()

        pxDecimal = NumberUtil.getDecimal(contract.price_index)
        lastPxDecimal = NumberUtil.getDecimal(contract.price_index - 1)

        ContractPublicDataAgent.getContractTicker(contract.instrument_id)?.let {
            updateHeaderUi(it)
        }
    }


    private fun updateHeaderUi(ticker: ContractTicker) {
        tvLastPrice?.text = lastPxDecimal.format(MathHelper.round(ticker.last_px))
        tvFairPrice?.text = pxDecimal.format(MathHelper.round(ticker.fair_px))

        if(ticker.change_rate.toDouble() >= 0){
            tvLastPrice?.textColor = ColorUtils.getMainColorType(context!!,true)
        }else{
            tvLastPrice?.textColor = ColorUtils.getMainColorType(context!!,false)
        }

    }

    private fun initItemChildClickListener() {
        holdAdapter?.setOnItemChildClickListener(object : OnItemChildClickListener {
            override fun onItemChildClick(
                adapter: BaseQuickAdapter<*, *>,
                view: View,
                position: Int
            ) {
                when (view.id) {
                    R.id.tv_adjust_margins -> {
                        //调整保证金
                        AdjustMarginActivity.show(mActivity!!, mList[position])
                    }
                    R.id.tv_stop_profit_loss -> {
                        //止盈止损
                        showStopWinLossDialog(position)
                    }
                    R.id.tv_close_position -> {
                        //平仓
                        doClosePositionAction(position)
                    }
                    R.id.tv_gains_balance -> {
                        //已实现盈亏弹窗
                        val item =  mList[position]
                        ContractPositionHelper.openGainsBalanceDialog(
                            mActivity as FragmentActivity,
                            item
                        ) { _ ->
                            FragmentWarpActivity.show(mActivity!!, FragmentType.POSITION_TAX_DETAIL,item.instrument_id,item.pid)
                        }
                    }
                    R.id.tv_share -> {
                        //分享
                    }
                }
            }

        })
    }

    /**
     * 止盈止损弹窗
     */
    private fun showStopWinLossDialog(position: Int) {
        ContractPositionHelper.openStopWinLossDialog(
            mActivity as FragmentActivity,
            mList[position]
        ) { originOrder, originStopWinOrder, originStopLossOrder ->
            val effectTime =
                if (LogicContractSetting.getStrategyEffectTime(mActivity) == 0) 24 else 168
            originOrder.nonce = System.currentTimeMillis() / 1000
            val winOrder = ContractOrder().fromJson(originOrder.toJson())
            val lossOrder = ContractOrder().fromJson(originOrder.toJson())
            winOrder.px = originOrder.profit_price
            winOrder.type = 1
            winOrder.qty = null //止盈止损不传qty
            winOrder.life_cycle = effectTime
            winOrder.trend =
                if (MathHelper.round(originOrder.profit_price) > MathHelper.round(originOrder.px)) 1 else 2
            if (originOrder.profit_category == 2) {
                winOrder.exec_px = null// 默认市价 不传执行价格】=
            } else {
                winOrder.exec_px = originOrder.profit_ex_price
            }
            winOrder.category = originOrder.profit_category

            lossOrder.px = originOrder.loss_price
            lossOrder.type = 2
            lossOrder.qty = null //止盈止损不传qty
            lossOrder.life_cycle = effectTime
            lossOrder.trend =
                if (MathHelper.round(originOrder.loss_price) > MathHelper.round(originOrder.px)) 1 else 2
            lossOrder.category = originOrder.loss_category
            if (originOrder.loss_category == 2) {
                lossOrder.exec_px = null// 默认市价 不传执行价格】=
            } else {
                lossOrder.exec_px = originOrder.loss_ex_price
            }
            when (originOrder.with_mission) {
                1 -> {
                    SDKLogUtil.d(ContractSDKAgent.sTAG, "提交止盈")
                    doPlanSubmit(winOrder)
                    if (originStopLossOrder != null) {
                        SDKLogUtil.d(ContractSDKAgent.sTAG, "取消止损")
                        doCancelPlanSubmit(originStopLossOrder)
                    }
                }
                2 -> {
                    SDKLogUtil.d(ContractSDKAgent.sTAG, "提交止损")
                    doPlanSubmit(lossOrder)
                    if (originStopWinOrder != null) {
                        SDKLogUtil.d(ContractSDKAgent.sTAG, "取消止盈")
                        doCancelPlanSubmit(originStopWinOrder)
                    }
                }
                3 -> {
                    SDKLogUtil.d(ContractSDKAgent.sTAG, "提交止盈和止损")
                    doPlanSubmit(winOrder)
                    doPlanSubmit(lossOrder)
                }
                0 -> {
                    if (originStopLossOrder != null) {
                        SDKLogUtil.d(ContractSDKAgent.sTAG, "取消止损")
                        doCancelPlanSubmit(originStopLossOrder)
                    }
                    if (originStopWinOrder != null) {
                        SDKLogUtil.d(ContractSDKAgent.sTAG, "取消止盈")
                        doCancelPlanSubmit(originStopWinOrder)
                    }
                }
            }
        }
    }

    private fun doPlanSubmit(order: ContractOrder){
        ContractUserDataAgent.doSubmitPlanOrder(order,object : IResponse<String>(){
            override fun onSuccess(data: String) {
                ToastUtil.shortToast(ContractSDKAgent.context!!, getString(R.string.str_order_success))
            }

            override fun onFail(code: String, msg: String) {
                ToastUtil.shortToast(ContractSDKAgent.context!!, msg)
            }

        })
    }
    private fun doCancelPlanSubmit(order: ContractOrder){
        val orders = ContractOrders()
        orders.contract_id = order.instrument_id
        orders.orders?.add(order)
        ContractUserDataAgent.doCancelPlanOrders(orders,object : IResponse<MutableList<Long>>(){
            override fun onSuccess(data: MutableList<Long>) {
                ToastUtil.shortToast(ContractSDKAgent.context!!, getString(R.string.str_cancel_success))
            }

            override fun onFail(code: String, msg: String) {
                ToastUtil.shortToast(ContractSDKAgent.context!!, msg)
            }

        })
    }

    /**
     * 平仓
     */
    private fun doClosePositionAction(position: Int) {
        val position = mList[position]
        ContractPositionHelper.openClosePositionDialog(
            mActivity as FragmentActivity,
            position
        ) { tebIndex: Int, vol: Int, px: Double ->
            //tebIndex 0 市价全平  1 限价平仓
            //最大可平
            val maxClosePosVol =
                MathHelper.round(MathHelper.sub(position.cur_qty, position.freeze_qty), 0)
            //如果平仓量 大于可平仓量，则取消全部订单 在执行平仓
            val needCancelOrder = vol > maxClosePosVol
            if (needCancelOrder) {
                val orderList = ContractUserDataAgent.getContractOrder(position)
                if (orderList != null && orderList.isNotEmpty()) {
                    val orders = ContractOrders()
                    orders.contract_id = position.instrument_id
                    orders.orders = orderList
                    //取消全部订单
                    showLoadingDialog()
                    ContractUserDataAgent.doCancelOrders(orders,
                        object : IResponse<MutableList<Long>>() {
                            override fun onSuccess(data: MutableList<Long>) {
                                //构建订单对象
                                var orders = ContractPositionHelper.buildCloseOrder(
                                    position,
                                    vol,
                                    px,
                                    tebIndex == 0
                                )
                                //提交订单
                                doSubmitOrder(orders)
                            }

                            override fun onFail(code: String, msg: String) {
                                super.onFail(code, msg)
                                hideLoadingDialog()
                                ToastUtil.shortToast(
                                    context!!,
                                    context!!.getString(R.string.str_cancel_fail)
                                )
                            }

                        })
                }else{
                    //获取一次计划订单
                    ContractUserDataAgent.getContractOrder(position.instrument_id,true)
                }
            } else {
                showLoadingDialog()
                var orders =
                    ContractPositionHelper.buildCloseOrder(position, vol, px, tebIndex == 0)
                doSubmitOrder(orders)
            }

        }
    }

    /**
     * 提交订单
     */
    private fun doSubmitOrder(order: ContractOrder) {
        ContractUserDataAgent.doSubmitOrder(order, object : IResponse<String>() {
            override fun onSuccess(data: String) {
                hideLoadingDialog()
                ToastUtil.shortToast(
                    context!!,
                    context!!.getString(R.string.str_close_position_success)
                )
            }

            override fun onFail(code: String, msg: String) {
                hideLoadingDialog()
                ToastUtil.shortToast(context!!, msg)
            }

        })
    }


    companion object {
        fun newInstance(): ContractHoldFragment {
            return ContractHoldFragment()
        }
    }
}