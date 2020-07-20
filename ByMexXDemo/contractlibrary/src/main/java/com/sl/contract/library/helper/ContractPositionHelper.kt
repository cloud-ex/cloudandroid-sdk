package com.sl.contract.library.helper

import android.text.TextUtils
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.data.*
import com.contract.sdk.impl.IResponse
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.widget.StopWinLossItem
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.ToastUtil
import com.sl.ui.library.widget.CommonInputLayout
import com.sl.ui.library.widget.bubble.BubbleSeekBar
import com.sl.ui.library.widget.kline.bean.Index
import com.timmy.tdialog.TDialog
import com.timmy.tdialog.base.BindViewHolder
import kotlin.math.min

class ContractPositionHelper {
    companion object {
        /**
         * 构建提交平仓订单
         */
         fun buildCloseOrder(info: ContractPosition,vol:Int,px: Double=0.00,isMarketClose:Boolean) : ContractOrder{
            val order = ContractOrder()
            order.instrument_id = info.instrument_id
            order.nonce = System.currentTimeMillis()
            order.qty = vol.toString()
            order.pid = info.pid
            if (info.side == ContractPosition.POSITION_TYPE_LONG) {
                order.side = ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG
            } else {
                order.side = ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT
            }
            if(isMarketClose){
                order.category = ContractOrder.ORDER_CATEGORY_MARKET
            }else{
                order.px = px.toString()
                order.category = ContractOrder.ORDER_CATEGORY_NORMAL
            }

            return order
         }
        /**
         * 构建止盈止损计算订单
         */
        private fun findStopWinLossOrder(
            position: ContractPosition,
            callBack: (originOrder: ContractOrder, stopWinOrder: ContractOrder?, stopLossOrder: ContractOrder?) -> Unit
        ) {
            val planList = ContractUserDataAgent.getContractPlanOrder(position.instrument_id)
            var stopWinOrder: ContractOrder? = null
            var stopLossOrder: ContractOrder? = null
            planList.filter {
                if (position.side == ContractPosition.POSITION_TYPE_LONG) {
                    return@filter it.side == ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG
                } else {
                    return@filter it.side == ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT
                }
            }.forEachIndexed { _, contractOrder ->
                if (contractOrder.type == 1) {//止盈
                    stopWinOrder = contractOrder
                } else if (contractOrder.type == 2) {//止损
                    stopLossOrder = contractOrder
                }
            }
            val newOrder = ContractOrder()

            newOrder.trend = position.side
            newOrder.instrument_id = position.instrument_id
            newOrder.side =
                if (position.side == 1) ContractOrder.CONTRACT_ORDER_WAY_SELL_CLOSE_LONG else ContractOrder.CONTRACT_ORDER_WAY_BUY_CLOSE_SHORT
            newOrder.nonce = System.currentTimeMillis() / 1000
            newOrder.px = position.avg_cost_px//持仓均价
            newOrder.qty = position.cur_qty
            newOrder.pid = position.pid
            newOrder.category = ContractOrder.ORDER_CATEGORY_MARKET
            newOrder.trigger_type = 1

            callBack.invoke(newOrder, stopWinOrder, stopLossOrder)
        }

        /**
         * 打开止盈止损弹窗
         */
        fun openStopWinLossDialog(
            context: FragmentActivity,
            position: ContractPosition,
            callBack: (originOrder: ContractOrder, originStopWinOrder : ContractOrder?,originStopLossOrder : ContractOrder?) -> Unit
        ): TDialog {
            var originStopWinOrder : ContractOrder? = null
            var originStopLossOrder : ContractOrder? = null
            var originOrder = ContractOrder()
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_stop_win_loss_layout)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        val contract = ContractPublicDataAgent.getContract(position.instrument_id)
                            ?: return@setOnBindViewListener
                        //止盈
                        val itemStopWin = getView<StopWinLossItem>(R.id.item_stop_win)
                        itemStopWin.isStopWin = true
                        itemStopWin.liqPrice = position.liquidate_price

                        //止损
                        val itemStopLoss = getView<StopWinLossItem>(R.id.item_stop_loss)
                        itemStopLoss.isStopWin = false
                        itemStopLoss.liqPrice = position.liquidate_price

                        //根据仓位寻找止盈止损订单
                        findStopWinLossOrder(position) { order, stopWinOrder, stopLossOrder ->
                            originStopWinOrder = stopWinOrder
                            originStopLossOrder = stopLossOrder
                            originOrder = order
                            itemStopWin.bindContract(contract, order)
                            //填充价格
                            stopWinOrder?.let {
                                itemStopWin.checkTab = true
                                itemStopWin.triggerPrice = it.px
                                itemStopWin.isMarker = it.isMarketOrder
                                if(!it.isMarketOrder){
                                    itemStopWin.exPrice = it.exec_px
                                }
                            }
                            itemStopLoss.bindContract(contract, order)
                            stopLossOrder?.let {
                                itemStopLoss.checkTab = true
                                itemStopLoss.triggerPrice = it.px
                                itemStopLoss.isMarker = it.isMarketOrder
                                if(!it.isMarketOrder){
                                    itemStopLoss.exPrice = it.exec_px
                                }
                            }
                        }


                    }
                }
                .addOnClickListener(R.id.iv_close, R.id.bt_sure)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.iv_close -> {
                            tDialog.dismiss()
                        }
                        R.id.bt_sure -> {
                            //止盈
                            val itemStopWin =
                                viewHolder.getView<StopWinLossItem>(R.id.item_stop_win)
                            //止损
                            val itemStopLoss =
                                viewHolder.getView<StopWinLossItem>(R.id.item_stop_loss)

                            originOrder.with_mission = 0
                            if (itemStopWin.checkTab) {
                                if (!itemStopWin.inputBaseVerify()) {
                                    return@setOnViewClickListener
                                }
                                originOrder.with_mission = 1
                            }
                            if (itemStopLoss.checkTab) {
                                if (!itemStopLoss.inputBaseVerify()) {
                                    return@setOnViewClickListener
                                }
                                originOrder.with_mission = 2
                                //校验止损预警价格
                                val warnText = itemStopLoss.verifyStopLossWarnPx()
                                if (!TextUtils.isEmpty(warnText)) {
                                    DialogUtils.showCenterDialog(
                                        context = context,
                                        content = warnText,
                                        sureTitle = context.getString(R.string.str_continue_confirm),
                                        clickListener = object :
                                            DialogUtils.DialogBottomListener {
                                            override fun clickTab(tabType: Int) {
                                                if (itemStopWin.checkTab && itemStopLoss.checkTab) {
                                                    originOrder.with_mission = 3
                                                }
                                                callBack.invoke(originOrder,originStopWinOrder,originStopLossOrder)
                                                tDialog.dismiss()
                                            }
                                        })
                                    return@setOnViewClickListener
                                }
                            }

                            if (itemStopWin.checkTab && itemStopLoss.checkTab) {
                                originOrder.with_mission = 3
                            }
                            tDialog.dismiss()
                            callBack.invoke(originOrder,originStopWinOrder,originStopLossOrder)
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 打开平仓对话框
         */
        fun openClosePositionDialog(
            context: FragmentActivity,
            position: ContractPosition,
            callBack: (tebIndex: Int,vol:Int,px:Double) -> Unit
        ): TDialog {
            //数量 默认展示所有
            val allQty = MathHelper.round(position.cur_qty, 0)
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_close_position_layout)
                .setScreenWidthAspect(context, 1.0f)
                .setGravity(Gravity.BOTTOM)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog)
                .setOnBindViewListener { viewHolder: BindViewHolder? ->
                    viewHolder?.apply {
                        val contract: Contract =
                            ContractPublicDataAgent.getContract(position.instrument_id)
                                ?: return@setOnBindViewListener
                        val inputEntrustPrice =
                            getView<CommonInputLayout>(R.id.input_entrust_price)
                        val inputCloseVol = getView<CommonInputLayout>(R.id.input_close_vol)
                        //滑块
                        val seekBar = getView<BubbleSeekBar>(R.id.seek_layout)
                        //价格单位
                        inputEntrustPrice.rightText = contract.quote_coin
                        //价格 默认显示最新价格
                        val ticker: ContractTicker? =
                            ContractPublicDataAgent.getContractTicker(position.instrument_id)
                        if (ticker != null) {
                            inputEntrustPrice.inputText = ticker.last_px
                        }
                        inputCloseVol.inputText = "${allQty.toInt()}"
                        //滑动监听
                        seekBar.configBuilder
                            .progress(100.toFloat())
                            .build()
                        seekBar.onProgressChangedListener = object : BubbleSeekBar.OnProgressChangedListener {
                            override fun onProgressChanged(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                                val vol = (allQty * progress / 100).toInt().toString()
                                inputCloseVol.inputText = vol
                                inputCloseVol.getInputView().setSelection(vol.length)
                            }

                            override fun getProgressOnActionUp(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                            }

                            override fun getProgressOnFinally(bubbleSeekBar: BubbleSeekBar?, progress: Int, progressFloat: Float) {
                            }
                        }
                        //量输入监听
                        inputCloseVol.setEditTextChangedListener(object :
                            CommonInputLayout.EditTextChangedListener{
                            override fun onTextChanged(view: CommonInputLayout) {
                                var vol = inputCloseVol.inputText
                                if (TextUtils.isEmpty(vol)) {
                                    seekBar.setProgress(0.0f)
                                } else {
                                    if (vol.toFloat() > allQty) {
                                        vol = allQty.toInt().toString()
                                        inputCloseVol.inputText = vol
                                        inputCloseVol.getInputView().setSelection(vol.length)
                                    }
                                    if (allQty > 0.0) {
                                        seekBar.setProgress(min((vol.toFloat() / allQty * 100).toFloat(), 100.0f))
                                    }
                                }
                            }

                        })
                    }
                }
                .addOnClickListener(R.id.iv_close,R.id.bt_market_px_flat,R.id.tv_limit_px_close)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    val vol = viewHolder.getView<CommonInputLayout>(R.id.input_close_vol).inputText
                    val price = viewHolder.getView<CommonInputLayout>(R.id.input_entrust_price).inputText
                    when (view.id) {
                        R.id.iv_close -> {
                            tDialog.dismiss()
                        }
                        R.id.bt_market_px_flat -> {
                            //市价全平
                            if(preVerifyClosePositionInput(vol,price)){
                                callBack.invoke(0,vol.toInt(),price.toDouble())
                                tDialog.dismiss()
                            }
                        }
                        R.id.tv_limit_px_close -> {
                            //限价平仓
                            if(preVerifyClosePositionInput(vol,price)){
                                callBack.invoke(1,vol.toInt(),price.toDouble())
                                tDialog.dismiss()
                            }
                        }
                    }
                }
                .create()
                .show()
        }

        /**
         * 校验平仓输入
         */
        private fun preVerifyClosePositionInput(vol: String, price: String): Boolean {
            val context = ContractSDKAgent.context!!
            if (TextUtils.isEmpty(vol) || vol.toInt() == 0) {
                ToastUtil.shortToast(context,context.getString(R.string.str_volume_too_low))
                return false
            }
            if (TextUtils.isEmpty(price) || vol.toInt() == 0) {
                ToastUtil.shortToast(context,context.getString(R.string.str_price_too_low))
                return false
            }
            return true
        }





    /**
     * 打开已实现盈亏弹窗
     */
    fun openGainsBalanceDialog(
        context: FragmentActivity,
        position: ContractPosition,
        callBack: (tabIndex: Int) -> Unit
    ): TDialog {
        return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
            .setLayoutRes(R.layout.dialog_gains_balance_layout)
            .setScreenWidthAspect(context, 0.9f)
            .setGravity(Gravity.CENTER)
            .setDimAmount(0.8f)
            .setCancelableOutside(true)
            .setDialogAnimationRes(com.sl.ui.library.R.style.animate_dialog_scale)
            .setOnBindViewListener { viewHolder: BindViewHolder? ->
                viewHolder?.apply {
                    val contract = ContractPublicDataAgent.getContract(position.instrument_id)
                        ?: return@setOnBindViewListener
                    //资金费用
                    val tax =  dealMoney(position.tax.toDouble()*-1, 8)
                    setText(R.id.tv_tax,NumberUtil.getNumSymbol(tax)+" "+contract.quote_coin)
                    //手续费
                    val poundageFee =  dealMoney(position.poundageFee*-1, 8)
                    setText(R.id.tv_fee,NumberUtil.getNumSymbol(poundageFee)+" "+contract.quote_coin)
                    //平仓盈亏
                    val closeRateFee =  dealMoney(MathHelper.sub(MathHelper.sub(position.earnings.toDouble(),poundageFee) ,tax ),8)
                    setText(R.id.tv_close_profit,NumberUtil.getNumSymbol(NumberUtil.getDecimal(8).format(closeRateFee))+" "+contract.quote_coin)
                }
            }
            .addOnClickListener(R.id.tv_cancel, R.id.tv_sure)
            .setOnViewClickListener { viewHolder, view, tDialog ->
                when (view.id) {
                    R.id.tv_cancel -> {
                        tDialog.dismiss()
                        callBack.invoke(0)
                    }
                    R.id.tv_sure -> {
                        tDialog.dismiss()
                    }
                }
            }
            .create()
            .show()
    }


        /**
         * 小于0则向上取值、大于0则向下取值。
         */
       private fun dealMoney(px:String,point:Int) : Double{
            if(TextUtils.isEmpty(px)){
                return  0.0
            }
            var tempPx = px.toDouble()
            return dealMoney(tempPx,point)
        }

        private fun dealMoney(px:Double,point:Int) : Double{
            return  if (px >= 0) {
                MathHelper.round(px,point)
            } else {
                MathHelper.roundUp(px,point)
            }
        }
    }




}