package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractAccount
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractTicker
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.flyco.tablayout.listener.OnTabSelectListener
import com.sl.contract.library.R
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.utils.finishActivityDialogAnim
import com.sl.ui.library.utils.startActivityDialogAnim
import com.sl.ui.library.widget.CommonInputLayout
import kotlinx.android.synthetic.main.activity_adjust_margin.*
import kotlinx.android.synthetic.main.activity_contract_calculate.tab_layout
import kotlin.math.abs

/**
 * 调整保证金
 */
class AdjustMarginActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_adjust_margin
    }

    private var contract: Contract? = null
    private var contractTicker: ContractTicker? = null
    private var contractAccount: ContractAccount? = null

    private var mPosition = ContractPosition()
    private var mNewPosition = ContractPosition()
    private var mMaxMargin = 0.0 //最大保证金
    private var mMinMargin = 0.0 //最小保证金

    private var dfValue = NumberUtil.getDecimal(-1)

    private var tabTextList = arrayOfNulls<String>(2)
    private var tabIndex = 0

    override fun loadData() {
        mPosition = intent.getParcelableExtra("position")
        contract = ContractPublicDataAgent.getContract(mPosition.instrument_id)
        contractTicker = ContractPublicDataAgent.getContractTicker(mPosition.instrument_id)
        if (contract == null || contractTicker == null) {
            finish()
            return
        }
        mNewPosition.fromJson(mPosition.toJson())
        contractAccount = ContractUserDataAgent.getContractAccount(contract!!.margin_coin)
        contractAccount?.let {
            mMaxMargin = MathHelper.round( it.available_vol_real,contract!!.value_index)
        }
        mMinMargin = MathHelper.round(ContractCalculate.doCalculateCanMinMargin(mPosition,contract!!,contractTicker!!.fair_px),contract!!.value_index)

        tabTextList[0] = getString(R.string.str_add)
        tabTextList[1] = getString(R.string.str_decrease)
    }

    override fun initView() {
        iv_close.setOnClickListener(this)
        bt_sure.setOnClickListener(this)
        initTabView()
        //当前仓位
        tv_current_position.text =
            mPosition.cur_qty.toString() + " " + getString(R.string.str_vol_unit)
        //保证金
        tv_margins.text =
            "${MathHelper.round(mPosition.im, contract!!.value_index)} " + contract!!.margin_coin
        //当前强平价格
        tv_liq_price.text = "${getLiqPrice(mPosition)} " + contract!!.margin_coin
        updateMaxMarginUi()
        updateLiqPriceUI()
        item_input_layout.setMoneyType(contract!!.value_index)
        item_input_layout.setEditTextChangedListener(object :
            CommonInputLayout.EditTextChangedListener{
            override fun onTextChanged(view: CommonInputLayout) {
                updateLiqPriceUI()
            }

        })
    }


    private fun initTabView() {
        tab_layout.setTabData(tabTextList)
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                tabIndex = index
                updateMaxMarginUi()
                updateLiqPriceUI()
            }

            override fun onTabReselect(p0: Int) {
            }

        })
    }

    private fun updateLiqPriceUI() {
        mNewPosition.im = mPosition.im
        var marginVol = MathHelper.round(item_input_layout.inputText)
        if(tabIndex == 0){
            if(marginVol > mMaxMargin){
                marginVol = mMaxMargin
                item_input_layout.inputText = "$marginVol"
            }
            mNewPosition.im = (MathHelper.round(mNewPosition.im)+marginVol).toString()
        }else{
            if(marginVol > mMinMargin){
                marginVol = mMinMargin
                item_input_layout.inputText = "$marginVol"
            }
            mNewPosition.im = (MathHelper.round(mNewPosition.im) - marginVol).toString()
        }

        tv_new_liq_price.text = "${getLiqPrice(mNewPosition)} "+contract!!.margin_coin
    }

    /**
     * 更新最大/最小保证金UI
     */
    private fun updateMaxMarginUi() {
        if(tabIndex == 0){
            item_input_layout.inputHelperHint = String.format(getString(R.string.str_max_add_margin),"$mMaxMargin"+contract!!.margin_coin)
        }else{
            item_input_layout.inputHelperHint = String.format(getString(R.string.str_max_decrease_margin),"$mMinMargin"+contract!!.margin_coin)
        }
    }

    /**
     * 强平价格
     */
    fun getLiqPrice(item: ContractPosition): Double {
        var liqPrice = 0.0
        if (item.position_type == 1) {
            liqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                item, null, contract!!
            )
        } else if (item.position_type == 2) {//全仓
            liqPrice = ContractCalculate.CalculatePositionLiquidatePrice(
                item, contractAccount, contract!!
            )
        }
        return liqPrice
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_close -> {
                finishActivityDialogAnim()
            }
            R.id.bt_sure -> {
                doAdjustMarginRequest()
            }
        }
    }

    private fun doAdjustMarginRequest() {
        var operateType = if (tabIndex == 0) 1 else 2
        var marginVol = item_input_layout.inputText
        showLoadingDialog()
        ContractUserDataAgent.doAdjustMargin(mPosition.instrument_id,mPosition.pid,
            marginVol,operateType,
            object: IResponse<String>(){
                override fun onSuccess(data: String) {
                    hideLoadingDialog()
                    ToastUtil.shortToast(mActivity, getString(R.string.str_adjust_succeed))
                    finishActivityDialogAnim()
                }

                override fun onFail(code: String, msg: String) {
                    hideLoadingDialog()
                    ToastUtil.shortToast(mActivity, msg)
                }
            })
    }

    companion object {
        fun show(activity: Activity, position: ContractPosition) {
            val intent = Intent(activity, AdjustMarginActivity::class.java)
            intent.putExtra("position", position)
            activity.startActivityDialogAnim(intent)
        }
    }
}