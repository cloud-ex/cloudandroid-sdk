package com.sl.bymex.api;

public class PublicApiConstant {
    /**
     * public_info
     */
    public static final String sPublicInfo = "v1/ifglobal/global";
    /**
     * 费率接口
     */
    public static final String sCoinPrice = "v1/ifglobal/coinPrice";
    /**
     * 验证码校验
     */
    public static final String sCaptchCheck = "v1/ifaccount/captchCheck";
    /**
     * 检查用户是否存在
     */
    public static final String sUserCheck = "v1/ifaccount/users/check";
    /**
     * 重置密码
     */
    public static final String sResetPassword = "v1/ifaccount/users/resetPassword";
    /**
     * 发送短信/邮箱验证码
     */
    public static final String sSendVerifyCode = "v1/ifaccount/verifyCode";
    /**
     * 国家地区
     */
    public static final String sPhoneCode = "v1/ifglobal/phoneCode";
    /**
     * 登录
     */
    public static final String sLogin = "v1/ifaccount/login";
    /**
     * 注册
     */
    public static final String sRegister = "v1/ifaccount/users/register";
    /**
     * 获取现货用户资产
     */
    public static final String sSpotAccount = "v1/ifaccount/users/me";
    /**
     * 资金划转
     */
    public static final String sSpotAssetTransfer = "swap/transferFunds";
    /**
     * 设置昵称
     */
    public static final String sSpotSetAccountName = "v1/ifaccount/user/accountName";
    /**
     * 绑定邮箱
     */
    public static final String sSpotBindEmail = "v1/ifaccount/bindEmail";
    /**
     * 绑定手机号
     */
    public static final String sSpotBindPhone = "v1/ifaccount/bindPhone";
    /**
     *谷歌验证码相关操作
     */
    public static final String sGoogleCodeQuery = "v1/ifaccount/GAKey?action=query";
    public static final String sGoogleCodeAdd = "v1/ifaccount/GAKey?action=add";
    public static final String sGoogleCodeDelete = "v1/ifaccount/GAKey?action=delete";
    /**
     *资金密码操作
     */
    public static final String sAssetPasswordAdd = "v1/ifaccount/assetPassword?action=add";
    public static final String sAssetPasswordReset = "v1/ifaccount/assetPassword?action=reset";
    /**
     * 资金密码有效期
     */
    public static final String sAssetPasswordResetEffectiveTime = "v1/ifaccount/assetPasswordEffectiveTime?action=reset";
    /**
     * 充值地址
     */
    public static final String sDepositAddress = "v1/ifaccount/address";
    /**
     * 提现
     */
    public static final String sDoWithdraw = "v1/ifaccount/withdraw";
    /**
     * 充提记录
     */
    public static final String sDWithdrawRecord = "v1/ifaccount/rewards";



    //服务协议
    public static final String BTURL_TERMS_STATEMENT_EN = "https://www.abit.com/terms?lang=en-us";
    public static final String BTURL_TERMS_STATEMENT_CN = "https://www.abit.com/terms?lang=zh-CN";
    ///风险披露说明
    public static final String BTURL_RISK_STATEMENT_EN = "https://www.abit.com/terms?lang=en-us";
    public static final String BTURL_RISK_STATEMENT_CN = "https://www.abit.com/terms?lang=zh-CN";
}
