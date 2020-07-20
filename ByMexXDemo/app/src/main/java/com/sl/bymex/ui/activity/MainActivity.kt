package com.sl.bymex.ui.activity

import android.Manifest
import android.os.Build
import android.os.Handler
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.contract.sdk.ContractPublicDataAgent
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.ContractUserDataAgent
import com.contract.sdk.impl.ContractPlanOrderListener
import com.contract.sdk.impl.ContractSDKListener
import com.contract.sdk.utils.SDKLogUtil
import com.sl.bymex.R
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.service.PublicInfoHelper
import com.sl.bymex.ui.fragment.*
import com.sl.contract.library.fragment.TabContractFragment
import com.sl.ui.library.data.PermissionResult
import com.sl.ui.library.service.LivePermissions
import com.sl.ui.library.service.event.MessageEvent
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseEasyActivity() {
    private var tabIconList = ArrayList<Int>()
    private var tabTextList = ArrayList<String>()
    private var fragmentList = arrayListOf<Fragment>()

    private val homeFragment = TabHomeFragment()
    private val followOrderFragment = TabFollowOrderFragment()
    private val contractFragment = TabContractFragment()
    private val otcFragment = TabOTCFragment()
    private val assetFragment = TabAssetFragment()
    private var currentIndex = 0
    private var currentFragment = Fragment()

    override fun setContentView(): Int {
        return R.layout.activity_main;
    }

    override fun loadData() {
        initTabData()
        //加载公共信息
        PublicInfoHelper.loadPublicInfoFromNet(this)
        //权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            doPermissionsRequest()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun doPermissionsRequest() {
        val arr = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
        LivePermissions(this@MainActivity).requestArray(arr).observe(this@MainActivity,
            Observer<PermissionResult> { t ->
                if(t == PermissionResult.Grant ){
                   // this@MainActivity.finish()
                }
            })
    }


    override fun initView() {
        initTabView()
        //设置SDKListener监听
        ContractSDKAgent.registerContractSDKListener(object:ContractSDKListener(){
            /**
             * 合约SDK初始化失败,
             * SDK内部 会做初始化加载重试
             */
            override fun sdkInitFail(message: String) {
                ToastUtil.shortToast(this@MainActivity, "合约SDK初始化失败$message")
            }

            /**
             * 合约SDK初始化成功,此时可操作ticker相关数据
             */
            override fun sdkInitSuccess() {
                ToastUtil.shortToast(this@MainActivity, "合约SDK初始化成功")
                homeFragment?.initContractMarketData()
                //订阅所有Ticker
                ContractPublicDataAgent.subscribeAllTickerWebSocket()
            }

        })
        Glide.with(this@MainActivity).load(R.drawable.icon_splash_logo).into(icon_splash_logo)
        //2s后移除启动图
        Handler().postDelayed({
            rl_splash_layout.visibility = View.GONE
            rl_main_layout.removeView(rl_splash_layout)
        },2000)
    }


    private fun initTabData() {
        fragmentList.clear()
        tabTextList.clear()
        tabIconList.clear()

        tabTextList.add(getString(R.string.str_tab_home))
        tabTextList.add(getString(R.string.str_tab_order))
        tabTextList.add(getString(R.string.str_tab_contract))
        tabTextList.add(getString(R.string.str_tab_otc))
        tabTextList.add(getString(R.string.str_tab_asset))

        tabIconList.add(R.drawable.tab_home_icon_bg)
        tabIconList.add(R.drawable.tab_order_icon_bg)
        tabIconList.add(R.drawable.tab_contract_icon_bg)
        tabIconList.add(R.drawable.tab_otc_icon_bg)
        tabIconList.add(R.drawable.tab_asset_icon_bg)

        fragmentList.add(homeFragment)
        fragmentList.add(followOrderFragment)
        fragmentList.add(contractFragment)
        fragmentList.add(otcFragment)
        fragmentList.add(assetFragment)

    }

    private fun initTabView() {
        tab_group.setData(tabIconList, tabTextList, View.OnClickListener { v ->
            currentIndex = v?.tag as Int
            showFragment()
        })
        showFragment()
    }

    private fun showFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = fragmentList[currentIndex]
        if (!fragment.isAdded) {
            transaction.hide(currentFragment).add(R.id.fragment_container, fragment)
        } else {
            transaction.hide(currentFragment).show(fragment)
        }
        currentFragment = fragment
        transaction.commitNowAllowingStateLoss()
        tab_group.showCurTabView(currentIndex)
    }


    override fun onMessageEvent(event: MessageEvent) {
        super.onMessageEvent(event)
        when (event.msg_type) {
            MessageEvent.sl_login_token_change_event -> {
                //登录状态变动
                homeFragment.updateLoginUI()
            }
        }

    }


    private var exitTime = 0L
    override fun onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtil.shortToast(this@MainActivity, getString(R.string.str_press_exit_app))
            exitTime = System.currentTimeMillis()
            return
        }
        super.onBackPressed()
    }




}
