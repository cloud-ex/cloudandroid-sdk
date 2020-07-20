package  com.sl.ui.library.widget.kline.bean

import com.sl.ui.library.widget.kline.bean.vice.IKDJ
import com.sl.ui.library.widget.kline.bean.vice.IMACD
import com.sl.ui.library.widget.kline.bean.vice.IRSI
import com.sl.ui.library.widget.kline.bean.vice.IWR


/**
 * @Author: Bertking
 * @Date：2019/2/25-10:55 AM
 * @Description: K线实体
 */
interface IKLine : CandleBean, VolumeBean, IKDJ, IMACD, IRSI, IWR