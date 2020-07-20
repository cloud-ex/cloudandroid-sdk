package com.sl.bymex.data;

public class CoinScans {
    private String coin_group;
    private String address;
    private String tx;
    private String regex;

    public String getCoin_group() {
        return coin_group;
    }

    public void setCoin_group(String coin_group) {
        this.coin_group = coin_group;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTx() {
        return tx;
    }

    public void setTx(String tx) {
        this.tx = tx;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
