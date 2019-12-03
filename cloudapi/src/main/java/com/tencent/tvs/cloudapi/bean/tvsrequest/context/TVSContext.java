package com.tencent.tvs.cloudapi.bean.tvsrequest.context;

import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSHeader;

/**
 * Created by sapphireqin on 2019/11/28.
 */

public class TVSContext {
    public TVSHeader header;
    public Object payload;

    public TVSContext(String name, String nameSpace) {
        header = new TVSHeader();
        header.setName(name);
        header.setNamespace(nameSpace);
    }
}
