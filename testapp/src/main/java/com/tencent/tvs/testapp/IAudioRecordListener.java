package com.tencent.tvs.testapp;

/**
 * 录音的回调
 */
public interface IAudioRecordListener {
    /**
     * 录音开始
     */
    void onRecordingStart(long session);

    /**
     * 录音过程中调用，可能会调用多次
     *
     * @param data    录音数据
     * @param dataLen 成功返回数据大小，失败返回错误码。
     */
    void onRecording(long session, byte[] data, int dataLen);

    /**
     * AudioRecord 创建失败
     */
    void onRecordCreateError(long session);

    /**
     * 录音过程中的异常处理
     */
    void onRecordingFailed(long session);

    void onRecordingEnd(long session);
}
