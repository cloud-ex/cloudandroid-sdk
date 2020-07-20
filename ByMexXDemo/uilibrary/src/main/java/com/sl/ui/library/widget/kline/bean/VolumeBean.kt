package  com.sl.ui.library.widget.kline.bean

import com.sl.ui.library.widget.kline.bean.Index


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:55 AM
 * @Description:
 */
interface VolumeBean : Index {
    /**
     * 开盘价
     */
    var openPrice: Float
    /**
     * 收盘价
     */
    var closePrice: Float
    /**
     * 成交量
     */
    val volume: Float
    /**
     * 五(月，日，时，分，5分等)均量
     */
    val volume4MA5: Float

    /**
     * 十(月，日，时，分，5分等)均量
     */
    var volume4MA10: Float
}