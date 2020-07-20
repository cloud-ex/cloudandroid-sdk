package com.sl.bymex.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class DwRecord implements Parcelable {
    public static final int SETTLE_STATUS_CREATED = 1;                   // 1 申请成功(用户提交申请)
    public static final int SETTLE_STATUS_PASSED = 2;                    // 2 审核通过(运营审核通过)
    public static final int SETTLE_STATUS_REJECTED = 3;                   // 3 审核拒绝(运营审核拒绝)
    public static final int SETTLE_STATUS_SIGNED = 4;                     // 4 签名完成(生成转账signstr完成)
    public static final int SETTLE_STATUS_PENDING = 5;                    // 5 打包中(待确认链上是否转账成功)
    public static final int SETTLE_STATUS_SUCCESS = 6;                    // 6 成功(转账成功)
    public static final int SETTLE_STATUS_FAILED = 7;                     // 7 失败(转账失败)

    private long settle_id;
    private long account_id;
    private String from_address;
    private String to_address;
    private String coin_code;
    private String block_id;
    private String block_hash;
    private String tx_hash;
    private int type;
    private int status;
    private String vol;
    private String fee;
    private boolean unfreeze_funds;
    private String created_at;
    private String updated_at;

    public DwRecord() {
    }

    protected DwRecord(Parcel in) {
        settle_id = in.readLong();
        account_id = in.readLong();
        from_address = in.readString();
        to_address = in.readString();
        coin_code = in.readString();
        block_id = in.readString();
        block_hash = in.readString();
        tx_hash = in.readString();
        type = in.readInt();
        status = in.readInt();
        vol = in.readString();
        fee = in.readString();
        unfreeze_funds = in.readByte() != 0;
        created_at = in.readString();
        updated_at = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(settle_id);
        dest.writeLong(account_id);
        dest.writeString(from_address);
        dest.writeString(to_address);
        dest.writeString(coin_code);
        dest.writeString(block_id);
        dest.writeString(block_hash);
        dest.writeString(tx_hash);
        dest.writeInt(type);
        dest.writeInt(status);
        dest.writeString(vol);
        dest.writeString(fee);
        dest.writeByte((byte) (unfreeze_funds ? 1 : 0));
        dest.writeString(created_at);
        dest.writeString(updated_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DwRecord> CREATOR = new Creator<DwRecord>() {
        @Override
        public DwRecord createFromParcel(Parcel in) {
            return new DwRecord(in);
        }

        @Override
        public DwRecord[] newArray(int size) {
            return new DwRecord[size];
        }
    };

    public long getSettle_id() {
        return settle_id;
    }

    public void setSettle_id(long settle_id) {
        this.settle_id = settle_id;
    }

    public long getAccount_id() {
        return account_id;
    }

    public void setAccount_id(long account_id) {
        this.account_id = account_id;
    }

    public String getFrom_address() {
        return from_address;
    }

    public void setFrom_address(String from_address) {
        this.from_address = from_address;
    }

    public String getTo_address() {
        return to_address;
    }

    public void setTo_address(String to_address) {
        this.to_address = to_address;
    }

    public String getCoin_code() {
        return coin_code;
    }

    public void setCoin_code(String coin_code) {
        this.coin_code = coin_code;
    }

    public String getBlock_id() {
        return block_id;
    }

    public void setBlock_id(String block_id) {
        this.block_id = block_id;
    }

    public String getBlock_hash() {
        return block_hash;
    }

    public void setBlock_hash(String block_hash) {
        this.block_hash = block_hash;
    }

    public String getTx_hash() {
        return tx_hash;
    }

    public void setTx_hash(String tx_hash) {
        this.tx_hash = tx_hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }


    public void setStatus(int status) {
        this.status = status;
    }

    public String getVol() {
        if(TextUtils.isEmpty(vol)){
            vol = "0";
        }
        return vol;
    }

    public void setVol(String vol) {
        this.vol = vol;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public boolean isUnfreeze_funds() {
        return unfreeze_funds;
    }

    public void setUnfreeze_funds(boolean unfreeze_funds) {
        this.unfreeze_funds = unfreeze_funds;
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
