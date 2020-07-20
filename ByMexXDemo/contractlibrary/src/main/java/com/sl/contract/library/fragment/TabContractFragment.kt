package com.sl.contract.library.fragment

import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.data.*
import com.contract.sdk.impl.*
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.activity.SelectLeverageActivity
import com.sl.contract.library.dialog.ContractCoinSearchDialog
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.helper.ContractService
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.contract.library.utils.ContractSettingUtils
import com.sl.contract.library.widget.SlDialogHelper
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.*
import kotlinx.android.synthetic.main.fragment_tab_contract.*
import java.util.ArrayList


/**
 * 合约
 */
class TabContractFragment : BaseFragment() {
    private val TAG = "SL_SDK"
    override fun setContentView(): Int {
        return R.layout.fragment_tab_contract
    }

    //合约id
    private var mContractId = 0
    var mContract: Contract? = null
    var mTicker: ContractTicker? = null

    //是否是闪电模式
    var isQuickTrade = true
    var tradeFragment = ContractTradeFragment()
    var tradeQuickFragment = ContractQuickTradeFragment()
    var currFragment = Fragment()

    //杠杆倍数
    var minLeverage = 1
    var maxLeverage = 100
    var currLeverage = 10
    var leverageType = 1


    override fun loadData() {
        registerSDKListener()
        registerTickerWsListener()
        registerDepthWsListener()
        registerAccountWsListener()
        registerPositionWsListener()
        /**
         * 初始化杠杆数据
         */
        initLeverageData()
        /**
         * 从sdk加载数据 并初始化UI
         */
        initDataFromSdk()
    }

    private fun registerSDKListener() {
        ContractSDKAgent.registerContractSDKListener(object : ContractSDKListener() {
            /**
             * 合约SDK初始化成功
             */
            override fun sdkInitSuccess() {
                initDataFromSdk()
                doSwitchContract(ContractPublicDataAgent.getContractTicker(mContractId))
            }

        })
    }


    override fun initView() {
        isQuickTrade = LogicContractSetting.getContractModeSetting(context) == 1
        initListener()
        showFragment()
        //showLoadingDialog()
        Handler().postDelayed({
            doSwitchContract(ContractPublicDataAgent.getContractTicker(mContractId))
            updateModeUI()
            // hideLoadingDialog()
        }, 100)
    }


    /**
     * 注册仓位监听
     */
    private fun registerPositionWsListener() {
        ContractUserDataAgent.registerContractPositionWsListener(this,
            object : ContractPositionListener() {
                override fun onWsContractPosition(instrument_id: Int?) {
                    if (isQuickTrade) {
                        tradeQuickFragment?.updatePositionData()
                    } else {
                        tradeFragment?.updatePositionData()
                    }
                }

            })
    }

    /**
     * 注册合约账户数据监听
     */
    private fun registerAccountWsListener() {
        ContractUserDataAgent.registerContractAccountWsListener(this,
            object : ContractAccountListener() {
                override fun onWsContractAccount(contractAccount: ContractAccount?) {
                    if (isQuickTrade) {

                    } else {
                        tradeFragment?.updateOpenVol()
                        tradeFragment?.updateUserAssetUI()
                    }
                }
            })
    }

    /**
     * 注册Ticker的ws监听
     */
    private fun registerTickerWsListener() {
        ContractPublicDataAgent.registerTickerWsListener(this, object : ContractTickerListener() {
            override fun onWsContractTicker(ticker: ContractTicker) {
                if (!isVisible) {
                    return
                }
                if (ticker.instrument_id == mContractId) {
                    mTicker = ticker
                    if (isQuickTrade) {
                        tradeQuickFragment?.updateTicker(ticker)
                    } else {
                        tradeFragment?.updateContractTicker(ticker, false)
                    }
                }
            }
        })
    }

    /**
     * 注册深度数据监听
     */
    private fun registerDepthWsListener() {
        ContractPublicDataAgent.registerDepthWsListener(this, 6, object : ContractDepthListener() {
            override fun onWsContractDepth(
                contractId: Int,
                buyList: ArrayList<DepthData>,
                sellList: ArrayList<DepthData>
            ) {
                if (contractId == mContractId) {
                    if (isQuickTrade) {
                        var buyPx = mTicker?.last_px ?: "0.00"
                        var sellPx = mTicker?.last_px ?: "0.00"
                        if (buyList.size >= 3) {
                            buyPx = buyList[2].price
                        }
                        if (sellList.size >= 3) {
                            sellPx = sellList[2].price
                        }
                        tradeQuickFragment?.updateDepthPrice(buyPx, sellPx)
                    } else {
                        tradeFragment?.bindBuyDepthData(buyList)
                        tradeFragment?.bindSellDepthData(sellList)
                    }

                }
            }

        })
    }


    private fun initListener() {
        /**
         * 侧边栏
         */
        tv_contract?.setOnClickListener {
            showLeftCoinWindow()
        }
        /**
         * 更多
         */
        iv_more?.setOnClickListener {
            if (mContractId > 0) {
                SlDialogHelper.openContractSetting(mActivity, iv_more, mContractId) { tabIndex ->
                    when (tabIndex) {
                        6 -> {
                            //打开模式切换
                            openTreadModeSwitch()
                        }
                    }


                }
            }
        }
        /**
         * 切换杠杆
         */
        tv_lever?.setOnClickListener {
            if (mContractId > 0) {
                if (isQuickTrade) {
                    openTreadModeSwitch()
                } else {
                    if (ContractSDKAgent.isLogin) {
                        val canSettingLever =
                            ContractUserDataAgent.getContractPlanOrder(mContractId).size == 0
                                    && ContractUserDataAgent.getContractOrder(mContractId).size == 0
                                    && ContractUserDataAgent.getCoinPositions(mContractId).size == 0
                        if (canSettingLever) {
                            SelectLeverageActivity.show(activity!!, mContractId, currLeverage)
                        } else {
                            DialogUtils.showCenterDialog(
                                context = activity!!,
                                content = getString(R.string.str_set_global_lever_tips),
                                cancelTitle = "",
                                sureTitle = getString(R.string.common_text_btn_i_see),
                                clickListener = null
                            )
                        }
                    } else {
                        ContractService.contractService?.doOpenLoginPage(mActivity!! as FragmentActivity)
                    }
                }
            }
        }
    }

    private fun openTreadModeSwitch() {
        SlDialogHelper.openTreadModeSwitch(mActivity) { isQuick ->
            this.isQuickTrade = isQuick
            showFragment()
            updateModeUI()
            doSwitchContract(ContractPublicDataAgent.getContractTicker(mContractId))
        }
    }

    private fun initLeverageData() {
        minLeverage = 1
        maxLeverage = 100
        currLeverage = 10

        mContract?.let {
            minLeverage = it.min_leverage!!.toInt()
            maxLeverage = it.max_leverage!!.toInt()
            currLeverage = ContractOrderHelper.getDefaultLever(mActivity!!, it)
            leverageType = ContractSettingUtils.getLeverageType(mActivity!!, it.instrument_id)
        }
    }

    /**
     * 获取全局杠杆
     */
    fun getGlobalLeverage() {
        if (ContractSDKAgent.isLogin) {
            mContract?.let { it ->
                ContractUserDataAgent.loadGlobalLeverage(it.instrument_id,
                    object : IResponse<MutableList<GlobalLeverage>>() {
                        override fun onSuccess(data: MutableList<GlobalLeverage>) {
                            if (data.isNullOrEmpty()) {
                                // 如果为空，默认设置全仓杠杆倍数
                                ContractUserDataAgent.setGlobalLeverage(
                                    it.instrument_id,
                                    currLeverage,
                                    leverageType,
                                    null
                                )
                                return
                            }
                            val item = data[0]
                            currLeverage = item.config_value.toInt()
                            leverageType = item.position_type
                            //保存
                            ContractSettingUtils.setLeverage(
                                ContractSDKAgent.context!!,
                                it.instrument_id,
                                currLeverage,
                                leverageType
                            )
                            //更新UI
                                updateLeverUI()
                        }

                        override fun onFail(code: String, msg: String) {
                            mActivity?.let { activity ->
                                TopToastUtils.showFail(activity, msg)
                            }
                        }
                    })
            }
        }
    }

    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        if (isQuickTrade) {
            if (!tradeQuickFragment.isAdded) {
                transaction.hide(currFragment)
                    .add(R.id.fragment_container, tradeQuickFragment, "2")
            } else {
                transaction.hide(currFragment).show(tradeQuickFragment)
            }
            currFragment = tradeQuickFragment
        } else {
            if (!tradeFragment.isAdded) {
                transaction.hide(currFragment)
                    .add(R.id.fragment_container, tradeFragment, "1")
            } else {
                transaction.hide(currFragment).show(tradeFragment)
            }
            currFragment = tradeFragment
        }
        transaction.commitNowAllowingStateLoss()
    }

    private fun showLeftCoinWindow() {
        if (DisplayUtils.isFastClick)
            return

        val searchDialog = ContractCoinSearchDialog()
        searchDialog.showDialog(childFragmentManager, "SlContractFragment", mContractId)
    }


    /**
     * 从SDK获取数据
     */
    private fun initDataFromSdk() {
        val contractList = ContractPublicDataAgent.getContracts()
        if (contractList.isNotEmpty()) {
            if (mContract == null) {
                //默认取第0个
                mContract = contractList[0]
                mContractId = mContract?.instrument_id ?: 0
            }
        }
    }

    /**
     * 切换合约
     */
    private fun doSwitchContract(ticker: ContractTicker?) {
        ticker?.let {
            //订阅ticker
            ContractPublicDataAgent.subscribeTickerWs(mContractId)
            updateHeaderView(it)
            if (isQuickTrade) {
                updateQuickContractUi(it, false)
            } else {
                updateContractUi(it, true)
            }
            //订阅深度
            ContractPublicDataAgent.subscribeDepthWs(mContractId)

            //切换合约 获取全局杠杆
            getGlobalLeverage()
        }
    }

    private fun updateQuickContractUi(ticker: ContractTicker, resetData: Boolean) {
        mContract?.let {
            tradeQuickFragment.bindContract(it, resetData)
        }
    }

    private fun updateContractUi(ticker: ContractTicker, resetData: Boolean = false) {
        mContract?.let {
            tradeFragment?.bindContract(it, resetData)
            //杠杆
            initLeverageData()
            updateLeverUI()
        }

        tradeFragment?.updateContractTicker(ticker, true)

    }


    /**
     * 更新头部View
     */
    private fun updateHeaderView(ticker: ContractTicker) {
        if (tv_contract == null) {
            return
        }
        //合约名称
        tv_contract.text = ticker.symbol
    }

    /**
     * 更新杠杆UI
     */
    private fun updateLeverUI() {
        if(isQuickTrade){
            return
        }
        if (leverageType == 1) {
            tv_lever.text = getString(R.string.sl_str_gradually_position) + currLeverage + "X"
        } else {
            tv_lever.text = getString(R.string.sl_str_full_position) + currLeverage + "X"
        }
    }

    private fun updateModeUI() {
        if (isQuickTrade) {
            tv_lever.text = getString(R.string.str_quick_mode)
        } else {
            updateLeverUI()
        }
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.sl_contract_left_coin_type -> {
                if (event.msg_content is ContractTicker) {
                    SDKLogUtil.d(ContractSDKAgent.sTAG, "--切换合约--" + event.msg_content)
                    val contractTicker = event.msg_content as ContractTicker
                    if (contractTicker.instrument_id != mContractId) {
                        //TODO 取消原来订阅
                        mContractId = contractTicker.instrument_id
                        mContract = ContractPublicDataAgent.getContract(mContractId)
                        //切换合约
                        doSwitchContract(contractTicker)
                    }
                }
            }
            MessageEvent.contract_select_leverage_event -> {
                currLeverage = event.msg_content as Int
                leverageType =
                    ContractSettingUtils.getLeverageType(context!!, mContract!!.instrument_id)
                updateLeverUI()
            }
        }

    }


}