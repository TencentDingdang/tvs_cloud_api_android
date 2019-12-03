package com.tencent.tvs.cloudapi.tools;

/**
 * Created by sapphireqin on 2019/12/2.
 */

public class SessionIdGenerator {
    public static long generateNewSessionId() {
        return System.currentTimeMillis();
    }
}
