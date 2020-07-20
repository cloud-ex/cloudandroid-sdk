package com.sl.contract.library.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.contract.sdk.data.Contract
import com.contract.sdk.data.DepthData
import com.contract.sdk.extra.Contract.ContractCalculate
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.sl.contract.library.R
import com.sl.contract.library.helper.LogicContractSetting
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.widget.ViewWrapper
import org.jetbrains.anko.backgroundColor
import java.lang.Exception
import java.util.*

class BuySellContractAdapter(private val mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mFlag = 1 //1 buy ;2 sell
    private var mDecimals = 8
    private var mVolIndex = 2
    private var mPriceIndex = 2
    private var mMaxVol = 0.0
    private var mShowNum = 6
    private val mShowNews: MutableList<DepthData> = arrayListOf()
    private val mList = ArrayList<DepthData>()
    private var mContract: Contract? = null
    var dfPrice = NumberUtil.getDecimal(mPriceIndex)

    private val colorRed =  mContext.resources.getColor(R.color.main_red)
    private val colorGreen = mContext.resources.getColor(R.color.main_green)
    private var  itemWidth = 0

    private var greenLightMain = ContextCompat.getColor(mContext,R.color.main_green_light)
    private var redLightMain = ContextCompat.getColor(mContext,R.color.main_red_light)



    fun getList():ArrayList<DepthData>{
        return mList
    }


    class BuySellContractHolder(var vRoot: View, type: Int) : RecyclerView.ViewHolder(vRoot) {
        var tvVolume: TextView
        var tvPrice: TextView
        var pbVolume: FrameLayout
        var rootView : View
        var fl_layout : View

        init {
            pbVolume = vRoot.findViewById(R.id.pb_volume)
            tvVolume = vRoot.findViewById(R.id.tv_volume)
            tvPrice = vRoot.findViewById(R.id.tv_price)
            fl_layout = vRoot.findViewById(R.id.fl_layout)
            rootView = vRoot
        }
    }

    fun bindContract(contract: Contract){
        mContract = contract
        mVolIndex = mContract!!.vol_index
        mPriceIndex = mContract!!.price_index - 1
        mDecimals = mContract!!.price_index
        dfPrice = NumberUtil.getDecimal(mPriceIndex)
    }

    fun setData(list: List<DepthData>?, flag: Int, show_num: Int) {
        try {

            mFlag = flag
            mShowNum = show_num
            if (show_num == 0) {
                mShowNews.clear()
                notifyDataSetChanged()
                return
            }
            if (list != null) {
                mList.clear()
                mList.addAll(list)
                mMaxVol = 0.0
                mShowNews.clear()
                var index = 0
                for (i in mList.indices) {
                    val item = mList[i]
                    if (index < show_num) {
                        val vol = MathHelper.round(item.vol.toString(), mDecimals)
                        if (vol > 0) {
                            if (vol > mMaxVol) {
                                mMaxVol = vol
                            }
                            index++
                            mShowNews.add(mList[i])
                        }
                    } else {
                        break
                    }
                }

                if (mFlag == 1) {
                    for (i in mShowNews.size until show_num) {
                        val depthData = DepthData()
                        depthData.price = "0.0"
                        depthData.vol = 0
                        mShowNews.add(depthData)
                    }
                } else if (mFlag == 2) {
                    val emptyNum = show_num - mShowNews.size
                    for (i in 0 until emptyNum) {
                        val depthData = DepthData()
                        depthData.price = "0.0"
                        depthData.vol = 0
                        mShowNews.add( depthData)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return mShowNews.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as BuySellContractHolder

        if(itemWidth == 0){
            itemWidth = itemViewHolder.fl_layout.width
        }

        val data = if (mFlag == 1)  mShowNews[position] else  mShowNews[mShowNum - position - 1]
        if (data.price.toDouble().compareTo(0.0) == 0 || data.vol.compareTo(0) == 0) {
            itemViewHolder.tvVolume.text = "--,--"
            itemViewHolder.tvPrice.text = "--,--"
           // itemViewHolder.pbVolume.progress = 0
            itemViewHolder.tvPrice.setTextColor(if(mFlag == 1) colorGreen else colorRed)
          //  itemViewHolder.pbVolume.progressDrawable =  if (mFlag == 1)mContext.resources.getDrawable(R.drawable.sl_buy_progress) else mContext.resources.getDrawable(R.drawable.sl_sell_progress)
//            itemViewHolder.tvPrice.setOnClickListener(null)
//            itemViewHolder.tvVolume.setOnClickListener(null)
            return
        }



        val maxVol = mMaxVol
        val vol = MathHelper.round(data.vol.toString(), mVolIndex)
        val price = MathHelper.round(data.price, mPriceIndex)
        itemViewHolder.tvVolume.text = ContractCalculate.getVolUnitNoSuffix(mContract, vol, price,
            LogicContractSetting.getContractUint(mContext))
        itemViewHolder.tvPrice.text = dfPrice.format(price)
       // itemViewHolder.pbVolume.progress = 100 - (100 * vol / maxVol).toInt()
        var viewWrapper =  itemViewHolder.pbVolume.tag
        if(viewWrapper == null){
            viewWrapper = ViewWrapper(itemViewHolder.pbVolume)
            itemViewHolder.pbVolume.tag = viewWrapper
        }
       // SDKLogUtil.d("libin","fl_layout:"+itewWidth+"=="+curr.toInt()+";"+vol / maxVol)
        (viewWrapper as ViewWrapper).startAnim((itemWidth*( vol / maxVol)).toInt())
        itemViewHolder.tvPrice.setTextColor(if(mFlag == 1) colorGreen else colorRed)
        itemViewHolder.pbVolume.backgroundColor =  if (mFlag == 1) greenLightMain else  redLightMain
        itemViewHolder.rootView.setOnClickListener(View.OnClickListener {
            EventBusUtil.post(MessageEvent(MessageEvent.SELECT_DEPTH_PRICE_notify,price))
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.sl_item_buy_sell_contract, parent, false)
        return BuySellContractHolder(v, viewType)
    }

}