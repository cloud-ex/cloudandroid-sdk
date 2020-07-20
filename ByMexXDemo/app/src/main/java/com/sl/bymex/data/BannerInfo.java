package com.sl.bymex.data;

public class BannerInfo {
    private String imgUrl = "https://img.zcool.cn/community/011ad05e27a173a801216518a5c505.jpg";

    public BannerInfo(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
