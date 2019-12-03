package com.tencent.tvs.cloudapi.core.api;

import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSReqeustBodyFactory;
import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContext;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEvent;
import com.tencent.tvs.cloudapi.core.TVSHttpManager;
import com.tencent.tvs.cloudapi.tools.Logger;
import com.tencent.tvs.cloudapi.tools.StringUtils;

import java.util.List;

/**
 * 发送TVS event
 * Created by sapphireqin on 2019/11/29.
 */

public class TVSEventManager implements ITVSEventManager {
    private static final String TAG = "TVSEventManager";
    private TVSHttpManager httpManager;

    public int init(TVSHttpManager manager) {
        httpManager = manager;
        return 0;
    }

    public void release() {

    }

    @Override
    public int sendEvent(final long sessionId, final TVSEvent event, List<TVSContext> contextList, final ITVSEventCallback callback) {
        String request = TVSReqeustBodyFactory.createTVSEventRequest(event, contextList);

        if (StringUtils.isEmpty(request)) {
            Logger.e(TAG, "Empty Request, session " + sessionId);
            if (callback != null) {
                callback.onError(sessionId, -1, "");
            }
            return -1;
        }
        Logger.i(TAG, "send request " + request);
        try {
            httpManager.sendEvent(request, new TVSHttpManager.ITVSHttpManagerCallback() {
                @Override
                public void onError(int errorCode) {
                    Logger.i(TAG, "onError, errorCode " + errorCode + ", session " + sessionId);
                    if (callback != null) {
                        callback.onError(sessionId, errorCode, "");
                    }
                }

                @Override
                public void onResponse(int responseCode, String responseBody) {
                    Logger.i(TAG, "onResponse, responseCode " + responseCode + ", body " + responseBody + ", session " + sessionId);
                    if (callback != null) {
                        callback.onReplay(sessionId, responseBody);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
