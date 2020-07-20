package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.text.InputType.*
import android.text.TextUtils
import android.view.View
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.utils.MathHelper
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.AssetTransferApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.UserAsset
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.ui.library.data.TabInfo
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.delayFinish
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_asset_transfer.*
import java.lang.Exception
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


/**
 * 资金划转
 */
class AssetTransferActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_asset_transfer
    }

    var coin = ""
    var contractAccount: ContractAccount? = null
    var spotAsset: UserAsset? = null
    var transferFromType = TransferType.SPOT
    var transferToType = TransferType.CONTRACT

    val decimalFormat = DecimalFormat(
        "###################.###########",
        DecimalFormatSymbols(Locale.ENGLISH)
    )


    override fun loadData() {
        coin = intent.getStringExtra("coin") ?: ""

    }

    override fun initView() {
        item_coin_layout.setLeftIcon(R.drawable.icon_coin_default)
        updateUiByCoin()
        updateBalanceUi()
        initClickListener()
    }

    private fun initClickListener() {
        /**
         * 选择币种
         */
        item_coin_layout.setItemOnClickListener(View.OnClickListener {
            val accounts =   ContractUserDataAgent.getContractAccounts()
            val tabInfoList =  ArrayList<TabInfo>()
            for (index in accounts.indices){
                val tabInfo = TabInfo()
                val item = accounts[index]
                if(TextUtils.equals(item.coin_code,coin)){
                    tabInfo.selected = true
                }
                tabInfo.index = index
                tabInfo.leftIcon = R.drawable.icon_coin_default
                tabInfo.name = item.coin_code
                tabInfoList.add(tabInfo)
            }
            SelectCommonActivity.show(this@AssetTransferActivity,tabInfoList,getString(R.string.str_select_coin),1001)
        })
        /**
         * 切换账户
         */
        iv_switch_transfer.setOnClickListener {
            when (transferFromType) {
                TransferType.SPOT ->{
                    transferFromType = TransferType.CONTRACT
                    transferToType = TransferType.SPOT
                    tv_from_account.text = getString(R.string.str_contract_account)
                    tv_to_account.text = getString(R.string.str_wallet_account)
                }
                TransferType.CONTRACT ->{
                    transferFromType = TransferType.SPOT
                    transferToType = TransferType.CONTRACT
                    tv_from_account.text = getString(R.string.str_wallet_account)
                    tv_to_account.text = getString(R.string.str_contract_account)
                }
            }
            updateBalanceUi()
        }
        /**
         * 点击全部
         */
        item_vol_layout.setRightListener(View.OnClickListener { item_vol_layout.inputText = getBalance() })
        /**
         * 输入金额监听
         */
        item_vol_layout.inputType =
            TYPE_NUMBER_FLAG_DECIMAL or TYPE_CLASS_NUMBER
        item_vol_layout.setEditTextChangedListener(object:
            CommonInputLayout.EditTextChangedListener{
            override fun onTextChanged(view: CommonInputLayout) {
                updateBtnUi()
            }

        })
        /**
         * 点击提交
         */
        bt_commit.listener = object : CommonlyUsedButton.OnBottomListener{
            override fun bottomOnClick() {
                bt_commit.showLoading()
                val vol = item_vol_layout.inputText
                val transferApi = AssetTransferApi()
                if(transferFromType == TransferType.SPOT){
                    transferApi.type = 1
                }else{
                    transferApi.type = 2
                }
                transferApi.vol = vol
                transferApi.coin_code = coin

                NetHelper.doHttpPost(this@AssetTransferActivity,transferApi,object:HttpCallback<HttpData<String>>(this@AssetTransferActivity){
                    override fun onFail(e: Exception) {
                        bt_commit.hideLoading()
                        TopToastUtils.showTopSuccessToast(this@AssetTransferActivity,e.message)
                    }

                    override fun onSucceed(result: HttpData<String>) {
                        TopToastUtils.showSuccess(this@AssetTransferActivity,getString(R.string.str_transfer_success))
                        //划转成功,
                        UserHelper.loadUserInfoFromNet()
                        ContractUserDataAgent.loadContractAccount(null)
                        delayFinish(bt_commit,1000)
                    }
                })
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((resultCode == Activity.RESULT_OK) and (requestCode == 1001)){
            var tabInfo = data?.getParcelableExtra(SelectCommonActivity.sSelectResponseKey) as TabInfo
            if(tabInfo != null){
                coin = tabInfo.name?:""

                updateUiByCoin()
                updateBalanceUi()
            }
        }
    }

    private fun updateBtnUi() {
        val vol = item_vol_layout.inputText
        if(TextUtils.isEmpty(vol)){
            bt_commit.isEnable(false)
            return
        }
        val volD = vol.toDouble()
        if(volD > getBalance().toDouble() ){
            bt_commit.isEnable(false)
            return
        }
        bt_commit.isEnable(true)
    }

    private fun updateUiByCoin() {
        contractAccount = ContractUserDataAgent.getContractAccount(coin)
        contractAccount?.let {
            item_coin_layout.itemLeftText = coin

        }
    }

    private fun getBalance():String{
        when (transferFromType) {
            TransferType.SPOT -> {
                spotAsset?.let {
                    val vol: Double = MathHelper.round(it.available_vol, 8)
                   return decimalFormat.format(vol)
                }
            }
            TransferType.CONTRACT -> {
                contractAccount?.let {
                    val vol: Double = MathHelper.round(it.getCanWithdraw(1.05), 8)
                    return decimalFormat.format(vol)
                }
            }
        }
        return "0.00"
    }

    private fun updateBalanceUi() {
        when (transferFromType) {
            TransferType.SPOT -> {
                spotAsset = UserHelper.getUserAsset(coin)
                item_vol_layout.inputHelperHint =
                    getString(R.string.str_balance) + ": " + getBalance() +coin
            }
            TransferType.CONTRACT -> {
                contractAccount = ContractUserDataAgent.getContractAccount(coin)
                item_vol_layout.inputHelperHint =
                    getString(R.string.str_balance) + ": " + getBalance() +coin
            }
        }

    }

    /**
     * 划转类型
     */
    enum class TransferType {
        SPOT,
        CONTRACT
    }

    companion object {
        fun show(activity: Activity, coin: String? = "USDT") {
            val intent = Intent(activity, AssetTransferActivity::class.java)
            intent.putExtra("coin", coin)
            activity.startActivity(intent)
        }
    }
}