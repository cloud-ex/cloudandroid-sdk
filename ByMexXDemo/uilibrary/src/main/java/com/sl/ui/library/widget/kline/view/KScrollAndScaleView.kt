package com.sl.ui.library.widget.kline.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.OverScroller
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat

/**
 * @Author: Bertking
 * @Date：2019/2/25-8:49 PM
 * @Description:
 *
 * 当前缩放倍数已参考货币设置
 */
abstract class KScrollAndScaleView @JvmOverloads constructor(context: Context,
                                                             attrs: AttributeSet? = null,
                                                             defStyleAttr: Int = 0) :
        RelativeLayout(context, attrs, defStyleAttr),
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {

    var gestureDetectorCompat: GestureDetectorCompat
    var scaleGestureDetector: ScaleGestureDetector
    private var scroller: OverScroller


    var multipleTouchEnable = true
    var isTouched = false
    var isLongPress = false

    /**
     * 设置水平滚动距离
     */
    var scroll4X = 0
        set(value) {
            field = value
            scrollTo(scroll4X, 0)
        }

    /**
     * 当前缩放倍数
     */
    var scale4X = 1.5f

    /**
     * 最大缩放倍数
     */
    var maxScale4X = 3f

    /**
     * 最小缩放倍数
     */
    var minScale4X = 0.5f


    /**
     * 设置是否可缩放
     */
    var scaleEnable = true
        set(value) {
            scaleEnable = value
        }

    /**
     * 设置是否可滚动
     */
    var scrollEnable = true
        set(value) {
            scrollEnable = value
        }




    init {
        this.setWillNotDraw(false)
        gestureDetectorCompat = GestureDetectorCompat(getContext(), this)
        scaleGestureDetector = ScaleGestureDetector(getContext(), this)
        scroller = OverScroller(getContext())
    }


    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        if (!this.isLongPress && !this.multipleTouchEnable) {
            scrollBy(Math.round(distanceX), 0)
            return true
        }
        return false
    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        if (!this.isTouched && scrollEnable) {
            scroller.fling(scroll4X, 0, Math.round(velocityX / scale4X), 0,
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    0, scroll4X)
        }
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (this.isTouched) {
                scroller.forceFinished(true)
            } else {
                scrollTo(scroller.currX, scroller.currY)
            }
        }
    }


    override fun scrollBy(x: Int, y: Int) {
        scrollTo(scroll4X - Math.round(x / scale4X), 0)
    }

    override fun scrollTo(x: Int, y: Int) {
        if (!scrollEnable) {
            scroller.forceFinished(true)
            return
        }

        val oldX = scroll4X
        scroll4X = x
        if (scroll4X < getMinScrollX()) {
            scroll4X = getMinScrollX()
            onRightSide()
            scroller.forceFinished(true)
        } else if (scroll4X > getMaxScrollX()) {
            scroll4X = getMaxScrollX()
            onLeftSide()
            scroller.forceFinished(true)
        }
        onScrollChanged(scroll4X, 0, oldX, 0)
        invalidate()
    }

    /* X轴上滚动的最大值 */
    abstract fun getMaxScrollX(): Int

    /* X轴上滚动的最小值 */
    abstract fun getMinScrollX(): Int

    /* 滚到最左边 */
    abstract fun onLeftSide()

    /* 滚到最右边 */
    abstract fun onRightSide()

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {

    }


    override fun getScaleX(): Float {
        return scale4X
    }




    fun onScaleChange(scale: Float,oldScale: Float){
        invalidate()
    }



    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (scaleEnable) {
            return false
        }
        val oldScale = scale4X
        scale4X *= detector.scaleFactor
        when {
            scale4X < minScale4X -> scale4X = minScale4X
            scale4X > maxScale4X -> scale4X = maxScale4X
            else -> onScaleChange(scale4X, oldScale)
        }
        return true
    }


    var startX = 0f

    override fun onLongPress(e: MotionEvent) {
        this.isLongPress = true
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        /**
         * 多指触摸屏幕，取消长按事件
         */
        if (event.pointerCount > 1) {
            isLongPress = false
        }
        /**
         * 处理多点触摸问题
         */
        when (event.action and MotionEvent.ACTION_MASK) {
            /**
             * 单点触摸的down事件
             */
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                startX = event.x
            }

            MotionEvent.ACTION_MOVE ->
                //长按之后移动
                if (isLongPress) {
                    onLongPress(event)
                }

            /**
             * 多点触摸的up事件
             */
            MotionEvent.ACTION_POINTER_UP -> invalidate()
            /**
             * 单点触摸的up事件
             */
            MotionEvent.ACTION_UP -> {
                if (startX == event.x) {
                    if (isLongPress) {
                        isLongPress = false
                    }
                }
                isTouched = false
                invalidate()
            }

            MotionEvent.ACTION_CANCEL -> {
                isLongPress = false
                isTouched = false
                invalidate()
            }
        }
        multipleTouchEnable = event.pointerCount > 1
        this.gestureDetectorCompat.onTouchEvent(event)
        this.scaleGestureDetector.onTouchEvent(event)
        return true
    }

    protected fun checkAndFixScrollX() {
        if (scroll4X < getMinScrollX()) {
            scroll4X = getMinScrollX()
            scroller.forceFinished(true)
        } else if (scroll4X > getMaxScrollX()) {
            scroll4X = getMaxScrollX()
            scroller.forceFinished(true)
        }
    }


    protected abstract fun onScaleChanged(scale: Float, oldScale: Float)
}

