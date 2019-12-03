#### 1、填写APPKEY和ACCESSTOKEN

在TVSDeviceConfig.java中填写APPKEY和ACCESSTOKEN：

```java
public static final String APPKEY = "YOUR APPKEY";
public static final String ACCESS_TOKEN = "YOUR ACCESS_TOKEN";
```

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

在MainActivity.java的startNLP方法中增加自定义上下文，增加景区标识：

```java
List<TVSContext> contexts = new ArrayList<>();

...

// 自定义上下文
Map<String, String> stateList = new HashMap<>();
// TO-DO 增加景区标识
stateList.put("spotLabel", "sotContent");
contexts.add(TVSContextFactory.createTvsCustomDataContext(stateList));

...
```