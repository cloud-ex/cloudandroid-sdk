package com.sl.contract.library.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.Contract
import com.contract.sdk.data.ContractLiqRecord
import com.contract.sdk.data.ContractOrder
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.TimeFormatUtils
import com.sl.contract.library.R
import com.sl.contract.library.adapter.ContractEntrustDetailAdapter
import com.sl.contract.library.helper.ContractOrderHelper
import com.sl.contract.library.widget.ContractUpDownItemLayout
import com.sl.ui.library.base.BaseActivity
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_contract_entrust_detail.*
import java.text.DecimalFormat
import kotlin.collections.ArrayList
import kotlin.math.abs

/**
 * 合约订单委托明细
 */
class ContractEntrustDetailActivity :BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_contract_entrust_detail
    }

    private var contractOrder : ContractOrder?=null
    private var mList = ArrayList<ContractOrder>()
    private var adapter: ContractEntrustDetailAdapter?= null
    private var mContract : Contract? = null

    val dfDefault: DecimalFormat = NumberUtil.getDecimal(-1)
    var dfVol: DecimalFormat = NumberUtil.getDecimal(-1)
    var dfPrice: DecimalFormat = NumberUtil.getDecimal(-1)

    private val  headerView by lazy {
        View.inflate(this@ContractEntrustDetailActivity, R.layout.header_contract_entrust_detail_top_layout, null)
    }
    private val  footerViewView by lazy {
        View.inflate(this@ContractEntrustDetailActivity, R.layout.header_contract_entrust_detail_footer_layout, null)
    }


    override fun loadData() {
        contractOrder = intent.extras?.getParcelable("order")
        if(contractOrder == null){
            finish()
            return
        }
        loadDataFromNet()
    }


    override fun initView() {
        adapter = ContractEntrustDetailAdapter(this@ContractEntrustDetailActivity,mList)
        adapter?.animationEnable = true
        adapter?.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn)
        adapter?.addHeaderView(headerView)
        adapter?.addFooterView(footerViewView)
        lv_list.layoutManager =  LinearLayoutManager(this@ContractEntrustDetailActivity)
        lv_list.adapter = adapter
        adapter?.isForceCloseOrder = contractOrder!!.isForceCloseOrder

        updateHerderView()
        updateFooterView()
    }

    private fun updateFooterView() {
        contractOrder?.let {
            //订单状态
            val errno: Int = it.errno
            if(errno > 1){
                footerViewView.visibility = View.VISIBLE
                footerViewView.findViewById<TextView>(R.id.tv_cancel_reason).text = getSystemCanceledFormat(errno)
            }
        }
    }

    private fun updateHerderView() {
        contractOrder?.let {
            //方向
            ContractOrderHelper.updateOrderSideUi(this,it.side, headerView.findViewById(R.id.tv_position_type))
            //委托状态
            updateEntrustStatusUi(it)
            mContract = ContractPublicDataAgent.getContract(it.instrument_id)
            mContract?.let { contract ->
                dfVol = NumberUtil.getDecimal(contract.vol_index)
                dfPrice =  NumberUtil.getDecimal(contract.price_index)
                //名称
                headerView.findViewById<TextView>(R.id.tv_name).text = contract.symbol
                //成交均价
                val itemAvgPx = headerView.findViewById<ContractUpDownItemLayout>(R.id.item_avg_px)
                itemAvgPx.title = getString(R.string.str_deal_cost_px) + "("+contract.quote_coin+")"
                itemAvgPx.content = dfDefault.format(MathHelper.round(it.avg_px, contract.price_index))
                //手续费
                val itemFee = headerView.findViewById<ContractUpDownItemLayout>(R.id.item_fee)
                itemFee.title = getString(R.string.str_fee) + "("+contract.margin_coin+")"
               // item_fee.content =

                //成交量
                val itemDealVol = headerView.findViewById<ContractUpDownItemLayout>(R.id.item_deal_vol)
                itemDealVol.title = getString(R.string.str_deal_volume)+"("+getString(R.string.str_vol_unit)+")"
                itemDealVol.content = dfVol.format(MathHelper.round(it.cum_qty))
            }

        }
    }


    /**
     * 更新委托状态UI
     */
    private fun   updateEntrustStatusUi(order: ContractOrder) {
       val tvEntrustStatus = headerView.findViewById<TextView>(R.id.tv_entrust_status)
        val category: Int = order.category
        if (category and 128 > 0) { //第7位为1表示:强平委托单
            tvEntrustStatus.visibility = View.VISIBLE
            tvEntrustStatus.text = getString(R.string.str_force_close_details)
        } else if (category and 256 > 0) { //第8位为1表示:爆仓委托单
            tvEntrustStatus.visibility = View.VISIBLE
            tvEntrustStatus.text = getString(R.string.str_force_close_details)
        } else if (category == 513) {
            tvEntrustStatus.visibility = View.VISIBLE
            if (MathHelper.round(order.take_fee) > 0) {
                tvEntrustStatus.text = getString(R.string.str_force_close_details)
            } else {
                tvEntrustStatus.text = getString(R.string.str_reduce_position_details)
            }
        }
        tvEntrustStatus.setOnClickListener {
            doQueryDetail(order)
        }
    }

    /**
     * 查询委托状态
     */
    private fun doQueryDetail(order: ContractOrder) {
        ContractUserDataAgent.loadLiqRecord(order.oid,order.instrument_id,object: IResponse<List<ContractLiqRecord>>(){
            override fun onSuccess(data: List<ContractLiqRecord>) {
                if (data != null && data.isNotEmpty()) {
                    var liqRecord: ContractLiqRecord? = data[0]
                    for (info in data) {
                        if (info.oid === order.oid) {
                            liqRecord = info
                            break
                        }
                    }

                    var createdAt = TimeFormatUtils.timeStampToDate(TimeFormatUtils.getUtcTimeToMillis(liqRecord!!.created_at), "yyyy-MM-dd  HH:mm:ss")

                    mContract?.let {
                        val contractName = it.symbol
                        var positionName = ""
                        var priceChange = ""
                        if (order.side === ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT) {
                            priceChange = getString(R.string.str_rose)
                            positionName = getString(R.string.sl_str_hold_sell_open)
                        } else if (order.side === ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG) {
                            priceChange = getString(R.string.str_fall)
                            positionName = getString(R.string.sl_str_hold_buy_open)
                        }

                        val type = liqRecord.type
                        val category: Int = order.category
                        var intro1 = ""
                        var intro2 = ""
                        var titleName = ""
                        if(type == 1){//部分强平
                             intro1 = String.format(getString(R.string.str_wiped_out_tips0),
                                createdAt,
                                contractName,
                                priceChange + dfDefault.format(MathHelper.round(liqRecord.trigger_px, it.price_index)) + it.quote_coin,
                                positionName, MathHelper.round(MathHelper.mul(liqRecord.mmr, "100"), 2).toString() + "%")
                             intro2 = String.format(getString(R.string.str_wiped_out_tips2),
                                liqRecord.order_px + it.quote_coin)
                              titleName = getString(R.string.str_force_close_details)
                        }else if (type == 2 || type == 3) { //爆仓
                             intro1 = String.format(getString(R.string.str_wiped_out_tips1),
                                createdAt,
                                contractName,
                                priceChange + dfDefault.format(MathHelper.round(liqRecord.trigger_px, it.price_index)) + it.quote_coin,
                                positionName, MathHelper.round(MathHelper.mul(liqRecord.mmr, "100"), 2).toString() + "%")
                             intro2 = java.lang.String.format(getString(R.string.str_wiped_out_tips2),
                                liqRecord.order_px + it.quote_coin)
                            titleName = getString(R.string.str_blowing_up_datail)
                        }else if (type == 4) {//减仓明细
                            if (category == 513) {
                                if (MathHelper.round(order.take_fee) > 0) {
                                    intro1 = String.format(getString(R.string.str_wiped_out_tips0),
                                        createdAt,
                                        contractName,
                                        priceChange + dfDefault.format(MathHelper.round(liqRecord.trigger_px, it.price_index)) + it.quote_coin,
                                        positionName, MathHelper.round(MathHelper.mul(liqRecord.mmr, "100"), 2).toString() + "%")
                                    intro2 = String.format(getString(R.string.str_wiped_out_tips2),
                                        liqRecord.order_px + it.quote_coin)
                                    titleName = getString(R.string.str_force_close_details)
                                }else{
                                     intro1 = String.format(getString(R.string.str_reduce_position_tips),
                                        createdAt,
                                        dfDefault.format(MathHelper.round(liqRecord.trigger_px, it.price_index)) + it.quote_coin,
                                        dfDefault.format(MathHelper.round(liqRecord.order_px, it.price_index)) + it.quote_coin)
                                    titleName = getString(R.string.str_reduce_position_details)
                                }
                            }else{
                                 intro1 = String.format( getString(R.string.str_reduce_position_tips),
                                    createdAt,
                                    dfDefault.format(MathHelper.round(liqRecord.trigger_px, it.price_index)) + it.quote_coin,
                                    dfDefault.format(MathHelper.round(liqRecord.order_px, it.price_index)) + it.quote_coin)
                                titleName = getString(R.string.str_reduce_position_details)
                            }

                        }
                        if(!TextUtils.isEmpty(intro1)){
                            DialogUtils.showCenterDialog(this@ContractEntrustDetailActivity,titleName,intro1+"\n"+intro2,"",getString(R.string.common_text_btn_i_see),null)
                        }
                    }

                }
            }

            override fun onFail(code: String, msg: String) {
                ToastUtil.shortToast(ContractSDKAgent.context!!,msg)
            }

        })
    }

    /**
     * 系统取消原因
     */
    private fun getSystemCanceledFormat(type:Int) : String{
        return when(type){
            2 -> getString(R.string.str_entrust_failed_reason2)
            3 -> getString(R.string.str_entrust_failed_reason3)
            4 -> getString(R.string.str_entrust_failed_reason4)
            5 -> getString(R.string.str_entrust_failed_reason5)
            6 -> getString(R.string.str_entrust_failed_reason6)
            7 -> getString(R.string.str_entrust_failed_reason7)
            8 -> getString(R.string.str_entrust_failed_reason8)
            9 -> getString(R.string.str_entrust_failed_reason9)
            10 -> getString(R.string.str_entrust_failed_reason10)
            11 -> getString(R.string.str_entrust_failed_reason11)
            12 -> getString(R.string.str_entrust_failed_reason12)
            13 -> getString(R.string.str_entrust_failed_reason13)
            14 -> getString(R.string.str_entrust_failed_reason14)
            15 -> getString(R.string.str_entrust_failed_reason15)
            16 -> getString(R.string.str_entrust_failed_reason16)
            17 -> getString(R.string.str_entrust_failed_reason17)
            18 -> getString(R.string.str_entrust_failed_reason18)

            else -> ""
        }
    }


    private fun loadDataFromNet() {
        showLoadingDialog()
        ContractUserDataAgent.loadOrderTrades(contractOrder?.instrument_id!!, contractOrder?.oid?:0,object: IResponse<List<ContractOrder>>(){
            override fun onSuccess(data: List<ContractOrder>) {
                hideLoadingDialog()
                if (data != null && data.isNotEmpty()) {
                    mList.addAll(data)
                    adapter?.notifyDataSetChanged()

                    //总手续费
                    val itemFee = headerView.findViewById<ContractUpDownItemLayout>(R.id.item_fee)
                    var totalFee = 0.0
                    for (item in mList){
                        if(!TextUtils.isEmpty(item.take_fee)&& item.take_fee.toDouble() > 0){
                            totalFee+= item.take_fee.toDouble()
                        }else  if(!TextUtils.isEmpty(item.make_fee)){
                            var makeFee = abs(item.make_fee.toDouble())
                            totalFee+= makeFee
                        }
                    }
                    itemFee.content = totalFee.toString()
                }
            }
            override fun onFail(code: String, msg: String) {
                hideLoadingDialog()
            }

        })
    }

    companion object{
        fun show(activity: Activity, order: ContractOrder){
            val intent = Intent(activity,ContractEntrustDetailActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelable("order",order)
            intent.putExtras(bundle)
            activity.startActivity(intent)
        }
    }
}