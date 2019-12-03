package com.tencent.tvs.cloudapi.core;

/**
 * Created by sapphireqin on 2019/11/26.
 */

public class RingBuffer {
    private byte[] array;
    private int readPos;
    private int writePos;

    public RingBuffer(int capacity) {
        array = new byte[capacity];
    }

    public void clear() {
        readPos = writePos = 0;
    }

    public int remaining() {
        if (readPos <= writePos) {
            return array.length - (writePos - readPos);
        } else {
            return readPos - writePos;
        }
    }

    public int readable() {
        return array.length - remaining();
    }

    private int reCapacity(int need) {
        if (need <= array.length) {
            return array.length;
        }
        int newCap = array.length;
        while (newCap < need) {
            newCap *= 2;
        }
        byte[] temp = new byte[newCap];
        if (readPos == writePos) {
            readPos = 0;
            writePos = 0;
            array = temp;
        } else if (readPos < writePos) {
            int length = readable();
            System.arraycopy(array, readPos, temp, 0, length);
            readPos = 0;
            writePos = readPos + length;
            array = temp;
        } else {
            int length = array.length - readPos;
            System.arraycopy(array, readPos, temp, 0, length);
            System.arraycopy(array, 0, temp, length, writePos);
            writePos = length + writePos;
            readPos = 0;
            array = temp;
        }
        return array.length;
    }

    public int write(byte[] data) {
        return write(data, 0, data.length);
    }

    public int write(byte[] data, int offset, int length) {
        if (data == null || data.length == 0 || length == 0) return 0;
        if (offset < 0 || length < 0 || (offset + length > data.length)) {
            throw new IllegalArgumentException(String.format("params error , offset = %s,length = %s,data.length = %s", offset, length, data.length));
        }
        int remaining = remaining();
        if (length > remaining) {
            reCapacity(array.length + (length - remaining));
        }
        if (readPos <= writePos) {
            int wrote = Math.min(array.length - writePos, length);
            System.arraycopy(data, offset, array, writePos, wrote);
            writePos += wrote;
            if (length > wrote) {
                System.arraycopy(data, offset + wrote, array, 0, length - wrote);
                writePos = (length - wrote);
            }
        } else {
            System.arraycopy(data, offset, array, writePos, length);
            writePos += length;
        }
        return length;
    }

    public int peek(byte[] buff) {
        return peek(buff, 0, buff.length);
    }

    public int peek(byte[] buff, int offset, int length) {
        return read(buff, offset, length, true);
    }

    public int read(byte[] buff) {
        return read(buff, 0, buff.length, false);
    }

    public int read(byte[] buff, int offset, int length, boolean peek) {
        if (buff == null || buff.length == 0 || length == 0) return 0;
        if (offset < 0 || length < 0 || offset >= buff.length) {
            throw new IllegalArgumentException(String.format("params error , offset = %s,length = %s,buff.length = %s", offset, length, buff.length));
        }
        length = Math.min(readable(), length);
        length = Math.min(buff.length - offset, length);
        if (length > 0) {
            if (readPos <= writePos) {
                System.arraycopy(array, readPos, buff, offset, length);
                if (!peek) {
                    readPos += length;
                }
            } else {
                int read = Math.min(array.length - readPos, length);
                System.arraycopy(array, readPos, buff, offset, read);
                if (!peek) {
                    readPos += read;
                }
                if (read < length) {
                    System.arraycopy(array, 0, buff, offset + read, length - read);
                    if (!peek) {
                        readPos = (length - read);
                    }
                }
            }
        }
        return length;
    }

    public String dump() {
        String msg = "array.length = " + array.length + ",";
        msg += ("readPos = " + readPos + ",");
        msg += ("writePos = " + writePos + ",");
        msg += ("readable = " + readable() + ",");
        msg += ("remaining = " + remaining());
        return msg;
    }

}
