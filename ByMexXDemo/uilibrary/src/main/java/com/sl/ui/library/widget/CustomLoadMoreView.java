package com.sl.ui.library.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.chad.library.adapter.base.loadmore.BaseLoadMoreView;
import com.chad.library.adapter.base.loadmore.LoadMoreStatus;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sl.ui.library.R;

import org.jetbrains.annotations.NotNull;

public class CustomLoadMoreView extends BaseLoadMoreView {

    private ProgressDrawable progressDrawable;

    private void stopLoading(){
        if(progressDrawable != null){
            progressDrawable.stop();
        }
    }

    private void startLoading(){
        if(progressDrawable != null){
            progressDrawable.start();
        }
    }

    @Override
    public void convert(@NotNull BaseViewHolder holder, int position, @NotNull LoadMoreStatus loadMoreStatus) {
        super.convert(holder, position, loadMoreStatus);

        if(loadMoreStatus == LoadMoreStatus.Loading){
            startLoading();
        }else {
            stopLoading();
        }
    }

    @NotNull
    @Override
    public View getLoadComplete(@NotNull BaseViewHolder holder) {
        // 布局中 “当前一页加载完成”的View
        return holder.findView(R.id.load_more_load_complete_view);
    }

    @NotNull
    @Override
    public View getLoadEndView(@NotNull BaseViewHolder holder) {
        // 布局中 “全部加载结束，没有数据”的View
        return holder.findView(R.id.load_more_load_end_view);
    }

    @NotNull
    @Override
    public View getLoadFailView(@NotNull BaseViewHolder holder) {
        // 布局中 “加载失败”的View
        return holder.findView(R.id.load_more_load_fail_view);
    }

    @NotNull
    @Override
    public View getLoadingView(@NotNull BaseViewHolder holder) {
        // 布局中 “加载中”的View
        return holder.findView(R.id.load_more_loading_view);
    }

    @NotNull
    @Override
    public View getRootView(@NotNull ViewGroup parent) {
        progressDrawable = new ProgressDrawable();
       View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_load_more, parent, false);
        ImageView loading_progress = view.findViewById(R.id.loading_progress);
        loading_progress.setImageDrawable(progressDrawable);
        return view;
    }
}
