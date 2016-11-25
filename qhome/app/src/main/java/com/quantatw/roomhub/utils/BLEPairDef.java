package com.quantatw.roomhub.utils;

/**
 * Created by 95011613 on 2016/5/26.
 */
public class BLEPairDef {
    public static final String BLE_STATUS="ble_status";
    public static final String BLE_SCAN_ASSET_LIST="ble_scan_asset_list";
    public static final String BLE_SELECTED_ASSET="ble_selected_asset";
    public static final String BLE_NAMME="ble_name";
    public static final String BLE_ERROR_CODE="ble_error_code";
    public static final String BLE_SUCCESS_ASSET="ble_sucess_asset";
    public static final String BLE_FAIL_ASSET="ble_fail_asset";

    public static enum STATUS {
        START,
        SUCCESS,
        FAIL,
        SCAN_ASSET,
        ADD_DEVICE,
        SCAN_RESULT,
        RENAME,
        DEFAULT_USER
    }

    public static enum ADD_STEP {
        ADD_ASSET,
        SET_ASSET_INFO,
        SET_NAME,
        REG_TO_CLOUD,
        SET_DEFAULT_USER
    }
}
