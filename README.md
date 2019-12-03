## TVS基础API Demo使用说明



#### 1、填写APPKEY和ACCESSTOKEN

在TVSDeviceConfig.java中填写APPKEY和ACCESSTOKEN：

```java
public static final String APPKEY = "YOUR APPKEY";
public static final String ACCESS_TOKEN = "YOUR ACCESS_TOKEN";
```

登录腾讯云小微开放平台 -- 设备开放平台，在应用列表中查看应用概览，可以找到该应用的APP KEY和AccessToken；如果没有应用，需要新建一个；

开放平台地址为：

[腾讯云小微开放平台](https://dingdang.qq.com/open#/)



#### 2、填写设备唯一标识

在TVSDeviceConfig.java中填写：

```java
public static String getDSN() {   
    // TO-DO 在此处填写设备唯一标识，接入方要保证该标识的唯一性
    return "YOUR DSN";
}
```

DSN是设备的唯一标识，要保证该标识的唯一性，比如使用手机的IMEI等；



#### 3、填写设备的QUA

在TVSDeviceConfig.java中填写：

```java
public static String getQUA() {    
    // TO-DO 在此处填写设备QUA    
    return "QV=3&VE=GA&VN=1.0.0.1000&PP=com.tencent.ai.test&CHID=10020";
}
```

关于QUA的字段说明，可以查阅如下文档：
[QUA字段说明](https://github.com/TencentDingdang/tvs-tools/blob/master/doc/%E8%85%BE%E8%AE%AF%E5%8F%AE%E5%BD%93HTTP%E6%96%B9%E5%BC%8F%E6%8E%A5%E5%85%A5API%E6%96%87%E6%A1%A3.md#71-qua%E5%AD%97%E6%AE%B5%E8%AF%B4%E6%98%8E)



#### 4、在NLP时填写自定义上下文

在MainActivity.java的startNLP方法中增加自定义上下文，增加自定义键-对标识：

```java
List<TVSContext> contexts = new ArrayList<>();

...

// 自定义上下文
Map<String, String> stateList = new HashMap<>();
// TO-DO 增加自定义标识，用于一些特殊需求
stateList.put("spotLabel", "spotContent");
contexts.add(TVSContextFactory.createTvsCustomDataContext(stateList));

...
```



#### 5、完善TVS指令解析

在MainActivity.java的onRecvDirectives方法中对后台下发的其他的TVS指令进行解析处理，当前只实现对SpeechSynthesizer.SpeakText （TTS指令） 和AudioPlayer.Play（媒体播放指令），其余指令需要自行扩展：

```java
private void onRecvDirectives(TVSDirectives directives) {
    if (directives == null) {
        return;
    }

    if (directives instanceof SpeechSynthesizer.SpeakText) {
        // TTS指令
        SpeechSynthesizer.SpeakText dir = (SpeechSynthesizer.SpeakText) directives;
        Logger.e(TAG, dir.toString());
    } else if (directives instanceof AudioPlayer.Play) {
        // 播放指令
        AudioPlayer.Play dir = (AudioPlayer.Play) directives;

        startPlayNetworkMedia(dir);
    } else if (directives instanceof SpeechSynthesizer.SpeakText) {
        // 未知指令
        UnknownDirectives dir = (UnknownDirectives) directives;

        onRecvUnknownDirectives(dir.getContent());
    } else {
        // TO-DO，处理其他指令
    }
}
```

TVS协议可以参考如下路径中的文档：
[TVS Protocol](https://github.com/TencentDingdang/tvs-tools/tree/master/Tvs%20Protocol)

如果不想使用JSON转java类的方式解析指令，可以在onRecvUnknownDirectives中直接对JSON字符串进行解析处理；



#### 6、Demo使用方法

运行testapp，安装apk到手机，点击"开始录音"，对手机麦克风说话，可以在界面下方看到语音识别后的文本；

点击”语义理解“，将对语音识别的结果进行语义理解，返回结果并打印在界面下方。



#### 7、其他

语音识别协议可以参考如下文档：

[腾讯云叮当语音识别协议](https://github.com/TencentDingdang/tvs-tools/blob/master/doc/腾讯叮当HTTP方式接入API文档.md#52-语音识别)