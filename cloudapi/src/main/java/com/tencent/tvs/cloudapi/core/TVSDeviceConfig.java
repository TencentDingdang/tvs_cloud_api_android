package com.tencent.tvs.cloudapi.core;

/**
 * Created by sapphireqin on 2019/11/26.
 */

public class TVSDeviceConfig {
    // TO-DO 在此处填写你的bot APP key
    public static final String APPKEY = "YOUR APPKEY";
    // TO-DO 在此处填写你的bot access token
    public static final String ACCESS_TOKEN = "YOUR ACCESS_TOKEN";

    public static String getDSN() {
        // TO-DO 在此处填写设备唯一标识，接入方要保证该标识的唯一性
        return "1234567890abcdefg";
    }

    public static String getQUA() {
        // TO-DO 在此处填写设备QUA
        return "QV=3&VE=GA&VN=1.0.0.1000&PP=com.tencent.tvs.cloud&CHID=10020";
    }
}
