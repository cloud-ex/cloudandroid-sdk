package com.sl.bymex.ui.activity

import android.app.Activity
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.contract.sdk.ContractSDKAgent
import com.github.promeg.pinyinhelper.Pinyin
import com.hjq.http.EasyLog
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.adapter.AreaAdapter
import com.sl.bymex.api.AreaApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.AreaData
import com.sl.bymex.data.HttpData
import com.sl.bymex.service.UserHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.widget.SectionDecoration
import com.sl.ui.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_area.*
import kotlin.collections.ArrayList

/**
 * 选择国家区域
 */
class AreaActivity : BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_area
    }

    private val mList = ArrayList<AreaData>()
    val mOriginList = ArrayList<AreaData>()
    private var areaAdapter: AreaAdapter? = null

    override fun loadData() {
        loadDataFromNet()
    }


    override fun initView() {
        tv_cancel.setOnClickListener { finish() }
        areaAdapter = AreaAdapter(mList, this@AreaActivity)
        rv_area?.layoutManager = LinearLayoutManager(this)
        rv_area?.adapter = areaAdapter

        area_side_bar?.setOnTouchingLetterChangedListener { s ->
            for (i in 0 until mList.size) {
                //对比字符串，忽略大小写
                if (mList[i].getStickItem().equals(s, true)) {
                    rv_area.scrollToPosition(i)
                    break
                }
            }
        }


        /**
         * 给 RecyclerView添加 ItemDecoration
         */
        rv_area?.addItemDecoration(
            SectionDecoration(
                this,
                object : SectionDecoration.DecorationCallback {
                    override fun getGroupFirstLine(position: Int): String {
                        return mList[position].getStickItem().substring(0, 1).toLowerCase()
                    }

                    override fun getGroupId(position: Int): Long {
                        return Character.toUpperCase(mList[position].getStickItem()[0]).toLong()

                    }

                })
        )

        /**
         * Adapter的点击事件
         */
        areaAdapter?.setOnItemClickListener { adapter, view, position ->
            setResult(Activity.RESULT_OK, intent)
            UserHelper.areaData = adapter.data[position] as AreaData
            //  EventBus.getDefault().post(mList[position])
            finish()
        }


        areaAdapter?.listener = object : AreaAdapter.FilterListener {
            override fun getFilterData(list: java.util.ArrayList<AreaData>) {
                mList.clear()
                mList.addAll(list)
                areaAdapter?.setNewData(mList)
            }

        }

        et_search?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                areaAdapter?.filter?.filter(s)
            }

        })


    }

    private fun loadDataFromNet() {
        NetHelper.doHttpGet(this, AreaApi(),object :HttpCallback<HttpData<String>>(this){
            override fun onSucceed(result: HttpData<String>) {
                val list = result.getBeanList(AreaData::class.java, "codes");
                if (ContractSDKAgent.isZhEnv) {
                    list.sortedBy { Pinyin.toPinyin(it.cn_name, "") }
                }
                mList.addAll(list)
                mOriginList.addAll(list)
                areaAdapter?.setNewData(mList)
                areaAdapter?.notifyDataSetChanged()
                EasyLog.print("baseActivity onSucceed:" + mList.size + ";" + areaAdapter?.data?.size + ContractSDKAgent.isZhEnv)
            }
        })
    }


    companion object {
        val CHOOSE_COUNTRY_CODE = 111

        fun show(activity: Activity, code: String? = "86") {
            val intent = Intent(activity, AreaActivity::class.java)
            intent.putExtra("code", code)
            activity.startActivityForResult(intent, CHOOSE_COUNTRY_CODE)
        }
    }
}