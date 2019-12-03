package com.tencent.tvs.cloudapi.core;

/**
 * Created by sapphireqin on 2019/11/27.
 */

public interface ITVSAudioProviderCallback {
    boolean onAudioOutput(long sessionId, int index, byte[] audio, int length, boolean isEnd);
}
