package com.tencent.tvs.cloudapi.tools;

import android.util.Base64;

import java.io.UnsupportedEncodingException;


public class Base64Util {

    public static String encode(byte[] binaryData) {
        try {

            return new String(Base64.encode(binaryData, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static String encode(byte[] binaryData, int length) {
        try {

            return new String(Base64.encode(binaryData, 0, length, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] decode(String base64String) {
        try {
            return Base64.decode(base64String.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


}
