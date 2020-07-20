package com.sl.bymex.service.net;

import com.contract.sdk.ContractSDKAgent;
import com.contract.sdk.utils.SDKLogUtil;
import com.hjq.http.config.ILogStrategy;

public class HttpLogStrategy implements ILogStrategy {
    public static String sTag = ContractSDKAgent.sTAG;
    @Override
    public void print() {
        SDKLogUtil.INSTANCE.d(sTag,"----------------华丽的分割线---------------");
    }

    @Override
    public void print(String log) {
        SDKLogUtil.INSTANCE.d(sTag,log);
    }

    @Override
    public void json(String json) {
        SDKLogUtil.INSTANCE.d(sTag,json);
    }

    @Override
    public void print(Throwable throwable) {
        SDKLogUtil.INSTANCE.d(sTag,"throwable:"+throwable.toString());
    }

    @Override
    public void print(String key, String value) {
        SDKLogUtil.INSTANCE.d(sTag,"KV:"+key+"="+value);
    }
}
