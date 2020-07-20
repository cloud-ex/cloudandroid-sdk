package com.sl.bymex.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.contract.sdk.ContractSDKAgent;
import com.netease.nis.captcha.Captcha;
import com.netease.nis.captcha.CaptchaListener;
import com.sl.bymex.R;
import com.sl.bymex.app.ByMexApp;
import com.sl.bymex.service.net.NetUrl;
import com.sl.ui.library.utils.SystemUtils;


/**
 * 网易云盾
 * Created by zhoujing on 2017/10/19.
 */

public class LogicCaptcha {
    private final static String TAG = "Captcha";

    private ICaptchaListener mListener;
    public interface ICaptchaListener {
        void onCaptchaValidate(boolean succeed, String validate);
    }

    private Captcha mCaptcha = null;
    private UserLoginTask mLoginTask = null;
    CaptchaListener myCaptchaListener = new CaptchaListener() {

        @Override
        public void onValidate(String result, String validate, String message) {
            //验证结果，valiadte，可以根据返回的三个值进行用户自定义二次验证
            if (validate.length() > 0) {
                if (mListener != null) {
                    mListener.onCaptchaValidate(true, validate);
                }
            } else {
                if (SystemUtils.isZh()) {
                    mListener.onCaptchaValidate(false, ByMexApp.appContext.getString(R.string.str_captcha_verify_failed));
                } else {
                    mListener.onCaptchaValidate(false, "Verify Failed");
                }
            }
        }

        @Override
        public void closeWindow() {
            //请求关闭页面
        }

        @Override
        public void onError(String errormsg) {
            //出错
            Log.i(TAG, errormsg);
            if (mListener != null) {
                mListener.onCaptchaValidate(false, "");
            }
        }

        @Override
        public void onCancel() {
            //用户取消加载或者用户取消验证，关闭异步任务，也可根据情况在其他地方添加关闭异步任务接口
            if (mLoginTask != null) {
                if (mLoginTask.getStatus() == AsyncTask.Status.RUNNING) {
                    Log.i(TAG, "stop mLoginTask");
                    mLoginTask.cancel(true);
                }
            }
            if (mListener != null) {
                mListener.onCaptchaValidate(false, ContractSDKAgent.INSTANCE.getContext().getString(R.string.str_user_cancel));
            }
        }

        @Override
        public void onReady(boolean ret) {
            //该为调试接口，ret为true表示加载Sdk完成
        }

    };

    private static LogicCaptcha instance = null;

    public static LogicCaptcha getInstance(){
        if (null == instance)
            instance = new LogicCaptcha();
        return instance;
    }


    private LogicCaptcha(){

    }

    public void start(Context context, ICaptchaListener listener) {

        mListener = null;
        mListener = listener;

        //初始化验证码SDK相关参数，设置CaptchaId、Listener最后调用start初始化。
        mCaptcha = new Captcha(context);

        mCaptcha.setCaptchaId(NetUrl.INSTANCE.getCaptchaId());
        mCaptcha.setCaListener(myCaptchaListener);
        //可选:设置验证码语言为英文，默认为中文
        if (!SystemUtils.isZh()) {
            mCaptcha.setEnglishLanguage();
        }
        //可选：开启debug
        mCaptcha.setDebug(false);
        //可选：设置超时时间
        mCaptcha.setTimeout(10000);

        //必填：初始化 captcha框架
        mCaptcha.start();
        mLoginTask = new UserLoginTask();
        //关闭mLoginTask任务可以放在myCaptchaListener的onCancel接口中处理
        mLoginTask.execute();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        UserLoginTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //可选：简单验证DeviceId、CaptchaId、Listener值
            if (mCaptcha != null) {
                return mCaptcha.checkParams();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                //必填：开始验证
                if (mCaptcha != null) {
                    mCaptcha.Validate();
                }

            } else {
                mListener.onCaptchaValidate(false, ByMexApp.appContext.getString(R.string.str_captcha_verify_failed));
            }
        }

        @Override
        protected void onCancelled() {
            mLoginTask = null;
        }
    }
}
