/**
 * Copyright 2019 bejson.com
 */
package com.tencent.tvs.cloudapi.bean.tvsrequest;

public class TVSHeader {

    private String namespace = "";
    private String name = "";
    private String messageId;
    private String dialogRequestId;

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setDialogRequestId(String dialogRequestId) {
        this.dialogRequestId = dialogRequestId;
    }

    public String getDialogRequestId() {
        return dialogRequestId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(" name:");
        builder.append(name);
        builder.append(" namespace:");
        builder.append(namespace);
        builder.append(" messageId: ");
        builder.append(messageId);
        builder.append(" dialogRequestId: ");
        builder.append(dialogRequestId);
        return builder.toString();
    }
}