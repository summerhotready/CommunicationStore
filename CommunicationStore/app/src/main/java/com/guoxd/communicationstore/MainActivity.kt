package com.guoxd.communicationstore

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
    var items= arrayOf("Http","Https","Socket","BLE","Simple Bluetooth","NFC")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this;
        var mBinding:ActivityMainBinding = DataBindingUtil.setContentView(this, layout.activity_main)
        var adapter:MainAdapter = MainAdapter();
        adapter.listener = object: onItemClickListener{
            override fun onClick(key: String) {
                when(key){
                    items[0]->{
                        startActivity(Intent(mContext, HttpSendActivity::class.java))
                    }
                    items[1]->{
                        startActivity(Intent(mContext, HttpsAuthActivity::class.java))
                    }
                    items[2]->{
                        startActivity(Intent(mContext, SocketSendActivity::class.java))
                    }
                    items[3]->{
                        startActivity(Intent(mContext, BlueToothActivity::class.java))
                    }
                    items[4]->{
                        startActivity(Intent(mContext, SampleBlueToothActivity::class.java))
                    }
                    items[5]->{
                        startActivity(Intent(mContext, NFCSendActivity::class.java))
                    }
                }
            }
        }
        mBinding.recyclerView.layoutManager = LinearLayoutManager(this);
        mBinding.recyclerView.adapter = adapter;
    }

    interface onItemClickListener{
        fun onClick(key:String);
    }
    inner class MainHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var tv_title:TextView = itemView.findViewById(android.R.id.text1);
        var item:View = itemView;
    }
    inner class MainAdapter : RecyclerView.Adapter<MainHolder>(){
        var listener:onItemClickListener?=null;
        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
            var view :View = LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1,parent,false)
            var holder = MainHolder(view);
            return holder;
        }

        override fun onBindViewHolder(holder: MainHolder?, position: Int) {
            holder?.tv_title?.setText(items[position])
            holder?.item?.setOnClickListener(View.OnClickListener { listener?.onClick(items[position]) })
        }
    }

}
