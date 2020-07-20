package com.sl.bymex.data;

import android.text.TextUtils;

public class UserAsset {
    private String coin_code;
    private String freeze_vol;
    private String available_vol;
    private String created_at;
    private String updated_at;

    public String getCoin_code() {
        return coin_code;
    }

    public void setCoin_code(String coin_code) {
        this.coin_code = coin_code;
    }

    public String getFreeze_vol() {
        return freeze_vol;
    }

    public void setFreeze_vol(String freeze_vol) {
        this.freeze_vol = freeze_vol;
    }

    public String getAvailable_vol() {
        if(TextUtils.isEmpty(available_vol)){
            available_vol = "0";
        }
        return available_vol;
    }

    public void setAvailable_vol(String available_vol) {
        this.available_vol = available_vol;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
