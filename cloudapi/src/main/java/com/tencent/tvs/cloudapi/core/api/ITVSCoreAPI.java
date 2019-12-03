package com.tencent.tvs.cloudapi.core.api;

/**
 * Created by sapphireqin on 2019/11/27.
 */

public interface ITVSCoreAPI {
    /**
     * 获取语音识别模块
     *
     * @return 语音识别模块对象，可以使用语音识别功能
     */
    ITVSSpeechRecognizer getSpeechRecognizer();

    /**
     * 获取事件发送模块，参考TVS协议文档
     *
     * @return
     */
    ITVSEventManager getEventManager();

    /**
     * 初始化
     *
     * @return 0为成功，其他值为失败
     */
    int init();

    /**
     * 释放、清理
     */
    void release();
}
