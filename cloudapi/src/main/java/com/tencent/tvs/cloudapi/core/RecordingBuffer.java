package com.tencent.tvs.cloudapi.core;

/**
 * Created by sapphireqin on 2019/11/26.
 */

public class RecordingBuffer {
    private int bitrate;
    private int channel;
    private RingBuffer encodingBuffer;
    private boolean writerFinish = false;

    private boolean encodingFinish = false;
    private RingBuffer sendingBuffer;

    private volatile boolean cancel = false;

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public RingBuffer getEncodingBuffer() {
        return encodingBuffer;
    }

    public void setEncodingBuffer(RingBuffer encodingBuffer) {
        this.encodingBuffer = encodingBuffer;
    }

    public boolean isWriterFinish() {
        return writerFinish;
    }

    public void setWriterFinish(boolean writerFinish) {
        this.writerFinish = writerFinish;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public RingBuffer getSendingBuffer() {
        return sendingBuffer;
    }

    public void setSendingBuffer(RingBuffer sendingBuffer) {
        this.sendingBuffer = sendingBuffer;
    }

    public boolean isEncodingFinish() {
        return encodingFinish;
    }

    public void setEncodingFinish(boolean encodingFinish) {
        this.encodingFinish = encodingFinish;
    }
}
