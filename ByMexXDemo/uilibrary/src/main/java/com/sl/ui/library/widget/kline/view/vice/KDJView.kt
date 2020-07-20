package com.sl.ui.library.widget.kline.view.vice

import android.graphics.Canvas
import android.graphics.Paint
import com.sl.ui.library.widget.kline.view.KLineChartView
import com.sl.ui.library.widget.kline.base.IChartViewDraw
import com.sl.ui.library.widget.kline.base.IValueFormatter
import com.sl.ui.library.widget.kline.bean.vice.IKDJ
import com.sl.ui.library.widget.kline.formatter.ValueFormatter
import com.sl.ui.library.widget.kline.view.BaseKLineChartView

/**
 * @Author: Bertking
 * @Date：2019/3/11-11:02 AM
 * @Description: KDJ视图
 */
class KDJView(view: KLineChartView) : IChartViewDraw<IKDJ> {
    val TAG = KDJView::class.java.simpleName

    private val paint4K = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4D = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paint4J = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun drawTranslated(lastPoint: IKDJ?, curPoint: IKDJ, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {

        if (lastPoint?.K != 0f) {
            view.drawChildLine(canvas, paint4K, lastX, lastPoint?.K ?: 0f, curX, curPoint.K)
        }
        if (lastPoint?.D != 0f) {
            view.drawChildLine(canvas, paint4D, lastX, lastPoint?.D ?: 0f, curX, curPoint.D)
        }
        if (lastPoint?.J != 0f) {
            view.drawChildLine(canvas, paint4J, lastX, lastPoint?.J ?: 0f, curX, curPoint.J)
        }

    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as IKDJ
        if (point.K != 0f) {
            var text = "KDJ(14,1,3)  "
            canvas.drawText(text, x, y, view.textPaint)
            var textLen = x
            textLen += view.textPaint.measureText(text)
            text = "K:" + view.formatValue(point.K) + " "
            canvas.drawText(text, textLen, y, paint4K)
            textLen += paint4K.measureText(text)
            if (point.D != 0f) {
                text = "D:" + view.formatValue(point.D) + " "
                canvas.drawText(text, textLen, y, paint4D)
                textLen += paint4D.measureText(text)
                text = "J:" + view.formatValue(point.J) + " "
                canvas.drawText(text, textLen, y, paint4J)
            }
        }
    }

    override fun getMaxValue(point: IKDJ): Float {
        return maxOf(point.K, point.D, point.J)
    }

    override fun getMinValue(point: IKDJ): Float {
        return minOf(point.K, point.D, point.J)
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }


    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        paint4K.color = color
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        paint4D.color = color
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        paint4J.color = color
    }

    /**
     * 设置曲线宽度
     */
    override fun setLineWidth(width: Float) {
        paint4K.strokeWidth = width
        paint4D.strokeWidth = width
        paint4J.strokeWidth = width
    }

    /**
     * 设置文字大小
     */
    override fun setTextSize(textSize: Float) {
        paint4K.textSize = textSize
        paint4D.textSize = textSize
        paint4J.textSize = textSize
    }
}