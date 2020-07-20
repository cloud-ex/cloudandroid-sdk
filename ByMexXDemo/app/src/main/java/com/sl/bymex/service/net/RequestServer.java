package com.sl.bymex.service.net;

import com.hjq.http.config.IRequestServer;
import com.hjq.http.model.BodyType;

/**
 * 请求配置
 */
public class RequestServer implements IRequestServer {
    @Override
    public String getHost() {
        return NetUrl.INSTANCE.getBaseUrl();
    }

    @Override
    public BodyType getType() {
        return  BodyType.JSON;
    }

    @Override
    public String getPath() {
        //path写在具体接口路径上
        return "";
    }
}
