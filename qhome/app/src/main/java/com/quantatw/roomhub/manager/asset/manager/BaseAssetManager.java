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
import android.os.Parcelable;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.manager.asset.listener.BaseAssetCallback;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.listener.HomeApplianceSignalListener;
import com.quantatw.sls.listener.RoomHubSignalListener;
import com.quantatw.sls.pack.device.ScanAssetResultResPack;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import static com.quantatw.sls.api.DeviceTypeConvertApi.CATEGORY;

/**
 * Created by 95010915 on 2016/01/28.
 */
public class BaseAssetManager implements BaseAssetCallback,RoomHubChangeListener,AssetListener,RoomHubSignalListener,HomeApplianceSignalListener,Comparable<BaseAssetManager> {
    private static boolean DEBUG=true;
    protected Context mContext;
    private MiddlewareApi mApi;
    protected int mAssetType;
    protected String mAssetName;
    protected int mAssetIcon;
    private String mTag;
    private int mConnectionType;

    public static final String KEY_ASSET_DATA = "asset_data";
    public static final String KEY_UUID = "uuid";
    public static final String KEY_ASSET_UUID = "asset_uuid";

    public static final String KEY_ROOMHUB_DATA= "roomhub_data";
    public static final String KEY_CMD_TYPE= "command_type";
    public static final String KEY_CMD_VALUE= "command_value";
    public static final String KEY_CMD_VALUE1= "command_value1";

    public static final String KEY_ASSET_INFO_DATA= "asset_info_data";
    public static final String KEY_ASSET_INFO= "asset_info";
    public static final String KEY_SOURCE_TYPE= "source_type";

    protected RoomHubManager mRoomHubMgr;

    private static final int MESSAGE_ADD_DEVICE             =100;
    private static final int MESSAGE_REMOVE_DEVICE          =101;
    private static final int MESSAGE_UPDATE_DEVICE          =102;
    private static final int MESSAGE_UPDATE_ROOMHUB_DATA    =103;
    private static final int MESSAGE_SET_COMMAND            =104;
    private static final int MESSAGE_UPGRADE_STATUS_CHANGE  =105;
    private static final int MESSAGE_ASSET_INFO_CHANGE      =106;
    protected static final int MESSAGE_COMMAND_TIMEOUT      =107;
    protected static final int MESSAGE_RETRY_ASSET_INFO     =108;
    protected static final int MESSAGE_RETRY_ABILITY_LIMIT  =109;
    protected static final int MESSAGE_LEARNING_RESULT      =110;
    protected static final int MESSAGE_WAKE_UP              =111;

    private LinkedHashSet<AssetChangeListener> mAssetsListener = new LinkedHashSet<AssetChangeListener>();

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;
    private HandlerThread mCommandThread;
    protected CommandHandler mCommandHandler;
    protected HashMap<Long,String> mCmdResult=new HashMap<>();

    protected final class CommandHandler extends Handler {
        public CommandHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_SET_COMMAND:
                    //BaseAsset_SendCommand(msg.getData());
                    SendCommand(msg.getData());
                    break;
                case MESSAGE_COMMAND_TIMEOUT:
                    BaseAsset_CommandTimeOut((long) msg.obj,msg.arg1);
                    break;
            }
        }
    }

    protected final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_ADD_DEVICE:
                    BaseAsset_AddDevice((AssetInfoData) msg.getData().getParcelable(KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_REMOVE_DEVICE:
                    BaseAsset_RemoveDevice((AssetInfoData) msg.getData().getParcelable(KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_DEVICE:
                    BaseAsset_UpdateDevice((AssetInfoData) msg.getData().getParcelable(KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    BaseAsset_UpdateRoomHubData(msg.getData().getInt(KEY_CMD_TYPE), (RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPGRADE_STATUS_CHANGE:
                    BaseAsset_UpgradeStats(msg.getData().getString(KEY_UUID), msg.getData().getBoolean(KEY_CMD_VALUE));
                    break;
                case MESSAGE_ASSET_INFO_CHANGE:
                    BaseAsset_AssetInfoChange((Object) msg.getData().getParcelable(KEY_ASSET_INFO), (SourceType) msg.getData().getSerializable(KEY_SOURCE_TYPE));
                    break;
                case MESSAGE_RETRY_ASSET_INFO:
                    BaseAsset_GetAssetInfo((Object) msg.obj);
                    break;
                case MESSAGE_RETRY_ABILITY_LIMIT:
                    BaseAsset_GetAbilityLimit((Object) msg.obj);
                    break;
                case MESSAGE_LEARNING_RESULT:
                    BaseAsset_LearningResult((LearningResultResPack) msg.obj);
                    break;
                case MESSAGE_WAKE_UP:
                    BaseAsset_WakeUp();
                    break;
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action=intent.getAction();
            log("broadcast action="+action);

            if(GlobalDef.ACTION_WAKE_UP.equals(action)){
                mBackgroundHandler.sendEmptyMessage(MESSAGE_WAKE_UP);
            }
        }
    };

    public BaseAssetManager(Context context, MiddlewareApi middlewareApi,String tag,int asset_type,String asset_name, int asset_icon,int connection_type){
        mContext = context;
        mTag = tag;
        mAssetType = asset_type;
        mAssetName = asset_name;
        mAssetIcon = asset_icon;
        mConnectionType = connection_type;
        mApi = middlewareApi;

        mBackgroundThread=new HandlerThread(tag);
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());

        mCommandThread=new HandlerThread(tag+"Command");
        mCommandThread.start();
        mCommandHandler = new CommandHandler(mCommandThread.getLooper());

        mRoomHubMgr=((RoomHubService)mContext).getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
        mRoomHubMgr.registerAssetListener(this);

        mApi.registerRoomHubSignalListener(CATEGORY.ROOMHUB, this);
        mApi.registerHomeApplianceSignalListeners(CATEGORY.ROOMHUB, this);
    }

    public void startup() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GlobalDef.ACTION_WAKE_UP);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    public void terminate() {

    }

    protected AssetInfoData newAssetData(RoomHubData data) {
        return null;
    }

    public String getAssetBrandAndModel(String asset_uuid){
        return mContext.getResources().getString(R.string.ac_na);
    }

    public void configIRSetting(String uuid, String assetUuid) {}
    public void startBLEPairing(String asset_uuid) {}

    protected int sendCommandMessage(AssetDef.COMMAND_TYPE cmd_type,String uuid,int cmd_value,int cmd_value1){
        Message msg=new Message();
        msg.what=MESSAGE_SET_COMMAND;
        Bundle bundle=new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, cmd_type);
        bundle.putString(KEY_UUID, uuid);
        bundle.putInt(KEY_CMD_VALUE, cmd_value);
        bundle.putInt(KEY_CMD_VALUE1, cmd_value1);
        msg.setData(bundle);
        mCommandHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    protected int sendCommandMessage(AssetDef.COMMAND_TYPE cmd_type,String uuid,String asset_uuid,int cmd_value,int cmd_value1){
        Message msg=new Message();
        msg.what=MESSAGE_SET_COMMAND;
        Bundle bundle=new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, cmd_type);
        bundle.putString(KEY_UUID, uuid);
        bundle.putString(KEY_ASSET_UUID, asset_uuid);
        bundle.putInt(KEY_CMD_VALUE, cmd_value);
        bundle.putInt(KEY_CMD_VALUE1, cmd_value1);
        msg.setData(bundle);
        mCommandHandler.sendMessage(msg);

        return ErrorKey.Success;
    }

    protected void ProgressCmdResultCallback(long thread_id,int ret){
        String uuid=mCmdResult.get(thread_id);
        if(uuid != null){
            mCmdResult.remove(thread_id);

            onCommandResult(mAssetType, uuid, ret);
        }
    }

    protected void UpdateAssetDataAfterOnLine(Object data){
        if(data != null){
            BaseAsset_GetAssetInfo(data);
            BaseAsset_GetAbilityLimit(data);
        }
    }

    protected void RetryMessage(BaseAssetData data,int message){
        if(data.getRoomHubData().IsAlljoyn()) {
            mBackgroundHandler.sendMessageDelayed(mBackgroundHandler.obtainMessage(message, data), mContext.getResources().getInteger(R.integer.config_retry_delay_for_alljoyn));
        }else{
            mBackgroundHandler.sendMessageDelayed(mBackgroundHandler.obtainMessage(message, data), mContext.getResources().getInteger(R.integer.config_retry_delay_for_cloud));
        }
    }

    private void SendCommand(final Bundle bundle){
        new Thread() {
            @Override
            public void run() {
                String uuid;
                if(bundle.getString(KEY_ASSET_UUID) != null)
                    uuid=bundle.getString(KEY_ASSET_UUID);
                else
                    uuid=bundle.getString(KEY_UUID);

                long thread_id=Thread.currentThread().getId();
                mCmdResult.put(thread_id, uuid);
                Object obj = thread_id;
                mCommandHandler.sendMessageDelayed(mCommandHandler.obtainMessage(MESSAGE_COMMAND_TIMEOUT, obj),
                        mContext.getResources().getInteger(R.integer.config_send_command_timeout));


                log("SendCommand send command :::");
                int ret=BaseAsset_SendCommand(bundle);
                log("SendCommand send command ::: ret=" + ret);

                mCommandHandler.removeMessages(MESSAGE_COMMAND_TIMEOUT, obj);
                ProgressCmdResultCallback(thread_id, ret);
            }
        }.start();

        //return ErrorKey.Success;
    }
    public int getAssetType(){
        return mAssetType;
    }

    public String getAssetName(){
        return mAssetName;
    }

    public int getAssetIcon(){
        return mAssetIcon;
    }

    public int getConnectionType(){
        return mConnectionType;
    }
    /* BaseAssetCallback */
    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {

    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {

    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {

    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {

    }

    @Override
    public int BaseAsset_SendCommand(Bundle bundle) {
        return 0;
    }

    @Override
    public void BaseAsset_CommandTimeOut(long thread_id, int error_code) {
        if(mCmdResult.get(thread_id) != null){
            log("BaseAsset_CommandTimeOut");
            ProgressCmdResultCallback(thread_id, error_code);
        }
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade) {

    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {

    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {

    }

    @Override
    public void BaseAsset_GetAbilityLimit(Object data) {

    }

    @Override
    public void BaseAsset_LearningResult(LearningResultResPack learningResultResPack) {

    }

    @Override
    public void BaseAsset_WakeUp() {

    }

    /* RoomHubManager listener (RoomHubChangeListener) */
    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {

    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data == null) return;

        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_ROOMHUB_DATA;
        Bundle bundle=new Bundle();
        bundle.putInt(KEY_CMD_TYPE, type);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        Message msg=new Message();
        msg.what=MESSAGE_UPGRADE_STATUS_CHANGE;
        Bundle bundle=new Bundle();
        bundle.putString(KEY_UUID, uuid);
        bundle.putBoolean(KEY_CMD_VALUE, is_upgrade);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    /* RoomHubManager listener (AssetListener) */
    @Override
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result) {
        if(result < ErrorKey.Success) return;

        if(asset_info_data.getAssetType()  != mAssetType)
            return;

        Message msg=new Message();
        msg.what=MESSAGE_ADD_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ASSET_INFO_DATA, asset_info_data);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        if(asset_info_data.getAssetType()  != mAssetType)
            return;

        Message msg=new Message();
        msg.what=MESSAGE_REMOVE_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ASSET_INFO_DATA, asset_info_data);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        if(asset_info_data.getAssetType()  != mAssetType)
            return;

        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_DEVICE;
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_ASSET_INFO_DATA, asset_info_data);
        bundle.putParcelable(KEY_ROOMHUB_DATA, data);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void onAssetResult(String uuid, String asset_uuid, int result) {

    }

    /* Middleware listener (RoomHubSignalListener)*/
    @Override
    public void RoomHubDataUpdate(RoomHubDataResPack dataResPack, SourceType sourceType) {

    }

    @Override
    public void RoomHubLearningResultUpdate(LearningResultResPack learningResultResPack) {
        log("RoomHubLearningResultUpdate enter");

        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_LEARNING_RESULT,learningResultResPack));
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

    }

    /* Middleware listener (HomeApplianceSignalListener) */
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
        if(assetDetailInfoResPack == null) return;
        if(assetType != mAssetType) return;

        Message msg = new Message();
        msg.what = MESSAGE_ASSET_INFO_CHANGE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_SOURCE_TYPE, sourceType);
        bundle.putParcelable(KEY_ASSET_INFO, (Parcelable) assetDetailInfoResPack);
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

    /* Asset Listener */
    public void registerAssetsChange(AssetChangeListener listener) {
        synchronized (mAssetsListener) {
            mAssetsListener.add(listener);
        }
    }

    public void unRegisterAssetsChange(AssetChangeListener listener) {
        synchronized(mAssetsListener) {
            mAssetsListener.remove(listener);
        }
    }

    protected void onCommandResult(int type, String uuid, int ret) {
        if (mAssetsListener != null) {
            synchronized (mAssetsListener) {
                for (Iterator<AssetChangeListener> it = mAssetsListener.iterator(); it.hasNext(); ) {
                    AssetChangeListener listener = it.next();
                    Log.d("BaseManager", "onCommandResult type="+type+" uuid=" + uuid + " ret=" + ret);
                    listener.onCommandResult(type, uuid, ret);
                }
            }
        }
    }

    protected  void UpdateAssetData(int type,Object data){
        if (mAssetsListener != null) {
            synchronized (mAssetsListener) {
                for (Iterator<AssetChangeListener> it = mAssetsListener.iterator(); it.hasNext(); ) {
                    AssetChangeListener listener = it.next();
                    listener.UpdateAssetData(type, data);
                }
            }
        }
    }

    protected void NotifyAddDevice(int type,Object data){
        if (mAssetsListener != null) {
            synchronized (mAssetsListener) {
                for (Iterator<AssetChangeListener> it = mAssetsListener.iterator(); it.hasNext(); ) {
                    AssetChangeListener listener = it.next();
                    listener.addDevice(type, data);
                }
            }
        }
    }

    protected void NotifyRemoveDevice(int type,Object data){
        if (mAssetsListener != null) {
            synchronized (mAssetsListener) {
                for (Iterator<AssetChangeListener> it = mAssetsListener.iterator(); it.hasNext(); ) {
                    AssetChangeListener listener = it.next();
                    listener.removeDevice(type, data);
                }
            }
        }
    }

    protected void NotifyPageStatus(int type,boolean enable,Object data){
        if (mAssetsListener != null) {
            synchronized (mAssetsListener) {
                for (Iterator<AssetChangeListener> it = mAssetsListener.iterator(); it.hasNext(); ) {
                    AssetChangeListener listener = it.next();
                    listener.UpdatePageStatus(type, enable, data);
                }
            }
        }
    }

    @Override
    public int compareTo(BaseAssetManager another) {
        return Integer.compare(mAssetType,another.getAssetType());
    }

    protected void log(String msg) {
        if(DEBUG)
            Log.d(mTag,msg);
    }
}
