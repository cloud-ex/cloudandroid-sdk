package com.sl.ui.library.widget.kline.view.vice

import android.graphics.Canvas
import android.graphics.Paint
import com.sl.ui.library.widget.kline.view.KLineChartView
import com.sl.ui.library.widget.kline.base.IChartViewDraw
import com.sl.ui.library.widget.kline.base.IValueFormatter
import com.sl.ui.library.widget.kline.bean.vice.IWR
import com.sl.ui.library.widget.kline.formatter.ValueFormatter
import com.sl.ui.library.widget.kline.view.BaseKLineChartView


/**
 * @Author: Bertking
 * @Date：2019/3/11-11:25 AM
 * @Description:
 */
class WRView(view: KLineChartView) : IChartViewDraw<IWR> {
    val TAG = WRView::class.java.simpleName

    private val paint4R = Paint(Paint.ANTI_ALIAS_FLAG)


    override fun drawTranslated(lastPoint: IWR?, curPoint: IWR, lastX: Float, curX: Float, canvas: Canvas, view: BaseKLineChartView, position: Int) {

        if (lastPoint!!.R != -10f) {
            view.drawChildLine(canvas, paint4R, lastX, lastPoint.R, curX, curPoint.R)
        }
    }

    override fun drawText(canvas: Canvas, view: BaseKLineChartView, position: Int, x: Float, y: Float) {
        val point = view.getItem(position) as IWR
        if (point.R != -10f) {
            var text = "WR(14):"
            canvas.drawText(text, x, y, view.textPaint)
            var textLen = x
            textLen += view.textPaint.measureText(text)
            text = view.formatValue(point.R) + " "
            canvas.drawText(text, textLen, y, paint4R)
        }
    }

    override fun getMaxValue(point: IWR): Float {
        return point.R
    }

    override fun getMinValue(point: IWR): Float {
        return point.R
    }

    override fun getValueFormatter(): IValueFormatter {
        return ValueFormatter()
    }

    override fun setTextSize(textSize: Float) {
        paint4R.textSize = textSize
    }

    override fun setLineWidth(width: Float) {
        paint4R.strokeWidth = width
    }

    /**
     * 设置%R颜色
     */
    fun setRColor(color: Int) {
        paint4R.color = color
    }

}