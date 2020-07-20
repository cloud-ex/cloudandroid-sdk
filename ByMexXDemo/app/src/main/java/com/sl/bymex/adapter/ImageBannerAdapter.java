package com.sl.bymex.adapter;

import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sl.bymex.data.BannerInfo;
import com.youth.banner.adapter.BannerAdapter;

import java.util.List;

public class ImageBannerAdapter extends BannerAdapter<BannerInfo,ImageBannerAdapter.BannerViewHolder> {

    public ImageBannerAdapter(List<BannerInfo> datas) {
        super(datas);
    }

    public void updateData(List<BannerInfo> data) {
        //这里的代码自己发挥，比如如下的写法等等
        mDatas.clear();
        mDatas.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());
        //注意，必须设置为match_parent，这个是viewpager2强制要求的
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

//        //通过裁剪实现圆角
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            BannerUtils.setBannerRound(imageView,20);
//        }

        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindView(BannerViewHolder holder, BannerInfo data, int position, int size) {
        Glide.with(holder.itemView)
                .load(data.getImgUrl())
//                .apply(RequestOptions.bitmapTransform(new RoundedCorners(30)))
                .into(holder.imageView);
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public BannerViewHolder(@NonNull ImageView view) {
            super(view);
            this.imageView = view;
        }
    }
}
