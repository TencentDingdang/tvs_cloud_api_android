package com.tencent.tvs.testapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;

/**
 * 录音接口的默认实现demo
 */
public class VoiceRecord {
    private String TAG;
    private AudioRecord mAudioRecord = null;
    private RecordingRunnable mRecordThread;
    private Thread mThread = null;

    public VoiceRecord() {
        TAG = "sap.VoiceRecord" + this.hashCode();
    }

    /**
     * 读取音频数据的同步对象
     */
    protected Object syncObj = new Object();
    private ArrayList<IAudioRecordListener> mListeners = new ArrayList<>();

    /**
     * 开始录音
     */
    public synchronized void startRecord(long sessionId) {
        Log.d(TAG, "startRecord()...");
        if (mRecordThread != null && mThread != null) {
            Log.d(TAG, "VoiceRecord is Started !");
            return;
        }

        try {
            if (mRecordThread == null) {
                mRecordThread = new RecordingRunnable(sessionId);
            }
            if (mThread == null) {
                mThread = new Thread(mRecordThread);
            }
            mThread.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            for (IAudioRecordListener listener : mListeners) {
                listener.onRecordingFailed(sessionId);
            }
            mThread = null;
            mRecordThread = null;
            return;
        }
    }

    /**
     * 停止录音
     */
    public synchronized void stopRecord() {
        Log.d(TAG, "stopRecord()...");
        if (mRecordThread == null && mThread == null) {
            Log.d(TAG, "VoiceRecord is Stoped !");
            return;
        }
        if (mRecordThread != null) {
            mRecordThread.stop();
        }
        mThread = null;
        mRecordThread = null;
    }

    public void addAudioRecordListener(IAudioRecordListener recordListener) {
        if (!mListeners.contains(recordListener)) mListeners.add(recordListener);
    }

    public void removeAudioRecordListener(IAudioRecordListener recordListener) {
        mListeners.remove(recordListener);
    }

    /**
     * 录音线程
     */
    private class RecordingRunnable implements Runnable {
        public long session;

        public RecordingRunnable(long session) {
            this.session = session;
        }

        /**
         * 是否结束识别
         */
        private volatile boolean mIsEnd = false;
        /**
         * 是否退出录音线程
         */
        private boolean mIsExit = false;
        /**
         * 录音线程buffer
         */
        int mRecordBufferSize = 0;

        private final int READ_FRAME_SIZE = 2048;

        private boolean init() {
            int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
            int audioEncodingBits = AudioFormat.ENCODING_PCM_16BIT;
            int sampleRate = 16000;
            try {
                mRecordBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfiguration, audioEncodingBits);
                Log.d(TAG, "mRecordBufferSize :" + mRecordBufferSize);
                if (mRecordBufferSize < 0) {
                    for (IAudioRecordListener listener : mListeners) {
                        listener.onRecordCreateError(session);
                    }
                    return false;
                } else if (mAudioRecord == null) {
                    if (mRecordBufferSize < READ_FRAME_SIZE) {
                        mRecordBufferSize = READ_FRAME_SIZE;
                    }
                    mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                            channelConfiguration, audioEncodingBits, mRecordBufferSize);
                }

                if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    for (IAudioRecordListener listener : mListeners) {
                        listener.onRecordCreateError(session);
                    }
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                for (IAudioRecordListener listener : mListeners) {
                    listener.onRecordCreateError(session);
                }
                return false;
            }
            Log.d(TAG, "init Recording");
            return true;
        }

        private boolean startup() {
            synchronized (syncObj) {
                mIsEnd = false;
            }

            mIsExit = false;
            long bfs = System.currentTimeMillis();
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                try {
                    Log.d(TAG, "start Recording");
                    mAudioRecord.startRecording();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Log.d(TAG, "start Recording failed");
                    for (IAudioRecordListener listener : mListeners) {
                        listener.onRecordCreateError(session);
                    }
                    return false;
                }
            } else {
                for (IAudioRecordListener listener : mListeners) {
                    listener.onRecordCreateError(session);
                }
                return false;
            }
            long afs = System.currentTimeMillis();
            Log.d(TAG, "start recording deltaTime = " + (afs - bfs));
            return true;
        }

        public void stop() {
            synchronized (syncObj) {
                mIsEnd = true;
            }
            
            if (mThread != null) {
                try {
                    mThread.join(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            if (!init()) {
                return;
            }

            try {
                // 每次读取音频数据大小
                byte[] pcmBuffer = new byte[mRecordBufferSize];
                Log.d(TAG, "mRecordBufferSize: " + mRecordBufferSize + ", thread id : " + Thread.currentThread().getId());
                // 实际读取音频数据大小
                int pcmBufferSize;
                if (startup()) {
                    for (IAudioRecordListener listener : mListeners) {
                        listener.onRecordingStart(session);
                    }
                    while (!mIsExit) {
                        if (null != mAudioRecord) {
                            pcmBufferSize = mAudioRecord.read(pcmBuffer, 0, mRecordBufferSize);
                            if (pcmBufferSize == AudioRecord.ERROR_INVALID_OPERATION) {
                                throw new IllegalStateException(
                                        "read() returned AudioRecord.ERROR_INVALID_OPERATION");
                            } else if (pcmBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                                throw new IllegalStateException(
                                        "read() returned AudioRecord.ERROR_BAD_VALUE");
                            }

                            for (IAudioRecordListener listener : mListeners) {
                                listener.onRecording(session, pcmBuffer, pcmBufferSize);
                            }

                            boolean isEndFlag = false;
                            synchronized (syncObj) {
                                isEndFlag = mIsEnd;
                            }
                            if (isEndFlag) {
                                mIsExit = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            //临时的修改方案，避免录音的抢占
            if (mAudioRecord != null) {
                if (AudioRecord.STATE_INITIALIZED == mAudioRecord.getState()) {
                    try {
                        mAudioRecord.stop();
                        mAudioRecord.release();
                        mAudioRecord = null;
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (IAudioRecordListener listener : mListeners) {
                listener.onRecordingEnd(session);
            }

            Log.d(TAG, "RecordingRuannable is exit, session " + session);
        }
    }
}
