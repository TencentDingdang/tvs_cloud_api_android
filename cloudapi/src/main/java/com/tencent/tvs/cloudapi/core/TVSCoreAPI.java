package com.tencent.tvs.cloudapi.core;

import com.tencent.tvs.cloudapi.core.api.ITVSCoreAPI;
import com.tencent.tvs.cloudapi.core.api.ITVSEventManager;
import com.tencent.tvs.cloudapi.core.api.ITVSSpeechRecognizer;
import com.tencent.tvs.cloudapi.core.api.TVSEventManager;

/**
 * Created by sapphireqin on 2019/11/27.
 */

public class TVSCoreAPI implements ITVSCoreAPI {
    // 语音识别
    private ITVSSpeechRecognizer speechRecognizer;

    // Event
    private ITVSEventManager eventManager;

    // 负责HTTPS发送
    private TVSHttpManager httpManager;

    @Override
    public ITVSSpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }

    @Override
    public ITVSEventManager getEventManager() {
        return eventManager;
    }

    @Override
    public int init() {
        if (httpManager == null) {
            httpManager = new TVSHttpManager();
            httpManager.init();
        }

        if (speechRecognizer == null) {
            TVSSpeechRecognizer recognizer = new TVSSpeechRecognizer();
            recognizer.init(httpManager);
            speechRecognizer = recognizer;
        }

        if (eventManager == null) {
            TVSEventManager manager = new TVSEventManager();
            manager.init(httpManager);
            eventManager = manager;
        }
        return 0;
    }

    @Override
    public void release() {
        if (speechRecognizer != null) {
            if (speechRecognizer instanceof TVSSpeechRecognizer) {
                TVSSpeechRecognizer recognizer = (TVSSpeechRecognizer) speechRecognizer;
                recognizer.release();
            }

            speechRecognizer = null;
        }

        if (eventManager != null) {
            if (eventManager instanceof TVSEventManager) {
                TVSEventManager manager = (TVSEventManager) eventManager;
                manager.release();
            }

            eventManager = null;
        }

        httpManager = null;
    }
}
