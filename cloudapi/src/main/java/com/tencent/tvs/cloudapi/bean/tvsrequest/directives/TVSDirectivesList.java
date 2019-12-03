package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

import java.util.List;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public class TVSDirectivesList {
    public TVSDirectivesStaus status;
    public List<TVSDirectives> directivesArray;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(status);
        builder.append("]");

        builder.append(directivesArray);

        return builder.toString();
    }
}
