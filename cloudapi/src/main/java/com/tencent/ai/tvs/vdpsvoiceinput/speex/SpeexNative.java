package com.tencent.ai.tvs.vdpsvoiceinput.speex;

public class SpeexNative {
    static {
        System.loadLibrary("dpps");
    }

    public native long speexEncodeInit();

    public native int speexEncode(long handle, byte[] data, int len, byte[] out);

    public native int speexEncodeRelease(long handle);

    public native long speexDecodeInit();

    public native int speexDecode(long handle, byte[] data, int len, byte[] out);

    public native int speexDecodeRelease(long handle);
}
