package com.sl.bymex.ui.activity.asset

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.fastjson.JSON
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.contract.sdk.ContractSDKAgent
import com.flyco.tablayout.listener.OnTabSelectListener
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.adapter.DwRecordAdapter
import com.sl.bymex.api.QueryDwRecordApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.DwRecord
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.utils.LogUtil
import com.sl.bymex.widget.DwRecordFilterDialog
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.CommonUtils
import com.sl.ui.library.utils.TopToastUtils
import com.sl.ui.library.widget.CustomLoadMoreView
import kotlinx.android.synthetic.main.activity_dw_record.*


/**
 * 充提记录
 */
class DwRecordActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_dw_record
    }

    /**
     * 0 充值 1 提现
     */
    private var tabIndex = 0
    private var tabTextList = arrayOfNulls<String>(2)

    private var offset = 0
    private var limit = 20
    private var coin = ""
    private var adapter: DwRecordAdapter? = null
    private var loadMoreModule: BaseLoadMoreModule? = null
    private var mList = ArrayList<DwRecord>()

    private var filterDialog: DwRecordFilterDialog? = null

    override fun loadData() {
        coin = intent.getStringExtra("coin")
        tabTextList[0] = getString(R.string.str_deposit)
        tabTextList[1] = getString(R.string.str_withdraw)

        loadDataFromNet()
    }


    override fun initView() {
        tab_layout.setTabData(tabTextList)


        adapter = DwRecordAdapter(coin, mList)
        loadMoreModule = adapter?.loadMoreModule
        loadMoreModule?.loadMoreView = CustomLoadMoreView()
        lv_list.layoutManager = LinearLayoutManager(this)
        adapter?.setEmptyView(View.inflate(this, R.layout.view_empty_layout, null))
        lv_list.adapter = adapter


        initListener()
    }

    fun addData() {
        //TODO 假数据
       var depositString = CommonUtils.readJsonFromAsset(this@DwRecordActivity,"spot_deposit.json")
        mList.addAll(JSON.parseArray(depositString,DwRecord::class.java))
        adapter?.notifyDataSetChanged()
    }

    private fun initListener() {
        /**
         * 筛选
         */
        title_layout.setRightOnClickListener(View.OnClickListener {
            filterDialog = DwRecordFilterDialog()
            filterDialog?.showDialog(supportFragmentManager, "filter",coin)
        })
        /**
         *  切换仓位类型
         */
        tab_layout.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(index: Int) {
                if (tabIndex != index) {
                    tabIndex = index
                    offset = 0
                    showLoadingDialog()
                    loadDataFromNet()
                }
            }

            override fun onTabReselect(p0: Int) {
            }

        })

        adapter?.setOnItemClickListener { adapter, view, position ->
            DwRecordDetailActivity.show(this@DwRecordActivity,mList.get(position))
        }
        adapter?.animationEnable = true
        loadMoreModule?.setOnLoadMoreListener {
            LogUtil.d(ContractSDKAgent.sTAG, "加载更多")
            lv_list.postDelayed({
                addData()
            }, 3000)
        }
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when(event.msg_type){
            MessageEvent.filter_coin_type_select_notify ->{
                coin = event.msg_content as String
                LogUtil.d(ContractSDKAgent.sTAG,"选择币种:$coin")
                loadDataFromNet()
            }
        }
    }

    private fun loadDataFromNet() {
        val dwRecordApi = QueryDwRecordApi()
        if (tabIndex == 0) {
            dwRecordApi.type = "deposit"
        } else {
            dwRecordApi.type = "withdraw"
        }
        dwRecordApi.offset = offset
        dwRecordApi.limit = limit
        dwRecordApi.coin = coin

        NetHelper.doHttpGet(
            this@DwRecordActivity,
            dwRecordApi,
            object : HttpCallback<HttpData<String>>(this@DwRecordActivity) {

                override fun onSucceed(result: HttpData<String>) {
                    hideLoadingDialog()
                    val list = result.getBeanList(DwRecord::class.java, "rewards")
                    if (offset == 0) {
                        mList.clear()
                    }
                    if (list.isNotEmpty()) {
                        loadMoreModule?.isEnableLoadMore = true
                        loadMoreModule?.loadMoreComplete()
                    } else {
                        loadMoreModule?.loadMoreEnd()
                    }
                    mList.addAll(list)
                    addData()
                    loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
                }

                override fun onFail(e: Exception?) {
                    super.onFail(e)
                    hideLoadingDialog()
                    loadMoreModule?.loadMoreEnd()
                    loadMoreModule?.isEnableLoadMoreIfNotFullPage = false
                    TopToastUtils.showTopFailToast(
                        this@DwRecordActivity,
                        e?.message ?: getString(R.string.str_net_fail)
                    )
                }
            })
    }

    companion object {
        fun show(activity: Activity, tabIndex: Int, coin: String) {
            val intent = Intent(activity, DwRecordActivity::class.java)
            intent.putExtra("tabIndex", tabIndex)
            intent.putExtra("coin", coin)
            activity.startActivity(intent)
        }
    }
}