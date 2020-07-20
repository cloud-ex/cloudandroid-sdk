package com.sl.bymex.data;

import android.os.Parcel;
import android.os.Parcelable;

public class SpotCoin implements Parcelable {
    private String name;
    private String coin_group;
    private String big;
    private String small;
    private String gray;
    private int cash_ability;
    private int deposit_style;
    private String full_name_en;
    private String full_name_zh;
    private int block;
    private String price_unit;
    private String vol_unit;
    private Boolean selected = false;

    public SpotCoin() {
    }

    protected SpotCoin(Parcel in) {
        name = in.readString();
        coin_group = in.readString();
        big = in.readString();
        small = in.readString();
        gray = in.readString();
        cash_ability = in.readInt();
        deposit_style = in.readInt();
        full_name_en = in.readString();
        full_name_zh = in.readString();
        block = in.readInt();
        price_unit = in.readString();
        vol_unit = in.readString();
        byte tmpSelected = in.readByte();
        selected = tmpSelected == 0 ? null : tmpSelected == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(coin_group);
        dest.writeString(big);
        dest.writeString(small);
        dest.writeString(gray);
        dest.writeInt(cash_ability);
        dest.writeInt(deposit_style);
        dest.writeString(full_name_en);
        dest.writeString(full_name_zh);
        dest.writeInt(block);
        dest.writeString(price_unit);
        dest.writeString(vol_unit);
        dest.writeByte((byte) (selected == null ? 0 : selected ? 1 : 2));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SpotCoin> CREATOR = new Creator<SpotCoin>() {
        @Override
        public SpotCoin createFromParcel(Parcel in) {
            return new SpotCoin(in);
        }

        @Override
        public SpotCoin[] newArray(int size) {
            return new SpotCoin[size];
        }
    };

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoin_group() {
        return coin_group;
    }

    public void setCoin_group(String coin_group) {
        this.coin_group = coin_group;
    }

    public String getBig() {
        return big;
    }

    public void setBig(String big) {
        this.big = big;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getGray() {
        return gray;
    }

    public void setGray(String gray) {
        this.gray = gray;
    }

    public int getCash_ability() {
        return cash_ability;
    }

    public void setCash_ability(int cash_ability) {
        this.cash_ability = cash_ability;
    }

    public int getDeposit_style() {
        return deposit_style;
    }

    public void setDeposit_style(int deposit_style) {
        this.deposit_style = deposit_style;
    }

    public String getFull_name_en() {
        return full_name_en;
    }

    public void setFull_name_en(String full_name_en) {
        this.full_name_en = full_name_en;
    }

    public String getFull_name_zh() {
        return full_name_zh;
    }

    public void setFull_name_zh(String full_name_zh) {
        this.full_name_zh = full_name_zh;
    }

    public int getBlock() {
        return block;
    }

    public void setBlock(int block) {
        this.block = block;
    }

    public String getPrice_unit() {
        return price_unit;
    }

    public void setPrice_unit(String price_unit) {
        this.price_unit = price_unit;
    }

    public String getVol_unit() {
        return vol_unit;
    }

    public void setVol_unit(String vol_unit) {
        this.vol_unit = vol_unit;
    }

    public int getPrice_index() {

        if (price_unit.equals("0")) {
            return 8;
        } else {
            return price_unit.length() - price_unit.indexOf(".") - 1;
        }
    }

    public int getVol_index() {

        if (vol_unit.equals("0")) {
            return 0;
        } else if (vol_unit.contains(".")) {
            return  vol_unit.length() - vol_unit.indexOf(".") - 1;
        } else {
            return 0;
        }
    }

}
