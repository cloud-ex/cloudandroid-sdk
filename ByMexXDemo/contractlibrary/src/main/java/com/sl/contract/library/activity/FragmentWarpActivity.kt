package com.sl.contract.library.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.contract.sdk.ContractSDKAgent
import com.contract.sdk.utils.SDKLogUtil
import com.sl.contract.library.R
import com.sl.contract.library.data.FragmentType
import com.sl.contract.library.fragment.detail.ContractIntroduceFragment
import com.sl.contract.library.fragment.detail.FundsRateFragment
import com.sl.contract.library.fragment.detail.InsuranceFundFragment
import com.sl.contract.library.fragment.detail.PositionTaxDetailFragment
import com.sl.ui.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_fragment_warp.*

/**
 * Fragment容器
 */
class FragmentWarpActivity : BaseActivity(){
    override fun setContentView(): Int {
        return R.layout.activity_fragment_warp
    }

    private var fragmentType : FragmentType? = null
    private val fragmentManager by lazy {
         supportFragmentManager
    }
    private val transaction by lazy {
        fragmentManager.beginTransaction()
    }
    override fun loadData() {
        fragmentType = FragmentType.values()[intent.getIntExtra("fragmentType",0)]
    }

    override fun initView() {

        fragmentType?.let {
            when(it){
                FragmentType.CONTRACT_INTRODUCE -> {
                    title_layout.title = getString(R.string.str_contract_info)
                    transaction
                        .add(R.id.fl_layout, ContractIntroduceFragment())
                }
                FragmentType.INSURANCE_FUND -> {
                    title_layout.title = getString(R.string.str_insurance_fund)
                    transaction
                        .add(R.id.fl_layout, InsuranceFundFragment())
                }
                FragmentType.FUNDS_RATE -> {
                    title_layout.title = getString(R.string.str_funds_rate)
                    transaction
                        .add(R.id.fl_layout, FundsRateFragment())
                }
                FragmentType.POSITION_TAX_DETAIL -> {
                    title_layout.title = getString(R.string.str_position_tax_detail)
                    transaction
                        .add(R.id.fl_layout, PositionTaxDetailFragment())
                }
            }
            transaction.commit()
        }
    }


    companion object{
        /**
         * @param activity 其它参数通过activity intent透传，Fragment中直接获取即可
         */
        fun show(activity: Activity,fragmentType: FragmentType,contractId:Int,pid:Long = 0){
            val intent = Intent(activity,FragmentWarpActivity::class.java)
            intent.putExtra("contractId",contractId)
            intent.putExtra("fragmentType",fragmentType.ordinal)
            intent.putExtra("pid",pid)
            activity.startActivity(intent)
        }
    }
}