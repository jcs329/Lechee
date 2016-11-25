package com.quantatw.roomhub.manager.asset.manager;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.listener.IRControllerCallback;
import com.quantatw.roomhub.listener.IRLearningResultCallback;
import com.quantatw.roomhub.listener.IRParingActionCallback;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.ui.IRSettingDataValues;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
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
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.CleanIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.GetTVAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.detail.TVAssetDetailInfoResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by 10110012 on 2016/05/03.
 */
public class TVManager extends BaseAssetManager {
    private static final String TAG = TVManager.class.getSimpleName();

    private HashMap<String, TVData> mTVList = new HashMap<String, TVData>();
    private IRController mIRController;

    private HashMap<String, HashMap<String, IRLearningResultCallback>> mIRLearningResultCallback = new HashMap<>();

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

    public TVManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.TV, context.getString(R.string.electric_tv), R.drawable.btn_tv_selector, AssetDef.CONNECTION_TYPE_IR);
    }

    public TVData getTVDataByUuid(String Uuid){
        if(mTVList==null) return null;

        TVData data=mTVList.get(Uuid);
        if(data == null) {
            synchronized (mTVList) {
                for (Iterator<TVData> it = mTVList.values().iterator(); it.hasNext(); ) {
                    data = it.next();
                    String roomhub_uuid = data.getRoomHubUuid();
                    if (roomhub_uuid.equals(Uuid))
                        return data;
                }
            }
        }else
            return data;

        return null;
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new TVData(data,mContext.getString(R.string.electric_tv),R.drawable.btn_tv);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        TVData tv_data=getTVDataByUuid(asset_uuid);
        String str_fan_models=mContext.getResources().getString(R.string.ac_na);
        if(tv_data!=null){
            if(!TextUtils.isEmpty(tv_data.getBrandName()) && !TextUtils.isEmpty(tv_data.getModelNumber()))
                str_fan_models=tv_data.getBrandName() +"/"+tv_data.getModelNumber();
        }
        return str_fan_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("TVMgr_AddDevice uuid="+uuid);

        TVData tv_data=getTVDataByUuid(uuid);
        boolean bAdd=false;
        if(tv_data==null){
            log("TVMgr_AddDevice is not exist");
            bAdd=true;
            tv_data=(TVData)asset_info_data;
        } else{
            log("TVMgr_AddDevice device is exist");
        }

        if(bAdd){
            synchronized(mTVList) {
                mTVList.put(uuid, tv_data);
            }
            NotifyAddDevice(mAssetType, tv_data);
        }

        if(room_hub.IsOnLine()) {
            UpdateAssetDataAfterOnLine(tv_data);
        }
    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        TVData tv_data=getTVDataByUuid(uuid);

        log("TVMgr_RemoveDevice uuid="+uuid);

        if (tv_data != null){
            NotifyRemoveDevice(mAssetType, tv_data);
            synchronized(mTVList) {
                mTVList.remove(uuid);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("TVMgr_UpdateDevice uuid="+uuid);

        TVData tv_data=getTVDataByUuid(uuid);

        if(tv_data!=null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(tv_data);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {
        if(mTVList == null) return;
        if(data == null) return;
        String uuid=data.getUuid();

        synchronized (mTVList) {
            for (Iterator<TVData> it = mTVList.values().iterator(); it.hasNext(); ) {
                TVData tv_data = it.next();
                if (tv_data.getRoomHubUuid().equals(uuid)) {
                    if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                        if (data.IsOnLine()) {
                            UpdateAssetDataAfterOnLine(tv_data);
                            NotifyPageStatus(mAssetType,true,tv_data);
                        }else{
                            NotifyPageStatus(mAssetType,false,tv_data);
                        }
                    }
                    tv_data.setRoomHubData(data);
                }
            }
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle) {
        return setTVCommand(bundle);
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade) {
        if(mTVList == null) return;

        synchronized (mTVList) {
            for (Iterator<TVData> it = mTVList.values().iterator(); it.hasNext(); ) {
                TVData data = it.next();
                if (data.getRoomHubUuid().equals(uuid)) {
                   if(is_upgrade)
                       NotifyPageStatus(mAssetType,false,data);
                    else
                       NotifyPageStatus(mAssetType,true,data);
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {
        if(ResPack == null) return;
        TVAssetDetailInfoResPack res_pack=(TVAssetDetailInfoResPack)ResPack;

        TVData data=getTVDataByUuid(res_pack.getUuid());
        if(data != null){
            if(data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            if(data.setAbilityLimit(res_pack.getBrand(), res_pack.getDevice()) == ErrorKey.TV_ABILITY_INVALID)
                RetryMessage(data,MESSAGE_RETRY_ABILITY_LIMIT);

            GetTVAssetInfoResPack asset_info = new GetTVAssetInfoResPack();
            asset_info.setUuid(res_pack.getUuid());
            asset_info.setPower(res_pack.getPower());

            asset_info.setSubType(res_pack.getSubType());
            asset_info.setConnectionType(res_pack.getConnectionType());
            asset_info.setBrand(res_pack.getBrand());
            asset_info.setDevice(res_pack.getDevice());
            asset_info.setBrandId(res_pack.getBrandId());
            asset_info.setModelId(res_pack.getModelId());
            asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            data.setTVAssetInfo(asset_info);
            log("TVMgr_AssetInfoChange Brand" + res_pack.getBrand());
            log("TVMgr_AssetInfoChange Device" + res_pack.getDevice());

            UpdateAssetData(mAssetType,data);
        }
    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        TVData tv_data = (TVData)data;
        log("BaseAsset_GetAssetInfo roomhub is online="+tv_data.getRoomHubData().IsOnLine());
        if((tv_data != null) && tv_data.getRoomHubData().IsOnLine()){
            if(tv_data.getTVAssetInfo() == ErrorKey.TV_ASSET_INFO_INVALID)
                RetryMessage(tv_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    @Override
    public void BaseAsset_GetAbilityLimit(Object data) {
        TVData tv_data = (TVData)data;
        if((tv_data != null) && (tv_data.getRoomHubData().IsOnLine())){
            if(tv_data.getAssetAbility() == ErrorKey.TV_ABILITY_INVALID)
                RetryMessage(tv_data,MESSAGE_RETRY_ABILITY_LIMIT);
        }
    }

    public int setPowerStatus(String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.POWER, uuid, value, 0);
    }

    public int setKeyId(String uuid,int value){
        if (!IsAbility(uuid, value)){
            onCommandResult(mAssetType,uuid,ErrorKey.TV_ABILITY_INVALID);
            return ErrorKey.TV_ABILITY_INVALID;
        }else {
            return sendCommandMessage(AssetDef.COMMAND_TYPE.KEY_ID, uuid, value, 0);
        }
    }

    private int setTVCommand(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        TVData data=getTVDataByUuid(uuid);

        if(data == null) {
            log("setTVCommand : not found device");
            return ErrorKey.TV_DATA_NOT_FOUND;
        }

        CommandRemoteControlReqPack req_pack=new CommandRemoteControlReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(uuid);

        AssetDef.COMMAND_TYPE cmd_type=(AssetDef.COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        int cmd_value=bundle.getInt(KEY_CMD_VALUE);

        RoomHubDevice device=data.getRoomHubData().getRoomHubDevice();
        switch(cmd_type){
            case POWER:
            case KEY_ID:
                req_pack.setKeyId(cmd_value);
                break;
        }

        log("setTVCommand uuid="+uuid+" cmd_type="+cmd_type+" cmd_value="+cmd_value);
        int ret_val=-7;
//        ret_val=device.command(roomhub_cmd_value);
        CommandResPack res_pack=device.commandRemoteControl(req_pack);
        if(res_pack != null) {
            ret_val=res_pack.getStatus_code();
        }

        if(ret_val == ErrorKey.Success)
            device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        log("setTVCommand ret_val="+ret_val);

        return ret_val;
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
                            assetUuid, s0, s1, 0, s3, IRSettingDataValues.IR_LEARNING_CHECK_TYPE.IR_AV_CHECK_TYPE);

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
            setIRPair(currentTarget.getAssetUuid(), "", "",0,"");

            //Step 1: cleanIRControlData
            mIRController.log(TAG, "onStart cleanIRControlData:::");
            cleanIRControlData(currentTarget.getAssetUuid());

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
                boolean result = addIRControlData(currentTarget.getAssetUuid(), reqPack);
                if(!result) {
                    mIRController.log(TAG, "onStart addIRControlData()::: FAIL => do Retry:");
                    final int RETRY_TIMES = 3;
                    int retry;
                    for(retry=0;retry<RETRY_TIMES;retry++) {
                        mIRController.log(TAG, "onStart addIRControlData()::: Retry "+Integer.toString(retry+1));
                        if(addIRControlData(currentTarget.getAssetUuid(), reqPack) == true) {
                            break;
                        }
                    }
                    if(retry == RETRY_TIMES) {
                        mIRController.log(TAG, "onStart addIRControlData()::: Retry "+Integer.toString(RETRY_TIMES)+" times still failed");
                        if(resultCallback != null)
                            resultCallback.onPairingResult(currentTarget, false);
                        return;
                    }
                }
                mIRController.log(TAG, "onStart addIRControlData():::" + Integer.toString(++i));

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
            boolean ret = setIRPair(currentTarget.getAssetUuid(), currentTarget.getBrandName(), currentTarget.getRemoteModelNum(),currentTarget.getBrandId(),currentTarget.getModelId());
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

    public boolean setIRPair(String uuid,String brand_name,String device_model,int brandId,String modelId){
        boolean ret = true;
        TVData data=getTVDataByUuid(uuid);

        if(data == null) return false;

        RoomHubDevice roomhub_device=data.getRoomHubData().getRoomHubDevice();
        if(roomhub_device != null) {
            SetAssetInfoReqPack reqPack = new SetAssetInfoReqPack();
            reqPack.setUuid(uuid);
            reqPack.setAssetType(mAssetType);
            reqPack.setBrand(brand_name);
            reqPack.setDevice(device_model);
            reqPack.setBrandId(brandId);
            reqPack.setModelId(modelId);
            roomhub_device.setAssetInfo(reqPack);
        }

        return ret;
    }

    private boolean checkIRData(String uuid, String irData) {
        RoomHubData roomHubData = mRoomHubMgr.getRoomHubDataByUuid(uuid);
        return roomHubData.getRoomHubDevice().checkIRData(IRUtils.hexStringToByteArray(irData))==0?true:false;
    }

    private void cleanIRControlData(String uuid) {
        TVData data=getTVDataByUuid(uuid);
        CleanIRControlDataReqPack reqPack = new CleanIRControlDataReqPack();
        reqPack.setAssetType(mAssetType);
        reqPack.setUuid(uuid);
        data.getRoomHubData().getRoomHubDevice().cleanIRControlData(reqPack);
    }

    private boolean addIRControlData(String uuid, AddIRControlDataReqPack reqPack) {
        TVData data = getTVDataByUuid(uuid);
        BaseHomeApplianceResPack baseHomeApplianceResPack = data.getRoomHubData().getRoomHubDevice().addIRControlData(reqPack);
        return baseHomeApplianceResPack.getStatus_code() == 0;
    }

    public boolean IsAbility(String uuid){
        TVData data=getTVDataByUuid(uuid);
        if(data!=null) {
            int[] ability_limit=data.getAbilityLimit();
            if((ability_limit != null) && (ability_limit.length > 0))
                return true;
        }
        return false;
    }

    public boolean IsAbility(String uuid,int key_id){
        TVData data=getTVDataByUuid(uuid);
        if(data != null){
            int[] ability=data.getAbilityLimit();
            if(ability != null){
                for(int i=0;i<ability.length;i++) {
                    if(ability[i] == key_id)
                        return true;
                }
            }
        }

        return false;
    }
}
