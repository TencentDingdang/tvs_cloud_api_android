package com.tencent.tvs.cloudapi.bean.tvsrequest.event;

import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSHeader;

/**
 * TVS事件
 * Created by sapphireqin on 2019/11/28.
 */

public class TVSEvent {
    TVSHeader header = new TVSHeader();

    public void setHeader(TVSHeader header) {
        this.header = header;
    }

    public TVSHeader getHeader() {
        return header;
    }

    public TVSEvent(String name, String nameSpace) {
        header.setName(name);
        header.setNamespace(nameSpace);
    }
}
