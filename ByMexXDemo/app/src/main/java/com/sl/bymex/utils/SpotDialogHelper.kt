package com.sl.bymex.utils

import android.content.Context
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import com.sl.bymex.R
import com.sl.ui.library.utils.DialogUtils
import com.timmy.tdialog.TDialog

/**
 * 现货对话框
 */
class SpotDialogHelper {
    companion object{
        /**
         * 提现审核中对话框
         */
        fun showWithdrawAuditDialog(
            context: Context, clickListener: DialogUtils.DialogBottomListener?
           ): TDialog {
            return TDialog.Builder((context as AppCompatActivity).supportFragmentManager)
                .setLayoutRes(R.layout.dialog_withdraw_audit_layout)
                .setScreenWidthAspect(context, 0.8f)
                .setGravity(Gravity.CENTER)
                .setDimAmount(0.8f)
                .setCancelableOutside(true)
                .setDialogAnimationRes(R.style.animate_dialog_scale)

                .addOnClickListener( R.id.tv_sure)
                .setOnViewClickListener { viewHolder, view, tDialog ->
                    when (view.id) {
                        R.id.tv_sure -> {
                            tDialog.dismiss()
                            clickListener?.clickTab(1)
                        }
                    }
                }
                .create()
                .show()
        }
    }
}