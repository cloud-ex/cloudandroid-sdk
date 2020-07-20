package com.sl.bymex.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sl.bymex.R;
import com.sl.ui.library.utils.DisplayUtils;


public class SectionDecoration extends RecyclerView.ItemDecoration {
    private DecorationCallback mCallback;

    private TextPaint mTextPaint;

    private Paint mPaint;

    private int topGap;

    private Paint.FontMetrics mFontMetrics;

    private Resources mRes;
    private Context context;

    public SectionDecoration(Context context, DecorationCallback decorationCallback) {
        this.context = context;
        mRes = context.getResources();
        mCallback = decorationCallback;


        mPaint = new Paint();
        mPaint.setColor(ContextCompat.getColor(context, R.color.bg_card_color));
        mTextPaint = new TextPaint();
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(DisplayUtils.INSTANCE.sp2px(context,16f));
        mTextPaint.setColor(ContextCompat.getColor(context, R.color.text_color));

        mFontMetrics = new Paint.FontMetrics();
        mTextPaint.getFontMetrics(mFontMetrics);
        //设置原点位置
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        topGap = DisplayUtils.INSTANCE.dip2px(context, 32);
    }


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        long groupId = mCallback.getGroupId(pos);
        if (groupId < 0) return;
        //同组第一个添加padding
        if (isFirstInGroup(pos)) {
            outRect.top = topGap;
        } else {
            outRect.top = 0;
        }
    }


    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        //list中子view的总数
        int itemCount = state.getItemCount();
        //当前屏幕上显示的子view数量
        int childCount = parent.getChildCount();
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - DisplayUtils.INSTANCE.dip2px(context, 15);
        float lineHeight = mTextPaint.getTextSize();
        long preGroupId, groupId = -1;
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);

            preGroupId = groupId;
            groupId = mCallback.getGroupId(position);
            if (groupId < 0 || groupId == preGroupId) continue;

            String textLine = mCallback.getGroupFirstLine(position).toUpperCase();
            if (TextUtils.isEmpty(textLine)) continue;

            int viewBottom = view.getBottom();
            float textY = Math.max(topGap, view.getTop());
            if (position + 1 < itemCount) {
                //下一个和当前不一样移动当前
                long nextGroupId = mCallback.getGroupId(position + 1);
                if (nextGroupId != groupId && viewBottom < textY) {
                    //组内最后一个view进入了header
                    textY = viewBottom;
                }
            }
            c.drawRect(left, textY - topGap, right, textY, mPaint);
            c.drawText(textLine, mRes.getDimension(R.dimen.dp_15), textY - (topGap - lineHeight) / 2, mTextPaint);
        }
    }

    private boolean isFirstInGroup(int pos) {
        if (pos == 0) {
            return true;
        } else {
            long preGroupId = mCallback.getGroupId(pos - 1);
            long groupId = mCallback.getGroupId(pos);
            return preGroupId != groupId;
        }
    }


    public interface DecorationCallback {

        long getGroupId(int position);

        String getGroupFirstLine(int position);
    }
}
