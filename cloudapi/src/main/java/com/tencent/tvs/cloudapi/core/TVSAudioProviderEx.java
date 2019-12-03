package com.tencent.tvs.cloudapi.core;

import android.util.Log;

import com.tencent.ai.tvs.vdpsvoiceinput.speex.SpeexNative;
import com.tencent.tvs.cloudapi.tools.Logger;

/**
 * 暂时不用
 * Created by sapphireqin on 2019/11/26.
 */

public class TVSAudioProviderEx {
    private static final String TAG = "TVSAudioProviderEx";
    // 单位为秒
    static final int RINGBUFFER_CAPACITY = 4;

    // 每次解码时从buffer中读取的PCM数据
    static final int ENCODING_DATA_PER_PACKAGE = 2 * 1024;

    // 每次发送时从buffer中读取的speex数据
    static final int SENDING_DATA_PER_PACKAGE = 512;

    private long currentSession = 0;
    private RecordingBuffer mCurrentBuffer;

    private int totalWrite = 0;

    private SpeexNative mSpeexCore;

    Thread mEncoderThread;

    Thread mSendingThread;

    ITVSAudioProviderCallback mAudioCallback;
    int index = 0;

    private void dolog(String log) {
        Logger.i(TAG, log);
    }

    private void doSleep(int mills) {
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
        }
    }

    public void setAduioCallback(ITVSAudioProviderCallback cb) {
        mAudioCallback = cb;
    }

    /**
     * 读取speex进行发送的线程
     */
    private class SendingRunnable implements Runnable {
        long sessionId;
        byte[] byteBuffer;
        RecordingBuffer recordingBuffer;
        int totalSend = 0;

        SendingRunnable(long sessionId, RecordingBuffer recordingBuffer) {
            this.sessionId = sessionId;
            this.recordingBuffer = recordingBuffer;
            byteBuffer = new byte[SENDING_DATA_PER_PACKAGE];
        }

        @Override
        public void run() {
            while (true) {
                if (!loop()) {
                    // 发送停止, 清除数据
                    synchronized (TVSAudioProviderEx.this) {
                        dolog("[SEND]remove send session " + sessionId + ", total send " + totalSend + " bytes");
                    }
                    return;
                }
            }
        }

        private boolean send(long sessionId, byte[] audio, int length, boolean isEnd) {
            dolog("[SEND]send session " + sessionId + ", " + length + " bytes");
            totalSend += length;

            if (mAudioCallback != null) {
                boolean end = !mAudioCallback.onAudioOutput(sessionId, index++, audio, length, isEnd);

                if (end) {
                    dolog("[SEND]send is finish by callback Object");
                    synchronized (TVSAudioProviderEx.this) {
                        recordingBuffer.setCancel(true);
                    }
                }
            }
            return true;
        }

        private boolean loop() {
            int readSize = 0;
            boolean isEnd = false;
            boolean doWait = false;
            synchronized (TVSAudioProviderEx.this) {
                if (recordingBuffer == null) {
                    dolog("[SEND]ERROR! invalid recording buffer");
                    return false;
                }

                if (recordingBuffer.isCancel()) {
                    dolog("[SEND]session " + sessionId + " is finished");
                    return false;
                }

                RingBuffer dataBuffer = recordingBuffer.getSendingBuffer();
                int size = dataBuffer.readable();
                boolean finish = recordingBuffer.isEncodingFinish();
                int bufsize = byteBuffer.length;

                if (finish && size < bufsize) {
                    // 最后一点数据
                    readSize = dataBuffer.read(byteBuffer);
                    isEnd = true;
                } else if (size >= bufsize) {
                    // 数据足够，直接读取并发送
                    readSize = dataBuffer.read(byteBuffer);
                } else {
                    // 数据不足，且编码未结束，需要等待数据
                    doWait = true;
                }
            }

            if (doWait) {
                doSleep(100);
            } else if (!send(sessionId, byteBuffer, readSize, isEnd)) {
                // 如果send失败，则退出循环
                return false;
            }

            if (isEnd) {
                // 结束，直接退出循环
                return false;
            }

            return true;
        }
    }

    /**
     * 进行语音编码的线程
     */
    private class EncoderRunnable implements Runnable {
        long sessionId;
        byte[] byteBuffer;
        RecordingBuffer recordingBuffer;
        long speecHandle;
        byte[] encodingBuffer;
        int totalEnc = 0;
        int totalResult = 0;

        EncoderRunnable(long sessionId, RecordingBuffer recordingBuffer) {
            this.sessionId = sessionId;
            this.recordingBuffer = recordingBuffer;
            byteBuffer = new byte[ENCODING_DATA_PER_PACKAGE];

            speecHandle = mSpeexCore.speexEncodeInit();
        }

        private boolean encode(byte[] data, int length) {
            int encSize = 0;
            if (speecHandle != 0) {
                int bufferMax = length / 3;
                if (encodingBuffer == null || encodingBuffer.length < bufferMax) {
                    encodingBuffer = new byte[bufferMax];
                }
                encSize = mSpeexCore.speexEncode(speecHandle, data, length, encodingBuffer);
            }

            totalEnc += length;
            totalResult += encSize;

            dolog("[ENC]encode session " + sessionId + ", " + length + " bytes PCM to Speex " + encSize + " bytes");

            if (encSize <= 0) {
                dolog("[ENC]Encoder is error");
                return false;
            }

            recordingBuffer.getSendingBuffer().write(encodingBuffer, 0, encSize);

            return true;
        }

        @Override
        public void run() {
            while (true) {
                if (!loop()) {
                    // 发送停止, 清除数据
                    mSpeexCore.speexEncodeRelease(speecHandle);
                    synchronized (TVSAudioProviderEx.this) {
                        recordingBuffer.setEncodingFinish(true);
                        dolog("[ENC]remove encode session " + sessionId + ", total encode " + totalEnc
                                + " bytes, total write " + totalWrite + " bytes, total speex " + totalResult + " bytes");
                    }
                    return;
                }
            }
        }

        private boolean loop() {
            int readSize = 0;
            boolean doWait = false;
            boolean isEnd = false;
            synchronized (TVSAudioProviderEx.this) {
                if (recordingBuffer == null) {
                    dolog("[ENC]ERROR! invalid session id " + sessionId);
                    return false;
                }

                if (recordingBuffer.isCancel()) {
                    dolog("[ENC]session " + sessionId + " is finished");
                    return false;
                }

                RingBuffer dataBuffer = recordingBuffer.getEncodingBuffer();
                int size = dataBuffer.readable();
                int bufsize = byteBuffer.length;
                boolean finish = recordingBuffer.isWriterFinish();

                if (finish && size < bufsize) {
                    // 最后一点数据
                    readSize = dataBuffer.read(byteBuffer);
                    isEnd = true;
                } else if (size >= bufsize) {
                    // 数据足够，直接读取并开始编码
                    readSize = dataBuffer.read(byteBuffer);
                } else {
                    // 数据不足，且写PCM未结束，需要等待数据
                    doWait = true;
                }
            }

            if (doWait) {
                doSleep(100);
            } else if (!encode(byteBuffer, readSize)) {
                // 如果編碼失敗，直接退出循環
                return false;
            }

            if (isEnd) {
                // 结束，直接退出循环
                return false;
            }

            return true;
        }
    }

    public synchronized int init() {
        mSpeexCore = new SpeexNative();
        return 0;
    }

    public int start(long sessionId, int bitrate, int channel) {
        dolog("start session " + sessionId);

        // 取消当前的识别流程
        cancel();

        synchronized (this) {
            currentSession = sessionId;

            RecordingBuffer recordingBuffer = new RecordingBuffer();

            // 用于存放待编码的PCM数据的buffer
            RingBuffer buffer = new RingBuffer(bitrate * 2 * RINGBUFFER_CAPACITY);

            recordingBuffer.setBitrate(bitrate);
            recordingBuffer.setChannel(channel);
            recordingBuffer.setEncodingBuffer(buffer);

            // 用于存放编码后待发送的speex数据的buffer
            buffer = new RingBuffer(bitrate * 2 * RINGBUFFER_CAPACITY);

            recordingBuffer.setBitrate(bitrate);
            recordingBuffer.setChannel(channel);
            recordingBuffer.setSendingBuffer(buffer);

            recordingBuffer.setWriterFinish(false);

            mCurrentBuffer = recordingBuffer;

            mEncoderThread = new Thread(new EncoderRunnable(sessionId, recordingBuffer));
            mEncoderThread.start();

            mSendingThread = new Thread(new SendingRunnable(sessionId, recordingBuffer));
            mSendingThread.start();
        }
        return 0;
    }

    private void waitThreadStop(Thread thread, long time) {
        if (thread == null) {
            return;
        }

        try {
            thread.join(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        synchronized (this) {
            dolog("cancel session " + currentSession);
            RecordingBuffer recordingBuffer = mCurrentBuffer;
            if (recordingBuffer == null) {
                dolog("ERROR! invalid session id " + currentSession);
                return;
            }

            // 取消上一次识别，发送线程和编码线程都将结束
            recordingBuffer.setCancel(true);
            totalWrite = 0;
        }

        // 因为speex库不能多线程，需要将上一个线程停止
        waitThreadStop(mEncoderThread, 3000);
        // 没必要等待发送线程结束
        //waitThreadStop(mSendingThread, 3000);
    }

    public synchronized int writeAudio(long sessionId, byte[] data, int length, boolean writeEnd) {
        if (currentSession != sessionId) {
            dolog("ERROR! invalid session id " + sessionId);
            return -2;
        }

        RecordingBuffer recordingBuffer = mCurrentBuffer;
        if (recordingBuffer == null) {
            dolog("ERROR! invalid recording buffer");
            return -1;
        }

        RingBuffer dataBuffer = recordingBuffer.getEncodingBuffer();
        int ret = dataBuffer.write(data, 0, length);

        dolog("write session " + sessionId + ", " + ret + " bytes PCM to buffer ");

        totalWrite += ret;

        if (writeEnd) {
            dolog("write finish!");
            recordingBuffer.setWriterFinish(true);
        }
        return ret;
    }
}
