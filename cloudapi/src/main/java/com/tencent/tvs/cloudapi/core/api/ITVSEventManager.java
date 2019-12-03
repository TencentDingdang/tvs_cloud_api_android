package com.tencent.tvs.cloudapi.core.api;

import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContext;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEvent;

import java.util.List;

/**
 * TVS Event发送模块
 * Created by sapphireqin on 2019/11/29.
 */

public interface ITVSEventManager {
    /**
     * 发送TVS Event
     *
     * @param sessionId   会话ID
     * @param request     请求
     * @param contextList 上下文
     * @param callback    回调
     * @return
     */
    int sendEvent(long sessionId, TVSEvent request, List<TVSContext> contextList, ITVSEventCallback callback);
}
