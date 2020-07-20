package com.sl.ui.library.base;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.sl.ui.library.R;
import com.sl.ui.library.service.event.EventBusUtil;
import com.sl.ui.library.service.event.MessageEvent;
import com.sl.ui.library.utils.SoftKeyboardUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public abstract class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    protected View layoutView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (null == layoutView) {
            //setTheme();
            layoutView = inflater.inflate(setContentView(), container, false);
            loadData();
        } else {
            ViewParent viewParent = layoutView.getParent();
            if (null != viewParent && viewParent instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) viewParent;
                vg.removeView(layoutView);
            }
        }
        return layoutView;
    }

    private void setTheme() {
        this.getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = this.getDialog().getWindow();
        //去掉dialog默认的padding
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置dialog的位置在底部
        lp.gravity = Gravity.BOTTOM;
        //设置dialog的动画
        lp.windowAnimations = R.style.leftin_rightout_DialogFg_animstyle;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }


    protected abstract int setContentView();

    protected <T extends View> T findViewById(int id) {
        return layoutView.findViewById(id);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBusUtil.register(this);
        hideKeyboard();
        initView();

    }

    protected abstract void initView();

    protected abstract void loadData();


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBusUtil.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onMessageEvent(MessageEvent event) {
    }

    @Override
    public void onClick(View v) {
        dismissDialog();
    }

    protected void dismissDialog() {
        if (isVisible()) {
            dismiss();
        }
    }

    @Override
    public void dismiss() {
        hideKeyboard();
        super.dismiss();
    }

    private void hideKeyboard() {
        View view = getDialog().getCurrentFocus();
        SoftKeyboardUtil.hideSoftKeyboard(view);
    }

    /*
     * 展示dialog
     */
    protected void showDialog(FragmentManager manager, String tag) {
        show(manager, tag);
    }
}
