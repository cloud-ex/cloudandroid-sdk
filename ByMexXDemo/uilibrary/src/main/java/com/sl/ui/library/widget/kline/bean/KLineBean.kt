package  com.sl.ui.library.widget.kline.bean


import com.sl.ui.library.widget.kline.bean.IKLine

/**
 * @Author: Bertking
 * @Date：2019/3/13-4:53 PM
 * @Description:
 */
class KLineBean : IKLine {
    override var openPrice: Float = 0f
    override var closePrice: Float = 0f

    /**
     * 其实就是时间
     */
    var id: Long = 0
    override var highPrice: Float = 0f
    override var lowPrice: Float = 0f

    override var price4MA5: Float = 0f

    override var price4MA10: Float = 0f

    override var price4MA20: Float = 0f

    override var price4MA30: Float = 0f

    override var price4MA60: Float = 0f

    override var up: Float = 0f

    override var mb: Float = 0f

    override var dn: Float = 0f

    override var volume: Float = 0f

    override var volume4MA5: Float = 0f

    override var volume4MA10: Float = 0f

    override var K: Float = 0f

    override var D: Float = 0f

    override var J: Float = 0f

    override var DEA: Float = 0f

    override var DIF: Float = 0f

    override var MACD: Float = 0f

    override var RSI: Float = 0f

    override var R: Float = 0f

}