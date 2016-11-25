package com.quantatw.sls.alljoyn;

public class RoomHubAllJoynDef {
    public class assetType {
        // asset type
        public static final int ASSET_TYPE_AC = 0;
        public static final int ASSET_TYPE_FAN =1;
        public static final int ASSET_TYPE_SPHYGMOMETER = 2;
        public static final int ASSET_TYPE_PM25 = 3;
        public static final int ASSET_TYPE_AIRPURIFIER = 4;
        public static final int ASSET_TYPE_BULB = 5;
        public static final int ASSET_TYPE_TV = 6;
    }

    public class method {
        // method
        public static final int METHOD_TYPE_COMMAND = 0;
        public static final int METHOD_TYPE_LED_CONTROL = 1;
        public static final int METHOD_TYPE_START_WPS = 2;
        public static final int METHOD_TYPE_LEARNING = 3;
        public static final int METHOD_TYPE_CHECK_IR_DATA = 4;
        public static final int METHOD_TYPE_CLEAN_IR_CONTROL_DATA = 5;
        public static final int METHOD_TYPE_ADD_IR_CONTROL_DATA = 6;
        public static final int METHOD_TYPE_ADD_SCHEDULE = 7;
        public static final int METHOD_TYPE_MODIFY_SCHEDULE = 8;
        public static final int METHOD_TYPE_GET_ALL_SCHEDULE = 9;
        public static final int METHOD_TYPE_REMOVE_SCHEDULE = 10;
        public static final int METHOD_TYPE_REMOVE_ALL_SCHEDULE = 11;
        public static final int METHOD_TYPE_GET_DEVICE_INFO = 12;
        public static final int METHOD_TYPE_SET_DEVICE_INFO = 13;
        public static final int METHOD_TYPE_GET_ASSET_TYPE = 14;
        public static final int METHOD_TYPE_SET_ASSET_TYPE = 15;
        public static final int METHOD_TYPE_GET_ABILITY_LIMIT = 16;
        public static final int METHOD_TYPE_SET_IRTX = 17;
        public static final int METHOD_TYPE_GET_ACONOFFSTATUS = 18;
        public static final int METHOD_TYPE_SET_CLOUD_SERVER_ADDRESS = 19;
        public static final int METHOD_TYPE_SPHYGMOMETER_COMMAND = 20;
        public static final int METHOD_TYPE_START_UPGRADE = 21;
        public static final int METHOD_TYPE_GET_NEXT_SCHEDULE = 22;
        public static final int METHOD_TYPE_ADD_DEVICE = 23;
        public static final int METHOD_TYPE_REMOVE_DEVICE = 24;
        public static final int METHOD_TYPE_GET_ALL_ASSETS = 25;
        public static final int METHOD_TYPE_AUTO_WIFI_BRIDGE_SWITCH = 27;
        public static final int METHOD_TYPE_GET_WIFI_BRIDGE_STATE = 28;
        public static final int METHOD_TYPE_REBOOT_ROOM_HUB = 29;
        public static final int METHOD_TYPE_FAIL_RECOVER = 30;
        public static final int METHOD_TYPE_ONBOARDING = 31;
        public static final int METHOD_TYPE_SCAN_ASSET = 33;
        public static final int METHOD_TYPE_SET_ASSET_PROFILE = 34;
        public static final int METHOD_TYPE_GET_ASSET_PROFILE = 35;
    }

    public class signal {
        // signal
        //  0: temperature 1: humidity 2: learningResult 3: deviceInfoChange 4: nameChange 5: syncTime
        //6: ACOnOffStatus 7: add or update schedule 8: deleteSchedule 9: nextSchedule 10: findSphygmometer
        //11: sphygmometerResult
        public static final int SIGNAL_TYPE_TEMPERATURE = 0;
        public static final int SIGNAL_TYPE_HUMIDITY = 1;
        public static final int SIGNAL_TYPE_LEARNING_RESULT = 2;
        public static final int SIGNAL_TYPE_DEVICE_INFO_CHANGE = 3;
        public static final int SIGNAL_TYPE_NAME_CHANGE = 4;
        public static final int SIGNAL_TYPE_SYNC_TIME = 5;
        public static final int SIGNAL_TYPE_ACONOFFSTATUS = 6;
        public static final int SIGNAL_TYPE_ADD_UPDATE_SCHEDULE = 7;
        public static final int SIGNAL_TYPE_DELETE_SCHEDULE = 8;
        public static final int SIGNAL_TYPE_NEXT_SCHEDULE = 9;
        public static final int SIGNAL_TYPE_FIND_SPHYGMOMETER = 10;
        public static final int SIGNAL_TYPE_SPHYGMOMETER_RESULT = 11;
    }

    public class result {
        public static final int RESULT_SUCCESS = 0;
        public static final int RESULT_ERROR = -1;
    }
}
