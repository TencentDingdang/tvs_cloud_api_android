package com.tencent.tvs.cloudapi.core;

import android.util.Log;

import com.tencent.ai.tvs.vdpsvoiceinput.speex.SpeexNative;
import com.tencent.tvs.cloudapi.tools.Logger;

/**
 * 语音提供者，供外部输入PCM，输出Speex编码后的语音数据
 * Created by sapphireqin on 2019/11/26.
 */
public class TVSAudioProvider {
    private static final String TAG = "TVSAudioProvider";

    static final int RINGBUFFER_CAPACITY = 4;

    // 每次发送时从buffer中读取的speex数据 -- 最小值，不足这个最小值，将会等待下次再读取
    // 调整这个大小可以改善ASR的速度
    static final int SENDING_PACKAGE_MIN = 256;
    // 每次发送时从buffer中读取的speex数据 -- 最大值，每次最多读取该大小的数据
    static final int SENDING_PACKAGE_MAX = 1024 * 8;

    // 当前正在进行的语音识别会话的ID
    private long currentSession = 0;
    private RecordingBuffer mCurrentBuffer;

    private int totalWrite = 0;
    private int totalEnc = 0;

    private SpeexNative mSpeexCore;
    private long speecHandle;
    byte[] encodingBuffer;

    Thread mSendingThread;

    ITVSAudioProviderCallback mAudioCallback;

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
        int index = 0;

        SendingRunnable(long sessionId, RecordingBuffer recordingBuffer) {
            this.sessionId = sessionId;
            this.recordingBuffer = recordingBuffer;
            byteBuffer = new byte[SENDING_PACKAGE_MAX];
        }

        @Override
        public void run() {
            while (true) {
                if (!loop()) {
                    // 发送停止, 清除数据
                    synchronized (TVSAudioProvider.this) {
                        dolog("[SEND]end session " + sessionId + ", total write " + totalWrite
                                + " bytes, total speex " + totalEnc + " bytes, total send " + totalSend + " bytes");
                    }
                    return;
                }
            }
        }

        private boolean send(long sessionId, byte[] audio, int length, boolean isEnd) {
            dolog("[SEND]send session " + sessionId + ", " + length + " bytes");
            index = totalSend;
            totalSend += length;

            if (mAudioCallback != null) {
                boolean end = !mAudioCallback.onAudioOutput(sessionId, index++, audio, length, isEnd);

                if (end) {
                    dolog("[SEND]send is finish by callback Object");
                    synchronized (TVSAudioProvider.this) {
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
            int minSize = SENDING_PACKAGE_MIN;
            synchronized (TVSAudioProvider.this) {
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
                boolean finish = recordingBuffer.isWriterFinish();

                if (size < minSize) {
                    // 数据不够最小值
                    if (finish) {
                        // 生产者已经停止，读取最后一点数据并发送
                        readSize = dataBuffer.read(byteBuffer);
                        isEnd = true;
                    } else {
                        // 还在写数据，等待
                        doWait = true;
                    }
                } else {
                    // 数据够最小值，有多少读多少，但是要小于bytebuffer
                    readSize = dataBuffer.read(byteBuffer);
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

    public synchronized boolean isCurrentSession(long session) {
        return currentSession == session;
    }

    private boolean encode(long sessionId, byte[] data, int length, RingBuffer sendingBuffer) {
        int encSize = 0;
        if (speecHandle != 0) {
            int bufferMax = length / 3;
            if (encodingBuffer == null || encodingBuffer.length < bufferMax) {
                encodingBuffer = new byte[bufferMax];
            }
            encSize = mSpeexCore.speexEncode(speecHandle, data, length, encodingBuffer);
        }

        dolog("[ENC]encode session " + sessionId + ", " + length + " bytes PCM to Speex " + encSize + " bytes");

        if (encSize <= 0) {
            dolog("[ENC]Encoder is error");
            return false;
        }

        totalEnc += encSize;

        sendingBuffer.write(encodingBuffer, 0, encSize);

        return true;
    }

    public synchronized int init() {
        mSpeexCore = new SpeexNative();
        return 0;
    }

    public void release() {
        cancel();

        // release的时候，等待并保证线程退出
        waitThreadStop(mSendingThread, 3000);

        mSendingThread = null;
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

    public int start(long sessionId, int bitrate, int channel) {
        dolog("start session " + sessionId);
        // 取消当前的识别流程
        cancel();

        synchronized (this) {
            currentSession = sessionId;

            RecordingBuffer recordingBuffer = new RecordingBuffer();

            // 用于存放编码后待发送的speex数据的buffer
            RingBuffer buffer = new RingBuffer(bitrate * 2 * RINGBUFFER_CAPACITY);

            recordingBuffer.setBitrate(bitrate);
            recordingBuffer.setChannel(channel);
            recordingBuffer.setSendingBuffer(buffer);

            recordingBuffer.setWriterFinish(false);

            mCurrentBuffer = recordingBuffer;

            speecHandle = mSpeexCore.speexEncodeInit();

            mSendingThread = new Thread(new SendingRunnable(sessionId, recordingBuffer));
            mSendingThread.start();

            totalEnc = 0;
            totalWrite = 0;
        }
        return 0;
    }

    public void cancel() {
        synchronized (this) {
            dolog("cancel session " + currentSession);
            RecordingBuffer recordingBuffer = mCurrentBuffer;
            if (recordingBuffer != null) {
                // 取消正在进行的识别，发送线程将结束，但是沒有必要阻塞等待线程退出
                recordingBuffer.setCancel(true);
            }

            totalWrite = 0;

            if (speecHandle != 0) {
                mSpeexCore.speexEncodeRelease(speecHandle);
                speecHandle = 0;
            }

            mCurrentBuffer = null;
            currentSession = 0;
        }
    }

    public synchronized int writeAudio(long sessionId, byte[] data, int length, boolean writeEnd) {
        if (currentSession != sessionId) {
            // session id对不上，不写入
            dolog("writeAudio ERROR! invalid session id " + sessionId);
            return -2;
        }

        RecordingBuffer recordingBuffer = mCurrentBuffer;
        if (recordingBuffer == null) {
            dolog("ERROR! invalid recording buffer");
            return -1;
        }

        totalWrite += length;

        if (length > 0) {
            RingBuffer dataBuffer = recordingBuffer.getSendingBuffer();

            encode(sessionId, data, length, dataBuffer);
        }

        if (writeEnd) {
            dolog("write finish!");
            recordingBuffer.setWriterFinish(true);
        }
        return length;
    }
}