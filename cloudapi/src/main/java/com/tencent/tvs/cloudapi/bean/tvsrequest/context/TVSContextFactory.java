package com.tencent.tvs.cloudapi.bean.tvsrequest.context;

import java.util.List;
import java.util.Map;

/**
 * 上下文工厂
 * Created by sapphireqin on 2019/11/29.
 */

public class TVSContextFactory {

    // UI上下文
    public static TvsUserInterface.ShowState createShowStateContext(boolean enable) {
        TvsUserInterface.ShowState showStateContext = new TvsUserInterface.ShowState();
        showStateContext.setEnable(true);
        return showStateContext;
    }

    // 自定义上下文
    public static TvsCustomData.State createTvsCustomDataContext(Map<String, String> stateList) {
        TvsCustomData.State customDataContext = new TvsCustomData.State();
        customDataContext.setCurrentState(stateList);
        return customDataContext;
    }
}
