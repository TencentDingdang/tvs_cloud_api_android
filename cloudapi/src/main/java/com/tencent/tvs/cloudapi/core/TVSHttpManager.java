package com.tencent.tvs.cloudapi.core;

import android.util.Log;

import com.tencent.tvs.cloudapi.tools.SignatureTool;

import org.w3c.dom.ProcessingInstruction;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by sapphireqin on 2019/11/26.
 */

public class TVSHttpManager {
    private static final String TAG = "sap.TVSHttpManager";
    private static final String eventUrl = "https://aiwx.html5.qq.com/api/v2/event";
    private static final String asrUrl = "https://aiwx.html5.qq.com/api/asr";

    private static final long TIMEOUT_READ = 10 * 1000L;
    private static final long TIMEOUT_WRITE = 10 * 1000L;
    private static final long TIMEOUT_CONNECT = 10 * 1000L;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse(SignatureTool.CONTENT_TYPE_JSON);

    private OkHttpClient mHttpClient;

    public interface ITVSHttpManagerCallback {
        void onError(int errorCode);

        void onResponse(int responseCode, String responseBody);
    }

    public int init() {
        OkHttpClient.Builder endPointerBuilder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS)
                .writeTimeout(TIMEOUT_WRITE, TimeUnit.MILLISECONDS)
                .connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS);

        mHttpClient = endPointerBuilder.build();
        return 0;
    }

    public boolean sendPostAsync(String url, String jsonBody, HashMap<String, String> headerMap, final ITVSHttpManagerCallback callback) {
        //请求body
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, jsonBody);
        //请求header的添加
        Headers.Builder headerBuilder = new Headers.Builder();
        for (String key : headerMap.keySet()) {
            headerBuilder.add(key, headerMap.get(key));
        }

        Headers headers = headerBuilder.build();

        //请求组合创建
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .headers(headers)
                .build();//发起请求

        mHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onError(-1);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    callback.onResponse(response.code(), response.body().string());
                }
            }
        });

        return true;
    }

    private HashMap<String, String> getCustomHeaders(String body) {
        HashMap<String, String> customHeaders = new HashMap<>();
        String authorization = null;
        try {
            authorization = SignatureTool.getAuthorization(TVSDeviceConfig.APPKEY, TVSDeviceConfig.ACCESS_TOKEN, body);
        } catch (Exception e) {
            e.printStackTrace();
            return customHeaders;
        }

        Log.i(TAG, authorization);
        String contentType = SignatureTool.getContentType();


        customHeaders.put("Authorization", authorization);
        customHeaders.put("Content-Type", contentType);
        return customHeaders;
    }

    public void sendEvent(String body, ITVSHttpManagerCallback callback) throws Exception {
        HashMap<String, String> customHeaders = getCustomHeaders(body);

        sendPostAsync(eventUrl, body, customHeaders, callback);
    }

    public void sendASRRequest(String body, ITVSHttpManagerCallback callback) {
        HashMap<String, String> customHeaders = getCustomHeaders(body);

        sendPostAsync(asrUrl, body, customHeaders, callback);
    }
}
