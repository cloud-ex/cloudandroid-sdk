package com.sl.bymex.service.net

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.text.TextUtils
import androidx.lifecycle.LifecycleOwner
import com.alibaba.fastjson.JSON
import com.contract.sdk.net.ContractApiConstants
import com.hjq.http.EasyLog
import com.hjq.http.config.IRequestHandler
import com.hjq.http.exception.*
import com.sl.bymex.api.PublicApiConstant
import com.sl.bymex.data.HttpData
import com.sl.bymex.data.User
import com.sl.bymex.service.UserHelper
import com.sl.ui.library.R
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RequestHandler(private val mApplication: Context) : IRequestHandler {
    @Throws(Exception::class)
    override fun requestSucceed(
        lifecycle: LifecycleOwner,
        response: Response,
        type: Type
    ): Any {
        if (Response::class.java == type) {
            return response
        }
        if (!response.isSuccessful) {
            // 返回响应异常
            throw ResponseException(mApplication.getString(R.string.http_server_error), response)
        }
        val body = response.body ?: return ""
        if (Bitmap::class.java == type) {
            // 如果这是一个 Bitmap 对象
            return BitmapFactory.decodeStream(body.byteStream())
        }
        val text: String
        text = try {
            body.string()
        } catch (e: IOException) {
            // 返回结果读取异常
            throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
        }

        // 打印这个 Json
        EasyLog.json("url:${response.request.url};原数据:$text")
        val result: Any
        if (String::class.java == type) {
            // 如果这是一个 String 对象
            result = text
        } else if (JSONObject::class.java == type) {
            result = try {
                // 如果这是一个 JSONObject 对象
                JSONObject(text)
            } catch (e: JSONException) {
                throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
            }
        } else if (JSONArray::class.java == type) {
            result = try {
                // 如果这是一个 JSONArray 对象
                JSONArray(text)
            } catch (e: JSONException) {
                throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
            }
        } else {
            result = try {
                JSON.parseObject<Any>(text, type)
            } catch (e: Exception) {
                EasyLog.print(e.toString())
                throw DataException(mApplication.getString(R.string.http_data_explain_error), e)
            }
            if (result is HttpData<*>) {
                val model = result
                return if (TextUtils.equals(model.errno, ContractApiConstants.ERRNO_OK)) {
                    // 代表执行成功
                    //如果是登录，则从header中获取Token
                    if(response.request.url.encodedPath.contains(PublicApiConstant.sLogin)){
                    //    EasyLog.print("header:"+response.headers.toString())
                        if(result.data is User){
                           var user : User = result.data as User
                            user.token = response.header(ContractApiConstants.getHttpHeader(ContractApiConstants.KEY_USER_TOKEN))
                        }
                    }
                    result
                } else if (TextUtils.equals(
                        model.errno,
                        ContractApiConstants.ERRNO_FORBIDDEN
                    )
                ) {
                    // 代表登录失效，需要重新登录
                    throw TokenException(mApplication.getString(R.string.http_account_error))
                } else {
                    // 代表执行失败
                    throw ResultException(model.message, model)
                }
            }
        }
        return result
    }

    override fun requestFail(
        lifecycle: LifecycleOwner,
        e: Exception
    ): Exception {
        var e = e
        // 判断这个异常是不是自己抛的
        if (e is HttpException) {
            if (e is TokenException) {
                // 登录信息失效，跳转到登录页
                UserHelper.exitLogin()
            }
        } else {
            e = if (e is SocketTimeoutException) {
                TimeoutException(
                    mApplication.getString(R.string.http_server_out_time),
                    e
                )
            } else if (e is UnknownHostException) {
                val info =
                    (mApplication.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
                // 判断网络是否连接
                if (info != null && info.isConnected) {
                    // 有连接就是服务器的问题
                    ServerException(
                        mApplication.getString(R.string.http_server_error),
                        e
                    )
                } else {
                    // 没有连接就是网络异常
                    NetworkException(mApplication.getString(R.string.http_network_error), e)
                }
            } else if (e is IOException) {
                //e = new CancelException(context.getString(R.string.http_request_cancel), e);
                CancelException("", e)
            } else {
                HttpException(e.message, e)
            }
        }
        EasyLog.print("requestFail:$e")
        return e
    }

}