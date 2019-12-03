/**
 * Copyright 2019 bejson.com
 */
package com.tencent.tvs.cloudapi.bean.tvsrequest.event;

/**
 * 文本解析事件，向后台发送该事件，对文本进行解析，返回Directives
 */
public class TvsTextRecognizer {
    private static final String nameSpace = "TvsTextRecognizer";

    public static class Recognize extends TVSEvent {
        private static final String name = "Recognize";
        private Payload payload = new Payload();

        public static class Payload {

            private String text;

            public void setText(String text) {
                this.text = text;
            }

            public String getText() {
                return text;
            }

        }

        public Recognize() {
            super(name, nameSpace);
        }

        public void setPayload(Payload payload) {
            this.payload = payload;
        }

        public Payload getPayload() {
            return payload;
        }
    }


}