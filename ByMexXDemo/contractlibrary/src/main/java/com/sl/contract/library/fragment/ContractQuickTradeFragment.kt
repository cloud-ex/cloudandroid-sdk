package com.sl.contract.library.fragment

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.*
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.activity.ContractPositionActivity
import com.sl.contract.library.adapter.HQuickContractHoldAdapter
import com.sl.contract.library.helper.ContractService
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.helper.ContractPositionHelper
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.TopToastUtils
import kotlinx.android.synthetic.main.fragment_trade_quick_contract.*
import kotlinx.android.synthetic.main.fragment_trade_quick_contract.kline_layout
import kotlinx.android.synthetic.main.fragment_trade_quick_contract.lv_position

/**
 * 合约闪电交易fragment
 */
class ContractQuickTradeFragment : BaseFragment(), View.OnClickListener {
    override fun setContentView(): Int {
        return R.layout.fragment_trade_quick_contract
    }

    private var mContract: Contract? = null
    private var ticker: ContractTicker? = null

    private var volDecimal = NumberUtil.getDecimal(-1)
    private var pxDecimal = NumberUtil.getDecimal(-1)
    private val defaultDecimal = NumberUtil.getDecimal(-1)

    private var positionAdapter: HQuickContractHoldAdapter? = null
    private val mPositionList = ArrayList<ContractPosition>()

    private var buyPx = "0.00"
    private var sellPx = "0.00"

    override fun initView() {
        //监听
        registerListener()
        initPositionAdapter()
    }

    private fun initPositionAdapter() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        lv_position.layoutManager = layoutManager
        positionAdapter = HQuickContractHoldAdapter(context!!, mPositionList)
        positionAdapter?.animationEnable = true
        positionAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        lv_position.adapter = positionAdapter
        positionAdapter?.setOnItemChildClickListener(object : OnItemChildClickListener {
            override fun onItemChildClick(
                adapter: BaseQuickAdapter<*, *>,
                view: View,
                position: Int
            ) {
                when (view.id) {
                    R.id.ll_item_warp_layout -> {
                        ContractPositionActivity.show(mActivity!!, mContract!!.instrument_id)
                    }
                    R.id.tv_quick_close -> {
                        //闪电平仓
                        val info = mPositionList[position]
                        showQuickClosePositionDialog(info)
                    }
                }
            }

        })

        kline_layout?.isQuickMode = true
    }

    /**
     * 打开闪电平仓确认弹窗
     */
    private fun showQuickClosePositionDialog(position: ContractPosition) {
        val vol = MathHelper.sub(position.cur_qty, position.freeze_qty).toInt()
        DialogUtils.showCenterDialog(mActivity!!, getString(R.string.str_quick_close_position),
            String.format(
                getString(R.string.str_quick_close_position_tips),
                vol.toString(),
                mContract!!.symbol
            ), clickListener = object :
                DialogUtils.DialogBottomListener {
                override fun clickTab(tabType: Int) {
                    if (tabType == 1) {
                        val orderList = ContractUserDataAgent.getContractOrder(position)
                        if (orderList != null && orderList.isNotEmpty()) {
                            val orders = ContractOrders()
                            orders.contract_id = position.instrument_id
                            orders.orders = orderList
                            //取消全部订单
                            ContractUserDataAgent.doCancelOrders(orders,
                                object : IResponse<MutableList<Long>>() {
                                    override fun onSuccess(data: MutableList<Long>) {
                                        //构建订单对象
                                        var orders = ContractPositionHelper.buildCloseOrder(
                                            position,
                                            vol,
                                            0.00,
                                            true
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
                        } else {
                            var orders =
                                ContractPositionHelper.buildCloseOrder(position, vol, 0.00, true)
                            doSubmitOrder(orders)
                        }
                    }
                }

            })
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


    fun bindContract(contract: Contract, resetData: Boolean) {
        this.mContract = contract
        volDecimal = NumberUtil.getDecimal(contract.value_index)
        pxDecimal = NumberUtil.getDecimal(contract.price_index)
        kline_layout?.bindContract(contract)
        //更新仓位
        updatePositionData()
    }

    /**
     * 更新Ticker
     */
    fun updateTicker(ticker: ContractTicker) {
        if (isVisible) {
            this.ticker = ticker
            //Ticker变化 须更新仓位
            if (ContractSDKAgent.isLogin) {
                positionAdapter?.notifyDataChanged(false)
            }
        }
    }

    /**
     * 更新深度价格
     */
    fun updateDepthPrice(buyPx: String, sellPx: String) {
        this.buyPx = pxDecimal.format(MathHelper.round(buyPx))
        this.sellPx = pxDecimal.format(MathHelper.round(sellPx))
        tv_open_long?.text = this.sellPx
        tv_open_short?.text = this.buyPx
    }

    /**
     * 更新仓位
     */
    fun updatePositionData() {
        mActivity ?: return
        mPositionList.clear()
        if (ContractSDKAgent.isLogin) {
            mPositionList.addAll(
                ContractUserDataAgent.getCoinPositions(
                    mContract?.instrument_id ?: 0
                )
            )
        }
        positionAdapter?.notifyDataChanged(true)
    }


    private fun registerListener() {
        ll_open_long.setOnClickListener(this)
        ll_open_short.setOnClickListener(this)
        //监听K线
        ContractPublicDataAgent.registerKlineWsListener(this, kline_layout.contractKlineListener)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ll_open_long -> {
                mContract ?: return
                //开多
                openQuickOpenOrderConfirmDialog(ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG)
            }
            R.id.ll_open_short -> {
                mContract ?: return
                //开空
                openQuickOpenOrderConfirmDialog(ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT)
            }
        }
    }

    private fun openQuickOpenOrderConfirmDialog(side: Int){
        if (ContractSDKAgent.isLogin) {
            if(ContractUserDataAgent.getContractAccount(mContract!!.margin_coin) == null){
                //开通合约
                ContractOrderHelper.openCreateContractDialog(mActivity as FragmentActivity)
                return
            }
            val order = buildContractOrder(side)
            order?.let {
                ContractOrderHelper.openQuickOpenOrderConfirmDialog(
                    activity!!,
                    order,
                    mContract!!
                ) { verifyLeverage ->
                    doCommit(verifyLeverage, order)
                }
            }
        } else {
            ContractService.contractService?.doOpenLoginPage(activity!!)
        }
    }

    private fun doCommit(verifyLeverage:Boolean,order: ContractOrder) {
        showLoadingDialog()
        if(verifyLeverage){
            ContractUserDataAgent.setGlobalLeverage(order.instrument_id,order.leverage,order.position_type,object:IResponse<MutableList<GlobalLeverage>>(){
                override fun onSuccess(data: MutableList<GlobalLeverage>) {
                    //保存杠杆
                    ContractSettingUtils.setLeverage(mActivity!!,order.instrument_id,order.leverage,order.position_type)
                    doRealSubmit(order)
                }

                override fun onFail(code: String, msg: String) {
                    TopToastUtils.showFail(mActivity,msg)
                }
            })
        }else{
            doRealSubmit(order)
        }
    }

    private fun doRealSubmit(order: ContractOrder){
        ContractUserDataAgent.doSubmitOrder(order, object : IResponse<String>() {
            override fun onSuccess(data: String) {
                hideLoadingDialog()
                ToastUtil.shortToast(
                    ContractSDKAgent.context!!,
                    getString(R.string.str_order_success)
                )
            }

            override fun onFail(code: String, msg: String) {
                super.onFail(code, msg)
                hideLoadingDialog()
                ToastUtil.shortToast(ContractSDKAgent.context!!, msg)
            }

        })
    }

    /**
     * 构建订单
     */
    private fun buildContractOrder(side: Int): ContractOrder {
        val contractOrder = ContractOrder()
        contractOrder.instrument_id = mContract!!.instrument_id
        contractOrder.position_type =
            ContractSettingUtils.getLeverageType(mActivity!!, contractOrder.instrument_id)
        contractOrder.leverage = ContractOrderHelper.getDefaultLever(mActivity!!, mContract!!)
        contractOrder.side = side
        contractOrder.category = ContractOrder.ORDER_CATEGORY_NORMAL
        contractOrder.time_in_force = 2 //FOK

        when (side) {
            ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG -> {
                //开多  取卖盘第三档
                if (MathHelper.round(sellPx) <= 0.0) {
                    contractOrder.px = ticker?.last_px
                } else {
                    contractOrder.px = sellPx
                }
            }
            ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT -> {
                //开空  取买盘第三档
                if (MathHelper.round(buyPx) <= 0.0) {
                    contractOrder.px = ticker?.last_px
                } else {
                    contractOrder.px = buyPx
                }
            }
        }

        return contractOrder
    }

}