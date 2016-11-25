package com.quantatw.roomhub.manager.asset.manager;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEControllerCallback;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.listener.IRControllerCallback;
import com.quantatw.roomhub.listener.IRLearningResultCallback;
import com.quantatw.roomhub.listener.IRParingActionCallback;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.ui.IRSettingDataValues;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.IRUtils;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.object.IRACKeyData;
import com.quantatw.sls.object.IRBrandAndModelData;
import com.quantatw.sls.pack.homeAppliance.AddIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CleanIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.GetAirPurifierAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;

import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;

import com.quantatw.sls.pack.homeAppliance.detail.AirPurifierAssetDetailInfoResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by 95010915 on 2016/01/28.
 */
public class AirPurifierManager extends BaseAssetManager {
    private static final String TAG = AirPurifierManager.class.getSimpleName();
    //private HashMap<String, AirPurifierData> mAirPurifierList = new HashMap<String, AirPurifierData>();
    private HashMap<String, HashMap<String,AirPurifierData>> mAirPurifierList = new HashMap<String, HashMap<String,AirPurifierData>>();
    private IRController mIRController;

    private HashMap<String, HashMap<String, IRLearningResultCallback>> mIRLearningResultCallback = new HashMap<>();
    private BLEPairController mBLEController;

    @Override
    public void startup() {
        mIRController = ((RoomHubService) mContext).getIRController();
    }

    @Override
    public void terminate() {

    }

    @Override
    public void configIRSetting(String uuid, String assetUuid) {
        mIRController.configIR(uuid, assetUuid, mAssetType, mIRParingAction);
    }

    public AirPurifierManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER, context.getString(R.string.electric_ion), R.drawable.air_btn_selector, AssetDef.CONNECTION_TYPE_ALL);
    }

    public AirPurifierData getAirPurifierDataByUuid(String roomhub_uuid,String Uuid){
        if(mAirPurifierList ==null) return null;

        if(!TextUtils.isEmpty(roomhub_uuid) && !TextUtils.isEmpty(Uuid)){
            HashMap<String,AirPurifierData> data=mAirPurifierList.get(roomhub_uuid);
            if(data != null){
                return data.get(Uuid);
            }
        }else if(!TextUtils.isEmpty(Uuid)){
            synchronized (mAirPurifierList) {
                for(int i=0;i < mAirPurifierList.size();i++){
                    HashMap<String,AirPurifierData> data=mAirPurifierList.get(i);
                    AirPurifierData air_purifier_data=data.get(Uuid);
                    if(air_purifier_data != null){
                        return air_purifier_data;
                    }
                }
            }
        }

        return null;
    }

    public int setKeyId(String roomhub_uuid,String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.KEY_ID,roomhub_uuid, uuid, value, 0);
    }

    private int setAirPurifierCommand(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        String asset_uuid=bundle.getString(KEY_ASSET_UUID);
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(uuid,asset_uuid);

        if(airPurifierData == null) {
            log("setAirPurifierCommand : not found device");
            return ErrorKey.AP_DATA_NOT_FOUND;
        }

        CommandRemoteControlReqPack req_pack=new CommandRemoteControlReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(asset_uuid);

        AssetDef.COMMAND_TYPE cmd_type=(AssetDef.COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        int cmd_value=bundle.getInt(KEY_CMD_VALUE);

        RoomHubDevice device=airPurifierData.getRoomHubData().getRoomHubDevice();
        switch(cmd_type){
//            case POWER:
//            case SPEED:
//            case AUTO:
//            case UV:
//            case ANION:
//            case TIMER:
            case KEY_ID:
                req_pack.setKeyId(cmd_value);
                break;
        }

        log("setAirPurifierCommand uuid="+uuid+" cmd_type="+cmd_type+" cmd_value="+cmd_value);
        int ret_val= ErrorKey.Success;

        CommandResPack res_pack=device.commandRemoteControl(req_pack);
        if(res_pack != null) {
            ret_val=res_pack.getStatus_code();
        }

        if(ret_val == ErrorKey.Success)
            device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        log("setAirPurifierCommand ret_val="+ret_val);

        return ret_val;
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new AirPurifierData(data,mContext.getString(R.string.electric_ion),R.drawable.btn_ion_enable);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        AirPurifierData air_data=getAirPurifierDataByUuid(null, asset_uuid);
        String str_fan_models=mContext.getResources().getString(R.string.ac_na);
        if(air_data!=null){
            if(!TextUtils.isEmpty(air_data.getBrandName()) && !TextUtils.isEmpty(air_data.getModelNumber()))
                str_fan_models=air_data.getBrandName() +"/"+air_data.getModelNumber();
        }
        return str_fan_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub){
        String uuid=asset_info_data.getAssetUuid();
        String roomhub_uuid=room_hub.getUuid();

        AirPurifierData airPurifierData=getAirPurifierDataByUuid(roomhub_uuid,uuid);
        boolean bAdd=false;
        if(airPurifierData==null){
            log("AirMgr_AddDevice is not exist");
            bAdd=true;
            airPurifierData=(AirPurifierData)asset_info_data;
        } else{
            log("AirMgr_AddDevice device is exist");
        }

        if(bAdd){
            synchronized(mAirPurifierList) {
                HashMap<String,AirPurifierData> data_map = new HashMap<String,AirPurifierData>();
                data_map.put(uuid,airPurifierData);
                log("AirMgr_AddDevice add roomhub_uuid="+roomhub_uuid+" asset_uuid="+uuid);
                mAirPurifierList.put(roomhub_uuid, data_map);
            }
            NotifyAddDevice(mAssetType, airPurifierData);
        }

        if(room_hub.IsOnLine()) {
            UpdateAssetDataAfterOnLine(airPurifierData);
        }
    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub){
        String uuid=asset_info_data.getAssetUuid();
        String roomhub_uuid=room_hub.getUuid();

        HashMap<String,AirPurifierData> ap_data_list=mAirPurifierList.get(roomhub_uuid);
        if (ap_data_list != null){
            AirPurifierData ap_data=ap_data_list.get(uuid);
            synchronized(mAirPurifierList) {
                if (ap_data != null) {
                    NotifyRemoveDevice(mAssetType, ap_data);
                    ap_data_list.remove(uuid);
                    log("BaseAsset_RemoveDevice asset_uuid="+uuid);
                }
                if (ap_data_list.size() == 0) {
                    mAirPurifierList.remove(roomhub_uuid);
                    log("BaseAsset_RemoveDevice roomhub_uuid=" + roomhub_uuid);
                }

            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub){
        String uuid=asset_info_data.getAssetUuid();

        log("AirMgr_UpdateDevice uuid="+uuid);

        AirPurifierData airPurifierData=getAirPurifierDataByUuid(room_hub.getUuid(), uuid);

        if(airPurifierData!=null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(airPurifierData);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data){
        if(mAirPurifierList == null) return;
        if(data == null) return;
        String roomhub_uuid=data.getUuid();

        synchronized (mAirPurifierList) {
            HashMap<String,AirPurifierData> data_map=mAirPurifierList.get(roomhub_uuid);
            if(data_map != null){
                for (Iterator<AirPurifierData> it = data_map.values().iterator(); it.hasNext(); ) {
                    AirPurifierData air_purifier_data = it.next();
                    if (air_purifier_data.getRoomHubUuid().equals(roomhub_uuid)) {
                        if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                            if (data.IsOnLine()) {
                                UpdateAssetDataAfterOnLine(air_purifier_data);
                                NotifyPageStatus(mAssetType,true,air_purifier_data);
                            }else{
                                NotifyPageStatus(mAssetType,false,air_purifier_data);
                            }
                        }
                        air_purifier_data.setRoomHubData(data);
                    }
                }
            }
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle){
        return setAirPurifierCommand(bundle);
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade){
        if(mAirPurifierList == null) return;

        synchronized (mAirPurifierList) {
            HashMap<String,AirPurifierData> data_map=mAirPurifierList.get(uuid);
            if(data_map != null) {
                for (Iterator<AirPurifierData> it = data_map.values().iterator(); it.hasNext(); ) {
                    AirPurifierData airPurifierData = it.next();
                    if (airPurifierData.getRoomHubUuid().equals(uuid)) {
                        if (is_upgrade)
                            NotifyPageStatus(mAssetType, false, airPurifierData);
                        else
                            NotifyPageStatus(mAssetType, true, airPurifierData);
                    }
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {
        AirPurifierAssetDetailInfoResPack res_pack=(AirPurifierAssetDetailInfoResPack)ResPack;
        log("BaseAsset_AssetInfoChange roomhub_uuid="+res_pack.getRoomHubUUID()+" uuid=" + res_pack.getUuid() +" source_type="+sourceType);
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(res_pack.getRoomHubUUID(),res_pack.getUuid());
        if(airPurifierData != null){
            if(airPurifierData.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            if(airPurifierData.setAbilityLimit(res_pack.getBrand(), res_pack.getDevice()) == ErrorKey.AP_ABILITY_INVALID)
                RetryMessage(airPurifierData,MESSAGE_RETRY_ABILITY_LIMIT);
            log("BaseAsset_AssetInfoChange uuid=" + res_pack.getUuid() + " connection_type=" + res_pack.getConnectionType() + " brand_name=" + res_pack.getBrand() + " device_model=" + res_pack.getDevice()+" onlinestatus="+res_pack.getOnLineStatus());
            GetAirPurifierAssetInfoResPack asset_info=new GetAirPurifierAssetInfoResPack();
            asset_info.setUuid(res_pack.getUuid());
            asset_info.setPower(res_pack.getPower());
            asset_info.setAutoOn(res_pack.getAutoOn());
            asset_info.setNotifyValue(res_pack.getNotifyValue());

            if(res_pack.getConnectionType() == AssetDef.CONNECTION_TYPE_BT){
                asset_info.setSpeed(res_pack.getSpeed());
                asset_info.setQuality(res_pack.getQuality());
                asset_info.setAutoFan(res_pack.getAutoFan());
                asset_info.setUv(res_pack.getUv());
                asset_info.setAnion(res_pack.getAnion());
                asset_info.setSpeed(res_pack.getSpeed());
                asset_info.setTimer(res_pack.getTimer());
                asset_info.setStrainer(res_pack.getStrainer());
            }else{
                asset_info.setSwing(res_pack.getSwing());
                asset_info.setFanSpeed(res_pack.getFanSpeed());
                asset_info.setMode(res_pack.getMode());
            }

            asset_info.setSubType(res_pack.getSubType());
            asset_info.setConnectionType(res_pack.getConnectionType());
            asset_info.setBrand(res_pack.getBrand());
            asset_info.setDevice(res_pack.getDevice());
            asset_info.setBrandId(res_pack.getBrandId());
            asset_info.setModelId(res_pack.getModelId());
            asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            airPurifierData.setAirPurifierAssetInfo(asset_info);

            UpdateAssetData(mAssetType,airPurifierData);
        }
    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        AirPurifierData air_data = (AirPurifierData)data;
        log("BaseAsset_GetAssetInfo roomhub is online="+air_data.getRoomHubData().IsOnLine());
        if((air_data != null) && air_data.getRoomHubData().IsOnLine()){
            if(air_data.getAirPurifierAssetInfo() == ErrorKey.AP_ASSET_INFO_INVAILD)
                RetryMessage(air_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    @Override
    public void BaseAsset_GetAbilityLimit(Object data) {
        AirPurifierData air_data = (AirPurifierData)data;
        if((air_data != null) && (air_data.getRoomHubData().IsOnLine())){
            if(air_data.getAssetAbility() == ErrorKey.AP_ABILITY_INVALID)
                RetryMessage(air_data,MESSAGE_RETRY_ABILITY_LIMIT);
        }
    }

    /*
    * IR Paring
     */
    @Override
    public void BaseAsset_LearningResult(LearningResultResPack learningResultResPack) {

        log("RoomHubLearningResultUpdate enter");

        HashMap<String,IRLearningResultCallback> callbackHashMap = mIRLearningResultCallback.get(learningResultResPack.getUuid());
        if(callbackHashMap != null) {
            String assetUuid = callbackHashMap.keySet().iterator().next();
            IRLearningResultCallback callback = callbackHashMap.values().iterator().next();

            if (callback != null) {
                byte[] irData = IRUtils.getIRDataFromResPack(learningResultResPack.getIrData());
                if (irData != null && irData.length == 1) {
                    callback.onLoadResultsFail(learningResultResPack.getUuid());
                } else {
                    log("irData len=" + irData.length);
                    if (irData.length > 22) {
                        byte[] newBytes = IRUtils.irLearningResult_hexStringToByteArray(new String(irData));
                        irData = newBytes;
                    }
                    int s1 = IRUtils.getIRS1(irData);
                    int s0 = IRUtils.getIRS0(irData);
                    String s3 = IRUtils.getIRS3(irData);
                    mRoomHubMgr.setLed(learningResultResPack.getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 2000, 0, 1);
                    callback.onLoadResultsSuccess(learningResultResPack.getUuid(),
                            mAssetType,
                            assetUuid, s0, s1, 0, s3, IRSettingDataValues.IR_LEARNING_CHECK_TYPE.IR_AC_CHECK_TYPE);

                }
            }
        }
    }

    private IRParingActionCallback mIRParingAction = new IRParingActionCallback() {
        @Override
        public void onTest(String uuid, String irData) {
            boolean ret = checkIRData(uuid, irData);
            mIRController.log(TAG, "onTest uuid=" + uuid + " checkIRData ret=" + ret);
            //setLed(uuid, RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);
            mRoomHubMgr.setLed(uuid, RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_OFF, 0, 0, 0);
        }

        @Override
        public void onStart(ApIRParingInfo currentTarget, IRControllerCallback resultCallback) {
            mIRController.log(TAG,"onStart uuid="+currentTarget.getUuid()
                    + ",assetUuid" + currentTarget.getAssetUuid()
                    + ",codeNum="+currentTarget.getCodeNum());

            mIRController.log(TAG, "onStart clear pair info:::");

            clearIRPair(currentTarget.getUuid(),currentTarget.getAssetUuid());
            //Step 1: cleanIRControlData
            mIRController.log(TAG, "onStart cleanIRControlData:::");
            cleanIRControlData(currentTarget.getUuid(),currentTarget.getAssetUuid());

            int i=0,count=0;
            //Step 2: addIRControlData
            for(IRACKeyData iracKeyData: currentTarget.getIracKeyDataList()) {
                AddIRControlDataReqPack reqPack = new AddIRControlDataReqPack();
                reqPack.setUuid(currentTarget.getAssetUuid());
                reqPack.setKeyId(Integer.parseInt(iracKeyData.getKeyId()));
                reqPack.setIrData(iracKeyData.getIrData());
                reqPack.setAssetType(currentTarget.getAssetType());

                /* Spec doesn't define this.
                if((++i)%2 == 1)
                    setLed(currentTarget.getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_ON, 0, 0, 0);
                else
                    setLed(currentTarget.getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_OFF, 0, 0, 0);
                    */

                mIRController.log(TAG, "onStart addIRControlData():::" + Integer.toString(++i));
                addIRControlData(currentTarget.getUuid(),currentTarget.getAssetUuid(), reqPack);

                if(resultCallback != null)
                    resultCallback.onPairingProgress(currentTarget, ++count);

            }
            //setLed(currentTarget.getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_OFF, 0, 0, 0);

            // query brand and model number
            if(TextUtils.isEmpty(currentTarget.getBrandName()) || TextUtils.isEmpty(currentTarget.getRemoteModelNum())) {
                IRBrandAndModelData brandAndModelData =
                        mIRController.getIRBrandAndModelName(mAssetType,currentTarget.getCodeNum(),currentTarget.getBrandName());
                if(brandAndModelData != null) {
                    if(TextUtils.isEmpty(currentTarget.getBrandName()))
                        currentTarget.setBrandName(brandAndModelData.getBrandName());
                    if(TextUtils.isEmpty(currentTarget.getRemoteModelNum()))
                        currentTarget.setRemoteModelNum(brandAndModelData.getRemoteModelNum());
                    if(TextUtils.isEmpty(currentTarget.getDevModelNumber()))
                        currentTarget.setDevModelNumber(brandAndModelData.getDevModelNum());
                    currentTarget.setBrandId(brandAndModelData.getBrandId());
                    currentTarget.setModelId(brandAndModelData.getModelId());
                }
                else {
                    if(TextUtils.isEmpty(currentTarget.getBrandName()))
                        currentTarget.setBrandName(Integer.toString(currentTarget.getCodeNum()));
                    if(TextUtils.isEmpty(currentTarget.getRemoteModelNum()))
                        currentTarget.setDevModelNumber(Integer.toString(currentTarget.getCodeNum()));
                    if(TextUtils.isEmpty(currentTarget.getDevModelNumber()))
                        currentTarget.setRemoteModelNum(currentTarget.getRemoteModelNum());
                }
            }

            //Step 3: setDeviceInfo(setIRPair)
            boolean ret = setIRPair(currentTarget);
            mIRController.log(TAG, "onStart setIRPair brandName=" + currentTarget.getBrandName() + ",model number=" + currentTarget.getRemoteModelNum() + ",model ID=" + currentTarget.getModelId() + ",brand ID=" + currentTarget.getBrandId() + ", ret=" + ret);
            if(ret == true) {
                mRoomHubMgr.setLed(currentTarget.getUuid(), RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 500, 0, 10);
            }
            else {
                mRoomHubMgr.setLed(currentTarget.getUuid(), RoomHubDef.LED_COLOR_RED, RoomHubDef.LED_FLASH, 500, 0, 10);
            }
            if(resultCallback != null)
                resultCallback.onPairingResult(currentTarget, ret);

        }

        @Override
        public void onLearning(String uuid, String assetUuid, IRLearningResultCallback resultCallback) {
            mIRController.log(TAG,"onLearning uuid="+uuid);
            if(!mIRLearningResultCallback.containsKey(uuid)) {
                HashMap<String,IRLearningResultCallback> callback = mIRLearningResultCallback.get(uuid);
                if(callback == null)
                    callback = new HashMap<>();
                callback.put(assetUuid,resultCallback);
                mIRLearningResultCallback.put(uuid, callback);
            }
            RoomHubData roomHubData = mRoomHubMgr.getRoomHubDataByUuid(uuid);
//            roomHubData.getRoomHubDevice().learningV1();
            roomHubData.getRoomHubDevice().learning();
        }
    };

    private boolean clearIRPair(String roomhub_uuid,String uuid){
        boolean ret = true;
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(roomhub_uuid,uuid);

        if(airPurifierData == null) return false;

        RoomHubDevice roomhub_device=airPurifierData.getRoomHubData().getRoomHubDevice();
        if(roomhub_device != null) {
            SetAssetInfoReqPack reqPack = new SetAssetInfoReqPack();
            reqPack.setUuid(uuid);
            reqPack.setAssetType(mAssetType);
            reqPack.setBrand("");
            reqPack.setDevice("");
            reqPack.setBrandId(0);
            reqPack.setModelId("");
            reqPack.setSubType(0);
            reqPack.setConnectionType(0);

            SetAssetInfoResPack resPack;
            resPack=roomhub_device.setAssetInfo(reqPack);
            if((resPack != null) && (resPack.getStatus_code() == ErrorKey.ASSET_INFO_NOT_SET))
                ret=false;
        }

        return ret;
    }

    private boolean setIRPair(ApIRParingInfo currentTarget){
        boolean ret = true;
        String uuid=currentTarget.getAssetUuid();
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(currentTarget.getUuid(),uuid);

        if(airPurifierData == null) return false;

        RoomHubDevice roomhub_device=airPurifierData.getRoomHubData().getRoomHubDevice();
        if(roomhub_device != null) {
            SetAssetInfoReqPack reqPack = new SetAssetInfoReqPack();
            reqPack.setUuid(uuid);
            reqPack.setAssetType(mAssetType);
            reqPack.setBrand(currentTarget.getBrandName());
            reqPack.setDevice(currentTarget.getRemoteModelNum());
            reqPack.setBrandId(currentTarget.getBrandId());
            reqPack.setModelId(currentTarget.getModelId());
            reqPack.setSubType(currentTarget.getSubType());
            reqPack.setConnectionType(currentTarget.getConnectionType());

            SetAssetInfoResPack resPack;
            resPack=roomhub_device.setAssetInfo(reqPack);
            if((resPack != null) && (resPack.getStatus_code() == ErrorKey.ASSET_INFO_NOT_SET))
                ret=false;
        }

        return ret;
    }

    private boolean checkIRData(String uuid, String irData) {
        RoomHubData roomHubData = mRoomHubMgr.getRoomHubDataByUuid(uuid);
        return roomHubData.getRoomHubDevice().checkIRData(IRUtils.hexStringToByteArray(irData))==ErrorKey.Success?true:false;
    }

    private void cleanIRControlData(String roomhub_uuid,String uuid) {
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(roomhub_uuid,uuid);
        CleanIRControlDataReqPack reqPack = new CleanIRControlDataReqPack();
        reqPack.setAssetType(mAssetType);
        reqPack.setUuid(uuid);
        airPurifierData.getRoomHubData().getRoomHubDevice().cleanIRControlData(reqPack);
    }

    private void addIRControlData(String roomhub_uuid,String uuid, AddIRControlDataReqPack reqPack) {
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(roomhub_uuid,uuid);
        airPurifierData.getRoomHubData().getRoomHubDevice().addIRControlData(reqPack);
    }

    public boolean IsAbility(String roomhub_uuid,String uuid,int key_id){
        AirPurifierData airPurifierData=getAirPurifierDataByUuid(roomhub_uuid,uuid);
        if(airPurifierData != null){
            int[] ability=airPurifierData.getAbilityLimit();
            if(ability != null){
                for(int i=0;i<ability.length;i++) {
                    if(ability[i] == key_id)
                        return true;
                }
            }
        }

        return false;
    }

    /* BLE Pair*/
    @Override
    public void startBLEPairing(String uuid) {

        BLEPairReqPack req_pack=new BLEPairReqPack();
        req_pack.setRoomHubUuid(uuid);
        req_pack.setCatetory(RoomHubDef.getCategory(mAssetType));
        req_pack.setAssetType(mAssetType);
        req_pack.setPrefixName(mContext.getResources().getString(R.string.config_ble_pair_prefix_name));
        req_pack.setExpireTime(mContext.getResources().getInteger(R.integer.config_ble_pair_expire_time));
        req_pack.setCallback(mBLECallback);
        req_pack.setShowRename(false);
        req_pack.setAssetName(mContext.getString(R.string.electric_ion));
        req_pack.setAssetIcon(R.drawable.img_ap_paring);
        req_pack.setBottomHint(mContext.getString(R.string.ble_pairing_ap_main_desc));
        req_pack.setUseDefault(true);
        req_pack.setShowDefaultUser(false);

        mBLEController=((RoomHubService)mContext).getBLEController();
        mBLEController.init(req_pack);
    }

    private BLEControllerCallback mBLECallback = new BLEControllerCallback() {


        @Override
        public int onAdd(BLEPairDef.ADD_STEP add_step, ScanAssetResult scaAsset) {
            int ret = ErrorKey.Success;
            switch (add_step){
                case ADD_ASSET:
                    ret=mRoomHubMgr.AddElectric(scaAsset.getRoomHubUuid(),scaAsset.getScanAsset().getUuid(),scaAsset.getScanAsset().getAssetType(),AssetDef.CONNECTION_TYPE_BT);
                    break;
                case SET_ASSET_INFO:
                    RoomHubData data=mRoomHubMgr.getRoomHubDataByUuid(scaAsset.getRoomHubUuid());
                    if(data != null) {
                        RoomHubDevice roomhub_device = data.getRoomHubDevice();
                        SetAssetInfoReqPack reqPack = new SetAssetInfoReqPack();
                        reqPack.setUuid(scaAsset.getScanAsset().getUuid());
                        reqPack.setAssetType(scaAsset.getScanAsset().getAssetType());
                        reqPack.setBrand(scaAsset.getScanAsset().getBrand());
                        reqPack.setDevice(scaAsset.getScanAsset().getDevice());
                        reqPack.setSubType(0);
                        reqPack.setConnectionType(AssetDef.CONNECTION_TYPE_BT);

                        log("onAdd SET_ASSETIN_INFO uuid=" + reqPack.getUuid() + " asset_type=" + reqPack.getAssetType() + " brand=" + reqPack.getBrand() +
                                " device=" + reqPack.getDevice() + " connection_type=" + reqPack.getConnectionType());
                        SetAssetInfoResPack resPack = roomhub_device.setAssetInfo(reqPack);

                        if (resPack != null)
                            ret=resPack.getStatus_code();

                        log("onAdd SET_ASSETIN_INFO ret="+ret);
                    }
                    break;
            }
            return ret;
        }

        @Override
        public int onRename(String uuid, String new_name) {
            return ErrorKey.Success;
        }

        @Override
        public int onRemove(String roomhub_uuid, String asset_uuid, int asset_type) {
            AirPurifierData data=getAirPurifierDataByUuid(roomhub_uuid,asset_uuid);
            if(data != null) {
                return mRoomHubMgr.RemoveElectric(roomhub_uuid,asset_uuid, asset_type);
            }
            return ErrorKey.Success;
        }
    };

}
