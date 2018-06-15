package com.guoxd.communicationstore.http

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TabHost
import com.guoxd.communicationstore.R
import com.guoxd.communicationstore.http.control.HttpModle
import android.view.MotionEvent
import android.widget.TextView
import com.guoxd.communicationstore.basepackage.utils.ToastUtils
import com.guoxd.communicationstore.databinding.ActivityHttpBinding
import com.guoxd.communicationstore.http.control.HttpModleImp
import com.guoxd.communicationstore.http.control.ModleSendListener


/**
 * Created by guoxd on 2018/4/9.
 * use mvc
 * Activity作为Controller控制层读取View视图层EditTextView的数据，
 * 然后向Model模型发起数据请求，也就是调用WeatherModel对象的方法getWeather（）方法。
 * 当Model模型处理数据结束后，通过接口OnWeatherListener
 * 通知View视图层数据处理完毕，View视图层该更新界面UI了。
 * 然后View视图层调用displayResult（）方法更新UI。
 * 至此，整个MVC框架流程就在Activity中体现出来了。
 */
open class HttpSendActivity:AppCompatActivity(){
    var postAddr = "POST"
    //用于智能聊天
    var getAddr = "http://api.qingyunke.com/api.php?key=free&appid=0&msg=%s"
    val flag1 = "Post"
    val flag2 = "Get"
    var mModle: HttpModle = HttpModleImp();

    var listener :ModleSendListener = object :ModleSendListener{
        override fun onPostCallBack(msg:String) {
            diaplayPostView(msg)
        }
        override fun onGetCallBack(msg:String) {
            ToastUtils.getIntence().showLog("MAin", "getBack" )
            diaplayGetView(msg)
        }
    }

        var tabHost:TabHost ?=null
        var getText:TextView ?=null
        var postText:TextView ?=null
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var mBinding: ActivityHttpBinding =DataBindingUtil.setContentView(this,R.layout.activity_http)
        tabHost = mBinding.tabhost
        tabHost?.setup()//解决TabSpec setContent空指针问题，实例化了tabWidget和tabContent

        var tab1:TabHost.TabSpec ?= tabHost?.newTabSpec("tab1")
                ?.setIndicator(flag1)
                ?.setContent(R.id.content_post)
        tabHost?.addTab(tab1)
        var tab2:TabHost.TabSpec ?= tabHost?.newTabSpec("tab2")
                ?.setIndicator(flag2)
                ?.setContent(R.id.content_get)
        tabHost?.addTab(tab2)

        mBinding.btnPostSend.setOnClickListener{
            //view->control->modle
            mModle.Post(postAddr,"data",listener)
        }
        mBinding.btnGetSend.setOnClickListener{
            if(mBinding.editGet.text.equals("")) {
                return@setOnClickListener
            }
            ToastUtils.getIntence().showLog("Main", "getSend")
            mModle.Get(String.format(getAddr,mBinding.editGet.text),listener)
        }

            getText = mBinding.getView
            postText = mBinding.postView
//        tabHost.setOnTabChangedListener{
//            id:String->
//            when(id){
//                "tab1"->{
////
//                }
//                "tab2"->{
//
//                }
//            }
//        }
    }
    //updat view
    fun diaplayGetView(tag:String){
        getText?.setText(tag)
    }
    fun diaplayPostView(tag:String){
        postText?.setText(tag)
    }

//监听滑动
    var initx=0;
    var currentx=0;
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                initx = event.x.toInt()
            }
            MotionEvent.ACTION_MOVE->{
                currentx = event.x.toInt()
            }
            MotionEvent.ACTION_UP->{
                if((currentx-initx)>50){
                    if(tabHost?.currentTab !=0){
                        tabHost?.currentTab = tabHost?.currentTab?.minus(1)?:0
                    }
                }else if((currentx-initx)<-50){
                    if (tabHost?.getCurrentTab() != tabHost?.getTabContentView()?.getChildCount()) {
                        tabHost?.setCurrentTab(tabHost?.getCurrentTab()?.plus(1)?:0);
                    }
                }
            }
        }
        return true
    }

}