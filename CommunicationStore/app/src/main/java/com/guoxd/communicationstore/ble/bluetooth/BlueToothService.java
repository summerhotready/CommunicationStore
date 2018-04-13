package com.guoxd.communicationstore.ble.bluetooth;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by guoxd on 2018/3/2.
 */

public class BlueToothService extends Service {

    static final String TAG="BlueToothService";

    //无连接
    private static final int STATE_DISCONNECTED = 0;
    //连接中
    private static final int STATE_CONNECTING = 1;
    //已连接
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.charon.www.NewBluetooth.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.charon.www.NewBluetooth.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.charon.www.NewBluetooth.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.charon.www.NewBluetooth.EXTRA_DATA";
    public final static String READ_RSSI = "com.charon.www.NewBluetooth.READ_RSSI";


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    //蓝牙设备地址
    private String mBluetoothDeviceAddress = "";
    public int mConnectionState = STATE_DISCONNECTED;

    public List<UUID> writeUuid = new ArrayList<>();
    public List<UUID> readUuid = new ArrayList<>();
    public List<UUID> notifyUuid = new ArrayList<>();
    public List<BluetoothGattCharacteristic> characteristics = new ArrayList<>();
    public List<BluetoothGattCharacteristic> characteristicsNotify = new ArrayList<>();

    final IBinder mBinder = new LocalBinder();

    public OnBlueWriteReadListener listener;
    public Timer mTimer;
    public TimerTask mTimerTask;
    public boolean pause;


    public void setListener(OnBlueWriteReadListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void startTimer(){
        Log.i(TAG,"startTimer");
        pause = true;
        if(mTimerTask == null){
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if(pause) {
                        readCharacteristic();
                        Log.i(TAG, Thread.currentThread() + " Timer:" + System.currentTimeMillis());
                    }
                }
            };
            if(mTimer == null){
                mTimer = new Timer();
            }
            mTimer.schedule(mTimerTask,10*1000,60*1000);
        }

    }
    public void closeTimer(){
        if(pause){
            pause = false;
        }
        if(mTimer != null){
            mTimer.cancel();
        }
    }

    public void close(){
        closeTimer();
        if(mTimer != null){
            mTimer = null;
        }
        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }

        if(mBluetoothGatt == null){
            return;
        }
//        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * 初始化
     * @return
     */
    @SuppressLint("ServiceCast")
    public boolean initialize(){
        // For API level 18 and above, get a reference to BluetoothAdapter
        if(mBluetoothManager ==null){
            mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null){
                Log.e(TAG,"Bluetooth Server 初始化错误");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null){
            Log.e(TAG,"BluetoothAdapter获取失败");
            return false;
        }
        return true;
    }

    public boolean connect(String address){

        Log.d(TAG, "将要连接 " +address);
        if(mBluetoothAdapter == null || address == null){
            Log.e(TAG,"连接失败 BluetoothAdapter初始化错误或未知adderss");
            return false;
        }
        //重连设备
        if(mBluetoothDeviceAddress != null
                && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null){
            Log.d(TAG,"尝试使用现在的 mBluetoothGatt连接");
            if(mBluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                Log.d(TAG,"连接重试 已连接");
                return true;
            }else{
                Log.d(TAG,"连接重试 未连接");
                return false;
            }
        }
        //开始连接
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.e(TAG,"设备没找到，不能连接");
            return false;
        }
        ////这个方法需要三个参数：一个Context对象，自动连接（boolean值,表示只要BLE设备可用是否自动连接到它），和BluetoothGattCallback调用。
        mBluetoothGatt = device.connectGatt(this,true,mGattCallBack);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }
    //发送广播通讯
    void broadcastUpdate(String action){
        Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null)
            return null;

        return mBluetoothGatt.getServices();
    }


//UUID:00002a00-0000-1000-8000-00805f9b34fb
    public void writeCharacteristic(String msg){
        try {//#12:82:46:96:31:54\n
            //6e400001-b5a3-f393-e0a9-e50e24dcca9e Service UUID
//            byte[] bytes = StringUtils.HexCommandtoByte(msg);
//            Log.d(TAG, "writeCharacteristic has characteristics" + characteristics.size()+" bytes:"+bytes.length);
//            for (int i = 0; i < characteristics.size(); i++) {
            if(characteristics.size()==0)
                return;
                BluetoothGattCharacteristic cc = characteristics.get(0);
            BluetoothGattCharacteristic notify = characteristicsNotify.get(0);
//                if(cc.getUuid().toString().equals("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) {//6e400002-b5a3-f393-e0a9-e50e24dcca9e
                    Log.d(TAG, "Bluetooth write has");
                    cc.setValue(msg.getBytes());
                    writeCharacteristic(cc,notify);
//                    break;
//                }
//            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"error:"+e.getMessage());
        }
//        startTimer();

    }
    //final UUID UUID_CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGattCharacteristic notify) {
        try {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            } else {
                mBluetoothGatt.writeCharacteristic(characteristic);
                /*boolean isEnableNotification = mBluetoothGatt.setCharacteristicNotification(notify, true);
                Log.d(TAG, "Bluetooth setCharacteristicNotification："+isEnableNotification);
                if(isEnableNotification) {
                    List<BluetoothGattDescriptor> descriptorList = notify.getDescriptors();
                    if(descriptorList != null && descriptorList.size() > 0) {
                        for(BluetoothGattDescriptor descriptor : descriptorList) {
                            //00002902-0000-1000-8000-00805f9b34fb
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(descriptor);
                            Log.d(TAG, "Bluetooth descriptor："+descriptor.getUuid());
                        }
                    }*/
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"error:"+e.getMessage());
        }
    }



    public void readCharacteristic(){
        Log.d(TAG, "readCharacteristic has characteristics"+characteristics.size());


        for(BluetoothGattCharacteristic characteristic:characteristics){
            if(characteristic.getUuid().toString().equals("6e400002-b5a3-f393-e0a9-e50e24dcca9e")) {//6e400002-b5a3-f393-e0a9-e50e24dcca9e
                readCharacteristic(characteristic);
                break;
            }

        }
    }
    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        } else mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void disconnect(){
        mBluetoothGatt.discoverServices();
    }

    //mBluetppthGatt的回调
    private BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {
        @Override
        //当连接状态发生改变
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){//连接状态发生改变
             intentAction = ACTION_GATT_CONNECTED;
             mConnectionState = STATE_CONNECTED;
             broadcastUpdate(intentAction);
                Log.d(TAG, "连接GATT server");
                // Attempts to discover services after successful connection.
               /* Log.i(TAG, "Attempting to start service discovery:"
                        + mBluetoothGatt.discoverServices());*/
                gatt.discoverServices();
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){//设备无法连接
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.d(TAG, "mGattCallBack status:" + status+" Disconnected from GATT server.");
//                close();
                broadcastUpdate(intentAction);
            }
        }

        @Override
        //发现新服务端
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS){
                characteristics.clear();
                characteristicsNotify.clear();
                List<BluetoothGattService> supportedGattServices = gatt.getServices();//三条
                Log.d(TAG, "PROPERTY_READ :" + BluetoothGattCharacteristic.PROPERTY_READ+" PROPERTY_WRITE:"+ BluetoothGattCharacteristic.PROPERTY_WRITE+" PROPERTY_NOTIFY:"+ BluetoothGattCharacteristic.PROPERTY_NOTIFY);
                for(BluetoothGattService gattService:supportedGattServices){
                    List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                    Log.d(TAG, "gattCharacteristics size:" + gattCharacteristics.size());
                    for(BluetoothGattCharacteristic gattCharacteristic :gattCharacteristics){
                        Log.d(TAG, "gattCharacteristic的UUID为:" + gattCharacteristic.getUuid()+" decp:"+gattCharacteristic.getDescriptors().size());
                        /*int charaProp = gattCharacteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            Log.d(TAG, "gattCharacteristic的属性为:  可读 "+(charaProp | BluetoothGattCharacteristic.PROPERTY_READ));
                            readUuid.add(gattCharacteristic.getUuid());
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                            Log.d(TAG, "gattCharacteristic的属性为:  可写 "+(charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE));
                            writeUuid.add(gattCharacteristic.getUuid());
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            Log.d(TAG, "gattCharacteristic的属性为:  具备通知属性 "+(charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY));
                            notifyUuid.add(gattCharacteristic.getUuid());
                        }*/
                       /* boolean b = true;
                        if ((BluetoothGattCharacteristic.PROPERTY_READ | gattCharacteristic.getProperties()) <= 0) {
// READ set one
                            b=false;
                        }else{
                            Log.d(TAG, "gattCharacteristic的属性为:  可读 "+(BluetoothGattCharacteristic.PROPERTY_READ | gattCharacteristic.getProperties()));
                        }
                        if ((BluetoothGattCharacteristic.PROPERTY_WRITE & gattCharacteristic.getProperties()) <= 0) {//筛掉了部分uuid
// write set one
                            b=false;
                        }else{
                            Log.d(TAG, "gattCharacteristic的属性为:  可写 "+(BluetoothGattCharacteristic.PROPERTY_WRITE & gattCharacteristic.getProperties()));
                        }*/
                        //(characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0
//                        Log.e(TAG,"gattCharacteristic的属性1 "+(gattCharacteristic.getProperties() &
//                                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) );
////                        Log.e(TAG,"gattCharacteristic的属性2 "+((gattCharacteristic.getProperties() &
////                                (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0? true:false) );
//                        int pro = gattCharacteristic.getProperties();
//                        Log.e(TAG, "uuid:" + gattCharacteristic.getUuid() + " Properties:"+pro);
                        if((gattCharacteristic.getProperties() &
                                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)>0) {
                            characteristics.add(gattCharacteristic);
                        }
                        if(gattCharacteristic.getDescriptors().size()>0){
                            characteristicsNotify.add(gattCharacteristic);
                        }
                    }
                }
                //发现服务
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }else{
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        // 读写特性
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                listener.onRead(new String(characteristic.getValue()));
                Log.i(TAG, Thread.currentThread().toString()+" onCharacteristicRead:"+ (listener == null? true:false));
            }else{
                listener.onRead("ERROR");
            }
            Log.i(TAG, Thread.currentThread()+" onCharacteristicRead status:"+status+" UUID:"+characteristic.getUuid()+" characteristic:"+new String(characteristic.getValue()));
        }



        //如果对一个特性启用通知,当远程蓝牙设备特性发送变化，回调函数onCharacteristicChanged( ))被触发。
        //当启用setCharacteristicNotification时会走这个
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e(TAG,"onCharacteristicChanged characteristic UUID:"+characteristic.getUuid()+" characteristic:"+new String(characteristic.getValue()));
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                listener.onWrite(new String(characteristic.getValue()));//BluetoothGatt: Unhandled exception in callback android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
            }else{
                listener.onWrite("ERROR");
            }
            Log.i(TAG, Thread.currentThread()+" onCharacteristicWrite status:"+status+" UUID:"+characteristic.getUuid()+" characteristic:"+new String(characteristic.getValue()));
        }



        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.i(TAG,"onReadRemoteRssi:"+rssi);
            //ControlActivity.rssi = rssi;
            broadcastUpdate(READ_RSSI);
        }


    };

    public class LocalBinder extends Binder {
        public BlueToothService getService(){
            return BlueToothService.this;
        }
    }

}
