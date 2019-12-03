package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

/**
 * Created by sapphireqin on 2019/11/29.
 */

public class TVSDirectivesStaus {
    public int code;
    public String msg;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" code: ");
        builder.append(code);
        builder.append(" msg: ");
        builder.append(msg);
        return builder.toString();
    }
}
