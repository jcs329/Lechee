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
import com.quantatw.sls.pack.homeAppliance.GetFanAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.FanAssetDetailInfoResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by 95010915 on 2016/01/28.
 */
public class FANManager extends BaseAssetManager {
    private static final String TAG = FANManager.class.getSimpleName();

    private HashMap<String, FANData> mFANList = new HashMap<String, FANData>();
    private IRController mIRController;

    private HashMap<String, HashMap<String, IRLearningResultCallback>> mIRLearningResultCallback = new HashMap<>();

    @Override
    public void startup() {
        mIRController = ((RoomHubService) mContext).getIRController();
    }

    @Override
    public void terminate() {

    }

    public FANManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.FAN, context.getString(R.string.electric_fan), R.drawable.fan_btn_selector, AssetDef.CONNECTION_TYPE_IR);
    }

    public FANData getFANDataByUuid(String Uuid){
        if(mFANList==null) return null;

        FANData fan_data=mFANList.get(Uuid);
        if(fan_data == null) {
            synchronized (mFANList) {
                for (Iterator<FANData> it = mFANList.values().iterator(); it.hasNext(); ) {
                    fan_data = it.next();
                    String roomhub_uuid = fan_data.getRoomHubUuid();
                    if (roomhub_uuid.equals(Uuid))
                        return fan_data;
                }
            }
        }else
            return fan_data;

        return null;
    }

    public int setKeyId(String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.KEY_ID, uuid, value, 0);
    }

    private int setFANCommand(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        FANData fan_data=getFANDataByUuid(uuid);

        if(fan_data == null) {
            log("setFANCommand : not found device");
            return ErrorKey.FAN_DATA_NOT_FOUND;
        }
/*
        long thread_id=Thread.currentThread().getId();
        mCmdResult.put(thread_id, uuid);
        Object obj = thread_id;
        mCommandHandler.sendMessageDelayed(mCommandHandler.obtainMessage(MESSAGE_COMMAND_TIMEOUT, obj),
                mContext.getResources().getInteger(R.integer.config_send_command_timeout));
*/
        CommandRemoteControlReqPack req_pack=new CommandRemoteControlReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(uuid);

        AssetDef.COMMAND_TYPE cmd_type=(AssetDef.COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        int cmd_value=bundle.getInt(KEY_CMD_VALUE);

        RoomHubDevice device=fan_data.getRoomHubData().getRoomHubDevice();
        switch(cmd_type){
            case KEY_ID:
                req_pack.setKeyId(cmd_value);
                break;
        }

        log("setFANCommand uuid="+uuid+" cmd_type="+cmd_type+" cmd_value="+cmd_value);
        int ret_val= ErrorKey.Success;
        CommandResPack res_pack=device.commandRemoteControl(req_pack);
        if(res_pack != null) {
            ret_val=res_pack.getStatus_code();
        }

        if(ret_val == ErrorKey.Success)
            device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        log("setFANCommand ret_val=" + ret_val);

//        mCommandHandler.removeMessages(MESSAGE_COMMAND_TIMEOUT, obj);
//        ProgressCmdResultCallback(thread_id, ret_val);
        return ret_val;
    }

    @Override
    public void configIRSetting(String uuid, String assetUuid) {
        mIRController.configIR(uuid, assetUuid, mAssetType, mIRParingAction);
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new FANData(data,mContext.getString(R.string.electric_fan),R.drawable.btn_fan2);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        FANData fan_data=getFANDataByUuid(asset_uuid);
        String str_fan_models=mContext.getResources().getString(R.string.ac_na);
        if(fan_data!=null){
            if(!TextUtils.isEmpty(fan_data.getBrandName()) && !TextUtils.isEmpty(fan_data.getModelNumber()))
                str_fan_models=fan_data.getBrandName() +"/"+fan_data.getModelNumber();
        }
        return str_fan_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("FANgr_AddDevice uuid="+uuid);

        FANData fan_data=getFANDataByUuid(uuid);
        boolean bAdd=false;
        if(fan_data==null){
            log("FANgr_AddDevice is not exist");
            fan_data=(FANData)asset_info_data;
            bAdd=true;
        } else{
            log("FANgr_AddDevice device is exist");
        }

        log("FANgr_AddDevice bAdd="+bAdd);
        if(bAdd){
            synchronized(mFANList) {
                mFANList.put(uuid, (FANData) asset_info_data);
            }
            log("FANgr_AddDevice NotifyAddDevice");
            NotifyAddDevice(mAssetType, fan_data);
        }

        log("FANgr_AddDevice roomhub is_online="+room_hub.IsOnLine());
        if(room_hub.IsOnLine()) {
            UpdateAssetDataAfterOnLine(fan_data);
        }

    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        FANData fan_data = getFANDataByUuid(uuid);

        log("FANMgr_RemoveDevice uuid=" + uuid);

        if (fan_data != null){
            NotifyRemoveDevice(mAssetType, fan_data);
            synchronized(mFANList) {
                mFANList.remove(uuid);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("FANMgr_UpdateDevice uuid=" + uuid);

        FANData fan_data=getFANDataByUuid(uuid);

        if(fan_data != null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(fan_data);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {
        if(mFANList == null) return;

        String uuid=data.getUuid();
        synchronized (mFANList) {
            for (Iterator<FANData> it = mFANList.values().iterator(); it.hasNext(); ) {
                FANData fan_data = it.next();
                if (fan_data.getRoomHubUuid().equals(uuid)) {
                    if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                        if (data.IsOnLine()) {
                            UpdateAssetDataAfterOnLine(fan_data);
                            NotifyPageStatus(mAssetType,true,fan_data);
                        }else{
                            NotifyPageStatus(mAssetType,false,fan_data);
                        }
                    }
                    fan_data.setRoomHubData(data);
                }
            }
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle) {
        return setFANCommand(bundle);
        /*
        new Thread() {
            @Override
            public void run() {
                log("FANMgr_SendCommand send command :::");
                int ret=setFANCommand(bundle);
                log("FANMgr_SendCommand send command ::: ret=" + ret);
            }
        }.start();
        return ErrorKey.Success;
        */
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade) {
        if(mFANList == null) return;

        synchronized (mFANList) {
            for (Iterator<FANData> it = mFANList.values().iterator(); it.hasNext(); ) {
                FANData fan_data = it.next();
                if (fan_data.getRoomHubUuid().equals(uuid)) {
                    if(is_upgrade)
                        NotifyPageStatus(mAssetType,false,fan_data);
                    else
                        NotifyPageStatus(mAssetType,true,fan_data);
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {
        FanAssetDetailInfoResPack res_pack=(FanAssetDetailInfoResPack)ResPack;

        FANData fan_data=getFANDataByUuid(res_pack.getUuid());
        if(fan_data != null){
            log("FANMgr_AssetInfoChange uuid="+fan_data.getAssetUuid()+" source_type="+sourceType);
            if(fan_data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            if(fan_data.setAbilityLimit(res_pack.getBrand(), res_pack.getDevice()) == ErrorKey.FAN_ABILITY_INVALID)
                RetryMessage(fan_data,MESSAGE_RETRY_ABILITY_LIMIT);

            GetFanAssetInfoResPack fan_asset_info = new GetFanAssetInfoResPack();
            fan_asset_info.setUuid(res_pack.getUuid());
            fan_asset_info.setPower(res_pack.getPower());
            fan_asset_info.setSwing(res_pack.getSwing());
            fan_asset_info.setSpeed(res_pack.getSpeed());
            fan_asset_info.setION(res_pack.getION());
            fan_asset_info.setHumidification(res_pack.getHumidification());
            fan_asset_info.setSavePower(res_pack.getSavePower());
            fan_asset_info.setMode(res_pack.getMode());

            fan_asset_info.setSubType(res_pack.getSubType());
            fan_asset_info.setConnectionType(res_pack.getConnectionType());
            fan_asset_info.setBrand(res_pack.getBrand());
            fan_asset_info.setDevice(res_pack.getDevice());
            fan_asset_info.setBrandId(res_pack.getBrandId());
            fan_asset_info.setModelId(res_pack.getModelId());
            fan_asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            fan_data.setFanAssetInfo(fan_asset_info);

            UpdateAssetData(mAssetType,fan_data);
        }
    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        FANData fan_data = (FANData)data;
        log("BaseAsset_GetAssetInfo roomhub is online="+fan_data.getRoomHubData().IsOnLine());
        if((fan_data != null) && fan_data.getRoomHubData().IsOnLine()){
            if(fan_data.getFanAssetInfo() == ErrorKey.FAN_ASSET_INFO_INVALID)
                RetryMessage(fan_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    @Override
    public void BaseAsset_GetAbilityLimit(Object data) {
        FANData fan_data = (FANData)data;
        if((fan_data != null) && (fan_data.getRoomHubData().IsOnLine())){
            if(fan_data.getAssetAbility() == ErrorKey.FAN_ABILITY_INVALID)
                RetryMessage(fan_data,MESSAGE_RETRY_ABILITY_LIMIT);
        }
    }
    /*
    * IR Paring
     */
    public void BaseAsset_LearningResult(LearningResultResPack learningResultResPack){
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

            clearIRPair(currentTarget.getAssetUuid());

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

    private boolean clearIRPair(String uuid){
        boolean ret = true;
        FANData fan_data=getFANDataByUuid(uuid);

        if(fan_data == null) return false;

        RoomHubDevice roomhub_device=fan_data.getRoomHubData().getRoomHubDevice();
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
        FANData fan_data=getFANDataByUuid(uuid);

        if(fan_data == null) return false;

        RoomHubDevice roomhub_device=fan_data.getRoomHubData().getRoomHubDevice();
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

    private void cleanIRControlData(String uuid) {
        FANData fan_data=getFANDataByUuid(uuid);
        CleanIRControlDataReqPack reqPack = new CleanIRControlDataReqPack();
        reqPack.setAssetType(mAssetType);
        reqPack.setUuid(uuid);
        fan_data.getRoomHubData().getRoomHubDevice().cleanIRControlData(reqPack);
    }

    private boolean addIRControlData(String uuid, AddIRControlDataReqPack reqPack) {
        FANData fan_data=getFANDataByUuid(uuid);
        BaseHomeApplianceResPack baseHomeApplianceResPack = fan_data.getRoomHubData().getRoomHubDevice().addIRControlData(reqPack);
        return baseHomeApplianceResPack.getStatus_code()==ErrorKey.Success?true:false;
    }

    public boolean IsAbility(String uuid,int key_id){
        FANData fan_data=getFANDataByUuid(uuid);
        if(fan_data != null){
            int[] ability=fan_data.getAbilityLimit();
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
