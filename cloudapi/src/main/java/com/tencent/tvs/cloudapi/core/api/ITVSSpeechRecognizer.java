package com.tencent.tvs.cloudapi.core.api;


/**
 * 语音识别模块
 * Created by sapphireqin on 2019/11/27.
 */

public interface ITVSSpeechRecognizer {
    /**
     * 开启语音识别功能
     *
     * @param session 本次语音识别的会话ID，需要保证每次语音识别的会话ID唯一，比如使用System.currentTimeMillis
     * @return 0为成功，其他值为失败
     */
    int start(long session);

    /**
     * 取消当前正在进行的语音识别功能
     *
     * @return 0为成功，其他值为失败
     */
    int cancel();

    /**
     * 外部写入录音，要求写入PCM数据，采样率为16000，通道数为2
     *
     * @param session    当前正在进行的语音识别会话的ID
     * @param data       PCM数据
     * @param dataLength PCM数据的长度，字节数
     * @param isEnd      本次录音是否结束，true为结束，false为未结束
     * @return 写入的字节数，大于等于0代表成功，其他代表失败
     */
    int writeAudio(long session, byte[] data, int dataLength, boolean isEnd);

    /**
     * 监听语音识别结果
     *
     * @param listener 语音识别结果监听器
     */
    void setListener(ITVSSpeechRecognizerListener listener);

}
