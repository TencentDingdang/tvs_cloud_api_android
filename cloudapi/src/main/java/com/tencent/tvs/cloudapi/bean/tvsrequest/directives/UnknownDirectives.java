package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public class UnknownDirectives extends TVSDirectives {
    public Payload payload;

    public UnknownDirectives() {
        super("", "");
    }

    public static class Payload extends TVSDirectivesPayload {
        public String payloadStr;

        @Override
        public String toString() {
            return "Unkonwn Directives: " + payloadStr;
        }
    }

    @Override
    public TVSDirectivesPayload getPayload() {
        return payload;
    }

    /**
     * 获取JSON
     *
     * @return 未知指令对应的json
     */
    public String getContent() {
        if (payload == null) {
            return null;
        }

        return payload.payloadStr;
    }
}
