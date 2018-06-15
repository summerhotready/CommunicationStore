package com.guoxd.communicationstore.https

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.gson.Gson
import com.guoxd.communicationstore.R
import com.guoxd.communicationstore.basepackage.utils.ToastUtils
import com.guoxd.communicationstore.https.modle.DeviceInfo
import com.guoxd.communicationstore.https.modle.DeviceModle
import com.guoxd.communicationstore.https.modle.Token
import okhttp3.*
import java.io.IOException

/**
 * Created by guoxd on 2018/4/9.
 * FragmentPagerAdapter+ViewPager实现Tab，与Fragment实现Tab方式相比前者简化了联动和切换
 *
 */
open class HttpsAuthActivity:AppCompatActivity() {
    val TAG = "HttpsAuthActivity"


    internal var APP_ID = "4BO5QYXybEJ6xGrUFtgV4ck0AxYa"
    internal var SECRET = "04wxTDxVhmqc1HOeUCaRgQSAHPwa"

    final internal var FLAG_POST = 0;
    final internal var FLAG_PUT = 1;

    internal var base_http="https://180.101.147.89:8743/iocm/app/"
    final internal var post_token="sec/v1.1.0/login"
    final internal var post_refreshToken="sec/v1.1.0/refreshToken"

    var listStrs = arrayOf("token","refresh","selectDevice","regesiterDeviceVerify","regesiterDeviceSecret","updateDevice","deleteDevice")

    internal var get_select_device_state="reg/v1.1.0/deviceCredentials/79427473-dd30-48c9-aa4f-553fd3b8abe1"
    internal var post_regesiter_device_versify="reg/v1.1.0/deviceCredentials"
    internal var post_regesiter_device_secret="reg/v2.0.0/deviceCredentials"
    internal var put_update_device="dm/v1.4.0/devices/%s"
    internal var del_device="dm/v1.4.0/devices/da86585f-4c8a-466d-a47d-32b752aaea33"

    internal  var post_key=100;
    internal  var get_key=200;

    private val GET_URL_DEVICEINFO = "reg/v1.1.0/devices/76e9fff6-24e7-4142-9c5c-73fcc3f7e22e?appId=%s"


    var listview:ListView ?=null;
    var valueShow:TextView ?=null
    //通信token
    var access_token=""
    //刷新用token
    var refresh_token=""

//    var mViewPager: ViewPager? = null;
//    var mTabLayout: TabLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_https)

        initView();
//        initViewPager();
    }

    fun initView() {
        listview = findViewById(R.id.listview) as ListView
        valueShow = findViewById(R.id.tv_value) as TextView


        var adapter:ArrayAdapter<String> = ArrayAdapter(this,android.R.layout.simple_list_item_activated_1,listStrs)

        listview?.adapter = adapter

        listview?.setOnItemClickListener{
            parent, view, position, id ->
            if(access_token.equals("") && position !=0){
                ShowToast("请先请求token")
                return@setOnItemClickListener;
            }
            //post
            if(position ==0){
                postForm(post_token)
            }
            if (position == 1){
                val jsonStr = "{\"appId\":\""+APP_ID+"\",\"secret\":\""+SECRET+"\",\"refreshToken\":\""+refresh_token+"\"}"
                postJson(FLAG_POST,post_refreshToken,jsonStr)
            }
            if(position == 3){//"deviceId":"c5f09e4b-ec60-4bd3-a1e0-920d31f9bd6f","verifyCode":"emmatest",
                var device: DeviceModle = DeviceModle();
                device.endUserId="15222659164"
                device.verifyCode = "emmatest"
                device.nodeId="emmatest"
                device.deviceInfo = DeviceInfo("Elco","Elco","IBLNBIoTDevice","CoAP","InterBoxLock")
                var jsonStr=Gson().toJson(device)
                postJson(FLAG_POST,post_regesiter_device_versify,jsonStr)
                }
            if(position == 4){// {"deviceId":"3d11bb84-c8a6-4440-93b8-72603f4ee48b","secret":"ddc216c9031271c2d57d","psk":"f908e988dd5bed7204d4b62dff8337c0"}
                var device: DeviceModle = DeviceModle();
                device.endUserId="15222659164"
                device.deviceInfo = DeviceInfo("xxxtest")
                var jsonStr=Gson().toJson(device)
                postJson(FLAG_POST,post_regesiter_device_secret,jsonStr)
            }

            if(position ==5){
                var device: DeviceModle = DeviceModle();
                var jsonStr=Gson().toJson(device)
                postJson(FLAG_POST,post_regesiter_device_secret,jsonStr)
            }

        }
    }

    fun ShowToast(msg:String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

    private fun postUrl(){

    }
    inner class Value{
        var appId:String = ""
        var secret:String=""
    }

    internal var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val key = msg.data.getString("key")
            var data = msg.data.getString("data")
            when (key) {
                post_refreshToken -> {
                    var tokenData:Token = Gson().fromJson(data,Token::class.java)
                    access_token = tokenData.accessToken
                }
                post_regesiter_device_versify->{
                    var device:DeviceModle = Gson().fromJson(data,DeviceModle::class.java)
                    if(device != null && device.deviceId !=null){
                        ShowToast("注册成功")
                    }else{
                        ShowToast("注册失败")
                    }
                    // {"deviceId":"c5f09e4b-ec60-4bd3-a1e0-920d31f9bd6f","verifyCode":"emmatest","timeout":180,"psk":"76a2e7f875eec3be2ffa5b9fd947a271"}
                }
                post_regesiter_device_secret->{
                    var device:DeviceModle = Gson().fromJson(data,DeviceModle::class.java)
                    if(device != null && device.deviceId !=null){
                        ShowToast("注册成功")
                    }else{
                        ShowToast("注册失败")
                    }
                }
                "" -> {
//                    data = msg.data.getString("data")
//                    mModle = Gson().fromJson<LoginModle>(data, LoginModle::class.java!!)
//                    ToastUtils.getIntence().showToast(this@HttpsAuthActivity, "success" + mModle?.accessToken)
                }
            }
        }
    }

    final val JSON = MediaType.parse("application/json; charset=utf-8")
    private fun postJson(flag:Int,urlStr:String,jsonStr:String) {
        try {
            Log.i(TAG,jsonStr)
            var body:RequestBody  = RequestBody.create(JSON,jsonStr );

            val client = HttpClientSslHelper.getSslOkHttpClient(this@HttpsAuthActivity)
            val url = String.format("%s%s",base_http,urlStr)
            Log.i(TAG,url)
            var builder = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + access_token)
                    .addHeader("Content-Type","application/json")
                    .addHeader("app_key",APP_ID)
            if(flag ==0){
                builder.post(body)
            }else if(flag ==1){
                builder.put(body)
            }
            val request:Request =builder.build()
            //异步调用
            val response = client.newCall(request).enqueue(object:Callback{
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.i(TAG,"failure")
                }
                override fun onResponse(call: Call?, response: Response) {
                    val value = response.body().string()
                    Log.i(TAG,value)
                    var msg:Message = Message.obtain()
                    var b:Bundle= Bundle()
                    b.putString("key",urlStr)
                    b.putString("data",value)
                    msg.data = b
                    mHandler.sendMessage(msg)
                }
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    //首次请求
    private fun postForm(key:String) {//{"totalCount":0,"pageNo":0,"pageSize":10,"devices":[]}
        try {
            val requestBody = FormBody.Builder()
                    .add("appId",APP_ID)
                    .add("secret",SECRET)
                    .build()
            val client = HttpClientSslHelper.getSslOkHttpClient(this@HttpsAuthActivity)
            val url = String.format("%s%s",base_http,key)
            Log.i(TAG,url)
            val request:Request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type","application/x-www-form-urlencoded")
                    .post(requestBody)
                    .build()
            //异步调用
            val response = client.newCall(request).enqueue(object:Callback{
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.i(TAG,"failure")
                }
                override fun onResponse(call: Call?, response: Response) {
                    val value = response.body().string()
                    var tokenData:Token = Gson().fromJson(value,Token::class.java)
                    refresh_token = tokenData.refreshToken
                    access_token = tokenData.accessToken
                    Log.i(TAG,value)//{"accessToken":"345ec7cd301cd671a2f070137ddd20","tokenType":"bearer","refreshToken":"a6e4231ad3a6f1a91d7cab6f62c82e2","expiresIn":3600,"scope":"default"}

                }
            })

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    /*private fun postJson(key:String,jsonStr:String) {//{"totalCount":0,"pageNo":0,"pageSize":10,"devices":[]}
        try {
            Log.i(TAG,jsonStr)
            val requestBody = FormBody.Builder()
                    .add("appId",APP_ID)
                    .add("secret",SECRET)
            var body:RequestBody  = RequestBody.create(JSON,jsonStr );

            val client = HttpClientSslHelper.getSslOkHttpClient(this@HttpsAuthActivity)
            val url = String.format("%s%s",base_http,key)
            Log.i(TAG,url)
            val request:Request = Request.Builder()
                    .url(url)

//                    .addHeader("Authorization", "Bearer " + mModle?.getAccessToken())
                    .addHeader("Content-Type","application/json")
                    .post(body)
                    .build()

//val formBody:RequestBody  =FromBody.Builder()
//    .add("platform", "android")
//    .add("name", "bug")
//    .add("subject", "XXXXXXXXXXXXXXX")
//    .build();

            //同步调用
            *//*val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val value = response.body().string()
                Log.i(TAG,value)
            }else{
                Log.i(TAG,"failure")
            }*//*
            //异步调用
            val response = client.newCall(request).enqueue(object:Callback{
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.i(TAG,"failure")
                }
                override fun onResponse(call: Call?, response: Response) {
                    val value = response.body().string()
                    Log.i(TAG,value)
                }
            })



            *//* if (response.isSuccessful) {
                 val value = response.body().string()
                 val msg = mHandler.obtainMessage()
                 msg.what = type
                 msg.data.putString("data", value)
                 Log.d("MainActivity", value)
             } else {
                 throw IOException("Unexpected code " + response)
             }*//*
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }*/



    /*public fun add(fragment: Fragment, id: Int, tag: String) {
        var fragmentManager: FragmentManager = getSupportFragmentManager();
        var fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction();
        //优先检查，fragment是否存在，避免重叠
        var tempFragment: Fragment = fragmentManager.findFragmentByTag(tag);
        *//*if(EmptyUtils.isNotEmpty(tempFragment)){
            fragment = tempFragment;
        }*//*
        if (fragment.isAdded()) {
            addOrShowFragment(fragmentTransaction, fragment, id, tag);
        } else {
            if (currentFragment != null && currentFragment.isAdded()) {
                fragmentTransaction.hide(currentFragment).add(id, fragment, tag).commit();
            } else {
                fragmentTransaction.add(id, fragment, tag).commit();
            }
            currentFragment = fragment;
        }
    }

    var currentFragment: Fragment? = null
    *//**
     * 添加或者显示 fragment
     *
     * @param fragment
     *//*
    private fun addOrShowFragment(transaction: FragmentTransaction, fragment: Fragment, id: Int, tag: String) {
        if (currentFragment == fragment)
            return;
        if (!fragment.isAdded()) { // 如果当前fragment未被添加，则添加到Fragment管理器中
            transaction.hide(currentFragment).add(id, fragment, tag).commit();
        } else {
            transaction.hide(currentFragment).show(fragment).commit();
        }
        currentFragment?.setUserVisibleHint(false);
        currentFragment = fragment;
        currentFragment?.setUserVisibleHint(true);
    }*/
//    add方式Fragment重叠BUG解决方案
//    为fragment设置Tag，通过findFragmentByTag查找是否存在，然后再添加


    class PostFragment :Fragment(){
        var activity :HttpsAuthActivity ?= null
        override fun onAttach(context: Context?) {
            super.onAttach(context)
            activity = context as HttpsAuthActivity
            ToastUtils.getIntence().showLog("PostFragment","onAttach()")
        }
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

            var view:View = inflater.inflate(R.layout.fragment_https_post,container,false)
            var button:Button = view.findViewById(R.id.button)
            button.setOnClickListener{
                postTest()
            }
            return view
        }

        override fun onDetach() {
            super.onDetach()
            activity = null
        }

        private fun postTest() {
            Thread(Runnable {
                /*try {
                    val client = HttpClientSslHelper.getSslOkHttpClient(activity)

                    val body = FormBody.Builder()
                            .add("appId", activity?.APP_ID)
                            .add("secret", activity?.SECRET)
                            .build()
var url = String.format("%s%s",activity?.base_http,activity?.post_token)
                    ToastUtils.getIntence().showLog("TAG",url)
                    val request = Request.Builder()
                            .url(url)
                            .post(body)
                            .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val value = response.body().string()
                        Log.d("MainActivity", value)
                        try {
                            val msg = activity?.mHandler?.obtainMessage()
                            msg?.what = activity?.post_key
                            msg?.data = Bundle()
                            msg?.data?.putString("data", value)
                            activity?.mHandler?.sendMessage(msg)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        throw IOException("Unexpected code " + response)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }*/
                try {
                    //            String json = "Post的数据";
                    //            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                    val client = HttpClientSslHelper.getSslOkHttpClient(activity)
                    //            RequestBody body = RequestBody.create(JSON, json);
                    val body = FormBody.Builder()
                            .add("appId", activity?.APP_ID)
                            .add("secret", activity?.SECRET)
                            .build()

                    val request = Request.Builder()
                            .url(String.format("%s%s",activity?.base_http,activity?.post_token))
                            .post(body)
                            .build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val value = response.body().string()
                        Log.d("MainActivity", value)


                    } else {
                        throw IOException("Unexpected code " + response)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }).start()
        }
    }
    class GetFragment :Fragment(){
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var view:View = inflater.inflate(R.layout.fragment_https_get,container,false)
            return view
        }

    }
    class SetFragment :Fragment(){
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            var view:View = inflater.inflate(R.layout.fragment_https_setting,container,false)
            return view
        }

    }

}