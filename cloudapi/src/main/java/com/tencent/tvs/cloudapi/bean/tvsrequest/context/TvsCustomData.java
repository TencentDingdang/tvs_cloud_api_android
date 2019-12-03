package com.tencent.tvs.cloudapi.bean.tvsrequest.context;

import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSHeader;

import java.util.List;
import java.util.Map;

/**
 * Created by sapphireqin on 2019/11/28.
 */

public class TvsCustomData {
    private static final String nameSpace = "TvsCustomData";

    public static class State extends TVSContext {
        private static final String name = "State";

        public static class Payload {
            public Map<String, String> currentState;
        }

        public State() {
            super(name, nameSpace);
            payload = new Payload();
        }

        public void setCurrentState(Map<String, String> state) {
            ((Payload) payload).currentState = state;
        }
    }
}
