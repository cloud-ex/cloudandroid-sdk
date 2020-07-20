package com.sl.ui.library.ui.activity

import android.app.Activity
import android.content.Intent
import android.view.View
import com.sl.ui.library.R
import com.sl.ui.library.base.BaseActivity
import kotlinx.android.synthetic.main.activity_html.*

class HtmlActivity : BaseActivity() {
    override fun setContentView(): Int {
        return R.layout.activity_html
    }

    var title = ""
    var url = ""
    var rightText = ""
    var rightLink = ""

    override fun loadData() {
        title = intent.getStringExtra("title")?:""
        url = intent.getStringExtra("url")?:""
        rightText = intent.getStringExtra("rightText")?:""
        rightLink = intent.getStringExtra("rightLink")?:""
    }

    override fun initView() {
        title_layout.title = title
        title_layout.rightText = rightText
        title_layout.setRightOnClickListener(View.OnClickListener { show(this@HtmlActivity,rightText,rightLink) })

        wb?.loadUrl(url,"","")
    }

    companion object {
        fun show(
            activity: Activity,
            title: String,
            url: String,
            rightText: String? = "",
            rightLink: String? = ""
        ) {

            val intent = Intent(activity,HtmlActivity::class.java)
            intent.apply {
                putExtra("title",title)
                putExtra("url",url)
                putExtra("rightText",rightText)
                putExtra("rightLink",rightLink)
            }
            activity.startActivity(intent)

        }
    }


}