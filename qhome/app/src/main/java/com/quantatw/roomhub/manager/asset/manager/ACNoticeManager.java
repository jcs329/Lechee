package com.quantatw.roomhub.manager.asset.manager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.FailureData;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.ui.RoomHubApplication;
import com.quantatw.roomhub.ui.RoomHubControllerFlipper;
import com.quantatw.roomhub.ui.RoomHubMainPage;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.RoomHubFailureCause;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;

import java.util.HashMap;

/**
 * Created by cherry on 2016/1/15.
 */
public class ACNoticeManager extends BaseManager{
    private final String TAG=ACNoticeManager.class.getSimpleName();
    private boolean DEBUG=true;
    private MiddlewareApi mApi;

    public static final String KEY_ROOMHUB_DATA= "roomhub_data";
    public static final String KEY_AC_DATA= "ac_data";
    public static final String KEY_FAIL_RECOVER= "fail_recover";

    /*
    public static final int MESSAGE_NOTICE                  =100;
    public static final int MESSAGE_NOTICE_CONFLICT         =101;
    */
    public static final int MESSAGE_NOTICE_DEVICE_LOST      =102;
    public static final int MESSAGE_NOTICE_DEVICE_OFFLINE   =103;
    public static final int MESSAGE_FAIL_RECOVER            =104;
    public static final int MESSAGE_FAIL_RECOVER_TEMP_TOO_HIGH =105;

    private static final int FAILURE_REPLAY_OK              = 300;
    private static final int FAILURE_REPLAY_NOTIFY_LATER    = 301;
    private static final int FAILURE_REPLAY_CHECK           = 302;
    private static final int FAILURE_REPLAY_NO_NOTIFY       = 303;
    private static final int FAILURE_DO_RESET = 304;
    private static final int FAILURE_DO_ACTIVATE = 305;

    private Context mContext;
    private AccountManager mAccountMgr;

    private HandlerThread mBackgroundThread;
    private BackgroundHandler mBackgroundHandler;

    private HashMap<String,HashMap<Integer,FailureData>> mFailureList=new HashMap<String,HashMap<Integer,FailureData>>();

    private final class FailureHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            log("FailureHandler message what="+msg.what);
            switch (msg.what) {
                case FAILURE_REPLAY_OK:
                    UpdateBtnCnt(FAILURE_REPLAY_OK,msg.arg1,(String)msg.obj);
                    break;
                case FAILURE_REPLAY_NOTIFY_LATER:
                    UpdateBtnCnt(FAILURE_REPLAY_NOTIFY_LATER,msg.arg1,(String)msg.obj);
                    break;
                case FAILURE_REPLAY_CHECK:
                    UpdateBtnCnt(FAILURE_REPLAY_CHECK, msg.arg1, (String) msg.obj);
                    launchController((String) msg.obj,msg.getData());
                    break;
                case FAILURE_REPLAY_NO_NOTIFY:
                    UpdateBtnCnt(FAILURE_REPLAY_NO_NOTIFY, msg.arg1, (String) msg.obj);
                    break;
                case FAILURE_DO_RESET:
                    break;
                case FAILURE_DO_ACTIVATE:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private final class BackgroundHandler extends Handler {
        public BackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("BackgroundHandler message what="+msg.what);
            switch (msg.what) {
                /*
                case MESSAGE_NOTICE:
                    NoticeProcess((ACData) msg.obj);
                    break;
                case MESSAGE_NOTICE_CONFLICT:
                    NotificationControlConflict((ACData) msg.obj);
                    break;
                */
                case MESSAGE_NOTICE_DEVICE_LOST:
                    NoticeDeviceLost((RoomHubData) msg.obj);
                    break;
                case MESSAGE_NOTICE_DEVICE_OFFLINE:
                    NoticeOffLine((RoomHubData) msg.obj);
                    break;
                case MESSAGE_FAIL_RECOVER:
                    FailRecoverProcess((ACData) msg.getData().getParcelable(KEY_AC_DATA), (AcFailRecoverResPack) msg.getData().getParcelable(KEY_FAIL_RECOVER));
                    break;
                case MESSAGE_FAIL_RECOVER_TEMP_TOO_HIGH:
                    FailRecoverTempTooHigh((RoomHubData) msg.getData().getParcelable(KEY_ROOMHUB_DATA), (AcFailRecoverResPack) msg.getData().getParcelable(KEY_FAIL_RECOVER));
                    break;
            }
        }
    }

    private void sendNoticeMessage(ACData data,FailureData failure_data, AcFailRecoverResPack acFailRecoverResPack) {
        String uuid, name, roomHubUuid="";

        if(data != null) {
            uuid = data.getAssetUuid();
            name = data.getRoomHubData().getName();
            roomHubUuid = data.getRoomHubUuid();
        }
        else {
            uuid = acFailRecoverResPack.gethubUUID();
            name = acFailRecoverResPack.getRoomHubDeviceName();
        }

        if(TextUtils.isEmpty(name))
            name = uuid;

        updateFailureData(uuid, failure_data);
        int failure_id=failure_data.getFailureId();
        ReminderData reminderData = new ReminderData();
        reminderData.setUuid(uuid);
        reminderData.setMessageId(failure_data.getFailureId());
        FailureCauseInfo failureCauseInfo = reminderData.obtainFailureCauseInfo(mContext);

        log("sendNoticeMessage failure_id=" + failure_id + ",extra_index=" + failureCauseInfo.getIndex());

        switch (failure_id) {
            case RoomHubFailureCause.ID.H60Failure_Control_002:
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_control_002), name));
                CreateButtonAction(failureCauseInfo, roomHubUuid, uuid, failure_id, true, true, true, false);
                break;
            case RoomHubFailureCause.ID.H60Failure_Control_003:
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_control_003), name));
                CreateButtonAction(failureCauseInfo, roomHubUuid, uuid, failure_id, true, true, true, false);
                break;
            case RoomHubFailureCause.ID.H60Failure_Control_007:
                reminderData.setSimpleMessage(String.format(mContext.getResources().getString(R.string.fail_msg_control_007), name));
                break;
            case RoomHubFailureCause.ID.H60Failure_Control_009:
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_control_009), name));
                CreateButtonAction(failureCauseInfo, roomHubUuid, uuid, failure_id, true, true, true, false);
                break;
            case RoomHubFailureCause.ID.H60Failure_Control_010:
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_control_010), name));
                CreateButtonAction(failureCauseInfo, roomHubUuid, uuid, failure_id, true, true, true, false);
                break;
            case RoomHubFailureCause.ID.H60Failure_Device_001: {
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_device_001), name));

                FailureCauseInfo.ButtonAction okButton = new FailureCauseInfo.ButtonAction();
                okButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_OK);
                okButton.setLaunchActionType(FailureCauseInfo.LaunchActionType.DO_NOTHING);
                failureCauseInfo.setActionButton1Message(okButton);

                FailureCauseInfo.ButtonAction resetButton = new FailureCauseInfo.ButtonAction();
                resetButton.setCustomButtonLabel(mContext.getString(R.string.fail_action_reset));
                resetButton.setReplyMessage(Message.obtain(mFailureHandler, FAILURE_DO_RESET, uuid));
                failureCauseInfo.setActionButton2Message(resetButton);

                FailureCauseInfo.ButtonAction activeButton = new FailureCauseInfo.ButtonAction();
                activeButton.setCustomButtonLabel(mContext.getString(R.string.fail_action_activate));
                activeButton.setReplyMessage(Message.obtain(mFailureHandler, FAILURE_DO_ACTIVATE, uuid));
                failureCauseInfo.setActionButton3Message(activeButton);
            }
            break;
        }
        sendReminderMessage(reminderData);
    }

    private void sendDeviceNoticeMessage(RoomHubData data,FailureData failure_data) {
        sendDeviceNoticeMessage(data,failure_data,null);
    }

    private void sendDeviceNoticeMessage(RoomHubData data,FailureData failure_data, AcFailRecoverResPack acFailRecoverResPack) {
        String uuid, name;
        if(data != null) {
            uuid=data.getUuid();
            name=data.getName();
        }
        else {
            uuid=acFailRecoverResPack.gethubUUID();
            name=acFailRecoverResPack.getRoomHubDeviceName();
        }

        if(TextUtils.isEmpty(name))
            name = uuid;

        updateFailureData(uuid, failure_data);

        int failure_id=failure_data.getFailureId();
        ReminderData reminderData = new ReminderData();
        reminderData.setUuid(uuid);
        reminderData.setMessageId(failure_data.getFailureId());
        FailureCauseInfo failureCauseInfo = reminderData.obtainFailureCauseInfo(mContext);

        log("sendNoticeMessage failure_id=" + failure_id + ",extra_index=" + failureCauseInfo.getIndex());

        switch (failure_id) {
            case RoomHubFailureCause.ID.H60Failure_Temp_001:
                failureCauseInfo.setCause(String.format(mContext.getResources().getString(R.string.fail_msg_temp_001), name));
                CreateButtonAction(failureCauseInfo,uuid,failure_id,true,true,false,false);
                break;
            case RoomHubFailureCause.ID.H60Failure_Net_001:
                reminderData.setSimpleMessage(String.format(mContext.getResources().getString(R.string.fail_msg_net_001), name));
                break;
            case RoomHubFailureCause.ID.H60Failure_Net_002:
                reminderData.setSimpleMessage(String.format(mContext.getResources().getString(R.string.fail_msg_net_002), name));
                break;
        }
        sendReminderMessage(reminderData);
    }

    private void CreateButtonAction(FailureCauseInfo failureCauseInfo,String uuid,int failure_id,boolean ok_btn,boolean later,boolean check_btn,boolean no_notify_btn) {
        CreateButtonAction(failureCauseInfo,"",uuid,failure_id,ok_btn,later,check_btn,no_notify_btn);
    }

    private void CreateButtonAction(FailureCauseInfo failureCauseInfo,String uuid,String assetUuid,int failure_id,boolean ok_btn,boolean later,boolean check_btn,boolean no_notify_btn){
        Message msg=new Message();
        msg.setTarget(mFailureHandler);
        msg.arg1=failure_id;
        msg.obj=(Object)assetUuid;
        Bundle data = new Bundle();
        data.putString(RoomHubManager.KEY_UUID,uuid);
        msg.setData(data);

        if(ok_btn == true){
            FailureCauseInfo.ButtonAction okButton = new FailureCauseInfo.ButtonAction();
            okButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_OK);
            msg.what=FAILURE_REPLAY_OK;

            Message sendMessage = Message.obtain(msg);
            Log.d(TAG,"Reminder CreateButtonAction okButton sendMessage="+sendMessage);
            okButton.setReplyMessage(sendMessage);
//            failureCauseInfo.setActionButton1Message(okButton);
            setActionButton(failureCauseInfo, okButton);
        }

        if(later == true){
            FailureCauseInfo.ButtonAction notifyLaterButton = new FailureCauseInfo.ButtonAction();
            msg.what=FAILURE_REPLAY_NOTIFY_LATER;
            notifyLaterButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_NOTIFY_LATER);
            Message sendMessage = Message.obtain(msg);
            Log.d(TAG, "Reminder CreateButtonAction notifyLaterButton sendMessage=" + sendMessage);
            notifyLaterButton.setReplyMessage(sendMessage);
//            failureCauseInfo.setActionButton2Message(notifyLaterButton);
            setActionButton(failureCauseInfo, notifyLaterButton);
        }

        if(check_btn == true){
            FailureCauseInfo.ButtonAction checkButton = new FailureCauseInfo.ButtonAction();
            msg.what=FAILURE_REPLAY_CHECK;
            checkButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_GO_CHECK);
            Message sendMessage = Message.obtain(msg);
            Log.d(TAG, "Reminder CreateButtonAction checkButton sendMessage=" + sendMessage);
            checkButton.setReplyMessage(sendMessage);
//            failureCauseInfo.setActionButton3Message(checkButton);
            setActionButton(failureCauseInfo, checkButton);
        }
/*
        if(no_notify_btn == true){
            FailureCauseInfo.ButtonAction NoNotifyButton = new FailureCauseInfo.ButtonAction();
            msg.what=FAILURE_REPLAY_NO_NOTIFY;
            NoNotifyButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_CUSTOM);
            NoNotifyButton.setReplyMessage(Message.obtain(msg));
            setActionButton(failureCauseInfo, NoNotifyButton);
        }
*/
    }

    private void setActionButton(FailureCauseInfo failureCauseInfo,FailureCauseInfo.ButtonAction btn_action){
        if(failureCauseInfo.getActionButton1Message() == null)
            failureCauseInfo.setActionButton1Message(btn_action);
        else if(failureCauseInfo.getActionButton2Message() == null)
            failureCauseInfo.setActionButton2Message(btn_action);
        else if(failureCauseInfo.getActionButton3Message() == null)
            failureCauseInfo.setActionButton3Message(btn_action);
    }

    public ACNoticeManager(Context context, MiddlewareApi api) {
        super(context,BaseManager.ACNOTICE_MANAGER);
        mFailureHandler = new FailureHandler();
        mApi = api;
        mContext = context;

        mBackgroundThread=new HandlerThread("ACNoticeManager");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());
    }

    @Override
    public void startup() {
        mAccountMgr=((RoomHubService) mContext).getAccountManager();
    }

    @Override
    public void terminate() {

    }

    public void sendNoticeProcess(ACData ac_data,int message_id){
        if(ac_data.getRoomHubData().IsUpgrade())
            return;

        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(message_id,(Object)ac_data));
    }

    public void sendRoomHubNoticeProcess(RoomHubData roomhub_data,int message_id){
        if(roomhub_data.IsUpgrade())
            return;

        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(message_id,(Object)roomhub_data));
    }

    public void sendFailRecover(ACData ac_data,AcFailRecoverResPack failRecoverResPack){
        Message msg=new Message();
        msg.what=MESSAGE_FAIL_RECOVER;
        Bundle bundle=new Bundle();
        bundle.putParcelable(KEY_AC_DATA, ac_data);
        bundle.putParcelable(KEY_FAIL_RECOVER, failRecoverResPack);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    public void sendFailRecover(RoomHubData roomhub_data,AcFailRecoverResPack failRecoverResPack){
        Message msg=new Message();
        msg.what=MESSAGE_FAIL_RECOVER_TEMP_TOO_HIGH;
        Bundle bundle=new Bundle();
        bundle.putParcelable(KEY_ROOMHUB_DATA, roomhub_data);
        bundle.putParcelable(KEY_FAIL_RECOVER, failRecoverResPack);
        msg.setData(bundle);
        mBackgroundHandler.sendMessage(msg);
    }

    private void UpdateBtnCnt(int type,int failure_id,String uuid){

        FailureData failure_data=getFailureDataById(uuid, failure_id);
        if(failure_data != null){
            int cnt;
            switch(type){
                case FAILURE_REPLAY_OK:
                    cnt=failure_data.getOKBtnCnt();
                    failure_data.setOKBtnCnt(cnt++);
                    break;
                case FAILURE_REPLAY_NOTIFY_LATER:
                    cnt=failure_data.getLaterBtnCnt();
                    failure_data.setLaterBtnCnt(cnt++);
                    break;
                case FAILURE_REPLAY_CHECK:
                    cnt=failure_data.getCheckBtnCnt();
                    failure_data.setCheckBtnCnt(cnt++);
                    break;
                case FAILURE_REPLAY_NO_NOTIFY:
                    cnt=failure_data.getNoNotifyBtnCnt();
                    failure_data.setNoNotifyBtnCnt(cnt++);
                    break;
            }
        }

    }

    private void FailRecoverProcess(ACData data,AcFailRecoverResPack res_pack){
        int failure_id = 0;

        if(data != null && data.getNoticeSetting().getSwitchOnOff() == ACDef.POWER_OFF)
            return;

        switch (res_pack.getReason()){
            case ACDef.AC_FAIL_RECOVER_TURN_ON_FAIL:
                failure_id=RoomHubFailureCause.ID.H60Failure_Control_002;
                break;
            case ACDef.AC_FAIL_RECOVER_TURN_OFF_FAIL:
                failure_id=RoomHubFailureCause.ID.H60Failure_Control_003;
                break;
            case ACDef.AC_FAIL_RECOVER_REPEAT_CONTROL:
                failure_id=RoomHubFailureCause.ID.H60Failure_Control_007;
                break;
        }
        if(data != null) {
            if (isNotifyPermission(data.getRoomHubData(), res_pack.getUserId(), failure_id) != null) {
                FailureData failure_data = getFailureDataById(res_pack.getUuid(), failure_id);

                log("FailRecoverProcess reason=" + res_pack.getReason());

                if (failure_data == null) {
                    failure_data = new FailureData();
                }

                failure_data.setFailureId(failure_id);
                failure_data.setLastSendTime(System.currentTimeMillis());

                sendNoticeMessage(data, failure_data, res_pack);
            }
        }
        else {
            FailureData failure_data = getFailureDataById(res_pack.getUuid(), failure_id);

            log("FailRecoverProcess reason=" + res_pack.getReason());

            if (failure_data == null) {
                failure_data = new FailureData();
            }

            failure_data.setFailureId(failure_id);
            failure_data.setLastSendTime(System.currentTimeMillis());

            sendNoticeMessage(null, failure_data, res_pack);
        }
    }

    private void FailRecoverTempTooHigh(RoomHubData data,AcFailRecoverResPack res_pack){
        int failure_id = RoomHubFailureCause.ID.H60Failure_Temp_001;

        if(res_pack.getReason() == ACDef.AC_FAIL_RECOVER_TEMP_TOO_HIGH){
            log("FailRecoverTempTooHigh reason=" + res_pack.getReason());

            if(data != null) {
                if (isNotifyPermission(data, res_pack.getUserId(), failure_id) != null) {
                    FailureData failure_data = getFailureDataById(res_pack.getUuid(), failure_id);

                    if (failure_data == null) {
                        failure_data = new FailureData();
                    }

                    failure_data.setFailureId(failure_id);
                    failure_data.setLastSendTime(System.currentTimeMillis());

                    sendDeviceNoticeMessage(data, failure_data,res_pack);
                }
            }
            else {
                FailureData failure_data = getFailureDataById(res_pack.getUuid(), failure_id);

                if (failure_data == null) {
                    failure_data = new FailureData();
                }

                failure_data.setFailureId(failure_id);
                failure_data.setLastSendTime(System.currentTimeMillis());

                sendDeviceNoticeMessage(null, failure_data,res_pack);
            }
        }
    }

    /*
    private void NoticeProcess(ACData ac_data){
        NotificationFire(ac_data);
        NotificationControlInvalid(ac_data);
        //NotificationMachineHealth(ac_data);
    }
    */
    private FailureCauseInfo isNotifyPermission(RoomHubData data,String user_id,int failure_id){
        FailureCauseInfo failureCauseInfo =RoomHubFailureCause.getInstance(mContext).obtainFailCause(failure_id);
        int notice_role=failureCauseInfo.getNoticeRole();

        if(notice_role == FailureCauseInfo.Role.ALL){
            return failureCauseInfo;
        }else if((notice_role & FailureCauseInfo.Role.OWNER) == FailureCauseInfo.Role.OWNER){
            if(data.IsOwner())
                return failureCauseInfo;
        }else if((notice_role & FailureCauseInfo.Role.USER) == FailureCauseInfo.Role.USER) {
            if(data.IsFriend())
                return failureCauseInfo;
        }else if((notice_role & FailureCauseInfo.Role.OPERATOR) == FailureCauseInfo.Role.OPERATOR) {
            if(mAccountMgr.getUserId().equalsIgnoreCase(user_id))
                return failureCauseInfo;
        }
        return null;
    }
/*
    private boolean NotificationControlConflict(ACData data){
        NoticeSetting notice_setting=data.getNoticeSetting();

        if(notice_setting.getSwitchOnOff() == ACDef.POWER_OFF)
            return false;

        RoomHubInterface.getDeviceInfo_return_values_iiiiiiiss dev_info=data.getDeviceInfo();
        if(dev_info == null)
            return false;

        int failure_id= RoomHubFailureCause.ID.H60Failure_Control_007;
        NoticeStatus notice_status=data.getNoticeStatus();

        long time_delta = System.currentTimeMillis() - notice_status.getSendCmdTime();
        int conflict_time = mContext.getResources().getInteger(R.integer.config_notification_conflict_time);

        log("NotificationControlConflict time_delta=" + time_delta + " conflict_time=" + conflict_time);
        if (time_delta < conflict_time) {
            log("NotificationControlConflict dev_info.userId=" + dev_info.userId + " login user_id=" + mAccountMgr.getUserId());
            if (!dev_info.userId.equals(mAccountMgr.getUserId())) {
                FailureData failure_data = getFailureDataById(data.getRoomHub().getUuid(),failure_id);

                if (failure_data == null) {
                    failure_data=new FailureData();
                }

                failure_data.setFailureId(failure_id);
                failure_data.setLastSendTime(System.currentTimeMillis());
                failure_data.setCheckTime(notice_setting.getNoticeTime());

                sendNoticeMessage(data,failure_data);

                return true;
            }
        }

        return false;
    }
*/
    /*
    Notify fire
    1.Notify once
    2.Temperature is raised further notice
    */
    /*
    private boolean NotificationFire(ACData data){
        NoticeSetting notice_setting=data.getNoticeSetting();

        if(notice_setting.getSwitchOnOff() == ACDef.POWER_OFF)
            return false;

        int fire_temp=mContext.getResources().getInteger(R.integer.config_notification_fire_temp);
        int failure_id= RoomHubFailureCause.ID.H60Failure_Temp_001;
        boolean is_notify=false;
        NoticeStatus notice_status=data.getNoticeStatus();
        int now_temp=notice_status.getNowTemp();

        if(isNotifyPermission(data.getRoomHub(), notice_status.getUserId(), failure_id) != null) {
            FailureData failure_data = getFailureDataById(data.getRoomHub().getUuid(), failure_id);

            log("NotificationFire now_temp=" + now_temp + " fire_temp" + fire_temp);
            if (now_temp >= fire_temp) {
                if(failure_data == null){
                    log("NotificationFire failure_data is null");
                    is_notify=true;
                    failure_data = new FailureData();
                    failure_data.setFailureId(failure_id);
                }else{
                    int last_temp=failure_data.getLastTemp();
                    log("NotificationFire failure_data isn't null last_temp=" + last_temp);
                    if(last_temp < now_temp) {
                        is_notify=true;
                    }
                }
            }else{
                if(failure_data != null)
                    failure_data.setLastTemp(0);
            }

            if (is_notify) {
                failure_data.setLastSendTime(System.currentTimeMillis());
                failure_data.setLastTemp(now_temp);
                failure_data.setCheckTime(notice_setting.getNoticeTime());

                sendDeviceNoticeMessage(data.getRoomHub(),failure_data);
                return true;
            }
        }
        return false;
    }

    private boolean NotificationControlInvalid(ACData data){
        int failure_id= RoomHubFailureCause.ID.H60Failure_Control_002;
        NoticeStatus notice_status=data.getNoticeStatus();

        NoticeSetting notice_setting=data.getNoticeSetting();
        if(notice_setting.getSwitchOnOff() == ACDef.POWER_OFF)
            return false;

        if(isNotifyPermission(data.getRoomHub(), notice_status.getUserId(), failure_id) != null) {
            String uuid=data.getRoomHub().getUuid();

            int time=notice_setting.getNoticeTime();
            int delta=notice_setting.getNoticeDelta();
            int now_temp=notice_status.getNowTemp();
            int origin_temp=notice_status.getOriginTemp();
            int fun_mode =notice_status.getFunMode();
            int time_interval=(int)((System.currentTimeMillis() - notice_status.getTimeStamp()) / (30*1000));

            if( time_interval == (time / 30)) {
                if ((fun_mode == ACDef.FUN_MODE_COOL) || (fun_mode == ACDef.FUN_MODE_HEAT)) {
                    if (notice_status.getLastAction() == ACDef.ACONOFF_LAST_ACTION_ON) {
                        if (fun_mode == ACDef.FUN_MODE_COOL){
                            if(!(now_temp < (origin_temp - delta))){
                                log("NotificationControlInvalid FUN_MODE_COOL H60Failure_Control_002");
                                FailureData failure_data = getFailureDataById(uuid,RoomHubFailureCause.ID.H60Failure_Control_002);
                                if(failure_data == null){
                                    failure_data = new FailureData();
                                    failure_data.setFailureId(RoomHubFailureCause.ID.H60Failure_Control_002);
                                }
                                failure_data.setLastSendTime(System.currentTimeMillis());
                                failure_data.setCheckTime(notice_setting.getNoticeTime());

                                sendNoticeMessage(data, failure_data);

                                return true;
                            }
                        }else{
                            if(!(now_temp > (origin_temp + delta))){
                                log("NotificationControlInvalid FUN_MODE_HEAT H60Failure_Control_002");
                                FailureData failure_data = getFailureDataById(uuid,RoomHubFailureCause.ID.H60Failure_Control_002);
                                if(failure_data == null){
                                    failure_data = new FailureData();
                                    failure_data.setFailureId(RoomHubFailureCause.ID.H60Failure_Control_002);
                                }
                                failure_data.setLastSendTime(System.currentTimeMillis());
                                failure_data.setCheckTime(notice_setting.getNoticeTime());

                                sendNoticeMessage(data,failure_data);

                                return true;
                            }
                        }
                    }
                }
            } else if(time_interval == ((time * 2) / 30)) {
                if ((fun_mode == ACDef.FUN_MODE_COOL) || (fun_mode == ACDef.FUN_MODE_HEAT)) {
                    int delta_temp;
                    if (notice_status.getLastAction() == ACDef.ACONOFF_LAST_ACTION_OFF) {
                        if (now_temp > origin_temp) {
                            delta_temp = now_temp - origin_temp;
                        } else {
                            delta_temp = origin_temp - now_temp;
                        }
                        int normal_delta=mContext.getResources().getInteger(R.integer.config_notification_normal_temp_delta);

                        if (!(delta_temp > normal_delta)) {
                            log("NotificationControlInvalid H60Failure_Control_003");
                            FailureData failure_data = getFailureDataById(uuid,RoomHubFailureCause.ID.H60Failure_Control_003);
                            if(failure_data == null){
                                failure_data = new FailureData();
                                failure_data.setFailureId(RoomHubFailureCause.ID.H60Failure_Control_003);
                            }
                            failure_data.setLastSendTime(System.currentTimeMillis());
                            failure_data.setCheckTime(notice_setting.getNoticeTime());

                            sendNoticeMessage(data, failure_data);

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean NotificationMachineHealth(ACData data){

        int failure_id= RoomHubFailureCause.ID.H60Failure_Control_009;
        NoticeStatus notice_status=data.getNoticeStatus();

        if(isNotifyPermission(data.getRoomHub(), notice_status.getUserId(), failure_id) != null) {
            NoticeSetting notice_setting=data.getNoticeSetting();
            int time=30*60;
            int delta=notice_setting.getNoticeDelta();

            int target_temp=notice_status.getTargetTemp();
            int now_temp=notice_status.getNowTemp();
            int fun_mode = notice_status.getFunMode();
            int time_interval=(int)((System.currentTimeMillis() - notice_status.getTimeStamp()) / (30*1000));

            if (time_interval == (time / 30)) {
                int delta_temp;
                if((fun_mode== ACDef.FUN_MODE_COOL) || (fun_mode== ACDef.FUN_MODE_HEAT)) {
                    if (notice_status.getLastAction() == ACDef.ACONOFF_LAST_ACTION_ON) {
                        if (now_temp > target_temp) {
                            delta_temp = now_temp - target_temp;
                        } else {
                            delta_temp = target_temp - now_temp;
                        }

                        if (delta_temp > delta) {
                            log("NotificationControlInvalid H60Failure_Control_009");

                            FailureData failure_data = getFailureDataById(data.getRoomHub().getUuid(),failure_id);
                            if(failure_data == null){
                                failure_data = new FailureData();
                                failure_data.setFailureId(failure_id);
                            }
                            failure_data.setLastSendTime(System.currentTimeMillis());
                            failure_data.setCheckTime(notice_setting.getNoticeTime());

                            sendNoticeMessage(data, failure_data);

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    */
    private void NoticeDeviceLost(RoomHubData data){
        int failure_id= RoomHubFailureCause.ID.H60Failure_Net_001;
        FailureData failure_data = getFailureDataById(data.getUuid(), failure_id);

        log("NoticeDeviceLost H60Failure_Net_001");

        if (failure_data == null) {
            failure_data=new FailureData();
        }

        failure_data.setFailureId(failure_id);
        failure_data.setLastSendTime(System.currentTimeMillis());

        sendDeviceNoticeMessage(data, failure_data);
    }

    private void NoticeOffLine(RoomHubData data){
        int failure_id= RoomHubFailureCause.ID.H60Failure_Net_002;
        FailureData failure_data = getFailureDataById(data.getUuid(),failure_id);

        log("NoticeOffLine H60Failure_Net_002");
        if (failure_data == null) {
            failure_data=new FailureData();
        }

        failure_data.setFailureId(failure_id);
        failure_data.setLastSendTime(System.currentTimeMillis());

        sendDeviceNoticeMessage(data, failure_data);
    }

    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG, msg);
    }

    public FailureData getFailureDataById(String uuid,int failure_id){
        if(mFailureList.get(uuid) == null)
            return null;

        return mFailureList.get(uuid).get(failure_id);
    }

    public void updateFailureData(String uuid,FailureData failure_data){
        int id=failure_data.getFailureId();

        if(mFailureList.get(uuid) == null){
            synchronized (mFailureList) {
                HashMap<Integer, FailureData> failurelist = new HashMap<Integer, FailureData>();
                failurelist.put(id,failure_data);
                mFailureList.put(uuid, failurelist);
            }
        }else{
            synchronized (mFailureList) {
                mFailureList.get(uuid).put(id, failure_data);
            }
        }
    }

    public void removeAllFailureData(){
        synchronized (mFailureList) {
            mFailureList.clear();
        }
    }

    private void launchController(String uuid, Bundle data) {
        String roomHubUuid="";
        if(data != null) {
            roomHubUuid = data.getString(RoomHubManager.KEY_UUID);
        }

        if(TextUtils.isEmpty(roomHubUuid))
            return;

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(mContext, RoomHubControllerFlipper.class);
        intent.putExtra(RoomHubManager.KEY_UUID,roomHubUuid);
        intent.putExtra(RoomHubManager.KEY_ASSET_UUID, uuid);
        mContext.startActivity(intent);
    }
}
