package com.quantatw.roomhub.ui;

/**
 * Created by erin on 10/14/15.
 */
public class IRSettingDataValues {
    public static final String KEY_DATA_IR_SETTING_MODE="IR_SETTING_MODE";
    public static final String KEY_DATA_ROOM_HUB_DEVICE_UUID="ROOM_HUB_DEVICE_UUID";
    public static final String KEY_DATA_IR_BRAND_NAME="IR_BRAND_NAME";
    public static final String KEY_DATA_IR_CODE_NUMBER="IR_CODE_NUMBER";
    public static final String KEY_DATA_IR_CODENUM_DATA="IR_CODENUM_DATA";
    public static final String KEY_IR_LEARNING_RESULTS = "KEY_IR_LEARNING_RESULTS";
    public static final String KEY_IR_AUTO_SCAN_RESULTS = "KEY_IR_AUTO_SCAN_RESULTS";
    public static final String KEY_IR_LEARNING_RESULTS_REASON = "KEY_IR_LEARNING_RESULTS_REASON";
    public static final String KEY_IR_SEARCH_RESULTS = "KEY_IR_SEARCH_RESULTS";

    public static final String KEY_DATA_IR_PARING_INFO = "KEY_DATA_IR_PARING_INFO";
    public static final String KEY_DATA_IR_AUTO_SCAN_COUNT = "KEY_DATA_IR_AUTO_SCAN_COUNT";

    public static final String KEY_ELECTRIC_TYPE="ELECTRIC_TYPE";
    public static final String KEY_ELECTRIC_UUID="ELECTRIC_UUID";
    /*
    * IR Setting mode:
    * 1. Choose from brand list
    * 2. Startup learning IR codes
    * 3. Auto scan
     */
    public static final int IR_SETTING_MODE_GET_LIST = 100;
    public static final int IR_SETTING_MODE_LEARN_CODES = 200;
    public static final int IR_SETTING_MODE_AUTO_SCAN = 300;
    public static final int IR_SETTING_MODE_SEARCH = 400;

    public static final int REQUEST_CODE_IR_CONFIG_DONE = 600;

    /* register for IR Learning results signal */
    public static final String ACTION_IR_LEARNING_RESULTS = "ACTION_IR_LEARNING_RESULTS";

    /* register for IR auto scan results broadcast */
    public static final String ACTION_IR_AUTO_SCAN_RESULTS = "ACTION_IR_AUTO_SCAN_RESULTS";

    /* register for searching results broadcast */
    public static final String ACTION_IR_SEARCH_RESULTS = "ACTION_IR_SEARCH_RESULTS";

    private static final int IR_AC_CHECK_TYPE = 1;
    private static final int IR_AV_CHECK_TYPE = 2;

    public static enum IR_LEARNING_CHECK_TYPE {
        IR_AC_CHECK_TYPE,
        IR_AV_CHECK_TYPE
    }

    public static final int IR_LEARNING_SUCCESS = 0;
    public static final int IR_LEARNING_TIMEOUT = 100;
    public static final int IR_LEARNING_NOT_MATCHED = 200;

}
