/**
 * Copyright 2019 bejson.com
 */
package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

public class AudioPlayer {
    private static final String nameSpace = "AudioPlayer";

    /**
     * AudioPlayer - Play指令，语义识别到媒体播放事件，后台下发的媒体信息
     */
    public static class Play extends TVSDirectives {
        private static final String name = "Play";
        private Payload payload = new Payload();

        public Play() {
            super(name, nameSpace);
        }

        @Override
        public TVSDirectivesPayload getPayload() {
            return payload;
        }

        public static class AudioItem {

            private String audioItemId;
            // 媒体详情
            private Stream stream;

            public void setAudioItemId(String audioItemId) {
                this.audioItemId = audioItemId;
            }

            public String getAudioItemId() {
                return audioItemId;
            }

            public void setStream(Stream stream) {
                this.stream = stream;
            }

            public Stream getStream() {
                return stream;
            }

        }

        public static class ProgressReport {

            private int progressReportDelayInMilliseconds;
            private int progressReportIntervalInMilliseconds;

            public void setProgressReportDelayInMilliseconds(int progressReportDelayInMilliseconds) {
                this.progressReportDelayInMilliseconds = progressReportDelayInMilliseconds;
            }

            public int getProgressReportDelayInMilliseconds() {
                return progressReportDelayInMilliseconds;
            }

            public void setProgressReportIntervalInMilliseconds(int progressReportIntervalInMilliseconds) {
                this.progressReportIntervalInMilliseconds = progressReportIntervalInMilliseconds;
            }

            public int getProgressReportIntervalInMilliseconds() {
                return progressReportIntervalInMilliseconds;
            }

        }

        public static class Stream {

            // 媒体播放的起始位置
            private int offsetInMilliseconds;

            private ProgressReport progressReport;
            // 下发的媒体的唯一标识
            private String token;
            // 媒体Url，可以直接用流媒体播放器播放
            private String url;

            public void setOffsetInMilliseconds(int offsetInMilliseconds) {
                this.offsetInMilliseconds = offsetInMilliseconds;
            }

            public int getOffsetInMilliseconds() {
                return offsetInMilliseconds;
            }

            public void setProgressReport(ProgressReport progressReport) {
                this.progressReport = progressReport;
            }

            public ProgressReport getProgressReport() {
                return progressReport;
            }

            public void setToken(String token) {
                this.token = token;
            }

            public String getToken() {
                return token;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUrl() {
                return url;
            }

        }

        public static class TvsExtraInformation {

            private String domain;

            public void setDomain(String domain) {
                this.domain = domain;
            }

            public String getDomain() {
                return domain;
            }
        }

        public static class Payload extends TVSDirectivesPayload {
            // 媒体信息
            private AudioItem audioItem;
            private String dataType;
            private String playBehavior;
            private TvsExtraInformation tvsExtraInformation;
            private String tvsPlayerMode;
            private boolean tvsPlayerModeSwitch;

            public void setAudioItem(AudioItem audioItem) {
                this.audioItem = audioItem;
            }

            public AudioItem getAudioItem() {
                return audioItem;
            }

            public void setDataType(String dataType) {
                this.dataType = dataType;
            }

            public String getDataType() {
                return dataType;
            }

            public void setPlayBehavior(String playBehavior) {
                this.playBehavior = playBehavior;
            }

            public String getPlayBehavior() {
                return playBehavior;
            }

            public void setTvsExtraInformation(TvsExtraInformation tvsExtraInformation) {
                this.tvsExtraInformation = tvsExtraInformation;
            }

            public TvsExtraInformation getTvsExtraInformation() {
                return tvsExtraInformation;
            }

            public void setTvsPlayerMode(String tvsPlayerMode) {
                this.tvsPlayerMode = tvsPlayerMode;
            }

            public String getTvsPlayerMode() {
                return tvsPlayerMode;
            }

            public void setTvsPlayerModeSwitch(boolean tvsPlayerModeSwitch) {
                this.tvsPlayerModeSwitch = tvsPlayerModeSwitch;
            }

            public boolean getTvsPlayerModeSwitch() {
                return tvsPlayerModeSwitch;
            }

        }
    }
}