package com.tencent.tvs.cloudapi.bean.tvsrequest.event;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public class TVSEventFactory {
    /**
     * 创建语义理解事件
     *
     * @param text    语义理解的目标字符串
     * @param session 语义理解的会话ID
     * @return
     */
    public static TVSEvent createTvsTextRecognizer(String text, long session) {
        TvsTextRecognizer.Recognize body = new TvsTextRecognizer.Recognize();
        body.getPayload().setText(text);
        body.getHeader().setDialogRequestId(String.valueOf(session));
        body.getHeader().setMessageId(String.valueOf(session));
        return body;
    }
}
