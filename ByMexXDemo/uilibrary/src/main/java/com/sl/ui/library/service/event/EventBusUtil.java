package com.sl.ui.library.service.event;


import org.greenrobot.eventbus.EventBus;

/**
 * Created by wanghao on 2018/5/3.
 */

public class EventBusUtil {
    /*
     * 注册订阅者
     */
    public static void register(Object obj){
        if(!EventBus.getDefault().isRegistered(obj)){
            EventBus.getDefault().register(obj);
        }
    }

    /*
     * 注销订阅者
     */
    public static void unregister(Object obj){
        EventBus.getDefault().unregister(obj);
    }

    /*
     *  发布普通事件
     */
    public static void post(MessageEvent event){
        EventBus.getDefault().post(event);
    }

}
