package com.sl.contract.library.helper;

import android.content.Context;


import com.sl.ui.library.utils.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhoujing on 2017/10/19.
 */

public class LogicContractSetting {

    public interface IContractSettingListener {
        void onContractSettingChange();
    }

    private static LogicContractSetting instance = null;

    public static LogicContractSetting getInstance(){
        if (null == instance)
            instance = new LogicContractSetting();
        return instance;
    }

    private List<IContractSettingListener> mListeners = new ArrayList<>();

    private LogicContractSetting(){

    }

    public void registListener(IContractSettingListener listener){

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount<mListeners.size(); iCount++){
            if(listener.equals(mListeners.get(iCount)))
                break;
        }

        if(iCount >= mListeners.size())
            mListeners.add(listener);
    }


    public void unregistListener(IContractSettingListener listener){

        if (listener == null) return;

        int iCount;
        for (iCount = 0; iCount<mListeners.size(); iCount++){
            if(listener.equals(mListeners.get(iCount))){
                mListeners.remove(mListeners.get(iCount));
                return;
            }
        }
    }

    public void refresh(){
        for (int i = 0; i<mListeners.size(); i++){
            if (mListeners.get(i) != null){
                mListeners.get(i).onContractSettingChange();
            }
        }
    }


    private static int s_contract_unit = 0;
    private static boolean s_contract_unit_first = true;
    //0 张 1 币
    public static int getContractUint(Context context) {
        if (s_contract_unit_first) {
            s_contract_unit = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_UNIT, 0);
            s_contract_unit_first = false;
        }
        return s_contract_unit;
    }

    public static void setContractUint(Context context, int unit) {
        s_contract_unit = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_UNIT, unit);
    }

    private static int s_pnl_calculate = 0;
    private static boolean s_pnl_calculate_first = true;
    //0 合理价格 1 最新成交价
    public static int getPnlCalculate(Context context) {
        if (s_pnl_calculate_first) {
            s_pnl_calculate = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_PNL_CALCULATE, 0);
            s_pnl_calculate_first = false;
        }
        return s_pnl_calculate;
    }

    public static void setPnlCalculate(Context context, int unit) {
        s_pnl_calculate = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_PNL_CALCULATE, unit);
    }

    private static int s_trigger_price_type = 1;
    private static boolean s_trigger_price_type_first = true;
    //1市场价 2合理价 4指数价
    public static int getTriggerPriceType(Context context) {
        if (s_trigger_price_type_first) {
            s_trigger_price_type = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_TRIGGER_PRICE_TYPE, 1);
            s_trigger_price_type_first = false;
        }
        //字段升级，需兼容老版本为0的情况
        if(s_trigger_price_type == 0){
            s_trigger_price_type = 1;
        }
        return s_trigger_price_type;
    }

    public static void setTriggerPriceType(Context context, int unit) {
        s_trigger_price_type = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_TRIGGER_PRICE_TYPE, unit);
    }

    private static int s_execution = 0;
    private static boolean s_execution_first = true;
    //0限价 1市价
    public static int getExecution(Context context) {
        if (s_execution_first) {
            s_execution = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_EXECUTION, 0);
            s_execution_first = false;
        }
        return s_execution;
    }

    public static void setExecution(Context context, int unit) {
        s_execution = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_EXECUTION, unit);
    }

    private static int s_strategy_effect_time = 0;
    private static boolean s_strategy_effect_time_first = true;
    //0 24h 17day
    public static int getStrategyEffectTime(Context context) {
        if (s_strategy_effect_time_first) {
            s_strategy_effect_time = PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, 1);
            s_strategy_effect_time_first = false;
        }
        return s_strategy_effect_time;
    }

    public static void setStrategyEffectTime(Context context, int unit) {
        s_strategy_effect_time = unit;
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_STRATEGY_EFFECTIVE_TIME, unit);
    }

    /**
     * 合约模式设置
     * @param context
     * @param mode 0 专业模式 1闪电模式
     */
    public static void setContractModeSetting(Context context, int mode) {
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_CONTRACT_MODE_SETTING, mode);
    }

    public static int getContractModeSetting(Context context) {
       return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_CONTRACT_MODE_SETTING, 0);
    }
}
