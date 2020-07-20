package com.sl.contract.library.fragment

import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.*
import com.contract.sdk.extra.Contract.AdvanceOpenCost
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.impl.ContractUserStatusListener
import com.contract.sdk.impl.IResponse
import com.sl.contract.library.utils.ContractUtils
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.flyco.tablayout.listener.OnTabSelectListener
import com.google.android.material.appbar.AppBarLayout
import com.sl.contract.library.R
import com.sl.contract.library.activity.ContractCalculateActivity
import com.sl.contract.library.activity.ContractEntrustActivity
import com.sl.contract.library.activity.ContractKlineActivity
import com.sl.contract.library.activity.ContractPositionActivity
import com.sl.contract.library.adapter.HContractHoldAdapter
import com.sl.contract.library.data.OrderPriceType
import com.sl.contract.library.helper.ContractService
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.contract.library.helper.LogicContractSetting.getExecution
import com.sl.contract.library.helper.LogicContractSetting.setExecution
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.*
import com.sl.ui.library.widget.CommonBorderInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import com.timmy.tdialog.TDialog
import kotlinx.android.synthetic.main.fragment_trade_contract.*
import kotlinx.android.synthetic.main.sl_include_contract_trade_left_layout.*
import kotlinx.android.synthetic.main.sl_include_contract_trade_right_layout.*
import org.jetbrains.anko.backgroundResource
import kotlin.math.roundToInt

/**
 * 合约交易fragment
 */
class ContractTradeFragment : BaseFragment(), View.OnClickListener,LogicContractSetting.IContractSettingListener {
    override fun setContentView(): Int {
        return R.layout.fragment_trade_contract
    }

    private var mContract: Contract? = null
    private var lastPrice = "0.00"
    private var tagPrice = "0.00"
    private var indexPrice = "0.00"

    /**
     * 订单类型 默认 开多 买
     */
    private var orderSide = ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG
    private var tabTextList = arrayOfNulls<String>(2)

    /**
     * 交易类型 0 开仓  1 平仓
     */
    private var tradeType = 0

    /**
     * 价格类型
     */
    private var priceType = OrderPriceType.CONTRACT_ORDER_LIMIT

    /**
     * 限价模式下，判断是否是高级委托
     */
    private var isAdvancedLimit = false
    var orderTypeList = ArrayList<TabInfo>()
    var orderTypeDialog: TDialog? = null

    /**
     *  0 限价委托 1 计划委托
     */
    private var tabEntrustIndex = 0
    private var priceEntrustFragment = ContractPriceEntrustFragment.newInstance()
    private var planEntrustFragment = ContractPlanEntrustFragment.newInstance()
    private var tempFragment = Fragment()

    private var volDecimal = NumberUtil.getDecimal(-1)
    private var pxDecimal = NumberUtil.getDecimal(-1)
    private var lastPxDecimal = NumberUtil.getDecimal(-1)
    private val defaultDecimal = NumberUtil.getDecimal(-1)

    /**
     * 当前杠杆
     */
    private var currLeverage = 10
    private var leverageType = 1
    private var positionAdapter: HContractHoldAdapter? = null
    private val mPositionList = ArrayList<ContractPosition>()

    //订单成交方式 1:普通,2:FOK,3:IOC
    var currAdvancedLimit = 1
    var advancedLimitTypeList = ArrayList<TabInfo>()
    var orderAdvancedDialog: TDialog? = null

    //输入量百分比
    private var volRate = 0

    private var isLoading = false

    private var orderConfirmDialog : TDialog ? = null

    override fun initView() {
        initPositionAdapter()
        initListener()
        doSwitchTradeType()
        showFragment()
        updateOrderType()
        //显示下划线
        tv_index_price.showUnderLine()
        tv_fair_price.showUnderLine()
        iv_funds_rate_value.showUnderLine()
        et_position.setEditTextChangedListener(mTextWatcher)
        et_price.setEditTextChangedListener(mTextWatcher)

        ContractSDKAgent.registerSDKUserStatusListener(this, object : ContractUserStatusListener() {
            /**
             * 合约SDK登录成功
             */
            override fun onContractLoginSuccess() {
                updateUserAssetUI()
                updateBtnText()
                //获取全局杠杆
                val pFragment = parentFragment as TabContractFragment
                pFragment.getGlobalLeverage()
            }

            /**
             * 合约SDK退出登录
             */
            override fun onContractExitLogin() {
                bt_commit.textContent = getString(R.string.str_login)
                bt_commit.isEnable(true)
                bt_commit.hideLoading()
            }

        })
    }

    private fun initPositionAdapter() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        lv_position.layoutManager = layoutManager
        positionAdapter = HContractHoldAdapter(context!!, mPositionList)
        positionAdapter?.animationEnable = true
        positionAdapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.ScaleIn)
        lv_position.adapter = positionAdapter
        positionAdapter?.setOnItemClickListener { adapter, view, position ->
            ContractPositionActivity.show(mActivity!!, mContract!!.instrument_id)
        }
    }

    private fun updateOrderType() {
        tv_order_advanced.visibility = if (isAdvancedLimit) View.VISIBLE else View.GONE
        val isPlan = priceType == OrderPriceType.CONTRACT_ORDER_PLAN
        et_trigger_price.visibility = if (isPlan) View.VISIBLE else View.GONE
        rg_trigger_type.visibility = if (isPlan) View.VISIBLE else View.GONE
        rg_order_type.visibility = if (isPlan) View.GONE else View.VISIBLE
        tv_plan_market_price.visibility = if (isPlan) View.VISIBLE else View.GONE
        et_price.inputHint =
            if (isPlan) getString(R.string.str_execution_price) else getString(R.string.str_price)
        et_price.rightText = if (isPlan) "" else mContract?.margin_coin ?: "USDT"
        et_price.requestInputView(false, isEnabled = true)
        when (priceType) {
            OrderPriceType.CONTRACT_ORDER_LIMIT -> {  //限价
            }
            OrderPriceType.CONTRACT_ORDER_MARKET -> { //市价
                et_price.inputText = getString(R.string.sl_str_market_order)

            }
            OrderPriceType.CONTRACT_ORDER_PLAN -> {//计划
                val execution = getExecution(context)
                if (execution == 0) {//限价
                    et_price.inputText = ""
                    et_price.requestInputView(true, isEnabled = true)
                } else {
                    //市价
                    et_price.inputText = ""
                    et_price.requestInputView(false, isEnabled = false)
                    et_price.inputHint = getString(R.string.str_market_price_label)
                }

                when(LogicContractSetting.getTriggerPriceType(mActivity)){
                    1 -> {
                        tab_latest_price.isChecked = true
                    }
                    2 -> {
                        tab_fair_price.isChecked = true
                    }
                    3 -> {
                        tab_index_price.isChecked = true
                    }

                }
            }
            else -> {
                if (priceType == OrderPriceType.CONTRACT_ORDER_BID_PRICE) {
                    et_price.inputText = getString(R.string.sl_str_buy1_price)
                } else {
                    et_price.inputText = getString(R.string.sl_str_sell1_price)
                }
            }

        }

        updateAboutExpectPrice()
    }

    private fun updateBtnText() {
        if (!ContractSDKAgent.isLogin) {
            bt_commit.textContent = getString(R.string.str_login)
            return
        }
        bt_commit.isEnable(true)
        if (tradeType == 0) {
            if (orderSide == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) {
                bt_commit.textContent = getString(R.string.str_buy_up)
                bt_commit.setBgColor(mainColorRise)
            } else {
                bt_commit.textContent = getString(R.string.str_buy_down)
                bt_commit.setBgColor(mainColorDown)
            }
        } else {
            if (orderSide == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG) {
                bt_commit.textContent = getString(R.string.str_sell_up)
                bt_commit.setBgColor(mainColorRise)
            } else {
                bt_commit.textContent = getString(R.string.str_sell_down)
                bt_commit.setBgColor(mainColorDown)
            }
        }
    }

    override fun loadData() {
        super.loadData()
        //价格类型
        orderTypeList.add(
            TabInfo(
                getString(R.string.str_limit_label),
                OrderPriceType.CONTRACT_ORDER_LIMIT.ordinal
            )
        )
        orderTypeList.add(
            TabInfo(
                getString(R.string.str_limit_advanced_entrust),
                OrderPriceType.CONTRACT_ORDER_ADVANCED_LIMIT.ordinal
            )
        )
        orderTypeList.add(
            TabInfo(
                getString(R.string.str_plan_entrust_label),
                OrderPriceType.CONTRACT_ORDER_PLAN.ordinal
            )
        )
        //订单类型
        initOrderTypeData()

        //高级委托设置类型
        advancedLimitTypeList.add(TabInfo(getString(R.string.str_item_post_only), 1))
        advancedLimitTypeList.add(TabInfo(getString(R.string.str_item_fok), 2))
        advancedLimitTypeList.add(TabInfo(getString(R.string.str_item_ioc), 3))
    }

    private val mTextWatcher = object :
        CommonBorderInputLayout.EditTextChangedListener {
        override fun onTextChanged(view: CommonBorderInputLayout) {
            if (view === et_price) {
                updateOpenVol()
            } else if (view === et_position) {
                updateAboutExpectPrice()
            }
        }
    }

    private fun initOrderTypeData() {
        if (tradeType == 0) {
            tabTextList[0] = getString(R.string.str_buy_up)
            tabTextList[1] = getString(R.string.str_buy_down)
            if (tab_layout != null) {
                orderSide = if (tab_layout?.currentTab == 0) {
                    ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG
                } else {
                    ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT
                }
            }
        } else {
            tabTextList[1] = getString(R.string.str_sell_down)
            tabTextList[0] = getString(R.string.str_sell_up)
            if (tab_layout != null) {
                orderSide = if (tab_layout?.currentTab == 0) {
                    ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG
                } else {
                    ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT
                }
            }
        }

    }

    /**
     * 切换交易类型
     */
    private fun doSwitchTradeType() {
        if (tradeType == 0) {
            tab_open_position.isChecked = true
            tab_close_position.isChecked = false
        } else {
            tab_open_position.isChecked = false
            tab_close_position.isChecked = true
        }
        //交易类型改变，需重新初始化订单类型的值
        initOrderTypeData()
        tab_layout.setTabData(tabTextList)
        doSwitchOrderType()
    }

    private fun doSwitchOrderType() {
        updateBtnText()
        if (tradeType == 0) {//开仓
            tv_max_amount_label.text = getString(R.string.str_max_open_vol)
            rl_entrust_value_layout.visibility = View.VISIBLE
            rl_hold_position_layout.visibility = View.GONE
            rl_available_layout.visibility = View.VISIBLE
            rl_cost_layout.visibility = View.VISIBLE
        } else {
            tv_max_amount_label.text = getString(R.string.str_max_close_vol)
            rl_hold_position_layout.visibility = View.VISIBLE
            rl_entrust_value_layout.visibility = View.GONE
            rl_available_layout.visibility = View.GONE
            rl_cost_layout.visibility = View.GONE
        }


        updateOpenVol()
        updateAboutExpectPrice()
        updateUserAssetUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        LogicContractSetting.getInstance().unregistListener(this)
    }
    private fun initListener() {
        LogicContractSetting.getInstance().registListener(this)
        iv_kline.setOnClickListener(this)
        iv_calculator.setOnClickListener(this)
        tv_plan_market_price.setOnClickListener(this)
        tab_latest_price.setOnClickListener(this)
        tab_fair_price.setOnClickListener(this)
        tab_index_price.setOnClickListener(this)
        iv_all_order.setOnClickListener(this)
        iv_cancel_all.setOnClickListener(this)
        tab_market_price.setOnClickListener(this)
        tab_buy1.setOnClickListener(this)
        tab_sell1.setOnClickListener(this)
        tab_quick25.setOnClickListener(this)
        tab_quick50.setOnClickListener(this)
        tab_quick75.setOnClickListener(this)
        tab_quick100.setOnClickListener(this)
        et_position.setInputFocusChangeListener(object :
            CommonBorderInputLayout.EditTextFocusChangeListener {
            override fun onFocusChange(view: CommonBorderInputLayout, hasFocus: Boolean) {
                if (hasFocus && volRate > 0) {
                    volRate = 0
                    et_position.inputText = ""
                    rg_position_quick.clearCheck()
                    val unit: Int = LogicContractSetting.getContractUint(mActivity)
                    //修改输入类型
                    if (unit == 0) {
                        et_position.setNumberType()
                    } else {
                        mContract?.let {
                            et_position.setMoneyType(8)
                        }
                    }
                }
            }
        })

        et_price.setInputFocusChangeListener(object :
            CommonBorderInputLayout.EditTextFocusChangeListener {
            override fun onFocusChange(view: CommonBorderInputLayout, hasFocus: Boolean) {
                if (hasFocus) {
                    when (priceType) {
                        OrderPriceType.CONTRACT_ORDER_MARKET, OrderPriceType.CONTRACT_ORDER_BID_PRICE, OrderPriceType.CONTRACT_ORDER_ASK_PRICE -> {
                            priceType = OrderPriceType.CONTRACT_ORDER_LIMIT
                            et_price.inputText = ""
                            mContract?.let {
                                et_price.setMoneyType(it.price_index-1)
                            }
                            rg_order_type.clearCheck()
                        }
                    }
                }
            }
        })
        /**
         * 开仓tab
         */
        tab_open_position.setOnClickListener {
            if (tradeType != 0) {
                tradeType = 0
                doSwitchTradeType()
            }
        }
        /**
         * 平仓tab
         */
        tab_close_position.setOnClickListener {
            if (tradeType != 1) {
                tradeType = 1
                doSwitchTradeType()
            }
        }
        /**
         *  切换仓位类型
         */
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                orderSide = if (tradeType == 0) {
                    if (index == 0) {
                        ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG
                    } else {
                        ContractOrder.CONTRACT_ORDER_WAY_SELL_OPEN_SHORT
                    }
                } else {
                    if (index == 0) {
                        ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG
                    } else {
                        ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT
                    }
                }
                doSwitchOrderType()

            }

            override fun onTabReselect(p0: Int) {
            }

        })

        /**
         * tab 限价委托和计划委托切换
         */
        rg_entrust_type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_limit_entrust -> {//普通委托
                    tabEntrustIndex = 0
                }
                R.id.rb_plan_entrust -> {//计划委托
                    tabEntrustIndex = 1
                }
            }
            showFragment()
        }
        /**
         * 下单限价、计划,高级委托切换
         */
        tv_order_type.setOnClickListener {
            showOrderTypeDialog()
        }
        /**
         * 高级委托模式选择
         */
        tv_order_advanced.setOnClickListener {
            orderAdvancedDialog = ContractOrderHelper.showOrderAdvancedTypeDialog(
                activity!!,
                advancedLimitTypeList,
                currAdvancedLimit
            ) { selectIndex: Int, showText: String ->
                currAdvancedLimit = selectIndex
                tv_order_advanced.text = showText
                orderAdvancedDialog?.dismiss()
            }
        }
        /**
         * 点击合理价
         */
        tv_fair_price.setOnClickListener {
           ContractOrderHelper.showFairPriceDialog(mActivity as FragmentActivity)
        }
        /**
         * 指数价格弹窗
         */
        tv_index_price.setOnClickListener {
            ContractOrderHelper.showIndexPriceDialog(mActivity as FragmentActivity)
        }
        /**
         * 资金费率弹窗
         */
        iv_funds_rate_value.setOnClickListener {
            DialogUtils.showBottomDialog(
                mActivity!!,
                getString(R.string.str_funds_rate),
                0,
                getString(R.string.str_funds_rate_intro),
                "",
                "",
                null
            )
        }
        /**
         * 下单操作
         */
        bt_commit.isEnable(true)
        bt_commit.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                if (!ContractSDKAgent.isLogin) {
                    ContractService.contractService?.doOpenLoginPage(activity!!)
                    return
                }
                doBuyOrSell()
            }

        }
        /**
         * 下拉刷新
         */
        refreshLayout.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
              Handler().postDelayed(Runnable {
                  if(ContractSDKAgent.isLogin){
                      ContractUserDataAgent.getContractAccounts(true)
                      mContract?.let {
                          ContractUserDataAgent.getCoinPositions(it.instrument_id,true)
                          if(tabEntrustIndex == 0){
                              ContractUserDataAgent.getContractOrder(it.instrument_id,true)
                          }else{
                              ContractUserDataAgent.getContractPlanOrder(it.instrument_id,true)
                          }
                      }
                  }else{
                      mContract?.let {
                          ContractPublicDataAgent.loadDepthFromNet(it.instrument_id,30)
                      }
                  }
                  refreshLayout.isRefreshing = false
              },2000)
            }
        })
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { p0, verticalOffset ->
            if (verticalOffset >= 0) {
                if(!refreshLayout.isEnabled){
                    refreshLayout.isEnabled = true
                }
            } else {
                if(refreshLayout.isEnabled){
                    refreshLayout.isEnabled = false
                }
            }
        })
    }

    private fun doBuyOrSell() {
        mContract?.let {
            var vol = MathHelper.round(getRealVol())
            var price = MathHelper.round(getRealPrice())
            var triggerPrice = MathHelper.round(et_trigger_price.inputText)
            //基本输入校验
            if (!ContractOrderHelper.doCommitPreBaseVerify(
                    activity!!,
                    tradeType,
                    priceType,
                    price,
                    triggerPrice,
                    vol,
                    it
                )
            ) {
                return
            }
            //合约开通情况校验
            if (!ContractOrderHelper.doPreAccountVerify(activity!!, tradeType, it)) return

            //下单二次确认弹窗开关
            val tradeConfirm = PreferenceManager.getInstance(context)
                .getSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, true)
            price = getRealPrice().toDouble()
            //构建订单
            val order = buildContractOrder(price, vol, it)
            order?.let {
                if (tradeConfirm) {
                    //弹出确认对话框
                    showBuyOrSellDialog(order)
                } else {
                    interBuyOrSell(order)
                }
            }
        }
    }

    private fun buildContractOrder(
        price: Double,
        vol: Double,
        it: Contract
    ): ContractOrder {
        val order = ContractOrder()
        if(priceType == OrderPriceType.CONTRACT_ORDER_PLAN){
            order.exec_px = price.toString()
            order.px = MathHelper.round(et_trigger_price.inputText).toString()
        }else{
            order.px = price.toString()
        }
        order.qty = vol.toString()
        order.leverage = currLeverage
        order.position_type = leverageType
        order.side = orderSide
        ContractOrderHelper.buildContractOrder(
            activity!!,
            it,
            tradeType,
            priceType,
            currLeverage,
            isAdvancedLimit,
            currAdvancedLimit,
            order
        )
        return order
    }

    /**
     * 展示确认对话框
     */
    private fun showBuyOrSellDialog(order: ContractOrder) {
        //警告提示
        var warning = getOrderWarningText(order)
        orderConfirmDialog = ContractOrderHelper.openOrderConfirmDialog(activity!!, order,priceType == OrderPriceType.CONTRACT_ORDER_PLAN,warning=warning) { tradeConfirm: Boolean ->
            PreferenceManager.getInstance(context)
                .putSharedBoolean(PreferenceManager.PREF_TRADE_CONFIRM, tradeConfirm)
            interBuyOrSell(order)
        }

    }

    /**
     * 获得订单警告文本
     */
    private fun getOrderWarningText(order: ContractOrder): String {
        var warning = ""
        val isBuy = order.side < ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG
        if (priceType == OrderPriceType.CONTRACT_ORDER_LIMIT) {
            val tipLimit: Double = if (isBuy) {
                MathHelper.div(MathHelper.sub(order.px, lastPrice), MathHelper.round(lastPrice))
            } else {
                MathHelper.div(MathHelper.sub(lastPrice, order.px), MathHelper.round(lastPrice))
            }
            if (0.05 < tipLimit) {
                warning = String.format(
                    getString(R.string.str_open_close_risk_tips),
                    if (tradeType == 0) getString(R.string.str_open_price) else getString(R.string.str_close_position_price)
                )
            }
        } else if (priceType == OrderPriceType.CONTRACT_ORDER_MARKET) {
            val depthDataList = if (isBuy) {
                ll_buy_layout.getData()
            } else {
                ll_sell_Layout.getData()
            }
            val avgPrice =
                ContractCalculate.CalculateMarketAvgPrice(order.qty, depthDataList, !isBuy)
            val tipLimit: Double = if (isBuy) {
                MathHelper.div(
                    MathHelper.sub(avgPrice, MathHelper.round(lastPrice)),
                    MathHelper.round(lastPrice)
                )
            } else {
                MathHelper.div(
                    MathHelper.sub(MathHelper.round(lastPrice), avgPrice),
                    MathHelper.round(lastPrice)
                )
            }
            if (0.03 < tipLimit) {
                warning = String.format(
                    getString(R.string.str_open_close_risk_tips),
                    if (tradeType == 0) getString(R.string.str_about_open_avg_price) else getString(
                        R.string.str_about_close_avg_price
                    )
                )
            }
        }
        return warning
    }


    private fun interBuyOrSell(order: ContractOrder) {
        mContract?.let {
            order.nonce = System.currentTimeMillis() / 1000
            bt_commit.showLoading()
            val response: IResponse<String> = object : IResponse<String>() {
                override fun onSuccess(data: String) {
                    bt_commit.hideLoading()
                    TopToastUtils.showSuccess(activity!!, getString(R.string.str_order_success))
                }

                override fun onFail(code: String, msg: String) {
                    bt_commit.hideLoading()
                    //处理全局杠杆逻辑
                    if(TextUtils.equals(code,"CONTRACT_LEVERAGE_MATCH_ERROR")){
                        TopToastUtils.showTopFailToast(activity!!, msg)
                        val pFragment = parentFragment as TabContractFragment
                        pFragment.getGlobalLeverage()
                    }else{
                        TopToastUtils.showTopFailToast(activity!!, msg)
                    }
                }
            }

            if (priceType == OrderPriceType.CONTRACT_ORDER_PLAN) {
                ContractUserDataAgent.doSubmitPlanOrder(order, response)
            } else {
                ContractUserDataAgent.doSubmitOrder(order, response)
            }
        }

    }


    private fun showOrderTypeDialog() {
        orderTypeDialog = DialogUtils.showListDialog(
            context!!,
            orderTypeList,
            if (isAdvancedLimit) OrderPriceType.CONTRACT_ORDER_ADVANCED_LIMIT.ordinal else priceType.ordinal,
            object : DialogUtils.DialogOnItemClickListener {
                override fun clickItem(item: Int) {
                    tv_order_type?.text = orderTypeList[item].name
                    orderTypeDialog?.dismiss()
                    if (orderTypeList[item].index == OrderPriceType.CONTRACT_ORDER_ADVANCED_LIMIT.ordinal) {//如果选择高级委托,priceType 按照普通限价类型走逻辑
                        isAdvancedLimit = true
                        priceType = OrderPriceType.CONTRACT_ORDER_LIMIT
                        //高级委托，市价单置灰
                        tab_market_price.isEnabled = false
                    } else {
                        tab_market_price.isEnabled = true
                        isAdvancedLimit = false
                        priceType = OrderPriceType.values()[orderTypeList[item].index]
                    }
                    updateOrderType()
                }
            })
    }



    /**
     * 绑定合约
     */
    fun bindContract(contract: Contract, resetData: Boolean = false) {
        mContract = contract
        volDecimal = NumberUtil.getDecimal(contract.value_index)
        pxDecimal = NumberUtil.getDecimal(contract.price_index)
        lastPxDecimal =  NumberUtil.getDecimal(contract.price_index-1)
        //合约深度
        if (resetData) {
            ll_buy_layout?.initData(null)
            ll_sell_Layout?.initData(null)
        }
        ll_buy_layout?.bindContract(contract)
        ll_sell_Layout?.bindContract(contract)

        updatePositionData()
        setVolUnit()
        updateAboutExpectPrice()

        priceEntrustFragment?.bindContract(mContract!!)
        planEntrustFragment?.bindContract(mContract!!)
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

        //仓位更新  须更新可开可平 和 用户资产
        updateOpenVol()
        updateUserAssetUI()
    }

    private fun getRealVol(): String {
        return if (volRate == 0) {
            et_position.inputText
        } else {
            if (ContractSDKAgent.isLogin) {
                val tag: Double = tv_max_amount.tag as Double
                val unit: Int = LogicContractSetting.getContractUint(mActivity)
                if (unit == 0) {
                    MathHelper.div(MathHelper.mul(volRate.toDouble(), tag), 100.0).roundToInt()
                        .toString()
                } else {
                    MathHelper.div(MathHelper.mul(volRate.toDouble(), tag), 100.0).toString()
                }
            } else {
                "0"
            }
        }
    }

    /**
     * 更新预计价格
     */
    private fun updateAboutExpectPrice() {
        if (et_price == null) {
            return
        }
        mContract?.let {
            var price = getRealPrice()
            var vol = getRealVol()
            if (TextUtils.isEmpty(price) || price == ".") {
                price = "0.00"
            }
            if (TextUtils.isEmpty(vol)) {
                vol = "0"
            }

            val value: String = ContractUtils.CalculateContractBasicValue(
                ContractCalculate.trans2ContractVol(
                    it,
                    vol,
                    price,
                    LogicContractSetting.getContractUint(mActivity)
                ),
                price,
                it
            )
            et_position.inputHelperHint = "≈ $value"

            //预估成本
            val buyCost = calculateAdvanceOpenCost(vol)
            if (buyCost == null) {
                tv_cost.text = "--"
            } else {
                tv_cost.text = " " + volDecimal.format(
                    MathHelper.round(
                        buyCost.freezAssets,
                        it.value_index
                    )
                ) + " " + it.margin_coin
            }

            //委托价值
            if (tradeType == 0) {
                val contractValue = ContractCalculate.CalculateContractValue(vol, price, it)
                tv_entrust_value.text = volDecimal.format(contractValue) + " " + it.margin_coin
            }

        }
    }

    /**
     * 更新用户资产
     */
    fun updateUserAssetUI() {
        if (tv_available == null) {
            return
        }
        mContract?.let {
            val marginCoin = it.margin_coin
            val contractAccount = ContractUserDataAgent?.getContractAccount(marginCoin)
            if (contractAccount == null) {
                tv_available.text = "0 $marginCoin"
                return
            }
            val available = contractAccount.available_vol_real
            tv_available.text = volDecimal.format(available) + " " + marginCoin
        }
    }


    /**
     * 更新可开可平数量
     */
     fun updateOpenVol() {
        if (et_price == null) {
            return
        }
        mContract?.let {
            var price = getRealPrice()
            if (TextUtils.isEmpty(price) || price == ".") {
                price = "0.00"
            }

            if (!ContractSDKAgent.isLogin) {
                tv_max_amount.text = "--"
                tv_entrust_value.text = "--"
                tv_cost.text = "--"
                tv_available.text = "--"
                tv_max_amount.tag = 0.0
                return
            }
            var longPosition: ContractPosition? = null
            if (tradeType == 1) {//平仓
                //平仓没有成本和可用
                longPosition = ContractUserDataAgent.getContractPosition(
                    it.instrument_id,
                    if (orderSide == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG) ContractPosition.POSITION_TYPE_LONG else ContractPosition.POSITION_TYPE_SHORT
                )
                var avail = 0.0
                if (longPosition != null) {
                    avail = MathHelper.sub(longPosition.cur_qty, longPosition.freeze_qty)
                    //持仓
                    tv_hold_position_value.text =
                        ContractUtils.getVolUnit(context!!, it, longPosition.cur_qty, price)
                } else {
                    tv_hold_position_value.text = "--"
                }
                val closeMax =
                    ContractUtils.getVolNoUnit(context!!, it, avail, MathHelper.round(price))
                //最大可平
                tv_max_amount.text =
                    if (LogicContractSetting.getContractUint(context) == 0) closeMax + context!!.getString(
                        R.string.str_vol_unit
                    ) else closeMax + it.base_coin
                tv_max_amount.tag = closeMax.toDouble()
                bt_commit.isEnable(avail > 0)
            } else {
                //开仓
                val contractAccount = ContractUserDataAgent?.getContractAccount(it.margin_coin)
                //计算最大可开
                var openVol = 0.0
               val trendType = if(orderSide == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG){
                   ContractPosition.POSITION_TYPE_LONG
                }else{
                   ContractPosition.POSITION_TYPE_SHORT
                }
                if (contractAccount != null) {
                    openVol = ContractCalculate.CalculateVolume(
                        volDecimal.format(contractAccount.available_vol_real),
                        currLeverage,
                        ContractUserDataAgent?.getContractOrderSize(
                            it.instrument_id,
                            orderSide
                        ),
                        ContractUserDataAgent.getContractPosition(it.instrument_id, trendType),
                        price,
                        trendType,
                        it
                    )
                }
                //最大可开
                tv_max_amount.tag = openVol
                tv_max_amount.text =
                    ContractUtils.getVolUnit(context!!, it, openVol, MathHelper.round(price))
            }
        }
    }

    /**
     * 计算预估成本
     */
    private fun calculateAdvanceOpenCost(vol: String): AdvanceOpenCost? {
        mContract?.let {
            val contractOrder = ContractOrder()
            contractOrder.leverage = currLeverage
            contractOrder.qty = vol
            contractOrder.position_type = leverageType
            contractOrder.side = orderSide
            contractOrder.px = getRealPrice()
            if (priceType == OrderPriceType.CONTRACT_ORDER_LIMIT) {//限价
                contractOrder.category = ContractOrder.ORDER_CATEGORY_NORMAL
            } else if (priceType == OrderPriceType.CONTRACT_ORDER_MARKET) {//市价
                contractOrder.category = ContractOrder.ORDER_CATEGORY_MARKET
            } else if (priceType == OrderPriceType.CONTRACT_ORDER_BID_PRICE) {
                contractOrder.category = ContractOrder.ORDER_CATEGORY_NORMAL
            } else if (priceType == OrderPriceType.CONTRACT_ORDER_ASK_PRICE) {
                contractOrder.category = ContractOrder.ORDER_CATEGORY_NORMAL
            }

            return ContractCalculate.CalculateAdvanceOpenCost(
                contractOrder,
                ContractUserDataAgent?.getContractPosition(
                    it.instrument_id,
                    if (orderSide == ContractOrder.CONTRACT_ORDER_WAY_BUY_OPEN_LONG) ContractPosition.POSITION_TYPE_LONG else ContractPosition.POSITION_TYPE_SHORT
                ),
                ContractUserDataAgent?.getContractOrderSize(
                    it.instrument_id,
                    orderSide
                ),
                mContract
            )
        }
        return null
    }

    /**
     * 1、普通委托，委托价格为市价时，计算最大可开和成本时使用最新价格；
    2、计划委托，执行价格为限价，计算预估成本和最大可开张数时使用设置的执行价格；
    3、计划委托，执行价格为市价，计算预估成本和最大可开张数时使用设置的触发价格。
     */
    private fun getRealPrice(): String {
        var price = "0.00"
        if (priceType == OrderPriceType.CONTRACT_ORDER_MARKET) {//普通市价->最新价
            price = lastPrice
        } else if (priceType == OrderPriceType.CONTRACT_ORDER_BID_PRICE) {//普通买一价
            val list = ll_buy_layout?.getData()
            price = if (list != null && list.size > 0) {
                list[0].price
            } else {
                lastPrice
            }
        } else if (priceType == OrderPriceType.CONTRACT_ORDER_ASK_PRICE) {//普通卖一价
            val list = ll_sell_Layout?.getData()
            price = if (list != null && list.size > 0) {
                list[0].price
            } else {
                lastPrice
            }
        } else if (priceType == OrderPriceType.CONTRACT_ORDER_LIMIT) {//普通限价-输入框
            price = et_price.inputText
            if (TextUtils.isEmpty(price)) {
                price = tagPrice//输入框为空取合理价
            }
        } else if (priceType == OrderPriceType.CONTRACT_ORDER_PLAN) {//计划委托
            price = if (getExecution(mActivity) == 0) {//限价->执行价格
                price = et_price.inputText.trim()
                if (TextUtils.isEmpty(price)) tagPrice else price
            } else {//市价->触发价格
                price = et_trigger_price.inputText.trim()
                if (TextUtils.isEmpty(price)) tagPrice else price
            }
        }
        return price
    }

    /**
     *  更新量和单位
     */
    private fun setVolUnit() {
        mActivity ?: return
        val unit: Int = LogicContractSetting.getContractUint(mActivity)
        //修改输入类型
        if (unit == 0) {
            et_position.setNumberType()
            et_position.rightText = getString(R.string.str_vol_unit)
        } else {
            et_position.setMoneyType(8)
            et_position.rightText = mContract?.base_coin ?: ""
        }
        //价格输入精度
        et_price.setMoneyType(mContract!!.price_index-1)
        et_trigger_price.setMoneyType(mContract!!.price_index-1)
        et_price.rightText = mContract?.margin_coin ?: ""
        et_trigger_price.rightText = mContract?.margin_coin ?: ""
    }


    fun bindBuyDepthData(listList: ArrayList<DepthData>) {
        ll_buy_layout?.initData(listList)
    }

    fun bindSellDepthData(listList: ArrayList<DepthData>) {
        ll_sell_Layout?.initData(listList)
    }


    /**
     * 更新合约Ticker
     */
    fun updateContractTicker(ticker: ContractTicker, updatePrice: Boolean = true) {
        if (!isVisible || tv_index_price == null) {
            return
        }
        mContract?.let {
            var lastPx = lastPxDecimal.format(MathHelper.round(ticker.last_px))

            if (!TextUtils.equals(lastPx, lastPrice)) {
                lastPrice = lastPx
                //如果是市价，需重新计算可开数量
                if (priceType == OrderPriceType.CONTRACT_ORDER_MARKET) {
                    updateOpenVol()
                }
            }

            tagPrice = pxDecimal.format(MathHelper.round(ticker.fair_px))
            indexPrice = pxDecimal.format(MathHelper.round(ticker.index_px))

            if (updatePrice) {
                val current: Double = MathHelper.round(ticker.last_px, it.price_index)
                et_price?.inputText = lastPxDecimal.format(current)
            }
            //指数价格
            tv_index_price.text = indexPrice
            //合理价格
            tv_fair_price.text = tagPrice
            //最新价格
            tv_last_price.text = lastPrice
            //资金费率
            val rate = MathHelper.mul(ticker.funding_rate, "100")
            iv_funds_rate_value.text =
                NumberUtil.getDecimal(-1).format(MathHelper.round(rate, 4)).toString() + "%"

            val riseFallRate: Double = MathHelper.round(ticker.change_rate.toDouble() * 100, 2)
            if (riseFallRate >= 0) {
                tv_last_price.setTextColor(mainColorRise)
                tv_rate.text = "+$riseFallRate%"
                tv_rate.backgroundResource = R.drawable.sl_border_green_fill
            } else {
                tv_last_price.setTextColor(mainColorDown)
                tv_rate.text = "$riseFallRate%"
                tv_rate.backgroundResource = R.drawable.sl_border_red_fill
            }
            //Ticker变化 须更新仓位
            if (ContractSDKAgent.isLogin) {
                positionAdapter?.notifyDataChanged(false)
            }
            //如果打开下单弹窗，需更新最新价格
            if (priceType == OrderPriceType.CONTRACT_ORDER_PLAN){
                orderConfirmDialog?.let { tDialog ->
                    if (tDialog.dialog?.isShowing == true){
                        tDialog.dialog?.findViewById<TextView>(R.id.tv_last_price)?.text = lastPrice
                    }else{
                        orderConfirmDialog = null
                    }
                }
            }

        }
    }

    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        tempFragment = if (tabEntrustIndex == 0) {
            if (!priceEntrustFragment.isAdded) {
                transaction.hide(tempFragment).add(
                    R.id.item_fragment_entrust_container,
                    priceEntrustFragment,
                    tabEntrustIndex.toString()
                )
            } else {
                transaction.hide(tempFragment).show(priceEntrustFragment)
            }
            priceEntrustFragment
        } else {
            if (!planEntrustFragment.isAdded) {
                transaction.hide(tempFragment).add(
                    R.id.item_fragment_entrust_container,
                    planEntrustFragment,
                    tabEntrustIndex.toString()
                )
            } else {
                transaction.hide(tempFragment).show(planEntrustFragment)
            }
            planEntrustFragment
        }
        transaction.commitNow()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_all_order -> {
                if (ContractSDKAgent.isLogin) {
                    mContract?.let {
                        ContractEntrustActivity.show(activity!!, it.instrument_id)
                    }
                } else {
                    ContractService.contractService?.doOpenLoginPage(activity!!)
                }

            }
            R.id.tab_latest_price -> {
                //最新价
                LogicContractSetting.setTriggerPriceType(mActivity, 1)
                updateAboutExpectPrice()
            }
            R.id.tab_fair_price -> {
                //合理价
                LogicContractSetting.setTriggerPriceType(mActivity, 2)
                updateAboutExpectPrice()
            }
            R.id.tab_index_price -> {
                //指数价
                LogicContractSetting.setTriggerPriceType(mActivity, 4)
                updateAboutExpectPrice()
            }
            R.id.tab_market_price -> {
                //市单价
                if (priceType != OrderPriceType.CONTRACT_ORDER_MARKET) {
                    priceType = OrderPriceType.CONTRACT_ORDER_MARKET
                    et_price.setNumberType()
                    et_price.requestInputView(false)
                    updateOrderType()
                }
            }
            R.id.tab_buy1 -> {
                //买一价
                if (priceType != OrderPriceType.CONTRACT_ORDER_BID_PRICE) {
                    priceType = OrderPriceType.CONTRACT_ORDER_BID_PRICE
                    et_price.setNumberType()
                    et_price.requestInputView(false)
                    updateOrderType()
                }
            }
            R.id.tab_sell1 -> {
                //卖一价
                if (priceType != OrderPriceType.CONTRACT_ORDER_ASK_PRICE) {
                    priceType = OrderPriceType.CONTRACT_ORDER_ASK_PRICE
                    et_price.setNumberType()
                    et_price.requestInputView(false)
                    updateOrderType()
                }
            }
            R.id.tab_quick25 -> {
                volRate = 25
                et_position.inputText = "25%"
                et_position.requestInputView(false)
                et_position.getEtInputView().isCursorVisible = false
                et_position.setNumberType()
            }
            R.id.tab_quick50 -> {
                volRate = 50
                et_position.inputText = "50%"
                et_position.requestInputView(false)
                et_position.getEtInputView().isCursorVisible = false
                et_position.setNumberType()
            }
            R.id.tab_quick75 -> {
                volRate = 75
                et_position.inputText = "75%"
                et_position.requestInputView(false)
                et_position.getEtInputView().isCursorVisible = false
                et_position.setNumberType()
            }
            R.id.tab_quick100 -> {
                volRate = 100
                et_position.inputText = "100%"
                et_position.requestInputView(false)
                et_position.getEtInputView().isCursorVisible = false
                et_position.setNumberType()
            }
            R.id.tv_plan_market_price -> {
                //点击市价
                tv_plan_market_price.isSelected = !tv_plan_market_price.isSelected
                priceType = OrderPriceType.CONTRACT_ORDER_PLAN
                if (getExecution(context) == 1) {
                    setExecution(context, 0)
                } else {
                    setExecution(context, 1)
                }
                updateOrderType()
            }
            R.id.iv_calculator -> {
                //计算器
                mContract?.let {
                    ContractCalculateActivity.show(mActivity!!, it.instrument_id)
                }
            }
            R.id.iv_kline ->{
                //K线
                mContract?.let {
                    ContractKlineActivity.show(mActivity!!, it.instrument_id)
                }
            }
            R.id.iv_cancel_all -> {
                if (ContractSDKAgent.isLogin) {
                    mContract?.let {
                        var orderList = ArrayList<ContractOrder>()
                        if(tabEntrustIndex == 0){
                            //普通
                            orderList.addAll(ContractUserDataAgent.getContractOrder(it.instrument_id))
                        }else{
                            //计划
                            orderList.addAll(ContractUserDataAgent.getContractPlanOrder(it.instrument_id))
                        }
                        if(orderList.size > 0){
                            DialogUtils.showCenterDialog(activity!!,content = getString(R.string.str_cancel_all_order),clickListener=object:
                                DialogUtils.DialogBottomListener{
                                override fun clickTab(tabType: Int) {
                                    if (tabType == 1){
                                        doCancelAllOrders(it.instrument_id, orderList)
                                    }
                                }

                            })
                        }
                    }
                }else{
                    ContractService.contractService?.doOpenLoginPage(activity!!)
                }
            }
        }
    }

    /**
     * 取消所有订单
     */
    private fun doCancelAllOrders(instrument_id: Int,orderList:ArrayList<ContractOrder>){
        val orders = ContractOrders()
        orders.contract_id = instrument_id
        orders.orders = orderList

        val response = object:IResponse<MutableList<Long>>(){
            override fun onSuccess(data: MutableList<Long>) {
                hideLoadingDialog()
                ToastUtil.shortToast(ContractSDKAgent.context!!,getString(R.string.str_cancel_success))
            }

            override fun onFail(code: String, msg: String) {
                super.onFail(code, msg)
                hideLoadingDialog()
                ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
            }

        }
        showLoadingDialog()
        if(tabEntrustIndex == 0){
            ContractUserDataAgent.doCancelOrders(orders,response)
        }else{
            ContractUserDataAgent.doCancelPlanOrders(orders,response)
        }
    }

    override fun onContractSettingChange() {
        updateOrderType()
        setVolUnit()
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.contract_select_leverage_event -> {
                currLeverage = event.msg_content as Int
                leverageType = ContractSettingUtils.getLeverageType(context!!,mContract!!.instrument_id)
                updateAboutExpectPrice()
                updateOpenVol()
            }
            MessageEvent.SELECT_DEPTH_PRICE_notify -> {
                val price = event.msg_content.toString()
                if(priceType == OrderPriceType.CONTRACT_ORDER_PLAN){
                    //计划委托更新触发价格
                    et_trigger_price.inputText = price
                }else{
                    et_price.inputText = price
                }
            }
        }
    }

}