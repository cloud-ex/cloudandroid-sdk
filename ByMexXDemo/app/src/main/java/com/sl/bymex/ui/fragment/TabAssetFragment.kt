package com.sl.bymex.ui.fragment

import androidx.fragment.app.Fragment
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.impl.ContractAccountListener
import com.sl.contract.library.utils.ContractUtils
import com.contract.sdk.utils.MathHelper
import com.sl.bymex.R
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.SpotAssetHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.ui.activity.asset.AssetTransferActivity
import com.sl.bymex.ui.activity.asset.DepositActivity
import com.sl.bymex.ui.activity.asset.FundsFlowActivity
import com.sl.bymex.ui.activity.asset.WithdrawActivity
import com.sl.bymex.ui.fragment.asset.SpotAssetFragment
import com.sl.contract.library.fragment.ContractAssetFragment
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.CommonUtils
import com.sl.ui.library.utils.setDrawableRight
import kotlinx.android.synthetic.main.fragment_tab_asset.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

/**
 * 资产
 */
class TabAssetFragment : BaseFragment(){
    override fun setContentView(): Int {
        return R.layout.fragment_tab_asset
    }

    private var contractAssetFragment = ContractAssetFragment.newInstance()
    private var spotAssetFragment = SpotAssetFragment()
    private var currFragment = Fragment()
    private var currentIndex = 0

    //合约
    private val decimalTwo = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))
    private var isShowAssetEye = true

    override fun initView() {
        initListener()
        showFragment()
        updateAccountUi()
        updateAssetEyeUi()

        ContractUserDataAgent.registerContractAccountWsListener(this,object:ContractAccountListener(){
            /**
             * 合约账户ws有更新，
             */
            override fun onWsContractAccount(contractAccount: ContractAccount?) {
                if (currentIndex == 0) {
                    contractAssetFragment?.updateAccountData()
                }
                updateAccountUi()
            }

        })
    }


    private fun initListener() {
        /**
         * 合约账户
         */
        ll_contract_account.setOnClickListener {
            if (currentIndex != 0) {
                currentIndex = 0
                showFragment()
            }
        }
        /**
         * 钱包账户
         */
        ll_spot_account.setOnClickListener {
            if (currentIndex != 1) {
                currentIndex = 1
                showFragment()
            }
        }
        /**
         * 是否显示金额
         */
        tv_asset_eye.setOnClickListener {
            SpotAssetHelper.isShowAssetEye = !isShowAssetEye
            updateAssetEyeUi()
        }
        /**
         * 资金划转
         */
        ll_transfer_layout.setOnClickListener {
            mActivity?.let {
                if (UserHelper.isLogin()) {
                    AssetTransferActivity.show(it)
                } else {
                    LoginHelper.openLogin(it)
                }
            }
        }
        /**
         * 充值
         */
        ll_deposit_layout.setOnClickListener {
            mActivity?.let {
                if (UserHelper.isLogin()) {
                    DepositActivity.show(it)
                } else {
                    LoginHelper.openLogin(it)
                }
            }
        }
        /**
         * 提现
         */
        ll_withdraw_layout.setOnClickListener {
            mActivity?.let {
                if (UserHelper.isLogin()) {
                    WithdrawActivity.show(it)
                } else {
                    LoginHelper.openLogin(it)
                }
            }
        }
        /**
         * 资金流水
         */
        ll_asset_flow_layout.setOnClickListener {
            mActivity?.let {
                if (UserHelper.isLogin()) {
                    FundsFlowActivity.show(it)
                } else {
                    LoginHelper.openLogin(it)
                }
            }
        }
    }


    /**
     * 更新资产眼睛UI
     */
    private fun updateAssetEyeUi() {
        isShowAssetEye = SpotAssetHelper.isShowAssetEye
        if (isShowAssetEye) {
            tv_asset_eye.setDrawableRight(R.drawable.icon_eye_open)
        } else {
            tv_asset_eye.setDrawableRight(R.drawable.icon_eye_off)
        }
        updateAccountUi()

        contractAssetFragment.updateAssetEye(isShowAssetEye)
        spotAssetFragment.updateAssetEye(isShowAssetEye)
    }

    private fun updateAccountUi() {
        //合约
        var totalContractBalance = ContractUtils.calculateTotalBalance("BTC")
        var totalContractBalanceCny = ContractUtils.calculateCnyByBtcBalance(totalContractBalance)
        CommonUtils.showAssetEye(
            isShowAssetEye,
            decimalTwo.format(totalContractBalance) + " BTC",
            tv_contract_balance
        )
        CommonUtils.showAssetEye(
            isShowAssetEye,
            decimalTwo.format(totalContractBalanceCny) + " CNY",
            tv_contract_balance_cny
        )
        //现货
        var totalSpotBalance = SpotAssetHelper.getBalanceTotal("BTC")
        var totalSpotBalanceCny = ContractUtils.calculateCnyByBtcBalance(totalSpotBalance)
        CommonUtils.showAssetEye(
            isShowAssetEye,
            decimalTwo.format(totalSpotBalance) + " BTC",
            tv_spot_balance
        )
        CommonUtils.showAssetEye(
            isShowAssetEye,
            decimalTwo.format(totalSpotBalanceCny) + " CNY",
            tv_spot_balance_cny
        )
        //总资产
        tv_total_asset.text =
            decimalTwo.format(MathHelper.add(totalContractBalance, totalSpotBalance)) + " BTC";
        tv_total_asset_cny.text = " ≈ " + decimalTwo.format(
            MathHelper.add(
                totalContractBalanceCny,
                totalSpotBalanceCny
            )
        ) + " CNY";
        CommonUtils.showAssetEye(
            isShowAssetEye,
            decimalTwo.format(MathHelper.add(totalContractBalance, totalSpotBalance)) + " BTC",
            tv_total_asset
        )
        CommonUtils.showAssetEye(
            isShowAssetEye,
            " ≈ " + decimalTwo.format(MathHelper.add(totalContractBalanceCny, totalSpotBalanceCny)),
            tv_total_asset_cny
        )
    }

    private fun showFragment() {
        val transaction = childFragmentManager.beginTransaction()
        currFragment = if (currentIndex == 0) {
            if (!contractAssetFragment.isAdded) {
                transaction.hide(currFragment).add(R.id.fl_asset_layout, contractAssetFragment)
            } else {
                transaction.hide(currFragment).show(contractAssetFragment)
            }
            contractAssetFragment
        } else {
            if (!spotAssetFragment.isAdded) {
                transaction.hide(currFragment).add(R.id.fl_asset_layout, spotAssetFragment)
            } else {
                transaction.hide(currFragment).show(spotAssetFragment)
            }
            spotAssetFragment
        }
        transaction.commitNowAllowingStateLoss()
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.sl_spot_assets_change_event -> {
                //现货资产变更
                if (currentIndex == 1) {
                    spotAssetFragment?.updateSpotAccount()
                }
                updateAccountUi()
            }
        }
    }


}