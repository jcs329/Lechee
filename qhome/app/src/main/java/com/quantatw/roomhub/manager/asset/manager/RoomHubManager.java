package com.quantatw.roomhub.manager.asset.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.PreferenceEditor;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.alljoyn.ConfigCtrlInterface;
import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.Asset;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.ReasonType;
import com.quantatw.sls.key.SensorTypeKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.listener.RoomHubDeviceListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.pack.Weather.CityListResPack;
import com.quantatw.sls.pack.Weather.TownListResPack;
import com.quantatw.sls.pack.base.BaseResPack;
import com.quantatw.sls.pack.device.AddCloudDeviceResPack;
import com.quantatw.sls.pack.device.AddDeviceReqPack;
import com.quantatw.sls.pack.device.DeleteDeviceReqPack;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AddHomeApplianceReqPack;
import com.quantatw.sls.pack.homeAppliance.AddHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.AssetProfile;
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.FirmwareUpdateStateResPack;
import com.quantatw.sls.pack.homeAppliance.GetHomeApplianceAllAssetsResPack;
import com.quantatw.sls.pack.homeAppliance.HomeApplianceAsset;
import com.quantatw.sls.pack.homeAppliance.RemoveHomeApplianceReqPack;
import com.quantatw.sls.pack.homeAppliance.RemoveHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.SignalDeleteSchedulePack;
import com.quantatw.sls.pack.homeAppliance.SignalUpdateSchedulePack;
import com.quantatw.sls.pack.homeAppliance.UpgradeReqPack;
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

import org.alljoyn.bus.BusException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import static com.quantatw.sls.api.DeviceTypeConvertApi.CATEGORY;

/**
 * Created by 95010915 on 2015/9/22.
 */
public class RoomHubManager extends BaseManager
        implements RoomHubDeviceListener,RoomHubSignalListener,AccountLoginStateListener, HomeApplianceSignalListener {
    //implements RoomHubDeviceListener,RoomHubSignalListener,AccountLoginStateListener, MicroLocationSequenceChangeListener, MicroLocationSequenceFirstChangeListener,HomeApplianceSignalListener {
    private static final String TAG = RoomHubManager.class.getSimpleName();
    private static boolean DEBUG=true;
    private MiddlewareApi mApi;

    private HashMap<String, RoomHubData> mRoomHubDataList = new HashMap<String, RoomHubData>();

    private static final String KEY_ROOMHUB_DATA = "roomhub_data";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_ASSET_UUID = "asset_uuid";
    public static final String KEY_DEV_NAME = "dev_name";
    private static final String KEY_DEVICE= "device";
    public static final String KEY_CMD_TYPE= "command_type";
    public static final String KEY_CMD_VALUE= "command_value";
    private static final String KEY_CMD_VALUE1= "command_value1";
    private static final String KEY_CMD_VALUE2= "command_value2";
    private static final String KEY_CMD_VALUE3= "command_value3";
    private static final String KEY_CMD_VALUE4= "command_value4";
    private static final String KEY_REASON_TYPE= "reason_type";
    private static final String KEY_FAIL_RECOVER= "fail_recover";

    public static final int UPDATE_SENSOR_DATA        = 0;
    public static final int UPDATE_ROOMHUB_DATA       = 1;
    public static final int UPDATE_ROOMHUB_NAME       = 2;
    public static final int UPDATE_ONLINE_STATUS      = 3;

    private AccountManager mAccountMgr;
    private OTAManager mOTAMgr;
    private ACNoticeManager mACNoticeManager;

    private static final int MESSAGE_CONTROL_LED        =100;
    private static final int MESSAGE_ADD_DEVICE         =101;
    private static final int MESSAGE_REMOVE_DEVICE      =102;
    private static final int MESSAGE_UPDATE_DEVICE      =103;
    private static final int MESSAGE_UPDATE_OWNER_ID    =104;
    private static final int MESSAGE_OTA_UPGRADE        =105;
    private static final int MESSAGE_ADD_ELECTRIC       =106;
    private static final int MESSAGE_REMOVE_ELECTRIC    =107;
    private static final int MESSAGE_FAIL_RECOVER       =108;
    private static final int MESSAGE_REBOOT_ROOM_HUB    =119;
    private static final int MESSAGE_NO_CLOUD_IDENTITY  =110;
    private static final int MESSAGE_WAKE_UP            =112;
    private static final int MESSAGE_NAME_CHANGE        =113;

    private static final int MAC_FIXED_LEN = 2;
    private static final int HOME_AP_SSID_MAX_LEN = 12; //27;

    private LinkedHashSet<RoomHubChangeListener> mRoomHubListener = null;
    private LinkedHashSet<AssetListener> mAssetListener = new LinkedHashSet<AssetListener>();;
    private LinkedHashMap<Integer, BaseAssetManager> mAssetMgrHashMap
            = new LinkedHashMap<>();

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;
    private HandlerThread mCommandThread;
    private CommandHandler mCommandHandler;

    private static final long DELAY_CHECK_NO_CLOUD_IDENTITY = 10000;

    private HashMap<String, String> mOnBoardingSSID = new HashMap<String, String>();
    private boolean mIsConnected=true;

    private final class CommandHandler extends Handler {
        public CommandHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_ADD_ELECTRIC:
                    RoomHubMgr_AddElectric(msg.getData().getString(KEY_UUID), msg.getData().getString(KEY_CMD_VALUE),msg.getData().getInt(KEY_CMD_VALUE1),msg.getData().getInt(KEY_CMD_VALUE2));
                    break;
                case MESSAGE_REMOVE_ELECTRIC:
                    RoomHubMgr_RemoveElectric(msg.getData().getString(KEY_UUID),msg.getData().getString(KEY_ASSET_UUID), msg.getData().getInt(KEY_CMD_VALUE));
                    break;
                case MESSAGE_FAIL_RECOVER:
                    RoomHubMgr_FailRecover((AcFailRecoverResPack) msg.getData().getParcelable(KEY_FAIL_RECOVER), (SourceType) msg.getData().getSerializable(KEY_CMD_VALUE));
                    break;
                case MESSAGE_REBOOT_ROOM_HUB:
                    RoomHubMgr_RebootRoomHub((String) msg.obj);
                    break;
            }
        }
    }

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_ADD_DEVICE:
                    RoomHubMgr_AddDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE), (ReasonType) msg.getData().getSerializable(KEY_REASON_TYPE));
                    break;
                case MESSAGE_REMOVE_DEVICE:
                    RoomHubMgr_RemoveDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE), (ReasonType) msg.getData().getSerializable(KEY_REASON_TYPE));
                    break;
                case MESSAGE_UPDATE_DEVICE:
                    RoomHubMgr_UpdateDevice((RoomHubDevice) msg.getData().getParcelable(KEY_DEVICE));
                    break;
                case MESSAGE_CONTROL_LED:
                    RoomHubMgr_Led(msg.getData());
                    break;
                case MESSAGE_UPDATE_OWNER_ID: {
                    String uuid = (String) msg.obj;
                    RoomHubData roomhub_data = getRoomHubDataByUuid(uuid);
                    if (roomhub_data != null) {
                        String owner_id = roomhub_data.getRoomHubDevice().getOwnerId();
                        log("MESSAGE_UPDATE_OWNER_ID owner_id=" + owner_id);
                        roomhub_data.setOwnerId(owner_id);
                        if (mAccountMgr.getUserId().equals(owner_id)) {
                            roomhub_data.setRoleName(RoomHubDef.ROLE_OWNER);
                        } else {
                            roomhub_data.setRoleName(RoomHubDef.ROLE_NONE);
                        }
                    }
                    break;
                }

                case MESSAGE_OTA_UPGRADE:
                    RoomHubMgr_OTAUpgradeChange((DeviceFirmwareUpdateStateResPack)msg.obj);
                    break;
                case MESSAGE_WAKE_UP:
                    RoomHubMgr_ProcessWakeUp();
                    break;
                case MESSAGE_NAME_CHANGE:
                    RoomHubMgr_NameChange((NameChangeResPack)msg.obj);
                    break;
                case MESSAGE_NO_CLOUD_IDENTITY:
                    RoomHubMgr_CheckNoCloudIdentity();
                    break;
            }
        }
    }

    @Override
    public void startup() {
        // startup asset managers
        Collection<BaseAssetManager> asset_device_mgr = mAssetMgrHashMap.values();
        for(BaseAssetManager asset_mgr:asset_device_mgr) {
            asset_mgr.startup();
        }

        mAccountMgr=((RoomHubService) mContext).getAccountManager();
        mAccountMgr.registerForLoginState(this);

        mACNoticeManager = ((RoomHubService) mContext).getACNoticeManager();
        mOTAMgr =((RoomHubService) mContext).getOTAManager();

        IntentFilter filter = new IntentFilter();
        filter.addAction(MiddlewareApi.CLOUD_DONE_ACTION);
        filter.addAction(GlobalDef.ACTION_WAKE_UP);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void terminate() {
        if (mBackgroundThread != null ) {
            mBackgroundThread.quit();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
        if(mAccountMgr!=null)
            mAccountMgr.unRegisterForLoginState(this);

        mApi.unregisterRoomHubDeviceListener(CATEGORY.ROOMHUB, this);
        mApi.unregisterRoomHubSignalListener(this);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action=intent.getAction();
            log("RoomHubMgr broadcast action="+action);

            if(MiddlewareApi.CLOUD_DONE_ACTION.equals(action)) {
                log("Cloud done");
                mBackgroundHandler.sendEmptyMessageDelayed(MESSAGE_NO_CLOUD_IDENTITY, DELAY_CHECK_NO_CLOUD_IDENTITY);
            }else if(GlobalDef.ACTION_WAKE_UP.equals(action)){
                mBackgroundHandler.sendEmptyMessage(MESSAGE_WAKE_UP);
            }
        }
    };

    public RoomHubManager(Context context, MiddlewareApi api) {
        super(context, BaseManager.ROOMHUB_MANAGER);
        mApi = api;

        mBackgroundThread=new HandlerThread("RoomHubManager");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        mCommandThread=new HandlerThread("RoomHubCommand");
        mCommandThread.start();
        mCommandHandler = new CommandHandler(mCommandThread.getLooper());

        mApi.registerRoomHubDeviceListener(CATEGORY.ROOMHUB, this);
        mApi.registerRoomHubSignalListener(CATEGORY.ROOMHUB, this);
        mApi.registerHomeApplianceSignalListeners(CATEGORY.ROOMHUB, this);

        mRoomHubListener = new LinkedHashSet<RoomHubChangeListener>();
    }

    @Override
    public void addDevice(RoomHubDevice device,ReasonType reason) {
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
    public void removeDevice(RoomHubDevice device,ReasonType reason) {
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
        log("switchNetwork connected:" + connected);
        mIsConnected=connected;
    }

    @Override
    public void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType) {
        boolean IsNotify = false;
        RoomHubData roomhub_data = getRoomHubDataByUuid(dataResPack.getUuid());

        if(roomhub_data != null) {
            if(roomhub_data.IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            String type = dataResPack.getSensorDataType();
            double value = dataResPack.getValue();

            if(type != null) {
                if (type.equals(SensorTypeKey.SENSOR_TEMPERATURE) && (roomhub_data.getSensorTemp() != value)) {
                    roomhub_data.setSensorTemp(value);

                    IsNotify = true;
                } else if (type.equals(SensorTypeKey.SENSOR_HUMIDITY) && (roomhub_data.getSensorHumidity() != value)) {
                    roomhub_data.setSensorHumidity(value);
                    IsNotify = true;
                }

                log("RoomHubDataUpdate type=" + type + " value=" + value + " IsNotify=" + IsNotify);

                if (IsNotify == true)
                    sendRoomHubDataBroadcast(roomhub_data, UPDATE_SENSOR_DATA);
            }
        }
    }

    @Override
    public void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack) {
    }

    @Override
    public void RoomHubDeviceInfoChangeUpdate(DeviceInfoChangeResPack deviceInfoChangeResPack, SourceType sourceTyep) {

    }

    @Override
    public void RoomHubNameChangeUpdate(NameChangeResPack nameResPack) {
        if(nameResPack != null)
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_NAME_CHANGE, nameResPack));
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
    public void RoomHubUpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {

    }

    @Override
    public void RoomHubDeleteSchedule(DeleteScheduleResPack resPack) {

    }

    @Override
    public void RoomHubNextSchedule(NextScheduleResPack resPack) {

    }

    @Override
    public void RoomHubOTAUpgradeStateChangeUpdate(DeviceFirmwareUpdateStateResPack resPack) {
        if(resPack != null)
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_OTA_UPGRADE, resPack));
    }

    /*get room hub data list of only cloud or only alljoyn*/
    public ArrayList<RoomHubData>getRoomHubDataList(boolean is_login){
        if(mRoomHubDataList == null) return null;

        ArrayList<RoomHubData> data_list = new ArrayList<RoomHubData>();

        if(is_login){
            synchronized(mRoomHubDataList) {
                for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                    RoomHubData data = it.next();
                    data_list.add(data);
                }
            }
        }else{
            synchronized(mRoomHubDataList) {
                for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                    RoomHubData data = it.next();
                    if (data.IsAlljoyn()) {
                        data_list.add(data);
                    }
                }
            }
        }

        Collections.sort(data_list);
        return data_list;
    }

    /* get roomhub data list of source type is alljoyn and no owner */
    public ArrayList<RoomHubData>getUnBindingList(){
        if(mRoomHubDataList==null) return null;

        ArrayList<RoomHubData> data_list=new ArrayList<RoomHubData>();
        RoomHubDevice device;

        synchronized(mRoomHubDataList) {
            for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                RoomHubData data = it.next();
                if (!data.IsCloud() && data.IsAlljoyn()) {
                    device = data.getRoomHubDevice();
                    if (device != null) {
                        if (device.getOwnerId() == null ||
                                device.getOwnerId().equals("")) {
                            data_list.add(data);
                        }
                    }
                }
            }
        }
        return data_list;
    }

    private void NotifyRemoveDevice(RoomHubDevice device,RoomHubData roomhub_data){
        if(device == null) return;

        boolean is_send = true;

        if(is_send) {
            ArrayList<AssetInfoData> asset_list=roomhub_data.getAssetList();
            if(asset_list != null && asset_list.size() > 0) {
                if (mAssetListener != null) {
                    synchronized (mAssetListener) {
                        for (Iterator<AssetListener> it = mAssetListener.iterator(); it.hasNext(); ) {
                            AssetListener listener = it.next();
                            for (Iterator<AssetInfoData> el_it = asset_list.iterator(); el_it.hasNext(); ) {
                                AssetInfoData asset_data = el_it.next();
                                listener.removeAssetDevice(asset_data, roomhub_data);
                            }
                        }
                    }
                }
            }

            RoomHubData data;
            data = new RoomHubData(device);
            if (mRoomHubListener != null) {
                synchronized (mRoomHubListener) {
                    for (Iterator<RoomHubChangeListener> it = mRoomHubListener.iterator(); it.hasNext(); ) {
                        RoomHubChangeListener listener = it.next();
                        listener.removeDevice(data);
                    }
                }
            }
        }
    }

    private void NotifyAddDevice(RoomHubData data){
        if(data == null) return;

        boolean is_send = true;

        if(is_send){
            log("NotifyAddDevice add device");
            if (mRoomHubListener != null) {
                synchronized (mRoomHubListener) {
                    for (Iterator<RoomHubChangeListener> it = mRoomHubListener.iterator(); it.hasNext(); ) {
                        RoomHubChangeListener listener = it.next();
                        listener.addDevice(data);
                    }
                }
            }
        }
    }

    private void NotifyUpgrade(RoomHubData data,boolean is_upgrade){
        if(data == null) return;

        String uuid=data.getUuid();
        data.setUpgrade(is_upgrade);
        log("NotifyUpgrade add device");

        if (mRoomHubListener != null) {
            synchronized (mRoomHubListener) {
                for (Iterator<RoomHubChangeListener> it = mRoomHubListener.iterator(); it.hasNext(); ) {
                    RoomHubChangeListener listener = it.next();
                    listener.UpgradeStatus(uuid, is_upgrade);
                }
            }
        }
    }

    private void sendRoomHubDataBroadcast(RoomHubData data,int data_type){
        if(data == null) return;

        if(mRoomHubListener != null) {
            synchronized (mRoomHubListener) {
                for (Iterator<RoomHubChangeListener> it = mRoomHubListener.iterator(); it.hasNext(); ) {
                    RoomHubChangeListener listener = it.next();
                    listener.UpdateRoomHubData(data_type, data);
                }
            }

        }
    }

    public RoomHubData getRoomHubDataByUuid(String Uuid){
        if(mRoomHubDataList==null) return null;

        return mRoomHubDataList.get(Uuid);
    }


    public int modifiedDeviceName(String uuid,String name){
        int ret=ErrorKey.ROOMHUB_RENAME_FAILURE;

        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null){
            RoomHubDevice device=roomhub_data.getRoomHubDevice();
            if(device != null)
                ret=device.modifyDeviceName(name);
        }

        return ret;
    }

    private void RoomHubMgr_AddDevice(RoomHubDevice device,ReasonType reason){
        String uuid=device.getUuid();
        String dev_name=device.getName();
        SourceType source_type=device.getSourceType();
        int category = device.getCategory();

        log("RoomHubMgr_AddDevice uuid="+uuid+" type="+source_type+" reason="+reason);

        if(category != CATEGORY.ROOMHUB) {
            log("category not matched!");
            return;
        }

        if(source_type == SourceType.ALLJOYN) {
            if (mOnBoardingSSID != null && mOnBoardingSSID.size() > 0) {
                log("RoomHubMgr_AddDevice mOnBoardingSSID size=" + mOnBoardingSSID.size());
                String mac_str = mOnBoardingSSID.get(uuid);
                if (mac_str != null && !mac_str.isEmpty()) {
                    log("RoomHubMgr_AddDevice set ssid uuid=" + uuid + " dev_name=" + dev_name + " mac_str=" + mac_str);

                    if (setOnBoardingSSID(device, dev_name, mac_str))
                        mOnBoardingSSID.remove(uuid);
                }
            }
        }

        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);

        if(roomhub_data==null){
            log("RoomHubMgr_AddDevice device is not exist");

            roomhub_data=new RoomHubData(device);
            NotifyUpgrade(roomhub_data, mOTAMgr.isUpgrading(uuid));
            UpdateRole(device, roomhub_data);

            synchronized(mRoomHubDataList) {
                mRoomHubDataList.put(uuid,roomhub_data);
            }

            NotifyAddDevice(roomhub_data);
            if(roomhub_data.IsOnLine()) {
                setAllAssets(roomhub_data, reason);
            }
        } else{
            log("RoomHubMgr_AddDevice device is exist");
            if(reason == ReasonType.USERSHARE){
                CloudDevice cloud_device=device.getExtraInfo();
                if(cloud_device != null) {
                    String targetUser=cloud_device.getTagetUser();
                    String cur_userId=mAccountMgr.getUserId();

                    log("RoomHubMgr_AddDevice targetUser=" + targetUser + " cur_userId=" + cur_userId);
                    if (targetUser.equals(cur_userId)){
                        roomhub_data.UpdateRoomHubData(device);
                        UpdateRole(device, roomhub_data);

                        NotifyUpgrade(roomhub_data, mOTAMgr.isUpgrading(uuid));
                    }
                }
            }else {
                roomhub_data.UpdateRoomHubData(device);
                UpdateRole(device, roomhub_data);
                NotifyUpgrade(roomhub_data, mOTAMgr.isUpgrading(uuid));
            }

            if(roomhub_data.IsOnLine())
                setAllAssets(roomhub_data, reason);

            sendRoomHubDataBroadcast(roomhub_data, UPDATE_SENSOR_DATA);
            sendRoomHubDataBroadcast(roomhub_data, UPDATE_ROOMHUB_DATA);
        }
    }

    private void UpdateRole(RoomHubDevice device,RoomHubData data){
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
                        data.setOwnerName(cloud_device.getOwnerName());
                    }
                }else{
                    data.setRoleName(RoomHubDef.ROLE_NONE);
                }
            }else if(data.IsCloud()){
                CloudDevice cloud_device=device.getExtraInfo();
                if(cloud_device != null) {
                    log("UpdateRole is_cloud  rolename="+cloud_device.getRoleName()+" owner_name="+cloud_device.getOwnerName());
                    data.setRoleName(cloud_device.getRoleName());
                    data.setOwnerName(cloud_device.getOwnerName());
                }
            }else{
                data.setRoleName(RoomHubDef.ROLE_NONE);
            }
        }
    }

    private void RoomHubMgr_RemoveDevice(RoomHubDevice device,ReasonType reason){
        log("RoomHubMgr_RemoveDevice uuid=" + device.getUuid() + " type=" + device.getSourceType());
        String uuid=device.getUuid();
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);

        if (roomhub_data != null){
            if(reason == ReasonType.USERSHARE){
                CloudDevice cloud_device=device.getExtraInfo();
                String targetUser=cloud_device.getTagetUser();
                String cur_user_id=mAccountMgr.getUserId();

                if(targetUser!=null) {
                    if (targetUser.equals(cur_user_id)) {
                        roomhub_data.RemoveRoomHubData(device);
                        roomhub_data.setRoleName(RoomHubDef.ROLE_NONE);
                    }
                }
            }else{
                roomhub_data.RemoveRoomHubData(device);
            }

            int type=roomhub_data.getSourceType();
            if(type == 0){
                NotifyRemoveDevice(device,roomhub_data);
                synchronized(mRoomHubDataList) {
                    mRoomHubDataList.remove(uuid);
                }
            }else{
                if(reason != ReasonType.USERSHARE)
                    UpdateRole(device, roomhub_data);

                sendRoomHubDataBroadcast(roomhub_data, UPDATE_ROOMHUB_DATA);
            }
            if(device.getSourceType() == SourceType.ALLJOYN) {
                String cur_activity_name=Utils.getCurrentActivityName(mContext);

                if (!cur_activity_name.equals("com.quantatw.roomhub.ui.OnBoardingActivity") &&
                        (roomhub_data.IsOwner()) && (!mAccountMgr.isLogin()) && (mIsConnected)) {
                    mACNoticeManager.sendRoomHubNoticeProcess(roomhub_data, ACNoticeManager.MESSAGE_NOTICE_DEVICE_LOST);
                }
            }
        } else {
            log("RoomHubMgr_RemoveDevice is not exist uuid=" + device.getUuid() + " type=" + device.getSourceType());
        }
    }

    private void RoomHubMgr_UpdateDevice(RoomHubDevice device){
        log("RoomHubMgr_UpdateDevice uuid="+device.getUuid()+" type="+device.getSourceType());

        RoomHubData roomhub_data = getRoomHubDataByUuid(device.getUuid());
        if (roomhub_data != null) {
            if(roomhub_data.IsAlljoyn())
                return;

            if(roomhub_data.UpdateOnLineStatus(device)){
                sendRoomHubDataBroadcast(roomhub_data,UPDATE_ONLINE_STATUS);
                if(roomhub_data.IsOnLine())
                    setAllAssets(roomhub_data, ReasonType.CLOUD);
                else
                    roomhub_data.getAssetList().clear();
            }

            if(roomhub_data.UpdateRoomHubName(device)){
                sendRoomHubDataBroadcast(roomhub_data,UPDATE_ROOMHUB_NAME);
            }

            roomhub_data.UpdateFirmwareVersion(device);

            roomhub_data.UpdateRoomHubDevice(device);
            sendRoomHubDataBroadcast(roomhub_data, UPDATE_ROOMHUB_DATA);

            if(!roomhub_data.IsOnLine()){
                if(roomhub_data.IsOwner()){
                    //detect MQTT off line notification
                    log("NotificationControlInvalid H60Failure_Net_002");
                    mACNoticeManager.sendRoomHubNoticeProcess(roomhub_data, ACNoticeManager.MESSAGE_NOTICE_DEVICE_OFFLINE);
                }
            }
        }
    }

    private int RoomHubMgr_Led(Bundle bundle){
        int ret=ErrorKey.ROOMHUB_LED_CONTROL_FAILURE;

        String uuid=bundle.getString(KEY_UUID);

        RoomHubData roomhub_data = getRoomHubDataByUuid(uuid);
        if(roomhub_data != null){
            RoomHubDevice device=roomhub_data.getRoomHubDevice();
            if(device != null){
                int cmd_value=bundle.getInt(KEY_CMD_VALUE);
                int cmd_value1=bundle.getInt(KEY_CMD_VALUE1);
                int cmd_value2=bundle.getInt(KEY_CMD_VALUE2);
                int cmd_value3=bundle.getInt(KEY_CMD_VALUE3);
                int cmd_value4 = bundle.getInt(KEY_CMD_VALUE4);

                ret=device.ledControl(cmd_value, cmd_value1, cmd_value2, cmd_value3, cmd_value4);
            }
        }

        log("RoomHubMgr_Led : ret=" + ret);

        return ret;
    }

    private void RoomHubMgr_OTAUpgradeChange(DeviceFirmwareUpdateStateResPack resPack){
        RoomHubData roomhub_data = getRoomHubDataByUuid(resPack.getUuid());

        if(roomhub_data != null) {
            if((resPack.getState() >= 0) && (resPack.getState() <= 2)){
                NotifyUpgrade(roomhub_data, true);
            }else if ((resPack.getState() == 3) || (resPack.getState() == -1000)){
                NotifyUpgrade(roomhub_data,false);
            }
        }
    }

    private void RoomHubMgr_ProcessWakeUp(){
        if(mRoomHubDataList == null) return;

        new Thread() {
            @Override
            public void run() {
                synchronized (mRoomHubDataList) {
                    for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                        RoomHubData data = it.next();
                        if (data.IsCloud()){
                            log("RoomHubMgr_ProcessWakeUp cloud device uuid="+data.getUuid());
                            data.getRoomHubDevice().setSourceType(SourceType.CLOUD);
                            data.UpdateRoomHubData(data.getRoomHubDevice());
                            sendRoomHubDataBroadcast(data,UPDATE_SENSOR_DATA);
                            sendRoomHubDataBroadcast(data,UPDATE_ROOMHUB_NAME);
                            sendRoomHubDataBroadcast(data,UPDATE_ROOMHUB_DATA);
                            sendRoomHubDataBroadcast(data,UPDATE_ONLINE_STATUS);
                        }
                    }
                }
            }
        }.start();
    }

    private void RoomHubMgr_NameChange(NameChangeResPack resPack){
        RoomHubData roomhub_data = getRoomHubDataByUuid(resPack.getUuid());
        if(roomhub_data != null){
            roomhub_data.setName(resPack.getName());
            sendRoomHubDataBroadcast(roomhub_data, UPDATE_ROOMHUB_NAME);
        }
    }

    private void RoomHubMgr_AddElectric(String roomhubUuid,String asset_uuid,int type,int connection_type){
        int retval=ErrorKey.ADD_APPLIANCES_FAILURE;

        RoomHubData roomhub_data=getRoomHubDataByUuid(roomhubUuid);
        if(roomhub_data != null){
            if(asset_uuid == null) {
                AssetInfoData asset_data = roomhub_data.AssetIsExist(type);
                if (asset_data == null) {
                    AddHomeApplianceReqPack req_pack = new AddHomeApplianceReqPack();
                    req_pack.setAssetType(type);
                    req_pack.setConnectionType(connection_type);

                    AddHomeApplianceResPack res_pack = roomhub_data.getRoomHubDevice().addHomeAppliance(req_pack);
                    if (res_pack != null) {
                        retval = res_pack.getStatus_code();
                    }
                } else
                    retval = ErrorKey.APPLIANCES_ALREADY_EXISTS;

                log("AddElectric type=" + type + " roomhubUuid="+ roomhubUuid+ " connection_type="+connection_type+" retval=" + retval);
            }else{
                AddHomeApplianceReqPack req_pack = new AddHomeApplianceReqPack();
                req_pack.setAssetUuid(asset_uuid);
                req_pack.setAssetType(type);
                req_pack.setConnectionType(connection_type);

                AddHomeApplianceResPack res_pack = roomhub_data.getRoomHubDevice().addHomeAppliance(req_pack);
                if (res_pack != null) {
                    retval = res_pack.getStatus_code();
                }
                log("AddElectric type=" + type + " roomhubUuid="+ roomhubUuid+" asset_uuid=" + asset_uuid + " connection_type="+connection_type+" retval=" + retval);
            }
        }

        if (mAssetListener != null) {
            synchronized (mAssetListener) {
                for (Iterator<AssetListener> it = mAssetListener.iterator(); it.hasNext(); ) {
                    AssetListener listener = it.next();
                    listener.onAssetResult(roomhubUuid,asset_uuid, retval);
                }
            }
        }
    }

    private void RoomHubMgr_RemoveElectric(String uuid,String asset_uuid,int type){
        String remove_asset_uuid=asset_uuid;
        int retval=ErrorKey.DELETE_APPLIANCES_FAILURE;
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null){
            if(remove_asset_uuid == null){
                AssetInfoData asset_data=roomhub_data.AssetIsExist(type);
                if(asset_data != null)
                    remove_asset_uuid=asset_data.getAssetUuid();
            }
            if(remove_asset_uuid != null){
                RemoveHomeApplianceReqPack req_pack=new RemoveHomeApplianceReqPack();
                req_pack.setAssetType(type);
                req_pack.setUuid(remove_asset_uuid);

                RemoveHomeApplianceResPack res_pack=roomhub_data.getRoomHubDevice().removeHomeAppliance(req_pack);
                if(res_pack != null) {
                    retval = res_pack.getStatus_code();
                }
            }
            log("RemoveElectric type=" + type + " asset_uuid=" + remove_asset_uuid + " retval=" + retval);
        }

        if (mAssetListener != null) {
            synchronized (mAssetListener) {
                for (Iterator<AssetListener> it = mAssetListener.iterator(); it.hasNext(); ) {
                    AssetListener listener = it.next();
                    listener.onAssetResult(uuid,remove_asset_uuid, retval);
                }
            }
        }
    }

    private void RoomHubMgr_FailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType){
        RoomHubData roomhub_data=getRoomHubDataByUuid(failRecoverResPack.gethubUUID());
        if(roomhub_data != null && roomhub_data.IsAlljoyn() && (sourceType == SourceType.CLOUD))
            return;

        mACNoticeManager.sendFailRecover(roomhub_data, failRecoverResPack);
    }

    private void RoomHubMgr_RebootRoomHub(String uuid){
        int ret_val=ErrorKey.ROOMHUB_REBOOT_FAILURE;

        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null){
            CommandResPack res_pack=roomhub_data.getRoomHubDevice().RebootRoomHub();

            if(res_pack != null) {
                ret_val=res_pack.getStatus_code();
            }
        }
        log("RoomHubMgr_RebootRoomHub uuid="+uuid+" ret_val="+ret_val);
    }

    private void RoomHubMgr_CheckNoCloudIdentity(){
        if(mRoomHubDataList == null) return;

        synchronized(mRoomHubDataList) {
            for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                RoomHubData data = it.next();
                if(data.IsOwner()){
                    if(data.IsAlljoyn() && !data.IsCloud()){
                        log("RoomHubMgr_CheckNoCloudIdentity uuid="+data.getUuid()+" dev_name="+ data.getName());
                        Intent intent = new Intent(GlobalDef.ACTION_NO_CLOUD_IDENTIFY);
                        Bundle bundle = new Bundle();
                        bundle.putString(GlobalDef.ROOMHUB_UUID_MESSAGE, data.getUuid());
                        bundle.putString(GlobalDef.ROOMHUB_DEVNAME_MESSAGE, data.getName());
                        intent.putExtras(bundle);
                        mContext.sendBroadcast(intent);

                    }
                }
            }
        }
    }

    @Override
    public void onLogin() {
        log("onLogin");
        if(mRoomHubDataList.size() >0 ){
            synchronized (mRoomHubDataList) {
                for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                    RoomHubData data = it.next();
                    UpdateRole(data.getRoomHubDevice(), data);
                }
            }
        }
    }

    @Override
    public void onLogout() {
        mBackgroundHandler.removeMessages(MESSAGE_NO_CLOUD_IDENTITY);
        if(mRoomHubDataList.size() >0 ){
            synchronized (mRoomHubDataList) {
                for (Iterator<RoomHubData> it = mRoomHubDataList.values().iterator(); it.hasNext(); ) {
                    RoomHubData data = it.next();
                    UpdateRole(data.getRoomHubDevice(), data);
                }
            }
        }
    }

    @Override
    public void onSkipLogin() {
        log("onSkipLogin");
    }

    public int regDeviceToCloud(String uuid, String dev_name, String dev_version,
                                String townId, String macString){
        AddDeviceReqPack reqPack=new AddDeviceReqPack();

        reqPack.setUuid(uuid);
        reqPack.setDevice_name(dev_name);
        reqPack.setTownId(townId);
        reqPack.setVersion(dev_version);
        reqPack.setDeviceType(CATEGORY.ROOMHUB);
        AddCloudDeviceResPack res=mApi.addDevice(reqPack);

        int ret_val=res.getStatus_code();
        log("regDeviceToCloud ret_val=" + ret_val);
        // FIXME: Add GlobalDef status code for -31. (For AddDevice() -31 means ok, too.)
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(ret_val == ErrorKey.Success || (ret_val == ErrorKey.DeviceHasOwnerAlready)){
            if(roomhub_data != null){
                log("regDeviceToCloud dev_data != null");
                RoomHubDevice device=roomhub_data.getRoomHubDevice();
                if(device != null){
                    log("regDeviceToCloud device != null");
                    if(!setOnBoardingSSID(device, dev_name, macString)){
                        log("regDeviceToCloud set ssid fail uuid=" + uuid + " macString=" + macString);
                        mOnBoardingSSID.put(uuid, macString);
                    }
                }
            }
        }else {
            ResetRoomHub(roomhub_data);
        }
        return ret_val;
    }

    private String getNewHomeApSSID(String dev_name, String macString) {
        String retString;

        /*
        * 20160328 change naming rule:
        *   Original: "Home wifi SSID" (MAX length = 15) + "_H60_" + "MAC address (last 3 bytes)"
        *   New:      "Home wifi SSID" (MAX length = 9)  + "_"     + "MAC address (last 1 byte)"
         */
        PreferenceEditor pref = new PreferenceEditor(mContext, GlobalDef.ROOMHUB_SETTINGS_PREFERENCE_NAME);
        String homeApSSID = pref.getStringValue(GlobalDef.ROOMHUB_SETTINGS_WIFI_SSID);
//        int mac_len = mContext.getResources().getInteger(R.integer.config_mac_fixed_len);
        int mac_len = MAC_FIXED_LEN;

        String appendStr = "_";
        int cutLen = homeApSSID.length() + mac_len + appendStr.length() - HOME_AP_SSID_MAX_LEN;
        if(cutLen > 0) {
            homeApSSID = homeApSSID.substring(0, homeApSSID.length() - cutLen);
        }

        macString = macString.substring(macString.length()-mac_len);
        retString = homeApSSID + appendStr + macString;

        return retString;
    }

    private boolean setOnBoardingSSID(RoomHubDevice dev,String dev_name, String macString){

        if(dev != null){
            String newHomeAPSSID = getNewHomeApSSID(dev_name, macString);
            log("regDeviceToCloud newHomeAPSSID=" + newHomeAPSSID);

            dev.setOwnerId(mAccountMgr.getUserId());
            dev.setSSID(newHomeAPSSID);

            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_UPDATE_OWNER_ID, dev.getUuid()));
            return true;
        }

        return false;
    }

    public int unRegDeviceToCloud(String uuid){
        DeleteDeviceReqPack pack = new DeleteDeviceReqPack();
        pack.setUuid(uuid);
        BaseResPack res = mApi.deleteDevice(pack);
        return res.getStatus_code();
    }

    public void DeleteDevice(String uuid){
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null) {
            if(roomhub_data.IsAlljoyn() && roomhub_data.IsCloud()){
                unRegDeviceToCloud(uuid);
                ResetRoomHub(roomhub_data);
            }else if(!roomhub_data.IsAlljoyn() && roomhub_data.IsCloud()){
                unRegDeviceToCloud(uuid);
            }else if(roomhub_data.IsAlljoyn() && !roomhub_data.IsCloud()){
                ResetRoomHub(roomhub_data);
            }

            NotifyRemoveDevice(roomhub_data.getRoomHubDevice(), roomhub_data);
            synchronized(mRoomHubDataList) {
                mRoomHubDataList.remove(uuid);
            }
        }
    }

    private void ResetRoomHub(RoomHubData data){
        if((data != null) && (data.IsAlljoyn())){
            log("ResetRoomHub is alljoyn uuid=" + data.getUuid());
            try {
                RoomHubDevice dev=data.getRoomHubDevice();
                if(dev != null) {
                    RoomHubInterface roomhub_interface=dev.getRoomHubInterface();
                    if(roomhub_interface != null)
                        roomhub_interface.setownerId("");

                    log("ResetRoomHub config_interface.factoryReset()");

                    ConfigCtrlInterface config_interface=dev.getConfigCtrlInterface();
                    if(config_interface != null) {
                        config_interface.factoryReset();
                    }
                }
            } catch (BusException e) {
                e.printStackTrace();
            }
        }
    }

    public int setLed(String uuid,int color, int controlType, int enableMsec, int disableMsec, int loopNumber){
        Message msg=new Message();
        msg.what=MESSAGE_CONTROL_LED;
        Bundle bundle=new Bundle();
        bundle.putString(KEY_UUID, uuid);
        bundle.putInt(KEY_CMD_VALUE, color);
        bundle.putInt(KEY_CMD_VALUE1, controlType);
        bundle.putInt(KEY_CMD_VALUE2, enableMsec);
        bundle.putInt(KEY_CMD_VALUE3, disableMsec);
        bundle.putInt(KEY_CMD_VALUE4, loopNumber);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    public void registerRoomHubChange(RoomHubChangeListener listener) {
        synchronized (mRoomHubListener) {
            mRoomHubListener.add(listener);
        }
    }

    public void unRegisterRoomHubChange(RoomHubChangeListener listener) {
        synchronized(mRoomHubListener) {
            mRoomHubListener.remove(listener);
        }
    }

    public void registerAssetListener(AssetListener listener) {
        synchronized (mAssetListener) {
            mAssetListener.add(listener);
        }
    }

    public void unRegisterAssetListener(AssetListener listener) {
        synchronized(mAssetListener) {
            mAssetListener.remove(listener);
        }
    }

    public void registerAssetChangeListener(AssetChangeListener listener,int asset_type) {
        BaseAssetManager base_asset=getAssetDeviceManager(asset_type);
        base_asset.registerAssetsChange(listener);
    }

    public void unRegisterAssetChangeListener(AssetChangeListener listener,int asset_type) {
        BaseAssetManager base_asset=getAssetDeviceManager(asset_type);
        base_asset.unRegisterAssetsChange(listener);

    }

    public CityListResPack getCityList(String lang) {
        return mApi.GetCityList(lang);
    }

    public TownListResPack getTownList(int cityId, String lang) {
        return mApi.GetTownList(cityId, lang);
    }

    public boolean setOTAServerPath(String uuid, String url) {
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null) {
            RoomHubDevice device=roomhub_data.getRoomHubDevice();
            if(device != null) {
                return device.startUpgrade(url);
            }
        }
        return false;
    }

    public boolean setOTAServerPath(String uuid, String url, String md5) {
        RoomHubData roomhub_data=getRoomHubDataByUuid(uuid);
        if(roomhub_data != null) {
            RoomHubDevice device=roomhub_data.getRoomHubDevice();
            if(device != null) {
                UpgradeReqPack reqPack = new UpgradeReqPack();
                reqPack.setImageURL(url);
                reqPack.setMd5(md5);
                BaseHomeApplianceResPack res = device.startUpgrade(reqPack);
                if(res.getStatus_code() == ErrorKey.Success){
                    return true;
                }
            }
        }
        return false;
    }

    /*
    * register new asset device
    */
    public int registerAssetDeviceManager(BaseAssetManager asset_mgr) {
        int asset_type = asset_mgr.getAssetType();
        if(RoomHubDef.getCategory(asset_type) != CATEGORY.ROOMHUB)
            return ErrorKey.ASSET_REGISTER_TYPE_ERROR;
        if(mAssetMgrHashMap.get(asset_type) != null)
            return ErrorKey.ASSET_REGISTER_TYPE_DUPLICATE;
        mAssetMgrHashMap.put(asset_type,asset_mgr);
        return ErrorKey.Success;
    }

    /*
    * get asset device manager by asset type
    */
    public BaseAssetManager getAssetDeviceManager(int type) {
        BaseAssetManager asset_device_mgr = mAssetMgrHashMap.get(type);
        return asset_device_mgr;
    }

    public ArrayList<BaseAssetManager> getAllAssetDevice(){
        ArrayList<BaseAssetManager> base_asset_list=new ArrayList<BaseAssetManager>(mAssetMgrHashMap.values());
        Collections.sort(base_asset_list);

        return base_asset_list;
    }

    @Override
    public void addAsset(AssetResPack assetResPack, SourceType sourceType) {
        int asset_type = assetResPack.getAssetType();

        BaseAssetManager base_asset_mgr = getAssetDeviceManager(asset_type);
        if(base_asset_mgr == null)
            return;

        RoomHubData data=getRoomHubDataByUuid(assetResPack.getUuid());

        if(assetResPack.getStatus_code() < ErrorKey.Success ){
            AddAssetDeviceListener(null, data, assetResPack.getStatus_code());
            return;
        }

        if(data != null){
            String asset_uuid=assetResPack.getAssetUuid();
            log("addAsset type=" + asset_type + " asset_uuid=" + asset_uuid+" source_type="+sourceType);

            boolean notify_add_asset=false;
            AssetInfoData asset_data = data.AssetIsExist(asset_uuid);
            if(asset_data == null) {
                log("addAsset is not exist");
                asset_data=base_asset_mgr.newAssetData(data);
                asset_data.setAssetUuid(asset_uuid);
                data.getAssetList().add(asset_data);

                notify_add_asset=true;
            }else{
                log("addAsset is exist");
            }

            if(sourceType == SourceType.ALLJOYN)
                asset_data.setSourceType(RoomHubData.SOURCE_TYPE_ALLJOYN);
            else
                asset_data.setSourceType(RoomHubData.SOURCE_TYPE_CLOUD);

            log("addAsset source_type="+asset_data.getSourceType());

            if(notify_add_asset) {
                AddAssetDeviceListener(asset_data, data, assetResPack.getStatus_code());

                ArrayList<AssetInfoData> asset_data_list=data.getAssetList();
                if(asset_data_list.size() > 0)
                    Collections.sort(asset_data_list);
            }
        }
    }

    @Override
    public void removeAsset(AssetResPack assetResPack, SourceType sourceType) {
        if(assetResPack.getStatus_code() < ErrorKey.Success )
            return;

        RoomHubData data=getRoomHubDataByUuid(assetResPack.getUuid());
        if(data != null){
            int asset_type=assetResPack.getAssetType();
            String asset_uuid=assetResPack.getAssetUuid();

            AssetInfoData asset_data = data.AssetIsExist(asset_uuid);
            if(asset_data != null){
                log("removeAsset type=" + asset_type + " asset_uuid=" + asset_uuid);

                if (mAssetListener != null) {
                    synchronized (mAssetListener) {
                        for (Iterator<AssetListener> it = mAssetListener.iterator(); it.hasNext(); ) {
                            AssetListener listener = it.next();
                            listener.removeAssetDevice(asset_data, data);
                        }
                    }
                }

                ArrayList<AssetInfoData> asset_data_list=data.getAssetList();
                synchronized (mRoomHubDataList) {
                    asset_data_list.remove(asset_data);
                }

                if(asset_data_list.size() > 0)
                    Collections.sort(asset_data_list);
            }
        }
    }

    @Override
    public void updateAsset(AssetResPack assetResPack, SourceType sourceType) {
        if(assetResPack.getStatus_code() < ErrorKey.Success )
            return;

        RoomHubData data=getRoomHubDataByUuid(assetResPack.getUuid());
        if(data.IsAlljoyn() && (sourceType == SourceType.CLOUD))
            return;

        if(data != null){
            String asset_uuid=assetResPack.getAssetUuid();
            int asset_type=assetResPack.getAssetType();

            AssetInfoData asset_data = data.AssetIsExist(asset_uuid);
            if (asset_data != null) {
                log("updateAsset type=" + asset_type + " asset_uuid=" + asset_uuid);
                if (mAssetListener != null) {
                    synchronized (mAssetListener) {
                        for (Iterator<AssetListener> it = mAssetListener.iterator(); it.hasNext(); ) {
                            AssetListener listener = it.next();
                            listener.updateAssetDevice(asset_data, data);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void AssetInfoChange(int assetType, Object assetDetailInfoResPack, SourceType sourceType) {

    }

    @Override
    public void FirmwareUpdateStateChange(FirmwareUpdateStateResPack firmwareUpdateStateResPack) {

    }

    @Override
    public void AcFailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {
        int reason=failRecoverResPack.getReason();
        log("AcFailRecover uuid=" + failRecoverResPack.getUuid() + " reason=" + reason);
        if(reason == ACDef.AC_FAIL_RECOVER_TEMP_TOO_HIGH) {
            Message msg = new Message();
            msg.what = MESSAGE_FAIL_RECOVER;
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_FAIL_RECOVER, failRecoverResPack);
            bundle.putSerializable(KEY_CMD_VALUE, sourceType);
            msg.setData(bundle);
            mCommandHandler.sendMessage(msg);
        }
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

    public int AddElectric(String roomhubUuid,String asset_uuid,int type,int connection_type){
        Message msg = new Message();
        msg.what = MESSAGE_ADD_ELECTRIC;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_UUID, roomhubUuid);
        bundle.putString(KEY_CMD_VALUE, asset_uuid);
        bundle.putInt(KEY_CMD_VALUE1, type);
        bundle.putInt(KEY_CMD_VALUE2, connection_type);
        msg.setData(bundle);
        mCommandHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    public int RemoveElectric(String roomhub_uuid,String asset_uuid,int type){
        Message msg = new Message();
        msg.what = MESSAGE_REMOVE_ELECTRIC;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_UUID, roomhub_uuid);
        bundle.putString(KEY_ASSET_UUID, asset_uuid);
        bundle.putInt(KEY_CMD_VALUE, type);
        msg.setData(bundle);
        mCommandHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    private void setAllAssets(RoomHubData data,ReasonType reason){
        //  if(!data.IsOwner() && !data.IsFriend()) return;
        if(data == null) return;

        boolean check_ver=data.checkVersion(data.getVersion(), "1.1.06");
        if(check_ver) {
            boolean retval;
            log("setAllAssets uuid=" + data.getUuid() + " reason=" + reason);
            if (reason == ReasonType.ALLJOYN) {
                retval = setAssetsForAlljoyn(data);
            } else if(reason == ReasonType.CLOUD) {
                retval = setAssetsForCloud(data);
            } else if(reason == ReasonType.USERSHARE){
                if(!data.IsAlljoyn() && (data.IsCloud()))
                    retval = setAssetsForCloud(data);
            }
        }

    }

    private boolean setAssetsForAlljoyn(RoomHubData data){
        if(data == null) return false;

        log("setAssetsForAlljoyn uuid=" + data.getUuid());

        GetHomeApplianceAllAssetsResPack res_pack = data.getRoomHubDevice().getHomeApplianceAllAssets();
        if(res_pack == null) return false;

        ArrayList<HomeApplianceAsset> appliance_list = res_pack.getAssets();
        if(appliance_list == null){
            RemoveOnlyCloudAsset(data);
            return false;
        }

        log("setAssetsForAlljoyn appliance size=" + appliance_list.size());
        for (Iterator<HomeApplianceAsset> it = appliance_list.iterator(); it.hasNext(); ) {
            HomeApplianceAsset appliance_data = it.next();
            ArrayList<String> asset_uuid_list = appliance_data.getUuid();
            if(asset_uuid_list != null){
                for (int i = 0; i < asset_uuid_list.size(); i++) {
                    String asset_uuid = asset_uuid_list.get(i);
                    int asset_type = appliance_data.getAssetType();
                    BaseAssetManager base_asset_mgr = getAssetDeviceManager(asset_type);
                    if(base_asset_mgr == null)
                        continue;

                    AssetInfoData asset_data=data.AssetIsExist(asset_uuid);
                    if (asset_data == null) {
                        log("setAssetsForAlljoyn add roomhub_uuid="+ data.getUuid() +" type=" + asset_type + " asset_uuid=" + asset_uuid);
                        asset_data = base_asset_mgr.newAssetData(data);
                        asset_data.setSourceType(RoomHubData.SOURCE_TYPE_ALLJOYN);
                        asset_data.setAssetUuid(asset_uuid);
                        data.getAssetList().add(asset_data);

                        AddAssetDeviceListener(asset_data, data, ErrorKey.Success);
                    }else{
                        log("setAssetsForAlljoyn update roomhub_uuid="+ data.getUuid() +" type=" + asset_type + " asset_uuid=" + asset_uuid);
                        asset_data.setSourceType(RoomHubData.SOURCE_TYPE_ALLJOYN);
                        if (mAssetListener != null) {
                            synchronized (mAssetListener) {
                                for (Iterator<AssetListener> listener_it = mAssetListener.iterator(); listener_it.hasNext(); ) {
                                    AssetListener listener = listener_it.next();
                                    listener.updateAssetDevice(asset_data, data);
                                }
                            }
                        }
                    }
                    log("setAssetsForAlljoyn source_type=" + asset_data.getSourceType());
                }
            }
        }

        RemoveOnlyCloudAsset(data);

        if(data.getAssetList().size() > 0)
            Collections.sort(data.getAssetList());

        return false;
    }

    private void AddAssetDeviceListener(AssetInfoData asset_data,RoomHubData data,int result){
        if (mAssetListener != null) {
            synchronized (mAssetListener) {
                for (Iterator<AssetListener> listener_it = mAssetListener.iterator(); listener_it.hasNext(); ) {
                    AssetListener listener = listener_it.next();
                    listener.addAssetDevice(asset_data, data, result);
                }
            }
        }
    }

    private void RemoveOnlyCloudAsset(RoomHubData data){
        log("RemoveOnlyCloudAsset uuid=" + data.getUuid());
        ArrayList<AssetInfoData> asset_data_list=data.getAssetList();
        log("RemoveOnlyCloudAsset list size=" + asset_data_list.size());
        if(asset_data_list != null){
            for (Iterator<AssetInfoData> it = asset_data_list.iterator(); it.hasNext(); ) {
                AssetInfoData asset_data = it.next();
                log("RemoveOnlyCloudAsset uuid=" + asset_data.getAssetUuid()+" source_type="+asset_data.getSourceType());
                if(!asset_data.IsAlljoyn()) {
                    log("RemoveOnlyCloudAsset uuid=" + asset_data.getAssetUuid());
                    if (mAssetListener != null) {
                        synchronized (mAssetListener) {
                            for (Iterator<AssetListener> listener_it = mAssetListener.iterator(); listener_it.hasNext(); ) {
                                AssetListener listener = listener_it.next();
                                listener.removeAssetDevice(asset_data, data);
                            }
                        }
                    }
                    it.remove();
                }
            }
        }
    }

    private boolean setAssetsForCloud(RoomHubData data) {
        if(data == null) return false;

        ArrayList<Asset> asset_lst = data.getRoomHubDevice().getExtraInfo().getDeviceAssets();
        if(asset_lst == null) return false;

        Asset asset;

        for (Iterator<Asset> it = asset_lst.iterator(); it.hasNext(); ) {
            asset = it.next();
            String asset_uuid=asset.getAssetUuid();
            int asset_type=asset.getAssetType();

            BaseAssetManager base_asset_mgr = getAssetDeviceManager(asset_type);
            if(base_asset_mgr == null)
                continue;

            AssetInfoData asset_data=data.AssetIsExist(asset_uuid);
            if ((asset_data == null) && (!data.IsAlljoyn())) {
                log("setAssetsForCloud add asset_type=" + asset_type + " asset_uuid=" + asset_uuid);

                asset_data = base_asset_mgr.newAssetData(data);
                asset_data.setSourceType(AssetInfoData.SOURCE_TYPE_CLOUD);
                asset_data.setAssetUuid(asset_uuid);
                data.getAssetList().add(asset_data);
                AddAssetDeviceListener(asset_data,data,ErrorKey.Success);

            } else {
                if(asset_data != null) {
                    log("setAssetsForCloud update type=" + asset_type + " asset_uuid=" + asset_uuid);
                    asset_data.setSourceType(AssetInfoData.SOURCE_TYPE_CLOUD);
                    if (mAssetListener != null) {
                        synchronized (mAssetListener) {
                            for (Iterator<AssetListener> listener_it = mAssetListener.iterator(); listener_it.hasNext(); ) {
                                AssetListener listener = listener_it.next();
                                listener.updateAssetDevice(asset_data, data);
                            }
                        }
                    }
                }
            }
        }

        if(data.getAssetList().size() > 0)
            Collections.sort(data.getAssetList());
        return false;
    }

    public void RebootRoomHub(String uuid){
        mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MESSAGE_REBOOT_ROOM_HUB,uuid));
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }
}