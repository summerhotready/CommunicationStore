package com.guoxd.communicationstore.ble.bluetooth;

/**
 * Created by guoxd on 2018/3/14.
 */

public interface OnBlueWriteReadListener {
    void onWrite(String msg);
    void onRead(String msg);
}
