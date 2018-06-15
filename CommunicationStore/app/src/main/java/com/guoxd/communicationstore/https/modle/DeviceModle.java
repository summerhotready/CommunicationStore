package com.guoxd.communicationstore.https.modle;

/**
 * Created by guoxd on 2018/6/14.
 */

public class DeviceModle {
    //注册
    String endUserId;
    //设备绑定信息，需要与设备 Profile 文件中的信息保持一致
    DeviceInfo deviceInfo;
    //验证码方式注册
    String verifyCode;
    String nodeId;

    int timeout;
    String psk;
    //密码方式注册
//    String region;//设备所在的区域信息。
//    String organization;//设备所属的组织信息。
//    String  timezon ;// 设备所在时区信息。
//    String subsyste ;// 用户系统标识。用于区分这个设 备是否是第三方子系统
    //callback has
    String deviceId;
    //密码方式
    String secret;



    public String getEndUserId() {
        return endUserId;
    }

    public void setEndUserId(String endUserId) {
        this.endUserId = endUserId;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPsk() {
        return psk;
    }

    public void setPsk(String psk) {
        this.psk = psk;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
