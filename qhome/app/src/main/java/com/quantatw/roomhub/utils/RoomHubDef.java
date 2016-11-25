package com.quantatw.roomhub.utils;

import com.quantatw.sls.api.DeviceTypeConvertApi;


/**
 * Created by 95011613 on 2015/9/30.
 */
public class RoomHubDef {

    public static final int LED_OFF    =   0;
    public static final int LED_ON     =   1;
    public static final int LED_FLASH  =   2;

    public static final int LED_COLOR_DARK      =   0;
    public static final int LED_COLOR_RED       =   1;
    public static final int LED_COLOR_GREEN     =   2;
    public static final int LED_COLOR_BLUE      =   3;
    public static final int LED_COLOR_PURPLE    =   4;
    public static final int LED_COLOR_YELLOW    =   5;
    public static final int LED_COLOR_SKY       =   6;
    public static final int LED_COLOR_WHITE     =   7;

    public static final String ROLE_NONE = "None";
    public static final String ROLE_OWNER = "Owner";
    public static final String ROLE_USER = "User";
    public static final String ROLE_ADMIN = "Administrator";

    public static int getCategory(int type) {
        switch (type) {
            // new
            case DeviceTypeConvertApi.TYPE_ROOMHUB.AC:
            case DeviceTypeConvertApi.TYPE_ROOMHUB.FAN:
            case DeviceTypeConvertApi.TYPE_ROOMHUB.PM25:
            case DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER:
            case DeviceTypeConvertApi.TYPE_ROOMHUB.BULB:
            case DeviceTypeConvertApi.TYPE_ROOMHUB.TV:
                return DeviceTypeConvertApi.CATEGORY.ROOMHUB;
            case DeviceTypeConvertApi.TYPE_HEALTH.BPM:
            case DeviceTypeConvertApi.TYPE_HEALTH.WEIGHT:
                return DeviceTypeConvertApi.CATEGORY.HEALTH;
            case DeviceTypeConvertApi.TYPE_SECURITY.LOCKER:
                return DeviceTypeConvertApi.CATEGORY.SECURITY;
            case DeviceTypeConvertApi.TYPE_ENVIRONMENT.CO2:
                return DeviceTypeConvertApi.CATEGORY.ENVIRONMENT;
        }
        return -1;
    }
}
