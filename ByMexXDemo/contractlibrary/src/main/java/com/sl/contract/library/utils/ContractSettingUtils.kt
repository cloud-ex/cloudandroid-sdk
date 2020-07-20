package com.sl.contract.library.utils

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.contract.sdk.data.ContractPosition
import com.contract.sdk.data.ContractWsKlineType
import com.sl.ui.library.service.event.EventBusUtil
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.PreferenceManager

object ContractSettingUtils {
    private const val kline_cur_time_key = "kline_cur_time_key"
    private const val leverage_type_key = "leverage_type_key"

    val KLineTimeLiveData = MutableLiveData<ContractWsKlineType>()

    /**
     * 设置当前K线时间
     */
    fun setKlineCurTime(context: Context,curTime: ContractWsKlineType) {
        PreferenceManager.getInstance(context).putSharedInt(kline_cur_time_key,curTime.ordinal)
    }

    /**
     * 获取当前K线时间
     */
    fun getKlineCurTime(context: Context):ContractWsKlineType{
        return ContractWsKlineType.values()[PreferenceManager.getInstance(context).getSharedInt(kline_cur_time_key,ContractWsKlineType.WEBSOCKET_BIN5M.ordinal)]
    }

    /**
     * 设置杠杆
     * @param lever 杠杆
     * @param position_type 开仓方式,1:逐仓,2:全仓
     */
    fun setLeverage(context: Context,contractId:Int,lever:Int,position_type:Int,notify:Boolean = true){
        PreferenceManager.getInstance(context).putSharedInt(PreferenceManager.PREF_LEVERAGE+contractId,lever)
        PreferenceManager.getInstance(context).putSharedInt(leverage_type_key+contractId,position_type)

        if (notify){
            EventBusUtil.post(
                MessageEvent(
                    MessageEvent.contract_select_leverage_event,
                    lever
                )
            )
        }
    }

    /**
     * 得到设置杠杆
     */
    fun getLeverage(context: Context,contractId:Int) : Int{
        return PreferenceManager.getInstance(context).getSharedInt(PreferenceManager.PREF_LEVERAGE+contractId,0)
    }

    /**
     * 得到杠杆类型
     */
    fun getLeverageType(context: Context,contractId:Int) : Int{
        return PreferenceManager.getInstance(context).getSharedInt(leverage_type_key+contractId,1)
    }
}