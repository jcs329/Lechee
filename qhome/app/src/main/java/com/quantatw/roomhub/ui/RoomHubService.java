package com.quantatw.roomhub.ui;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.blepair.BLEPairController;
import com.quantatw.roomhub.gcm.GCMMessageDetail;
import com.quantatw.roomhub.gcm.GcmController;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.ACNoticeManager;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.IRController;
import com.quantatw.roomhub.manager.NetworkMonitor;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.OnBoardingManager;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.manager.RoomHubDBHelper;
import com.quantatw.roomhub.manager.asset.manager.AirPurifierManager;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.BulbManager;
import com.quantatw.roomhub.manager.asset.manager.PMManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.asset.manager.FANManager;
import com.quantatw.roomhub.manager.asset.manager.TVManager;
import com.quantatw.roomhub.manager.health.bpm.BPMManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.RoomHubFailureCause;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.FriendData;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.pack.healthcare.BPMDataInfo;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.roomhub.AcOnOffStatusResPack;
import com.quantatw.sls.pack.roomhub.VersionCheckUpdateResPack;

/**
 * Created by erin on 12/3/15.
 */
public class RoomHubService extends Service implements AccountLoginStateListener,
NetworkMonitor.NetworkStateReceiverListener {
    private final String TAG=RoomHubService.class.getSimpleName();

    private final boolean DEBUG = true;

    private Context mContext;
    public static String INTENT_ROOMHUB_SERVICE_INIT_DONE = "android.intent.action.roomhub_service_init_done";
    public static final int MESSAGE_HANDLE_REMIND_MESSAGE_FINISH = 10;

    private RoomHubManager mRoomHubManager;
    private AccountManager mAccountManager;
    private RoomHubDBHelper mRoomHubDBHelper;
    private IRController mIRController;
    private MiddlewareApi mMiddlewareApi;
    private OnBoardingManager mOnBoardingManager;
    //private MicroLocationManager mMicroLocationManager;
    private NetworkMonitor mNetworkMinitor;
    private GcmController mGcmController;
    private OTAManager mOTAManager;
    private ACNoticeManager mACNoticeManager;

    private HealthDeviceManager mHealthDeviceManager;
    private BLEPairController mBLEController;

    //private static final String MQTT_SERVER =  "tcp://10.0.8.101:1883";
    //private static final String MQTT_SERVER =  "tcp://h60nservice.cloudapp.net:1883";
    private static final String MQTT_SERVER =  "tcp://h60mqtt.cloudapp.net:1883";
    //private static final String CLOUD_SERVER =  "http://10.0.8.101";
    //private static final String CLOUD_SERVER =  "http://h60service.cloudapp.net";
    private static final String CLOUD_SERVER =  "https://h60service.eql.com.tw";
    //private static final String CLOUD_SERVER = "https://h60.azurewebsites.net";

    private final IBinder mBinder = new LocalBinder();

    private boolean mIsStarup = false;
    private boolean mSchedule = false;

    private final String ACTION_SCHEDULE_ALARM = "android.intent.action.SCHEDULE_ALARM";
    private final String ACTION_POLL_ALARM = "android.intent.action.POLL_ALARM";
    private final String ACTION_RESTART = "com.quantatw.roomhub.action.restart";

    private final int RESTART_DISABLE = 0;
    private final int RESTART_ENABLE = 1;

    private PendingIntent mAlarmIntent;

    private Object mPollLock = new Object();
    private boolean mPollCheckStart = false;

    private final int ROOMHUB_NOTIFY_ID = 999;

    private ReminderDialog mAlertDialog;

    private boolean mIsOnboarding = false;
    private boolean mIsWakeUp=false;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    public class LocalBinder extends Binder {
        RoomHubService getService() {
            return RoomHubService.this;
        }
        Messenger getMessenger() {return mMessenger; }
    }

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    private final int MESSAGE_CONNECTION_CHECK = 100;
    private final int MESSAGE_STARTUP_POLLCHECK = 101;

    private final int MESSAGE_SHOW_DIALOG = 200;
    private final int MESSAGE_SHOW_NOTIFICATION = 201;
    private final int MESSAGE_SHOW_TOAST = 202;
    private final int MESSAGE_SAVE_NOTIFICATION = 203;
    private final int MESSAGE_SAVE_GCM = 204;

    private final int MESSAGE_NETWORK_AVAILABLE = 300;
    private final int MESSAGE_NETWORK_UNAVAILABLE = 301;
    private final int MESSAGE_NETWORK_SWITCH = 302;

    //private final int MESSAGE_DELAY_STARTUP_IBEACON = 500;

    private final int MESSAGE_SEND_GCM_MESSAGE = 600;

    private final int MESSAGE_BPM_NOTICE    = 700;

    private final int MESSAGE_RELEASE_GCM_WAKELOCK = 800;

    private final int MESSAGE_SEND_NO_CLOUD_IDENTIFY = 900;

    private final int MESSAGE_LAUNCH_CONTROLLER_BY_GCM = 1000;

    private int mConnectRetryCount = 0;

    private Thread mPollCheckThread;

    private Dialog mPromptUserConnectionWifiDialog;

    @Override
    public void onLogin() {
        mGcmController.register();
    }

    @Override
    public void onLogout() {
        // clear gcm registration state
        Utils.setGcmRegistration(mContext, false);
        // 20160307 leave Gcm ON
//        mGcmController.unregister();
    }

    @Override
    public void onSkipLogin() {

    }

    /**
     * Handler of incoming messages from clients.
     */
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FailureCauseInfo.LaunchActionType.DO_NOTHING:
                    log("--- ReminderData.ReminderLaunchActionType.DO_NOTHING ---");
                    break;
                case FailureCauseInfo.LaunchActionType.LAUNCH_APP:
                    break;
                case FailureCauseInfo.LaunchActionType.LAUNCH_CONTROLLER:
                    launchController((ReminderData)msg.obj);
                    break;
                case FailureCauseInfo.LaunchActionType.LAUNCH_WIFI:
                    launchWifiSetting();
                    break;
                case FailureCauseInfo.LaunchActionType.LAUNCH_MOBILE_NETWORK:
                    launchMobileSetting();
                    break;
                case MESSAGE_NETWORK_AVAILABLE:
                    break;
                case MESSAGE_NETWORK_UNAVAILABLE:
                    break;
                case MESSAGE_NETWORK_SWITCH:
                    break;
                case MESSAGE_HANDLE_REMIND_MESSAGE_FINISH:
                    log("--- MESSAGE_HANDLE_REMIND_MESSAGE_FINISH ---");
                    ReminderData reminderData = (ReminderData)msg.obj;
                    reminderData.discard(mContext);
                    log("setInUse=> false, reminder messageId="+reminderData.getMessageId()+",message extra index="+reminderData.getMessage_extraIndex());
                    break;
                case GlobalDef.PROMPT_USER_CONNECT_WIFI:
                    log("--- PROMPT_USER_CONNECT_WIFI ---");
                    if(mPromptUserConnectionWifiDialog != null && mPromptUserConnectionWifiDialog.isShowing()) {
                        log("dialog is still showing!!!");
                        return;
                    }
                    showPromptUserConnectionWifiDialog((Message)msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_CONNECTION_CHECK:
                    checkConnection();
                    break;
                case MESSAGE_STARTUP_POLLCHECK:
                    pollCheck();
                    break;
                case MESSAGE_SHOW_DIALOG:
                    showDialog((ReminderData) msg.obj);
                    break;
                case MESSAGE_SHOW_TOAST:
                    showToast((ReminderData) msg.obj);
                    break;
                case MESSAGE_SHOW_NOTIFICATION:
                    sendNotification((ReminderData) msg.obj);
                    break;
                /*
                case MESSAGE_DELAY_STARTUP_IBEACON:
                    log("--- MESSAGE_DELAY_STARTUP_IBEACON ---");
                    mMicroLocationManager.enable();
                    break;
                */
                case MESSAGE_SEND_GCM_MESSAGE:
                    log("--- MESSAGE_SEND_GCM_MESSAGE ---");
                    Bundle data = (Bundle)msg.obj;
                    int gcm_message_id = data.getInt(GlobalDef.GCM_MESSAGE_TYPE_ID);
                    if(gcm_message_id == GlobalDef.GCM_MESSAGE_ID_ROOMHUB_NOTICE) {
                        AcFailRecoverResPack acFailRecoverResPack = data.getParcelable(GlobalDef.GCM_MESSAGE_ROOMHUB_NOTICE);
                        mRoomHubManager.AcFailRecover(acFailRecoverResPack, SourceType.CLOUD);
                        ACManager acManager=(ACManager)mRoomHubManager.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
                        acManager.AcFailRecover(acFailRecoverResPack, SourceType.CLOUD);
                    }
                    else if(gcm_message_id == GlobalDef.GCM_MESSAGE_ID_GENERAL) {
                        // general message for future used, such any appliances
                        dispatchGeneralGcmMessage(data);
                    }
                    else    // for Ads or System notifications
                        showGcmMessage(data);
                    break;
                case MESSAGE_SAVE_NOTIFICATION:
                    log("--- MESSAGE_SAVE_NOTIFICATION ---");
                    saveNotification((ReminderData)msg.obj);
                    break;
                case MESSAGE_SAVE_GCM:
                    log("--- MESSAGE_SAVE_GCM ---");
                    saveGcmMessage(msg.getData());
                    break;
                case MESSAGE_BPM_NOTICE:
                    handleBPMNotice((Bundle)msg.obj);
                    break;
                case MESSAGE_RELEASE_GCM_WAKELOCK:
                    PowerManager.WakeLock wakeLock = (PowerManager.WakeLock)msg.obj;
                    wakeLock.release();
                    break;
                case MESSAGE_SEND_NO_CLOUD_IDENTIFY:
                    showNoCloudIdentityMessage((Bundle)msg.obj);
                    break;
                case MESSAGE_LAUNCH_CONTROLLER_BY_GCM: {
                    // TODO: check online status ?
                    Bundle bundle = msg.getData();
                    ReminderData reminderData = bundle.getParcelable(GlobalDef.REMINDER_MESSAGE);
                    launchController(reminderData);
                }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private AcOnOffStatusResPack getAcOnOffStatusResPack(ReminderData reminderData) {
        AcOnOffStatusResPack acOnOffStatusResPack = new AcOnOffStatusResPack();
        acOnOffStatusResPack.setUuid(reminderData.getGcmDetailTag().getUuid());
        acOnOffStatusResPack.setFunctionMode(reminderData.getGcmDetailTag().getMode());
        acOnOffStatusResPack.setTargetTemperature(reminderData.getGcmDetailTag().getTarTemp());
        acOnOffStatusResPack.setOriginTemperature(reminderData.getGcmDetailTag().getOriTemp());
        acOnOffStatusResPack.setNowTemperature(reminderData.getGcmDetailTag().getNowTemp());
        acOnOffStatusResPack.setTimeInterval(reminderData.getGcmDetailTag().getTimeDiff());
        acOnOffStatusResPack.setLastAction(reminderData.getGcmDetailTag().getLastNoti());
        acOnOffStatusResPack.setUserId(reminderData.getGcmDetailTag().getUserId());
        return acOnOffStatusResPack;
    }

    /* Notification value changed */
    private BroadcastReceiver mNotificationChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean enable = intent.getBooleanExtra(GlobalDef.KEY_SETTINGS_VALUE,false);
            Log.d(TAG,"onReceive ACTION_SETTINGS_NOTIFICATION_CHANGED enable="+enable);
            Log.d(TAG,"onReceive ACTION_SETTINGS_NOTIFICATION_CHANGED mSchedule="+mSchedule);
            if(enable) {
                if(mSchedule)
                    return;

                Intent sendIntent = new Intent(context,RoomHubService.class);
                sendIntent.putExtra(ACTION_RESTART,RESTART_ENABLE);
                startService(sendIntent);

                Log.d(TAG,"onReceive ACTION_SETTINGS_NOTIFICATION_CHANGED alarm is registerd!");
            }
            else {
                if(!mSchedule)
                    return;

                mSchedule = false;
                unregisterReceiver(mReminderReceiver);
                unregisterReceiver(mPollReceiver);

                Intent sendIntent = new Intent(context,RoomHubService.class);
                sendIntent.putExtra(ACTION_RESTART,RESTART_DISABLE);
                startService(sendIntent);

                Log.d(TAG, "onReceive ACTION_SETTINGS_NOTIFICATION_CHANGED alarm is unregisterd!");
            }
        }
    };

    /* poll alarm receiver */
    private BroadcastReceiver mPollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            log("onReceive "+action);
            if(action.equals(ACTION_SCHEDULE_ALARM)) {
                if(mSchedule)
                    sendBroadcast(new Intent(ACTION_POLL_ALARM));
                scheduleAlarms(context);
            }
            else if(action.equals(ACTION_POLL_ALARM)) {
                mServiceHandler.sendEmptyMessage(MESSAGE_CONNECTION_CHECK);
            }
        }

        private void scheduleAlarms(Context context) {
            final int PERIOD = context.getResources().getInteger(R.integer.config_poll_alarm_interval);

            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(ACTION_SCHEDULE_ALARM);
            intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
            mgr.cancel(pi);
            mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + PERIOD, pi);
            mSchedule = true;
        }
    };

    /* receive reminder
    message */
    private BroadcastReceiver mReminderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalDef.ACTION_REMINDER.equals(intent.getAction())){
                ReminderData data = intent.getParcelableExtra(GlobalDef.REMINDER_MESSAGE);
                /* no need to check provisioned state */
                boolean isProvisioned = true;   //Utils.isProvisioned(context);
                boolean isSilent = data.isSilent();

                log("--- ACTION_REMINDER ---");
                FailureCauseInfo failureCauseInfo = data.getFailureCauseInfo(mContext);

                log("MessageId=" + data.getMessageId());
                if(failureCauseInfo == null) {
                    log("failureCauseInfo not found!!!");
                    return;
                }
                log("reminderData=" + data
                                + "\n,messageId=" + data.getMessageId()
                                + "\n,message extra index=" + data.getMessage_extraIndex()
                                + "\n,failureCauseInfo" + failureCauseInfo
                                + "\n,getActionButton1Message=" + failureCauseInfo.getActionButton1Message()
                                + "\n,getActionButton2Message=" + failureCauseInfo.getActionButton2Message()
                                + "\n,getActionButton3Message=" + failureCauseInfo.getActionButton3Message()
                );

                if((failureCauseInfo.getStyle() & FailureCauseInfo.DisplayStyle.DIALOG) != 0) {
                    mServiceHandler.sendMessage(mServiceHandler.obtainMessage(MESSAGE_SAVE_NOTIFICATION, data));
                    if(isProvisioned && !mIsOnboarding) {
                        if (Utils.isRoomHubAppForeground(mContext) && !isSilent) {
                            mServiceHandler.sendMessage(
                                    mServiceHandler.obtainMessage(MESSAGE_SHOW_DIALOG, data));
                        } else {
                            mServiceHandler.sendMessage(
                                    mServiceHandler.obtainMessage(MESSAGE_SHOW_NOTIFICATION, data));
                        }
                    }
                }

                if((failureCauseInfo.getStyle() & FailureCauseInfo.DisplayStyle.TOAST) != 0) {
                    if(Utils.isRoomHubAppForeground(mContext) && !isSilent) {
                        mServiceHandler.sendMessage(
                                mServiceHandler.obtainMessage(MESSAGE_SHOW_TOAST, data));
                    }
                }

            }
            else if(GlobalDef.ACTION_GCM_MESSAGE.equals(intent.getAction())) {
                mServiceHandler.sendMessage(
                        mServiceHandler.obtainMessage(MESSAGE_SEND_GCM_MESSAGE,intent.getBundleExtra(GlobalDef.GCM_MESSAGE)));
            }else if(GlobalDef.ACTION_BPM_NOTICE.equals(intent.getAction())) {
                mServiceHandler.sendMessage(
                        mServiceHandler.obtainMessage(MESSAGE_BPM_NOTICE, intent.getBundleExtra(GlobalDef.BP_MESSAGE)));
            }else if(GlobalDef.ACTION_NO_CLOUD_IDENTIFY.equals(intent.getAction())){
                mServiceHandler.sendMessage(
                        mServiceHandler.obtainMessage(MESSAGE_SEND_NO_CLOUD_IDENTIFY,intent.getExtras()));
            }
        }
    };

    /* onBoarding receiver */
    private BroadcastReceiver mOnBoardingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(MiddlewareApi.ONBOARDING_START_ACTION)) {
                mIsOnboarding = true;
            }
            else if(action.equals(MiddlewareApi.ONBOARDING_STOP_ACTION)) {
                mIsOnboarding = false;
            }
        }
    };

    /* wake up receiver*/
    private BroadcastReceiver mWakeUpReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            log("mWakeUpReceiver action="+action);

            if(Intent.ACTION_SCREEN_ON.equals(action)){
                mIsWakeUp=true;
            }else if(GlobalDef.ACTION_NETWORK_STATE_CHAGNED.equals(action)){
                boolean connect=intent.getBooleanExtra(GlobalDef.EXTRA_NETWORK_STATE,false);
                if(mIsWakeUp && connect) {
                    sendBroadcast(new Intent(GlobalDef.ACTION_WAKE_UP));
                    mIsWakeUp=false;
                }
            }
        }
    };

    private void saveGcmMessage(Bundle data) {
        int message_type_id = data.getInt(GlobalDef.GCM_MESSAGE_TYPE_ID);
        String message = data.getString(GlobalDef.GCM_HTML_MESSAGE);
        ContentValues contentValues = new ContentValues();
        contentValues.put(RoomHubDBHelper.Notification.MESSAGE_ID, message_type_id);
        contentValues.put(RoomHubDBHelper.Notification.MESSAGE, message);
        contentValues.put(RoomHubDBHelper.Notification.TIMESTAMP, System.currentTimeMillis());
        contentValues.put(RoomHubDBHelper.Notification.EXPIRE_TIMESTAMP, -999);
        mRoomHubDBHelper.notificationInsert(mRoomHubDBHelper.getWritableDatabase(), contentValues);
    }

    private void saveNotification(ReminderData data) {
        FailureCauseInfo failureCauseInfo = data.getFailureCauseInfo(mContext);

        log("saveNotification message id="+data.getMessageId());

        ContentValues contentValues = new ContentValues();
        contentValues.put(RoomHubDBHelper.Notification.MESSAGE_ID, data.getMessageId());
        String message = data.getSimpleMessage();
        if(TextUtils.isEmpty(message))
            message = failureCauseInfo.getCause();
        contentValues.put(RoomHubDBHelper.Notification.MESSAGE, message);
        contentValues.put(RoomHubDBHelper.Notification.UUID, data.getUuid());
        contentValues.put(RoomHubDBHelper.Notification.SENDER_ID, data.getSenderId());
        final long currentTime = System.currentTimeMillis();
        log("saveNotification getTime currentTimeMillis=" + currentTime);
        contentValues.put(RoomHubDBHelper.Notification.TIMESTAMP, currentTime);
        final long endTime = (failureCauseInfo.getExpireTime()*1000);
        final long expire = currentTime+endTime;
        log("saveNotification getTime expire timestamp=" + Long.toString(expire));
        contentValues.put(RoomHubDBHelper.Notification.EXPIRE_TIMESTAMP, expire);
        FailureCauseInfo.ButtonAction buttonAction =failureCauseInfo.getActionButton1Message();
        if(buttonAction != null) {
            contentValues.put(RoomHubDBHelper.Notification.BUTTON1_TYPE, buttonAction.getButtonType());
            contentValues.put(RoomHubDBHelper.Notification.BUTTON1_LABEL,
                    buttonAction.getCustomButtonLabel());

            int launchActionType = buttonAction.getLaunchActionType();
            if (launchActionType == FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON1_HANDLEID,
                        failureCauseInfo.getActionButton1Message().getReplyMessage().what);
            } else {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON1_HANDLEID, launchActionType);
            }
        }
        buttonAction =failureCauseInfo.getActionButton2Message();
        if(buttonAction != null) {
            contentValues.put(RoomHubDBHelper.Notification.BUTTON2_TYPE, buttonAction.getButtonType());
            contentValues.put(RoomHubDBHelper.Notification.BUTTON2_LABEL,
                    buttonAction.getCustomButtonLabel());

            int launchActionType = buttonAction.getLaunchActionType();
            if (launchActionType == FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON2_HANDLEID,
                        failureCauseInfo.getActionButton2Message().getReplyMessage().what);
            } else {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON2_HANDLEID, launchActionType);
            }
        }
        buttonAction =failureCauseInfo.getActionButton3Message();
        if(buttonAction != null) {
            contentValues.put(RoomHubDBHelper.Notification.BUTTON3_TYPE, buttonAction.getButtonType());
            contentValues.put(RoomHubDBHelper.Notification.BUTTON3_LABEL,
                    buttonAction.getCustomButtonLabel());

            int launchActionType = buttonAction.getLaunchActionType();
            if (launchActionType == FailureCauseInfo.LaunchActionType.LAUNCH_CUSTOM) {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON3_HANDLEID,
                        failureCauseInfo.getActionButton3Message().getReplyMessage().what);
            } else {
                contentValues.put(RoomHubDBHelper.Notification.BUTTON3_HANDLEID, launchActionType);
            }
        }
        mRoomHubDBHelper.notificationInsert(mRoomHubDBHelper.getWritableDatabase(), contentValues);
    }

    private void dispatchGeneralGcmMessage(Bundle bundle) {
        GCMMessageDetail gcmMessageDetail = bundle.getParcelable(GlobalDef.GCM_MESSAGE_GENERAL);
        ReminderData reminderData = new ReminderData();
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_GCM_Notice);
        reminderData.setUuid(gcmMessageDetail.getRoomHubUUID());
        reminderData.setAssetUuid(gcmMessageDetail.getUuid());
        reminderData.setMsgType(gcmMessageDetail.getMsgType());

        FailureCauseInfo failureCauseInfo = reminderData.obtainFailureCauseInfo(this);
        failureCauseInfo.setCause(gcmMessageDetail.getGcmMessage());
        FailureCauseInfo.ButtonAction okBtn = new FailureCauseInfo.ButtonAction();
        okBtn.setButtonType(FailureCauseInfo.FailureButton.BUTTON_OK);
        failureCauseInfo.setActionButton1Message(okBtn);

        FailureCauseInfo.ButtonAction checkButton = new FailureCauseInfo.ButtonAction();
        checkButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_GO_CHECK);
        Message replyMessage = mServiceHandler.obtainMessage(MESSAGE_LAUNCH_CONTROLLER_BY_GCM);
        Bundle data = new Bundle();
        data.putParcelable(GlobalDef.REMINDER_MESSAGE, (Parcelable)reminderData);
        replyMessage.setData(data);
        checkButton.setReplyMessage(replyMessage);
        failureCauseInfo.setActionButton3Message(checkButton);

        Intent intent = new Intent(GlobalDef.ACTION_REMINDER);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable)reminderData);
        mContext.sendBroadcast(intent);

    }

    private void showGcmMessage(Bundle bundle) {
        int message_type_id = bundle.getInt(GlobalDef.GCM_MESSAGE_TYPE_ID);
        String html_message = bundle.getString(GlobalDef.GCM_HTML_MESSAGE);
        String message = bundle.getString(GlobalDef.GCM_MESSAGE);
        Bundle data = new Bundle();
        data.putInt(GlobalDef.GCM_MESSAGE_TYPE_ID,message_type_id);
        data.putString(GlobalDef.GCM_HTML_MESSAGE, html_message);
        Message sendMessage = mServiceHandler.obtainMessage(MESSAGE_SAVE_GCM);
        sendMessage.setData(data);
        mServiceHandler.sendMessage(sendMessage);
        if(Utils.isRoomHubAppForeground(this))
            showDialog(message_type_id,html_message);
        else
            sendNotification(message_type_id,message);
        turnOnScreen();
    }

    private void turnOnScreen() {
        PowerManager powerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock =
                powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "GCM");
        wakeLock.acquire();

        mServiceHandler.sendMessageDelayed(mServiceHandler.obtainMessage(MESSAGE_RELEASE_GCM_WAKELOCK, wakeLock), 1000);

    }

    private void handleBPMNotice(Bundle bundle) {
        BPMDataInfo resPack = bundle.getParcelable(GlobalDef.BP_DATA_MESSAGE);

        getHealthDeviceManager().handleBPMNotification(resPack.getUuid(), resPack);
        if(!Utils.isRoomHubAppForeground(this))
            sendBPMNotification(resPack);

    }

    private void showNoCloudIdentityMessage(Bundle bundle) {
        if(Utils.isRoomHubAppForeground(this))
            showNoCloudIdentityDialog(bundle.getString(GlobalDef.ROOMHUB_UUID_MESSAGE),bundle.getString(GlobalDef.ROOMHUB_DEVNAME_MESSAGE));
    }

    private void showDialog(final ReminderData data) {
        if(mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();

        mAlertDialog = new ReminderDialog(mContext, mMessenger, data);
        mAlertDialog.show();
    }

    private void showDialog(int messageId, final String message) {
        if(mAlertDialog != null && mAlertDialog.isShowing())
            mAlertDialog.dismiss();

        mAlertDialog = new ReminderDialog(getApplication(), messageId, message);
        mAlertDialog.show();
    }

    private void showToast(ReminderData reminderData) {
        FailureCauseInfo failureCauseInfo = reminderData.getFailureCauseInfo(mContext);

        String message = reminderData.getSimpleMessage();
        if (TextUtils.isEmpty(message))
            message = failureCauseInfo.getCause();

        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void showNoCloudIdentityDialog(final String uuid,final String dev_name) {
        final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.no_cloud_identity_msg, dev_name));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        getRoomHubManager().DeleteDevice(uuid);
                        Log.d(TAG, "showNoCloudIdentityDialog DeleteDevice uuid=" + uuid);
                    }
                };
                thread.start();
            }
        });

        Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendBPMNotification(BPMDataInfo bpmDataInfo) {
        String uuid = bpmDataInfo.getUuid();
        String deviceName = bpmDataInfo.getDeviceName();
        String deviceTitle=uuid;
        String message;

//        if(!TextUtils.isEmpty(deviceName))
//            deviceTitle = deviceName;
        FriendData friendData = mAccountManager.getFriendDataByUserId(bpmDataInfo.getUserId());
        deviceTitle = friendData!=null?friendData.getNickName():bpmDataInfo.getUserAccount();

        deviceTitle="["+deviceTitle+"]";
        message = getString(R.string.bpm_notification_message,deviceTitle);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(GlobalDef.ACTION_REDIRECT_NOTIFICATION);
        intent.putExtra(GlobalDef.BP_UUID_MESSAGE,uuid);
        intent.putExtra(GlobalDef.BP_USERID_MESSAGE, bpmDataInfo.getUserId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  /*Request code*/, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.app_name)+" "+getString(R.string.app_notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ROOMHUB_NOTIFY_ID  /*ID of notification*/, notificationBuilder.build());
    }

    private void sendNotification(ReminderData data) {
        // tell app to go to controller
        data.setReminderMessageType(ReminderData.ReminderMessageType.REDIRECT_TARGET_UUID);

//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        intent.setAction(GlobalDef.ACTION_REDIRECT_NOTIFICATION);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE, (Parcelable) data);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  /*Request code*/, intent,
                PendingIntent.FLAG_ONE_SHOT);

        FailureCauseInfo failureCauseInfo = data.getFailureCauseInfo(mContext);
        String message = data.getSimpleMessage();
        if (TextUtils.isEmpty(message))
            message = failureCauseInfo.getCause();
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.app_name)+" "+getString(R.string.app_notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ROOMHUB_NOTIFY_ID  /*ID of notification*/, notificationBuilder.build());
    }

    private void sendNotification(int messageId, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra(GlobalDef.GCM_MESSAGE_TYPE_ID,messageId);
        intent.putExtra(GlobalDef.GCM_MESSAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0  /*Request code*/, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.app_name)+" "+getString(R.string.app_notification_title))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(ROOMHUB_NOTIFY_ID  /*ID of notification*/, notificationBuilder.build());
    }

    private void pollCheck() {
        synchronized (mPollLock) {
            if(mPollCheckStart == true) {
                log("pollCheck Thread has been started!");
                return;
            }
            mPollCheckThread = new PollCheckThread();
            mPollCheckThread.start();
        }
    }

    private class PollCheckThread extends Thread {

        PollCheckThread() {
            super("ROOMHUB_POLL_CHECK_THREAD");
        }

        @Override
        public void run() {
            log("poll check thread is started!");
            doCheck();

            synchronized (mPollLock) {
                mPollCheckStart = false;
            }
            log("poll check thread is finished!");
        }
    }

    private void checkConnection() {
        //boolean bStartCheck = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            mConnectRetryCount=0;
            mServiceHandler.sendEmptyMessage(MESSAGE_STARTUP_POLLCHECK);
            /*
            if(getResources().getBoolean(R.bool.config_poll_for_all_connection)) {
                bStartCheck = true;
            }
            else { // only poll for Wifi
                if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    bStartCheck = true;
                }
            }
            */
        }
        else {
            final int max_retry = getResources().getInteger(R.integer.config_poll_retry_max_count);
            if(mConnectRetryCount++ > max_retry) {
                log("checkConnection : connection hasn't been established for over "+Integer.toString(max_retry)+" secs! Give up and do nothing!!!");
                mConnectRetryCount = 0;
            }
            else
                mServiceHandler.sendEmptyMessageDelayed(MESSAGE_CONNECTION_CHECK,1000);
        }
        /*
        log("checkConnection bStartCheck="+bStartCheck);
        if(bStartCheck) {
            mServiceHandler.sendEmptyMessage(MESSAGE_STARTUP_POLLCHECK);
        }
        */
    }

    private void doCheck() {
        boolean isLogin = mAccountManager.isLogin();
        if(!isLogin) {
            if(mAccountManager.couldAutoLogin()) {
                isLogin = (mAccountManager.autoLogin() == ErrorKey.Success) ? true : false;
                log("autoLogin ret="+isLogin);
            }
        }
//        log("doCheck isLogin=" + isLogin);
//        ArrayList<RoomHubData> roomHubList = mRoomHubManager.getRoomHubDataList(isLogin);
//        log("doCheck get RoomHub list number="+roomHubList.size());

        //mRoomHubManager.RoomHubNotificationModify(null);
        //test
        /*
        Intent intent = new Intent(GlobalDef.ACTION_REMINDER);
        ReminderData data = new ReminderData();
        data.setReminderMessageType(GlobalDef.ReminderMessageType.REDIRECT_TARGET_UUID);
        data.setUuid("RoomHub-207c8fed2a30");
        data.setMessage("Poll Done!");
        data.setReminderMessageStyle(GlobalDef.REMINDER_MESSAGE_UI_TYPE_DIALOG | GlobalDef.REMINDER_MESSAGE_UI_TYPE_TOAST);
        //data.setReminderMessageStyle(GlobalDef.REMINDER_MESSAGE_UI_TYPE_TOAST);
        intent.putExtra(GlobalDef.REMINDER_MESSAGE,(Parcelable)data);
        sendBroadcast(intent);
        */
    }

    private int getAssetType(String msgType) {
        if(msgType.equalsIgnoreCase("PM25"))
            return DeviceTypeConvertApi.TYPE_ROOMHUB.PM25;
        // TODO: others

        return -1;
    }

    private void launchController(ReminderData reminderData) {
        String roomHubUUID = reminderData.getUuid();
        String assetUuid = reminderData.getAssetUuid();
        String msgType = reminderData.getMsgType();
        int type = getAssetType(msgType);

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(BaseAssetManager.KEY_UUID, roomHubUUID);
        intent.putExtra(BaseAssetManager.KEY_ASSET_UUID, assetUuid);

        switch (type) {
            case DeviceTypeConvertApi.TYPE_ROOMHUB.PM25:
                intent.setClass(this, PMActivity.class);
                break;
            default:
                return;
        }

        startActivity(intent);
    }

    private void launchWifiSetting() {
        Intent intent = new Intent(mContext,SetupWifiActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void launchMobileSetting() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.android.phone",
                "com.android.phone.MobileNetworkSettings"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
        mContext = getApplicationContext();

        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        initManagers();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand mIsStarup=" + mIsStarup);
        if(!mIsStarup) {
            startup();
        }

        int restart = -1;
        if(intent != null) {
            restart = intent.getIntExtra(ACTION_RESTART, -1);
        }

        IntentFilter wake_up_filter = new IntentFilter();
        wake_up_filter.addAction(Intent.ACTION_SCREEN_ON);
        wake_up_filter.addAction(GlobalDef.ACTION_NETWORK_STATE_CHAGNED);
        registerReceiver(mWakeUpReceiver, wake_up_filter);

        final boolean notificationOn = Utils.isNotificationOn(mContext);
        log("onStartCommand isNotificationOn=" + notificationOn);
        log("onStartCommand restart=" + restart);
        if(notificationOn) {
            final IntentFilter filter = new IntentFilter(GlobalDef.ACTION_REMINDER);
            filter.addAction(GlobalDef.ACTION_GCM_MESSAGE);
            filter.addAction(GlobalDef.ACTION_NO_CLOUD_IDENTIFY);
            filter.addAction(GlobalDef.ACTION_BPM_NOTICE);
            registerReceiver(mReminderReceiver, filter);

            // 20160419 remove alarm
            /*
            final IntentFilter pollFilter = new IntentFilter(ACTION_POLL_ALARM);
            pollFilter.addAction(ACTION_SCHEDULE_ALARM);
            registerReceiver(mPollReceiver, pollFilter);
            */

            IntentFilter onboardingFilter = new IntentFilter();
            onboardingFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            onboardingFilter.addAction(MiddlewareApi.ONBOARDING_START_ACTION);
            onboardingFilter.addAction(MiddlewareApi.ONBOARDING_STOP_ACTION);
            registerReceiver(mOnBoardingReceiver, onboardingFilter);

            sendBroadcast(new Intent(ACTION_SCHEDULE_ALARM));
        }

        if(restart >= 0) {
            if(restart==RESTART_ENABLE)
                return Service.START_STICKY;
            else
                return Service.START_NOT_STICKY;
        }
        else {
            if(notificationOn)
                return Service.START_STICKY;
            else
                return Service.START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        log("onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void networkAvailable(int networkType) {
        log("networkAvailable");
        ReminderData reminderData = new ReminderData();
        reminderData.setSenderId(BaseManager.NETWORK_MONITOR);
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Phone_001);
        reminderData.setSimpleMessage(
                mContext.getString(R.string.fail_msg_network_available, getNetworkTypeName(networkType))
        );
        Utils.sendReminderMessage(mContext, reminderData);
    }

    @Override
    public void networkUnavailable(int networkType) {
        log("networkUnavailable");

        ReminderData reminderData = new ReminderData();
        reminderData.setSenderId(BaseManager.NETWORK_MONITOR);
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Phone_001);
        reminderData.setSimpleMessage(
                mContext.getString(R.string.fail_msg_network_unavailable, getNetworkTypeName(networkType))
        );

        Utils.sendReminderMessage(mContext, reminderData);
    }

    @Override
    public void networkTypeChanged(int origNetworkType, int currentNetworkType) {
        log("networkTypeChanged");

        ReminderData reminderData = new ReminderData();
        reminderData.setSenderId(BaseManager.NETWORK_MONITOR);
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Phone_002);
        reminderData.setSimpleMessage(
                mContext.getString(R.string.fail_msg_phone_002
                        , getNetworkTypeName(origNetworkType)
                        , getNetworkTypeName(currentNetworkType))
        );

        Utils.sendReminderMessage(mContext, reminderData);
    }

    public synchronized RoomHubManager getRoomHubManager() {
        return mRoomHubManager;
    }

    public synchronized AccountManager getAccountManager() {
        return mAccountManager;
    }

    public synchronized OnBoardingManager getOnBoardingManager() {
        return mOnBoardingManager;
    }

    public synchronized RoomHubDBHelper getRoomHubDBHelper() {
        return mRoomHubDBHelper;
    }
    //public synchronized MicroLocationManager getMicroLocationManager() { return mMicroLocationManager;  }

    public synchronized IRController getIRController() { return mIRController;  }

    public synchronized OTAManager getOTAManager() { return mOTAManager; }

    public synchronized ACNoticeManager getACNoticeManager() {
        return mACNoticeManager;
    }

    /*
    public synchronized ACManager getACManager() {
        return mACManager;
    }

    public synchronized FANManager getFANManager() {
        return mFANManager;
    }

    public synchronized AirPurifierManager getAirPurifierManager() {
        return mAirPurifierManager;
    }

    public synchronized PMManager getPMManager() {
        return mPMManager;
    }

    public synchronized BulbManager getBulbManager(){
        return mBulbManager;
    }

    public synchronized TVManager getTVManager(){
        return mTVManager;
    }
*/
    public synchronized HealthDeviceManager getHealthDeviceManager() { return mHealthDeviceManager; }

    public synchronized BLEPairController getBLEController() { return mBLEController;  }

    public VersionCheckUpdateResPack checkAppVersion() {
        return mMiddlewareApi.AppCheckVersion();
    }

    private void initManagers() {
        mMiddlewareApi = MiddlewareApi.getInstance(this, new MiddlewareApi.ApiConfig(MQTT_SERVER, CLOUD_SERVER));

        if(mIRController == null) {
            HandlerThread irControllerThread = new HandlerThread("IRController");
            irControllerThread.start();
            mIRController = new IRController(irControllerThread.getLooper(), getApplication(), mMiddlewareApi);
        }

        if(mBLEController == null) {
            HandlerThread bleControllerThread = new HandlerThread("BLEPairController");
            bleControllerThread.start();
            mBLEController = new BLEPairController(bleControllerThread.getLooper(), this, mMiddlewareApi);
        }

        if(mAccountManager == null) {
            mAccountManager = new AccountManager(this, mMiddlewareApi);
            // do initilize or nothing
        }

        if(mRoomHubDBHelper == null){
            mRoomHubDBHelper = new RoomHubDBHelper(this);
            mRoomHubDBHelper.getWritableDatabase();
        }

        if(mOnBoardingManager == null) {
            mOnBoardingManager = new OnBoardingManager(this, mMiddlewareApi);
        }
        /*
        if (mMicroLocationManager == null){
            //startService(new Intent(getApplicationContext(),MicroLocationManager.class));
            mMicroLocationManager = new MicroLocationManager(this);
        }
        */
        if(mACNoticeManager == null){
            mACNoticeManager = new ACNoticeManager(this, mMiddlewareApi);
        }

        if(mRoomHubManager == null) {
            mRoomHubManager = new RoomHubManager(this, mMiddlewareApi);
            mRoomHubManager.registerAssetDeviceManager(new ACManager(this,mMiddlewareApi));
            mRoomHubManager.registerAssetDeviceManager(new FANManager(this,mMiddlewareApi));
            if(mContext.getResources().getBoolean(R.bool.config_pm25))
                mRoomHubManager.registerAssetDeviceManager(new PMManager(this,mMiddlewareApi));

            if(mContext.getResources().getBoolean(R.bool.config_air_purifier))
                mRoomHubManager.registerAssetDeviceManager(new AirPurifierManager(this,mMiddlewareApi));

            if(mContext.getResources().getBoolean(R.bool.config_tv))
                mRoomHubManager.registerAssetDeviceManager(new TVManager(this,mMiddlewareApi));

            if(mContext.getResources().getBoolean(R.bool.config_bulb))
                mRoomHubManager.registerAssetDeviceManager(new BulbManager(this,mMiddlewareApi));
            // do initilize or nothing
        }

        if(mNetworkMinitor == null) {
            HandlerThread networkMonitorThread = new HandlerThread("NetworkMonitor");
            networkMonitorThread.start();
            mNetworkMinitor = new NetworkMonitor(networkMonitorThread.getLooper(), this, mMiddlewareApi);
        }

        if(mOTAManager == null) {
            mOTAManager = new OTAManager(this, mMiddlewareApi);
        }

        if(mHealthDeviceManager == null) {
            mHealthDeviceManager = new HealthDeviceManager(this, mMiddlewareApi);
            mHealthDeviceManager.registerHealthDeviceManager(new BPMManager(this));
        }

        mGcmController = new GcmController(this, mMiddlewareApi);

    }

    private void startup() {
        mIsStarup = true;

        mAccountManager.startup();
        mOnBoardingManager.startup();
        //mMicroLocationManager.startup();
        mACNoticeManager.startup();
        mRoomHubManager.startup();
		mOTAManager.startup();		
        mHealthDeviceManager.startup();

        mAccountManager.registerForLoginState(this);

        //mServiceHandler.sendEmptyMessageDelayed(MESSAGE_DELAY_STARTUP_IBEACON, 5000);

        registerReceiver(mNotificationChangedReceiver,
                new IntentFilter(GlobalDef.ACTION_SETTINGS_NOTIFICATION_CHANGED));

        mNetworkMinitor.addListener(this);
        // SetRepeating: Beginning in API 19, the trigger time passed to this method is treated as inexact
        //registerPollAlarm();
    }

    private void registerPollAlarm() {
        if(mAlarmIntent != null)
            mAlarmIntent.cancel();

        Intent intent = new Intent(ACTION_POLL_ALARM);
        intent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        long periodMs = getResources().getInteger(R.integer.config_poll_alarm_interval);
        long firstTime = SystemClock.elapsedRealtime() + periodMs;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, firstTime,
                periodMs, mAlarmIntent);
        /*
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, currentRealtime,
                getResources().getInteger(R.integer.config_poll_alarm_interval), mAlarmIntent);
                */

    }

    private String getNetworkTypeName(int networkType) {
        switch(networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return mContext.getString(R.string.network_type_mobile);
            case ConnectivityManager.TYPE_WIFI:
                return mContext.getString(R.string.network_type_wifi);
        }
        return "";
    }

    private void showPromptUserConnectionWifiDialog(final Message onComplete) {
        final Dialog dialog = new Dialog(mContext,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(mContext.getString(R.string.wifi_prompt_connect));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(onComplete == null) {
                    Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                    launchIntent.setComponent(new ComponentName("com.android.settings",
                            "com.android.settings.wifi.WifiSettings"));
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(launchIntent);
                }
                else {
                    Message replyMessage = Message.obtain(onComplete);
                    replyMessage.obj = true;
                    replyMessage.sendToTarget();
                }
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(onComplete != null) {
                    Message replyMessage = Message.obtain(onComplete);
                    replyMessage.obj = false;
                    replyMessage.sendToTarget();
                }
            }
        });

        dialog.setCancelable(false);
        dialog.show();
        mPromptUserConnectionWifiDialog = dialog;
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }
}
