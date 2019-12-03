package com.tencent.tvs.cloudapi.bean.tvsrequest.directives;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.tvs.cloudapi.bean.tvsrequest.TVSHeader;
import com.tencent.tvs.cloudapi.tools.Logger;
import com.tencent.tvs.cloudapi.tools.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sapphireqin on 2019/11/28.
 */

public abstract class TVSDirectives {
    public static final String TAG = "TVSDirectives";
    public TVSHeader header = new TVSHeader();

    public abstract TVSDirectivesPayload getPayload();

    public TVSDirectives(String name, String nameSpace) {
        header.setName(name);
        header.setNamespace(nameSpace);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(header);
        builder.append("]");

        builder.append("[");
        builder.append(getPayload());
        builder.append("]");

        return builder.toString();
    }

    public static List<TVSDirectives> tvsDirectivesList = new ArrayList<>();

    static {
        // TTS文本和url
        tvsDirectivesList.add(new SpeechSynthesizer.SpeakText());
        // 媒体播放
        tvsDirectivesList.add(new AudioPlayer.Play());
    }

    public static Class check(String nameSpace, String name) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(nameSpace)) {
            return null;
        }

        for (TVSDirectives directives : tvsDirectivesList) {
            if (null == directives || null == directives.header) {
                continue;
            }
            if (nameSpace.equals(directives.header.getNamespace()) && name.equals(directives.header.getName())) {
                // 找到了对应的TVSDirectives
                Logger.i(TAG, "header.name " + name + ", namespace" + nameSpace + ", class " + directives.getClass());
                return directives.getClass();
            }
        }

        return null;
    }

    private static JsonArray getDirectivesArray(String directives) {
        try {
            return new JsonParser().parse(directives).getAsJsonObject().get("directives").getAsJsonArray();
        } catch (Exception e) {
            Logger.e(TAG, "getDirectivesArray Error!", e);
            return null;
        }
    }

    private static TVSDirectivesStaus getStatus(Gson gson, String directives) {
        try {
            return gson.fromJson(new JsonParser().parse(directives).getAsJsonObject().get("status").toString(), TVSDirectivesStaus.class);
        } catch (Exception e) {
            Logger.e(TAG, "getStatus Error!", e);
            return null;
        }
    }

    private static TVSHeader getHeader(Gson gson, JsonArray array, int index) {
        try {
            JsonObject object = array.get(index).getAsJsonObject();
            String header = object.getAsJsonObject("header").getAsJsonObject().toString();

            TVSHeader headerObj = gson.fromJson(header, TVSHeader.class);

            return headerObj;

        } catch (Exception e) {
            Logger.e(TAG, "get headder error!", e);
            return null;
        }
    }

    private static String getPayload(JsonArray array, int index) {
        try {
            JsonObject object = array.get(index).getAsJsonObject();
            String payload = object.getAsJsonObject("payload").getAsJsonObject().toString();

            return payload;

        } catch (Exception e) {
            Logger.e(TAG, "get payload error!", e);
            return null;
        }
    }

    public static TVSDirectivesList parse(String directives) {
        Gson gson = new Gson();
        JsonArray array = getDirectivesArray(directives);

        TVSDirectivesList directivesList = new TVSDirectivesList();

        directivesList.status = getStatus(gson, directives);
        directivesList.directivesArray = new ArrayList<>();
        if (array == null) {
            Logger.e(TAG, "parse directives Error");
            return directivesList;
        }

        int size = array.size();

        TVSHeader tvsHeader;
        Class targetClass;
        TVSDirectives child;
        String childStr;

        for (int i = 0; i < size; i++) {
            tvsHeader = getHeader(gson, array, i);
            if (tvsHeader == null) {
                Logger.e(TAG, "get headder error! null object from json");
                continue;
            }

            childStr = array.get(i).toString();
            targetClass = check(tvsHeader.getNamespace(), tvsHeader.getName());
            if (targetClass != null) {
                child = (TVSDirectives) gson.fromJson(childStr, targetClass);
            } else {
                child = null;
            }

            if (child == null) {
                UnknownDirectives direc = new UnknownDirectives();
                direc.header = tvsHeader;
                direc.payload = new UnknownDirectives.Payload();
                direc.payload.payloadStr = getPayload(array, i);
                directivesList.directivesArray.add(direc);
            } else {
                directivesList.directivesArray.add(child);
            }
        }

        return directivesList;
    }
}
