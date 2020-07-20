package com.sl.bymex.ui.fragment.asset

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sl.bymex.R
import com.sl.bymex.adapter.SpotAssetAdapter
import com.sl.bymex.data.UserAsset
import com.sl.bymex.service.UserHelper
import com.sl.ui.library.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_spot_asset.*

/**
 * 现货资产
 */
class SpotAssetFragment : BaseFragment() {
    override fun setContentView(): Int {
        return R.layout.fragment_spot_asset
    }

    var adapter: SpotAssetAdapter? = null
    val mList = ArrayList<UserAsset>()

    override fun initView() {
        adapter = SpotAssetAdapter(mActivity!!,mList)
        lv_contract.layoutManager = LinearLayoutManager(context)
        lv_contract.adapter = adapter
        adapter?.setEmptyView( View.inflate(activity, R.layout.view_empty_layout, null))

        updateSpotAccount()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden){
            updateSpotAccount()
        }
    }

    fun updateSpotAccount(){
        mList.clear()
        mList.addAll(UserHelper.user.user_assets)
        adapter?.notifyDataSetChanged()
    }

    fun updateAssetEye(isShowAssetEye : Boolean){
        adapter?.notifyAssetDataRefresh(isShowAssetEye)
    }
}