package com.sl.contract.library.widget.depth

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.sl.contract.library.R
import com.sl.ui.library.utils.BigDecimalUtils
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.DisplayUtils
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp

/**
 * @Author: Bertking
 * @Date：2019-07-25-10:27
 * @Description:
 */
class DepthMarkView(context: Context, layoutRes: Int) : MarkerView(context, layoutRes) {

    val TAG = DepthMarkView::class.java.simpleName

    var screenWidth = 0f
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var volume = ""
    var price = ""
    var myCanvas: Canvas? = null


    init {
        circlePaint.style = Paint.Style.FILL
        circlePaint.strokeWidth = 4f

        markerBgPaint.style = Paint.Style.FILL
        markerBgPaint.color = ContextCompat.getColor(context, R.color.marker_bg)


        markerBorderPaint.style = Paint.Style.STROKE
        markerBorderPaint.strokeWidth = dip(0.5f).toFloat()
        markerBorderPaint.color =   ContextCompat.getColor(context, R.color.marker_border)


        screenWidth = DisplayUtils.getWidth(context).toFloat()

        textPaint.color = ContextCompat.getColor(context, R.color.text_color)
        textPaint.textSize = sp(10f).toFloat()
        textPaint.textAlign = Paint.Align.LEFT
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        volume = BigDecimalUtils.showDepthVolume(e?.y.toString())
        price = e?.data.toString()

        Log.d(TAG, "==volume:${e?.y},price:${e?.data}==")

        super.refreshContent(e, highlight)
    }


    override fun getOffset(): MPPointF {
        Log.d(TAG, "width:$width,height:$height")
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }

    override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
        Log.d(TAG, "=======x:$posX,y:$posY======")
        super.draw(canvas, posX, posY)

        drawCircle(posX, canvas, posY)
        /**
         * 文字高度
         */
        val metrics = textPaint.fontMetrics
        val textHeight = metrics.descent - metrics.ascent
        val textWidth4Price = textPaint.measureText(price)

        val textPadding = dip(12f).toFloat()
        val height4Chart = dip(198).toFloat()

        val xRectF = RectF(posX - textWidth4Price / 2.0f - textPadding, height4Chart, posX + textWidth4Price / 2.0f + textPadding, height4Chart - dip(20).toFloat())
        canvas?.drawRect(xRectF, markerBgPaint)
        canvas?.drawText(price, posX - textWidth4Price / 2.0f, height4Chart - textHeight / 2.0f, textPaint)
        canvas?.drawRect(xRectF, markerBorderPaint)

        val textWidth4Volume = textPaint.measureText(volume)
        val yRectF = RectF(screenWidth, posY, screenWidth - textWidth4Volume - 2 * textPadding, posY - dip(20).toFloat())
        canvas?.drawRect(yRectF, markerBgPaint)
        canvas?.drawText(volume, screenWidth - textWidth4Volume - textPadding, posY - textHeight / 2.0f, textPaint)
        canvas?.drawRect(yRectF, markerBorderPaint)

        myCanvas = canvas

    }

    /**
     * 画圆圈
     */
    private fun drawCircle(posX: Float, canvas: Canvas?, posY: Float) {
        val center = DisplayUtils.getWidth(context) / 2.0
        val pair = if (posX <= center) {
            Pair<Int, Int>(ColorUtils.getMainColorType(context,true), ColorUtils.getMainColorType(context,true))
        } else {
            Pair<Int, Int>(ColorUtils.getMainColorType(context,false), ColorUtils.getMainColorType(context,false))
        }
        circlePaint.color = pair.first
        canvas?.drawCircle(posX, posY, dip(8f).toFloat(), circlePaint)
        circlePaint.color = pair.second
        canvas?.drawCircle(posX, posY, dip(4f).toFloat(), circlePaint)
    }

}