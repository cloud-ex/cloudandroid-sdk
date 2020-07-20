package com.sl.bymex.adapter

import android.text.TextUtils
import android.widget.Filter
import android.widget.Filterable
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.contract.sdk.ContractSDKAgent.isZhEnv
import com.sl.bymex.R
import com.sl.bymex.data.AreaData
import com.sl.bymex.ui.activity.AreaActivity

class AreaAdapter(var dataList: ArrayList<AreaData>, var activity: AreaActivity) :
    BaseQuickAdapter<AreaData, BaseViewHolder>(
        R.layout.item_list_counrty_code
    ) , Filterable {
    override fun convert(helper: BaseViewHolder, item: AreaData) {
        item?.let {
            val isZhEnv = isZhEnv
            if(isZhEnv){
                helper.setText(R.id.tv_area_name, it.cn_name)
            }else{
                helper.setText(R.id.tv_area_name, it.us_name)
            }
            helper?.setText(R.id.tv_area_code, "+"+it.code)
        }
    }
    private var filter: MyFilter? = null
    var listener: FilterListener? = null

    interface FilterListener {
        fun getFilterData(list: java.util.ArrayList<AreaData>) //获取过滤后的数据
    }


    override fun getFilter(): Filter {
        return  filter ?: MyFilter(dataList)
    }

    internal inner class MyFilter(var originalData: java.util.ArrayList<AreaData>) : Filter(){
        /**
         *  该方法返回搜索过滤后的数据
         */
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = Filter.FilterResults()
            /**
             * 没有搜索内容的话就还是给results赋值原始数据的值和大小
             * 执行了搜索的话，根据搜索的规则过滤即可，最后把过滤后的数据的值和大小赋值给results
             */
            if (TextUtils.isEmpty(constraint)) {
                results.values = activity.mOriginList
                results.count = activity.mOriginList.size
            }else {
                // 创建集合保存过滤后的数据
                val filteredList = java.util.ArrayList<AreaData>()
                // 遍历原始数据集合，根据搜索的规则过滤数据
                for (s in originalData) {
                    // 这里就是过滤规则的具体实现【规则有很多，大家可以自己决定怎么实现】
                    if (s.cn_name.toLowerCase().contains(constraint.toString().trim().toLowerCase()) || s.us_name.toLowerCase().contains(constraint.toString().trim().toLowerCase()) || s.code.contains(constraint.toString().trim().toLowerCase())) {
                        // 规则匹配的话就往集合中添加该数据
                        filteredList.add(s)
                    }
                }
                results.values = filteredList
                results.count = filteredList.size
            }

            // 返回FilterResults对象
            return results
        }

        /**
         * 该方法用来刷新用户界面，根据过滤后的数据重新展示列表
         */
        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            // 获取过滤后的数据
            if (results.values != null) {
                dataList = results.values as java.util.ArrayList<AreaData>
            }
            // 如果接口对象不为空，那么调用接口中的方法获取过滤后的数据，具体的实现在new这个接口的时候重写的方法里执行
            listener?.getFilterData(dataList)
            // 刷新数据源显示
            notifyDataSetChanged()
        }

    }
}