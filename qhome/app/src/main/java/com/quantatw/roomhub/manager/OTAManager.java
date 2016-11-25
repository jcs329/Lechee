package com.quantatw.roomhub.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.listener.OTAStateChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.ui.OTAActivity;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.RoomHubFailureCause;
import com.quantatw.roomhub.utils.SupportVersion;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.device.FirmwareVersion;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.listener.OTACloudStateUpdateListener;
import com.quantatw.sls.pack.roomhub.DeviceFirmwareUpdateStateResPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateReqPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by erin on 1/15/16.
 */
public class OTAManager extends BaseManager implements RoomHubChangeListener,AccountLoginStateListener {
    private static final String TAG = OTAManager.class.getSimpleName();

    public static final String OTA_DEVICE_UUID = "OTA_DEVICE_UUID";
    public static final String OTA_AUTO_UPDATE = "OTA_AUTO_UDPATE";

    public final int OTA_DEVICE_CAPS_LOGIN = 0x1;
    public final int OTA_DEVICE_CAPS_OWNER = 0x2;
    public final int OTA_DEVICE_CAPS_ALLJOYN = 0x4;
    public final int OTA_DEVICE_CAPS_UPGRADE = 0x8;

    public enum OTADeviceLackCapability {
        NONE(0x0,0),
        LOGIN(0x1,R.string.ota_login_hint),
        OWNER(0x2,R.string.only_owner_use),
        ALLJOYN(0x4,R.string.roomhub_warning_msg),
        UPGRADE(0x5,R.string.device_upgrade_not_operate);

        private int value;
        private int msg_res_id;
        private OTADeviceLackCapability(int value,int msg_res_id) {
            this.value = value;
            this.msg_res_id = msg_res_id;
        }

        public int getValues() {
            return this.value;
        }

        public int getStringResourceId() {
            return this.msg_res_id;
        }
    }

    private MiddlewareApi middlewareApi;
    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private RoomHubManager mRoomHubManager;
    private AccountManager mAccountMgr;
    private IRController mIRController;
    private Timer mCheckVersionTimer;

    private final int MESSAGE_ADD_DEVICE = 100;
    private final int MESSAGE_REMOVE_DEVICE = 101;
    private final int MESSAGE_CHECK_VERSION = 102;
    private final int MESSAGE_START_UPGRADE = 103;
    private final int MESSAGE_UPGRADE_FAIL = 104;
    private final int MESSAGE_UPGRADE_DONE = 105;
    private final int MESSAGE_LOGIN = 106;

    private final int MESSAGE_STATE_CHANGE = 200;
    private final int MESSAGE_STATE_CHANGE_TIMEOUT = 201;

    private final int MESSAGE_AUTO_UPDATE_PREPARE = 300;
    private final int MESSAGE_AUTO_UPDATE_USER_CONFIRM = 301;
    private final int MESSAGE_AUTO_UPDATE_USER_FEEDBACK = 302;

    private final int MESSAGE_AUTO_UPDATE_USER_CONFIRM_TIMEOUT = 400;

    private final int MESSAGE_INTERNAL_CHECK_VERSION_TIMEOUT = 500;
    private final int MESSAGE_INTERNAL_AUTO_UPDATE_START = 501;

    private final int MESSAGE_LAUNCH_FROM_NOTIFICATION_CENTER = 600;

    private final int mStateChangeTimeout;

    private HashMap<String, OTADevice> autoUpdateDeviceHashMap = new HashMap<>();

    private enum AutoUpdateState {
        IDLE,
        START,
        SEND_CONFIRM,
        SEND_CONFIRM_DONE
    }
    private AutoUpdateState mAutoUpdateState = AutoUpdateState.IDLE;

    private OTAState mState = OTAState.IDLE;

    public interface OTAManagerCallback {
        void checkVersion(OTADevice otaDevice, VersionCheckUpdateReqPack versionCheckUpdateReqPack);
        void upgrade(String uuid, String url);
        void upgrade(String uuid, String url, String md5);
    }

    private OTAManagerCallback mOTAManagerCallback = new OTAManagerCallback() {
        @Override
        public void checkVersion(OTADevice otaDevice, VersionCheckUpdateReqPack versionCheckUpdateReqPack) {
            //log(TAG,"checkVersion start otaDevice="+otaDevice+",uuid="+otaDevice.getUuid());
            String assignVersion = mContext.getString(R.string.config_ota_auto_udpate_debug);
            if(!TextUtils.isEmpty(assignVersion)) {
                versionCheckUpdateReqPack.setVersion(assignVersion);
            }
            OTADevice.NewVersionInfo newVersionInfo = null;
            VersionCheckUpdateResPack versionCheckUpdateResPack = middlewareApi.CheckVersion(versionCheckUpdateReqPack);
            if (versionCheckUpdateResPack != null &&
                    versionCheckUpdateResPack.getStatus_code() == ErrorKey.Success) {
                FirmwareVersion firmwareVersion = versionCheckUpdateResPack.getData();
                if (firmwareVersion != null) {
                    newVersionInfo = new OTADevice.NewVersionInfo();
                    newVersionInfo.setDriverName(firmwareVersion.getName());
                    newVersionInfo.setVersion(firmwareVersion.getVersion());
                    newVersionInfo.setMd5(firmwareVersion.getMd5());
                    newVersionInfo.setUrl(firmwareVersion.getUrl());
                    log(TAG, newVersionInfo.toString());
                }
            }
            //log(TAG, "checkVersion done, newVersionInfo=" + newVersionInfo);
            otaDevice.setNewVersionInfo(newVersionInfo);
            if(newVersionInfo == null)
                otaDevice.setCurrentState(OTAState.READY);
        }

        @Override
        public void upgrade(String uuid, String url) {
            log(TAG, "upgrade uuid=" + uuid+",url="+url);
            if (!mRoomHubManager.setOTAServerPath(uuid, url)){
                DeviceFirmwareUpdateStateResPack resPack = new DeviceFirmwareUpdateStateResPack();
                resPack.setState(-1);
                resPack.setStateMsg("Update fail");
                resPack.setUuid(uuid);
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_STATE_CHANGE, resPack));
            }
        }

        @Override
        public void upgrade(String uuid, String url, String md5) {
            log(TAG, "upgrade uuid=" + uuid+", url="+url+",md5="+md5);
            if (!mRoomHubManager.setOTAServerPath(uuid, url, md5)){
                DeviceFirmwareUpdateStateResPack resPack = new DeviceFirmwareUpdateStateResPack();
                resPack.setState(-1);
                resPack.setStateMsg("Update fail");
                resPack.setUuid(uuid);
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_STATE_CHANGE, resPack));
            }
        }
    };

    private HashMap<String, OTADevice> mOTADeviceList = new HashMap<>();

    private OTACloudStateUpdateListener otaStateChangeListener = new OTACloudStateUpdateListener() {
        @Override
        public void stateChange(DeviceFirmwareUpdateStateResPack stateResPack) {
            if(stateResPack.getStatus_code()!= ErrorKey.Success)
                return;
            log(TAG, "stateChange from MQTT: uuid=" + stateResPack.getUuid() + ",state=" + stateResPack.getState());
            mBackgroundHandler.sendMessage(
                    mBackgroundHandler.obtainMessage(MESSAGE_STATE_CHANGE, stateResPack));
        }
    };

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_ADD_DEVICE:
                    break;
                case MESSAGE_REMOVE_DEVICE:
                    break;
                case MESSAGE_STATE_CHANGE:
                    log(TAG, "--- MESSAGE_STATE_CHANGE ---");
                    updateState((DeviceFirmwareUpdateStateResPack)msg.obj);
                    break;
                case MESSAGE_CHECK_VERSION:
                    log(TAG,"--- MESSAGE_CHECK_VERSION ---");
                    deviceCheckVersionNow((String) msg.obj);
                    break;
                case MESSAGE_START_UPGRADE:
                    log(TAG,"--- MESSAGE_START_UPGRADE ---");
                    deviceStartUpgradeNow((String)msg.obj);
                    break;
                case MESSAGE_UPGRADE_DONE:
                    log(TAG,"--- MESSAGE_UPGRADE_DONE ---");
                    deviceUpgradeDone((OTADevice) msg.obj);
                    break;
                case MESSAGE_UPGRADE_FAIL:
                    log(TAG,"--- MESSAGE_UPGRADE_FAIL ---");
                    deviceUpgradeFail((OTADevice) msg.obj);
                    break;
                case MESSAGE_STATE_CHANGE_TIMEOUT:
                    log(TAG, "--- MESSAGE_STATE_CHANGE_TIMEOUT ---");
                    deviceUpgradeTimeout((OTADevice) msg.obj);
                    break;
                case MESSAGE_AUTO_UPDATE_PREPARE:
                    log(TAG, "--- MESSAGE_AUTO_UPDATE_PREPARE ---");
                    prepareAutoUpdate();
                    break;
                case MESSAGE_AUTO_UPDATE_USER_CONFIRM:
                    log(TAG, "--- MESSAGE_AUTO_UPDATE_USER_CONFIRM ---");
                    if(Utils.isRoomHubAppForeground(mContext)) {
                        launchOTAActivity((OTADevice) msg.obj);
                    }
                    else {
                        sendReminderMessage((OTADevice) msg.obj);
                    }
                    break;
                case MESSAGE_AUTO_UPDATE_USER_CONFIRM_TIMEOUT:
                    log(TAG, "--- MESSAGE_AUTO_UPDATE_USER_CONFIRM_TIMEOUT ---");
                    handleAutoUpdateUserConfirmTimeout((OTADevice)msg.obj);
                    break;
                case MESSAGE_AUTO_UPDATE_USER_FEEDBACK:
                    log(TAG, "--- MESSAGE_AUTO_UPDATE_USER_FEEDBACK ---");
                    handleAutoUpdateUserFeedback((OTADevice)msg.obj);
                    break;
                case MESSAGE_INTERNAL_CHECK_VERSION_TIMEOUT:
                    log(TAG, "--- MESSAGE_INTERNAL_CHECK_VERSION_TIMEOUT ---");
                    OTADevice otaDevice = (OTADevice)msg.obj;
                    if(otaDevice.getNewVersionInfo() != null && otaDevice.isForceUpdate()) {
                        synchronized (autoUpdateDeviceHashMap) {
                            autoUpdateDeviceHashMap.put(otaDevice.getUuid(), otaDevice);
                        }
                        if(!hasMessages(MESSAGE_INTERNAL_AUTO_UPDATE_START))
                            sendEmptyMessageDelayed(MESSAGE_INTERNAL_AUTO_UPDATE_START,5000);
                    }
                    break;
                case MESSAGE_INTERNAL_AUTO_UPDATE_START:
                    log(TAG, "--- MESSAGE_INTERNAL_AUTO_UPDATE_START ---");
                    removeMessages(MESSAGE_INTERNAL_AUTO_UPDATE_START);
                    log(TAG,"mAutoUpdateState="+mAutoUpdateState);
                    if(!Utils.isRoomHubDoingOnBoarding(mContext) &&
                            (mAutoUpdateState == AutoUpdateState.IDLE ||
                            mAutoUpdateState == AutoUpdateState.SEND_CONFIRM_DONE))
                    {
                        mAutoUpdateState = AutoUpdateState.START;
                        if (autoUpdateDeviceHashMap.size() > 0)
                            prepareAutoUpdate();
                    }
                    break;
                case MESSAGE_LAUNCH_FROM_NOTIFICATION_CENTER: {
                    log(TAG, "--- MESSAGE_LAUNCH_FROM_NOTIFICATION_CENTER ---");
                    String uuid = (String) msg.obj;
                    OTADevice target = getOTADevice(uuid);
                    if (target != null)
                        if(checkOTACapability(target))
                            launchOTAActivity(target);
                        else {
                            OTADeviceLackCapability capability = checkDevicCapability(uuid);
                            log(TAG, "device caps = " + capability);
                            Toast.makeText(mContext,capability.getStringResourceId(),Toast.LENGTH_SHORT).show();
                        }
                    else {
                        Toast.makeText(mContext,R.string.ota_device_not_exist,Toast.LENGTH_SHORT).show();
                    }

                }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void deviceUpgradeDone(OTADevice otaDevice) {
        otaDevice.upgradeDone();
    }

    private void deviceUpgradeFail(OTADevice otaDevice) {
        otaDevice.upgradeFail();
    }

    private void updateState(DeviceFirmwareUpdateStateResPack resPack) {
        OTADevice otaDevice = getOTADevice(resPack.getUuid());
        log(TAG, "updateState enter uuid=" + resPack.getUuid() + ",otaDevice=" + otaDevice);
        if(otaDevice != null) {
            //log(TAG,"updateState removeMessages "+otaDevice);
            mBackgroundHandler.removeMessages(MESSAGE_STATE_CHANGE_TIMEOUT, otaDevice);
            otaDevice.setDoUpgrade(true);
            otaDevice.setCurrentState(OTAState.UPGRADE);
            OTADevice.UpgradeStateInfo upgradeStateInfo =
                    otaDevice.getUpgradeStateInfo();
            if(upgradeStateInfo == null) {
                upgradeStateInfo = new OTADevice.UpgradeStateInfo(resPack.getState(),resPack.getStateMsg());
            }
            else {
                if(resPack.getState()<upgradeStateInfo.state && resPack.getState() >=0) {
                    log(TAG,"updateState : new state ["+resPack.getState()+"] < current state ["+upgradeStateInfo.state+"] . Ignore it!!!");
                    return;
                }
                upgradeStateInfo.update(resPack.getState(), resPack.getStateMsg());
            }
            otaDevice.setUpgradeStateInfo(upgradeStateInfo);
            if(otaDevice.getUpgradeStateInfo().state < 0) { // upgrade fail:
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_UPGRADE_FAIL, otaDevice));
            }
            else {
                if(otaDevice.getUpgradeStateInfo().state == 3) { // upgrade done:
                    mBackgroundHandler.sendMessage(
                            mBackgroundHandler.obtainMessage(MESSAGE_UPGRADE_DONE, otaDevice));
                }
                else {
                    log(TAG,"updateState sendMessageDelayed "+otaDevice);
                    mBackgroundHandler.sendMessageDelayed(
                            mBackgroundHandler.obtainMessage(MESSAGE_STATE_CHANGE_TIMEOUT, otaDevice), mStateChangeTimeout);
                }
            }
        }
    }

    private void enableCheckTimer(boolean enable) {
        if(enable) {
            scheduleTimer();
            /*
//            final long day =
//                    mContext.getResources().getInteger(R.integer.config_ota_auto_check_timeout)*60*60*1000;
            final long day = 60 * 1000;   //test 90 secs
            mCheckVersionTimer = new Timer();
            Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR));
            calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
            Date firstTime = calendar.getTime();
            //mCheckVersionTimer.schedule(new CheckLatestVersionTask(), firstTime, day);
            mCheckVersionTimer.schedule(new CheckLatestVersionTask(), day);
            */
        }
        else {
            mCheckVersionTimer.cancel();
        }
    }

    private void scheduleTimer() {
//        final long day = 60 * 1000;   //test 90 secs
        final long day =
              mContext.getResources().getInteger(R.integer.config_ota_auto_check_timeout)*60*1000;
        mCheckVersionTimer = new Timer();
        mCheckVersionTimer.schedule(new CheckLatestVersionTask(), day);
    }

    private class CheckLatestVersionTask extends TimerTask {
        @Override
        public void run() {
            log(TAG,"CheckLatestVersionTask run");
            for (Iterator<OTADevice> it = mOTADeviceList.values().iterator(); it.hasNext(); ) {
                OTADevice otaDevice = it.next();
                if(checkOTACapability(otaDevice)==true) {
                    OTADevice.NewVersionInfo newVersionInfo = otaDevice.getNewVersionInfo();
                    if (newVersionInfo != null) {
                        autoUpdateDeviceHashMap.put(otaDevice.getUuid(), otaDevice);
                    }
                }
            }
            if(autoUpdateDeviceHashMap.size() > 0) {
                mBackgroundHandler.sendEmptyMessage(MESSAGE_AUTO_UPDATE_PREPARE);
            }
            // schedule next timer:::
            scheduleTimer();
        }
    }

    private void deviceCheckVersionNow(String uuid) {
        //log(TAG, "deviceCheckVersionNow uuid=" + uuid);
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null)
            otaDevice.checkVersion();
    }

    private void deviceStartUpgradeNow(String uuid) {
        //log(TAG, "deviceStartUpgradeNow uuid=" + uuid);
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null)
            otaDevice.doUpgrade();

        log(TAG, "deviceStartUpgradeNow sendMessageDelayed " + otaDevice);
        mBackgroundHandler.sendMessageDelayed(
                mBackgroundHandler.obtainMessage(MESSAGE_STATE_CHANGE_TIMEOUT, otaDevice), mStateChangeTimeout);

    }

    private void deviceUpgradeTimeout(OTADevice otaDevice) {
        log(TAG, "deviceUpgradeTimeout uuid=" + otaDevice.getUuid());
        otaDevice.handleUpgradeTimeout();
        // TODO: for temporary: notify RoomHubManager
        notifyRoomHubManagerUpgradeTimeout(otaDevice);
        //
    }

    private void notifyRoomHubManagerUpgradeTimeout(OTADevice otaDevice) {
        int timeoutState = -1000;
        DeviceFirmwareUpdateStateResPack deviceFirmwareUpdateStateResPack =
                new DeviceFirmwareUpdateStateResPack();
        deviceFirmwareUpdateStateResPack.setUuid(otaDevice.getUuid());
        deviceFirmwareUpdateStateResPack.setState(timeoutState);
        deviceFirmwareUpdateStateResPack.setStateMsg(otaDevice.getUpgradeStateInfo().msg);
        mRoomHubManager.RoomHubOTAUpgradeStateChangeUpdate(deviceFirmwareUpdateStateResPack);
    }

    private synchronized OTADevice getOTADevice(String uuid) {
        return mOTADeviceList.get(uuid);
    }

    private void sendConfirm(OTADevice otaDevice) {
        log(TAG, "sendConfirm " + otaDevice);
        mBackgroundHandler.sendMessage(
                mBackgroundHandler.obtainMessage(MESSAGE_AUTO_UPDATE_USER_CONFIRM, otaDevice)
        );
//        mBackgroundHandler.sendMessageDelayed(
//                mBackgroundHandler.obtainMessage(MESSAGE_AUTO_UPDATE_USER_CONFIRM_TIMEOUT, otaDevice)
//                , 5000);
    }

    private void prepareAutoUpdate() {
        log(TAG, "prepareAutoUpdate enter");
        OTADevice target = null;
        for (Iterator<OTADevice> it = autoUpdateDeviceHashMap.values().iterator(); it.hasNext(); ) {
            OTADevice otaDevice = it.next();
            if(!otaDevice.isUserConfirmAutoUpdate()) {
                target = otaDevice;
                break;
            }
        }

        if(target != null) {
            if(!target.isUpgrading()) {
                mAutoUpdateState = AutoUpdateState.SEND_CONFIRM;
                sendConfirm(target);
            }
            else {
                synchronized (autoUpdateDeviceHashMap) {
                    autoUpdateDeviceHashMap.remove(target.getUuid());
                }
            }
        }
    }

    private void sendNextAutoUpdate(OTADevice otaDevice) {
        log(TAG, "sendNextAutoUpdate " + otaDevice);
        if(otaDevice != null) {
//            mBackgroundHandler.removeMessages(MESSAGE_AUTO_UPDATE_USER_CONFIRM_TIMEOUT, otaDevice);

            OTADevice nextTarget = getNextAutoUpdateDevice(otaDevice);
            synchronized (autoUpdateDeviceHashMap) {
                autoUpdateDeviceHashMap.remove(otaDevice.getUuid());
            }
            if(nextTarget != null) {
                mAutoUpdateState = AutoUpdateState.SEND_CONFIRM;
                sendConfirm(nextTarget);
            }
            else {
                mAutoUpdateState = AutoUpdateState.SEND_CONFIRM_DONE;
            }
        }
    }

    private void handleAutoUpdateUserConfirmTimeout(OTADevice otaDevice) {
        sendNextAutoUpdate(otaDevice);
    }

    private void handleAutoUpdateUserFeedback(OTADevice otaDevice) {
        sendNextAutoUpdate(otaDevice);
    }

    private void launchOTAActivity(OTADevice otaDevice) {
        if(!Utils.isRoomHubDoingOnBoarding(mContext) && !mIRController.isIROnParing()) {
            log(TAG, "launchOTAActivity uuid=" + otaDevice.getUuid() + "," + otaDevice);
            otaDevice.setUserConfirmAutoUpdate(true);

            Intent intent = new Intent(mContext, OTAActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Bundle bundle = new Bundle();
            bundle.putString(OTAManager.OTA_DEVICE_UUID, otaDevice.getUuid());
            bundle.putBoolean(OTAManager.OTA_AUTO_UPDATE, true);
            intent.putExtras(bundle);
            mContext.startActivity(intent);
        }
    }

    private void sendReminderMessage(OTADevice otaDevice) {
        ReminderData reminderData = new ReminderData();
        reminderData.setUuid(otaDevice.getUuid());
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Device_002);
        reminderData.setSilent(true);
        FailureCauseInfo failureCauseInfo = reminderData.obtainFailureCauseInfo(mContext);
        failureCauseInfo.setCause(
                mContext.getString(R.string.ota_show_latest_version
                        , otaDevice.getName()
                        , otaDevice.getNewVersionInfo().getVersion()));
        FailureCauseInfo.ButtonAction okButton = new FailureCauseInfo.ButtonAction();
        okButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_OK);
        okButton.setLaunchActionType(FailureCauseInfo.LaunchActionType.DO_NOTHING);
        failureCauseInfo.setActionButton1Message(okButton);

        FailureCauseInfo.ButtonAction updateButton = new FailureCauseInfo.ButtonAction();
        updateButton.setReplyMessage(
                Message.obtain(mBackgroundHandler, MESSAGE_LAUNCH_FROM_NOTIFICATION_CENTER, otaDevice.getUuid()));
        failureCauseInfo.setActionButton3Message(updateButton);
        sendReminderMessage(reminderData);
    }

    private OTADevice getNextAutoUpdateDevice(OTADevice current) {
        log(TAG, "getNextAutoUpdateDevice current=" + current);
        OTADevice target = null;
        for (Iterator<OTADevice> it = autoUpdateDeviceHashMap.values().iterator(); it.hasNext(); ) {
            OTADevice otaDevice = it.next();
            if(equals(otaDevice,current)) {
                if(it.hasNext())
                    target = it.next();
                break;
            }
        }

        log(TAG, "getNextAutoUpdateDevice 1st found target=" + target);
        if(target != null)
            return target;

        // lookup over:
        for (Iterator<OTADevice> it = autoUpdateDeviceHashMap.values().iterator(); it.hasNext(); ) {
            OTADevice otaDevice = it.next();
            if (!equals(otaDevice,current) && !otaDevice.isUserConfirmAutoUpdate()) {
                if(it.hasNext())
                    target = it.next();
                break;
            }
        }

        log(TAG, "getNextAutoUpdateDevice 2nd found target=" + target);
        return target;
    }

    private boolean isAutoCheckEnable() {
        return mContext.getResources().getBoolean(R.bool.config_ota_auto_update);
    }

    private boolean checkOTACapability(OTADevice otaDevice) {
        log(TAG, "checkOTACapability uuid=" + otaDevice.getUuid() + ",isLogin=" + mAccountMgr.isLogin()
                + ",isAllJoyn=" + otaDevice.isAllJoyn() + ",isOwner=" + otaDevice.isOwner()
                + ",isOnLine=" + otaDevice.isOnLine()
                + ",isUpgrading=" + otaDevice.isUpgrading()+",isOnBoarding="+Utils.isRoomHubDoingOnBoarding(mContext)
                + ",isIROnParing=" + mIRController.isIROnParing()
        );


        return mAccountMgr.isLogin() && (otaDevice.isAllJoyn()||
                (mContext.getResources().getBoolean(R.bool.config_ota_support_remote)
//                        &&Utils.checkFirmwareVersion(otaDevice.getCurrentVersion(),"1.1.16.3",false)))
                        &&SupportVersion.OTAVer.isValid(otaDevice.getCurrentVersion())))
                && otaDevice.isOwner() && otaDevice.isOnLine() && !otaDevice.isUpgrading()
                && !Utils.isRoomHubDoingOnBoarding(mContext) && !mIRController.isIROnParing();
    }

    private void autoCheckAndUpdate(OTADevice otaDevice) {
        otaDevice.setForceUpdate(true);
        if(checkOTACapability(otaDevice)) {
            otaDevice.checkVersion();
            mBackgroundHandler.sendMessageDelayed(
                    mBackgroundHandler.obtainMessage(MESSAGE_INTERNAL_CHECK_VERSION_TIMEOUT, otaDevice)
                    , 30*1000); // after 30 secs
        }
    }

    /* ----------------------------------------------------------------------------------------- */
    public OTAManager(Context context, MiddlewareApi api) {
        super(context, BaseManager.OTA_MANAGER);
        middlewareApi = api;

        mBackgroundThread=new HandlerThread("OTAManager");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
        mFailureHandler = mBackgroundHandler;

        middlewareApi.registerOTAStateChange(otaStateChangeListener);

        mStateChangeTimeout =
                mContext.getResources().getInteger(R.integer.config_ota_state_change_timeout)*60*1000;
    }

    public boolean isUpgrading(String uuid) {
        OTADevice device = getOTADevice(uuid);
        if(device != null) {
            return device.isUpgrading();
        }
        return false;
    }

    public void checkVersion(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            boolean otaCapability = checkOTACapability(otaDevice);
            if(otaCapability == true) {
                mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_CHECK_VERSION, uuid));
            }
        }
    }

    public void startUpgrade(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            if(otaDevice.isUserConfirmAutoUpdate()) {
                otaDevice.setUserConfirmAutoUpdate(false);
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_AUTO_UPDATE_USER_FEEDBACK,otaDevice));
            }
            otaDevice.setPendingUpdate(false);
            mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(MESSAGE_START_UPGRADE, uuid));
        }
    }

    public void cancelUpgrade(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            otaDevice.setPendingUpdate(true);
            otaDevice.setCurrentState(OTAState.READY);
            if(otaDevice.isUserConfirmAutoUpdate()) {
                otaDevice.setUserConfirmAutoUpdate(false);
                mBackgroundHandler.sendMessage(
                        mBackgroundHandler.obtainMessage(MESSAGE_AUTO_UPDATE_USER_FEEDBACK,otaDevice));
            }
        }
    }

    public OTAState getFirmwareUpdateCurrentState(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null)
            return otaDevice.getCurrentState();
        return null;
    }

    public int getFirmwareUpdateOngoingState(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            OTADevice.UpgradeStateInfo upgradeStateInfo = otaDevice.getUpgradeStateInfo();
            if(upgradeStateInfo != null)
                return upgradeStateInfo.state;
        }
        return 0;
    }

    public OTADevice.NewVersionInfo getFirmwareNewVersion(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            if (otaDevice.getNewVersionInfo() == null)
                otaDevice.setCurrentState(OTAState.READY);
            else
                return otaDevice.getNewVersionInfo();
        }
        return null;
    }

    public void registerForOTAStateChange(String uuid, OTAStateChangeListener listener) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            otaDevice.registerStateChangeListener(listener);
        }
    }

    public void unregisterForOTAStateChange(String uuid, OTAStateChangeListener listener) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            otaDevice.unregisterStateChangeListener(listener);
        }
    }

    public String getDeviceName(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            return otaDevice.getName();
        }
        return "";
    }

    public String getDeviceCurrentVersion(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            return otaDevice.getCurrentVersion();
        }
        return "";
    }

    public int getDeviceOTACapability(String uuid) {
        int caps = 0;
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            if(!mAccountMgr.isLogin())
                caps |= OTA_DEVICE_CAPS_LOGIN;
            if(!otaDevice.isOwner())
                caps |= OTA_DEVICE_CAPS_OWNER;
            if(!otaDevice.isAllJoyn())
                caps |= OTA_DEVICE_CAPS_ALLJOYN;
            if(!otaDevice.isUpgrading())
                caps |= OTA_DEVICE_CAPS_UPGRADE;
        }
        return caps;
    }

    public OTADeviceLackCapability checkDevicCapability(String uuid) {
        OTADevice otaDevice = getOTADevice(uuid);
        if(otaDevice != null) {
            if(!mAccountMgr.isLogin())
                return OTADeviceLackCapability.LOGIN;
            if(!otaDevice.isOwner())
                return OTADeviceLackCapability.OWNER;
            if(!otaDevice.isAllJoyn())
                return OTADeviceLackCapability.ALLJOYN;
            if(otaDevice.isUpgrading())
                return OTADeviceLackCapability.UPGRADE;
        }
        return OTADeviceLackCapability.NONE;
    }

    /* ----------------------------------------------------------------------------------------- */

    /* BaseManager ------------------------------------------------------------------ */
    @Override
    public void startup() {
        mIRController = ((RoomHubService) mContext).getIRController();
        mRoomHubManager=((RoomHubService) mContext).getRoomHubManager();
        mRoomHubManager.registerRoomHubChange(this);
        mAccountMgr=((RoomHubService) mContext).getAccountManager();
        mAccountMgr.registerForLoginState(this);

        if(isAutoCheckEnable() && mAccountMgr.isLogin())
            enableCheckTimer(true);
    }

    @Override
    public void terminate() {
    }
    /* BaseManager ------------------------------------------------------------------ */

    /* AccountLoginStateListener ------------------------------------------------------------------ */
    @Override
    public void onLogin() {
        // startup timer
        if(mCheckVersionTimer == null && isAutoCheckEnable())
            enableCheckTimer(true);

        if(autoUpdateDeviceHashMap.size() > 0) {
            if(mAutoUpdateState == AutoUpdateState.IDLE
                    || mAutoUpdateState == AutoUpdateState.SEND_CONFIRM_DONE)
                mBackgroundHandler.sendEmptyMessage(MESSAGE_AUTO_UPDATE_PREPARE);
        }
    }

    @Override
    public void onLogout() {
        // stop timer
        if(mCheckVersionTimer != null)
            enableCheckTimer(false);
    }

    @Override
    public void onSkipLogin() {

    }
    /* AccountLoginStateListener ------------------------------------------------------------------ */

    /* RoomHubChangeListener ------------------------------------------------------------------ */

    @Override
    public void addDevice(RoomHubData data) {
        log(TAG, "addDevice uuid=" + data.getUuid() + ",currentVersion="
                        + data.getVersion()+",isAllJoyn="+data.IsAlljoyn()
                        + ",isOwner="+data.IsOwner()
        );

        OTADevice otaDevice = getOTADevice(data.getUuid());
        if(otaDevice == null) {
            otaDevice = new OTADevice(mOTAManagerCallback);
            otaDevice.setCurrentVersion(data.getVersion());
            otaDevice.setUuid(data.getUuid());
            otaDevice.setName(data.getName());
            if(TextUtils.isEmpty(otaDevice.getCurrentVersion())) {
                // TODO: get current version
            }
            else {
                otaDevice.setCurrentState(OTAState.READY);
            }
            otaDevice.setIsOwner(data.IsOwner());
            otaDevice.setIsAllJoyn(data.IsAlljoyn());
            mOTADeviceList.put(data.getUuid(), otaDevice);
            log(TAG, "add ["+data.getUuid()+"] to mOTADeviceList size=" + mOTADeviceList.size());
        }
        else {
            // Update data:
            if(!otaDevice.isUpgrading() && !otaDevice.isUserConfirmAutoUpdate())
                otaDevice.setCurrentVersion(data.getVersion());
            otaDevice.setName(data.getName());
            otaDevice.setIsOwner(data.IsOwner());
            otaDevice.setIsAllJoyn(data.IsAlljoyn());
        }
        otaDevice.setIsOnLine(data.IsOnLine());

        if(mContext.getResources().getBoolean(R.bool.config_ota_auto_update_add_device)) {
            autoCheckAndUpdate(otaDevice);
        }
    }

    @Override
    public void removeDevice(RoomHubData data) {
        log(TAG,"removeDevice uuid="+data.getUuid()+",isAllJoyn="+data.IsAlljoyn());

        OTADevice otaDevice = getOTADevice(data.getUuid());
        if(otaDevice != null) {
            log(TAG, "removeDevice doUpgrade="+otaDevice.isUpgrading());
            if(!otaDevice.isUpgrading() &&  !otaDevice.isUserConfirmAutoUpdate())
                mOTADeviceList.remove(data.getUuid());
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        log(TAG,"UpdateRoomData type"+type+",uuid="+data.getUuid()+",isAllJoyn="+data.IsAlljoyn());

        if((type==RoomHubManager.UPDATE_ROOMHUB_DATA) || (type==RoomHubManager.UPDATE_ROOMHUB_NAME)){
            // update info
            addDevice(data);
        }
    }

    /*
    @Override
    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {

    }
    */
    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {

    }

    /* RoomHubChangeListener ------------------------------------------------------------------ */
    public static void log(String tag, String msg) {
        Log.d(OTAManager.class.getSimpleName(), "[" + tag + "] " + msg);
    }

    /**
     * Null-safe equivalent of {@code a.equals(b)}.
     */
    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
