package com.sl.bymex.ui.fragment

import android.view.View
import androidx.lifecycle.Observer
import com.sl.bymex.R
import com.sl.bymex.adapter.ImageBannerAdapter
import com.sl.bymex.data.BannerInfo
import com.sl.bymex.data.User
import com.sl.bymex.service.LoginHelper
import com.sl.bymex.service.UserHelper
import com.sl.bymex.ui.activity.asset.AssetTransferActivity
import com.sl.bymex.ui.activity.asset.DepositActivity
import com.sl.bymex.ui.activity.user.LoginActivity
import com.sl.bymex.utils.AppUtils
import com.sl.bymex.utils.LogUtil
import com.sl.bymex.widget.HomeSideDialog
import com.sl.contract.library.fragment.ContractMarketFragment
import com.sl.contract.library.fragment.ContractRankFragment
import com.sl.ui.library.base.BaseFragment
import com.sl.ui.library.service.event.MessageEvent
import com.youth.banner.indicator.CircleIndicator
import kotlinx.android.synthetic.main.fragment_tab_home.*
import java.util.ArrayList

/**
 * 首页
 */
class TabHomeFragment : BaseFragment(), View.OnClickListener {
    override fun setContentView(): Int {
        return R.layout.fragment_tab_home
    }

    var homeSideDialog: HomeSideDialog? = null
    var bannerAdapter: ImageBannerAdapter? = null
    var bannerList = ArrayList<BannerInfo>()

    /**
     * 合约行情
     */
    var contractMarketFragment : ContractMarketFragment?=null

    /**
     * 日盈利排行榜
     */
    var contractRankFragment : ContractRankFragment?=null

    override fun initView() {
        iv_account.setOnClickListener(this)
        tv_login.setOnClickListener(this)
        ll_deposit_layout.setOnClickListener(this)
        ll_transfer_layout.setOnClickListener(this)
        ll_help_layout.setOnClickListener(this)
        ll_guide_layout.setOnClickListener(this)
        initBanner()

        contractMarketFragment = childFragmentManager.findFragmentById(R.id.fragment_market) as ContractMarketFragment?
        contractRankFragment = childFragmentManager.findFragmentById(R.id.fragment_rank) as ContractRankFragment?

        updateLoginUI()
    }

    private fun initBanner() {
//        bannerList.add(BannerInfo("https://img.zcool.cn/community/0148fc5e27a173a8012165184aad81.jpg"))
//        bannerList.add(BannerInfo("https://img.zcool.cn/community/011ad05e27a173a801216518a5c505.jpg"))
//        bannerList.add(BannerInfo("https://img.zcool.cn/community/013c7d5e27a174a80121651816e521.jpg"))
//        bannerList.add(BannerInfo("https://img.zcool.cn/community/01b8ac5e27a173a80120a895be4d85.jpg"))
        bannerAdapter = ImageBannerAdapter(bannerList)
        banner_layout.adapter = bannerAdapter
        banner_layout.indicator = CircleIndicator(activity)
        banner_layout.setBannerRound2(5.0f)
    }

    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when(event.msg_type){
            MessageEvent.nick_name_change_notify ->{
                //昵称变更
                updateLoginUI()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.iv_account -> {//点击头像
                if (homeSideDialog == null) {
                    homeSideDialog = HomeSideDialog()
                }
                homeSideDialog?.showDialog(childFragmentManager, "TabHomeFragment")
            }
            R.id.tv_login ->{//登录注册
                LoginHelper.openLogin(activity!!)
            }
            R.id.ll_deposit_layout -> {//充值
                mActivity?.let {
                    if (UserHelper.isLogin()) {
                        DepositActivity.show(it)
                    } else {
                        LoginHelper.openLogin(it)
                    }
                }
            }
            R.id.ll_transfer_layout -> {//资金划转
                if (UserHelper.isLogin()) {
                    AssetTransferActivity.show(activity!!)
                }else{
                    LoginHelper.openLogin(activity!!)
                }
            }
            R.id.ll_help_layout -> {//帮助中心

            }
            R.id.ll_guide_layout -> {//新手指导

            }
        }
    }

    /**
     * 初始化合约行情
     */
    fun initContractMarketData(){
        contractMarketFragment?.loadData()

        contractRankFragment?.loadData()
    }


    fun updateLoginUI(){
        if(UserHelper.isLogin()){
            tv_login.visibility = View.GONE
            tv_account.visibility = View.VISIBLE
            tv_account.text = String.format(getString(R.string.str_say_hi),UserHelper.user.showName)
        }else{
            tv_login.visibility = View.VISIBLE
            tv_account.visibility = View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        banner_layout.start();
    }

    override fun onStop() {
        super.onStop()
        banner_layout.stop()
    }


}