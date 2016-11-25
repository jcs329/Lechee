package com.quantatw.roomhub.manager.asset.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEControllerCallback;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.homeAppliance.AddScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.AddScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.BaseScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.BulbScheduleData;
import com.quantatw.sls.pack.homeAppliance.CommandBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.CommonReqPack;
import com.quantatw.sls.pack.homeAppliance.GetBulbAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.ModifyScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.ModifyScheduleBulbResPack;
import com.quantatw.sls.pack.homeAppliance.RemoveScheduleBulbReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetProfileReqPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.BulbAssetDetailInfoResPack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by 95010915 on 2016/04/26.
 */
public class BulbManager extends BaseAssetManager {
    private static final String TAG = BulbManager.class.getSimpleName();

    private HashMap<String, BulbData> bulbDataHashMap = new HashMap<String, BulbData>();

    private static final int MESSAGE_SET_SCHEDULE           =200;
    private static final int MESSAGE_SET_NAME               =201;


    public enum SCHEDULE_TYPE{
        ADD,
        EDIT,
        REMOVE,
        REMOVE_ALL
    }

    private HandlerThread mBulbBackgroundThread;
    private BulbBakgroundHandler mBulbBackgroundHandler;
    private BLEPairController mBLEController;

    private final class BulbBakgroundHandler extends Handler {
        public BulbBakgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_SET_SCHEDULE:
                    updateSchedule(msg.getData());
                    break;
                case MESSAGE_SET_NAME:
                    BulbMgr_setBulbName(msg.getData());
                    break;
            }
        }
    }

    @Override
    public void startup() {
    }

    @Override
    public void terminate() {

    }

    @Override
    public void configIRSetting(String uuid, String assetUuid) {

    }

    public BulbManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.BULB, context.getString(R.string.electric_lamp), R.drawable.bulb_btn_selector, AssetDef.CONNECTION_TYPE_BT);

        mBulbBackgroundThread=new HandlerThread("BulbMgrBackgroud");
        mBulbBackgroundThread.start();
        mBulbBackgroundHandler = new BulbBakgroundHandler(mBulbBackgroundThread.getLooper());
    }

    public BulbData getBulbDataByUuid(String Uuid){
        if(bulbDataHashMap ==null) return null;

        BulbData bulb_data = bulbDataHashMap.get(Uuid);
        if(bulb_data == null) {
            synchronized (bulbDataHashMap) {
                for (Iterator<BulbData> it = bulbDataHashMap.values().iterator(); it.hasNext(); ) {
                    bulb_data = it.next();
                    String roomhub_uuid = bulb_data.getRoomHubUuid();
                    if (roomhub_uuid.equals(Uuid))
                        return bulb_data;
                }
            }
        }else
            return bulb_data;

        return null;
    }

    @Override
    public void UpdateSchedule(SignalUpdateSchedulePack updateSchedulePack) {
        BulbData bulbData = getBulbDataByUuid(updateSchedulePack.getUuid());
        if (bulbData != null) {
            BulbScheduleData updateScheduleData = new BulbScheduleData(updateSchedulePack);
            List<BulbScheduleData> scheduleList = bulbData.getSchedules();
            for (BulbScheduleData scheduleData : scheduleList
                    ) {
                if (scheduleData.getIndex() == updateSchedulePack.getIndex()) {
                    scheduleList.remove(scheduleData);
                    break;
                }
            }
            scheduleList.add(updateScheduleData);
        }
    }

    @Override
    public void DeleteSchedule(SignalDeleteSchedulePack deleteSchedulePack) {
        BulbData bulbData = getBulbDataByUuid(deleteSchedulePack.getUuid());
        if (bulbData != null) {
            List<BulbScheduleData> scheduleList = bulbData.getSchedules();
            for (BulbScheduleData scheduleData : scheduleList
                    ) {
                if (scheduleData.getIndex() == deleteSchedulePack.getValue()) {
                    scheduleList.remove(scheduleData);
                    break;
                }
            }
        }
    }

    @Override
    public void AssetProfileChange(AssetProfile profile) {
        BulbData bulbData = getBulbDataByUuid(profile.getUuid());
        if (bulbData != null) {
            bulbData.setName(profile.getName());
            UpdateAssetData(mAssetType, bulbData);
        }
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new BulbData(data,mContext.getString(R.string.electric_lamp),R.drawable.btn_lamp_off);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        BulbData bulb_data=getBulbDataByUuid(asset_uuid);
        String str_models=mContext.getResources().getString(R.string.ac_na);
        if(bulb_data!=null){
            if(!TextUtils.isEmpty(bulb_data.getBrandName()) && !TextUtils.isEmpty(bulb_data.getModelNumber()))
                str_models=bulb_data.getBrandName() +"/"+bulb_data.getModelNumber();
        }
        return str_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("BulbMgr_AddDevice uuid="+uuid);

        BulbData bulb_data = getBulbDataByUuid(uuid);
        boolean bAdd=false;
        if(bulb_data ==null){
            log("BulbMgr_AddDevice is not exist");
            bAdd=true;
            bulb_data=(BulbData)asset_info_data;
        } else{
            log("BulbMgr_AddDevice device is exist");
        }

        if(bAdd){
            synchronized(bulbDataHashMap) {
                bulbDataHashMap.put(uuid, bulb_data);
            }
            NotifyAddDevice(mAssetType, bulb_data);
        }

        if(room_hub.IsOnLine()) {
            UpdateAssetDataAfterOnLine(bulb_data);
        }
    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        BulbData bulb_data = getBulbDataByUuid(uuid);

        log("BulbMgr_RemoveDevice uuid="+uuid);

        if (bulb_data != null){
            NotifyRemoveDevice(mAssetType, bulb_data);
            synchronized(bulbDataHashMap) {
                bulbDataHashMap.remove(uuid);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("BulbMgr_UpdateDevice uuid=" + uuid);

        BulbData bulb_data = getBulbDataByUuid(uuid);

        if (bulb_data !=null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(bulb_data);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {
        if(bulbDataHashMap == null) return;
        if(data == null) return;
        String uuid= data.getUuid();

        synchronized (bulbDataHashMap) {
            for (Iterator<BulbData> it = bulbDataHashMap.values().iterator(); it.hasNext(); ) {
                BulbData bulb_data = it.next();
                if (bulb_data.getRoomHubUuid().equals(uuid)) {
                    if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                        if (data.IsOnLine()) {
                            UpdateAssetDataAfterOnLine(bulb_data);
                            NotifyPageStatus(mAssetType,true, bulb_data);
                        }else{
                            NotifyPageStatus(mAssetType,false, bulb_data);
                        }
                    }
                    bulb_data.setRoomHubData(data);
                }
            }
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle) {
        return setBulbCommand(bundle);
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade){
        if(bulbDataHashMap == null) return;

        synchronized (bulbDataHashMap) {
            for (Iterator<BulbData> it = bulbDataHashMap.values().iterator(); it.hasNext(); ) {
                BulbData bulb_data = it.next();
                if (bulb_data.getRoomHubUuid().equals(uuid)) {
                   if(is_upgrade)
                       NotifyPageStatus(mAssetType,false, bulb_data);
                    else
                       NotifyPageStatus(mAssetType,true, bulb_data);
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType){
        if(ResPack == null) return;

        BulbAssetDetailInfoResPack res_pack=(BulbAssetDetailInfoResPack)ResPack;
        log("BulbMgr_AssetInfoChange ResPack:" + ResPack);
        BulbData bulb_data = getBulbDataByUuid(res_pack.getUuid());
        if(bulb_data != null){
            if(bulb_data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            GetBulbAssetInfoResPack bulb_asset_info = new GetBulbAssetInfoResPack();

            bulb_asset_info.setUuid(res_pack.getUuid());
            bulb_asset_info.setSubType(res_pack.getSubType());
            bulb_asset_info.setConnectionType(res_pack.getConnectionType());
            bulb_asset_info.setBrand(res_pack.getBrand());
            bulb_asset_info.setDevice(res_pack.getDevice());
            bulb_asset_info.setBrandId(res_pack.getBrandId());
            bulb_asset_info.setModelId(res_pack.getModelId());
            bulb_asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            bulb_asset_info.setPower(res_pack.getPower());
            bulb_asset_info.setLuminance(res_pack.getLuminance());

            bulb_data.setBulbAssetInfo(bulb_asset_info);

            UpdateAssetData(mAssetType, bulb_data);
        }
    }


    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        BulbData bulb_data = (BulbData)data;
        if((bulb_data != null) && bulb_data.getRoomHubData().IsOnLine()){
            if(bulb_data.getBulbAssetInfo() == ErrorKey.BULB_ASSET_INFO_INVAILD)
                RetryMessage(bulb_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    public int setPower(String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.POWER, uuid, value,0);
    }

    public int setLuminance(String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.LUMINANCE,uuid,value,0);
    }

    private int sendSetNameMessage(String uuid,String cmd_value){
        Message msg=new Message();
        msg.what=MESSAGE_SET_NAME;
        Bundle bundle=new Bundle();
        bundle.putString(KEY_UUID, uuid);
        bundle.putString(KEY_CMD_VALUE, cmd_value);
        msg.setData(bundle);
        mBulbBackgroundHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    private int setBulbCommand(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        BulbData bulb_data = getBulbDataByUuid(uuid);

        if(bulb_data == null) {
            log("setBulbCommand : not found device");
            return ErrorKey.BULB_DATA_NOT_FOUND;
        }

        CommandBulbReqPack req_pack=new CommandBulbReqPack();
        req_pack.setUuid(uuid);

        AssetDef.COMMAND_TYPE cmd_type=(AssetDef.COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        int cmd_value=bundle.getInt(KEY_CMD_VALUE);

        RoomHubDevice device= bulb_data.getRoomHubData().getRoomHubDevice();
        switch(cmd_type){
            case POWER:
                req_pack.setPower(cmd_value);
                if(bulb_data.getLuminance() ==0)
                    req_pack.setLuminance(60);
                else
                    req_pack.setLuminance(bulb_data.getLuminance());
                break;
            case LUMINANCE:
                req_pack.setPower(bulb_data.getPower());
                req_pack.setLuminance(cmd_value);
                break;
        }

        log("setBulbCommand uuid="+uuid+" cmd_type="+cmd_type+" cmd_value="+cmd_value);
        int ret_val= ErrorKey.Success;
        CommandResPack res_pack=device.commandBulb(req_pack);
        if(res_pack != null) {
            ret_val=res_pack.getStatus_code();
        }

        if(ret_val == ErrorKey.Success)
            device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        log("setBulbCommand ret_val="+ret_val);

        return ret_val;
    }

    public List<BulbData> getBulbList(String roomHubUUID){
        List<BulbData> bulbDataList = new ArrayList<>();
        for (BulbData bulb_data :bulbDataHashMap.values()
                ) {
            if (bulb_data.getRoomHubUuid().equals(roomHubUUID)) {
                bulbDataList.add(bulb_data);
            }
        }
        Collections.sort(bulbDataList, new Comparator<BulbData>() {
            @Override
            public int compare(BulbData lhs, BulbData rhs) {
                if (lhs.getName() == null||rhs.getName() == null){
                    return 0;
                }
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        return bulbDataList;
    }

    public int setBulbName(String uuid,String name){
        return sendSetNameMessage(uuid,name);
    }

    public void BulbMgr_setBulbName(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        String name=bundle.getString(KEY_CMD_VALUE);
        int ret = ErrorKey.BULB_DATA_NOT_FOUND;
        BulbData bulb_data = getBulbDataByUuid(uuid);
        if (bulb_data == null){
            log("Error: no bulb data");
        }else {
            RoomHubDevice device = bulb_data.getRoomHubData().getRoomHubDevice();
            SetAssetProfileReqPack reqPack = new SetAssetProfileReqPack();
            reqPack.setUuid(uuid);
            reqPack.setName(name);
            CommandResPack res_pack = device.setAssetProfile(reqPack);
            if (res_pack != null) {
                ret = res_pack.getStatus_code();
                log("res_pack status:" + res_pack.getStatus_code());
            }
        }
        onCommandResult(mAssetType,uuid,ret);
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
        req_pack.setAssetName(mContext.getString(R.string.electric_lamp));
        req_pack.setUseDefault(false);
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
                    ret=mRoomHubMgr.AddElectric(scaAsset.getRoomHubUuid(),scaAsset.getScanAsset().getUuid(),scaAsset.getScanAsset().getAssetType(), AssetDef.CONNECTION_TYPE_BT);
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

                        Log.d(TAG, "onAdd SET_ASSETIN_INFO uuid=" + reqPack.getUuid() + " asset_type=" + reqPack.getAssetType() + " brand=" + reqPack.getBrand() +
                                " device=" + reqPack.getDevice() + " connection_type=" + reqPack.getConnectionType());
                        SetAssetInfoResPack resPack = roomhub_device.setAssetInfo(reqPack);

                        if (resPack != null)
                            ret=resPack.getStatus_code();

                        Log.d(TAG,"onAdd SET_ASSETIN_INFO ret="+ret);
                    }
                    break;
                case SET_NAME:
                    ret=setBulbName(scaAsset.getScanAsset().getUuid(),scaAsset.getScanAsset().getDeviceName());
                    break;
            }
            return ret;
        }

        @Override
        public int onRename(String uuid, String new_name) {
            setBulbName(uuid,new_name);

            return ErrorKey.Success;
        }

        @Override
        public int onRemove(String roomhub_uuid, String asset_uuid, int asset_type) {
            BulbData data=getBulbDataByUuid(asset_uuid);
            if(data != null) {
                return mRoomHubMgr.RemoveElectric(roomhub_uuid,asset_uuid, asset_type);
            }
            return ErrorKey.Success;
        }
    };

    private void updateSchedule(Bundle bundle){
        SCHEDULE_TYPE cmd_type=(SCHEDULE_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        String uuid=bundle.getString(KEY_UUID);
        BulbScheduleData schedule = (BulbScheduleData) bundle.getSerializable(KEY_CMD_VALUE);
        int idx=bundle.getInt(KEY_CMD_VALUE);
        switch (cmd_type) {
            case ADD:
                addSchedule(uuid, schedule);
                break;
            case EDIT:
                modifySchedule(uuid, schedule);
            break;
            case REMOVE:
                removeSchedule(uuid,idx);
                break;
            case REMOVE_ALL:
                removeAllSchedule(uuid);
                break;
        }
    }

    private void addSchedule(String uuid, BulbScheduleData schedule) {
        int ret = ErrorKey.Success;
        BulbData bulb_data = getBulbDataByUuid(uuid);
        if (bulb_data != null) {
            RoomHubDevice device = bulb_data.getRoomHubData().getRoomHubDevice();
            AddScheduleBulbReqPack req = new AddScheduleBulbReqPack();
            req.setUuid(uuid);
            req.setStartTime(schedule.getStartTime());
            req.setEndTime(schedule.getEndTime());
            req.setRepeat(schedule.getRepeat());
            req.setState(schedule.getState());
            req.setWeekday(schedule.getWeekday());
            req.setGroupId(schedule.getGroupId());
            req.setPower(schedule.getPower());
            req.setLuminance(schedule.getLuminance());
            AddScheduleBulbResPack res = device.addScheduleBulb(req);

            if (res != null) {
                ret = res.getStatus_code();
            }
            Log.d(TAG, "addSchedule uuid= " + uuid + " ret=" + ret);

        }
        onCommandResult(mAssetType,uuid,ret);
    }

    private void modifySchedule(String uuid, BulbScheduleData schedule) {
        int ret = ErrorKey.Success;
        BulbData bulb_data = getBulbDataByUuid(uuid);
        if (bulb_data != null) {
            RoomHubDevice device = bulb_data.getRoomHubData().getRoomHubDevice();
            ModifyScheduleBulbReqPack req = new ModifyScheduleBulbReqPack();
            req.setUuid(uuid);
            req.setStartTime(schedule.getStartTime());
            req.setEndTime(schedule.getEndTime());
            req.setRepeat(schedule.getRepeat());
            req.setState(schedule.getState());
            req.setWeekday(schedule.getWeekday());
            req.setGroupId(schedule.getGroupId());
            req.setPower(schedule.getPower());
            req.setLuminance(schedule.getLuminance());
            req.setIndex(schedule.getIndex());
            ModifyScheduleBulbResPack res = device.modifyScheduleBulb(req);

            if (res != null)
                ret = res.getStatus_code();

            Log.d(TAG, "modifySchedule uuid= " + uuid + " ret=" + ret);
        }
        onCommandResult(mAssetType,uuid,ret);
    }

    private void removeSchedule(String uuid, int index) {
        int ret = ErrorKey.Success;
        BulbData bulb_data = getBulbDataByUuid(uuid);
        if (bulb_data != null) {
            RoomHubDevice device = bulb_data.getRoomHubData().getRoomHubDevice();
            RemoveScheduleBulbReqPack req = new RemoveScheduleBulbReqPack();
            req.setUuid(uuid);
            req.setIndex(index);
            BaseScheduleBulbResPack res = device.removeScheduleBulb(req);

            if (res != null)
                ret = res.getStatus_code();

            Log.d(TAG, "removeSchedule uuid= " + uuid + " ret=" + ret);
        }
        onCommandResult(mAssetType,uuid,ret);
    }

    private void removeAllSchedule(String uuid) {
        int ret = ErrorKey.Success;
        BulbData bulb_data = getBulbDataByUuid(uuid);
        if (bulb_data != null) {
            RoomHubDevice device = bulb_data.getRoomHubData().getRoomHubDevice();
            CommonReqPack req = new CommonReqPack();
            req.setUuid(uuid);
            req.setAssetType(mAssetType);
            BaseScheduleBulbResPack res = device.removeAllScheduleBulb(req);

            if (res != null)
                ret = res.getStatus_code();

            Log.d(TAG, "removeSchedule uuid= " + uuid + " ret=" + ret);
        }
        onCommandResult(mAssetType, uuid, ret);
    }

    public void AddSchedule(String uuid, BulbScheduleData schedule) {
        Message msg=new Message();
        msg.what=MESSAGE_SET_SCHEDULE;
        Bundle bundle=new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.ADD);
        bundle.putString(KEY_UUID, uuid);
        bundle.putSerializable(KEY_CMD_VALUE, schedule);

        msg.setData(bundle);
        mBulbBackgroundHandler.sendMessage(msg);
    }

    public void ModifySchedule(String uuid, BulbScheduleData schedule) {
        Message msg=new Message();
        msg.what=MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.EDIT);
        bundle.putString(KEY_UUID, uuid);
        bundle.putSerializable(KEY_CMD_VALUE, schedule);
        msg.setData(bundle);
        mBulbBackgroundHandler.sendMessage(msg);
    }

    public void RemoveSchedule(String uuid,int index){
        Message msg=new Message();
        msg.what = MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.REMOVE);
        bundle.putString(KEY_UUID, uuid);
        bundle.putInt(KEY_CMD_VALUE, index);

        msg.setData(bundle);
        mBulbBackgroundHandler.sendMessage(msg);
    }

    public void RemoveAllSchedule(String uuid){
        Message msg=new Message();
        msg.what = MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.REMOVE_ALL);
        bundle.putString(KEY_UUID, uuid);
        msg.setData(bundle);
        mBulbBackgroundHandler.sendMessage(msg);
    }

    public HashMap<Integer,HashMap<String,BulbScheduleData>> getGroupSchedule(String roomHubUUID){
        HashMap<Integer,HashMap<String,BulbScheduleData>> groupSchedule = new HashMap<>();
        List<BulbData> bulbs = getBulbList(roomHubUUID);
        for (BulbData bulbData:bulbs
             ) {
            for (BulbScheduleData scheduleData:bulbData.getSchedules()
                 ) {
                HashMap<String,BulbScheduleData> schedules = groupSchedule.get(scheduleData.getGroupId());
                if (schedules == null){
                    schedules = new HashMap<>();
                    groupSchedule.put(scheduleData.getGroupId(),schedules);
                }
                schedules.put(bulbData.getAssetUuid(),scheduleData);
            }
        }
        return groupSchedule;
    }
}
