package com.guoxd.communicationstore.https.modle;

/**
 * Created by guoxd on 2018/6/14.
 */

public class DeviceUpdateModle {

    String appId;
    String name;//设备名称
    String endUser;// 终端用户，若为直连设备，则 endUser 可选
    String mute;//表示设备是否处于冻结状态
    String manufacturerId;//厂商 ID，唯一标识一个厂商。
    String manufacturerName;//厂商名称。
    String deviceType;//设备类型，大驼峰命名方式，例如： MultiSensor、ContactSensor、Camera 和 WaterMeter。 在 NB-IoT 方案中，注册设备后必须 修改设备类型，且要与 profile 中定义 的保持一致。
    String model;//设备型号，由厂商定义。
    String location;//设备位置。
    String protocolType;//设备使用的协议类型，当前支持的协 议类型：CoAP，huaweiM2M，ZWave，ONVIF，WPS，Hue，WiFi， J808，Gateway，ZigBee，LWM2M。
//    String deviceConfig;//设备配置信息，具体参见 DeviceConfigDTO 结构体。
    String region;//设备区域信息。
    String organization;//设备所属的组织信息。
    String timezone;//设备所在时区信息，使用时区编码， 如北京时区对应的时区编码为 Asia/Beijing。
    String imsi ;//NB-IoT 终端的 IMSI。


}
