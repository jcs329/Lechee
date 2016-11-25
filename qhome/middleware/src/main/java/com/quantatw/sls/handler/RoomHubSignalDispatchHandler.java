package com.quantatw.sls.handler;

import android.os.SystemClock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.homeAppliance.AssetData;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.AcAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.AirPurifierAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.AssetResPack;
import com.quantatw.sls.pack.homeAppliance.detail.BloodPressureAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.FanAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.BulbAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.PMAssetDetailInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.TVAssetDetailInfoResPack;
import com.quantatw.sls.pack.mqtt.AssetRecvPack;

/**
 * Created by erin on 2/1/16.
 */
public class RoomHubSignalDispatchHandler {
    public class signalType {
        public static final int TEMPERATURE = 0;
        public static final int HUMIDITY = 1;
        public static final int LEARNING_RESULTS = 2;
        public static final int DEVICE_INFO_CHANGE = 3;
        public static final int NAME_CHANGE = 4;
        public static final int SYNC_TIME = 5;
        public static final int AC_ON_OFF_STATUS = 6;
        public static final int ADD_UPDATE_SCHEDULE = 7;
        public static final int DELETE_SCHEDULE = 8;
        public static final int NEXT_SCHEDULE = 9;
        public static final int FIND_SPHYGMOMETER = 10;
        public static final int SPHYGMOMETER_RESULT = 11;
        public static final int ADD_ASSET = 12;
        public static final int DELETE_ASSET = 13;
        public static final int ASSET_INFO_CHANGE = 14;
        public static final int FW_UPGRADE_STATE = 15;
        public static final int FAIL_RECOVER = 16;
        public static final int SCAN_ASSET_RESULT = 17;
        public static final int ASSET_SENSOR_DATA = 18;
        public static final int ASSET_PROFILE_CHANGE = 19;
    }

    public static void dispatch(HomeApplianceSignalListener listener, RoomHubDevice device, String jsonString) {
        Gson gson = new GsonBuilder().create();
        final AssetRecvPack pack = gson.fromJson(jsonString, AssetRecvPack.class);
        switch (pack.getType()) {
            case signalType.SYNC_TIME: {
                long currentTimeSecs = SystemClock.currentThreadTimeMillis()/1000;
                device.setTime((int)currentTimeSecs);
            }
                break;
            case signalType.ADD_ASSET: {
                AssetData assetData = gson.fromJson(jsonString, AssetData.class);
                if (assetData != null) {
                    AssetResPack assetResPack = new AssetResPack();
                    // RoomHub uuid:
                    assetResPack.setUuid(device.getUuid());
                    // Asset info:
                    assetResPack.setAssetType(assetData.getAssetType());
                    assetResPack.setAssetUuid(assetData.getUuid());
                    if(assetData.getResult() < ErrorKey.Success)
                        assetResPack.setStatus_code(ErrorKey.ADD_APPLIANCES_FAILURE);
                    else
                        assetResPack.setStatus_code(assetData.getResult());
                    if (listener != null) {
                        listener.addAsset(assetResPack, SourceType.ALLJOYN);
                    }
                }
            }
                break;
            case signalType.DELETE_ASSET: {
                AssetData assetData = gson.fromJson(jsonString, AssetData.class);
                if (assetData != null) {
                    AssetResPack assetResPack = new AssetResPack();
                    // RoomHub uuid:
                    assetResPack.setUuid(device.getUuid());
                    // Asset info:
                    assetResPack.setAssetType(assetData.getAssetType());
                    assetResPack.setAssetUuid(assetData.getUuid());
                    if (listener != null) {
                        listener.removeAsset(assetResPack,SourceType.ALLJOYN);
                    }
                }
            }
                break;
            case signalType.ASSET_INFO_CHANGE: {
                switch (pack.getAssetType()) {
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_AC: {
                        AcAssetDetailInfoResPack assetDetailInfoResPack =
                                gson.fromJson(jsonString, AcAssetDetailInfoResPack.class);
                        assetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(),assetDetailInfoResPack,SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_FAN: {
                        FanAssetDetailInfoResPack assetDetailInfoResPack =
                                gson.fromJson(jsonString, FanAssetDetailInfoResPack.class);
                        assetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(),assetDetailInfoResPack,SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_AIRPURIFIER: {
                        AirPurifierAssetDetailInfoResPack airPurifierAssetDetailInfoResPack =
                                gson.fromJson(jsonString, AirPurifierAssetDetailInfoResPack.class);
                        airPurifierAssetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(), airPurifierAssetDetailInfoResPack, SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_PM25: {
                        PMAssetDetailInfoResPack pmAssetDetailInfoResPack =
                                gson.fromJson(jsonString, PMAssetDetailInfoResPack.class);
                        pmAssetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(), pmAssetDetailInfoResPack, SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_SPHYGMOMETER: {
                        BloodPressureAssetDetailInfoResPack bloodPressureAssetDetailInfoResPack =
                                gson.fromJson(jsonString, BloodPressureAssetDetailInfoResPack.class);
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(), bloodPressureAssetDetailInfoResPack, SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_BULB: {
                        BulbAssetDetailInfoResPack bulbAssetDetailInfoResPack =
                                gson.fromJson(jsonString, BulbAssetDetailInfoResPack.class);
                        bulbAssetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(), bulbAssetDetailInfoResPack, SourceType.ALLJOYN);
                    }
                    break;
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_TV: {
                        TVAssetDetailInfoResPack tvAssetDetailInfoResPack =
                                gson.fromJson(jsonString, TVAssetDetailInfoResPack.class);
                        tvAssetDetailInfoResPack.setRoomHubUUID(device.getUuid());
                        if(listener != null)
                            listener.AssetInfoChange(pack.getAssetType(), tvAssetDetailInfoResPack, SourceType.ALLJOYN);
                    }
                    break;
                    default:
                        break;
                }
            }
                break;
            case signalType.FW_UPGRADE_STATE:
                FirmwareUpdateStateResPack firmwareUpdateState = gson.fromJson(jsonString, FirmwareUpdateStateResPack.class);
                if(firmwareUpdateState != null) {
                    if(listener != null)
                        listener.FirmwareUpdateStateChange(firmwareUpdateState);
                }
                break;
            case signalType.FAIL_RECOVER:
                switch (pack.getAssetType()) {
                    case RoomHubAllJoynDef.assetType.ASSET_TYPE_AC: {
                        AcFailRecoverResPack failRecoverResPack =
                                gson.fromJson(jsonString, AcFailRecoverResPack.class);
                        if(listener != null)
                            listener.AcFailRecover(failRecoverResPack,SourceType.ALLJOYN);
                    }
                    break;
                }
                break;
            case signalType.SCAN_ASSET_RESULT:
                ScanAssetResultResPack scanAssetResult = gson.fromJson(jsonString, ScanAssetResultResPack.class);
                if(scanAssetResult != null) {
                    scanAssetResult.setUuid(device.getUuid());
                    if(listener != null)
                        listener.ScanAssetResult(scanAssetResult);
                }
                break;
            case signalType.ADD_UPDATE_SCHEDULE:
                SignalUpdateSchedulePack updateSchedulePack = gson.fromJson(jsonString, SignalUpdateSchedulePack.class);
                if(updateSchedulePack != null) {
                    if(listener != null)
                        listener.UpdateSchedule(updateSchedulePack);
                }
                break;
            case signalType.DELETE_SCHEDULE:
                SignalDeleteSchedulePack deleteSchedulePack = gson.fromJson(jsonString, SignalDeleteSchedulePack.class);
                if(deleteSchedulePack != null) {
                    if(listener != null)
                        listener.DeleteSchedule(deleteSchedulePack);
                }
                break;
            case signalType.ASSET_PROFILE_CHANGE:
                AssetProfile profile = gson.fromJson(jsonString, AssetProfile.class);
                if(profile != null) {
                    if(listener != null)
                        listener.AssetProfileChange(profile);
                }
                break;
            default:
                break;
        }
    }
}
