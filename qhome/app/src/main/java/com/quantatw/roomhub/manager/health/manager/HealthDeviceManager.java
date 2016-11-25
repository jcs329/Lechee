package com.quantatw.roomhub.manager.health.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.listener.ShareUserChangedListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceUpdateType;
import com.quantatw.roomhub.manager.health.listener.ShareHealthDataListener;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.cloudapi.CloudApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.ScanAsset;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.ReasonType;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.DeviceDefaultUserListener;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.listener.RoomHubDeviceListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.pack.account.GetUserFriendListResPack;
import com.quantatw.sls.pack.account.UserSharedDataResPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.AddCloudDeviceResPack;
import com.quantatw.sls.pack.device.AddDeviceReqPack;
import com.quantatw.sls.pack.device.DeleteDeviceReqPack;
import com.quantatw.sls.pack.device.DeviceDefaultUserResPack;
import com.quantatw.sls.pack.device.DeviceUserReqPack;
import com.quantatw.sls.pack.device.DeviceUserResPack;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.detail.AssetResPack;
import com.quantatw.sls.pack.roomhub.AcOnOffStatusResPack;
import com.quantatw.sls.pack.roomhub.DeleteScheduleResPack;
import com.quantatw.sls.pack.roomhub.DeviceFirmwareUpdateStateResPack;
import com.quantatw.sls.pack.roomhub.DeviceInfoChangeResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import com.quantatw.sls.pack.roomhub.NameChangeResPack;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;
import com.quantatw.sls.pack.roomhub.RoomHubDataResPack;
import com.quantatw.sls.pack.roomhub.UpdateScheduleResPack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static com.quantatw.sls.api.DeviceTypeConvertApi.CATEGORY;

/**
 * Created by erin on 5/11/16.
 */
public class HealthDeviceManager extends BaseManager implements
        RoomHubDeviceListener,RoomHubSignalListener,AccountLoginStateListener,
        HomeApplianceSignalListener,ShareUserChangedListener,
        DeviceDefaultUserListener
{

    private static final String TAG=HealthDeviceManager.class.getSimpleName();
    private AccountManager mAccountMgr;
    private MiddlewareApi mMiddlewareApi;
    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private LinkedHashMap<Integer, HealthDeviceController> healthDeviceControllerLinkedHashMap
            = new LinkedHashMap<>();

    private LinkedHashMap<String, HealthData> healthDeviceListMap
            = new LinkedHashMap<>();

    private HashMap<Integer, ArrayList<HealthData>> sharedHealthDataList = new HashMap<>();

    // healthcare devices
    private HashSet<HealthDeviceChangeListener> healthDeviceChangeListenerHashSet = new HashSet<>();

    // healthcare shared data
    private HashSet<ShareHealthDataListener> shareHealthDataListeners = new HashSet<>();

    private static final String KEY_UUID = "uuid";
    private static final String KEY_DEV_NAME = "dev_name";
    private static final String KEY_DEVICE= "device";
    private static final String KEY_DEVICE_TYPE= "dev_type";
    private static final String KEY_DEVICE_INFO= "dev_info";
    private static final String KEY_CMD_VALUE= "command_value";
    private static final String KEY_REASON_TYPE= "reason_type";
    private static final String KEY_SOURCE_TYPE= "source_type";
    private static final String KEY_UPDATE_SHARE_INFO="update_share_info";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DEVICE_USER="dev_default_user";

    private final int MESSAGE_ADD_DEVICE         =101;
    private final int MESSAGE_REMOVE_DEVICE      =102;
    private final int MESSAGE_UPDATE_DEVICE      =103;
    private final int MESSAGE_UPDATE_SHARE_USER_DATA = 104;
    private final int MESSAGE_ONLOGIN_OBTAIN_SHARE_LIST = 105;
    private final int MESSAGE_DEFAULT_USER_CHANGED = 106;

    private final int MESSAGE_INFO_CHANGE        = 201;

    // GCM Notify
    private final int MESSAGE_HANDLE_NOTIFY      = 301;

    public static final boolean NEW_BPM_SHARE_STYLE = true;

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ADD_DEVICE:
                    HealthcareMgr_AddDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE), (ReasonType) msg.getData().getSerializable(KEY_REASON_TYPE));
                    break;
                case MESSAGE_REMOVE_DEVICE:
                    HealthcareMgr_RemoveDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE), (ReasonType) msg.getData().getSerializable(KEY_REASON_TYPE));
                    break;
                case MESSAGE_UPDATE_DEVICE:
                    HealthcareMgr_UpdateDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE));
                    break;
                case MESSAGE_INFO_CHANGE:
                    HealthcareMgr_InfoChange(msg.getData().getInt(KEY_DEVICE_TYPE),
                            (SourceType)msg.getData().getSerializable(KEY_SOURCE_TYPE),
                            (BaseResPack)msg.getData().getParcelable(KEY_DEVICE_INFO));
                    break;
                case MESSAGE_HANDLE_NOTIFY:
                    int type = msg.getData().getInt(KEY_DEVICE_TYPE);
                    String uuid = msg.getData().getString(KEY_UUID);
                    BPMDataInfo lastHistoryResPack = msg.getData().getParcelable(KEY_DEVICE_INFO);
                    HealthcareMgr_RefreshHistory(type, uuid, lastHistoryResPack);
                    break;
                case MESSAGE_UPDATE_SHARE_USER_DATA:
                    HealthcareMgr_UpdateShareUserData((UserSharedDataResPack) msg.getData().getParcelable(KEY_UPDATE_SHARE_INFO));
                    break;
                case MESSAGE_ONLOGIN_OBTAIN_SHARE_LIST:
                    obtainShareHealthDataList();
                    break;
                case MESSAGE_DEFAULT_USER_CHANGED:
                    HealthcareMgr_DefaultUserChanged((DeviceDefaultUserResPack)msg.getData().get(KEY_DEVICE_USER));
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    public HealthDeviceManager(Context context, MiddlewareApi api) {
        super(context, BaseManager.HEALTH_MANAGER);
        mMiddlewareApi = api;

        mBackgroundThread=new HandlerThread("HealthDeviceManager");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        mMiddlewareApi.registerRoomHubDeviceListener(CATEGORY.HEALTH,this);
        mMiddlewareApi.registerRoomHubSignalListener(CATEGORY.ROOMHUB,this);
        mMiddlewareApi.registerHomeApplianceSignalListeners(CATEGORY.ROOMHUB,this);
        mMiddlewareApi.registerDeviceDefaultUserChangeListener(CATEGORY.HEALTH, this);
    }

    public void startup() {
        mAccountMgr=((RoomHubService) mContext).getAccountManager();
        mAccountMgr.registerForLoginState(this);
        mAccountMgr.registerShareUserChanged(this);

        // startup health managers
        Collection<HealthDeviceController> healthDeviceControllers = healthDeviceControllerLinkedHashMap.values();
        for(HealthDeviceController healthDeviceController:healthDeviceControllers) {
            healthDeviceController.startup();
        }
    }

    public void terminate() {

    }

    /*
     * register new health device
     */
    public int registerHealthDeviceManager(HealthDeviceController healthDeviceController) {
        int type = healthDeviceController.getType();
        if(RoomHubDef.getCategory(type) != DeviceTypeConvertApi.CATEGORY.HEALTH)
            return ErrorKey.HEALTHCARE_REGISTER_TYPE_ERROR;
        if(healthDeviceControllerLinkedHashMap.get(healthDeviceController.getType()) != null)
            return ErrorKey.HEALTHCARE_REGISTER_TYPE_DUPLICATE;
        healthDeviceControllerLinkedHashMap.put(healthDeviceController.getType(),healthDeviceController);
        return ErrorKey.Success;
    }

    /*
     * get current support health device type count
     */
    public ArrayList<Integer> getSupportTypeNumbers() {
        ArrayList<Integer> list = new ArrayList<>();
        if(healthDeviceControllerLinkedHashMap.size() > 0) {
            for(int type: healthDeviceControllerLinkedHashMap.keySet()) {
                list.add(type);
                return list;
            }
        }
        return list;
    }

    /*
     * get device manager by type
     */
    public HealthDeviceController getDeviceManager(int type) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(type);
        return healthDeviceController;
    }

    /*
     * get health device list by type
     */
    public synchronized ArrayList<HealthData> getHealthDeviceList(int type) {
        ArrayList<HealthData> list = new ArrayList<>();

        for(Iterator iterator = healthDeviceListMap.values().iterator(); iterator.hasNext();) {
            HealthData healthData = (HealthData)iterator.next();
            if(healthData.getType()==type)
                list.add(healthData);
        }

        Collections.sort(list);
        return list;
    }

    /*
     * get all health device list
     */
    public synchronized ArrayList<HealthData> getAllHealthDeviceList() {
        if(healthDeviceListMap != null) {
            ArrayList<HealthData> list = new ArrayList<>(healthDeviceListMap.values());
            Collections.sort(list);
            return list;
        }
        return null;
    }

    public synchronized ArrayList<HealthData> getSharedHealthDataList(int type, boolean forceRenew) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(type);
        if(healthDeviceController == null)
            return null;

        if(!forceRenew) {
            return sharedHealthDataList.get(type);
        }

        ArrayList<FriendData> shareList = null;
        GetUserFriendListResPack resPack = CloudApi.getInstance().getSharedUserDataList(type);
        if (resPack != null) {
            shareList = resPack.getList();
        }

        ArrayList<HealthData> healthDataList = new ArrayList<>();
        insertMyCard(shareList);
        if(shareList != null) {
            int idx = 0;
            for(FriendData friendData: shareList) {
                HealthData newHealthData = newHealthData(type);
                FriendData friend = mAccountMgr.getFriendDataByUserId(friendData.getUserId());
                friendData.setNickName(friend!=null?friend.getNickName():friendData.getUserAccount());
                newHealthData.setDeviceName(friendData.getNickName());
                newHealthData.setFriendData(friendData);
                newHealthData.setRoleName((idx++)==0?RoomHubDef.ROLE_OWNER:RoomHubDef.ROLE_USER);
                healthDataList.add(newHealthData);

                // notify manager
                healthDeviceController.addShareHealthData(newHealthData, new HealthDeviceController.NotifyCallback() {
                    @Override
                    public void onResult(int result, HealthData healthData) {
                        // notify UI list
                        notifyAddShareHealthDataListeners(healthData);
                    }
                });

//                healthDeviceController.addShareHealthData(newHealthData);

            }

            synchronized (sharedHealthDataList) {
                sharedHealthDataList.put(type, healthDataList);
            }
        }

        return healthDataList;
    }

    public synchronized ArrayList<HealthData> getSharedHealthDataList(int type) {
        return getSharedHealthDataList(type, false);
    }

    public HealthData newHealthData(int type) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(type);
        if(healthDeviceController != null)
            return healthDeviceController.newHealthData();
        return null;
    }

    public HealthData getHealthDataByUuid(String uuid) {
        return getHealthData(uuid);
    }

    public HealthData getShareHealthDataByUserId(int deviceType, String userId) {
        return getShareHealthData(deviceType,userId);
    }

    public int getHealthDeviceTypeTitleResource(int type) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(type);
        if(healthDeviceController != null)
            return healthDeviceController.getTitleStringResourceId();
        return -1;
    }

    public int getHealthDeviceTypeImageResource(int type) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(type);
        if(healthDeviceController != null)
            return healthDeviceController.getDrawableResourceId();
        return -1;
    }

    /*
    * register here or register from device manager
     */
    public void registerHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        healthDeviceChangeListenerHashSet.add(listener);
    }

    public void unregisterHealthDeviceChangeListener(HealthDeviceChangeListener listener) {
        healthDeviceChangeListenerHashSet.remove(listener);
    }

    public void registerShareHealthDataListener(ShareHealthDataListener listener) {
        shareHealthDataListeners.add(listener);
    }

    public void unregisterShareHealthDataListener(ShareHealthDataListener listener) {
        shareHealthDataListeners.remove(listener);
    }

    //public int regDeviceToCloud(RegHealDeviceContent regHealDeviceContent) {
    public int regDeviceToCloud(String roomHubUUID,ScanAsset scanAsset) {
        AddDeviceReqPack reqPack=new AddDeviceReqPack();

        reqPack.setDeviceType(scanAsset.getAssetType());
        reqPack.setCategory(CATEGORY.HEALTH);
        reqPack.setUuid(scanAsset.getUuid());
        reqPack.setDevice_name(scanAsset.getDeviceName());
//        reqPack.setTownId(townId);
        reqPack.setVersion("");
        reqPack.setBrandName(scanAsset.getBrand());
        reqPack.setModelName(scanAsset.getDevice());
        reqPack.setRoomHubUUID(roomHubUUID);
        AddCloudDeviceResPack res=mMiddlewareApi.addDevice(reqPack);

        int ret_val=res.getStatus_code();
        log("regDeviceToCloud ret_val=" + ret_val);

        return ErrorKey.Success;
    }

    public int unRegDeviceToCloud(int deviceType, String uuid){
        DeleteDeviceReqPack pack = new DeleteDeviceReqPack();
        pack.setUuid(uuid);
        pack.setDeviceType(deviceType);
        pack.setCategory(CATEGORY.HEALTH);
        BaseResPack res = mMiddlewareApi.deleteDevice(pack);
        return res.getStatus_code();
    }

    public void handleBPMNotification(String uuid, BaseResPack resPack) {
        Message msg = new Message();
        msg.what = MESSAGE_HANDLE_NOTIFY;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_UUID, uuid);
        bundle.putInt(KEY_DEVICE_TYPE, DeviceTypeConvertApi.TYPE_HEALTH.BPM);
        bundle.putParcelable(KEY_DEVICE_INFO, resPack);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    public int SaveDeviceDefaultUser(int deviceType, String uuid,String user_id){
        // TODO: deviceType for future used
        DeviceUserReqPack deviceUserReqPack=new DeviceUserReqPack();
        deviceUserReqPack.setUserId(user_id);
        BaseResPack resPack= CloudApi.getInstance().saveDeviceDefaultUser(uuid, deviceUserReqPack);
        if(resPack.getStatus_code() == ErrorKey.Success) {
            HealthData healthData = getHealthData(uuid);
            if(healthData != null)
                UpdateDefaultUser(healthData);
        }
        return resPack.getStatus_code();
    }

    /**********************************************************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/
    private void UpdateRole(RoomHubDevice device,HealthData data){
        //SourceType source_type=device.getSourceType();
        boolean is_admin=mContext.getResources().getBoolean(R.bool.config_administrator_for_debug);

        log("UpdateRole UUID="+device.getUuid()+" is_admin="+is_admin);

        if(is_admin){
            data.setRoleName(RoomHubDef.ROLE_ADMIN);
        }else{
            if(data.IsAlljoyn()){
                String owner_id = device.getOwnerId();
                String cur_user_id=mAccountMgr.getUserId();
                log("UpdateRole is_alljoyn owner_id=" + owner_id + " cur_user_id=" + cur_user_id);
                if (cur_user_id.equals(owner_id)) {
                    data.setRoleName(RoomHubDef.ROLE_ADMIN);
                    data.setOwnerId(owner_id);
                }else if(data.IsCloud()){
                    CloudDevice cloud_device=device.getExtraInfo();
                    if(cloud_device != null) {
                        log("UpdateRole alljoyn is_cloud  rolename="+cloud_device.getRoleName()+" owner_name="+cloud_device.getOwnerName());

                        data.setRoleName(cloud_device.getRoleName());
                        data.setOwnerId(cloud_device.getOwnerName());
                    }
                }else{
                    data.setRoleName(RoomHubDef.ROLE_NONE);
                }
            }else if(data.IsCloud()){
                CloudDevice cloud_device=device.getExtraInfo();
                if(cloud_device != null) {
                    log("UpdateRole is_cloud  rolename="+cloud_device.getRoleName()+" owner_name="+cloud_device.getOwnerName());
                    data.setRoleName(cloud_device.getRoleName());
                    data.setOwnerId(cloud_device.getOwnerName());
                }
            }else{
                data.setRoleName(RoomHubDef.ROLE_NONE);
            }
        }
    }


    private void HealthcareMgr_AddDevice(RoomHubDevice device,ReasonType reason) {
        String uuid = device.getUuid();
        String dev_name = device.getName();
        SourceType source_type = device.getSourceType();

        int deviceType = device.getMappingType();
        int category = device.getCategory();

        log("HealthcareMgr_AddDevice uuid=" + uuid + " deviceType=" + deviceType + " category=" + category+ " reason=" + reason + " source_type=" + source_type);

        if(!isMine(device)) {
            log("category or source type is not matched!");
            return;
        }

        HealthData healthData = getHealthData(uuid);
        if(healthData == null) {
            // New:
            HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
            if(healthDeviceController != null) {
                healthData = transferToHealthData(device, deviceType, healthDeviceController);
                if (healthData != null) {
                    UpdateDefaultUser(healthData);
                    UpdateRole(device, healthData);

                    synchronized (healthDeviceListMap) {
                        healthDeviceListMap.put(uuid, healthData);
                    }
                    // notify manager
                    healthDeviceController.addDeivce(healthData);

                    // notify UI list
                    notifyAddListeners(healthData);
                }
            }
        }
        else {
            // Update:
        }
    }

    private void HealthcareMgr_RemoveDevice(RoomHubDevice device,ReasonType reason) {
        String uuid = device.getUuid();
        String dev_name = device.getName();
        SourceType source_type = device.getSourceType();

        int deviceType = device.getMappingType();
        int category = device.getCategory();

        log("HealthcareMgr_RemoveDevice uuid="+device.getUuid()+" deviceType=" + deviceType + " category=" + category + " type="+device.getSourceType());

        if(!isMine(device)) {
            log("category or source type is not matched!");
            return;
        }

        HealthData healthData = getHealthData(uuid);
        if(healthData != null) {
            HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
            if(healthDeviceController != null) {
                boolean doRemove = false;
                if(reason == ReasonType.USERSHARE){
                    CloudDevice cloud_device=device.getExtraInfo();
                    String targetUser=cloud_device.getTagetUser();
                    String cur_user_id=mAccountMgr.getUserId();

                    if(targetUser!=null) {
                        if (targetUser.equals(cur_user_id)) {
                            doRemove = true;
                        }
                    }
                }
                else {
                    doRemove = true;
                }

                if(doRemove) {
                    // notify UI list
                    notifyRemoveListeners(healthData);

                    // notify manager
                    healthDeviceController.removeDevice(healthData);

                    synchronized (healthDeviceListMap) {
                        healthDeviceListMap.remove(uuid);
                    }
                }
            }
        }
        else {
            log("HealthcareMgr_RemoveDevice device not found!");
        }
    }

    private void HealthcareMgr_UpdateDevice(RoomHubDevice device) {
        String uuid = device.getUuid();
        String dev_name = device.getName();
        SourceType source_type = device.getSourceType();

        int deviceType = device.getMappingType();
        int category = device.getCategory();

        log("HealthcareMgr_UpdateDevice uuid="+device.getUuid()+" deviceType=" + deviceType + " category=" + category + " type="+device.getSourceType());

        if(!isMine(device)) {
            log("category or source type is not matched!");
            return;
        }

        HealthData healthData = getHealthData(uuid);
        if(healthData != null) {
            HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
            if(healthDeviceController != null) {
                // TODO: Update data:
                if(healthData.isUpdateDeviceName(device)) {
                    // notify manager
                    healthDeviceController.updateDevice(HealthDeviceUpdateType.DEVICE_NAME, healthData);
                    // notify UI list
                    notifyUpdateListeners(HealthDeviceUpdateType.DEVICE_NAME, healthData);
                }

                if(healthData.isUpdateOnLineStatus(device)) {
                    // notify manager
                    healthDeviceController.updateDevice(HealthDeviceUpdateType.ONLINE_STATUS, healthData);
                    // notify UI list
                    notifyUpdateListeners(HealthDeviceUpdateType.ONLINE_STATUS, healthData);
                }
            }
        }
        else {
            log("HealthcareMgr_UpdateDevice device not found!");
        }

    }

    private void HealthcareMgr_RefreshHistory(int deviceType, String uuid, BPMDataInfo lastHistoryResPack) {
        int category = RoomHubDef.getCategory(deviceType);
        log("HealthcareMgr_RefreshHistory uuid=" + uuid + " deviceType=" + deviceType + " category=" + category);

//        HealthData healthData = getHealthData(uuid);
        HealthData healthData = getShareHealthData(deviceType, lastHistoryResPack.getUserId());
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
        if (healthData != null && healthDeviceController != null) {
            // notify manager
            healthDeviceController.contentChange(healthData, lastHistoryResPack);
            // notify UI list
            notifyUpdateListeners(HealthDeviceUpdateType.CONTENT_CHANGE, healthData);
        }
    }

    private void HealthcareMgr_UpdateShareUserData(UserSharedDataResPack userSharedDataResPack) {
        if(userSharedDataResPack != null && userSharedDataResPack.getStatus_code() == ErrorKey.Success) {
            if(mAccountMgr.getUserId().equalsIgnoreCase(userSharedDataResPack.getUserId())) {
                log("initiative add/del, ignore it");   // Owner
                return;
            }

            int deviceType = userSharedDataResPack.getDeviceType();
            HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
            if(healthDeviceController == null) {
                log("this deviceType:"+deviceType+" is not found!!!");
                return;
            }
            synchronized (sharedHealthDataList) {
                ArrayList<HealthData> list = sharedHealthDataList.get(deviceType);
                if (list != null) {
                    String methodType = userSharedDataResPack.getMethodType();
                    if (methodType.equalsIgnoreCase("add")) {
                        // Add Share HealthData
                        HealthData newHealthData = newHealthData(deviceType);
                        FriendData friend = mAccountMgr.getFriendDataByUserId(userSharedDataResPack.getUserId());
                        if(friend == null) {
                            friend = new FriendData();
                            friend.setUserAccount(userSharedDataResPack.getUserAccount());
                            friend.setUserId(userSharedDataResPack.getUserId());
                            friend.setNickName(userSharedDataResPack.getUserAccount());
                        }
                        newHealthData.setFriendData(friend);
                        newHealthData.setDeviceName(friend.getNickName());
                        newHealthData.setRoleName(RoomHubDef.ROLE_USER);
                        list.add(newHealthData);

                        // notify manager
                        healthDeviceController.addShareHealthData(newHealthData);

                        // notify UI list
                        notifyAddShareHealthDataListeners(newHealthData);

                    } else {
                        // Remove Share HealthData
                        HealthData removeTarget = null;
                        for (HealthData healthData : list) {
                            if(healthData.getFriendData() != null &&
                                    healthData.getFriendData().getUserId().equalsIgnoreCase(userSharedDataResPack.getUserId())) {
                                removeTarget = healthData;
                                break;
                            }
                        }
                        if(removeTarget != null) {
                            // notify UI list
                            notifyRemoveShareHealthDataListeners(removeTarget);

                            list.remove(removeTarget);
                        }

                    }
                }
            }
        }
    }

    private void HealthcareMgr_DefaultUserChanged(DeviceDefaultUserResPack deviceDefaultUserResPack) {
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(DeviceTypeConvertApi.TYPE_HEALTH.BPM);
        if(healthDeviceController == null)
            return;

        HealthData healthData = getHealthData(deviceDefaultUserResPack.getUuid());
        if(healthData!=null) {
            if (deviceDefaultUserResPack.getType().equalsIgnoreCase("add")) {
                if(deviceDefaultUserResPack.getUserId().equalsIgnoreCase(mAccountMgr.getUserId())) {
                    FriendData friendData = new FriendData();
                    friendData.setUserAccount(mAccountMgr.getCurrentAccount());
                    friendData.setUserId(mAccountMgr.getUserId());
                    friendData.setNickName(mAccountMgr.getCurrentAccountName());
                    healthData.setFriendData(friendData);
                }
                else {
                    FriendData friendData = mAccountMgr.getFriendDataByUserId(deviceDefaultUserResPack.getUserId());
                    healthData.setFriendData(friendData);
                }
            }
            else {
            }

            // notify manager;
            healthDeviceController.updateDevice(HealthDeviceUpdateType.DEVICE_USER_CHANGE, healthData);

            // notify UI list
            notifyUpdateListeners(HealthDeviceUpdateType.DEVICE_USER_CHANGE, healthData);
        }
    }

    private void HealthcareMgr_InfoChange(int deviceType, SourceType sourceType, BaseResPack resPack) {
        String uuid = resPack.getUuid();
        int category = RoomHubDef.getCategory(deviceType);
        log("HealthcareMgr_InfoChange uuid=" + uuid + " deviceType=" + deviceType + " category=" + category);
        HealthData healthData = getHealthData(uuid);
        HealthDeviceController healthDeviceController = healthDeviceControllerLinkedHashMap.get(deviceType);
        if (healthDeviceController != null) {
            healthDeviceController.contentChange(healthData, resPack);
        }
    }

    private int UpdateDefaultUser(HealthData healthData) {
        DeviceUserResPack deviceUserReqPack = CloudApi.getInstance().getDeviceDefaultUser(healthData.getUuid());
        if(deviceUserReqPack != null && deviceUserReqPack.getStatus_code() == ErrorKey.Success) {
            if(deviceUserReqPack.getDefaultUser() != null) {
                AccountManager accountManager = ((RoomHubApplication)mContext.getApplicationContext()).getAccountManager();
                FriendData friendData = accountManager.getFriendDataByUserId(deviceUserReqPack.getDefaultUser().getUserId());
                if(friendData == null) {
                    friendData = new FriendData();
                    friendData.setUserAccount(deviceUserReqPack.getDefaultUser().getUserAccount());
                    friendData.setUserId(deviceUserReqPack.getDefaultUser().getUserId());
                }
                healthData.setFriendData(friendData);
            }
        }

        return deviceUserReqPack!=null?deviceUserReqPack.getStatus_code():-1;
    }

    private HealthData transferToHealthData(RoomHubDevice roomHubDevice, int deviceType, HealthDeviceController healthDeviceController) {
        HealthData healthData = healthDeviceController.newHealthData();
        healthData.setUuid(roomHubDevice.getUuid());
        if(roomHubDevice.getSourceType()==SourceType.ALLJOYN)
            healthData.setSourceType(RoomHubData.SOURCE_TYPE_ALLJOYN);
        else
            healthData.setSourceType(RoomHubData.SOURCE_TYPE_CLOUD);
        healthData.setOnlineStatus(roomHubDevice.getExtraInfo().isOnlineStatus()==true?1:0);
        healthData.setBrandName(roomHubDevice.getExtraInfo().getBrandName());
        healthData.setModelNumber(roomHubDevice.getExtraInfo().getModelName());
        healthData.setDeviceName(roomHubDevice.getExtraInfo().getDevice_name());
        healthData.setOwnerId(roomHubDevice.getExtraInfo().getOwnerName());
        healthData.setRoleName(roomHubDevice.getExtraInfo().getRoleName());
        healthData.setRoomHubUuid(roomHubDevice.getExtraInfo().getRoomHubUUID());
        return healthData;
    }

    private HealthData getHealthData(String uuid) {
        if(healthDeviceListMap == null)
            return null;

        return healthDeviceListMap.get(uuid);
    }

    private HealthData getShareHealthData(int deviceType, String userId) {
        if(sharedHealthDataList == null)
            getSharedHealthDataList(deviceType, true);

        ArrayList<HealthData> list = sharedHealthDataList.get(deviceType);
        if(list != null) {
            for(HealthData healthData: list) {
                if(healthData.getFriendData() != null &&
                        healthData.getFriendData().getUserId().equalsIgnoreCase(userId))
                    return healthData;
            }
        }

        return null;
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

    private void notifyAddShareHealthDataListeners(HealthData healthData) {
        for(Iterator iterator = shareHealthDataListeners.iterator(); iterator.hasNext();) {
            ShareHealthDataListener listener = (ShareHealthDataListener)iterator.next();
            listener.addShareHealthData(healthData);
        }
    }

    private void notifyRemoveShareHealthDataListeners(HealthData healthData) {
        for(Iterator iterator = shareHealthDataListeners.iterator(); iterator.hasNext();) {
            ShareHealthDataListener listener = (ShareHealthDataListener)iterator.next();
            listener.removeHealthData(healthData);
        }
    }

    private void notifyUpdateShareHealthDataListeners(HealthData healthData) {
        for(Iterator iterator = shareHealthDataListeners.iterator(); iterator.hasNext();) {
            ShareHealthDataListener listener = (ShareHealthDataListener)iterator.next();
            listener.updateHealthData(healthData);
        }
    }

    private boolean isMine(RoomHubDevice roomHubDevice) {
        int category = roomHubDevice.getCategory();
        SourceType source_type = roomHubDevice.getSourceType();

        if(category != CATEGORY.HEALTH || source_type != SourceType.CLOUD) {
            log("category or source type is not matched!");
            return false;
        }

        return true;
    }

    private void insertMyCard(ArrayList<FriendData> shareList) {
        FriendData friendData = new FriendData();
        friendData.setUserAccount(mAccountMgr.getCurrentAccount());
        friendData.setUserId(mAccountMgr.getUserId());
        friendData.setNickName(mAccountMgr.getCurrentAccountName());

        if(shareList == null)
            shareList = new ArrayList<>();
        shareList.add(0,friendData);
    }

    private void obtainShareHealthDataList() {
        Collection<HealthDeviceController> healthDeviceControllers = healthDeviceControllerLinkedHashMap.values();
        for(HealthDeviceController healthDeviceController:healthDeviceControllers) {
            getSharedHealthDataList(healthDeviceController.getType(), true);
        }
    }

    private void clearShareHealthDataList() {
        sharedHealthDataList.clear();
    }

    private void log(String msg) {
        Log.d(TAG,msg);
    }
    /**********************************************************************************************/
    /**********************************************************************************************/
    /**********************************************************************************************/

    public static void traceLog(String msg) {
        traceLog("",msg);
    }

    public static void traceLog(String tag,String msg) {
        if(TextUtils.isEmpty(tag))
            Log.d(TAG,msg);
        else
            Log.d(TAG,"["+tag+"] "+msg);
    }

    @Override
    public void onLogin() {
        Message msg=new Message();
        msg.what=MESSAGE_ONLOGIN_OBTAIN_SHARE_LIST;
        Bundle bundle=new Bundle();
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void onLogout() {
        clearShareHealthDataList();
    }

    @Override
    public void onSkipLogin() {

    }

    @Override
    public void addAsset(AssetResPack assetResPack, SourceType sourceType) {

    }

    @Override
    public void removeAsset(AssetResPack assetResPack, SourceType sourceType) {

    }

    @Override
    public void updateAsset(AssetResPack assetResPack, SourceType sourceType) {

    }

    @Override
    public void AssetInfoChange(int assetType, Object assetDetailInfoResPack, SourceType sourceType) {
        int category = RoomHubDef.getCategory(assetType);

        traceLog("AssetInfoChange","category="+category+",type="+assetType);
        if(category != CATEGORY.HEALTH) {
            traceLog("AssetInfoChange","this is not for Health category device, drop it!");
            return;
        }

        Message msg = new Message();
        msg.what = MESSAGE_INFO_CHANGE;
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_DEVICE_TYPE, assetType);
        bundle.putSerializable(KEY_SOURCE_TYPE, sourceType);
        bundle.putParcelable(KEY_DEVICE_INFO, (BaseResPack)assetDetailInfoResPack);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);

    }

    @Override
    public void FirmwareUpdateStateChange(FirmwareUpdateStateResPack firmwareUpdateStateResPack) {

    }

    @Override
    public void AcFailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {

    }

    @Override
    public void ScanAssetResult(ScanAssetResultResPack scanAssetResPack) {

    }

    @Override
    public void UpdateSchedule(SignalUpdateSchedulePack updateSchedulePack) {

    }

    @Override
    public void DeleteSchedule(SignalDeleteSchedulePack deleteSchedulePack) {

    }

    @Override
    public void AssetProfileChange(AssetProfile profile) {

    }

    @Override
    public void addDevice(RoomHubDevice device, ReasonType reason) {
        log("addDevice uuid=" + device.getUuid() + " reason=" + reason);
        Message msg=new Message();
        msg.what=MESSAGE_ADD_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        bundle.putSerializable(KEY_REASON_TYPE, reason);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void removeDevice(RoomHubDevice device, ReasonType reason) {
        Message msg=new Message();
        msg.what=MESSAGE_REMOVE_DEVICE;
        Bundle bundle=new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        bundle.putSerializable(KEY_REASON_TYPE, reason);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void updateDevice(RoomHubDevice device) {
        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_DEVICE;
        Bundle bundle=new Bundle();
        bundle.putParcelable(KEY_DEVICE, device);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void switchNetwork(boolean connected) {

    }

    @Override
    public void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType) {

    }

    @Override
    public void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack) {

    }

    @Override
    public void RoomHubDeviceInfoChangeUpdate(DeviceInfoChangeResPack deviceInfoChangeResPack, SourceType sourceType) {

    }

    @Override
    public void RoomHubNameChangeUpdate(NameChangeResPack nameResPack) {

    }

    @Override
    public void RoomHubSyncTime() {

    }

    @Override
    public void RoomHubAcOnOffStatusUpdate(AcOnOffStatusResPack resPack) {

    }

    @Override
    public void RoomHubUpdateSchedule(UpdateScheduleResPack resPack) {

    }

    @Override
    public void RoomHubUpdateAllSchedule(String uuid, ArrayList<Schedule> schedule_lst) {

    }

    @Override
    public void RoomHubDeleteSchedule(DeleteScheduleResPack resPack) {

    }

    @Override
    public void RoomHubNextSchedule(NextScheduleResPack resPack) {

    }

    @Override
    public void RoomHubOTAUpgradeStateChangeUpdate(DeviceFirmwareUpdateStateResPack resPack) {

    }

    @Override
    public void AddShareUser(CloudDevice device) {

    }

    @Override
    public void RemoveShareUser(CloudDevice device) {

    }

    @Override
    public void UserSharedData(UserSharedDataResPack userSharedData) {
        if(userSharedData != null && userSharedData.getStatus_code() == ErrorKey.Success) {
            Message msg = new Message();
            msg.what = MESSAGE_UPDATE_SHARE_USER_DATA;
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_UPDATE_SHARE_INFO, userSharedData);
            msg.setData(bundle);
            mBackgroundHandler.sendMessage(msg);
        }
    }

    @Override
    public void defaultUserChange(DeviceDefaultUserResPack deviceDefaultUserResPack) {
        if(deviceDefaultUserResPack != null && deviceDefaultUserResPack.getStatus_code()==ErrorKey.Success) {
            Message msg = new Message();
            msg.what = MESSAGE_DEFAULT_USER_CHANGED;
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_DEVICE_USER, deviceDefaultUserResPack);
            msg.setData(bundle);
            mBackgroundHandler.sendMessage(msg);
        }
    }
}
