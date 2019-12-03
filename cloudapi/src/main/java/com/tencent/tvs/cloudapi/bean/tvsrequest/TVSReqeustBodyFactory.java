package com.tencent.tvs.cloudapi.bean.tvsrequest;

import com.google.gson.Gson;
import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContext;
import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TvsCustomData;
import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TvsUserInterface;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEvent;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TvsTextRecognizer;
import com.tencent.tvs.cloudapi.core.TVSDeviceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sapphireqin on 2019/11/26.
 */

public class TVSReqeustBodyFactory {

    // 打包TVS Event请求体
    public static String createTVSEventRequest(TVSEvent event, List<TVSContext> contexts) {
        Gson gson = new Gson();
        TVSRequestBody body = new TVSRequestBody();
        body.getBaseInfo().setQua(TVSDeviceConfig.getQUA());
        body.getBaseInfo().getDevice().setSerial_num(TVSDeviceConfig.getDSN());

        body.setEvent(event);

        // 增加上下文
        body.setContext(contexts);

        return gson.toJson(body);
    }
}
