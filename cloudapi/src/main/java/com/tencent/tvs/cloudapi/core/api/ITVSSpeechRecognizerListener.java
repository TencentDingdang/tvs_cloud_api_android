package com.tencent.tvs.cloudapi.core.api;

/**
 * 语音识别结果监听器
 * Created by sapphireqin on 2019/11/27.
 */

public interface ITVSSpeechRecognizerListener {

    /**
     * 语音识别结果返回
     *
     * @param session    语音识别会话ID，代表是哪次语音识别会话的结果
     * @param resultText 语音识别结果
     * @param isEnd      为false代表resultText为中间结果，true代表resultText为最终结果
     * @return
     */
    int onRecognize(long session, String resultText, boolean isEnd);

    /**
     * 语音识别出错
     *
     * @param session   语音识别会话ID，代表是哪次语音识别会话的结果
     * @param errorCode 错误码
     * @param reason    错误原因
     */
    void onError(long session, int errorCode, String reason);
}
