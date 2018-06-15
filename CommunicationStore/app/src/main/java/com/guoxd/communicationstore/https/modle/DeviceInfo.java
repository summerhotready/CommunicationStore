package com.guoxd.communicationstore.https.modle;

/**
 * Created by guoxd on 2018/6/15.
 */

public class DeviceInfo {
    //验证码方式
    String manufacturerId;
    String manufacturerName;
    String model;
    String protocolType;
    String deviceType;

    //密码方式
    String nodeId;

    public DeviceInfo(String manufacturerId, String manufacturerName, String model, String protocolType, String deviceType) {
        this.manufacturerId = manufacturerId;
        this.manufacturerName = manufacturerName;
        this.model = model;
        this.protocolType = protocolType;
        this.deviceType = deviceType;
    }
    public DeviceInfo(String nodeId){
        this.nodeId = nodeId;
    }
}
