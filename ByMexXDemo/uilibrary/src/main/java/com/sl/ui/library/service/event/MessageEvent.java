package com.sl.ui.library.service.event;

public class MessageEvent {
    public static final int sl_login_token_change_event = 10034; //token改变通知
    public static final int sl_spot_assets_change_event = 10035; //现货资产有变动(可能)
    /**
     * 合约侧边栏
     */
    public static final int sl_contract_left_coin_type = 10041;
    /**
     * 合约SDK初始化完成
     */
    public static final int contract_sdk_init_complete = 10043;
    /**
     * 昵称改变通知
     */
    public static final int nick_name_change_notify = 10046;
    /**
     * 账户安全相关配置修改
     * 1:绑定手机邮箱
     */
    public static final int account_info_change_notify = 10047;
    /**
     * 币种选择通知
     */
    public static final int filter_coin_type_select_notify = 10048;
    /**
     * 合约选择杠杆
     */
    public static final int contract_select_leverage_event = 10049;
    /**
     * 委托订单选择通知
     */
    public static final int filter_order_type_select_notify = 10050;
    /**
     * 切换语言通知
     */
    public static final int SWITCH_APP_LANG_notify = 10051;
    /**
     * 切换语言通知
     */
    public static final int SELECT_DEPTH_PRICE_notify = 10052;


    private MessageEvent() {
    }

    private Object msg_content;//事件内容
    private int msg_type;//事件类型

    public MessageEvent(int msg_type) {
        this.msg_type = msg_type;
    }


    public MessageEvent(int msg_type, Object msg_content) {
        this.msg_content = msg_content;
        this.msg_type = msg_type;
    }


    public MessageEvent setMsg_content(Object msg_content) {
        this.msg_content = msg_content;
        return this;
    }

    public Object getMsg_content() {
        return msg_content;
    }

    public int getMsg_type() {
        return msg_type;
    }

    @Override
    public String toString() {
        return "SdkMessageEvent{" +
                "msg_content=" + msg_content +
                ", msg_type=" + msg_type +
                '}';
    }
}