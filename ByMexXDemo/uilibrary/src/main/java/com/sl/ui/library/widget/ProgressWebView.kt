package com.sl.ui.library.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import com.sl.ui.library.R
import com.sl.ui.library.ui.activity.HtmlActivity
import com.sl.ui.library.utils.ColorUtils
import com.sl.ui.library.utils.CommonUtils
import com.sl.ui.library.utils.ToastUtil
import kotlinx.android.synthetic.main.view_web_progress.view.*


import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.FileOutputStream
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.Date


/**
 * Created by zj on 2017/10/18.
 */

class ProgressWebView @JvmOverloads constructor(private val mContext: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : LinearLayout(mContext, attrs, defStyle) {

    var url: String? = null

    init {
        initView(mContext)
    }

    private fun initView(context: Context) {
        val view = View.inflate(context, R.layout.view_web_progress, this)
    }

    fun loadUrl(url: String?, method: String, body: String) {
        var url = url
        if (url == null) {
            url = "http://www.baidu.com"
        }
        initWebview(url, method, body)
    }


    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled", "AddJavascriptInterface")
    private fun initWebview(url: String, method: String, body: String) {

        web_view!!.addJavascriptInterface(this, "bridge")
        val webSettings = web_view!!.settings

        webSettings.javaScriptEnabled = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        // 设置可以访问文件
        //webSettings.setAllowFileAccess(true);
        // 设置可以支持缩放
        webSettings.setSupportZoom(true)
        // 设置默认缩放方式尺寸是far
        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webSettings.defaultFontSize = 16
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        // 设置出现缩放工具
        webSettings.builtInZoomControls = true
        webSettings.domStorageEnabled = true

        //自适应屏幕
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        if (TextUtils.equals(method, "POST")) {
            try {
                web_view!!.postUrl(url, body.toByteArray(charset("UTF8")))
            } catch (ignored: UnsupportedEncodingException) {
            }

        } else {
            web_view!!.loadUrl(url)
        }

        // 设置WebViewClient
        web_view!!.webViewClient = object : WebViewClient() {
            // url拦截
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // 使用自己的WebView组件来响应Url加载事件，而不是使用默认浏览器器加载页面
                view.loadUrl(url)
                // 相应完成返回true
                return true
                // return super.shouldOverrideUrlLoading(view, url);
            }

            // 页面开始加载
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                progress_bar!!.visibility = View.VISIBLE
                super.onPageStarted(view, url, favicon)
            }

            // 页面加载完成
            override fun onPageFinished(view: WebView, url: String) {
                progress_bar!!.visibility = View.GONE
                super.onPageFinished(view, url)
            }

            // WebView加载的所有资源url
            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
            }

            override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                //				view.loadData(errorHtml, "text/html; charset=UTF-8", null);
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {

            }
        }

        // 设置WebChromeClient
        web_view!!.webChromeClient = object : WebChromeClient() {
            override// 处理javascript中的alert
            fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return super.onJsAlert(view, url, message, result)
            }

            override// 处理javascript中的confirm
            fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
                return super.onJsConfirm(view, url, message, result)
            }

            override// 处理javascript中的prompt
            fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
                return super.onJsPrompt(view, url, message, defaultValue, result)
            }

            // 设置网页加载的进度条
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                progress_bar!!.progress = newProgress
                super.onProgressChanged(view, newProgress)
            }

            // 设置程序的Title
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }
        }

        web_view!!.setOnKeyListener(OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK && web_view!!.canGoBack()) { // 表示按返回键

                    web_view!!.goBack() // 后退

                    // webview.goForward();//前进
                    return@OnKeyListener true // 已处理
                }
            }
            false
        })
    }


    @JavascriptInterface
    fun navigate(`object`: String) {
        Log.d("WebView", "navigate() called!\n"
                + `object` + "\n")
        try {
            val jsonObject = JSONObject(`object`)

            val url = jsonObject.optString("url")
            val method = jsonObject.optString("method")
            val body = jsonObject.optString("body")
            val title = jsonObject.optString("title")
            val rightText = jsonObject.optString("rightText")
            val rightLink = jsonObject.optString("rightLink")

            val intent = Intent(mContext, HtmlActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("title", title)
            intent.putExtra("body", body)
            intent.putExtra("method", method)
            intent.putExtra("rightText", rightText)
            intent.putExtra("rightLink", rightLink)
            mContext.startActivity(intent)

        } catch (ignored: JSONException) {

        }

    }

    @JavascriptInterface
    fun shareMessage(`object`: String) {
        Log.d("WebView", "share() called!\n"
                + `object` + "\n")
        try {
            val jsonObject = JSONObject(`object`)

            val body = jsonObject.optString("body")
            val imageUrl = jsonObject.optString("imageUrl")
            val title = jsonObject.optString("title")
            val link = jsonObject.optString("shareLink")

//            val shareDialog = ShareDialog(mContext)
//            shareDialog.setType(2)
//            shareDialog.setMessage(imageUrl, link, title, body)
//            shareDialog.show()

        } catch (ignored: JSONException) {

        }

    }

    @JavascriptInterface
    fun SaveImage(`object`: String) {
        Log.d("WebView", "SaveImage() called!\n"
                + `object` + "\n")
        try {
            val jsonObject = JSONObject(`object`)
            val imageUrl = jsonObject.optString("imageUrl")
            val succeedCallback = jsonObject.optString("succeedCallback")
            val failCallBack = jsonObject.optString("failCallBack")

            doSavePhoto(imageUrl, succeedCallback, failCallBack)

        } catch (ignored: JSONException) {

        }

    }


//    @JavascriptInterface
//    fun Copy2Clipboard(data: String) {
//        Log.d("WebView", "Copy2Clipboard() called!\n"
//                + data + "\n")
//
//        try {
//            val jsonObject = JSONObject(data)
//            val cyInfo = jsonObject.optString("cyInfo")
//
//            var cm = mContext.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clipData = ClipData.newPlainText("Label", cyInfo)
//            if (cm != null) {
//                cm.primaryClip = clipData
//            }
//            ToastUtil.shortToast(mContext, context.getString(R.string.str_copy_succeed))
//
//        } catch (ignored: JSONException) {
//
//        }
//
//    }

    @JavascriptInterface
    fun toast(data: String) {
        Log.d("WebView", "toast() called!\n"
                + data + "\n")

        try {
            val jsonObject = JSONObject(data)
            val cyInfo = jsonObject.optString("content")
            ToastUtil.shortToast(mContext, cyInfo)

        } catch (ignored: JSONException) {

        }

    }

    @JavascriptInterface
    fun Version(): String {
        Log.d("WebView", "Version() called!\n")
        return CommonUtils.getVersionName(mContext)
    }


    fun canBack(): Boolean {
        if (web_view!!.canGoBack()) {
            web_view!!.goBack()
            return false
        }
        return true
    }

    private fun doSavePhoto(imgUrl: String, succeedCallback: String, failCallback: String) {
        if (TextUtils.isEmpty(imgUrl)) {
            return
        }

        val bitmap = arrayOf<Bitmap>()
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                val base64 = imgUrl.substring(imgUrl.lastIndexOf(","))
                val bb = ColorUtils.base64ToBitmap(base64)

                if (bb != null) {
                    bitmap[0] = bb
                }

                return null
            }

            override fun onPostExecute(aVoid: Void) {
                if (bitmap[0] != null) {
                    SaveImageTask().execute(bitmap[0])

                    val jsInvoke = String.format("javascript:%s()", succeedCallback)
                    web_view!!.loadUrl(jsInvoke)
                } else {

                    val jsInvoke = String.format("javascript:%s()", failCallback)
                    web_view!!.loadUrl(jsInvoke)
                }
            }
        }.execute()
    }

    private inner class SaveImageTask : AsyncTask<Bitmap, Void, String>() {
        override fun doInBackground(vararg params: Bitmap): String {
            var result = mContext.getString(R.string.str_save_failed)

            try {
                val sdcard = Environment.getExternalStorageDirectory().toString()
                val file = File("$sdcard/ByMex/Photo")
                if (!file.exists()) {
                    file.mkdirs()
                }

                val fmt = SimpleDateFormat("yyyyMMddHHmmss")
                val fileName = fmt.format(Date()) + ".jpg"
                val imageFile = File(file.absolutePath, fileName)
                var outStream: FileOutputStream? = null
                outStream = FileOutputStream(imageFile)
                val image = params[0]
                image.compress(Bitmap.CompressFormat.JPEG, 100, outStream)

                MediaStore.Images.Media.insertImage(mContext.contentResolver, imageFile.absolutePath, fileName, null)
                //保存图片后发送广播通知更新数据库
                val uri = Uri.fromFile(imageFile)
                mContext.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

                outStream.flush()
                outStream.close()
                result = mContext.getString(R.string.str_save_succeed) + file.absolutePath
            } catch (e: Exception) {
                Log.d("WebView", "SaveImageTask $e")
            }

            return result
        }

        override fun onPostExecute(result: String) {
            Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show()
        }
    }

}
