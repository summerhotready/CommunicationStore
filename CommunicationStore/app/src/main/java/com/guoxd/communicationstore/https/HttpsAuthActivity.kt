package com.guoxd.communicationstore.https

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.gson.Gson
import com.guoxd.communicationstore.R
import com.guoxd.communicationstore.basepackage.utils.ToastUtils
import okhttp3.FormBody
import okhttp3.Request
import java.io.IOException

/**
 * Created by guoxd on 2018/4/9.
 * FragmentPagerAdapter+ViewPager实现Tab，与Fragment实现Tab方式相比前者简化了联动和切换
 *
 */
open class HttpsAuthActivity:AppCompatActivity(){
    val TAG="HttpsAuthActivity"

    var mModle: LoginModle? = null

    internal var APP_ID = ""
    internal var SECRET = ""
    private val GET_URL_1 = ""
    internal var GAR_URL_2 = ""
    private val GET_URL_2 = "你服务器的接口地址"
    private val POS_URL = ""

    var mViewPager:ViewPager?= null;
    var mTabLayout:TabLayout?=null

    var mAdapter:FragmentPagerAdapter ?= null
    var mFragemnts:List<Fragment>?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_https)


        mTabLayout = findViewById(R.id.tl_tab) as TabLayout
        mViewPager = findViewById(R.id.id_viewpager) as ViewPager
        initViewPager();
        mTabLayout?.post(Runnable {
            ToastUtils.getIntence().showToast(this@HttpsAuthActivity,"mTabLayout"+mTabLayout?.height)
        })

        mTabLayout?.setOnClickListener(View.OnClickListener {
            postTest()
        })
    }
    fun initViewPager(){
        /*mViewPager?.adapter = object :FragmentPagerAdapter(supportFragmentManager){

            override fun getItem(position: Int): Int {
                return 1;
            }

            override fun getCount(): Int {
                return mFragemnts?.size?:0
            }

        }*/
    }

    private fun getTest(type: Int) {//{"totalCount":0,"pageNo":0,"pageSize":10,"devices":[]}
        if (mModle == null)
            return
        try {
            val client = HttpClientSslHelper.getSslOkHttpClient(this@HttpsAuthActivity)
            val url = String.format(GET_URL_1, APP_ID)

            val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + mModle?.getAccessToken())
                    .addHeader("app_key", APP_ID)
                    .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val value = response.body().string()
                val msg = mHandler.obtainMessage()
                msg.what = type
                msg.data.putString("data", value)
                Log.d("MainActivity", value)
            } else {
                throw IOException("Unexpected code " + response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun postTest() {
        try {
            //            String json = "Post的数据";
            //            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            val client = HttpClientSslHelper.getSslOkHttpClient(this@HttpsAuthActivity)
            //            RequestBody body = RequestBody.create(JSON, json);
            val body = FormBody.Builder()
                    .add("appId", APP_ID)
                    .add("secret", SECRET)
                    .build()

            val request = Request.Builder()
                    .url(POS_URL)
                    .post(body)
                    .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val value = response.body().string()
                Log.d("MainActivity", value)
                try {
                    val msg = mHandler.obtainMessage()
                    msg.what = 1
                    msg.data = Bundle()
                    msg.data.putString("data", value)
                    mHandler.sendMessage(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                throw IOException("Unexpected code " + response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    internal var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            val data: String
            when (msg.what) {
                0 -> {
                    data = msg.data.getString("data")
                    mModle = Gson().fromJson<LoginModle>(data, LoginModle::class.java!!)
                    ToastUtils.getIntence().showToast(this@HttpsAuthActivity,"success"+mModle?.accessToken)
                }
                1 -> {
                    data = msg.data.getString("data")
                    mModle = Gson().fromJson<LoginModle>(data, LoginModle::class.java!!)
                    ToastUtils.getIntence().showToast(this@HttpsAuthActivity,"success"+mModle?.accessToken)
                }
            }
        }
    }



    class PostFragment :Fragment(){

    }
    class GetFragment :Fragment(){

    }
    class SetFragment :Fragment(){

    }

}