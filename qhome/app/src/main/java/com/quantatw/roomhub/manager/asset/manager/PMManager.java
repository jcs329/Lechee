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
import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.GetPMAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.detail.PMAssetDetailInfoResPack;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by 95010915 on 2016/01/28.
 */
public class PMManager extends BaseAssetManager {
    private static final String TAG = PMManager.class.getSimpleName();

    //private HashMap<String, PMData> mPMList = new HashMap<String, PMData>();
    private HashMap<String, HashMap<String,PMData>> mPMList = new HashMap<String, HashMap<String,PMData>>();

    public static final String KEY_NOTICE_SETTING = "notice_setting";

    private static final int MESSAGE_PM_SET_NOTICE_SETTING = 200;

    private HandlerThread mPM25BackgroundThread;
    private PM25BackgroundHandler mPM25BackgroundHandler;
    private BLEPairController mBLEController;

    private final class PM25BackgroundHandler extends Handler {
        public PM25BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PM_SET_NOTICE_SETTING:
                    PMMgr_SetNoticeSetting(msg.getData().getString(KEY_UUID),msg.getData().getString(KEY_ASSET_UUID), (NoticeSetting) msg.getData().getParcelable(KEY_NOTICE_SETTING));
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

    public PMManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.PM25, context.getString(R.string.electric_pm), R.drawable.pm25_btn_selector, AssetDef.CONNECTION_TYPE_BT);

        mPM25BackgroundThread=new HandlerThread("PM25MgrBackgroud");
        mPM25BackgroundThread.start();
        mPM25BackgroundHandler = new PM25BackgroundHandler(mPM25BackgroundThread.getLooper());
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new PMData(data,mContext.getString(R.string.electric_pm),R.drawable.btn_pm_main);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        PMData pm_data=getPMDataByUuid(null, asset_uuid);
        String str_models=mContext.getResources().getString(R.string.ac_na);
        if(pm_data!=null){
            if(!TextUtils.isEmpty(pm_data.getBrandName()) && !TextUtils.isEmpty(pm_data.getModelNumber()))
                str_models=pm_data.getBrandName() +"/"+pm_data.getModelNumber();
        }
        return str_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();
        String roomhub_uuid=room_hub.getUuid();

        PMData pm_data=getPMDataByUuid(roomhub_uuid,uuid);
        boolean bAdd=false;
        if(pm_data==null){
            log("PMMgr_AddDevice is not exist");
            bAdd=true;
            pm_data=(PMData)asset_info_data;
        } else{
            log("PMMgr_AddDevice device is exist");
        }
        if(bAdd){
            synchronized(mPMList) {
                HashMap<String,PMData> pm_data_map = new HashMap<String,PMData>();
                pm_data_map.put(uuid, pm_data);
                log("PMMgr_AddDevice add roomhub_uuid=" + roomhub_uuid + " asset_uuid=" + uuid);
                mPMList.put(roomhub_uuid, pm_data_map);
            }
            NotifyAddDevice(mAssetType, pm_data);
        }
        if(room_hub.IsOnLine()) {
            UpdateAssetDataAfterOnLine(pm_data);
        }
    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();
        String roomhub_uuid=room_hub.getUuid();

        HashMap<String,PMData> pm_data_list=mPMList.get(roomhub_uuid);
        if (pm_data_list != null){
            PMData pm_data=pm_data_list.get(uuid);
            if(pm_data != null){
                NotifyRemoveDevice(mAssetType, pm_data);
                pm_data_list.remove(uuid);
                log("PMMgr_RemoveDevice asset_uuid=" + uuid);
            }
            if (pm_data_list.size() == 0) {
                mPMList.remove(roomhub_uuid);
                log("PMMgr_RemoveDevice roomhub_uuid=" + roomhub_uuid);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub){
        String uuid=asset_info_data.getAssetUuid();

        log("PMMgr_UpdateDevice uuid="+uuid);

        PMData pm_data=getPMDataByUuid(room_hub.getUuid(), uuid);

        if(pm_data!=null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(pm_data);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {
        if(mPMList == null) return;
        if(data == null) return;
        String roomhub_uuid=data.getUuid();

        synchronized (mPMList) {
            HashMap<String,PMData> pm_data_map=mPMList.get(roomhub_uuid);
            if(pm_data_map != null){
                for (Iterator<PMData> it = pm_data_map.values().iterator(); it.hasNext(); ) {
                    PMData pm_data = it.next();
                    if (pm_data.getRoomHubUuid().equals(roomhub_uuid)) {
                        if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                            if (data.IsOnLine()) {
                                UpdateAssetDataAfterOnLine(pm_data);
                                NotifyPageStatus(mAssetType,true,pm_data);
                            }else{
                                NotifyPageStatus(mAssetType,false,pm_data);
                            }
                        }
                        pm_data.setRoomHubData(data);
                    }
                }
            }
        }
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade) {
        if(mPMList == null) return;

        synchronized (mPMList) {
            HashMap<String,PMData> pm_data_map=mPMList.get(uuid);
            if(pm_data_map != null) {
                for (Iterator<PMData> it = pm_data_map.values().iterator(); it.hasNext(); ) {
                    PMData pm_data = it.next();
                    if (pm_data.getRoomHubUuid().equals(uuid)) {
                        if (is_upgrade)
                            NotifyPageStatus(mAssetType, false, pm_data);
                        else
                            NotifyPageStatus(mAssetType, true, pm_data);
                    }
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {
        PMAssetDetailInfoResPack res_pack=(PMAssetDetailInfoResPack)ResPack;

        PMData pm_data = getPMDataByUuid(res_pack.getRoomHubUUID(), res_pack.getUuid());
        if(pm_data != null){
            if(pm_data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;
            log("setPM25AssetInfo roomhub_uuid="+pm_data.getRoomHubUuid() +" asset_uuid=" + res_pack.getUuid() + " onlinestatus=" + res_pack.getOnLineStatus());
            GetPMAssetInfoResPack pm_asset_info=new GetPMAssetInfoResPack();

            pm_asset_info.setUuid(res_pack.getUuid());
            pm_asset_info.setValue(res_pack.getValue());
            pm_asset_info.setCapacity(res_pack.getCapacity());
            pm_asset_info.setAdapter(res_pack.getAdapter());
            pm_asset_info.setTime(res_pack.getTime());
            pm_asset_info.setNotifyValue(res_pack.getNotifyValue());
            pm_asset_info.setAutoOn(res_pack.getAutoOn());

            pm_asset_info.setSubType(res_pack.getSubType());
            pm_asset_info.setConnectionType(res_pack.getConnectionType());
            pm_asset_info.setBrand(res_pack.getBrand());
            pm_asset_info.setDevice(res_pack.getDevice());
            pm_asset_info.setBrandId(res_pack.getBrandId());
            pm_asset_info.setModelId(res_pack.getModelId());
            pm_asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            pm_data.setPM25AssetInfo(pm_asset_info);

            UpdateAssetData(mAssetType,pm_data);
        }
    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        PMData pm_data = (PMData)data;
        if((pm_data != null) && pm_data.getRoomHubData().IsOnLine()){
            if(pm_data.getPM25AssetInfo() == ErrorKey.PM_ASSET_INFO_INVAILD)
                RetryMessage(pm_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle) {
        return setPMRelod(bundle);
    }

    private int setPMRelod(Bundle bundle){
        int ret_val = ErrorKey.PM_COMMAND_FAILURE;
        String uuid=bundle.getString(KEY_UUID);
        String asset_uuid=bundle.getString(KEY_ASSET_UUID);

        log("setPMRelod uuid=" + uuid +" asset_uuid=" + asset_uuid);

        PMData pm_data=getPMDataByUuid(uuid,asset_uuid);
        if(pm_data != null) {
            CommandRemoteControlReqPack req_pack = new CommandRemoteControlReqPack();
            req_pack.setAssetType(mAssetType);
            req_pack.setUuid(asset_uuid);

            AssetDef.COMMAND_TYPE cmd_type = (AssetDef.COMMAND_TYPE) bundle.getSerializable(KEY_CMD_TYPE);
            int cmd_value = bundle.getInt(KEY_CMD_VALUE);

            RoomHubDevice device = pm_data.getRoomHubData().getRoomHubDevice();
            switch (cmd_type) {
                case KEY_ID:
                    req_pack.setKeyId(cmd_value);
                    break;
            }

            log("setPMRelod cmd_type=" + cmd_type + " cmd_value=" + cmd_value);

            CommandResPack res_pack = device.commandRemoteControl(req_pack);
            if (res_pack != null) {
                ret_val = res_pack.getStatus_code();
            }

            if (ret_val == ErrorKey.Success)
                device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        }
        log("setPMRelod ret_val=" + ret_val);

        return ret_val;
    }

    private void setPMNoticeSetting(final String roomhub_uuid,final String uuid, final NoticeSetting newSettings) {
        //int ret= -1;

        new Thread() {
            @Override
            public void run(){
                long thread_id = Thread.currentThread().getId();
                mCmdResult.put(thread_id, uuid);
                Object obj = thread_id;
                mCommandHandler.sendMessageDelayed(mCommandHandler.obtainMessage(MESSAGE_COMMAND_TIMEOUT, obj),
                        mContext.getResources().getInteger(R.integer.config_send_command_timeout));

                int ret=ErrorKey.Success;
                PMData pm_data=getPMDataByUuid(roomhub_uuid,uuid);
                if(pm_data!=null){
                    ret=pm_data.SetNoticeSetting(newSettings);
                }
                Log.d(TAG,"setPMNoticeSetting ret="+ret);
                mCommandHandler.removeMessages(MESSAGE_COMMAND_TIMEOUT,obj);

                ProgressCmdResultCallback(thread_id, ret);
            }
        }.start();
    }

    private void PMMgr_SetNoticeSetting(final String roomhub_uuid,final String uuid, final NoticeSetting newSettings) {
        Log.d(TAG, "PMMgr_SetNoticeSetting uuid="+uuid);
        setPMNoticeSetting(roomhub_uuid, uuid, newSettings);
        //Log.d(TAG, "PMMgr_SetNoticeSetting ret=" + ret);
    }

    public PMData getPMDataByUuid(String roomhub_uuid,String Uuid){
        if(mPMList==null) return null;
        if(!TextUtils.isEmpty(roomhub_uuid) && !TextUtils.isEmpty(Uuid)){
            HashMap<String,PMData> pm_data=mPMList.get(roomhub_uuid);
            if(pm_data != null){
                return pm_data.get(Uuid);
            }
        }else if(!TextUtils.isEmpty(Uuid)){
            synchronized (mPMList) {
                for(int i=0;i < mPMList.size();i++){
                    HashMap<String,PMData> pm_data_map=mPMList.get(i);
                    PMData pm_data=pm_data_map.get(Uuid);
                    if(pm_data != null){
                        return pm_data;
                    }
                }
            }
        }
        /*
        PMData pm_data=mPMList.get(Uuid);
        if(pm_data == null) {
            synchronized (mPMList) {
                for (Iterator<PMData> it = mPMList.values().iterator(); it.hasNext(); ) {
                    pm_data = it.next();
                    String roomhub_uuid = pm_data.getRoomHubUuid();
                    if (roomhub_uuid.equals(Uuid))
                        return pm_data;
                }
            }
        }else
            return pm_data;
        */
        return null;
    }

    public int setKeyId(String roomhub_uuid,String uuid,int value){
        return sendCommandMessage(AssetDef.COMMAND_TYPE.KEY_ID, roomhub_uuid,uuid, value, 0);
    }

    public void setNoticeSetting(String roomhub_uuid,String uuid,NoticeSetting noticeSetting) {
        Message msg=new Message();
        msg.what=MESSAGE_PM_SET_NOTICE_SETTING;
        Bundle bundle=new Bundle();
        bundle.putString(KEY_UUID, roomhub_uuid);
        bundle.putString(KEY_ASSET_UUID, uuid);
        bundle.putParcelable(KEY_NOTICE_SETTING, noticeSetting);
        msg.setData(bundle);
        mPM25BackgroundHandler.sendMessage(msg);
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
        req_pack.setAssetName(mContext.getString(R.string.electric_pm));
        req_pack.setAssetIcon(R.drawable.img_pm_25_paring);
        req_pack.setBottomHint(mContext.getString(R.string.ble_pairing_pm25_main_desc));
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

                        Log.d(TAG, "onAdd SET_ASSETIN_INFO uuid=" + reqPack.getUuid()+" asset_type="+reqPack.getAssetType()+" brand="+reqPack.getBrand()+
                                " device="+reqPack.getDevice()+" connection_type="+reqPack.getConnectionType());

                        SetAssetInfoResPack resPack = roomhub_device.setAssetInfo(reqPack);

                        if (resPack != null)
                            ret=resPack.getStatus_code();

                        Log.d(TAG,"onAdd SET_ASSETIN_INFO ret="+ret);
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
            PMData data=getPMDataByUuid(roomhub_uuid,asset_uuid);
            if(data != null) {
                return mRoomHubMgr.RemoveElectric(roomhub_uuid,asset_uuid, asset_type);
            }
            return ErrorKey.Success;
        }
    };
}
