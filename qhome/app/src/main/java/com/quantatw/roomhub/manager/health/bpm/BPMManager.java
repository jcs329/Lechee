package com.quantatw.roomhub.manager.health.bpm;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Primitives;
import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEControllerCallback;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.blepair.BLEPairReqPack;
import com.quantatw.roomhub.blepair.ScanAssetResult;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceUpdateType;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceController;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.json.ExangeJson;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.account.GetUserFriendListResPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.DeviceUserReqPack;
import com.quantatw.sls.pack.device.DeviceUserResPack;
import com.quantatw.sls.pack.device.ModifyDeviceNameReqPack;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;
import com.quantatw.sls.pack.healthcare.BPMHistoryResPack;
import com.quantatw.sls.pack.healthcare.BPMLastHistoryResPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by erin on 4/29/16.
 */
public class BPMManager extends HealthDeviceController {
    private final String TAG=BPMManager.class.getSimpleName();
    private ArrayList<BPMData> bpmDevices = new ArrayList<>();
    private HashSet<HealthDeviceChangeListener> healthDeviceChangeListenerHashSet = new HashSet<>();

    private final int MESSAGE_ADD_DEVICE = 100;
    private final int MESSAGE_REMOVE_DEVICE = 101;
    private final int MESSAGE_UPDATE_DEVICE = 102;
    private final int MESSAGE_CONTENT_CHANGE = 103;
    private final int MESSAGE_ADD_SHARE_HEALTHDATA = 104;
    private final int MESSAGE_REMOVE_SHARE_HEALTHDATA = 105;
    private final int MESSAGE_CHANGE_DEFAULT_USER = 106;

    public static final String KEY_DEVICE= "device";
    public static final String KEY_CONTENT_INFO = "content_info";
    public static final String KEY_UPDATE_CONTENT_TYPE = "update_type";

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ADD_DEVICE: {
                    BPMData bpmData = (BPMData)msg.obj;
                    getDefaultUser(bpmData);
                    if(bpmData.getLastHistory() == null) {
                        getLastHistory(bpmData);
                    }
                    notifyAddListeners(bpmData);
                }
                    break;
                case MESSAGE_REMOVE_DEVICE: {
                    BPMData bpmData = (BPMData)msg.obj;
                    notifyRemoveListeners(bpmData);
                }
                    break;
                case MESSAGE_UPDATE_DEVICE: {
                    BPMData bpmData = msg.getData().getParcelable(KEY_DEVICE);
                    int updateType = msg.getData().getInt(KEY_UPDATE_CONTENT_TYPE);
                    notifyUpdateListeners(updateType,bpmData);
                }
                    break;
                case MESSAGE_ADD_SHARE_HEALTHDATA:
                case MESSAGE_CONTENT_CHANGE: {
                    BPMData bpmData = msg.getData().getParcelable(KEY_DEVICE);
                    BPMDataInfo bpmLastHistoryResPack = msg.getData().getParcelable(KEY_CONTENT_INFO);
                    if(bpmLastHistoryResPack != null
                            && bpmLastHistoryResPack.getStatus_code() == ErrorKey.Success) {
                        bpmData.setLastHistory(bpmLastHistoryResPack);
                    }
                    else {
                        // force obtain latest history
                        getLastHistory(bpmData);
                    }

                    if(mNotifyCallback != null) {
                        mNotifyCallback.onResult(bpmData.getLastHistory()!=null?ErrorKey.Success:-1, bpmData);
                    }

                    notifyUpdateListeners(HealthDeviceUpdateType.CONTENT_CHANGE,bpmData);
                }
                    break;
                case MESSAGE_REMOVE_SHARE_HEALTHDATA: {

                }
                    break;
                case MESSAGE_CHANGE_DEFAULT_USER: {

                }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private final int PARSE_PACK_ACTION_GET_LAST = 100;
    private final int PARSE_PACK_ACTION_GET_LIST = 101;

    protected int parseDataPack(HealthData healthData, int action, String jsonString) {
        switch (action) {
            case PARSE_PACK_ACTION_GET_LAST: {
                ExangeJson<BPMLastHistoryResPack> mExangeJson = new ExangeJson<>();
                BPMLastHistoryResPack resPack = new BPMLastHistoryResPack();
                resPack = mExangeJson.Exe(jsonString, resPack);
                if(resPack.getStatus_code() == ErrorKey.Success) {
                    BPMData bpmData = (BPMData) healthData;
                    bpmData.setLastHistory(resPack.getData());
                }
                return ErrorKey.HEALTHCARE_PARSE_HISTORY_FAIL;
            }
            case PARSE_PACK_ACTION_GET_LIST: {
                ExangeJson<BPMHistoryResPack> mExangeJson = new ExangeJson<>();
                BPMHistoryResPack resPack = new BPMHistoryResPack();
                resPack = mExangeJson.Exe(jsonString, resPack);
                if(resPack.getStatus_code() == ErrorKey.Success) {
                    BPMData bpmData = (BPMData)healthData;
                    bpmData.setHistoryList(resPack.getData());
                }
                return resPack.getStatus_code();
            }
        }
        return ErrorKey.HEALTHCARE_PARSE_HISTORY_FAIL;
    }

    private void notifyAddListeners(HealthData healthData) {
        for(Iterator iterator = healthDeviceChangeListenerHashSet.iterator(); iterator.hasNext();) {
            HealthDeviceChangeListener listener = (HealthDeviceChangeListener)iterator.next();
            listener.addDeivce(healthData);
        }
    }

    private void notifyRemoveListeners(HealthData healthData) {
        for(Iterator iterator = healthDeviceChangeListenerHashSet.iterator();iterator.hasNext();) {
            HealthDeviceChangeListener listener = (HealthDeviceChangeListener)iterator.next();
            listener.removeDevice(healthData);
        }
    }

    private void notifyUpdateListeners(int type, HealthData healthData) {
        for(Iterator iterator = healthDeviceChangeListenerHashSet.iterator();iterator.hasNext();) {
            HealthDeviceChangeListener listener = (HealthDeviceChangeListener)iterator.next();
            listener.updateDevice(type, healthData);
        }
    }

    private int getDefaultUser(BPMData bpmData) {
        DeviceUserResPack deviceUserReqPack = CloudApi.getInstance().getDeviceDefaultUser(bpmData.getUuid());
        if(deviceUserReqPack != null && deviceUserReqPack.getStatus_code() == ErrorKey.Success) {
            if(deviceUserReqPack.getDefaultUser() != null) {
                AccountManager accountManager = ((RoomHubApplication)mContext.getApplicationContext()).getAccountManager();
                FriendData friendData = accountManager.getFriendDataByUserId(deviceUserReqPack.getDefaultUser().getUserId());
                if(friendData == null) {
                    friendData = new FriendData();
                    friendData.setUserAccount(deviceUserReqPack.getDefaultUser().getUserAccount());
                    friendData.setUserId(deviceUserReqPack.getDefaultUser().getUserId());
                }
                bpmData.setFriendData(friendData);
            }
        }

        return deviceUserReqPack!=null?deviceUserReqPack.getStatus_code():-1;
    }

    public BPMDataInfo getLastHistory(BPMData bpmData) {
//        String jsonString = CloudApi.getInstance().getBPMLastData(bpmData.getUuid());
        if(bpmData.getFriendData() == null)
            return null;

        String jsonString = CloudApi.getInstance().getBPMLastDataByUserId(bpmData.getFriendData().getUserId());
        if(!TextUtils.isEmpty(jsonString)) {
            parseDataPack(bpmData,PARSE_PACK_ACTION_GET_LAST,jsonString);
        }
        return bpmData.getLastHistory();
    }

    public ArrayList<BPMDataInfo> getHistoryList(BPMData bpmData) {
        final int HISTORY_DATA_DAYS = mContext.getResources().getInteger(R.integer.config_bpm_history_days);
        return getHistoryList(bpmData,HISTORY_DATA_DAYS);
    }

    public ArrayList<BPMDataInfo> getHistoryList(BPMData bpmData, int days) {
//        String jsonString = CloudApi.getInstance().getBPMDataList(bpmData.getUuid(),days);
        if(bpmData.getFriendData() == null)
            return null;

        String jsonString = CloudApi.getInstance().getBPMDataListByUserId(bpmData.getFriendData().getUserId(), days);
        if(!TextUtils.isEmpty(jsonString)) {
            parseDataPack(bpmData, PARSE_PACK_ACTION_GET_LIST,jsonString);
        }
        return bpmData.getHistoryList();
    }

    public BPMManager(Context context) {
        super(context, DeviceTypeConvertApi.TYPE_HEALTH.BPM,"BPMManager",R.string.electric_sphygmometer,R.drawable.bpm_btn_selector);
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
        mHealthDeviceView = new BPMViewController(context, this);
        log(TAG,"new BPMManager");
    }

    public void startup() {
        log(TAG,"startup");
    }

    public void terminate() {
    }

    public HealthData newHealthData() {
        return new BPMData();
    }

    public int registerHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        healthDeviceChangeListenerHashSet.add(listener);
        return ErrorKey.Success;
    }

    public int unregisterHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        healthDeviceChangeListenerHashSet.remove(listener);
        return ErrorKey.Success;
    }

    public void startBLEPairing() {
        BLEPairReqPack req_pack=new BLEPairReqPack();
        req_pack.setRoomHubUuid(null);
        req_pack.setCatetory(DeviceTypeConvertApi.CATEGORY.HEALTH);
        req_pack.setAssetType(DeviceTypeConvertApi.TypeDef.BPM.getValue());
        req_pack.setPrefixName(mContext.getResources().getString(R.string.config_ble_pair_prefix_name));
        req_pack.setExpireTime(mContext.getResources().getInteger(R.integer.config_ble_pair_expire_time));
        req_pack.setCallback(mBLECallback);
        req_pack.setShowRename(true);
        req_pack.setAssetName(mContext.getString(R.string.electric_sphygmometer));
        req_pack.setAssetIcon(R.drawable.img_bpm_press_btn);
        req_pack.setBottomHint(mContext.getString(R.string.ble_pairing_bpm_main_desc));
        req_pack.setUseDefault(true);
        req_pack.setShowDefaultUser(true);

        BLEPairController ble_controller=((RoomHubService)mContext).getBLEController();
        ble_controller.init(req_pack);
    }

    private int SaveDeviceDefaultUser(String uuid,String user_id){
        HealthDeviceManager healthDeviceManager = ((RoomHubApplication)mContext.getApplicationContext()).getHealthDeviceManager();
        healthDeviceManager.SaveDeviceDefaultUser(mType, uuid, user_id);
        return ErrorKey.Success;
    }

    private BLEControllerCallback mBLECallback = new BLEControllerCallback() {
        @Override
        public int onAdd(BLEPairDef.ADD_STEP add_step, ScanAssetResult scaAsset) {
            int ret = ErrorKey.Success;
            switch (add_step){
                case ADD_ASSET:
                    ret=((RoomHubService)mContext).getRoomHubManager().AddElectric(scaAsset.getRoomHubUuid(), scaAsset.getScanAsset().getUuid(), scaAsset.getScanAsset().getAssetType(), AssetDef.CONNECTION_TYPE_BT);
                    break;
                case REG_TO_CLOUD:
                    ret=((RoomHubService)mContext).getHealthDeviceManager().regDeviceToCloud(scaAsset.getRoomHubUuid(),scaAsset.getScanAsset());
                    break;
                case SET_DEFAULT_USER:
                    ret=SaveDeviceDefaultUser(scaAsset.getScanAsset().getUuid(),scaAsset.getScanAsset().getDefaultUserId());
                    break;
            }
            return ret;
        }

        @Override
        public int onRename(String uuid, String new_name) {
            ModifyDeviceNameReqPack req = new ModifyDeviceNameReqPack();
            req.setDeviceName(new_name);
            BaseResPack res = CloudApi.getInstance().ModifyDeviceName(uuid,req);
            return res.getStatus_code();
        }

        @Override
        public int onRemove(String roomhub_uuid, String asset_uuid, int asset_type) {
            return ((RoomHubService)mContext).getHealthDeviceManager().unRegDeviceToCloud(asset_type,asset_uuid);
        }
    };

    @Override
    public void addDeivce(HealthData device) {
        BPMData bpmData = (BPMData)device;
        log(TAG,"addDeivce "+bpmData.toString());
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_ADD_DEVICE,device));
    }

    @Override
    public void removeDevice(HealthData device) {
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_REMOVE_DEVICE,device));
    }

    @Override
    public void updateDevice(int type, HealthData device) {
        Message msg = new Message();
        msg.what = MESSAGE_UPDATE_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        bundle.putInt(KEY_UPDATE_CONTENT_TYPE,type);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void contentChange(HealthData device, BaseResPack updateResPack) {
        Message msg = new Message();
        msg.what = MESSAGE_CONTENT_CHANGE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        bundle.putParcelable(KEY_CONTENT_INFO, updateResPack);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    protected void addShareHealthData(HealthData healthData, NotifyCallback callback) {
        super.addShareHealthData(healthData,callback);
        addShareHealthData(healthData);
    }

    @Override
    public void addShareHealthData(HealthData healthData) {
        Message msg = new Message();
        msg.what = MESSAGE_ADD_SHARE_HEALTHDATA;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, healthData);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void removeHealthData(HealthData healthData) {
        Message msg = new Message();
        msg.what = MESSAGE_REMOVE_SHARE_HEALTHDATA;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, healthData);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void updateHealthData(HealthData healthData) {

    }

    public ArrayList<BPMData> getList() {
        return bpmDevices;
    }

}
