package com.guoxd.communicationstore.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.*
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.guoxd.communicationstore.R
import com.guoxd.communicationstore.basepackage.utils.CheckPermissionUtil
import com.guoxd.communicationstore.basepackage.utils.SharePreaceUtils
import com.guoxd.communicationstore.basepackage.utils.ToastUtils
import com.guoxd.communicationstore.ble.bluetooth.BlueToothService
import com.guoxd.communicationstore.ble.bluetooth.OnBlueWriteReadListener
import java.util.ArrayList
import java.util.HashMap

/**
 * Created by guoxd on 2018/4/9.
 */
open class BlueToothActivity:AppCompatActivity(){
    val TAG="BlueToothActivity"
    var fresh: TextView ?= null;
    var sao: TextView ?= null;
    var listScan: ListView ?= null;
    var listLinked:ListView ?= null;

    //data
    var mConnectBluetoothList:ArrayList<BluetoothDevice>  = ArrayList();
    var mSearchBluetoothList:ArrayList<BluetoothDevice>  =ArrayList();
    internal var mSearchBluetoothListScan: ArrayList<BluetoothDevice> = ArrayList()
    internal var mLinkedData = ArrayList<DeviceData>()
    var currentDevice:BluetoothDevice ?= null

    //获得BluetoothAdapter
    internal var mBluetoothAdapter: BluetoothAdapter? = null
    internal var adapterScan: SimpleAdapter? = null
    //是否允许搜索
    var mScanning = false
    //请求启用蓝牙请求码
    private val REQUEST_ENABLE_BT = 1
    //倒计时时间
    internal var SEARCHTIME = 10 * 1000
    internal var SCAN_PERIOD = 10 * 1000
    //权限
    var checkPermissionUtil:CheckPermissionUtil ?= null
    //搜索
    var searchTimer:CountDownTimer ?= null;
    //连接判断
    internal var islinking = false

    internal val msg1 = 101
    internal val msg2 = 102
    internal val msg3 = 103
    internal val msg4 = 104
    internal val msg_conn = 105
    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                msg1 -> {
                }
                msg2 -> {
                }
                msg3 -> {
                }
                msg4 -> {
                    resetSearchData()
                }
                msg_conn -> {
                }/*if(!mConnectBluetoothList.contains(currentDevice)){
                        mConnectBluetoothList.add(currentDevice);
                        setMAC(currentDevice);
                    }
                    mSearchBluetoothList.remove(currentDevice);
//                    resetLinkedData();
                    resetSearchData();
                    islinking = false;*/
            }
        }
    }
    @SuppressLint("MissingPermission")
    internal var onItemClickListener: AdapterView.OnItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
        if (islinking) {
            Toast.makeText(this@BlueToothActivity, "连接中……", Toast.LENGTH_SHORT).show()
            return@OnItemClickListener
        }
        currentDevice = mSearchBluetoothList.get(position);
        showConnDialog(mSearchBluetoothList[position])
         mBluetoothAdapter?.stopLeScan(mLeScanCallback);
        if(mBluetoothLeService == null)
            return@OnItemClickListener;
        islinking = true;

       mBluetoothLeService?.connect(currentDevice?.getAddress());
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link_bluetooth)
        initView()
    }
    //
    private fun initData() {
        val str = SharePreaceUtils.init(this).getString("MAC", "")
        if (str != "") {
            val strs = str.split(" ".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (i in strs.indices) {
                val mac = strs[i]
                val name = SharePreaceUtils.init(this).getString(mac, "")
                val data = DeviceData(mac, name)
                mLinkedData.add(data)
            }
            initLinkedAdapter();
            /*代码重译
            String[] strs = var.split(" ");
            for(int i=0;i<strs.length;i++){
                String mac = strs[i];
                String name = ShardUtils.init(this).getString(mac,"");
                DeviceData data = new DeviceData(mac,name);
                mLinkedData.add(data);
            }
            * */
        }
        //
    }
    fun initView(){
        fresh = findViewById(R.id.btn_fresh) as TextView
        fresh?.setOnClickListener(View.OnClickListener {
            fresh?.setEnabled(false)
            startSearch()
        })
        fresh = findViewById(R.id.btn_sao) as TextView
        sao?.setOnClickListener(View.OnClickListener() {
              ToastUtils.getIntence().showToast(this@BlueToothActivity,"扫码")
        })

        listLinked = findViewById(R.id.listview_linked) as ListView
        listScan = findViewById(R.id.listview_islink) as ListView

        checkPermissionUtil = CheckPermissionUtil(this)
        initBlueAdapter()
        initSearch()
        initData()

        // SSLUtils.initSSL(this);
       /* Thread(Runnable {
            //                        getTest();
            postTest()
        }).start()*/
    }

    private fun initBlueAdapter():Boolean {
        val blueManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = blueManager.adapter
        if (mBluetoothAdapter == null) {//没有蓝牙ble功能的才会为null，关闭了是inEnable = false
            Toast.makeText(this, "您的设备不支持蓝牙BLE", Toast.LENGTH_SHORT).show()
            return false;
        }else{
            return true
        }
    }

    override fun onResume() {
        super.onResume()
        checkPermissionUtil?.onResumeCheck()
    }

    override fun onDestroy() {
        if (searchTimer != null) {
            searchTimer?.cancel()
            searchTimer = null
        }
        super.onDestroy()
    }

    private fun postTest() {
        /*try {
            val json = "Post的数据"
            val JSON = MediaType.parse("application/json; charset=utf-8")
            val client = HttpClientSslHelper.getSslOkHttpClient(this)
            val body = RequestBody.create(JSON, json)
            val request = Request.Builder()
                    .url(ElcoUrl.SSL_POS_URL)
                    .post(body)
                    .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful()) {
                Log.d("MainActivity", response.body().string())
            } else {
                throw IOException("Unexpected code " + response)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }*/

    }



    private fun checkBlueTooth() {
        if (mBluetoothAdapter == null || !(mBluetoothAdapter?.isEnabled()?:false)) {
            Log.i(TAG, "mBluetoothAdapter check：" + (mBluetoothAdapter == null))
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            initBlueAdapter()
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
//        intentFilter.addAction(BlueToothService.ACTION_GATT_CONNECTED)
//        intentFilter.addAction(BlueToothService.ACTION_GATT_DISCONNECTED)
//        intentFilter.addAction(BlueToothService.ACTION_GATT_SERVICES_DISCOVERED)
//        intentFilter.addAction(BlueToothService.ACTION_DATA_AVAILABLE)
//        intentFilter.addAction(BlueToothService.READ_RSSI)
        return intentFilter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult")
        checkPermissionUtil?.onRequestPermissionsResult(requestCode,
                permissions, grantResults)
    }
    //search ble
    private fun initSearch() {
        searchTimer = object : CountDownTimer(SEARCHTIME.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                //秒后可重发
            }
            override fun onFinish() {
                //开始搜索
                fresh?.setEnabled(true)
            }
        }
    }

    private fun startSearch() {
        //        mConnectBluetoothList.clear();
        if (!mScanning) {
            mSearchBluetoothListScan.clear()
            searchTimer?.start()
            Log.i(TAG, "startSearch start")
            scanLeDevice(true)
        } else {
            Log.i(TAG, "startSearch finish")
            searchTimer?.cancel()
            scanLeDevice(false)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(enable: Boolean) {
        if (enable) {
            Log.i(TAG, "显示" + mScanning)
            handler.postDelayed({
                if (mScanning) {
                    Log.i(TAG, "scanLeDevice达成结束搜索条件")
                    mScanning = false
                    mBluetoothAdapter?.stopLeScan(mLeScanCallback)
                }
            }, SCAN_PERIOD.toLong())
            Log.i(TAG, "scanLeDevice开始")
            mScanning = true
            handler.sendEmptyMessage(msg3)
            //蓝牙4.0以前的方式是bAdapter.startDiscovery()，然后注册监听器监听广播，通过广播获得扫描结果，
            //mBluetoothAdapter?.startLeScan的方法过时，被BluetoothLeScanner的startScan（List,ScanSettings,ScanCallback）取代，但BluetoothLeScanner由API5.0开始支持
            mBluetoothAdapter?.startLeScan(mLeScanCallback)
        } else {
            Log.i(TAG, "scanLeDevice结束")
            mScanning = false
            mBluetoothAdapter?.stopLeScan(mLeScanCallback)
        }
    }

    internal var mLeScanCallback: BluetoothAdapter.LeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            //                    boolean check = mConnectBluetoothList.contains(device);
            if (!mSearchBluetoothListScan.contains(device)) {
                mSearchBluetoothListScan.add(device)//08:D0:CC:38:65:E9
                handler.sendEmptyMessage(msg4)
            }
        }
    }

    internal var hashSearchData: ArrayList<HashMap<String, String>> = ArrayList()

    var adapterLinked:LinkedAdapter?=null;
    fun initLinkedAdapter(){
        adapterLinked = LinkedAdapter(this);
        adapterLinked?.setmData(mLinkedData);
        adapterLinked?.listener = object:ItemButtonClickListener {
            override fun onDetail(mac: String) {
                /*Intent intent = new Intent(LinkBlueActivity.this,DeviceDetailActivity.class);
                intent.putExtra(ElcoUrl.IEMI,mac);
                startActivity(intent);*/
            }

            override fun onLinked(mac: String) {
                mBluetoothLeService?.writeCharacteristic(mac);
                /*for (BluetoothDevice device:mConnectBluetoothList){
                    if (device.getAddress().equals(mac)) {
                        Intent intent = new Intent(LinkBlueActivity.this, DeviceDetailActivity.class);
                        intent.putExtra("device", currentDevice);
                startActivity(intent);
                    }
                }*/
            }

        };
        listLinked?.setAdapter(adapterLinked);
    }
    @SuppressLint("MissingPermission")
    fun resetSearchData() {
        mSearchBluetoothList = mSearchBluetoothListScan
        val size = mSearchBluetoothList.size
        hashSearchData.clear()
        if (size > 0) {
            for (i in 0..size - 1 - 1) {
                val device:BluetoothDevice = mSearchBluetoothList.get(i)
                val maps = HashMap<String,String>()

                if (device.name == null || device.name== "") {
                    maps.put("name", device.getAddress())
                } else {
                    maps.put("name", device.getName())
                }
                hashSearchData.add(maps)
            }
        }
        var adapterScan = SimpleAdapter(this, hashSearchData,
                android.R.layout.simple_list_item_activated_1,
                arrayOf("name"),
                intArrayOf(android.R.id.text1))
        listScan?.setAdapter(adapterScan)
        listScan?.setOnItemClickListener(onItemClickListener)
    }
    //
    class DeviceData(internal var name: String, internal var mac: String)
//link
    private fun initServer():Boolean{
        //连接BlueService
    var gattServiceIntent:Intent = Intent(this@BlueToothActivity,BlueToothService::class.java)
        //bind a service
        var bll = bindService(gattServiceIntent, mServiceConnection, //2
        Context.BIND_AUTO_CREATE);
        Log.d(TAG,"service start is " +bll);
        return bll;
    }
    var mBluetoothLeService:BlueToothService ?= null;
var mServiceConnection: ServiceConnection = object:ServiceConnection{
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        mBluetoothLeService = (service as BlueToothService.LocalBinder).getService();
        mBluetoothLeService?.setListener(object:OnBlueWriteReadListener{
            override fun onRead(msg: String?) {

            }
            override fun onWrite(msg: String?) {
                var message:Message  = handler.obtainMessage();
                message.what = msg1;
                message.getData().putString("data",msg);
                handler.sendMessage(message);
            }
        })
        if(mBluetoothLeService?.initialize()?:false){
            Log.d(TAG, "能初始化");
        }else{
            Log.d(TAG, "初始化失败");
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mBluetoothLeService = null;
    }
}




//dialog
var isDialoginit = false;
var dialog:AlertDialog ?= null
    var dialogView:View ?=null
@SuppressLint("WrongViewCast")
protected  fun initDialog():Boolean{
    if(!isDialoginit){
        isDialoginit = true;
        dialog = AlertDialog.Builder(this).create()
        dialogView = View.inflate(this, R.layout.dialog_is_conn_bluetooth, null)
        dialogView?.findViewById<ImageView>(R.id.image_delete)?.setOnClickListener(View.OnClickListener { dialog?.dismiss() })
        dialogView?.findViewById<Button>(R.id.dialog_conn)?.setOnClickListener(View.OnClickListener { dialog?.dismiss() })
        dialogView?.findViewById<Button>(R.id.dialog_detail)?.setOnClickListener(View.OnClickListener { dialog?.dismiss() })
        dialog?.setView(dialogView)
    }
    return isDialoginit
}
protected fun showConnDialog(currentDevice: BluetoothDevice) {
    if(initDialog()?:false){
        dialogView?.findViewById<TextView>(R.id.dialog_title)?.setText("是否连接设备"+currentDevice.address+"？")
        dialog?.show()
    }
}

    inner class LinkedAdapter(var mContext: Context) : BaseAdapter() {
        internal var listener: ItemButtonClickListener?=null
        var mData: List<DeviceData>?=null
        fun setmData(mData: List<DeviceData>) {
            this.mData = mData
        }

        override fun getCount(): Int {
            return mData?.size?:0
        }

        override fun getItem(position: Int): Any {
            return mData?.get(position)?:""
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            var convertView = view
            val holder: LinkedHolder
            if (convertView == null) {
                holder = LinkedHolder()
                convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_item_linked, null)
                holder.name = convertView.findViewById<TextView>(R.id.text)
                holder.btn_detail = convertView.findViewById<Button>(R.id.btn_detail)
                holder.btn_linke = convertView.findViewById<Button>(R.id.btn_link) as Button
                convertView.tag = holder
            } else {
                holder = convertView.tag as LinkedHolder
            }
            val device = mData?.get(position)
            holder.name?.setText(if (device?.name.equals("")) device?.mac else device?.name)
            holder.btn_detail?.setOnClickListener(View.OnClickListener { listener?.onDetail(device?.mac?:"") })
            holder.btn_linke?.setOnClickListener(View.OnClickListener { listener?.onLinked(device?.mac?:"") })

            return convertView?: view!!
        }
    }

    inner internal class LinkedHolder {
        var name: TextView? = null
        var btn_detail: Button? = null
        var btn_linke: Button? = null
    }
    internal interface ItemButtonClickListener {
        fun onDetail(mac: String)
        fun onLinked(mac: String)
    }
}