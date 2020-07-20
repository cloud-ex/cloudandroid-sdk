package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import android.view.View
import com.contract.sdk.utils.MathHelper
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.DoWithdrawApi
import com.sl.bymex.api.SendVerifyCodeApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.SpotCoin
import com.sl.bymex.service.PublicInfoHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.VerifyCodeHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.utils.QRCodeUtil
import com.sl.bymex.utils.SpotDialogHelper
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.utils.SystemUtils
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.CommonlyUsedButton
import kotlinx.android.synthetic.main.activity_withdraw.*
import java.lang.Exception
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.collections.ArrayList

/**
 * 提现
 */
class WithdrawActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_withdraw
    }

    /**
     * 是否是手机验证
     */
    private var isPhoneVerify = true
    private var codeHelper = VerifyCodeHelper()

    private val sSelectCoinRequestCode = 1001
    private val sQrWithdrawAddressRequestCode = 1002
    private val sQrMemoAddressRequestCode = 1003

    var spotCoinList = ArrayList<SpotCoin>()
    var selectSpotCoin: SpotCoin? = null
    /**
     * 是否设置资金密码
     */
    private var hasAssetPwd = false
    //可提现余额
    var balance = 0.0
    //配置提现手续费
    var configFee = "0"
    var configMinVol = "0"
    var withdrawAmount = 0.0
    val decimalFormat = DecimalFormat(
        "###################.###########",
        DecimalFormatSymbols(Locale.ENGLISH)
    )

    private val inputTextWatcher = object: CommonInputLayout.EditTextChangedListener{
        override fun onTextChanged(view: CommonInputLayout) {
            updateButtonState()
        }

    }
    override fun loadData() {
        hasAssetPwd = UserHelper.user.asset_password_effective_time > 0
        isPhoneVerify = UserHelper.isPhoneVerify()
        spotCoinList.addAll(PublicInfoHelper.getSpotCoin())
        if (spotCoinList.isNotEmpty()) {
            selectSpotCoin = spotCoinList[0]
        }
    }

    override fun initView() {
        tv_tab_erc20.isSelected = true
        tv_tab_omin.isSelected = false
        item_pwd_layout.visibility = if(hasAssetPwd) View.VISIBLE else View.GONE
        item_pwd_layout.setNumberPwdType()
        item_vol_layout.setMoneyType()

        item_pwd_layout.setEditTextChangedListener(inputTextWatcher)
        item_address_layout.setEditTextChangedListener(inputTextWatcher)
        item_vol_layout.setEditTextChangedListener(inputTextWatcher)

        initListener()
        updateUiByCoin()
    }

    private fun updateUiByCoin() {
        selectSpotCoin?.let {
            val withdrawalConfig = PublicInfoHelper.getWithdrawalConfig(selectSpotCoin!!.name)
            configFee = withdrawalConfig?.fee?:"0.0"
            configMinVol = withdrawalConfig?.min_vol?:"0.0"

            item_coin_layout.itemLeftText = it.name
            item_coin_layout.setLeftIcon(it.small ?: "", R.drawable.icon_coin_default)
            //余额
            val asset = UserHelper.getUserAsset(it.name)
            balance = 0.0
            asset?.let {
                balance = MathHelper.round(
                    asset.available_vol,
                    selectSpotCoin!!.vol_index
                )
            }
            item_vol_layout.inputText = ""
            item_vol_layout.inputHelperHint = getString(R.string.str_balance)+":"+ decimalFormat.format(balance)+ it.name

            //手续费
            tv_miner_fee.text = decimalFormat.format(MathHelper.round(configFee,selectSpotCoin!!.vol_index))+it.name
            //实际到账
            tv_actual_to_account.text = "0.00"+it.name

        }
    }

    /**
     * 更新按钮状态
     */
    private fun updateButtonState(){
        val address = item_address_layout.inputText
        var vol = item_vol_layout.inputText
        val pwd = item_pwd_layout.inputText

        var factAmount = 0.0
        //校正输入数量
        selectSpotCoin?.let {
            if(vol.contains(".")){
                if(TextUtils.equals(vol,"0")){
                    vol = "0."
                    item_vol_layout.inputText = vol
                    item_vol_layout.getInputView().setSelection(vol.length)
                }else{
                    val volIndex = it.vol_index+1
                    var index =vol.indexOf(".")
                    if(index + volIndex < vol.length){
                        vol = vol.substring(0,index+volIndex)
                        item_vol_layout.inputText = vol
                        item_vol_layout.getInputView().setSelection(vol.length)
                    }
                }
            }

            //手续费和实际到账
            if(TextUtils.isEmpty(vol)){
                tv_actual_to_account.text = "0.00" + it.name
            }else{
                withdrawAmount = MathHelper.round(vol, 10)
                //外部地址 才计算手续费
                factAmount = MathHelper.round(
                    kotlin.math.max(0.0, MathHelper.sub(withdrawAmount, configFee.toDouble())),
                    10
                )
                tv_actual_to_account.text = decimalFormat.format(factAmount) + it.name
            }

        }


        //地址
        if(address.length <= 6){
            bt_confirm.isEnable(false)
            return
        }

        //校验密码
        if(hasAssetPwd and (pwd.length < 6)){
            bt_confirm.isEnable(false)
            return
        }
        if(factAmount <= 0.0 ){
            bt_confirm.isEnable(false)
            return
        }
        bt_confirm.isEnable(true)


    }

    private fun initListener() {
        tv_tab_erc20.setOnClickListener {
            tv_tab_erc20.isSelected = true
            tv_tab_omin.isSelected = false
        }
        tv_tab_omin.setOnClickListener {
            tv_tab_erc20.isSelected = false
            tv_tab_omin.isSelected = true
        }
        /**
         * 选择币种
         */
        item_coin_layout.setItemOnClickListener(View.OnClickListener {
            for (index in spotCoinList.indices) {
                val item = spotCoinList[index]
                if (selectSpotCoin != null) {
                    item.selected = TextUtils.equals(item.name, selectSpotCoin!!.name)
                }
            }
            SelectCoinActivity.show(
                this@WithdrawActivity,
                spotCoinList,
                getString(R.string.str_select_coin),
                sSelectCoinRequestCode
            )
        })
        /**
         * 提现数量 全部 点击
         */
        item_vol_layout.setRightListener(View.OnClickListener {
            item_vol_layout.inputText = decimalFormat.format(balance)
        })
        /**
         * 提现地址扫码
         */
        item_address_layout.setRightListener(View.OnClickListener {
            QRCodeUtil.openScan(
                this@WithdrawActivity,
                sQrWithdrawAddressRequestCode
            )
        })
        /**
         * Memo扫码
         */
        item_memo_layout.setRightListener(View.OnClickListener {
            QRCodeUtil.openScan(this@WithdrawActivity, sQrMemoAddressRequestCode)
        })
        /**
         * 充提记录
         */
        title_layout.setRightOnClickListener(View.OnClickListener {
            selectSpotCoin?.let {
                DwRecordActivity.show(this@WithdrawActivity,1,it.name)
            }
        })

        bt_confirm.isEnable(false)
        bt_confirm.listener = object : CommonlyUsedButton.OnBottomListener {
            override fun bottomOnClick() {
                val vol = item_vol_layout.inputText
                if(vol.toDouble() > balance){
                    TopToastUtils.showTopFailToast(this@WithdrawActivity,getString(R.string.str_not_surricient_funds))
                    return
                }

                if(withdrawAmount < configMinVol.toDouble()){
                    TopToastUtils.showTopFailToast(
                        this@WithdrawActivity,
                        getString(R.string.str_minium_withdraw) + " " + decimalFormat.format(
                            configMinVol
                        ) + selectSpotCoin!!.name
                    )
                    return
                }

                doGraphicCodeCheck()
            }
        }
    }

    /**
     * 图片验证
     */
    private fun doGraphicCodeCheck() {
        VerifyCodeHelper.doGraphicCodeCheckApi(VerifyCodeHelper.VERIFY_TYPE_WITHDRAW,this@WithdrawActivity,
        object:VerifyCodeHelper.GraphicCodeListener{
            override fun doVerifySuccess(validate: String) {
                val codeApi = SendVerifyCodeApi()
                if(isPhoneVerify){
                    codeApi.phone = UserHelper.user.phone
                }else{
                    codeApi.email = UserHelper.user.email
                }
                codeApi.type =VerifyCodeHelper.VERIFY_TYPE_RESET_PWD
                codeApi.validate = validate
                codeHelper.sendVerifyCode(
                    this@WithdrawActivity,
                    codeApi,
                    verifyCodeListener
                )
            }

            override fun doVerifyFail(msg: String) {
                bt_confirm.hideLoading()
                TopToastUtils.showTopFailToast(this@WithdrawActivity,msg)
            }

        })
    }

    private val verifyCodeListener = object: VerifyCodeHelper.VerifyCodeListener{
        override fun reSendCode() {
            doGraphicCodeCheck()
        }

        override fun doCancel() {
            bt_confirm.hideLoading()
        }

        override fun sendFail(msg: String) {
            bt_confirm.hideLoading()
            TopToastUtils.showTopFailToast(this@WithdrawActivity,msg)
        }

        override fun doCommit(code: String) {
            val gaCode = UserHelper.user.ga_key
            if (TextUtils.equals(gaCode, "unbound")) {
                doWithdraw(code,"")
            }else{
                codeHelper.dismissVerifyCodeDialog()
                //弹出谷歌验证码
                codeHelper.openGoogleVerifyCodeDialog(this@WithdrawActivity,object:VerifyCodeHelper.DialogConfirmListener{
                    override fun doConfirm(ga_code: String) {
                        doWithdraw(code,ga_code)
                    }

                })
            }
        }

    }

    private fun doWithdraw(code: String,ga_code:String) {
        val vol = item_vol_layout.inputText
        val pwd = item_pwd_layout.inputText
        val address = item_address_layout.inputText
        val memo = item_memo_layout.inputText

        val withdrawApi = DoWithdrawApi()
        withdrawApi.coin_code = selectSpotCoin!!.name
        withdrawApi.vol = vol
        withdrawApi.to_address = address
        if (isPhoneVerify) {
            withdrawApi.sms_code = code
        } else {
            withdrawApi.email_code = code
        }
        if (!TextUtils.isEmpty(memo)) {
            withdrawApi.memo = memo
        }
        if(TextUtils.isEmpty(ga_code)){
            withdrawApi.ga_code = ga_code
        }

        NetHelper.doHttpPost(
            this@WithdrawActivity,
            withdrawApi,
            object : HttpCallback<HttpData<String>>(this@WithdrawActivity) {
                override fun onFail(e: Exception?) {
                    bt_confirm.hideLoading()
                    codeHelper.dialogBtnStopLoading()
                    TopToastUtils.showTopFailToast(
                        this@WithdrawActivity,
                        e?.message ?: getString(R.string.str_net_fail)
                    )
                }

                override fun onSucceed(result: HttpData<String>?) {
                    UserHelper.loadUserInfoFromNet()
                    codeHelper.dismissVerifyCodeDialog()
                    SpotDialogHelper.showWithdrawAuditDialog(this@WithdrawActivity, object :
                        DialogUtils.DialogBottomListener {
                        override fun clickTab(tabType: Int) {
                            this@WithdrawActivity.finish()
                        }

                    })
                }
            },
            SystemUtils.toMD5(pwd)
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((resultCode == Activity.RESULT_OK)) {
            when (requestCode) {
                sSelectCoinRequestCode -> {
                    var tabInfo =
                        data?.getParcelableExtra(SelectCoinActivity.sSelectResponseKey) as SpotCoin
                    if (tabInfo != null) {
                        selectSpotCoin = tabInfo
                        updateUiByCoin()
//                loadDepositAddress()
                    }
                }
                sQrWithdrawAddressRequestCode ,sQrMemoAddressRequestCode -> {
                    var result = data?.getStringExtra("result")?:""
                    if(!TextUtils.isEmpty(result)){
                        if(sQrWithdrawAddressRequestCode == requestCode){
                            item_address_layout.inputText = result
                        }else{
                            item_memo_layout.inputText = result
                        }
                    }
                }

            }

        }
    }

    companion object {
        fun show(activity: Activity) {
            val intent = Intent(activity, WithdrawActivity::class.java)
            activity.startActivity(intent)
        }
    }
}