package com.sl.ui.library.data;

import android.text.TextUtils;

public class CoinFeeRate {
    private String name;
    private String price_usd;
    private String price_btc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice_usd() {
        if(TextUtils.isEmpty(price_usd)){
            price_usd = "0";
        }
        return price_usd;
    }

    public void setPrice_usd(String price_usd) {
        this.price_usd = price_usd;
    }

    public String getPrice_btc() {
        if(TextUtils.isEmpty(price_btc)){
            price_btc = "0";
        }
        return price_btc;
    }

    public void setPrice_btc(String price_btc) {
        this.price_btc = price_btc;
    }
}
