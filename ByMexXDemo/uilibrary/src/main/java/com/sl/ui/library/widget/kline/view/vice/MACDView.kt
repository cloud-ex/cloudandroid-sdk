package com.sl.ui.library.widget.kline.view.vice

import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import com.sl.ui.library.widget.kline.view.KLineChartView
import com.sl.ui.library.widget.kline.base.IChartViewDraw
import com.sl.ui.library.widget.kline.base.IValueFormatter
import com.sl.ui.library.widget.kline.bean.vice.IMACD
import com.sl.ui.library.widget.kline.formatter.ValueFormatter
import com.sl.ui.library.widget.kline.view.BaseKLineChartView
import com.sl.ui.library.widget.kline.view.IFallRiseColor

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:17 AM
 * @Description:
 */
class MACDView(view: KLineChartView) : IChartViewDraw<IMACD>, IFallRiseColor {


    val TAG = MACDView::class.java.simpleName

    /**
     * macd 中柱子的宽度
     */
    private var mMACDWidth = 0f

    private val fallPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val risePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paint4DIF = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4DEA = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4MACD = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun drawTranslated(lastPoint: IMACD?, curPoint: IMACD, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {
        drawMACD(canvas, view, curX, curPoint.MACD)
        view.drawChildLine(canvas, paint4DIF, lastX, lastPoint?.DEA ?: 0f, curX, curPoint.DEA)
        view.drawChildLine(canvas, paint4DEA, lastX, lastPoint?.DIF ?: 0f, curX, curPoint.DIF)
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {

        val point = view.getItem(position) as IMACD
        var text = "MACD(12,26,9)  "
        canvas.drawText(text, x, y, view.textPaint)

        var textLen = x

        textLen += view.textPaint.measureText(text)
        text = "MACD:" + view.formatValue(point.MACD) + "  "
        canvas.drawText(text, textLen, y, paint4MACD)
        textLen += paint4MACD.measureText(text)
        text = "DIF:" + view.formatValue(point.DIF) + "  "
        canvas.drawText(text, textLen, y, paint4DEA)
        textLen += paint4DIF.measureText(text)
        text = "DEA:" + view.formatValue(point.DEA)
        canvas.drawText(text, textLen, y, paint4DIF)


    }

    override fun getMaxValue(point: IMACD): Float {
        return maxOf(point.MACD, point.DEA, point.DIF)

    }

    override fun getMinValue(point: IMACD): Float {
        return minOf(point.MACD, point.DEA, point.DIF)
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun setFallRiseColor(riseColor: Int, fallColor: Int) {
        fallPaint.color = fallColor
        risePaint.color = riseColor
    }

    /**
     * 画macd
     *
     * @param canvas
     * @param x
     * @param macd
     */
    private fun drawMACD(canvas: Canvas, view: BaseKLineChartView, x: Float, macd: Float) {
        var macdy = view.getChildY(macd)
        val r = mMACDWidth / 2
        var zeroy = view.getChildY(0f)
        Log.d(TAG, "==macdy:$macdy,r:$r,zeroy:$zeroy==value:=${macdy - zeroy}")
        if (macd > 0) {
            //               left   top   right  bottom
            canvas.drawRect(x - r, macdy, x + r, zeroy, risePaint)
        } else {
            canvas.drawRect(x - r, zeroy, x + r, macdy, fallPaint)
        }
    }

    override fun setTextSize(textSize: Float) {
        paint4DEA.textSize = textSize
        paint4DIF.textSize = textSize
        paint4MACD.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4DEA.strokeWidth = width
        paint4DIF.strokeWidth = width
        paint4MACD.strokeWidth = width
    }


    /**
     * 设置DIF颜色
     */
    fun setDIFColor(color: Int) {
        paint4DIF.color = color
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(color: Int) {
        paint4DEA.color = color
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(color: Int) {
        paint4MACD.color = color
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(MACDWidth: Float) {
        mMACDWidth = MACDWidth
    }

}