package com.sl.bymex.api;

import com.hjq.http.config.IRequestApi;

public final  class AreaApi implements IRequestApi {
    @Override
    public String getApi() {
        return PublicApiConstant.sPhoneCode;
    }
}
