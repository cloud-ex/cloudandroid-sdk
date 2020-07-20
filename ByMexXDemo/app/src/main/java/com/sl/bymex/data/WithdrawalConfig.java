package com.sl.bymex.data;

/**
 * 提现配置
 */
public class WithdrawalConfig {
    private String coin_name;
    private String fee;
    private String fee_coin_name;
    private String min_vol;
    private String kyc_max_vol;
    private int coin_precision;
    private String wallet_max_vol;
    private String middle_wallet_max_vol;


    public String getCoin_name() {
        return coin_name;
    }

    public void setCoin_name(String coin_name) {
        this.coin_name = coin_name;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getFee_coin_name() {
        return fee_coin_name;
    }

    public void setFee_coin_name(String fee_coin_name) {
        this.fee_coin_name = fee_coin_name;
    }

    public String getMin_vol() {
        return min_vol;
    }

    public void setMin_vol(String min_vol) {
        this.min_vol = min_vol;
    }

    public String getKyc_max_vol() {
        return kyc_max_vol;
    }

    public void setKyc_max_vol(String kyc_max_vol) {
        this.kyc_max_vol = kyc_max_vol;
    }

    public int getCoin_precision() {
        return coin_precision;
    }

    public void setCoin_precision(int coin_precision) {
        this.coin_precision = coin_precision;
    }

    public String getWallet_max_vol() {
        return wallet_max_vol;
    }

    public void setWallet_max_vol(String wallet_max_vol) {
        this.wallet_max_vol = wallet_max_vol;
    }

    public String getMiddle_wallet_max_vol() {
        return middle_wallet_max_vol;
    }

    public void setMiddle_wallet_max_vol(String middle_wallet_max_vol) {
        this.middle_wallet_max_vol = middle_wallet_max_vol;
    }
}
