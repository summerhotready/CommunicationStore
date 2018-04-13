package com.guoxd.communicationstore.http.utils

import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.guoxd.communicationstore.basepackage.constants.Constant
import com.guoxd.communicationstore.http.info.OnHttpListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by guoxd on 2018/4/10.
 */

class HttpUtils private constructor() {

    val intence:HttpUtils get() = httpUtils

    companion object{
        var httpUtils:HttpUtils = HttpUtils()
    }

    var conn:HttpURLConnection ?=null;

    var connectTimeout=60*1000
    var readTimeout = 30*1000

    fun httpPost(addr:String,data:String,listener: OnHttpListener){
        Thread(Runnable {
            try {
                var url: URL = URL(addr)
                conn = url.openConnection() as HttpURLConnection
                conn?.requestMethod = "POST"
                conn?.doInput = true
                conn?.doOutput = true
                conn?.setRequestProperty("Charset", "UTF-8")
                conn?.setRequestProperty("Content-Type", "application/json");
                conn?.connectTimeout = connectTimeout
                conn?.readTimeout = readTimeout

                //获取URLConnection对象对应的输出流
                var printWriter: PrintWriter = PrintWriter(conn?.outputStream)
                // 发送请求参数,post的参数 xx=xx&yy=yy
                printWriter.write(data)
                //清缓存
                printWriter.flush()

                if (conn?.responseCode == 200) {
                    var inputStream = conn?.inputStream
                    var inputStreamReader = InputStreamReader(inputStream)
                    var bufferedReader = BufferedReader(inputStreamReader)

                    var buffer = StringBuffer()
                    var str: String = bufferedReader.readLine().toString()
                    while (str != null) {
                        buffer.append(str)
                        str = bufferedReader.readLine().toString()
                    }

                    var b = Bundle()
                    b.putString("data", buffer.toString())
                   listener.onSuccess(b)
                } else {
                    listener.onTimeOut()
                }
            }catch (e:Exception){
                var b = Bundle()
                b.putString("error", e.message?:"")
                listener.onFailure(b)
            }
        }).start()
    }


    fun httpGet(addr:String,listener: OnHttpListener){
        Thread(Runnable {
            try{
                var url: URL = URL(addr)
                conn = url.openConnection() as HttpURLConnection
                conn?.requestMethod = "GET"
                conn?.setRequestProperty("Charset", "UTF-8")
                conn?.setRequestProperty("Content-Type", "application/json");
                //conn?.setRequestProperty("Authorization", userNameAndPasswd);
                conn?.connectTimeout = connectTimeout
                conn?.readTimeout = readTimeout

                if (conn?.responseCode == 200) {
                    var inputStream = conn?.inputStream
                    var inputStreamReader = InputStreamReader(inputStream)
                    var bufferedReader = BufferedReader(inputStreamReader)

                    var buffer = StringBuffer()
                    var str: String ?=null
                    str=bufferedReader.readLine()
                    while (str != null) {
                        buffer.append(str)
                        str = bufferedReader.readLine()
                    }

                    var b = Bundle()
                    b.putString("data", buffer.toString())
                    listener.onSuccess(b)
                }else{
                    listener.onTimeOut()
                }
            }catch (e:Exception){
                var b = Bundle()
                b.putString("error", e.message?:"")
                listener.onFailure(b)
            }
        }).start()
    }


    fun uploadFile(addr:String, file: File, handler:Handler){
        Thread(Runnable {
            try {
                if(file == null) {
                    return@Runnable
                }
                var url: URL = URL(addr)
                conn = url.openConnection() as HttpURLConnection
                conn?.requestMethod = "POST"
                conn?.doInput = true
                conn?.doOutput = true
                conn?.useCaches = false // 不允许使用缓存
                conn?.setRequestProperty("Charset", "UTF-8")
                conn?.setRequestProperty("Content-Type", "application/json");
                conn?.connectTimeout = connectTimeout
                conn?.readTimeout = readTimeout

                var outourStream:OutputStream ?= conn?.outputStream;
                var dos = DataOutputStream(outourStream)
                //读取文件
                var inputStream:InputStream = FileInputStream(file)
                var bytes=ByteArray(1024)
                var len=inputStream.read(bytes)
                while (len>=0){
                    dos.write(bytes,0,len)
                    len=inputStream.read(bytes)
                }
                inputStream.close()
                dos.flush()

                if (conn?.responseCode == 200) {
                    var inputStream = conn?.inputStream
                    var inputStreamReader = InputStreamReader(inputStream)
                    var bufferedReader = BufferedReader(inputStreamReader)

                    var buffer = StringBuffer()
                    var str: String = bufferedReader.readLine().toString()
                    while (str != null) {
                        buffer.append(str)
                        str = bufferedReader.readLine().toString()
                    }

                    var msg: Message = handler.obtainMessage()
                    var b = Bundle()
                    b.putString("data", buffer.toString())
                    msg.data = b
                    msg.what = Constant.MSG_SAVE_SUCCESS
                    handler.sendMessage(msg)
                } else {
                    var msg: Message = handler.obtainMessage()
                    msg.what = Constant.MSG_SAVE_TIMEOUT
                    handler.sendMessage(msg)
                }
            }catch (e:Exception){
                var msg: Message = handler.obtainMessage()
                var b = Bundle()
                b.putString("error", e.message?:"")
                msg.data = b
                msg.what = Constant.MSG_SAVE_FAILURE
                handler.sendMessage(msg)
            }
        }).start()
    }
}
