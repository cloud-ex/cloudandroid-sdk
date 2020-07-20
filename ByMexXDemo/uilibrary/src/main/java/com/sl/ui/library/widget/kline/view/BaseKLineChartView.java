package com.sl.ui.library.widget.kline.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import com.sl.ui.library.R;
import com.sl.ui.library.utils.BigDecimalUtils;
import com.sl.ui.library.utils.DisplayUtils;
import com.sl.ui.library.widget.kline.base.IChartViewDraw;
import com.sl.ui.library.widget.kline.base.IDateFormatter;
import com.sl.ui.library.widget.kline.base.IValueFormatter;
import com.sl.ui.library.widget.kline.bean.IKLine;
import com.sl.ui.library.widget.kline.data.IAdapter;
import com.sl.ui.library.widget.kline.formatter.DateFormatter;
import com.sl.ui.library.widget.kline.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * k线图
 *
 * @author Bertking
 * @Date：2019/3/12-3:35 PM
 * @Description: 所有文字的大小都设置相等
 */
public abstract class BaseKLineChartView extends ScrollAndScaleView {
    public static final String TAG = BaseKLineChartView.class.getSimpleName();


    private int childDrawPosition = -1;

    private float translateX = Float.MIN_VALUE;

    private int width = 0;


    private int topPadding;
    private int childPadding;
    private int bottomPadding;

    protected Bitmap logoBitmap;

    /**
     * 缩放比例
     */
    private float mainScaleY = 1;

    private float volScaleY = 1;

    private float childScaleY = 1;

    /**
     * 所有数据所占的宽度
     */
    private float dataLen = 0;

    /**
     * Kline 右边的最大值和最小值(刻度)
     */
    private float mainMaxValue = Float.MAX_VALUE;

    private float mainMinValue = Float.MIN_VALUE;


    /**
     * Kline线的最大值&最小值
     */
    private float mainHighMaxValue = 0;

    private float mainLowMinValue = 0;


    private int mainMaxIndex = 0;

    private int mainMinIndex = 0;

    /**
     * 交易量图的 max & min
     */
    private Float volMaxValue = Float.MAX_VALUE;

    private Float volMinValue = Float.MIN_VALUE;

    /**
     * 子图的 max & min
     */
    private Float childMaxValue = Float.MAX_VALUE;

    private Float childMinValue = Float.MIN_VALUE;

    /********-----------------------------------------*********/

    private int startIndex = 0;

    private int stopIndex = 0;

    private float pointWidth = 0;


    /**
     * 主图背景后面的网格设置
     */
    private int gridRows = 5;
    private int gridColumns = 5;
    /**
     * 网格画笔
     */
    private Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    /**
     * 主图背景画笔
     */
    private Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * K线右边的边界值
     */
    private Paint boundaryValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 时间画笔
     */
    private Paint timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * K线上的最值
     */
    private Paint maxMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 长按选中的值
     */
    private Paint selectedTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * X轴选中的颜色
     */
    private Paint selectedXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * Y轴选中的颜色
     */
    private Paint selectedYLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    private Paint selectPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint selectorFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * logo paint
     */
    protected Paint logoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int selectedIndex;

    private IChartViewDraw mMainDraw;
    private MainKLineView mainDraw;
    private IChartViewDraw mVolDraw;

    private IAdapter mAdapter;

    private Boolean isWR = false;
    /**
     * 即：副图指标是否显示
     */
    private Boolean isShowChild = false;


    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            itemCount = getAdapter().getCount();
            Log.d(TAG, "========mItemCount1:====" + itemCount);
            notifyChanged();
        }

        @Override
        public void onInvalidated() {
            itemCount = getAdapter().getCount();
            notifyChanged();
        }
    };

    /**
     * 一共有多少条数据
     */
    private int itemCount;

    private IChartViewDraw childDraw;
    private List<IChartViewDraw> childDraws = new ArrayList<>();

    private IValueFormatter valueFormatter;
    private IDateFormatter dateTimeFormatter;

    private ValueAnimator animator;

    private long animationDuration = 100;

    private float overScrollRange = 0;

    private OnSelectedChangedListener mOnSelectedChangedListener = null;


    /**
     * 分时线填充渐变的上部颜色
     */
    protected int timeLineFillTopColor;

    /**
     * 分时线填充渐变的下部颜色
     */
    protected int timeLineFillBottomColor;


    /**
     * 要画3个子图
     * 1. 主KLine图
     * 2. 交易量图
     * 3. 副图
     */
    private Rect mainRect;

    private Rect volRect;

    private Rect childRect;

    int displayHeight = 0;
    int displayWidth = 0;

    private float mLineWidth;

    public BaseKLineChartView(Context context) {
        super(context);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);

        topPadding = (int) getResources().getDimension(R.dimen.chart_top_padding);
        childPadding = (int) getResources().getDimension(R.dimen.child_top_padding);
        bottomPadding = (int) getResources().getDimension(R.dimen.chart_bottom_padding);

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        setLayerType(LAYER_TYPE_SOFTWARE, gridPaint);
        gridPaint.setPathEffect(new DashPathEffect(new float[] {8, 8}, 0));
        /**
         * 选中值的边框设置
         */
        selectorFramePaint.setStrokeWidth(DisplayUtils.INSTANCE.dip2px(getContext(),0.6f));
        selectorFramePaint.setStyle(Paint.Style.STROKE);
        selectorFramePaint.setColor(ContextCompat.getColor(getContext(), R.color.chart_selected_indicator));

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "==========onSizeChanged:width========" + w + ",oldW = " + oldw);
        this.width = w;
//        displayWidth = (int) (width * 0.8f);
        displayWidth = width;
        displayHeight = h - topPadding - bottomPadding;
        initRect();
        setTranslateXFromScrollX(mScrollX);
    }


    /**
     * 火币规则
     * 设置是否有子图
     * 1 有子图 ----- 0.6 | 0.2 | 0.2
     * 2 无子图 ----- 0.8 | 0.2
     */
    protected void initRect() {
        setOverScrollRange(width * 0.2f);
        Log.d("========", "=====initRect=======" + isShowChild);
        if (isShowChild) {
            int mMainHeight = (int) (displayHeight * 0.6f);
            int mVolHeight = (int) (displayHeight * 0.2f);
            int mChildHeight = (int) (displayHeight * 0.2f);
            mainRect = new Rect(0, topPadding, displayWidth, topPadding + mMainHeight);
            volRect = new Rect(0, mainRect.bottom + childPadding, displayWidth, mainRect.bottom + mVolHeight);
            childRect = new Rect(0, volRect.bottom + childPadding, displayWidth, volRect.bottom + mChildHeight);
        } else {
            Log.d("=====onSizeChanged===", "width:" + width + ",height:" + displayHeight);
            int mMainHeight = (int) (displayHeight * 0.8f);
            int mVolHeight = (int) (displayHeight * 0.2f);
            mainRect = new Rect(0, topPadding, displayWidth, topPadding + mMainHeight);
            volRect = new Rect(0, mainRect.bottom + childPadding, displayWidth, mainRect.bottom + mVolHeight);
        }

        if (null != logoBitmap) {
            logoTop = mainRect.bottom - logoBitmap.getHeight() - logoPaddingBottom;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(bgPaint.getColor());
        drawLogo(canvas);
        if (width == 0 || mainRect.height() == 0 || itemCount == 0) {
            Log.d(TAG, "发生未知错误。。。");
            return;
        }
        calculateValue();
        canvas.save();
        /**
         * 这里控制整个画布
         */
        canvas.scale(1, 1);
        /**
         * drawGrid()
         */
        drawGird(canvas);
        /**
         * draw K线
         */
        drawK(canvas);

        drawText(canvas);
        drawMaxAndMin(canvas);
        drawValue(canvas, isLongPress ? selectedIndex : stopIndex);
        canvas.restore();
    }


    /**
     * 绘制整体背景
     *
     * @param canvas canvas
     */
    protected void drawBackground(Canvas canvas) {
        float mid = width / 2, bottom;
        int mainBottom = mainRect.bottom;
        bgPaint.setShader(new LinearGradient(mid, 0, mid, mainBottom, Color.red(255),  Color.GREEN, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, mainBottom, bgPaint);


    }


    public float getMainY(float value) {
        return (mainMaxValue - value) * mainScaleY + mainRect.top;
    }

    public float getMainBottom() {
        return mainRect.bottom;
    }

    public float getVolY(float value) {
        Log.d("=====getVolY======", "max:" + volMaxValue + ",scale:" + volScaleY + ",top:" + volRect.top);
        return (volMaxValue - value) * volScaleY + volRect.top;
    }

    public float getChildY(float value) {
        if(childMaxValue.intValue() == 1){
            childMaxValue = 0.02f;
        }
        Log.d("=====getChildY======", "max:" + (childMaxValue - value) + ",scale:" + childScaleY + ",top:" + childRect.top);
        return (childMaxValue - value) * childScaleY + childRect.top;
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY(float y) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return y + fontMetrics.descent - fontMetrics.ascent;
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY1(float y) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    /**
     * 画表格
     * <p>
     * Done
     *
     * @param canvas
     */
    private void drawGird(Canvas canvas) {
        //-----------------------上方k线图------------------------
        //横向的grid
        float rowSpace = mainRect.height() / gridRows;
        for (int i = 0; i <= gridRows; i++) {
            /**
             * 画横线
             */
            canvas.drawLine(0, rowSpace * i + mainRect.top, width, rowSpace * i + mainRect.top, gridPaint);
        }


        /**
         * -----------------------下方子图------------------------
         * 如果有childView: 画出交易量底部的线 & 子图底部的线
         */
        if (childDraw != null) {
            canvas.drawLine(0, volRect.bottom, width, volRect.bottom, gridPaint);
            canvas.drawLine(0, childRect.bottom, width, childRect.bottom, gridPaint);
        } else {
            canvas.drawLine(0, volRect.bottom, width, volRect.bottom, gridPaint);
        }

        //纵向的grid
        Log.d(TAG, "======columns=========" + gridColumns);
        float columnSpace = width / gridColumns;
        for (int i = 1; i < gridColumns; i++) {
            canvas.drawLine(columnSpace * i, 0, columnSpace * i, mainRect.bottom, gridPaint);
            canvas.drawLine(columnSpace * i, mainRect.bottom, columnSpace * i, volRect.bottom, gridPaint);
            if (childDraw != null) {
                /**
                 * 从Volu的底部 --- 副图的底部
                 */
                canvas.drawLine(columnSpace * i, volRect.bottom, columnSpace * i, childRect.bottom, gridPaint);
            }
        }
    }

    /**
     * 画k线图
     *
     * @param canvas
     */
    private void drawK(Canvas canvas) {
        //保存之前的平移，缩放
        canvas.save();
        Log.d(TAG, "tranX:" + translateX + ",scaleX =" + mScaleX +
                ", startIndex =" + startIndex + ",stopIndex=" + stopIndex);

        canvas.translate(translateX * mScaleX, 0);
        canvas.scale(mScaleX, 1);
        for (int i = startIndex; i <= stopIndex; i++) {
            /**
             * 根据下标 获取对应的Item
             */
            Object currentPoint = getItem(i);
            /**
             * 根据下标获取对应的X轴位置
             */
            float currentPointX = getX(i);

            /**
             * 最后一个Item
             */
            Object lastPoint = i == 0 ? currentPoint : getItem(i - 1);

            float lastX = i == 0 ? currentPointX : getX(i - 1);

            if (mMainDraw != null) {
                mMainDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }
            if (mVolDraw != null) {
                mVolDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }
            if (childDraw != null) {
                childDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }
        }

        /**
         * draw 选中蜡烛线的部分
         */
        if (isLongPress) {
            IKLine point = (IKLine) getItem(selectedIndex);
            float x = getX(selectedIndex);
            float y = getMainY(point.getClosePrice());
            // k线图竖线
            canvas.drawLine(x, mainRect.top, x, mainRect.bottom, selectedYLinePaint);
            // k线图横线
            canvas.drawLine(-translateX, y, -translateX + width / mScaleX, y, selectedXLinePaint);
            // 柱状图竖线
            canvas.drawLine(x, mainRect.bottom, x, volRect.bottom, selectedYLinePaint);

            /**
             * 长按选中X & Y的交点，画圈
             * TODO 可能需要配置颜色，大小
             */
            canvas.drawCircle(x, y, 10f, selectedTextPaint);

            canvas.drawCircle(x, y, 30f, selectedYLinePaint);

            if (childDraw != null) {
                // 子线图竖线
                canvas.drawLine(x, volRect.bottom, x, childRect.bottom, selectedYLinePaint);
            }
        }
        //还原 平移缩放
        canvas.restore();
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private int calculateWidth(String text) {
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.width() + 5;
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private Rect calculateMaxMin(String text) {
        Rect rect = new Rect();
        maxMinPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;

        /***--------------画k线图的值 右边的）-------------**/

        Log.d(TAG, "===========mainMaxValue:=" + mainMaxValue + ",mainMinValue = " + mainMinValue);

        if (mMainDraw != null) {
            canvas.drawText(formatValue(mainMaxValue), width - calculateWidth(formatValue(mainMaxValue)) - DisplayUtils.INSTANCE.dip2px(getContext(),15f), baseLine + mainRect.top, boundaryValuePaint);
            canvas.drawText(formatValue(mainMinValue), width - calculateWidth(formatValue(mainMinValue)) -DisplayUtils.INSTANCE.dip2px(getContext(),15f), mainRect.bottom - textHeight + baseLine, boundaryValuePaint);
            float rowValue = (mainMaxValue - mainMinValue) / gridRows;
            float rowSpace = mainRect.height() / gridRows;
            for (int i = 1; i < gridRows; i++) {
                String text = formatValue(rowValue * (gridRows - i) + mainMinValue);
                canvas.drawText(text, width - calculateWidth(text) -DisplayUtils.INSTANCE.dip2px(getContext(),15f), fixTextY(rowSpace * i + mainRect.top), boundaryValuePaint);
            }
        }
        /**--------------画中间子图的值-------------**/
        if (mVolDraw != null) {
            /**
             * 画最大值
             */
            canvas.drawText(mVolDraw.getValueFormatter().format(volMaxValue),
                    width - calculateWidth(formatValue(volMaxValue)) -DisplayUtils.INSTANCE.dip2px(getContext(),5f), mainRect.bottom + baseLine, boundaryValuePaint);
            /**
             * 画最小值
             */
//            canvas.drawText(mVolDraw.getValueFormatter().format(volMinValue),
//                    width - calculateWidth(formatValue(volMinValue))-DisplayUtils.INSTANCE.dip2px(15f), volRect.bottom, boundaryValuePaint);
        }

        /**--------------画下方子图的值-------------**/
        if (childDraw != null) {
            canvas.drawText(childDraw.getValueFormatter().format(childMaxValue),
                    width - calculateWidth(formatValue(childMaxValue)) -DisplayUtils.INSTANCE.dip2px(getContext(),15f), volRect.bottom + baseLine, boundaryValuePaint);
            /**
             * 画最小值
             */
            canvas.drawText(childDraw.getValueFormatter().format(childMinValue),
                    width - calculateWidth(formatValue(childMinValue)) -DisplayUtils.INSTANCE.dip2px(getContext(),15f), childRect.bottom, boundaryValuePaint);
        }

        /**--------------画时间---------------------**/
        float columnSpace = width / gridColumns;
        float y;
        if (isShowChild) {
            y = childRect.bottom + baseLine + 5;
        } else {
            y = volRect.bottom + baseLine + 5;
        }

        float startX = getX(startIndex) - pointWidth / 2;
        float stopX = getX(stopIndex) + pointWidth / 2;

        for (int i = 1; i < gridColumns; i++) {
            float translateX = xToTranslateX(columnSpace * i);
            if (translateX >= startX && translateX <= stopX) {
                int index = indexOfTranslateX(translateX);
                String text = mAdapter.getDate(index);
                if (i == 1) {
                    Log.d(TAG, "======the Time:=====" + text);
                }
                canvas.drawText(text, columnSpace * i - timePaint.measureText(text) / 2, y, timePaint);
            }
        }

        float translateX = xToTranslateX(0);
        if (translateX >= startX && translateX <= stopX) {
            Log.d(TAG, "======the Time:=====" + getAdapter().getDate(startIndex) + "start Index:" + startIndex);
            canvas.drawText(getAdapter().getDate(startIndex).split(" ")[1], 0, y, timePaint);
        }


        translateX = xToTranslateX(width);
        if (translateX >= startX && translateX <= stopX) {
            String text = getAdapter().getDate(stopIndex);
            canvas.drawText(text, width - timePaint.measureText(text), y, timePaint);
        }


        /**
         * 长按选中画
         */
        if (isLongPress) {
            // 画Y值
            IKLine point = (IKLine) getItem(selectedIndex);
            float w1 =DisplayUtils.INSTANCE.dip2px(getContext(),5f);
            float w2 =DisplayUtils.INSTANCE.dip2px(getContext(),3f);
            float r = textHeight / 2 + w2;
            y = getMainY(point.getClosePrice());
            float x;
            String text = formatValue(point.getClosePrice());
            float textWidth = selectedTextPaint.measureText(text);
            if (translateXtoX(getX(selectedIndex)) < getChartWidth() / 2) {
                x = 1;
                Path path = new Path();
                path.moveTo(x, y - r);
                path.lineTo(x, y + r);
                path.lineTo(textWidth + 2 * w1, y + r);
                path.lineTo(textWidth + 2 * w1 + w2, y);
                path.lineTo(textWidth + 2 * w1, y - r);
                path.close();

                canvas.drawPath(path, selectPointPaint);
                canvas.drawPath(path, selectorFramePaint);
                canvas.drawText(text, x + w1, fixTextY1(y), selectedTextPaint);
            } else {
                x = width - textWidth - 1 - 2 * w1 - w2;
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x + w2, y + r);
                path.lineTo(width - 2, y + r);
                path.lineTo(width - 2, y - r);
                path.lineTo(x + w2, y - r);
                path.close();
                canvas.drawPath(path, selectPointPaint);
                canvas.drawPath(path, selectorFramePaint);
                canvas.drawText(text, x + w1 + w2, fixTextY1(y), selectedTextPaint);
            }

            // 画X值
            String date = mAdapter.getDate(selectedIndex);
            textWidth = selectedTextPaint.measureText(date);
            r = textHeight / 2;
            x = translateXtoX(getX(selectedIndex));
            if (isShowChild) {
                y = childRect.bottom;
            } else {
                y = volRect.bottom;
            }

            if (x < textWidth + 2 * w1) {
                x = 1 + textWidth / 2 + w1;
            } else if (width - x < textWidth + 2 * w1) {
                x = width - 1 - textWidth / 2 - w1;
            }

            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, selectPointPaint);
            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, selectorFramePaint);
            canvas.drawText(date, x - textWidth / 2, y + baseLine + 5, selectedTextPaint);
        }
    }


    protected float logoTop;

    protected float logoPaddingLeft = 15;
    protected float logoPaddingBottom = 15;

    protected void drawLogo(Canvas canvas) {
        if (null != logoBitmap) {
            canvas.drawBitmap(logoBitmap, logoPaddingLeft, logoTop, logoPaint);
        }
    }

    /**
     * 绘制Kline线的最大值&最小值
     *
     * @param canvas
     */
    private void drawMaxAndMin(Canvas canvas) {
        if (!mainDraw.isLine()) {
            IKLine maxEntry = null, minEntry = null;
            boolean firstInit = true;

            //绘制最大值和最小值
            float x = translateXtoX(getX(mainMinIndex));

            float y = getMainY(mainLowMinValue);
            String LowString = "── " + BigDecimalUtils.showSNormal(String.valueOf(mainLowMinValue));
            //计算显示位置
            //计算文本宽度
            int lowStringWidth = calculateMaxMin(LowString).width();
            int lowStringHeight = calculateMaxMin(LowString).height();
            if (x < getWidth() / 2) {
                //画右边
                canvas.drawText(LowString, x, y + lowStringHeight / 2, maxMinPaint);
            } else {
                //画左边
                LowString = BigDecimalUtils.showSNormal(String.valueOf(mainLowMinValue)) + " ──";
                canvas.drawText(LowString, x - lowStringWidth, y + lowStringHeight / 2, maxMinPaint);
            }

            x = translateXtoX(getX(mainMaxIndex));

            y = getMainY(mainHighMaxValue);

            /**
             * 最大值
             */
            String highString = "── " + BigDecimalUtils.showSNormal(String.valueOf(mainHighMaxValue));

            int highStringWidth = calculateMaxMin(highString).width();
            int highStringHeight = calculateMaxMin(highString).height();

            if (x < getWidth() / 2) {
                //画右边
                canvas.drawText(highString, x, y + highStringHeight / 2, maxMinPaint);
            } else {
                //画左边
                highString = BigDecimalUtils.showSNormal(String.valueOf(mainHighMaxValue)) + " ──";
                canvas.drawText(highString, x - highStringWidth, y + highStringHeight / 2, maxMinPaint);

            }

        }
    }

    /**
     * 画值
     *
     * @param canvas
     * @param position 显示某个点的值
     */
    private void drawValue(Canvas canvas, int position) {
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        if (position >= 0 && position < itemCount) {
            if (mMainDraw != null) {
                float y = mainRect.top + baseLine - textHeight;
                mMainDraw.drawText(canvas, this, position, 0, y);
            }
            if (mVolDraw != null) {
                float y = mainRect.bottom + baseLine;
                mVolDraw.drawText(canvas, this, position, 0, y);
            }
            if (childDraw != null) {
                float y = volRect.bottom + baseLine;
                childDraw.drawText(canvas, this, position, 0, y);
            }
        }
    }


    /**
     * 格式化值
     */
    public String formatValue(float value) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().format(value);
    }

    /**
     * 重新计算并刷新线条
     */
    public void notifyChanged() {
        if (isShowChild && childDrawPosition == -1) {
            childDraw = childDraws.get(0);
            childDrawPosition = 0;
        }

        if (itemCount != 0) {
            dataLen = (itemCount - 1) * pointWidth;
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);
        } else {
            setScrollX(0);
        }


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });

    }

    /**
     * MA/BOLL切换及隐藏
     *
     * @param status MA/BOLL/NONE
     */
    public void changeMainDrawType(MainKlineViewStatus status) {
        if (mainDraw != null && mainDraw.getStatus() != status) {
            mainDraw.setStatus(status);
            invalidate();
        }
    }

    private void calculateSelectedX(float x) {
        selectedIndex = indexOfTranslateX(xToTranslateX(x));
        if (selectedIndex < startIndex) {
            selectedIndex = startIndex;
        }
        if (selectedIndex > stopIndex) {
            selectedIndex = stopIndex;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        int lastIndex = selectedIndex;
        calculateSelectedX(e.getX());
        if (lastIndex != selectedIndex) {
            onSelectedChanged(this, getItem(selectedIndex), selectedIndex);
        }
        invalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        setTranslateXFromScrollX(mScrollX);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        checkAndFixScrollX();
        setTranslateXFromScrollX(mScrollX);
        super.onScaleChanged(scale, oldScale);
    }

    /**
     * 计算当前的显示区域
     */
    private void calculateValue() {
        Log.d(TAG, "===calculateValue()====" + width);

        if (!isLongPress()) {
            selectedIndex = -1;
        }

        mainMaxValue = Float.MIN_VALUE;
        mainMinValue = Float.MAX_VALUE;


        volMaxValue = Float.MIN_VALUE;
        volMinValue = Float.MAX_VALUE;


        childMaxValue = Float.MIN_VALUE;
        childMinValue = Float.MAX_VALUE;


        startIndex = indexOfTranslateX(xToTranslateX(0));
        stopIndex = indexOfTranslateX(xToTranslateX(displayWidth));
        Log.d(TAG, "========stopIndex:==" + stopIndex);

//        stopIndex = indexOfTranslateX(xToTranslateX(displayWidth));

        mainMaxIndex = startIndex;
        mainMinIndex = startIndex;


        /**
         * 最大的最高价
         */
        mainHighMaxValue = Float.MIN_VALUE;
        /**
         * 最小的最低价
         */
        mainLowMinValue = Float.MAX_VALUE;

        for (int i = startIndex; i <= stopIndex; i++) {
            IKLine point = (IKLine) getItem(i);
            if (mMainDraw != null) {
                mainMaxValue = Math.max(mainMaxValue, mMainDraw.getMaxValue(point));

                mainMinValue = Math.min(mainMinValue, mMainDraw.getMinValue(point));
                /**
                 * 确定最大最高价，最小值最低价
                 */
                if (mainHighMaxValue != Math.max(mainHighMaxValue, point.getHighPrice())) {
                    mainHighMaxValue = point.getHighPrice();
                    mainMaxIndex = i;
                }
                if (mainLowMinValue != Math.min(mainLowMinValue, point.getLowPrice())) {
                    mainLowMinValue = point.getLowPrice();
                    mainMinIndex = i;
                }
            }

            /**
             * 确定交易量-》最值
             */
            if (mVolDraw != null) {
                volMaxValue = Math.max(volMaxValue, mVolDraw.getMaxValue(point));
                volMinValue = Math.min(volMinValue, mVolDraw.getMinValue(point));
            }

            /**
             * 确定子图-》最值
             */
            if (childDraw != null) {
                childMaxValue = Math.max(childMaxValue, childDraw.getMaxValue(point));
                childMinValue = Math.min(childMinValue, childDraw.getMinValue(point));
                Log.d(TAG, "====max:=" + childMaxValue + "min:" + childMinValue);
            }

        }


        if (mainMaxValue != mainMinValue) {
            float padding = (mainMaxValue - mainMinValue) * 0.05f;
            mainMaxValue += padding;
            mainMinValue -= padding;
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mainMaxValue += Math.abs(mainMaxValue * 0.05f);
            mainMinValue -= Math.abs(mainMinValue * 0.05f);
            if (mainMaxValue == 0) {
                mainMaxValue = 1;
            }
        }

        if (Math.abs(volMaxValue) < 0.01) {
            volMaxValue = 15.00f;
        }

        if (Math.abs(childMaxValue) < 0.000001 && Math.abs(childMinValue) < 0.000001) {
            childMaxValue = 1f;
        } else if (childMaxValue.equals(childMinValue)) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            childMaxValue += Math.abs(childMaxValue * 0.05f);
            childMinValue -= Math.abs(childMinValue * 0.05f);
            if (childMaxValue == 0) {
                childMaxValue = 1f;
            }
        }

        if (isWR) {
            childMaxValue = 0f;
            if (Math.abs(childMinValue) < 0.01) {
                childMinValue = -10.00f;
            }
        }

        mainScaleY = mainRect.height() * 1f / (mainMaxValue - mainMinValue);

        volScaleY = volRect.height() * 1f / (volMaxValue - volMinValue);

        if (childRect != null) {
            childScaleY = childRect.height() * 1f / (childMaxValue - childMinValue);
        }

        if (animator.isRunning()) {
            float value = (float) animator.getAnimatedValue();
            stopIndex = startIndex + Math.round(value * (stopIndex - startIndex));
        }


    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        if (!isFullScreen()) {
            return getMaxTranslateX();
        }
        return -dataLen + width / mScaleX - pointWidth / 2;
    }


    /**
     * 获取平移的最大值
     *
     * @return
     */
    private float getMaxTranslateX() {
        return pointWidth / 2;
    }

    @Override
    public int getMinScrollX() {
        return (int) -(overScrollRange / mScaleX);
    }

    @Override
    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }


    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, itemCount - 1);
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint);
    }


    /**
     * 在主区域画分时线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainMinuteLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {

        float y = displayHeight + topPadding + bottomPadding;
        LinearGradient linearGradient = new LinearGradient(startX, topPadding,
                stopX, y, timeLineFillTopColor, timeLineFillBottomColor , Shader.TileMode.CLAMP);
        paint.setShader(linearGradient);

        Path path5 = new Path();
        path5.moveTo(startX, displayHeight + topPadding + bottomPadding);
        path5.lineTo(startX, getMainY(startValue));
        path5.lineTo(stopX, getMainY(stopValue));
        path5.lineTo(stopX, displayHeight + topPadding + bottomPadding);
        path5.close();
        canvas.drawPath(path5, paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawChildLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getChildY(startValue), stopX, getChildY(stopValue), paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawVolLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getVolY(startValue), stopX, getVolY(stopValue), paint);
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    public Object getItem(int position) {
        if (mAdapter != null) {
            return mAdapter.getItem(position);
        } else {
            return null;
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
     */
    public float getX(int position) {
        return position * pointWidth;
    }

    /**
     * 获取适配器
     *
     * @return
     */
    public IAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 设置当前子图
     *
     * @param position
     */
    public void setChildDraw(int position) {
        if (childDrawPosition != position) {
            if (!isShowChild) {
                isShowChild = true;
                initRect();
            }
            childDraw = childDraws.get(position);
            childDrawPosition = position;
            isWR = position == 5;
            invalidate();
        }
    }


    /**
     * 隐藏子图
     */
    public void hideChildDraw() {
        childDrawPosition = -1;
        isShowChild = false;
        childDraw = null;
        initRect();
        invalidate();
    }

    /**
     * 给子区域添加画图方法
     *
     * @param childDraw IChartViewDraw
     */
    public void addChildDraw(IChartViewDraw childDraw) {
        childDraws.add(childDraw);
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        translateX = scrollX + getMinTranslateX();
    }

    /**
     * 获取ValueFormatter
     *
     * @return
     */
    public IValueFormatter getValueFormatter() {
        return valueFormatter;
    }

    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    public void setValueFormatter(IValueFormatter valueFormatter) {
        this.valueFormatter = valueFormatter;
    }

    /**
     * 获取DatetimeFormatter
     *
     * @return 时间格式化器
     */
    public IDateFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    /**
     * 设置dateTimeFormatter
     *
     * @param dateTimeFormatter 时间格式化器
     */
    public void setDateTimeFormatter(IDateFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    /**
     * 格式化时间
     *
     * @param date
     */
    public String formatDateTime(Date date) {
        if (getDateTimeFormatter() == null) {
            setDateTimeFormatter(new DateFormatter());
        }
        return getDateTimeFormatter().format(date);
    }

    /**
     * 获取主区域的 IChartViewDraw
     *
     * @return IChartViewDraw
     */
    public IChartViewDraw getMainDraw() {
        return mMainDraw;
    }

    /**
     * 设置主区域的 IChartViewDraw
     *
     * @param mainDraw IChartViewDraw
     */
    public void setMainDraw(IChartViewDraw mainDraw) {
        mMainDraw = mainDraw;
        this.mainDraw = (MainKLineView) mMainDraw;
    }

    public IChartViewDraw getVolDraw() {
        return mVolDraw;
    }

    public void setVolDraw(IChartViewDraw mVolDraw) {
        this.mVolDraw = mVolDraw;
    }

    /**
     * 二分查找当前值的index
     * TODO 递归调用 (可能导致栈溢出)
     *
     * @return
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        while (true) {
            if (end <= start) {
                return start;
            }
            if (end - start == 1) {
                float startValue = getX(start);
                float endValue = getX(end);
                return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
            }
            int mid = start + (end - start) / 2;
            float midValue = getX(mid);
            if (translateX < midValue) {
                end = mid;
            } else if (translateX > midValue) {
                start = mid;
            } else {
                return mid;
            }
        }
    }

    /**
     * 设置数据适配器
     */
    public void setAdapter(IAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            itemCount = mAdapter.getCount();
            Log.d(TAG, "========itemCount:====" + itemCount);
        } else {
            itemCount = 0;
        }
        notifyChanged();
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (animator != null) {
            animator.start();
        }
    }

    /**
     * 设置动画时间
     */
    public void setAnimationDuration(long duration) {
        if (animator != null) {
            animator.setDuration(duration);
        }
    }

    /**
     * 设置表格行数
     */
    public void setGridRows(int gridRows) {
        if (gridRows < 1) {
            gridRows = 1;
        }
        this.gridRows = gridRows;
    }

    /**
     * 设置表格列数
     */
    public void setGridColumns(int gridColumns) {
        if (gridColumns < 1) {
            gridColumns = 1;
        }
        this.gridColumns = gridColumns;
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    public float xToTranslateX(float x) {
        return -translateX + x / mScaleX;
    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    public float translateXtoX(float translateX) {
        return (translateX + this.translateX) * mScaleX;
    }

    /**
     * 获取上方padding
     */
    public float getTopPadding() {
        return topPadding;
    }

    /**
     * 获取上方padding
     */
    public float getChildPadding() {
        return childPadding;
    }

    /**
     * 获取子试图上方padding
     */
    public float getmChildScaleYPadding() {
        return childPadding;
    }

    /**
     * 获取图的宽度
     *
     * @return
     */
    public int getChartWidth() {
        return width;
    }

    /**
     * 是否长按
     */
    public boolean isLongPress() {
        return isLongPress;
    }

    /**
     * 获取选择索引
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public Rect getChildRect() {
        return childRect;
    }

    public Rect getVolRect() {
        return volRect;
    }

    /**
     * 设置选择监听
     */
    public void setOnSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void onSelectedChanged(BaseKLineChartView view, Object point, int index) {
        if (this.mOnSelectedChangedListener != null) {
            mOnSelectedChangedListener.onSelectedChanged(view, point, index);
        }
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    public boolean isFullScreen() {
        return dataLen >= width / mScaleX;
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        this.overScrollRange = overScrollRange;
    }

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    public void setTopPadding(int topPadding) {
        this.topPadding = topPadding;
    }

    /**
     * 设置下方padding
     *
     * @param bottomPadding
     */
    public void setBottomPadding(int bottomPadding) {
        this.bottomPadding = bottomPadding;
    }

    /**
     * 设置表格线宽度
     */
    public void setGridLineWidth(float width) {
        gridPaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setGridLineColor(int color) {
        gridPaint.setColor(color);
    }

    /**
     * 设置选择器横线宽度
     */
    public void setSelectedXLineWidth(float width) {
        selectedXLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器横线颜色
     */
    public void setSelectedXLineColor(int color) {
        selectedXLinePaint.setColor(color);
    }

    /**
     * 设置选择器竖线宽度
     */
    public void setSelectedYLineWidth(float width) {
        selectedYLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器竖线颜色
     */
    public void setSelectedYLineColor(int color) {
        selectedYLinePaint.setColor(color);
    }

    public void setSelectedTextColor(int color) {
        selectedTextPaint.setColor(color);
    }


    /**
     * 设置文字颜色
     */
    public void setTextColor(int color) {
        textPaint.setColor(color);
    }

    public void setBoundaryValueColor(int color) {
        boundaryValuePaint.setColor(color);
        timePaint.setColor(color);
    }


    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        textPaint.setTextSize(textSize);
        boundaryValuePaint.setTextSize(textSize);
        timePaint.setTextSize(textSize);
        selectedTextPaint.setTextSize(textSize);
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    public void setMTextColor(int color) {
        maxMinPaint.setColor(color);
    }

    /**
     * 设置最大值/最小值文字大小
     */
    public void setMTextSize(float textSize) {
        maxMinPaint.setTextSize(textSize);
    }

    /**
     * 设置背景颜色
     */
    @Override
    public void setBackgroundColor(int color) {
        bgPaint.setColor(color);
    }

    /**
     * 设置分时线填充渐变的下部颜色
     *
     * @param color time line top fill color
     * @return {@link KLineChartView}
     */
    public void setTimeLineFillTopColor(int color) {
        this.timeLineFillTopColor = color;
    }

    /**
     * 设置分时线填充渐变的下部颜色
     *
     * @param color time line bottom fill color
     * @return {@link KLineChartView}
     */
    public void setTimeLineFillBottomColor(int color) {
        this.timeLineFillBottomColor = color;
    }



    /**
     * 设置选中point 值显示背景
     */
    public void setSelectPointColor(int color) {
        selectPointPaint.setColor(color);
    }

    /**
     * 选中点变化时的监听
     */
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseKLineChartView view, Object point, int index);
    }

    /**
     * 获取文字大小
     */
    public float getTextSize() {
        return textPaint.getTextSize();
    }

    /**
     * 获取曲线宽度
     */
    public float getLineWidth() {
        return mLineWidth;
    }

    /**
     * 设置曲线的宽度
     */
    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    /**
     * 设置每个点的宽度
     */
    public void setPointWidth(float pointWidth) {
        this.pointWidth = pointWidth;
    }

    public Paint getGridPaint() {
        return gridPaint;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public Paint getBackgroundPaint() {
        return bgPaint;
    }

    public int getDisplayHeight() {
        return displayHeight + topPadding + bottomPadding;
    }


}
