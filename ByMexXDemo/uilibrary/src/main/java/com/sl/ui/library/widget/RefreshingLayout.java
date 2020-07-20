package com.sl.ui.library.widget;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sl.ui.library.R;

public class RefreshingLayout extends SwipeRefreshLayout {
    public RefreshingLayout(@NonNull Context context) {
        super(context);
        iniView();
    }

    public RefreshingLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        iniView();
    }

    private void iniView() {
        setColorSchemeColors(getResources().getColor(R.color.main_yellow),getResources().getColor(R.color.main_yellow_8));
        setProgressViewEndTarget(true, 200);
    }
}
