package com.quantatw.roomhub.manager.asset.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.ir.ApIRParingInfo;
import com.quantatw.roomhub.listener.AccountLoginStateListener;
import com.quantatw.roomhub.listener.IRControllerCallback;
import com.quantatw.roomhub.listener.IRLearningResultCallback;
import com.quantatw.roomhub.listener.IRParingActionCallback;
import com.quantatw.roomhub.listener.ScheduleChangeListener;
import com.quantatw.roomhub.manager.*;
import com.quantatw.roomhub.ui.IRSettingDataValues;
import com.quantatw.roomhub.ui.RoomHubService;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.IRUtils;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.alljoyn.RoomHubAllJoynDef;
import com.quantatw.sls.alljoyn.RoomHubInterface;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.api.MiddlewareApi;
import com.quantatw.sls.device.RoomHubDevice;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.key.SourceType;
import com.quantatw.sls.object.IRACKeyData;
import com.quantatw.sls.object.IRBrandAndModelData;
import com.quantatw.sls.pack.homeAppliance.AcFailRecoverResPack;
import com.quantatw.sls.pack.homeAppliance.AddIRControlDataAcReqPack;
import com.quantatw.sls.pack.homeAppliance.BaseHomeApplianceResPack;
import com.quantatw.sls.pack.homeAppliance.CleanIRControlDataReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandAcReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandRemoteControlReqPack;
import com.quantatw.sls.pack.homeAppliance.CommandResPack;
import com.quantatw.sls.pack.homeAppliance.GetAcAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.HomeApplianceAbilityAc;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoReqPack;
import com.quantatw.sls.pack.homeAppliance.SetAssetInfoResPack;
import com.quantatw.sls.pack.homeAppliance.SetFailRecoverReqPack;
import com.quantatw.sls.pack.homeAppliance.detail.AcAssetDetailInfoResPack;
import com.quantatw.sls.pack.roomhub.DeleteScheduleResPack;
import com.quantatw.sls.pack.roomhub.LearningResultResPack;
import com.quantatw.sls.pack.roomhub.UpdateScheduleResPack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.quantatw.roomhub.utils.AssetDef.COMMAND_TYPE;
/**
 * Created by cherry on 2016/06/22.
 */
public class ACManager extends BaseAssetManager implements AccountLoginStateListener {
    private static final String TAG = ACManager.class.getSimpleName();

    private String mCurAccount=RoomHubDB.Account.DEFAULT_ACCOUNT;
    private static final int DEFAULT_RECOMMEND_TEMP=24;
    private static final int DEFAULT_TEMP=25;
    private static final int DEFAULT_MIN_TEMP=15;
    private static final int DEFAULT_MAX_TEMP=30;

    private static final int MESSAGE_SET_SCHEDULE           =200;
    private static final int MESSAGE_UPDATE_SCHEDULE        =201;
    private static final int MESSAGE_UPDATE_ALL_SCHEDULE    =202;
    private static final int MESSAGE_DELETE_SCHEDULE        =203;
    private static final int MESSAGE_FAIL_RECOVER           =204;
    //private static final int MESSAGE_WAKE_UP                =205;

    private static final String KEY_FAIL_RECOVER= "fail_recover";

    private enum SCHEDULE_TYPE{
        ADD,
        EDIT,
        REMOVE,
        REMOVE_ALL
    }

    private HashMap<String, ACData> mACList = new HashMap<String, ACData>();
    private IRController mIRController;
    private AccountManager mAccountMgr;
    private ACNoticeManager mACNoticeManager;
    private RoomHubDBHelper mRoomHubDBHelper;

    private HashMap<String, HashMap<String, IRLearningResultCallback>> mIRLearningResultCallback = new HashMap<>();
    private LinkedHashSet<ScheduleChangeListener> mScheduleListener = new LinkedHashSet<ScheduleChangeListener>();
    private HandlerThread mACBackgroundThread;
    private ACBackgroundHandler mACBackgroundHandler;

    protected final class ACBackgroundHandler extends Handler {
        public ACBackgroundHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            log("message what="+msg.what);
            switch (msg.what) {
                case MESSAGE_SET_SCHEDULE:
                    ACMgr_Schedule(msg.getData());
                    break;
                case MESSAGE_FAIL_RECOVER:
                    ACMgr_FailRecover((AcFailRecoverResPack) msg.getData().getParcelable(KEY_FAIL_RECOVER), (SourceType) msg.getData().getSerializable(KEY_CMD_VALUE));
                    break;
                case MESSAGE_UPDATE_SCHEDULE:
                    ACMgr_UpdateSchedule((UpdateScheduleResPack) msg.obj);
                    break;
                case MESSAGE_UPDATE_ALL_SCHEDULE:
                    ACMgr_UpdateAllSchedule(msg.getData().getString(KEY_UUID), (ArrayList<Schedule>) msg.getData().getSerializable(KEY_CMD_VALUE));
                    break;
                case MESSAGE_DELETE_SCHEDULE:
                    ACMgr_DeleteSchedule((DeleteScheduleResPack) msg.obj);
                    break;
            }
        }
    }

    @Override
    public void startup() {
        super.startup();
        mIRController = ((RoomHubService) mContext).getIRController();
    }

    @Override
    public void terminate() {

    }

    public ACManager(Context context, MiddlewareApi api) {
        super(context, api, TAG, DeviceTypeConvertApi.TYPE_ROOMHUB.AC, context.getString(R.string.electric_ac), R.drawable.ac_btn_selector, AssetDef.CONNECTION_TYPE_IR);

        mACBackgroundThread=new HandlerThread("ACMgrBackgroud");
        mACBackgroundThread.start();
        mACBackgroundHandler = new ACBackgroundHandler(mACBackgroundThread.getLooper());

        mAccountMgr=((RoomHubService) mContext).getAccountManager();
        mAccountMgr.registerForLoginState(this);

        mACNoticeManager = ((RoomHubService) mContext).getACNoticeManager();
        mRoomHubDBHelper=((RoomHubService) mContext).getRoomHubDBHelper();
    }

    private ACData getACDataByUuid(String Uuid){
        if(mACList==null) return null;

        ACData ac_data=mACList.get(Uuid);
        if(ac_data == null) {
            synchronized (mACList) {
                for (Iterator<ACData> it = mACList.values().iterator(); it.hasNext(); ) {
                    ac_data = it.next();
                    String roomhub_uuid = ac_data.getRoomHubUuid();
                    if (roomhub_uuid.equals(Uuid))
                        return ac_data;
                }
            }
        }else
            return ac_data;

        return null;
    }

    public ACData getACDataByFunMode(String uuid,int fun_mode){
        ACData ac_data=getACDataByUuid(uuid);

        if(ac_data != null) {
            SQLiteDatabase db = mRoomHubDBHelper.getWritableDatabase();
            RoomHubProfile profile;
            if(ac_data.getPowerStatus() == ACDef.POWER_OFF){
                profile=mRoomHubDBHelper.getProfileBySelected(db, uuid, mCurAccount);
            }else{
                profile = mRoomHubDBHelper.getProfileByFunMode(db, uuid, mCurAccount, fun_mode);
            }

            if (profile != null) {
                ac_data.setTemperature(getReasonableTemp(ac_data, fun_mode, profile.getTemp()));
                ac_data.setSwing(CheckSwingAbilityByFunMode(ac_data, fun_mode,profile.getSwing()));
                ac_data.setFan(CheckFanAbilityByFunMode(ac_data, fun_mode, profile.getFan()));
                ac_data.setFunctionMode(profile.getFunMode());
            } else {
                ac_data.setTemperature(getReasonableTemp(ac_data,fun_mode,DEFAULT_TEMP));
                ac_data.setFunctionMode(fun_mode);
                ac_data.setFan(CheckFanAbilityByFunMode(ac_data, fun_mode, ACDef.FAN_AUTO));
            }
            return ac_data;
        }

        return null;
    }

    public ACData getCurrentACDataByUuid(String Uuid){
        ACData ac_data=getACDataByUuid(Uuid);
        if(ac_data == null) return null;

        if(!ac_data.getRoomHubData().IsOnLine()) return null;

        boolean isProfile = true;

        int power_status = ACDef.POWER_OFF;
        int fun_mode = ACDef.FUN_MODE_COOL;
        int timer_on = -1;
        int timer_off = -1;

        fun_mode = ac_data.getFunctionMode();
        power_status = ac_data.getPowerStatus();
        timer_on = ac_data.getTimerOn();
        timer_off = ac_data.getTimerOff();
        if(ac_data.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
            ac_data.setFunctionMode(fun_mode);
            ac_data.setSwing(ac_data.getSwing());
            ac_data.setFan(ac_data.getFan());
            isProfile = false;
        }else if ((ac_data.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE) || (power_status == ACDef.POWER_ON)) {
            ac_data.setFunctionMode(fun_mode);
            ac_data.setTemperature(getReasonableTemp(ac_data, fun_mode, ac_data.getTemperature()));
            ac_data.setSwing(CheckSwingAbilityByFunMode(ac_data, fun_mode, ac_data.getSwing()));
            ac_data.setFan(CheckFanAbilityByFunMode(ac_data, fun_mode, ac_data.getFan()));
            isProfile = false;
        } else {
            isProfile = true;
        }

        if (!TextUtils.isEmpty(ac_data.getBrandName()) && !TextUtils.isEmpty(ac_data.getModelNumber())) {
            log("current RoomHub[" + Uuid + "] brand=" + ac_data.getBrandName() + ",model=" + ac_data.getModelNumber());
        }

        log("getCurrentACDataByUuid uuid=" + Uuid + " powerStatus=" + power_status + ",fun_mode" + fun_mode +
                ",temp=" + ac_data.getTemperature() + ",swing=" + ac_data.getSwing() + ",fan=" + ac_data.getFan() + ",timer_on=" + ac_data.getTimerOn() +
                ",timer_off=" + ac_data.getTimerOff());

        ac_data.setPowerStatus(power_status);
        ac_data.setTimer(timer_on, timer_off);
        if (isProfile) {
            getACDataByFunMode(Uuid, fun_mode);
        }
        log("get curRoomHubData powerStatus=" + ac_data.getPowerStatus() + ",fun_mode" + ac_data.getFunctionMode() +
                ",temp=" + ac_data.getTemperature() + ",swing=" + ac_data.getSwing() + ",fan=" + ac_data.getFan());
        return ac_data;
    }

    public int setCommand(String uuid,COMMAND_TYPE command_id,int value){
        return sendCommandMessage(command_id, uuid, value, 0);
    }

    public int setCommand(String uuid,COMMAND_TYPE command_id,int cmd_value,int cmd_value2){
        return sendCommandMessage(command_id, uuid, cmd_value, cmd_value2);
    }

    private int setACCommand(Bundle bundle){
        String uuid=bundle.getString(KEY_UUID);
        ACData ac_data=getACDataByUuid(uuid);

        if(ac_data == null) {
            log("setACCommand : not found device");
            return ErrorKey.AC_DATA_NOT_FOUND;
        }
/*
        long thread_id=Thread.currentThread().getId();
        mCmdResult.put(thread_id, uuid);
        Object obj = thread_id;
        mCommandHandler.sendMessageDelayed(mCommandHandler.obtainMessage(MESSAGE_COMMAND_TIMEOUT, obj),
                mContext.getResources().getInteger(R.integer.config_send_command_timeout));
*/
        int ret_val;
        if(ac_data.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
            ret_val=ACWindowTypeCommand(ac_data,bundle);
        }else{
            ret_val=ACNormalCommand(ac_data,bundle);
        }

        //mCommandHandler.removeMessages(MESSAGE_COMMAND_TIMEOUT, obj);
        //ProgressCmdResultCallback(thread_id, ret_val);

        return ret_val;
    }

    private int ACNormalCommand(ACData ac_data,Bundle bundle){
        String uuid=ac_data.getAssetUuid();

        CommandAcReqPack req_pack=new CommandAcReqPack();
        req_pack.setUuid(uuid);
        req_pack.setPower(ac_data.getPowerStatus());
        req_pack.setMode(ac_data.getFunctionMode());
        req_pack.setTemp(ac_data.getTemperature());
        req_pack.setSwing(ac_data.getSwing());
        req_pack.setFan(ac_data.getFan());
        req_pack.setUserId(mAccountMgr.getUserId());
        req_pack.setTimeOn(ac_data.getTimerOn());
        req_pack.setTimeOff(ac_data.getTimerOff());

        log("ACNormalCommand start uuid="+req_pack.getUuid()+" powerStatus="+req_pack.getPower()+",fun_mode="+req_pack.getMode()+
                ",temp="+req_pack.getTemp()+",swing="+req_pack.getSwing()+",fan="+req_pack.getFan()+
                ",user_id="+req_pack.getUserId());

        COMMAND_TYPE cmd_type=(COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);

        if((ac_data.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE) &&
                (cmd_type != COMMAND_TYPE.POWER)) {
            req_pack.setPower(ACDef.POWER_ON);
        }

        int cmd_value=bundle.getInt(KEY_CMD_VALUE);
        boolean is_command=true;
        switch(cmd_type){
            case POWER:
                req_pack.setPower(cmd_value);
                break;
            case FUN_MODE:
                if(req_pack.getPower() == ACDef.POWER_OFF){
                    ac_data.setFunctionMode(cmd_value);
                    is_command=false;
                }
                req_pack.setMode(cmd_value);
                break;
            case TEMP:
                req_pack.setTemp(cmd_value);
                break;
            case TIMER_ON_OFF:
                int cmd_value1=bundle.getInt(KEY_CMD_VALUE1);
                req_pack.setTimeOn(cmd_value);
                req_pack.setTimeOff(cmd_value1);
                break;
            case FAN:
                req_pack.setFan(cmd_value);
                break;
            case SWING:
                req_pack.setSwing(cmd_value);
                break;
        }

        req_pack.setSwing(CheckSwingAbilityByFunMode(ac_data, req_pack.getMode(), req_pack.getSwing()));
        req_pack.setFan(CheckFanAbilityByFunMode(ac_data, req_pack.getMode(), req_pack.getFan()));
        log("ACNormalCommand end powerStatus=" + req_pack.getPower() + ",fun_mode=" + req_pack.getMode() +
                ",temp=" + req_pack.getTemp() + ",swing=" + req_pack.getSwing() + ",fan=" + req_pack.getFan() +
                ",user_id=" + req_pack.getUserId() + " is_command=" + is_command);

        int ret_val=ErrorKey.Success;
        RoomHubDevice device=ac_data.getRoomHubData().getRoomHubDevice();
        if(is_command) {
            CommandResPack res_pack = device.commandAc(req_pack);
            if(res_pack != null) {
                ret_val = res_pack.getStatus_code();
            }
        }else{
            UpdateAssetData(mAssetType,ac_data);
        }
        log("command uuid=" + uuid+" ret_val="+ret_val);

        //if the set command successfully written profile
        if(ret_val == ErrorKey.Success) {
            String[] fields;
            int[] fields_value;
            SQLiteDatabase db=mRoomHubDBHelper.getWritableDatabase();

            switch(cmd_type){
                case POWER:
                    setFailRecover(ac_data, cmd_value);
                    if((cmd_value == ACDef.POWER_ON) || (cmd_value == ACDef.POWER_TOGGLE))
                        device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);
                    else
                        device.ledControl(RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 2000, 0, 1);
                    break;
                case FUN_MODE:
                    if ((cmd_value == ACDef.FUN_MODE_COOL) || (cmd_value == ACDef.FUN_MODE_HEAT))
                        setFailRecover(ac_data,req_pack.getPower());

                    fields = new String[]{RoomHubDB.Profile.FUN_MODE, RoomHubDB.Profile.TEMP};
                    fields_value = new int[]{cmd_value, ac_data.getTemperature()};

                    mRoomHubDBHelper.UpdateProfileData(db, uuid, mCurAccount, cmd_value, fields, fields_value);
                    break;
                case TEMP:
                    int fun_mode = ac_data.getFunctionMode();

                    if ((fun_mode == ACDef.FUN_MODE_COOL) || (fun_mode == ACDef.FUN_MODE_HEAT))
                        setFailRecover(ac_data,req_pack.getPower());

                    fields = new String[]{RoomHubDB.Profile.TEMP};
                    fields_value = new int[]{cmd_value};

                    mRoomHubDBHelper.UpdateProfileData(db, uuid, mCurAccount, fun_mode, fields, fields_value);
                    break;
                case FAN:
                case SWING:
                    if((ac_data.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE) ||
                            (req_pack.getPower() == ACDef.POWER_ON)){
                        device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);
                    }
                    break;
            }
        }
        return ret_val;
    }

    private int ACWindowTypeCommand(ACData ac_data,Bundle bundle){
        String uuid=ac_data.getAssetUuid();

        CommandRemoteControlReqPack req_pack=new CommandRemoteControlReqPack();
        req_pack.setAssetType(mAssetType);
        req_pack.setUuid(uuid);

        COMMAND_TYPE cmd_type=(COMMAND_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        int cmd_value=bundle.getInt(KEY_CMD_VALUE);

        RoomHubDevice device=ac_data.getRoomHubData().getRoomHubDevice();
        switch(cmd_type){
            case KEY_ID:
                req_pack.setKeyId(cmd_value);
                break;
        }

        log("ACWindowTypeCommand uuid=" + uuid + " cmd_type=" + cmd_type + " cmd_value=" + cmd_value);
        int ret_val= ErrorKey.AC_COMMAND_FAILURE;
        CommandResPack res_pack=device.commandRemoteControl(req_pack);
        if(res_pack != null) {
            ret_val=res_pack.getStatus_code();
        }

        if(ret_val == ErrorKey.Success)
            device.ledControl(RoomHubDef.LED_COLOR_GREEN, RoomHubDef.LED_FLASH, 1000, 0, 1);

        log("ACWindowTypeCommand ret_val=" + ret_val);

        return ret_val;
    }

    private void setFailRecover(ACData ac_data,int power_status){
        if(ac_data == null) return;

        if(ac_data.getSubType() != ACDef.AC_SUBTYPE_SPLIT_TYPE)
            return;

        if(!Utils.isNotificationOn(mContext))
            return;

        NoticeSetting notice_setting=ac_data.getNoticeSetting();
        if(notice_setting.getSwitchOnOff() == ACDef.POWER_OFF)
            return;

        SetFailRecoverReqPack reqPack=new SetFailRecoverReqPack();
        reqPack.setAssetType(RoomHubAllJoynDef.assetType.ASSET_TYPE_AC);
        reqPack.setUuid(ac_data.getAssetUuid());
        if(power_status == ACDef.POWER_OFF){
            reqPack.setTime(notice_setting.getNoticeTime()*2);
            reqPack.setTemp(mContext.getResources().getInteger(R.integer.config_notification_temp_delta));
        }else {
            reqPack.setTime(notice_setting.getNoticeTime());
            reqPack.setTemp(notice_setting.getNoticeDelta());
        }
        reqPack.setUserId(mAccountMgr.getUserId());
        reqPack.setRepeatCheck(mContext.getResources().getInteger(R.integer.config_notification_conflict_time) / 60 / 1000);

        CommandResPack res_pack=ac_data.getRoomHubData().getRoomHubDevice().setFailRecover(reqPack);
        if(res_pack != null) {
            log("ACMgr_FailRecover retval=" + res_pack.getStatus_code());
        }
    }

    public void registerScheduleChange(ScheduleChangeListener listener) {
        synchronized (mScheduleListener) {
            mScheduleListener.add(listener);
        }
    }

    public void unRegisterScheduleChange(ScheduleChangeListener listener) {
        synchronized(mScheduleListener) {
            mScheduleListener.remove(listener);
        }
    }

    public int[] getLimitTemp(String uuid,int fun_mode){
        ACData ac_data=getACDataByUuid(uuid);
        int[] limit_temp=new int[]{DEFAULT_MIN_TEMP,DEFAULT_MAX_TEMP};

        if(ac_data != null){
            limit_temp[0]=ac_data.getMinValuebyFunMode(fun_mode);
            limit_temp[1]=ac_data.getMaxValuebyFunMode(fun_mode);
        }

        return limit_temp;
    }

    public ArrayList<Schedule> getAllSchedules(String uuid){
        ACData ac_data=getACDataByUuid(uuid);
        if(ac_data != null)
            return ac_data.getAllSchedule();

        return null;
    }

    public void AddSchedule(String uuid, Schedule schedule) {
        Message msg=new Message();
        msg.what=MESSAGE_SET_SCHEDULE;
        Bundle bundle=new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.ADD);
        bundle.putString(KEY_UUID, uuid);
        bundle.putSerializable(KEY_CMD_VALUE, schedule);

        msg.setData(bundle);
        mACBackgroundHandler.sendMessage(msg);
    }

    public void ModifySchedule(String uuid, Schedule schedule) {
        Message msg=new Message();
        msg.what=MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.EDIT);
        bundle.putString(KEY_UUID, uuid);
        bundle.putSerializable(KEY_CMD_VALUE, schedule);
        //bundle.putInt(KEY_CMD_VALUE1, idx);
        msg.setData(bundle);
        mACBackgroundHandler.sendMessage(msg);
    }

    public void RemoveSchedule(String uuid,int idx){
        Message msg=new Message();
        msg.what = MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.REMOVE);
        bundle.putString(KEY_UUID, uuid);
        bundle.putInt(KEY_CMD_VALUE, idx);

        msg.setData(bundle);
        mACBackgroundHandler.sendMessage(msg);
    }

    public void RemoveAllSchedule(String uuid){
        Message msg=new Message();
        msg.what = MESSAGE_SET_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CMD_TYPE, SCHEDULE_TYPE.REMOVE_ALL);
        bundle.putString(KEY_UUID, uuid);
        msg.setData(bundle);
        mACBackgroundHandler.sendMessage(msg);
    }

    private int ACMgr_Schedule(Bundle bundle){
        SCHEDULE_TYPE cmd_type=(SCHEDULE_TYPE)bundle.getSerializable(KEY_CMD_TYPE);
        String uuid=bundle.getString(KEY_UUID);
        int retval= ErrorKey.Success;
        ACData ac_data = getACDataByUuid(uuid);

        if(ac_data == null) {
            log("ACMgr_Schedule : not found device");
            return ErrorKey.AC_DATA_NOT_FOUND;
        }
        RoomHubDevice device=ac_data.getRoomHubData().getRoomHubDevice();
        switch (cmd_type){
            case ADD:
            case EDIT:
                Schedule schedule=(Schedule)bundle.getSerializable(KEY_CMD_VALUE);

                RoomHubInterface.AddSchedule_Schedule add_schedule=new RoomHubInterface.AddSchedule_Schedule();
                add_schedule.state=schedule.getEnable();
                add_schedule.modeType=schedule.getType();
                add_schedule.value=schedule.getValue();
                add_schedule.startTime=schedule.getStartTime();
                add_schedule.endTime=schedule.getEndTime();
                add_schedule.repeat=schedule.getRepeat();

                log("ACMgr_Schedule : enable="+schedule.getEnable()+" mode="+schedule.getType()+" value="+schedule.getValue()+" start_time="+schedule.getStartTime()+" end_time="+schedule.getEndTime());
                int[] weeks=schedule.getWeek();
                RoomHubInterface.weekDay_i[] schedule_week = new RoomHubInterface.weekDay_i[weeks.length];

                for(int i=0;i<weeks.length;i++){
                    schedule_week[i] = new RoomHubInterface.weekDay_i();
                    schedule_week[i].day=weeks[i];
                }
                add_schedule.weekday=schedule_week;

                if(cmd_type == SCHEDULE_TYPE.ADD) {
                    retval=device.addSchedule(add_schedule);
                } else {
                    retval=device.modifySchedule(add_schedule, schedule.getIndex());
                }
                break;
            case REMOVE:
                int idx=bundle.getInt(KEY_CMD_VALUE);
                retval=device.removeSchedule(idx);
                break;
            case REMOVE_ALL:
                retval=device.removeAllSchedule();
                break;
        }

        onCommandResult(mAssetType, uuid, retval);

        return retval;
    }

    private void ACMgr_UpdateSchedule(UpdateScheduleResPack resPack){
        String uuid=resPack.getUuid();
        ACData ac_data = getACDataByUuid(uuid);

        if(ac_data != null) {
            Schedule schedule = new Schedule();
            schedule.setIndex(resPack.getIndex());
            schedule.setType(resPack.getModeType());
            schedule.setValue(resPack.getValue());
            schedule.setRepeat(resPack.getRepeat());
            schedule.setEnable(resPack.getState());
            schedule.setStartTime(resPack.getStartTime());
            schedule.setEndTime(resPack.getEndTime());
            int[] week = new int[resPack.getWeekday().length];

            for (int i = 0; i < week.length; i++) {
                week[i] = resPack.getWeekday()[i].day;
            }
            schedule.setWeek(week);

            //ac_data.addSchedule(schedule);
            ac_data.updateSchedule(schedule);

            if (mScheduleListener != null) {
                synchronized (mScheduleListener) {
                    for (Iterator<ScheduleChangeListener> it = mScheduleListener.iterator(); it.hasNext(); ) {
                        ScheduleChangeListener listener = it.next();
                        listener.UpdateSchedule(uuid, schedule);
                    }
                }
            }
        }
    }

    private void ACMgr_UpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst){
        if(schedule_lst == null) return;

        ACData ac_data = getACDataByUuid(uuid);

        if(ac_data != null) {
            if(ac_data.getRoomHubData().IsAlljoyn())
                return;
            //ArrayList<Schedule> old_schedule_lst=ac_data.getAllSchedule();
            ac_data.setAllSchedule(schedule_lst);

            if (mScheduleListener != null) {
                synchronized (mScheduleListener) {
                    for (Iterator<ScheduleChangeListener> it = mScheduleListener.iterator(); it.hasNext(); ) {
                        ScheduleChangeListener listener = it.next();
                        listener.UpdateAllSchedule(uuid, ac_data.getAllSchedule());
                    }
                }
            }
        }
    }

    private void ACMgr_DeleteSchedule(DeleteScheduleResPack resPack){
        ACData ac_data = getACDataByUuid(resPack.getUuid());
        if(ac_data != null) {
            ac_data.removeSchedule(resPack.getIndex());

            if (mScheduleListener != null) {
                synchronized (mScheduleListener) {
                    for (Iterator<ScheduleChangeListener> it = mScheduleListener.iterator(); it.hasNext(); ) {
                        ScheduleChangeListener listener = it.next();
                        listener.DeleteSchedule(resPack.getIndex());
                    }
                }
            }
        }
    }

    private void ACMgr_FailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {
        ACData ac_data= getACDataByUuid(failRecoverResPack.getUuid());
        if(ac_data != null && ac_data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
            return;

        mACNoticeManager.sendFailRecover(ac_data, failRecoverResPack);
    }

    private void UpdateNoticeSetting(ACData ac_data){
        NoticeSetting notice_setting=ac_data.getNoticeSetting();
        if(notice_setting == null) {
            int notify_time = mContext.getResources().getInteger(R.integer.config_notification_time) * 60;
            int notify_delta = mContext.getResources().getInteger(R.integer.config_notification_temp_delta);

            notice_setting=new NoticeSetting(ACDef.POWER_ON,notify_time,notify_delta);
            notice_setting.setIsDefaultTime(1);
            notice_setting.setIsDefaultDelta(1);
            ac_data.setNoticeSetting(notice_setting);
        }
    }

    //Determine whether the temperature is in the range of reasonable
    //If unreasonable (min_value+max_value)/2
    private int getReasonableTemp(ACData ac_data,int fun_mode,int temp){
        int min_value=ac_data.getMinValuebyFunMode(fun_mode);
        int max_value=ac_data.getMaxValuebyFunMode(fun_mode);

        if((temp >= min_value) && (temp <= max_value) )
            return temp;

        return (int)(min_value+max_value)/2;
    }

    private int CheckFanAbilityByFunMode(ACData ac_data,int fun_mode,int fan){
        if(ac_data != null){
            HomeApplianceAbilityAc ability=ac_data.getAbilityByFunMode(fun_mode);
            if(ability != null) {
                int ability_fan = ability.getFan();
                boolean is_ability = false;
                switch (fan) {
                    case ACDef.FAN_AUTO:
                        if ((ability_fan & ACDef.AC_ABILITY_FAN_AUTO) != 0)
                            is_ability = true;
                        break;
                    case ACDef.FAN_HIGH:
                        if ((ability_fan & ACDef.AC_ABILITY_FAN_HIGH) != 0)
                            is_ability = true;
                        break;
                    case ACDef.FAN_MEDIUM:
                        if ((ability_fan & ACDef.AC_ABILITY_FAN_MEDIUM) != 0)
                            is_ability = true;
                        break;
                    case ACDef.FAN_LOW:
                        if ((ability_fan & ACDef.AC_ABILITY_FAN_LOW) != 0)
                            is_ability = true;
                        break;
                }
                if (!is_ability) {
                    if ((ability_fan & ACDef.AC_ABILITY_FAN_AUTO) != 0)
                        return ACDef.FAN_AUTO;
                    else if ((ability_fan & ACDef.AC_ABILITY_FAN_HIGH) != 0)
                        return ACDef.FAN_HIGH;
                    else if ((ability_fan & ACDef.AC_ABILITY_FAN_MEDIUM) != 0)
                        return ACDef.FAN_MEDIUM;
                    else if ((ability_fan & ACDef.AC_ABILITY_FAN_LOW) != 0)
                        return ACDef.FAN_LOW;
                }
            }
        }
        return fan;
    }

    private int CheckSwingAbilityByFunMode(ACData ac_data,int fun_mode,int swing){
        if(ac_data != null){
            HomeApplianceAbilityAc ability=ac_data.getAbilityByFunMode(fun_mode);
            if(ability != null) {
                int ability_swing = ability.getSwing();
                boolean is_ability = false;
                switch (swing) {
                    case ACDef.SWING_AUTO:
                        if ((ability_swing & ACDef.AC_ABILITY_SWING_AUTO) != 0)
                            is_ability = true;
                        break;
                    case ACDef.SWING_FIX:
                        if ((ability_swing & ACDef.AC_ABILITY_SWING_FIX) != 0)
                            is_ability = true;
                        break;
                }
                if (!is_ability) {
                    if ((ability_swing & ACDef.AC_ABILITY_SWING_AUTO) != 0)
                        return ACDef.SWING_AUTO;
                    else if ((ability_swing & ACDef.AC_ABILITY_SWING_FIX) != 0)
                        return ACDef.SWING_FIX;
                }
            }
        }
        return swing;
    }

    /* AccountLoginStateListener */
    @Override
    public void onLogin() {
        mCurAccount=mAccountMgr.getCurrentAccount();
        String name = mAccountMgr.getCurrentAccountName();
        SQLiteDatabase db=mRoomHubDBHelper.getWritableDatabase();
        mRoomHubDBHelper.InsertAccount(db, mCurAccount, name);
    }

    @Override
    public void onLogout() {
        mCurAccount=RoomHubDB.Account.DEFAULT_ACCOUNT;
    }

    @Override
    public void onSkipLogin() {

    }

    /* RoomHubSignalListener */
    @Override
    public void RoomHubUpdateSchedule(UpdateScheduleResPack resPack) {
        if(resPack != null)
            mACBackgroundHandler.sendMessage(mACBackgroundHandler.obtainMessage(MESSAGE_UPDATE_SCHEDULE, resPack));
    }

    @Override
    public void RoomHubUpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {
        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_ALL_SCHEDULE;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_UUID, uuid);
        bundle.putSerializable(KEY_CMD_VALUE, schedule_lst);
        msg.setData(bundle);
        mACBackgroundHandler.sendMessage(msg);
    }

    @Override
    public void RoomHubDeleteSchedule(DeleteScheduleResPack resPack) {
        if(resPack != null)
            mACBackgroundHandler.sendMessage(mACBackgroundHandler.obtainMessage(MESSAGE_DELETE_SCHEDULE, resPack));
    }

    /* HomeApplianceSignalListener */
    @Override
    public void AcFailRecover(AcFailRecoverResPack failRecoverResPack, SourceType sourceType) {
        int reason=failRecoverResPack.getReason();
        log("AcFailRecover uuid=" + failRecoverResPack.getUuid() + " reason=" + reason);
        if((reason >= ACDef.AC_FAIL_RECOVER_TURN_ON_FAIL) && (reason <= ACDef.AC_FAIL_RECOVER_REPEAT_CONTROL)) {
            Message msg = new Message();
            msg.what = MESSAGE_FAIL_RECOVER;
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_FAIL_RECOVER, failRecoverResPack);
            bundle.putSerializable(KEY_CMD_VALUE, sourceType);
            msg.setData(bundle);
            mACBackgroundHandler.sendMessage(msg);
        }
    }

    @Override
    public void configIRSetting(String uuid, String assetUuid) {
        mIRController.configIR(uuid, assetUuid, mAssetType, mIRParingAction);
    }

    /* BaseAssetCallback */
    protected AssetInfoData newAssetData(RoomHubData data) {
        return new ACData(data,mContext.getString(R.string.electric_ac),R.drawable.btn_ac2);
    }

    public String getAssetBrandAndModel(String asset_uuid){
        ACData ac_data=getACDataByUuid(asset_uuid);
        String str_fan_models=mContext.getResources().getString(R.string.ac_na);
        if(ac_data!=null){
            if(!TextUtils.isEmpty(ac_data.getBrandName()) && !TextUtils.isEmpty(ac_data.getModelNumber()))
                str_fan_models=ac_data.getBrandName() +"/"+ac_data.getModelNumber();
        }
        return str_fan_models;
    }

    @Override
    public void BaseAsset_AddDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();
        log("ACMgr_AddDevice uuid="+uuid);

        ACData ac_data=getACDataByUuid(uuid);
        boolean bAdd=false;

        if(ac_data==null){
            log("ACMgr_AddDevice is not exist");
            ac_data=(ACData)asset_info_data;
            bAdd=true;

        } else{
            log("ACMgr_AddDevice device is exist");
        }
        if(bAdd){
            synchronized(mACList) {
                mACList.put(uuid,ac_data);
            }
            NotifyAddDevice(mAssetType, ac_data);
        }
        if(room_hub.IsOnLine()) {
            ac_data.setRoomHubDB(mRoomHubDBHelper);
            UpdateAssetDataAfterOnLine(ac_data);
            UpdateNoticeSetting(ac_data);
        }

        if(bAdd){
            getCurrentACDataByUuid(uuid);
        }
    }

    @Override
    public void BaseAsset_RemoveDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();
        ACData ac_data=getACDataByUuid(uuid);

        log("ACMgr_RemoveDevice uuid=" + uuid);

        if (ac_data != null){
            NotifyRemoveDevice(mAssetType, ac_data);
            synchronized(mACList) {
                mACList.remove(uuid);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateDevice(AssetInfoData asset_info_data, RoomHubData room_hub) {
        String uuid=asset_info_data.getAssetUuid();

        log("ACMgr_UpdateDevice uuid="+uuid);

        ACData ac_data = getACDataByUuid(uuid);

        if(ac_data!=null) {
            if(room_hub.IsOnLine()) {
                UpdateAssetDataAfterOnLine(ac_data);
            }
        }
    }

    @Override
    public void BaseAsset_UpdateRoomHubData(int type, RoomHubData data) {
        if(mACList == null) return;

        String uuid=data.getUuid();

        synchronized (mACList) {
            for (Iterator<ACData> it = mACList.values().iterator(); it.hasNext(); ) {
                ACData ac_data = it.next();
                if (ac_data.getRoomHubUuid().equals(uuid)) {
                    if(type == RoomHubManager.UPDATE_ONLINE_STATUS) {
                        if(data.IsOnLine()){
                            UpdateAssetDataAfterOnLine(ac_data);
                            NotifyPageStatus(mAssetType,true,ac_data);
                        }else
                            NotifyPageStatus(mAssetType,false,ac_data);
                    }else if(type == RoomHubManager.UPDATE_SENSOR_DATA){
                        //ac_data.getNoticeStatus().setNowTemp((int)data.getSensorTemp());
                    }
                    ac_data.setRoomHubData(data);
                }
            }
        }
    }

    @Override
    public int BaseAsset_SendCommand(final Bundle bundle) {
         return setACCommand(bundle);
        /*
        new Thread() {
            @Override
            public void run() {
                log("ACMgr_SendCommand send command :::");
                int ret=setACCommand(bundle);
                log("ACMgr_SendCommand send command ::: ret=" + ret);
            }
        }.start();
        return ErrorKey.Success;
        */
    }

    @Override
    public void BaseAsset_UpgradeStats(String uuid, boolean is_upgrade) {
        if(mACList == null) return;

        synchronized (mACList) {
            for (Iterator<ACData> it = mACList.values().iterator(); it.hasNext(); ) {
                ACData ac_data = it.next();
                if (ac_data.getRoomHubUuid().equals(uuid)) {
                    if(is_upgrade)
                        NotifyPageStatus(mAssetType,false,ac_data);
                    else
                        NotifyPageStatus(mAssetType,true,ac_data);
                }
            }
        }
    }

    @Override
    public void BaseAsset_AssetInfoChange(Object ResPack, SourceType sourceType) {
        AcAssetDetailInfoResPack res_pack=(AcAssetDetailInfoResPack)ResPack;

        ACData ac_data=getACDataByUuid(res_pack.getUuid());
        if(ac_data != null){
            log("ACMgr_AssetInfoChange uuid="+ac_data.getAssetUuid()+" source_type="+sourceType);
            if(ac_data.getRoomHubData().IsAlljoyn() && (sourceType == SourceType.CLOUD))
                return;

            if(ac_data.setAbilityLimit(res_pack.getBrand(), res_pack.getDevice()) == ErrorKey.AP_ABILITY_INVALID)
                RetryMessage(ac_data,MESSAGE_RETRY_ABILITY_LIMIT);

            GetAcAssetInfoResPack ac_asset_info = new GetAcAssetInfoResPack();
            ac_asset_info.setUuid(res_pack.getUuid());
            ac_asset_info.setPower(res_pack.getPower());
            ac_asset_info.setTemp(res_pack.getTemp());
            ac_asset_info.setMode(res_pack.getMode());
            ac_asset_info.setSwing(res_pack.getSwing());
            ac_asset_info.setFan(res_pack.getFan());
            ac_asset_info.setTimeOn(res_pack.getTimerOn());
            ac_asset_info.setTimeOff(res_pack.getTimerOff());
            ac_asset_info.setSubType(res_pack.getSubType());
            ac_asset_info.setConnectionType(res_pack.getConnectionType());
            ac_asset_info.setBrand(res_pack.getBrand());
            ac_asset_info.setDevice(res_pack.getDevice());
            ac_asset_info.setBrandId(res_pack.getBrandId());
            ac_asset_info.setModelId(res_pack.getModelId());
            ac_asset_info.setOnLineStatus(res_pack.getOnLineStatus());

            ac_data.setAcAssetInfo(ac_asset_info);

            int power_status = ac_asset_info.getPower();
            int fun_mode = ac_asset_info.getMode();
            int temp = ac_asset_info.getTemp();

            ac_data.setPowerStatus(power_status);

            if ((ac_data.getSubType() != ACDef.AC_SUBTYPE_SPLIT_TYPE) ||
                    (power_status == ACDef.POWER_ON)) {
                ac_data.setFunctionMode(fun_mode);
                ac_data.setTemperature(getReasonableTemp(ac_data, fun_mode, temp));
            }
            if(ac_data.getSubType() != ACDef.AC_SUBTYPE_WINDOW_TYPE) {
                ac_data.setSwing(CheckSwingAbilityByFunMode(ac_data, fun_mode, ac_asset_info.getSwing()));
                ac_data.setFan(CheckFanAbilityByFunMode(ac_data, fun_mode, ac_asset_info.getFan()));
            }

            ac_data.setTimer(ac_asset_info.getTimeOn(), ac_asset_info.getTimeOff());

            UpdateAssetData(mAssetType,ac_data);

            log("ACMgr_AssetInfoChange powerStatus=" + ac_data.getPowerStatus() + ",fun_mode=" + ac_data.getFunctionMode() +
                    ",temp=" + ac_data.getTemperature() + ",swing=" + ac_data.getSwing() + ",fan=" + ac_data.getFan() +
                    ",timer_on=" + ac_data.getTimerOn() + ",timer_off=" + ac_data.getTimerOff() + ",sub_type="+ac_data.getSubType() +
                    ",connection_type="+ac_data.getConnectionType() +",brand="+ac_data.getBrandName() + ",device="+ac_data.getModelNumber() +
                    ",brand_id=" + ac_data.getBrandId() +",model_id="+ac_data.getModelId() + ",onLineStatus="+ac_data.getOnlineStatus());
        }
    }

    @Override
    public void BaseAsset_GetAssetInfo(Object data) {
        ACData ac_data = (ACData)data;
        if((ac_data != null) && ac_data.getRoomHubData().IsOnLine()){
            if(ac_data.getAcAssetInfo() == ErrorKey.AC_ASSET_INFO_INVALID)
                RetryMessage(ac_data,MESSAGE_RETRY_ASSET_INFO);
        }
    }

    @Override
    public void BaseAsset_GetAbilityLimit(Object data) {
        ACData ac_data = (ACData)data;
        if((ac_data != null) && ac_data.getRoomHubData().IsOnLine()){
            if(ac_data.getAssetAbility() == ErrorKey.AC_ABILITY_INVALID)
                RetryMessage(ac_data,MESSAGE_RETRY_ABILITY_LIMIT);
        }
    }


    public void BaseAsset_WakeUp(){
        if(mACList == null) return;

        new Thread() {
            @Override
            public void run() {
                synchronized (mACList) {
                    for (Iterator<ACData> it = mACList.values().iterator(); it.hasNext(); ) {
                        ACData ac_data = it.next();
                        if (ac_data.getRoomHubData().IsCloud()){
                            log("ACMgr_ProcessWakeUp cloud device uuid="+ac_data.getAssetUuid());
                            UpdateAssetDataAfterOnLine(ac_data);

                            UpdateAssetData(mAssetType,ac_data);
                        }
                    }
                }
            }
        }.start();
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
            if(callback != null) {
                byte[] irData = IRUtils.getIRDataFromResPack(learningResultResPack.getIrData());
                if(irData != null && irData.length == 1) {
                    callback.onLoadResultsFail(learningResultResPack.getUuid());
                }
                else {
                    log("irData len=" + irData.length);
                    if(irData.length > 22) {
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
                AddIRControlDataAcReqPack reqPack = new AddIRControlDataAcReqPack();
                reqPack.setUuid(currentTarget.getAssetUuid());
                reqPack.setKeyId(Integer.parseInt(iracKeyData.getKeyId()));
                reqPack.setIrData(iracKeyData.getIrData());
                reqPack.setStPower(iracKeyData.getStPower());
                reqPack.setStMode(iracKeyData.getStMode());
                reqPack.setStTemp(iracKeyData.getStTemp());

                reqPack.setStFan(iracKeyData.getStFan());
                reqPack.setStSwing(iracKeyData.getStSwing());

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
        ACData ac_data=getACDataByUuid(uuid);

        if(ac_data == null) return false;

        RoomHubDevice roomhub_device=ac_data.getRoomHubData().getRoomHubDevice();
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
        ACData ac_data=getACDataByUuid(uuid);

        if(ac_data == null) return false;

        RoomHubDevice roomhub_device=ac_data.getRoomHubData().getRoomHubDevice();
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
        ACData ac_data=getACDataByUuid(uuid);
        CleanIRControlDataReqPack reqPack = new CleanIRControlDataReqPack();
        reqPack.setAssetType(mAssetType);
        reqPack.setUuid(uuid);
        ac_data.getRoomHubData().getRoomHubDevice().cleanIRControlData(reqPack);
    }

    private boolean addIRControlData(String uuid, AddIRControlDataAcReqPack  reqPack) {
        ACData ac_data=getACDataByUuid(uuid);
        BaseHomeApplianceResPack baseHomeApplianceResPack = ac_data.getRoomHubData().getRoomHubDevice().addIRControlDataAc(reqPack);
        return baseHomeApplianceResPack.getStatus_code()==ErrorKey.Success?true:false;
    }

    public boolean IsAbilityByKeyId(String uuid,int key_id){
        ACData ac_data=getACDataByUuid(uuid);
        if(ac_data != null){
            if(ac_data.getSubType() != ACDef.AC_SUBTYPE_WINDOW_TYPE)
                return false;

            int[] ability=ac_data.getRemoteControlAbilityLimit();
            if(ability != null){
                for(int i=0;i<ability.length;i++) {
                    if(ability[i] == key_id)
                        return true;
                }
            }
        }

        return false;
    }

    public int getRecommendTemp(String uuid){
        return DEFAULT_RECOMMEND_TEMP;
    }
}
