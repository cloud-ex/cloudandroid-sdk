package com.sl.ui.library.widget.kline.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.DimenRes
import com.sl.ui.library.R
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.widget.kline.view.vice.KDJView
import com.sl.ui.library.widget.kline.view.vice.MACDView
import com.sl.ui.library.widget.kline.view.vice.RSIView
import com.sl.ui.library.widget.kline.view.vice.WRView
import org.jetbrains.anko.layoutInflater
import kotlin.math.abs

/**
 * @Author: Bertking
 * @Date：2019/3/13-10:31 AM
 * @Description:
 */
class KLineChartView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BaseKLineChartView(context, attrs, defStyleAttr), IFallRiseColor {

    val TAG = KLineChartView::class.java.simpleName


    var mProgressBar: ProgressBar? = null
    private var isRefreshing = false
    private var isLoadMoreEnd = false
    private var mLastScrollEnable: Boolean = false
    private var mLastScaleEnable: Boolean = false

    private var mRefreshListener: KChartRefreshListener? = null

    private var mMACDDraw: MACDView? = null
    private var mRSIDraw: RSIView? = null
    private var mMainDraw: MainKLineView? = null
    private var mKDJDraw: KDJView? = null
    private var mWRDraw: WRView? = null
    private var mVolumeDraw: VolumeView? = null
    private var startXX = 0
    private var startYY = 0
    private var pricePricision = 2


    init {
        initView()
        initAttrs(attrs)
    }

    private fun initView() {
        /**
         * 进度条
         */
        val view = context.layoutInflater.inflate(R.layout.layout_kline_loading, this,true)
        mProgressBar = view.findViewById(R.id.pb_kline_load)
        mProgressBar!!.visibility = View.GONE
        mMACDDraw = MACDView(this)
        mWRDraw = WRView(this)
        mKDJDraw = KDJView(this)
        mRSIDraw = RSIView(this)

        /**
         * 交易量图
         */
        mVolumeDraw = VolumeView(this)

        /**
         * 主K图
         */
        mMainDraw = MainKLineView(this)

        addChildDraw(mMACDDraw)
        addChildDraw(mKDJDraw)
        addChildDraw(mRSIDraw)
        addChildDraw(mWRDraw)

        volDraw = mVolumeDraw
        mainDraw = mMainDraw
    }

    private fun initAttrs(attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.KLineChartView)
        if (array != null) {
            try {

                setLogoResouce(array.getResourceId(R.styleable.KLineChartView_kc_chart_logo, 0))

                /**
                 * 设置蜡烛线和交易量图柱的颜色
                 */
                setFallRiseColor(ColorUtils.getMainColorType(context), ColorUtils.getMainColorType(context,false))

                /**
                 * 蜡烛线相关设置
                 */
                setCandleWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)))
                setCandleLineWidth(array.getDimension(R.styleable.KLineChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)))
                setCandleSolid(array.getBoolean(R.styleable.KLineChartView_kc_candle_solid, true))


                /**
                 * MA & 蜡烛线
                 */
                setMa5Color(array.getColor(R.styleable.KLineChartView_kc_dif_color, context.resources.getColor(R.color.chart_ma5)))
                setMa10Color(array.getColor(R.styleable.KLineChartView_kc_dea_color, context.resources.getColor(R.color.chart_ma10)))
                setMa30Color(array.getColor(R.styleable.KLineChartView_kc_macd_color, context.resources.getColor(R.color.chart_ma30)))

                /**
                 * KLine图上的最大&最小值
                 */
                setMTextSize(array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size)))
                setMTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, context.resources.getColor(R.color.chart_max_min)))


                /**
                 * KLineView，VolumeView右边的边界值
                 */
                setBoundaryValueColor(array.getColor(R.styleable.KLineChartView_kc_boundary_value, context.resources.getColor(R.color.normal_text_color)))


                /**
                 * Point决定了蜡烛线之间的间隙
                 */
                setPointWidth(array.getDimension(R.styleable.KLineChartView_kc_point_width, getDimension(R.dimen.chart_point_width)))

                setSelectPointColor(array.getColor(R.styleable.KLineChartView_kc_background_color, context.resources.getColor(R.color.bg_card_color)))

                /**
                 * KLine，Volume的边界上的值
                 */
                textSize = array.getDimension(R.styleable.KLineChartView_kc_text_size, getDimension(R.dimen.chart_text_size))

                setTextColor(array.getColor(R.styleable.KLineChartView_kc_text_color, context.resources.getColor(R.color.chart_max_min)))

                /**
                 * 主图(KLine,volume,副图)的背景色
                 */
                setBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_background_color, context.resources.getColor(R.color.bg_card_color)))
                /**
                 * 主图指标线的宽度
                 */
                lineWidth = array.getDimension(R.styleable.KLineChartView_kc_line_width, getDimension(R.dimen.chart_line_width))

                /**
                 * 选中X轴的颜色
                 */
                setSelectedXLineColor(context.resources.getColor(R.color.chart_selected_x))
                setSelectedXLineWidth(getDimension(R.dimen.chart_line_width))
                /**
                 * 选中Y轴的颜色
                 */
                setSelectedYLineColor(context.resources.getColor(R.color.chart_selected_y))
                setSelectedYLineWidth(getDimension(R.dimen.chart_selected_y_width))

                /**
                 * 网格线参数
                 */
                setGridLineWidth(array.getDimension(R.styleable.KLineChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)))
                setGridLineColor(array.getColor(R.styleable.KLineChartView_kc_grid_line_color, context.resources.getColor(R.color.line_color)))


                /**
                 * MACD
                 */
                setMACDWidth(array.getDimension(R.styleable.KLineChartView_kc_macd_width, getDimension(R.dimen.chart_candle_width)))
                setDIFColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, context.resources.getColor(R.color.chart_ma5)))
                setDEAColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, context.resources.getColor(R.color.chart_ma10)))
                setMACDColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, context.resources.getColor(R.color.chart_ma30)))

                /**
                 * KDJ
                 */
                setKColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, context.resources.getColor(R.color.chart_ma5)))
                setDColor(array.getColor(R.styleable.KLineChartView_kc_dea_color, context.resources.getColor(R.color.chart_ma10)))
                setJColor(array.getColor(R.styleable.KLineChartView_kc_macd_color, context.resources.getColor(R.color.chart_ma30)))
                /**
                 * WR
                 */
                setRColor(array.getColor(R.styleable.KLineChartView_kc_dif_color, context.resources.getColor(R.color.chart_ma5)))
                /**
                 * RSI
                 */
                setRSI1Color(array.getColor(R.styleable.KLineChartView_kc_dif_color, context.resources.getColor(R.color.chart_ma5)))
                setRSI2Color(array.getColor(R.styleable.KLineChartView_kc_dea_color, context.resources.getColor(R.color.chart_ma10)))
                setRSI3Color(array.getColor(R.styleable.KLineChartView_kc_macd_color, context.resources.getColor(R.color.chart_ma30)))


                /**
                 * MarkView的背景色
                 */
                setSelectorBackgroundColor(array.getColor(R.styleable.KLineChartView_kc_selector_background_color, context.resources.getColor(R.color.marker_bg)))
                setSelectorTextSize(array.getDimension(R.styleable.KLineChartView_kc_selector_text_size, getDimension(R.dimen.chart_selector_text_size)))
                setMarkerTitleColor(array.getColor(R.styleable.KLineChartView_kc_text_color, context.resources.getColor(R.color.normal_text_color)))
                setMarkerValueColor(array.getColor(R.styleable.KLineChartView_kc_marker_value_color, context.resources.getColor(R.color.chart_max_min)))
                /**
                 * 分时线
                 */
                setTimeLineFillTopColor(array.getColor(R.styleable.KLineChartView_kc_timeLineFillTopColor, context.resources.getColor(R.color.main_yellow)))
                setTimeLineFillBottomColor(array.getColor(R.styleable.KLineChartView_kc_timeLineFillBottomColor, context.resources.getColor(R.color.main_yellow_8)))


            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                array.recycle()
            }
        }
    }

    private fun getDimension(@DimenRes resId: Int): Float {
        return resources.getDimension(resId)
    }


    override fun onLeftSide() {
      //  showLoading()
    }

    override fun onRightSide() {}

    fun showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar!!.visibility = View.VISIBLE
            }
            if (mRefreshListener != null) {
                mRefreshListener!!.onLoadMoreBegin(this)
            }
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.setScrollEnable(mLastScaleEnable)
            super.setScaleEnable(mLastScaleEnable)
        }
    }

    fun justShowLoading() {
        if (!isRefreshing) {
            isLongPress = false
            isRefreshing = true
            if (mProgressBar != null) {
                mProgressBar!!.visibility = View.VISIBLE
            }
            if (mRefreshListener != null) {
                mRefreshListener!!.onLoadMoreBegin(this)
            }
            mLastScaleEnable = isScaleEnable
            mLastScrollEnable = isScrollEnable
            super.setScrollEnable(false)
            super.setScaleEnable(false)
        }
    }

    private fun hideLoading() {
        if (mProgressBar != null) {
            mProgressBar!!.visibility = View.GONE
        }
        super.setScrollEnable(mLastScrollEnable)
        super.setScaleEnable(mLastScaleEnable)
    }

    /**
     * 隐藏选择器内容
     */
    fun hideSelectData() {
        isLongPress = false
        invalidate()
    }

    /**
     * 刷新完成
     */
    fun refreshComplete() {
        isRefreshing = false
        hideLoading()
    }

    /**
     * 刷新完成，没有数据
     */
    fun refreshEnd() {
        isLoadMoreEnd = true
        isRefreshing = false
        hideLoading()
    }

    /**
     * 重置加载更多
     */
    fun resetLoadMoreEnd() {
        isLoadMoreEnd = false
    }

    fun setLoadMoreEnd() {
        isLoadMoreEnd = true
    }

    interface KChartRefreshListener {
        /**
         * 加载更多
         *
         * @param chart
         */
        fun onLoadMoreBegin(chart: KLineChartView)
    }

    override fun setScaleEnable(scaleEnable: Boolean) {
        if (isRefreshing) {
            throw IllegalStateException("请勿在刷新状态设置属性")
        }
        super.setScaleEnable(scaleEnable)

    }

    override fun setScrollEnable(scrollEnable: Boolean) {
        if (isRefreshing) {
            throw IllegalStateException("请勿在刷新状态设置属性")
        }
        super.setScrollEnable(scrollEnable)
    }

    override fun setFallRiseColor(riseColor: Int, fallColor: Int) {
        // TODO 这里必须相反,蜡烛线的逻辑反了
        mMainDraw?.setFallRiseColor(fallColor, riseColor)
        mVolumeDraw?.setFallRiseColor(riseColor, fallColor)
        mMACDDraw?.setFallRiseColor(riseColor, fallColor)
    }


    /**
     * 设置DIF颜色
     */
    fun setDIFColor(color: Int) {
        mMACDDraw!!.setDIFColor(color)
    }

    /**
     * 设置DEA颜色
     */
    fun setDEAColor(color: Int) {
        mMACDDraw!!.setDEAColor(color)
    }

    /**
     * 设置MACD颜色
     */
    fun setMACDColor(color: Int) {
        mMACDDraw!!.setMACDColor(color)
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    fun setMACDWidth(MACDWidth: Float) {
        mMACDDraw?.setMACDWidth(MACDWidth)
    }

    /**
     * add logo in Kline View
     *
     * @param bitmap logo bitmap
     * @return [KLineChartView]
     */
    fun setLogoBigmap(bitmap: Bitmap?) {
        if (null != bitmap) {
            logoBitmap = bitmap
            initRect()
        }
    }

    /**
     * add logo in Kline View
     *
     * @param bitmapRes logo resource
     * @return [KLineChartView]
     */
    fun setLogoResouce(bitmapRes: Int) {
        if (bitmapRes == 0) {
            logoBitmap = null
        }
        setLogoBigmap(
            BitmapFactory.decodeResource(
                context.resources, bitmapRes
            )
        )
    }

    /**
     * 设置K颜色
     */
    fun setKColor(color: Int) {
        mKDJDraw?.setKColor(color)
    }

    /**
     * 设置D颜色
     */
    fun setDColor(color: Int) {
        mKDJDraw?.setDColor(color)
    }

    /**
     * 设置J颜色
     */
    fun setJColor(color: Int) {
        mKDJDraw?.setJColor(color)
    }

    /**
     * 设置R颜色
     */
    fun setRColor(color: Int) {
        mWRDraw?.setRColor(color)
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    fun setMa5Color(color: Int) {
        mMainDraw?.setMa5Color(color)
        mVolumeDraw?.setMa5Color(color)
    }


    /**
     * 设置ma10颜色
     *
     * @param color
     */
    fun setMa10Color(color: Int) {
        mMainDraw?.setMa10Color(color)
        mVolumeDraw?.setMa10Color(color)
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    fun setMa30Color(color: Int) {
        mMainDraw?.setMa30Color(color)
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    fun setSelectorTextSize(textSize: Float) {
        mMainDraw?.setMarkerTextSize(textSize)
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    fun setSelectorBackgroundColor(color: Int) {
        mMainDraw?.setMarkerBackgroundColor(color)
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    fun setCandleWidth(candleWidth: Float) {
        mMainDraw?.setCandleWidth(candleWidth)
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    fun setCandleLineWidth(candleLineWidth: Float) {
        mMainDraw!!.setCandleLineWidth(candleLineWidth)
    }

    /**
     * 蜡烛是否空心
     */
    fun setCandleSolid(candleSolid: Boolean) {
        mMainDraw!!.isCandleSolid = candleSolid
    }

    fun setRSI1Color(color: Int) {
        mRSIDraw!!.setRSI1Color(color)
    }

    fun setRSI2Color(color: Int) {
        mRSIDraw!!.setRSI2Color(color)
    }

    fun setRSI3Color(color: Int) {
        mRSIDraw!!.setRSI3Color(color)
    }

    override fun setTextSize(textSize: Float) {
        super.setTextSize(textSize)
        mMainDraw?.setTextSize(textSize)
        mRSIDraw?.setTextSize(textSize)
        mMACDDraw?.setTextSize(textSize)
        mKDJDraw?.setTextSize(textSize)
        mWRDraw?.setTextSize(textSize)
        mVolumeDraw?.setTextSize(textSize)
    }

    override fun setLineWidth(lineWidth: Float) {
        super.setLineWidth(lineWidth)
        mMainDraw!!.setLineWidth(lineWidth)
        mRSIDraw!!.setLineWidth(lineWidth)
        mMACDDraw!!.setLineWidth(lineWidth)
        mKDJDraw!!.setLineWidth(lineWidth)
        mWRDraw!!.setLineWidth(lineWidth)
        mVolumeDraw!!.setLineWidth(lineWidth)
    }


    fun setMarkerTitleColor(color: Int) {
        mMainDraw?.setMarkerTitleColor(color)
    }


    fun setMarkerValueColor(color: Int) {
        mMainDraw?.setMarkerValueColor(color)
    }

    /**
     * 设置刷新监听
     */
    fun setRefreshListener(refreshListener: KChartRefreshListener) {
        mRefreshListener = refreshListener
    }

    fun setMainDrawLine(isLine: Boolean) {
        mMainDraw!!.isLine = isLine
        mVolumeDraw!!.isLine = isLine
        invalidate()
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                startXX = ev.x.toInt()
                startYY = ev.y.toInt()
                Log.d("onInterceptTouchEvent", "====DOWN====x:${ev.x.toInt()},y:${ev.y.toInt()}========")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.d("onInterceptTouchEvent", "====MOVE====x:${ev.x.toInt()},y:${ev.y.toInt()}========")
                val dX = (ev.x - startXX).toInt()
                val dY = (ev.y - startYY).toInt()
            }
            MotionEvent.ACTION_UP -> {
                Log.d("onInterceptTouchEvent", "====UP====x:${ev.x.toInt()},y:${ev.y.toInt()}========")

//                return Math.abs(dX) > Math.abs(dY)
            }
        }
        return super.onInterceptTouchEvent(ev)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action?.and(MotionEvent.ACTION_MASK)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                startXX = event.x.toInt()
                startYY = event.y.toInt()

                Log.d("onTouchEvent", "====DOWN====x:${event.x.toInt()},y:${event.y.toInt()}========")

            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x.toInt() - startXX)
                val dy = (event.y.toInt() - startYY)
                // 按压手指超过1个
                if (event.pointerCount > 1) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }else{
                    parent.requestDisallowInterceptTouchEvent((abs(dx) - abs(dy))>100)
                }
            }

            MotionEvent.ACTION_UP -> {
                parent.requestDisallowInterceptTouchEvent(false)
                Log.d("onTouchEvent", "====UP====x:${event.x.toInt()},y:${event.y.toInt()}========")

            }
        }
        return super.onTouchEvent(event)
    }


    override fun onLongPress(e: MotionEvent) {
        if (!isRefreshing) {
            super.onLongPress(e)
        }
    }


    override fun onDetachedFromWindow() {
        logoBitmap = null
        super.onDetachedFromWindow()
    }

}
