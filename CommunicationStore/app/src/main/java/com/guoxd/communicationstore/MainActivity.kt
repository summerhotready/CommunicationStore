package com.guoxd.communicationstore

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.HttpAuthHandler
import com.guoxd.communicationstore.R.*
import com.guoxd.communicationstore.ble.BlueToothActivity
import com.guoxd.communicationstore.bluetoothSample.SampleBlueToothActivity
import com.guoxd.communicationstore.databinding.ActivityMainBinding
import com.guoxd.communicationstore.http.HttpSendActivity
import com.guoxd.communicationstore.https.HttpsAuthActivity
import com.guoxd.communicationstore.nfc.NFCSendActivity
import com.guoxd.communicationstore.socket.SocketSendActivity

class MainActivity : AppCompatActivity(){

    var mContext:MainActivity ?= null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this;
        var mBinding:ActivityMainBinding = DataBindingUtil.setContentView(this, layout.activity_main)
        mBinding.tvHttp.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,HttpSendActivity::class.java))
        })
        mBinding.tvHttps.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,HttpsAuthActivity::class.java))
        })
        mBinding.tvSocket.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,SocketSendActivity::class.java))
        })
        mBinding.tvBle.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,BlueToothActivity::class.java))
        })
        mBinding.tvBluetooth.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,SampleBlueToothActivity::class.java))
        })
        mBinding.tvNfc.setOnClickListener(View.OnClickListener {
            startActivity(Intent(mContext,NFCSendActivity::class.java))
        })
    }

}
