package com.tencent.tvs.cloudapi.core.api;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public interface ITVSEventCallback {

    int onReplay(long session, String directives);

    void onError(long session, int errorCode, String reason);
}
