package com.sl.ui.library.data;

import android.text.TextUtils;

/**
 * usd费率
 */
public class UsdRates {
    private String name;
    private String rate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRate() {
        if(TextUtils.isEmpty(rate)){
            return "0";
        }
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }
}
