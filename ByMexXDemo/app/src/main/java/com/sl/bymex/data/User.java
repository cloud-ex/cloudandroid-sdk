package com.sl.bymex.data;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class User {

    /**
     * account_id : 16800455
     * phone : +86 18500644127
     * account_type : 2
     * owner_type : 1
     * status : 2
     * register_ip : 114.241.190.131
     * latest_login_ip : 114.241.190.131
     * asset_password_effective_time : -2
     * ga_key : unbound
     * kyc_type : 0
     * first_name : 斌
     * last_name : 李
     * kyc_status : 5
     * kyc_reject_reason : 123
     * created_at : 2019-09-06T03:14:35.378449Z
     * updated_at : 2019-12-03T09:58:20.822604Z
     */

    private long account_id;
    private String phone;
    private String email;
    private int account_type;
    private int owner_type;
    private int status;
    private String register_ip;
    private String latest_login_ip;
    private int asset_password_effective_time;
    private String ga_key;
    private int kyc_type;
    private String first_name;
    private String last_name;
    private int kyc_status;
    private String kyc_reject_reason;
    private String created_at;
    private String updated_at;
    private String token;
    private String account_name;

    private List<UserAsset> user_assets;

    public List<UserAsset> getUser_assets() {
        if(user_assets == null){
            user_assets = new ArrayList<UserAsset>();
        }
        return user_assets;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public void setUser_assets(List<UserAsset> user_assets) {
        this.user_assets = user_assets;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getAccount_id() {
        return account_id;
    }

    /**
     * 展示昵称
     * @return
     */
    public String getShowName(){
        if(TextUtils.isEmpty(account_name)){
            return account_id+"";
        }
        return account_name;
    }
    /**
     * 得到登录号码
     * @return
     */
    public String getLoginNum(){
        if(TextUtils.isEmpty(phone)){
            return email;
        }
        return phone;
    }


    public void setAccount_id(long account_id) {
        this.account_id = account_id;
    }

    public String getPhone() {
        //TODO 补丁模式  手动去掉+86
        if(!TextUtils.isEmpty(phone) && phone.contains("+86")){
            phone = phone.split(" ")[1];
        }
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccount_type() {
        return account_type;
    }

    public void setAccount_type(int account_type) {
        this.account_type = account_type;
    }

    public int getOwner_type() {
        return owner_type;
    }

    public void setOwner_type(int owner_type) {
        this.owner_type = owner_type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRegister_ip() {
        return register_ip;
    }

    public void setRegister_ip(String register_ip) {
        this.register_ip = register_ip;
    }

    public String getLatest_login_ip() {
        return latest_login_ip;
    }

    public void setLatest_login_ip(String latest_login_ip) {
        this.latest_login_ip = latest_login_ip;
    }

    public int getAsset_password_effective_time() {
        return asset_password_effective_time;
    }

    public void setAsset_password_effective_time(int asset_password_effective_time) {
        this.asset_password_effective_time = asset_password_effective_time;
    }

    public String getGa_key() {
        return ga_key;
    }

    public void setGa_key(String ga_key) {
        this.ga_key = ga_key;
    }

    public int getKyc_type() {
        return kyc_type;
    }

    public void setKyc_type(int kyc_type) {
        this.kyc_type = kyc_type;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public int getKyc_status() {
        return kyc_status;
    }

    public void setKyc_status(int kyc_status) {
        this.kyc_status = kyc_status;
    }

    public String getKyc_reject_reason() {
        return kyc_reject_reason;
    }

    public void setKyc_reject_reason(String kyc_reject_reason) {
        this.kyc_reject_reason = kyc_reject_reason;
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
