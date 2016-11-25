package com.quantatw.roomhub.utils;

/**
 * Created by 95011613 on 2015/04/28.
 */
public class AssetDef {
    public static final int CONNECTION_TYPE_IR    =   0;
    public static final int CONNECTION_TYPE_BT    =   1;
    public static final int CONNECTION_TYPE_WIFI  =   2;
    public static final int CONNECTION_TYPE_ALL   =   3;

    public static final int ONLINE_STATUS_OFFLINE  =   0;
    public static final int ONLINE_STATUS_ONLINE   =   1;

    public static final int POWER_OFF  =   0;
    public static final int POWER_ON  =   1;

    public static final String ADD_STATUS="add_status";
    public static final String ASSET_NAME="asset_name";
    public static final String ASSET_UUID="asset_uuid";
    public static final String ASSET_TYPE="asset_type";
    public static final String ASSET_DEFAULT_USER="asset_default_user";

    public static enum ADD {
        ASSET,
        HEALTHCARE,
        CONNECTION_TYPE
    }

    public enum COMMAND_TYPE {
        TEMP,
        FUN_MODE,
        POWER,
        TIMER_ON_OFF,
        FAN,
        SWING,
        KEY_ID,
        LUMINANCE,
    }
}
