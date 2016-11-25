package com.quantatw.sls.api;

import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.key.DeviceType;

/**
 * Created by erin on 5/19/16.
 */
public class DeviceTypeConvertApi {

    public static final int TYPE_NOT_FOUND = -1;

    public static class AppDeviceCategoryType {
        int category = TYPE_NOT_FOUND;
        int type = TYPE_NOT_FOUND;

        public AppDeviceCategoryType() {}
        public AppDeviceCategoryType(int category,int type) {
            this.category = category;
            this.type = type;
        }

        public int getCategory() {
            return category;
        }

        public void setCategory(int category) {
            this.category = category;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

    public enum TypeDef {
        AC(RoomHubAllJoynDef.assetType.ASSET_TYPE_AC,"AC"),
        FAN(RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN,"FAN"),
        BPM(RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER,"BPM"),
        PM25(RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25,"PM2.5"),
        AIR_PURIFIER(RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25,"AIR_PURIFIER"),
        BULB(RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB,"BULB"),
        TV(RoomHubAllJoynDef.assetType.ASSET_TYPE_TV,"TV"),
        LOCKER(36,"LOCKER"),    // TODO:
        CO2(56,"CO2");  // TODO:

        private int value;
        private String tag;

        TypeDef(int value, String tag) {
            this.value = value;
            this.tag = tag;
        }

        public int getValue() { return this.value; }
        public String getTag() { return this.tag; }
    }

    private static final class Column {
        static final int CATEGORY       = 1;
        static final int APP_TYPE       = 2;
        static final int ALLJOYN_TYPE   = 3;
        static final int CLOUD_TYPE     = 4;
    }

    private static class CommonData {
        private CommonData() {}
        static int getCategory() { return -1; }
    }

    public static final class CATEGORY {
        public static final int ROOMHUB       = 0;
        public static final int HEALTH        = 1;
        public static final int SECURITY      = 2;
        public static final int ENVIRONMENT   = 3;
    }

    public static final class TYPE_ROOMHUB extends CommonData {
        public static final int AC                  = 0;
        public static final int FAN                 = 1;
        public static final int PM25                = 3;
        public static final int AIR_PURIFIER        = 4;
        public static final int BULB                = 5;
        public static final int TV                  = 6;

        public static int getCategory() {
            return CATEGORY.ROOMHUB;
        }
    }

    public static final class TYPE_HEALTH extends CommonData{
        public static final int BPM                 = 20;
        public static final int WEIGHT              = 21;

        public static int getCategory() {
            return CATEGORY.HEALTH;
        }
    }

    public static final class TYPE_SECURITY extends CommonData{
        public static final int LOCKER              = 50;

        public static int getCategory() {
            return CATEGORY.SECURITY;
        }
    }

    public static final class TYPE_ENVIRONMENT extends CommonData{
        public static final int CO2                 = 70;

        public static int getCategory() {
            return CATEGORY.ENVIRONMENT;
        }
    }

    private static final int[][] mLookupTable = {
            /* INDEX,CATEGORY */
            {TypeDef.AC.value,CATEGORY.ROOMHUB,
                    /* APP_VALUE,       ALLJOYN_VALUE,      CLOUD_VALUE */
                    TYPE_ROOMHUB.AC,RoomHubAllJoynDef.assetType.ASSET_TYPE_AC,RoomHubAllJoynDef.assetType.ASSET_TYPE_AC},

            {TypeDef.FAN.value,CATEGORY.ROOMHUB,
                    TYPE_ROOMHUB.FAN,RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN,RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN},

            {TypeDef.BPM.value,CATEGORY.HEALTH,
                    TYPE_HEALTH.BPM,RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER,RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER},

            {TypeDef.PM25.value,CATEGORY.ROOMHUB,
                    TYPE_ROOMHUB.PM25,RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25,RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25},

            {TypeDef.AIR_PURIFIER.value,CATEGORY.ROOMHUB,
                    TYPE_ROOMHUB.AIR_PURIFIER,RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER,RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER},

            {TypeDef.BULB.value,CATEGORY.ROOMHUB,
                    TYPE_ROOMHUB.BULB,RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB,RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB},

            {TypeDef.TV.value,CATEGORY.ROOMHUB,
                    TYPE_ROOMHUB.TV,RoomHubAllJoynDef.assetType.ASSET_TYPE_TV,RoomHubAllJoynDef.assetType.ASSET_TYPE_TV},

            // TODO:
            {TypeDef.LOCKER.value,CATEGORY.SECURITY,
                    TYPE_SECURITY.LOCKER,66,66},

            // TODO:
            {TypeDef.CO2.value,CATEGORY.ENVIRONMENT,
                    TYPE_ENVIRONMENT.CO2,88,88}

    };

    private static int findValue(int findIndex, int resultIndex, int findType) {
        for(int i=0;i<mLookupTable.length;i++) {
            if(mLookupTable[i][findIndex] == findType) {
                return mLookupTable[i][resultIndex];
            }
        }
        return TYPE_NOT_FOUND;
    }

    private static int findValue(AppDeviceCategoryType appDeviceCategoryType, int findIndex, int resultIndex) {
        for(int i=0;i<mLookupTable.length;i++) {
            if(mLookupTable[i][Column.CATEGORY] == appDeviceCategoryType.getCategory() &&
                    mLookupTable[i][findIndex] == appDeviceCategoryType.getType()) {
                return mLookupTable[i][resultIndex];
            }
        }
        return TYPE_NOT_FOUND;
    }

    private static boolean isDeviceSupported(int deviceType) {
        switch (deviceType) {
            case DeviceType.RoomHubDevice:
            case DeviceType.BPMDevice:
                return true;
        }
        return false;
    }

    public static int ConvertType_GetCategoryByCloudDeviceType(int deviceType) {
        if(!isDeviceSupported(deviceType))
            return TYPE_NOT_FOUND;
        return findValue(Column.CLOUD_TYPE,Column.CATEGORY,deviceType);
    }

    public static int ConvertType_AllJoynToCloud(int type) {
        return findValue(Column.ALLJOYN_TYPE,Column.CLOUD_TYPE,type);
    }

    public static AppDeviceCategoryType ConvertType_AllJoynToApp(int type) {
        AppDeviceCategoryType appDeviceCategoryType = new AppDeviceCategoryType();
//        int category = ConvertType_GetCategoryByCloudDeviceType(type);

        for(int i=0;i<mLookupTable.length;i++) {
            if(mLookupTable[i][Column.ALLJOYN_TYPE] == type) {
                appDeviceCategoryType.setCategory(mLookupTable[i][Column.CATEGORY]);
                appDeviceCategoryType.setType(mLookupTable[i][Column.APP_TYPE]);
                break;
            }
        }

        return appDeviceCategoryType;

    }

    public static int ConvertType_CloudToAllJoyn(int type) {
        return findValue(Column.CLOUD_TYPE,Column.ALLJOYN_TYPE,type);
    }

    public static AppDeviceCategoryType ConvertType_CloudToApp(int type) {
        AppDeviceCategoryType appDeviceCategoryType = new AppDeviceCategoryType();
//        int category = ConvertType_GetCategoryByCloudDeviceType(type);

        for(int i=0;i<mLookupTable.length;i++) {
            if(mLookupTable[i][Column.CLOUD_TYPE] == type) {
                appDeviceCategoryType.setCategory(mLookupTable[i][Column.CATEGORY]);
                appDeviceCategoryType.setType(mLookupTable[i][Column.APP_TYPE]);
                break;
            }
        }

        return appDeviceCategoryType;
    }

    public static int ConvertType_AppToAllJoyn(AppDeviceCategoryType appDeviceCategoryType) {
        return findValue(appDeviceCategoryType,Column.APP_TYPE,Column.ALLJOYN_TYPE);
    }

    public static int ConvertType_AppToCloud(AppDeviceCategoryType appDeviceCategoryType) {
        return findValue(appDeviceCategoryType,Column.APP_TYPE,Column.CLOUD_TYPE);
    }

}
