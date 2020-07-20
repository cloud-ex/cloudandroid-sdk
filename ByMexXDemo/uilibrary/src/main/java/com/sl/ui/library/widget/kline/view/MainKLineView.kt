package com.sl.ui.library.widget.kline.view

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.sl.ui.library.R
import com.sl.ui.library.utils.BigDecimalUtils
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.DisplayUtils
import com.sl.ui.library.utils.StringUtil
import com.sl.ui.library.widget.kline.base.IChartViewDraw
import com.sl.ui.library.widget.kline.base.IValueFormatter
import com.sl.ui.library.widget.kline.bean.CandleBean
import com.sl.ui.library.widget.kline.bean.IKLine
import com.sl.ui.library.widget.kline.formatter.ValueFormatter
import org.jetbrains.anko.dip
import java.math.BigDecimal

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:28 AM
 * @Description: K线主图
 */
class MainKLineView(kLineChartView: KLineChartView) : IChartViewDraw<CandleBean>, IFallRiseColor {

    val TAG = MainKLineView::class.java.simpleName

    private var candleWidth = 0f

    private var candleLineWidth = 0f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val fallPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val risePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA5 = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA10 = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4MA30 = Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * marker
     */
    private val markerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerValuePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 蜡烛线是否是实线
    var isCandleSolid = true
    // 是否分时
    var isLine = false
        set(value) {
            field = value
            if (isLine != value) {
                isLine = value
                if (isLine) {
                    kChartView.setCandleWidth(
                            mContext.dip(7f).toFloat())
                } else {
                    kChartView.setCandleWidth(
                            mContext.dip(6).toFloat())
                }
            }

        }

    lateinit var mContext: Context

    var status = MainKlineViewStatus.MA


    lateinit var kChartView: KLineChartView

    init {
        mContext = kLineChartView.context
        kChartView = kLineChartView
        mLinePaint.color = ContextCompat.getColor(mContext, R.color.main_yellow)
        paint.color = ContextCompat.getColor(mContext, R.color.main_yellow_8)
        markerBorderPaint.color = ContextCompat.getColor(mContext, R.color.marker_border)
        markerBorderPaint.style = Paint.Style.STROKE
    }


    override fun drawTranslated(lastPoint: CandleBean?, curPoint: CandleBean, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        if (isLine) {

            view.drawMainLine(canvas, mLinePaint, lastX, lastPoint?.closePrice
                    ?: 0f, curX, curPoint.closePrice)

            /**
             * 画分时线
             */
            view.drawMainMinuteLine(canvas, paint, lastX, lastPoint?.closePrice
                    ?: 0f, curX, curPoint.closePrice)
        } else {
            drawCandle(view, canvas, curX, curPoint.highPrice, curPoint.lowPrice, curPoint.openPrice, curPoint.closePrice)

            if (status == MainKlineViewStatus.MA) {
                //画ma5
                if (lastPoint!!.price4MA5 != 0f) {
                    view.drawMainLine(canvas, paint4MA5, lastX, lastPoint.price4MA5, curX, curPoint.price4MA5)
                }
                //画ma10
                if (lastPoint.price4MA10 != 0f) {
                    view.drawMainLine(canvas, paint4MA10, lastX, lastPoint.price4MA10,
                            curX, curPoint.price4MA10)
                }
                //画ma30
                if (lastPoint.price4MA30 != 0f) {
                    view.drawMainLine(canvas, paint4MA30, lastX, lastPoint.price4MA30,
                            curX, curPoint.price4MA30)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                //画boll
                if (lastPoint!!.up != 0f) {
                    view.drawMainLine(canvas, paint4MA5, lastX, lastPoint.up, curX, curPoint.up)
                }
                if (lastPoint.mb != 0f) {
                    view.drawMainLine(canvas, paint4MA10, lastX, lastPoint.mb, curX, curPoint.mb)
                }
                if (lastPoint.dn != 0f) {
                    view.drawMainLine(canvas, paint4MA30, lastX, lastPoint.dn, curX, curPoint.dn)
                }
            }
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as IKLine?
        var textHeight = y
        textHeight -= 5
        if (isLine) {
            if (status == MainKlineViewStatus.MA) {
                if (point!!.price4MA60 != 0f) {
                    val text = "MA60:" + view.formatValue(point.price4MA60) + "     "
                    canvas.drawText(text, x + DisplayUtils.dip2px(mContext,5f), textHeight, paint4MA10)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                if (point!!.mb != 0f) {
                    val text = "BOLL:" + view.formatValue(point.mb) + "     "
                    canvas.drawText(text, +DisplayUtils.dip2px(mContext,5f).toFloat(), textHeight, paint4MA10)
                }
            }
        } else {
            if (status == MainKlineViewStatus.MA) {
                var text: String
                var textLen = x
                if (point!!.price4MA5 != 0f) {
                    text = "MA5:" + view.formatValue(point.price4MA5)
                    canvas.drawText(text, textLen + DisplayUtils.dip2px(mContext,15f), textHeight, paint4MA5)
                    textLen += paint4MA5.measureText(text)
                }
                if (point.price4MA10 != 0f) {
                    text = "MA10:" + view.formatValue(point.price4MA10)
                    canvas.drawText(text, textLen + DisplayUtils.dip2px(mContext,25f), textHeight, paint4MA10)
                    textLen += paint4MA10.measureText(text)
                }
                if (point.price4MA30 != 0f) {
                    text = "MA30:" + view.formatValue(point.price4MA30)
                    canvas.drawText(text, textLen + DisplayUtils.dip2px(mContext,35f), textHeight, paint4MA30)
                }
            } else if (status == MainKlineViewStatus.BOLL) {
                if (point!!.mb != 0f) {
                    var textLen = x
                    var text = "BOLL:" + view.formatValue(point.mb) + "     "
                    canvas.drawText(text, textLen + DisplayUtils.dip2px(mContext,5f), textHeight, paint4MA10)
                    textLen += paint4MA5.measureText(text)
                    text = "UB:" + view.formatValue(point.up) + "     "
                    canvas.drawText(text, textLen, textHeight, paint4MA5)
                    textLen += paint4MA10.measureText(text)
                    text = "LB:" + view.formatValue(point.dn)
                    canvas.drawText(text, textLen, textHeight, paint4MA30)
                }
            }
        }
        if (view.isLongPress) {
            drawMarker(view, canvas)
        }

    }

    override fun getMaxValue(point: CandleBean): Float {
        return if (status == MainKlineViewStatus.BOLL) {

            when (point.up) {
                Float.NaN -> {
                    if (point.mb == 0f) point.highPrice else point.mb
                }

                0f -> {
                    point.highPrice
                }

                else -> {
                    point.up
                }
            }

        } else {
            maxOf(point.highPrice, point.price4MA30)
        }
    }

    override fun getMinValue(point: CandleBean): Float {
        return if (status == MainKlineViewStatus.BOLL) {
            if (point.dn == 0f) point.lowPrice else point.dn
        } else {
            if (point.price4MA30 == 0f) point.lowPrice else Math.min(point.price4MA30, point.lowPrice)
        }
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun setTextSize(textSize: Float) {
        paint4MA30.textSize = textSize
        paint4MA10.textSize = textSize
        paint4MA5.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4MA30.strokeWidth = width
        paint4MA10.strokeWidth = width
        paint4MA5.strokeWidth = width
        mLinePaint.strokeWidth = width
        markerBorderPaint.strokeWidth = width
    }

    override fun setFallRiseColor(riseColor: Int, fallColor: Int) {
        fallPaint.color = fallColor
        risePaint.color = riseColor
    }


    /**
     * 画Candle
     *
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    private fun drawCandle(view: BaseKLineChartView, canvas: Canvas, x: Float, high: Float, low: Float, open: Float, close: Float) {

        Log.d(TAG, "============画蜡烛线啦...======")

        var high = high
        var low = low
        var open = open
        var close = close
        high = view.getMainY(high)
        low = view.getMainY(low)
        open = view.getMainY(open)
        close = view.getMainY(close)
        val r = candleWidth / 2
        val lineR = candleLineWidth / 2
        if (open > close) {


            //实心
            if (isCandleSolid) {
                canvas.drawRect(x - r, close, x + r, open, fallPaint)
                canvas.drawRect(x - lineR, high, x + lineR, low, fallPaint)
            } else {
                fallPaint.strokeWidth = candleLineWidth
                canvas.drawLine(x, high, x, close, fallPaint)
                canvas.drawLine(x, open, x, low, fallPaint)
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, fallPaint)
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, fallPaint)
                fallPaint.strokeWidth = candleLineWidth * view.scaleX
                canvas.drawLine(x - r, open, x + r, open, fallPaint)
                canvas.drawLine(x - r, close, x + r, close, fallPaint)
            }

        } else if (open < close) {
            canvas.drawRect(x - r, open, x + r, close, risePaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, risePaint)
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, fallPaint)
            canvas.drawRect(x - lineR, high, x + lineR, low, fallPaint)
        }
    }

    /**
     *
     * MarkerView
     * @param view
     * @param canvas
     */
    private fun drawMarker(view: BaseKLineChartView, canvas: Canvas) {
        val metrics = markerTitlePaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent

        val index = view.selectedIndex
        val padding = mContext.dip(5f).toFloat()
        val margin = mContext.dip(5f).toFloat()
        /**
         * 设置MarkerView的宽度
         */
        var width = mContext.dip(108).toFloat()
        width += padding * 2


        val left: Float
        val top = margin + view.topPadding
        val height = padding * 8 + textHeight * 9

        val point = view.getItem(index) as IKLine

        var map = linkedMapOf<String, String>()
        map[mContext.getString(R.string.kline_text_dealTime)] = view.adapter?.getDate(index).toString()
        // 开
        map[mContext.getString(R.string.kline_text_open)] = BigDecimalUtils.showSNormal(point.openPrice.toString())
        // 高
        map[mContext.getString(R.string.kline_text_high)] = BigDecimalUtils.showSNormal(point.highPrice.toString())
        // 低
        map[mContext.getString(R.string.kline_text_low)] = BigDecimalUtils.showSNormal(point.lowPrice.toString())
        // 收
        map[mContext.getString(R.string.kline_text_close)] = BigDecimalUtils.showSNormal(point.closePrice.toString())
        //涨幅额
        var lines = BigDecimalUtils.sub(point.closePrice.toString(), point.openPrice.toString()).toPlainString()
        map[mContext.getString(R.string.str_change_value)] = getAbsoluteText4Kline(lines)

        //涨幅度
        var fist = BigDecimalUtils.div(lines, point.openPrice.toString()).toPlainString()

        map[mContext.getString(R.string.str_change_rate)] = getRoseText4Kline(fist)
        //成交量
        map[mContext.getString(R.string.str_deal_volume)] = BigDecimalUtils.showSNormal(point.volume.toString())


        val x = view.translateXtoX(view.getX(index))
        left = if (x > view.chartWidth / 2) {
            margin
        } else {
            view.chartWidth - width - margin
        }
        val r = RectF(left, top, left + width, top + height + padding)
        canvas.drawRoundRect(r, padding, padding, markerBgPaint)
        Log.d(TAG, "=========padding:$padding=====")
        canvas.drawRoundRect(r, DisplayUtils.dip2px(mContext,1.5f).toFloat(), DisplayUtils.dip2px(mContext,1.5f).toFloat(), markerBorderPaint)

        var y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2
        /**
         * 设置文字R->L
         */
        markerValuePaint.textAlign = Paint.Align.RIGHT
        for ((k, v) in map) {
            if (mContext.getString(R.string.str_change_rate) == k || mContext.getString(R.string.str_change_value) == k) {
                markerValuePaint.color = ColorUtils.getMainColorType(mContext,!v.contains("-"))
            } else {
                markerValuePaint.color = ContextCompat.getColor(mContext,R.color.chart_max_min)
            }
            canvas.drawText(k, left + padding, y, markerTitlePaint)
            canvas.drawText(v, width - padding + left, y, markerValuePaint)
            y += textHeight + padding
        }

    }

    private fun getRoseText4Kline(rose: String?): String {
        if (!StringUtil.isNumeric(rose)) return ""
        var lines = rose ?: ""
        val roseValue = BigDecimalUtils.divForDown(BigDecimal(rose).multiply(BigDecimal("100")).toPlainString(), 2)
        val compareTo = BigDecimalUtils.compareTo(roseValue.toPlainString(), "0")
        when (compareTo) {
            -1 -> {
                lines = "${roseValue.toPlainString()}%"
            }

            0 -> {
                lines = roseValue.toPlainString() + "%"
            }

            1 -> {
                lines = "+${roseValue.toPlainString()}%"
            }
        }
        return lines
    }

    fun getAbsoluteText4Kline(rose: String): String {
        if (!StringUtil.isNumeric(rose)) return ""
        var lines = rose
        val compareTo = BigDecimalUtils.compareTo(rose, "0")
        when (compareTo) {
            -1, 0 -> {
                lines = rose
            }

            1 -> {
                lines = "+${rose}"
            }
        }
        return lines
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        this.candleWidth = candleWidth
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        this.candleLineWidth = candleLineWidth
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        this.paint4MA5.color = color
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        this.paint4MA10.color = color
    }

    /**
     * 设置ma30颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        this.paint4MA30.color = color
    }

    /**
     * 设置选择器标题文字颜色(时间，开，高，低，收)
     * @param color
     */
    fun setMarkerTitleColor(color: Int) {
        markerTitlePaint.color = color
    }

    /**
     * 设置选择器数值文字颜色
     * @param color
     */
    fun setMarkerValueColor(color: Int) {
        markerValuePaint.color = color
    }

    /**
     * 设置选择器文字大小
     * @param textSize
     */
    fun setMarkerTextSize(textSize: Float) {
        markerTitlePaint.textSize = textSize
        markerValuePaint.textSize = textSize
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setMarkerBackgroundColor(color: Int) {
        markerBgPaint.color = color
    }


}