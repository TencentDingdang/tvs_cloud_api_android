package com.tencent.tvs.cloudapi.bean.tvsrequest.context;

import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSHeader;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public class TvsUserInterface {
    private static final String nameSpace = "TvsUserInterface";

    public static class ShowState extends TVSContext {
        private static final String name = "ShowState";

        public static class Payload {
            public boolean isEnable;
        }

        public ShowState() {
            super(name, nameSpace);
            payload = new Payload();
        }

        public void setEnable(boolean enable) {
            ((Payload) payload).isEnable = enable;
        }
    }
}
