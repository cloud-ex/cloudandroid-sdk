package com.sl.bymex.ui.activity.asset

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.hjq.http.listener.HttpCallback
import com.sl.bymex.R
import com.sl.bymex.api.DepositAddressApi
import com.sl.bymex.app.BaseEasyActivity
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.SpotCoin
import com.sl.bymex.service.PublicInfoHelper
import com.sl.bymex.service.net.NetHelper
import com.sl.bymex.utils.AppUtils
import com.sl.bymex.utils.QRCodeUtil
import com.sl.ui.library.data.PermissionResult
import com.sl.ui.library.service.LivePermissions
import com.sl.ui.library.utils.DialogUtils
import com.sl.ui.library.utils.TopToastUtils
import kotlinx.android.synthetic.main.activity_deposit.*
import kotlinx.android.synthetic.main.activity_deposit.item_coin_layout
import kotlinx.android.synthetic.main.activity_deposit.title_layout
import kotlinx.android.synthetic.main.activity_deposit.tv_tab_erc20
import kotlinx.android.synthetic.main.activity_deposit.tv_tab_omin

/**
 * 充值
 */
class DepositActivity:BaseEasyActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_deposit
    }

    var  spotCoinList = ArrayList<SpotCoin>()
    var  selectSpotCoin : SpotCoin? = null

    private var mClipboardManager: ClipboardManager? = null
    private var mClipData: ClipData? = null
    private var qrCode = ""

    override fun loadData() {
        spotCoinList.addAll(PublicInfoHelper.getSpotCoin())
        if(spotCoinList.isNotEmpty()){
            selectSpotCoin = spotCoinList[0]

            loadDepositAddress()
        }
    }

    private fun loadDepositAddress() {
        val addressApi = DepositAddressApi()
        addressApi.coin = selectSpotCoin!!.name
        showLoadingDialog()
        NetHelper.doHttpGet(this,addressApi,object:HttpCallback<HttpData<String>>(this){

            override fun onSucceed(result: HttpData<String>) {
                hideLoadingDialog()
                //充值地址
               val depositAddress = result.getStringDataByKey("deposit_address")

                if(!TextUtils.isEmpty(depositAddress)){
                    qrCode = depositAddress
                    val bitmap: Bitmap? = QRCodeUtil.createQRCodeBitmap(depositAddress, 480, 480)
                    iv_qr_code.setImageBitmap(bitmap);
                }
                tv_deposit_address.text = depositAddress
                ll_memo_layout.visibility = View.GONE

                    if(TextUtils.equals(selectSpotCoin!!.coin_group,"EOS")){
                    val account = result.getStringDataByKey("account")
                    if(!TextUtils.isEmpty(account)){
                        tv_deposit_address.text = account
                        val bitmap: Bitmap? = QRCodeUtil.createQRCodeBitmap(account, 480, 480)
                        iv_qr_code.setImageBitmap(bitmap);
                        qrCode = account
                    }
                        //处理Memo
                    val memo =  result.getStringDataByKey("memo")
                        if(!TextUtils.isEmpty(memo)){
                            ll_memo_layout.visibility = View.VISIBLE
                            tv_memo_address.text = memo
                        }

                }

                updateUiByCoin()

            }

            override fun onFail(e: Exception?) {
                //hideLoadingDialog()
                TopToastUtils.showTopFailToast(this@DepositActivity,e?.message?:getString(R.string.str_net_fail))
                tv_deposit_address.text = "--"
                iv_qr_code.setImageResource(R.color.main_yellow)
                tv_memo_address.text = "--"
                qrCode = ""
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun initView() {
        mClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        initListener()
        updateUiByCoin()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initListener() {
        tv_tab_erc20.setOnClickListener {
            tv_tab_erc20.isSelected = true
            tv_tab_omin.isSelected = false
        }
        tv_tab_omin.setOnClickListener {
            tv_tab_erc20.isSelected = false
            tv_tab_omin.isSelected = true
        }
        /**
         * 充提记录
         */
        title_layout.setRightOnClickListener(View.OnClickListener {
            selectSpotCoin?.let {
                DwRecordActivity.show(this@DepositActivity,0,it.name)
            }
        })
        /**
         * 选择币种
         */
        item_coin_layout.setItemOnClickListener(View.OnClickListener {
            for (index in spotCoinList.indices){
                val item = spotCoinList[index]
                if(selectSpotCoin!=null){
                    item.selected = TextUtils.equals(item.name,selectSpotCoin!!.name)
                }
            }
            SelectCoinActivity.show(this@DepositActivity,spotCoinList,getString(R.string.str_select_coin),1001)
        })
        /**
         * 保存二维码
         */
        tv_save_qr_code.setOnClickListener {
            //检查权限
            LivePermissions(this@DepositActivity).request(Manifest.permission.WRITE_EXTERNAL_STORAGE).observe(this@DepositActivity,
                Observer {
                    when(it){
                        is PermissionResult.Grant ->{
                            DialogUtils.showCenterDialog(this@DepositActivity,getString(R.string.str_tips),getString(
                                R.string.str_save_photo_tips),getString(R.string.common_text_btnCancel),
                                getString(R.string.str_confirm),object:DialogUtils.DialogBottomListener{
                                    override fun clickTab(tabType: Int) {
                                        if(tabType == 1){
                                            if(TextUtils.isEmpty(qrCode)){
                                                qrCode = "unknown"
                                            }
                                            iv_qr_code.isDrawingCacheEnabled = true
                                            iv_qr_code.buildDrawingCache()
                                            val bitmap  = Bitmap.createBitmap(iv_qr_code.drawingCache)
                                            if (bitmap != null) {
                                                val saveImageToGallery = QRCodeUtil.saveImageToGallery(this@DepositActivity, bitmap)
                                                if (saveImageToGallery) {
                                                    TopToastUtils.showTopSuccessToast(this@DepositActivity,getString(R.string.str_save_succeed))
                                                }else{
                                                    TopToastUtils.showTopFailToast(this@DepositActivity,getString(R.string.str_save_failed))
                                                }
                                            }else{
                                                TopToastUtils.showTopFailToast(this@DepositActivity,getString(R.string.str_save_failed))
                                            }
                                        }
                                    }

                                })
                        }
                        else ->{
                            //PermissionResult.Rationale 被拒绝的权限
                            //PermissionResult.Deny  权限拒绝，且勾选了不再询问
//                            it.permissions.forEach {s->
//                                println("Rationale:${s}")//被拒绝的权限
//                            }
                            TopToastUtils.showTopFailToast(this@DepositActivity,getString(R.string.str_permission_deny))
                        }
                    }
                })
        }
        /**
         * 区块链游览器
         */
        tv_block_browser.setOnClickListener {
            var depositAddress = tv_deposit_address.text.toString()
            if(TextUtils.isEmpty(depositAddress)){
                return@setOnClickListener
            }

            selectSpotCoin?.let {
                val coinScan =PublicInfoHelper.getCoinScan(it.coin_group)
                if(coinScan!=null){
                    AppUtils.safeOpenUrl(this@DepositActivity,coinScan.address+depositAddress)
                }
            }
        }
        /**
         * 充值地址复制
         */
        iv_deposit_address_copy.setOnClickListener {
            mClipData = ClipData.newPlainText("Address", tv_deposit_address.text)
            mClipboardManager?.setPrimaryClip(mClipData!!)
            TopToastUtils.showTopSuccessToast(this@DepositActivity,getString(R.string.str_copy_success))
        }
        /**
         * memo地址复制
         */
        iv_memo_address_copy.setOnClickListener {
            mClipData = ClipData.newPlainText("Address", tv_memo_address.text)
            mClipboardManager?.setPrimaryClip(mClipData!!)
            TopToastUtils.showTopSuccessToast(this@DepositActivity,getString(R.string.str_copy_success))
        }
    }

    private fun updateUiByCoin() {
       selectSpotCoin?.let {
           item_coin_layout.itemLeftText = it.name
           item_coin_layout.setLeftIcon(it.small?:"",R.drawable.icon_coin_default)
           tv_deposit_warn.text = String.format(getString(R.string.str_deposit_warn),it.name)


           tv_tab_erc20.isSelected = true
           tv_tab_omin.isSelected = false

       }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((resultCode == Activity.RESULT_OK) and (requestCode == 1001)){
            var tabInfo = data?.getParcelableExtra(SelectCoinActivity.sSelectResponseKey) as SpotCoin
            if(tabInfo != null){
                selectSpotCoin = tabInfo
                loadDepositAddress()
            }
        }
    }



    companion object{
        fun show(activity: Activity){
            val intent = Intent(activity,DepositActivity::class.java)
            activity.startActivity(intent)
        }
    }
}