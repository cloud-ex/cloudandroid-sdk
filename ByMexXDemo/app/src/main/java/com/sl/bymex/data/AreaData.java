package com.sl.bymex.data;

import android.text.TextUtils;

import com.contract.sdk.ContractSDKAgent;
import com.github.promeg.pinyinhelper.Pinyin;
import com.sl.ui.library.data.BaseStickyBean;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class AreaData implements BaseStickyBean, Comparator<AreaData> {
    private String us_name;
    private String cn_name;
    private String code;
    private String us_index;
    private String cn_index;

    public AreaData(String us_name, String cn_name, String code) {
        this.us_name = us_name;
        this.cn_name = cn_name;
        this.code = code;
    }

    public AreaData() {
    }

    public String getUs_name() {
        return us_name;
    }

    public void setUs_name(String us_name) {
        this.us_name = us_name;
    }

    public String getCn_name() {
        return cn_name;
    }

    public void setCn_name(String cn_name) {
        this.cn_name = cn_name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUs_index() {
        return us_index;
    }

    public void setUs_index(String us_index) {
        this.us_index = us_index;
    }

    public String getCn_index() {
        return cn_index;
    }

    public void setCn_index(String cn_index) {
        this.cn_index = cn_index;
    }

    @NotNull
    @Override
    public String getStickItem() {
      boolean isZhEnv =  ContractSDKAgent.INSTANCE.isZhEnv();
      if(isZhEnv){
          return TextUtils.isEmpty(cn_name)?"-": Pinyin.toPinyin(cn_name, "").substring(0,1).toUpperCase();
      }else {
          return TextUtils.isEmpty(us_name)?"-":us_name.substring(0,1).toUpperCase();
      }
    }

    @Override
    public int compare(AreaData o1, AreaData o2) {
        return o1.getStickItem().compareTo(o2.getStickItem());
    }
}
