package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

/**
 * 后台下发的语音合成（TTS）指令
 * Created by sapphireqin on 2019/11/28.
 */

public class SpeechSynthesizer {
    private static final String nameSpace = "SpeechSynthesizer";

    /**
     * TTS指令
     */
    public static class SpeakText extends TVSDirectives {
        private static final String name = "SpeakText";
        public Payload payload;

        @Override
        public TVSDirectivesPayload getPayload() {
            return payload;
        }

        public static class Payload extends TVSDirectivesPayload {
            /**
             * TTS文本，可以使用这个文本申请TTS
             */
            public String text;
            /**
             * TTS token
             */
            public String token;
            /**
             * TTS流媒体链接，可以直接播放，如果不支持https流媒体播放器，使用text属性申请TTS;
             * 注意，这个url的有效期只有几分钟，一般TTS的实时性比较高，
             */
            public String url;
            /**
             * TTS属性
             */
            public String ttsParam;

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append(" text: ");
                builder.append(text);
                builder.append(" token: ");
                builder.append(token);
                builder.append(" url: ");
                builder.append(url);
                builder.append(" ttsParam: ");
                builder.append(ttsParam);
                return builder.toString();
            }
        }

        public SpeakText() {
            super(name, nameSpace);
        }
    }
}
