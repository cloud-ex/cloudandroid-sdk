package com.sl.contract.library.fragment.detail

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.contract.sdk.data.Contract
import com.contract.sdk.data.DepthData
import com.contract.sdk.extra.dispense.DataDepthHelper
import com.contract.sdk.utils.MathHelper
import com.contract.sdk.utils.NumberUtil
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.sl.contract.library.R
import com.sl.contract.library.widget.depth.DepthMarkView
import com.sl.contract.library.widget.depth.DepthYValueFormatter
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.utils.ColorUtils
import kotlinx.android.synthetic.main.fragment_contract_depth_line.*
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min

/**
 * 合约深度
 */
class ContractDepthFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_contract_depth_line
    }

    private var pxDecimal = NumberUtil.getDecimal(-1)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun initView() {
        initDepthChart()
        DataDepthHelper.instance?.getDepthSource(count = 15) { buyList, sellList ->
            bindDepthData(buyList, sellList)
        }
    }



    fun bindContract(contract: Contract) {
        clearDepthChart()
        pxDecimal = NumberUtil.getDecimal(contract.price_index-1)
    }

    fun bindDepthData(buyList: ArrayList<DepthData>, sellList: ArrayList<DepthData>) {
        if (buyList.size >= 2 && sellList.size >= 2) {
            initDepthChartView(buyList,sellList)
        }
    }

    private fun initDepthChartView(buyList: ArrayList<DepthData>, sellList: ArrayList<DepthData>) {
        if(depth_chart == null){
            return
        }
        try {

            val sells = java.util.ArrayList<DepthData>()
            val buys = java.util.ArrayList<DepthData>()
            var minCount = min(buyList.size, sellList.size)
            //取从小到大前20
            sells.addAll(sellList.subList(0, min(15, minCount)))
            //取从大到小前20
            buys.addAll(buyList.subList(0, min(15, minCount)))
            minCount = sells.size
            //买
            val valuesBuy = java.util.ArrayList<Entry>()
            var maxBuyVol = 0.0
            var maxBuyPrice = MathHelper.round(buys[0].price, 6)
            var minBuyPrice = MathHelper.round(buys.last().price, 6)

            //卖
            val valuesSell = java.util.ArrayList<Entry>()
            var maxSellVol = 0.0
            var maxSellPrice =  MathHelper.round(sells.last().price, 6)
            var minSellPrice =  MathHelper.round(sells[0].price, 6)


            var closePrice = (maxBuyPrice + minSellPrice) / 2
            for (i in 0 until minCount) {
                var info = buys[i]
                maxBuyVol += info.vol.toDouble()
                valuesBuy.add(0, Entry(((minCount-i)).toFloat(), maxBuyVol.toFloat(), info.price))
            }

            for (i in 0 until minCount) {
                var info = sells[i]
                val vol = MathHelper.round(info.vol.toString(), 6)
                val price = MathHelper.round(info.price, 6)
                maxSellVol += info.vol.toDouble()
                valuesSell.add(Entry(((i+minCount)).toFloat(), maxSellVol.toFloat(), price))
            }


            val maxVol = max(maxBuyVol, maxSellVol)

            val yAxis = depth_chart.axisRight
            yAxis.axisMinimum = 0f
            yAxis.axisMaximum = maxVol.toFloat() * 1.1f

            val buyLineDataSet = lineDataSet(valuesBuy, true)
            val sellLineDataSet = lineDataSet(valuesSell, false)
            //        LineData表示一个LineChart的所有数据(即一个LineChart中所有折线的数据)
            val lineData = LineData(buyLineDataSet, sellLineDataSet)

            depth_chart?.data = lineData
            depth_chart?.invalidate()

            tv_buy_price?.text = minBuyPrice.toString()
            tv_sell_price?.text = maxSellPrice.toString()
            tv_close_price?.text = pxDecimal.format(closePrice)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置lineDataSet  in深度图
     */
    private fun lineDataSet(yData: java.util.ArrayList<Entry>, isBuy: Boolean): LineDataSet {
        val lineDataSet: LineDataSet?
        if (isBuy) {
            lineDataSet = LineDataSet(yData, "")
            lineDataSet.color = mainColorRise
            lineDataSet.fillColor = mainColorRise
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = mainColorRise

        } else {
            lineDataSet = LineDataSet(yData, "")
            lineDataSet.color = mainColorDown
            lineDataSet.fillColor = mainColorDown
            /**
             * 设置折线的颜色
             */
            lineDataSet.color = mainColorDown
        }

        lineDataSet.fillAlpha = 26
        /**
         * 是否填充折线以及填充色设置
         */
        lineDataSet.setDrawFilled(true)

        /**
         * 控制MarkView的显示与隐藏
         * 点击是否显示高亮线
         */
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = Color.TRANSPARENT


        /**
         * 设置折线的宽度
         */
        lineDataSet.lineWidth = 1.0f


        /**
         * 隐藏每个数据点的值
         */
        lineDataSet.setDrawValues(false)

        /**
         * 数据点是否用小圆圈表示
         */
        lineDataSet.setDrawCircles(false)
        return lineDataSet
    }

    /**
     * 清理深度
     */
    fun clearDepthChart() {
        depth_chart?.clear()
        depth_chart?.notifyDataSetChanged()
        depth_chart?.invalidate()
    }

    /**
     * 配置深度图基本属性
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initDepthChart() {
        depth_chart?.setNoDataText(getString(R.string.str_no_data))
        depth_chart?.setNoDataTextColor(resources.getColor(R.color.normal_text_color))
        depth_chart?.setTouchEnabled(true)
        /**
         * 图例 的相关设置
         */
        val legend = depth_chart.legend
        legend.isEnabled = false
        /**
         * 是否缩放
         */
        depth_chart?.setScaleEnabled(false)
        /**
         * X,Y同时缩放
         */
        depth_chart?.setPinchZoom(false)

        /**
         * 关闭图表的描述信息
         */
        depth_chart?.description?.isEnabled = false
        // 打开触摸手势
        depth_chart?.setTouchEnabled(true)
        depth_chart.isLongClickable = true
        depth_chart.isNestedScrollingEnabled = false

        /**
         * X
         */
        val xAxis: XAxis = depth_chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        //xAxis.setLabelCount(3, true)
        // 不绘制竖直方向的方格线
        xAxis.setDrawGridLines(false)
        //x坐标轴不可见
        xAxis.isEnabled = true
        //禁止x轴底部标签
        xAxis.setDrawLabels(true)
        //最小的间隔设置
        xAxis.textColor = ContextCompat.getColor(context!!, R.color.normal_text_color)
        xAxis.textSize = 10f
        //在绘制时会避免“剪掉”在x轴上的图表或屏幕边缘的第一个和最后一个坐标轴标签项。
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setDrawAxisLine(false)
        /**
         * Y
         */
        depth_chart.axisRight.isEnabled = true
        depth_chart.axisLeft.isEnabled = false
        /**********左边Y轴********/
        depth_chart.axisLeft.axisMinimum = 0f
        /**********右边Y轴********/
        val yAxis = depth_chart.axisRight
        yAxis.setDrawGridLines(false)
        yAxis.setDrawAxisLine(false)
        //设置Y轴的Label显示在图表的内侧还是外侧，默认外侧
        yAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //不绘制水平方向的方格线
        yAxis.textColor =  ContextCompat.getColor(context!!, R.color.normal_text_color)
        yAxis.textSize = 10f
        //设置Y轴显示的label个数
        yAxis.setLabelCount(6, true)
        //控制上下左右坐标轴显示的距离
        depth_chart?.setViewPortOffsets(0f, 15f, 0f, 6f)
        yAxis.valueFormatter = DepthYValueFormatter()


        depth_chart?.setOnClickListener {
            if (depth_chart.marker != null) {
                depth_chart.marker = null
            }
        }

        depth_chart?.setOnLongClickListener {
            val mv = DepthMarkView(mActivity!!, R.layout.layout_depth_marker)
            mv.chartView = depth_chart // For bounds control
            depth_chart?.marker = mv // Set the marker to the ch
            false
        }
    }


}