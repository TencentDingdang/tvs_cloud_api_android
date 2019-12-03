package com.tencent.tvs.cloudapi.core;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.tvs.cloudapi.bean.airequest.AIRequest;
import com.tencent.tvs.cloudapi.bean.airequest.ReqHeader;
import com.tencent.tvs.cloudapi.bean.airequest.asr.AsrReqPayload;
import com.tencent.tvs.cloudapi.core.api.ITVSSpeechRecognizer;
import com.tencent.tvs.cloudapi.core.api.ITVSSpeechRecognizerListener;
import com.tencent.tvs.cloudapi.tools.Base64Util;
import com.tencent.tvs.cloudapi.tools.Logger;


/**
 * Created by sapphireqin on 2019/11/27.
 */

public class TVSSpeechRecognizer implements ITVSSpeechRecognizer, ITVSAudioProviderCallback {
    private static final String TAG = "TVSSpeechRecognizer";

    // 语音编码类型
    private static final String AUDIO_TYPE = "SPEEX";

    private TVSAudioProvider mAudioProvider;

    private ITVSSpeechRecognizerListener mListener;

    private TVSHttpManager httpManager;

    private boolean mIsEnd;

    protected TVSSpeechRecognizer() {

    }

    private synchronized boolean isCurrentSpeechEnd() {
        return mIsEnd;
    }

    private synchronized void setCurrentSpeechEnd(boolean end) {
        mIsEnd = end;
    }

    public int init(TVSHttpManager httpManager) {
        if (mAudioProvider != null) {
            return 0;
        }

        this.httpManager = httpManager;
        mAudioProvider = new TVSAudioProvider();
        mAudioProvider.init();
        mAudioProvider.setAduioCallback(this);
        return 0;
    }

    public void release() {
        if (mAudioProvider == null) {
            return;
        }
        mAudioProvider.release();
        mAudioProvider = null;
        mListener = null;
    }

    @Override
    public int start(long session) {
        if (session == 0 || mAudioProvider == null) {
            return -1;
        }

        setCurrentSpeechEnd(false);
        return mAudioProvider.start(session, 16000, 2);
    }

    @Override
    public int cancel() {
        if (mAudioProvider == null) {
            return -1;
        }
        mAudioProvider.cancel();
        return 0;
    }

    @Override
    public int writeAudio(long session, byte[] data, int dataLength, boolean isEnd) {
        if (mAudioProvider == null) {
            return -1;
        }

        return mAudioProvider.writeAudio(session, data, dataLength, isEnd);
    }

    @Override
    public void setListener(ITVSSpeechRecognizerListener listener) {
        mListener = listener;
    }

    private void onRecvResponse(long sessionId, String response) {
        boolean isFinish = false;
        String result = "";
        String sessionIdStr = "";
        try {
            JsonObject obj = new JsonParser().parse(response).getAsJsonObject();
            isFinish = obj.get("payload").getAsJsonObject().get("final_result").getAsBoolean();
            result = obj.get("payload").getAsJsonObject().get("result").getAsString();
            sessionIdStr = obj.get("header").getAsJsonObject().get("session").getAsJsonObject().get("session_id").getAsString();
            Logger.i(TAG, "onResopnse result " + result + " session id " + sessionId + ", isFinish " + isFinish + ", session str " + sessionIdStr);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (mListener != null) {
                mListener.onError(sessionId, -1, "");
            }

            return;
        }
        if (!isCurrentSpeechEnd()) {
            // 如果已经停止了，就不再下发not finish的ASR结果
            if (mListener != null) {
                mListener.onRecognize(sessionId, result, isFinish);
            }
        }

        if (isFinish) {
            setCurrentSpeechEnd(true);
        }
    }

    @Override
    public boolean onAudioOutput(final long sessionId, int index, byte[] audio, int length, boolean isEnd) {
        // 收到數據，開始發送
        AIRequest req = new AIRequest();
        ReqHeader header = new ReqHeader();

        header.device.serial_num = TVSDeviceConfig.getDSN();
        header.qua = TVSDeviceConfig.getQUA();

        req.header = header;

        AsrReqPayload payload = new AsrReqPayload();
        payload.voice_meta.compress = AUDIO_TYPE;
        payload.session_id = String.valueOf(sessionId);
        payload.index = index;
        payload.voice_finished = isEnd;

        if (length > 0) {
            payload.voice_base64 = Base64Util.encode(audio, length);
        }

        req.payload = payload;

        Gson gson = new Gson();
        String audioJson = gson.toJson(req);

        Logger.i(TAG, audioJson);

        httpManager.sendASRRequest(audioJson, new TVSHttpManager.ITVSHttpManagerCallback() {
            @Override
            public void onError(int errorCode) {
                if (!mAudioProvider.isCurrentSession(sessionId)) {
                    // 并非当前的session id，不通知外层
                    Logger.i(TAG, "onError, but seesion id is out-of-date");
                    return;
                }

                Logger.i(TAG, "onError " + errorCode + " session " + sessionId);
                if (mListener != null) {
                    mListener.onError(sessionId, errorCode, "");
                }
            }

            @Override
            public void onResponse(int responseCode, String responseBody) {
                if (!mAudioProvider.isCurrentSession(sessionId)) {
                    // 并非当前的session id，不通知外层
                    Logger.i(TAG, "onResopnse, but seesion id is out-of-date");
                    return;
                }

                if (responseCode == 200) {
                    onRecvResponse(sessionId, responseBody);
                    return;
                }

                Logger.i(TAG, "onResopnse Error " + responseCode + " session " + sessionId);

                if (mListener != null) {
                    mListener.onError(sessionId, responseCode, "");
                }
            }
        });
        return true;
    }
}
