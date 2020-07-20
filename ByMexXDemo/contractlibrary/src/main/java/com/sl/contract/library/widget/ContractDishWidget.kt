package com.sl.contract.library.widget

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IdRes
import com.contract.sdk.data.Contract
import com.contract.sdk.data.DepthData
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.NumberUtil
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.widget.ViewWrapper
import java.util.ArrayList

/**
 * 合约深度widget
 */
class ContractDishWidget : LinearLayout {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var viewCacheMap = HashMap<Int, View>()
    private val views: SparseArray<View> = SparseArray()

    private val mSells = ArrayList<DepthData>()
    private val mBuys = ArrayList<DepthData>()

    private var mContract: Contract? = null
    private var mPriceIndex = 2
    private var mVolIndex = 2
    private var mMaxBuyVol = 0
    private var mMaxSellVol = 0
    private var emptyData = DepthData()
    private var dfVol = NumberUtil.getDecimal(mVolIndex)
    private var dfPrice = NumberUtil.getDecimal(mPriceIndex)
    private var itemWidth = 0

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        orientation = VERTICAL
        initAttrs(attrs)
    }

    var itemCount = 0
        set(value) {
            field = value
            //初始化Item
            removeAllViews()
            for (index in 0 until itemCount) {
                val viewItem = layoutInflater.inflate(R.layout.view_contract_dish_item_layout, null)
                viewCacheMap[index] = viewItem
                addView(viewItem)
            }
        }


    private fun updateDishView() {
        for (index in 0 until itemCount) {
            updateItemView(index, mBuys[index], mSells[index])
        }
    }

    private fun updateItemView(index: Int, buyData: DepthData, sellData: DepthData) {
        val itemView = viewCacheMap[index] as ViewGroup
        val tvBuyVol = getViewOrNull<TextView>(itemView, R.id.tv_buy_vol)
        val viewBuyProgress = getViewOrNull<View>(itemView, R.id.view_buy_progress)
        val tvBuyPrice = getViewOrNull<TextView>(itemView, R.id.tv_buy_price)
        val viewSellProgress = getViewOrNull<View>(itemView, R.id.view_sell_progress)
        val tvSellPrice = getViewOrNull<TextView>(itemView, R.id.tv_sell_price)
        val tvSellVol = getViewOrNull<TextView>(itemView, R.id.tv_sell_vol)
        val llBuyLayout = getViewOrNull<FrameLayout>(itemView, R.id.ll_buy_layout)

        if (itemWidth == 0) {
            itemWidth = llBuyLayout!!.width
        }

        // 0 张  1币
        val volType = LogicContractSetting.getContractUint(context)
        //买盘
        val buyVol = buyData.vol
        val buyPrice = buyData.price.toDouble()

        if (buyData.vol == 0) {
            tvBuyVol!!.text = "--"
            tvBuyPrice!!.text = "--"
        } else {
            if (volType == 0) {
                tvBuyVol!!.text = dfVol.format(buyVol)
            } else {
                tvBuyVol!!.text = ContractCalculate.getVolUnitNoSuffix(
                    mContract,
                    buyVol.toDouble(),
                    buyPrice,
                    LogicContractSetting.getContractUint(context)
                )
            }
        }
        tvBuyPrice!!.text = dfPrice.format(buyPrice)

        var buyViewWrapper = viewBuyProgress!!.tag
        if (buyViewWrapper == null) {
            buyViewWrapper = ViewWrapper(viewBuyProgress)
            viewBuyProgress!!.tag = buyViewWrapper
        } else {
        }
        val buyCurr = itemWidth * (buyVol / mMaxBuyVol.toDouble())
        (buyViewWrapper as ViewWrapper).startAnim(buyCurr.toInt())

        //卖盘
        val sellVol = sellData.vol
        val sellPrice = sellData.price.toDouble()

        if (sellData.vol == 0) {
            tvSellVol!!.text = "--"
            tvSellPrice!!.text = "--"
        } else {
            if (volType == 0) {
                tvSellVol!!.text = dfVol.format(sellVol)
            } else {
                tvSellVol!!.text = ContractCalculate.getVolUnitNoSuffix(
                    mContract,
                    sellVol.toDouble(),
                    sellPrice,
                    LogicContractSetting.getContractUint(context)
                )
            }
            tvSellPrice!!.text = dfPrice.format(sellPrice)

        }

        var sellViewWrapper = viewSellProgress!!.tag
        if (sellViewWrapper == null) {
            sellViewWrapper = ViewWrapper(viewSellProgress)
            viewSellProgress.tag = sellViewWrapper
        }
        val sellCurr = itemWidth * (sellVol / mMaxSellVol.toDouble())
        (sellViewWrapper as ViewWrapper).startAnim(sellCurr.toInt())

    }

    private fun <T : View> getViewOrNull(itemView: View, @IdRes viewId: Int): T? {
        val view = views.get(viewId)
        if (view == null) {
            itemView.findViewById<T>(viewId)?.let {
                //   views.put(viewId, it)
                return it
            }
        }
        return view as? T
    }


    fun setData(sells: List<DepthData>, buys: List<DepthData>, contract: Contract) {
        if (mContract == null || mContract!!.instrument_id != contract.instrument_id) {
            mContract = contract
            mPriceIndex = contract.price_index - 1
            mVolIndex = contract.vol_index
            dfVol = NumberUtil.getDecimal(mVolIndex)
            dfPrice = NumberUtil.getDecimal(mPriceIndex)
        }
        //遍历 寻找卖盘最大量
        mSells.clear()
        mSells.addAll(sells)
        mMaxSellVol = 0
        val sellCount = mSells.size
        for (index in 0 until itemCount) {
            if (index < sellCount) {
                if (mSells[index].vol > mMaxSellVol) {
                    mMaxSellVol = mSells[index].vol
                }
            } else {
                mSells.add(emptyData)
            }
        }
        //遍历 寻找买盘最大量
        mMaxBuyVol = 0
        mBuys.clear()
        mBuys.addAll(buys)
        val buyCount = mBuys.size
        for (index in 0 until itemCount) {
            if (index < buyCount) {
                if (mBuys[index].vol > mMaxBuyVol) {
                    mMaxBuyVol = mBuys[index].vol
                }
            } else {
                mBuys.add(emptyData)
            }
        }

        updateDishView()
    }


    private fun initAttrs(attrs: AttributeSet?) {
        attrs?.let {
            var typedArray = context.obtainStyledAttributes(it, R.styleable.contractDepth, 0, 0)
            itemCount = typedArray.getInt(R.styleable.contractDepth_depth_count, 5)
        }
    }


}