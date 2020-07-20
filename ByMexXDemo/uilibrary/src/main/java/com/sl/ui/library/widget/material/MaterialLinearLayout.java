package com.sl.ui.library.widget.material;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.sl.ui.library.R;


public class MaterialLinearLayout extends LinearLayout implements MaterialBackgroundDetector.Callback {

    private MaterialBackgroundDetector mDetector;

    public MaterialLinearLayout(Context context) {
        super(context);
        init(null, 0);
    }

    public MaterialLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MaterialLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {
        int color =  getResources().getColor(R.color.btn_touch_color);
        mDetector = new MaterialBackgroundDetector(getContext(), this, null, color);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDetector.onSizeChanged(w, h);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superResult = super.onTouchEvent(event);
        return mDetector.onTouchEvent(event, superResult);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            return;
        }
        mDetector.draw(canvas);
    }

    @Override
    public boolean performLongClick() {
        return super.performLongClick();
    }

    @Override
    public void performClickAfterAnimation() {
        super.performClick();
    }

    @Override
    public void performLongClickAfterAnimation() {
        super.performLongClick();

    }
}
