package com.tencent.tvs.testapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContext;
import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContextFactory;
import com.tencent.tvs.cloudapi.bean.tvsrequest.directives.AudioPlayer;
import com.tencent.tvs.cloudapi.bean.tvsrequest.directives.SpeechSynthesizer;
import com.tencent.tvs.cloudapi.bean.tvsrequest.directives.TVSDirectives;
import com.tencent.tvs.cloudapi.bean.tvsrequest.directives.TVSDirectivesList;
import com.tencent.tvs.cloudapi.bean.tvsrequest.directives.UnknownDirectives;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEvent;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEventFactory;
import com.tencent.tvs.cloudapi.core.TVSCoreAPI;
import com.tencent.tvs.cloudapi.core.api.ITVSCoreAPI;
import com.tencent.tvs.cloudapi.core.api.ITVSEventCallback;
import com.tencent.tvs.cloudapi.core.api.ITVSSpeechRecognizerListener;
import com.tencent.tvs.cloudapi.tools.Logger;
import com.tencent.tvs.cloudapi.tools.SessionIdGenerator;
import com.tencent.tvs.cloudapi.tools.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView resultView;
    private Button recordingButton;
    private Button sendNLPButton;
    private Button stopRecordingButton;
    private ScrollView mScrollView;

    private ITVSCoreAPI mCore;
    private VoiceRecord mVoiceRecord;

    private String mASRResult;

    private IAudioRecordListener recordListener;

    // 语音识别专用的session id
    private long sessionId;

    public synchronized long getCurrentSessionId() {
        return sessionId;
    }

    public synchronized long newSessionId() {
        sessionId = SessionIdGenerator.generateNewSessionId();
        return sessionId;
    }

    private void startSpeechRecognize() {
        setText("************开始录音************");
        stopSpeechRecognize();
        long session = newSessionId();
        mVoiceRecord.startRecord(session);
        mCore.getSpeechRecognizer().start(session);
    }

    private void stopSpeechRecognize() {
        mVoiceRecord.stopRecord();
        mCore.getSpeechRecognizer().cancel();
    }

    private void stopSpeechRecognize(long session) {
        long curSession = getCurrentSessionId();
        if (curSession == session) {
            stopSpeechRecognize();
        }
    }

    private void stopRecording(long session) {
        long curSession = getCurrentSessionId();
        if (curSession == session) {
            mVoiceRecord.stopRecord();
        }
    }

    private void setText(String resultText) {
        runOnUiThread(() -> {
            resultView.setText(resultView.getText() + "\n" + resultText);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void startPlayNetworkMedia(AudioPlayer.Play directives) {
        if (null == directives || directives.getPayload() == null) {
            return;
        }
        AudioPlayer.Play.Payload payload = (AudioPlayer.Play.Payload) directives.getPayload();
        if (null == payload || payload.getAudioItem() == null || payload.getAudioItem().getStream() == null) {
            return;
        }

        setText("开始播放媒体：" + payload.getAudioItem().getStream().getUrl());
    }

    private void onRecvUnknownDirectives(String json) {
        if (StringUtils.isEmpty(json)) {
            return;
        }

        // 未知指令，可以直接解析json
        Logger.e(TAG, "get unknown directives json " + json);
    }

    // 在这里处理所有的directives
    private void onRecvDirectives(TVSDirectives directives) {
        if (directives == null) {
            return;
        }

        if (directives instanceof SpeechSynthesizer.SpeakText) {
            // TTS指令
            SpeechSynthesizer.SpeakText dir = (SpeechSynthesizer.SpeakText) directives;
            Logger.e(TAG, dir.toString());
        } else if (directives instanceof AudioPlayer.Play) {
            // 播放指令
            AudioPlayer.Play dir = (AudioPlayer.Play) directives;

            startPlayNetworkMedia(dir);
        } else if (directives instanceof SpeechSynthesizer.SpeakText) {
            // 未知指令
            UnknownDirectives dir = (UnknownDirectives) directives;

            onRecvUnknownDirectives(dir.getContent());
        } else {
            // TO-DO，处理其他指令
        }
    }

    // 发送语义理解事件，通过上传文本，解析语义，返回TTS以及其他信息
    private void sendNLP(String text) {
        if (StringUtils.isEmpty(text)) {
            return;
        }

        // 创建语义理解的session id
        long sessionId = SessionIdGenerator.generateNewSessionId();

        List<TVSContext> contexts = new ArrayList<>();

        // 上下文，语义理解必须要UI上下文
        contexts.add(TVSContextFactory.createShowStateContext(true));

        // 自定义上下文
        Map<String, String> stateList = new HashMap<>();
        // TO-DO 增加自定义标识
        stateList.put("spotLabel", "spotContent");
        contexts.add(TVSContextFactory.createTvsCustomDataContext(stateList));

        // 创建语义理解Event
        TVSEvent event = TVSEventFactory.createTvsTextRecognizer(text, sessionId);

        // 发送Event，异步返回结果
        mCore.getEventManager().sendEvent(sessionId, event, contexts, new ITVSEventCallback() {

            @Override
            public int onReplay(long session, String directives) {
                // 解析出所有的指令
                TVSDirectivesList directivesList = TVSDirectives.parse(directives);
                if (directivesList.directivesArray != null) {
                    for (TVSDirectives dir : directivesList.directivesArray) {
                        // 处理每个指令
                        onRecvDirectives(dir);
                    }
                }

                setText("语义理解结果：" + directivesList);
                return 0;
            }

            @Override
            public void onError(long session, int errorCode, String reason) {
                setText("语义理解: ERROR!");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCore.release();
        mCore = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        resultView = findViewById(R.id.text_result);
        recordingButton = findViewById(R.id.recording);
        sendNLPButton = findViewById(R.id.textreco);
        stopRecordingButton = findViewById(R.id.stop_record);
        mScrollView = findViewById(R.id.scroller);

        recordingButton.setOnClickListener(v -> {
            // 开始语音识别

            startSpeechRecognize();
        });

        sendNLPButton.setOnClickListener(v -> {
            // 开始语义理解
            setText("************语义理解：" + mASRResult + "************");
            sendNLP(mASRResult);
        });

        stopRecordingButton.setOnClickListener(v -> {
            setText("************主动结束录音************");
            stopRecording(getCurrentSessionId());
        });

        mCore = new TVSCoreAPI();
        mCore.init();
        mCore.getSpeechRecognizer().setListener(new ITVSSpeechRecognizerListener() {
            @Override
            public int onRecognize(long session, String resultText, boolean isEnd) {
                if (!StringUtils.isEmpty(resultText)) {
                    setText("Result: " + resultText);
                }

                if (isEnd) {
                    mASRResult = resultText;
                    stopRecording(session);
                }
                return 0;
            }

            @Override
            public void onError(long session, int errorCode, String reason) {
                stopSpeechRecognize(session);
                setText("语音识别: ERROR!");
            }
        });

        recordListener = new IAudioRecordListener() {

            @Override
            public void onRecordingStart(long session) {
                Logger.i(TAG, "onRecordingStart " + session);
            }

            @Override
            public void onRecording(long session, byte[] data, int dataLen) {
                mCore.getSpeechRecognizer().writeAudio(session, data, dataLen, false);
            }

            @Override
            public void onRecordCreateError(long session) {
                Logger.i(TAG, "onRecordCreateError " + session);
                stopSpeechRecognize(session);
                setText("************录音出错************");
            }

            @Override
            public void onRecordingFailed(long session) {
                Logger.i(TAG, "onRecordingFailed " + session);
                stopSpeechRecognize(session);
                setText("************录音出错************");
            }

            @Override
            public void onRecordingEnd(long session) {
                // 录制结束，发送end标识
                Logger.i(TAG, "onRecordingEnd " + session);
                mCore.getSpeechRecognizer().writeAudio(session, null, 0, true);
                setText("************录音结束************");
            }
        };

        mVoiceRecord = new VoiceRecord();
        mVoiceRecord.addAudioRecordListener(recordListener);
    }
}
