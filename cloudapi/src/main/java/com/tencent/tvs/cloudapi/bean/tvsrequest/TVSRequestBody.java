/**
 * Copyright 2019 bejson.com
 */
package com.tencent.tvs.cloudapi.bean.tvsrequest;

import com.tencent.tvs.cloudapi.bean.tvsrequest.context.TVSContext;
import com.tencent.tvs.cloudapi.bean.tvsrequest.event.TVSEvent;

import java.util.List;

/**
 * TVS基础API协议体
 */
public class TVSRequestBody {
    public static class User {

        private String user_id;
        private Account account;

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setAccount(Account account) {
            this.account = account;
        }

        public Account getAccount() {
            return account;
        }

    }

    public static class Lbs {

        private double longitude;
        private double latitude;

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLatitude() {
            return latitude;
        }

    }

    public static class Device {

        private String network = " Wi-Fi";
        private String serialNum;

        public void setNetwork(String network) {
            this.network = network;
        }

        public String getNetwork() {
            return network;
        }

        public void setSerial_num(String serial_num) {
            this.serialNum = serial_num;
        }

        public String getSerial_num() {
            return serialNum;
        }
    }

    public static class BaseInfo {

        private String qua;
        private User user;
        private Lbs lbs;
        private String ip;
        private Device device = new Device();

        public void setQua(String qua) {
            this.qua = qua;
        }

        public String getQua() {
            return qua;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public void setLbs(Lbs lbs) {
            this.lbs = lbs;
        }

        public Lbs getLbs() {
            return lbs;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getIp() {
            return ip;
        }

        public void setDevice(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }
    }

    public static class Account {

        private String id;
        private String appid;
        private String type;
        private String token;

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getAppid() {
            return appid;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }

    }


    private BaseInfo baseInfo = new BaseInfo();
    private TVSEvent event;
    private List<TVSContext> context;

    public void setBaseInfo(BaseInfo baseInfo) {
        this.baseInfo = baseInfo;
    }

    public BaseInfo getBaseInfo() {
        return baseInfo;
    }

    public void setEvent(TVSEvent event) {
        this.event = event;
    }

    public TVSEvent getEvent() {
        return event;
    }

    public void setContext(List<TVSContext> context) {
        this.context = context;
    }

    public List<TVSContext> getContext() {
        return context;
    }

}