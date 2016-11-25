package com.quantatw.roomhub.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.listener.ScheduleChangeListener;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.Schedule;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.roomhub.NextScheduleResPack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


/**
 * Created by 95011613 on 2015/10/5.
 */
public class ScheduleSettingActivity extends AbstractRoomHubActivity implements View.OnClickListener,CheckBox.OnCheckedChangeListener,
        AssetChangeListener,ScheduleChangeListener {
    private static final String TAG = "ScheduleSettingActivity";
    private static boolean DEBUG=true;
    private ACManager mACMgr;
    private final static int MESSAGE_UPDATE_SCHEDULE        = 100;
    private final static int MESSAGE_UPDATE_ALL_SCHEDULE    = 101;
    private final static int MESSAGE_DELETE_SCHEDULE        = 102;
    private final static int MESSAGE_LAUNCH_DEVICE_LIST     = 103;

    private Schedule mSchedule;
    private ScheduleListActivity.CMD cmd_type;
    private int[] week_btn_resId = {R.id.btn_week_mon,R.id.btn_week_tue,R.id.btn_week_wed,
            R.id.btn_week_thu,R.id.btn_week_fri,R.id.btn_week_sat,R.id.btn_week_sun};
    private int[] mode_resId={R.id.btn_schedule_cooler,R.id.btn_schedule_heater,R.id.btn_schedule_dehum,R.id.btn_schedule_fan};

    private Button mBtnStartTime;
    private Button mBtnEndTime;
    private Button[] mBtnWeek=new Button[week_btn_resId.length];
    private CheckBox mRepeat;
    private ImageView[] mBtnMode=new ImageView[mode_resId.length];
    private Button mTxtTemp;
    private Button mBtnOk;

    private int mCurMode= ACDef.FUN_MODE_COOL;
    private boolean mCurRepeat;
    ArrayList<Integer> mCurWeek=new ArrayList<Integer>();

    private int mMaxTemp;
    private int mMinTemp;
    private String mCurUuid;
    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int idx;
            switch(msg.what) {
                case MESSAGE_UPDATE_SCHEDULE: {
                    String uuid = msg.getData().getString(ACManager.KEY_UUID);

                    if (mCurUuid.equals(uuid)) {
                        Schedule schedule = (Schedule) msg.getData().getSerializable(ACManager.KEY_CMD_VALUE);
                        if (cmd_type == ScheduleListActivity.CMD.EDIT) {
                            if (schedule.getIndex() == mSchedule.getIndex()) {
                                mSchedule = schedule;
                                UpdateScheduleData();
                                SwitchModeBtn(mCurMode);
                            }
                        }
                    }
                    break;
                }
                case MESSAGE_UPDATE_ALL_SCHEDULE: {
                    String uuid = msg.getData().getString(ACManager.KEY_UUID);
                    if (mCurUuid.equals(uuid)) {
                        ArrayList<Schedule> schedule_lst = (ArrayList<Schedule>) msg.getData().getSerializable(ACManager.KEY_CMD_VALUE);
                        for(int i=0;i<schedule_lst.size();i++){
                            Schedule schedule=schedule_lst.get(i);
                            if(mSchedule.getIndex() == schedule.getIndex()){
                                mSchedule=schedule;
                                UpdateScheduleData();
                                SwitchModeBtn(mCurMode);
                                break;
                            }
                        }
                    }
                    break;
                }
                case MESSAGE_DELETE_SCHEDULE:
                    idx=(int)msg.obj;
                    if(mSchedule != null){
                        if(mSchedule.getIndex() == idx){
                            finish();
                        }
                    }

                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_setting);

        mContext=this;
        getWindow().setBackgroundDrawableResource(R.color.color_very_dark_blue);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mACMgr=(ACManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //ledOnOff(mData.getUuid(), true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mACMgr.registerAssetsChange(this);
        mACMgr.registerScheduleChange(this);

        cmd_type= (ScheduleListActivity.CMD) getIntent().getSerializableExtra(RoomHubManager.KEY_CMD_TYPE);
        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);

        if(cmd_type == ScheduleListActivity.CMD.EDIT) {
            mSchedule = getIntent().getParcelableExtra(RoomHubManager.KEY_CMD_VALUE);
        }

        initLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
       //ledOnOff(mData.getUuid(),false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mACMgr.unRegisterAssetsChange(this);
        mACMgr.unRegisterScheduleChange(this);

        if(mCurWeek!=null)
            mCurWeek.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initLayout(){

        if(cmd_type == ScheduleListActivity.CMD.ADD)
            getActionBar().setTitle(getResources().getString(R.string.add_schedule));
        else
            getActionBar().setTitle(getResources().getString(R.string.edit_schedule));

        mBtnStartTime=(Button)findViewById(R.id.btn_start_time);
        mBtnStartTime.setOnClickListener(this);

        mBtnEndTime=(Button)findViewById(R.id.btn_end_time);
        mBtnEndTime.setOnClickListener(this);

        mRepeat=(CheckBox)findViewById(R.id.cb_repeat);
        mRepeat.setOnCheckedChangeListener(this);

        for(int i=0;i<mode_resId.length;i++) {
            mBtnMode[i] = (ImageView)findViewById(mode_resId[i]);
            mBtnMode[i].setOnClickListener(this);
        }

        mBtnOk=(Button)findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);

        mTxtTemp=(Button)findViewById(R.id.txt_temp);
        mTxtTemp.setOnClickListener(this);

        for(int i=0;i<week_btn_resId.length;i++) {
            mBtnWeek[i] = (Button)findViewById(week_btn_resId[i]);
            mBtnWeek[i].setOnClickListener(this);
        }

        if(cmd_type == ScheduleListActivity.CMD.EDIT){
            UpdateScheduleData();
        }else{
            mTxtTemp.setText(String.valueOf((int) Utils.getTemp(this, mACMgr.getRecommendTemp(mCurUuid))));
        }

        SwitchModeBtn(mCurMode);

    }

    private void UpdateScheduleData(){
        boolean is_selected=false;
        mBtnStartTime.setText(mSchedule.getStartTime());
        String[] splitTime1 = mSchedule.getStartTime().split(":");
        String[] splitTime2 = mSchedule.getEndTime().split(":");
        String splitTime3 = splitTime2[0].subSequence(splitTime2[0].length() - 2, splitTime2[0].length()).toString();
        int time1 = Integer.valueOf(splitTime1[0])*100 +Integer.valueOf(splitTime1[1]);
        int time2 = Integer.valueOf(splitTime3)*100 +Integer.valueOf(splitTime2[1]);

        if(time1 > time2)
            mBtnEndTime.setText(getString(R.string.next_day) +" "+ splitTime3 +":"+splitTime2[1]);
        else
            mBtnEndTime.setText(mSchedule.getEndTime());

        if(mCurWeek!=null)
            mCurWeek.clear();

        for(int i=0;i<week_btn_resId.length;i++) {
            int[] weeks=mSchedule.getWeek();
            is_selected=false;

            for(int j=0;j<weeks.length;j++){
                if((i+1) == weeks[j]){
                    mBtnWeek[i].setSelected(true);
                    mCurWeek.add(i + 1);
                    is_selected=true;
                    break;
                }
            }
            if(!is_selected)
                mBtnWeek[i].setSelected(false);
        }
        if(mSchedule.getRepeat() == true)
            mRepeat.setChecked(true);
        else
            mRepeat.setChecked(false);
        mCurMode=mSchedule.getType();

        mTxtTemp.setText(String.valueOf((int) Utils.getTemp(this, mSchedule.getValue())));

    }

    private boolean SwitchModeBtn(int mode) {
        LinearLayout ll_temp=(LinearLayout)findViewById(R.id.ll_temp);
        TextView txt_mode=(TextView)findViewById(R.id.txt_mode);

        int[] limit_temp=mACMgr.getLimitTemp(mCurUuid,mode);

        if(limit_temp[0] < 0 && limit_temp[1] < 0) {
            Toast.makeText(mContext,getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
            return false;
        }

        mMinTemp=(int)Utils.getTemp(this,limit_temp[0]);
        mMaxTemp=(int)Utils.getTemp(this,limit_temp[1]);

        mCurMode=mode;

        switch (mCurMode){
            case ACDef.FUN_MODE_COOL:
            case ACDef.FUN_MODE_HEAT:
                ll_temp.setVisibility(View.VISIBLE);
                if(mCurMode == ACDef.FUN_MODE_COOL)
                    txt_mode.setText(getResources().getString(R.string.cooler));
                else
                    txt_mode.setText(getResources().getString(R.string.heater));

                try {
                    if (mMinTemp > Integer.parseInt(mTxtTemp.getText().toString())) {
                        mTxtTemp.setText(String.valueOf((int) Utils.getTemp(this, mMinTemp)));
                    } else if (mMaxTemp < Integer.parseInt(mTxtTemp.getText().toString())) {
                        mTxtTemp.setText(String.valueOf((int) Utils.getTemp(this, mMaxTemp)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ACDef.FUN_MODE_DRY:
            case ACDef.FUN_MODE_FAN:
                ll_temp.setVisibility(View.INVISIBLE);
                if(mCurMode == ACDef.FUN_MODE_DRY)
                    txt_mode.setText(getResources().getString(R.string.dehumidifier));
                else
                    txt_mode.setText(getResources().getString(R.string.fan));
                break;
        }

        int idx=getModeBtnIdx(mCurMode);
        for (int i = 0; i < mBtnMode.length; i++) {
            if (i == idx) {
                mBtnMode[i].setSelected(true);
            } else {
                mBtnMode[i].setSelected(false);
            }
        }
        return true;
    }

    private int getModeBtnIdx(int mode){
        int idx=0;
        switch (mode){
            case ACDef.FUN_MODE_COOL:
                idx=0;
                break;
            case ACDef.FUN_MODE_HEAT:
                idx=1;
                break;
            case ACDef.FUN_MODE_DRY:
                idx=2;
                break;
            case ACDef.FUN_MODE_FAN:
                idx=3;
                break;
        }
        return idx;
    }

    @Override
    public void onClick(View v){
         switch (v.getId()){
             case R.id.btn_start_time:
             case R.id.btn_end_time:
                 final View time_view=v;
                 Calendar c = Calendar.getInstance();
                 int hour = c.get(Calendar.HOUR_OF_DAY);
                 int minute = c.get(Calendar.MINUTE);
                 TimePickerDialog start_time_dialog = new TimePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT,
                         new TimePickerDialog.OnTimeSetListener() {
                             public void onTimeSet(TimePicker view, int hourOfDay,
                                                   int minute) {
                                 ((Button) time_view).setText(String.format("%02d:%02d", hourOfDay, minute));

                                 String[] splitTime1 = mBtnStartTime.getText().toString().split(":");
                                 String[] splitTime2 = mBtnEndTime.getText().toString().split(":");
                                 String splitTime3 = splitTime2[0].subSequence(splitTime2[0].length() - 2, splitTime2[0].length()).toString();
                                 if(mBtnStartTime.getText().toString().equals("-- : --")
                                         ||mBtnEndTime.getText().toString().equals("-- : --")) {
                                 }else{
                                     try {
                                         int time1 = Integer.valueOf(splitTime1[0]) * 100 + Integer.valueOf(splitTime1[1]);
                                         int time2 = Integer.valueOf(splitTime3) * 100 + Integer.valueOf(splitTime2[1]);

                                         if (time1 > time2)
                                             mBtnEndTime.setText(getString(R.string.next_day) + " " + splitTime3 + ":" + splitTime2[1]);
                                         else
                                             mBtnEndTime.setText(splitTime3 + ":" + splitTime2[1]);
                                     } catch (Exception e) {
                                         e.printStackTrace();
                                     }
                                 }
                             }
                         }, hour, minute, false);
                 start_time_dialog.show();

                 break;
             case R.id.btn_week_mon:
             case R.id.btn_week_tue:
             case R.id.btn_week_wed:
             case R.id.btn_week_thu:
             case R.id.btn_week_fri:
             case R.id.btn_week_sat:
             case R.id.btn_week_sun:
                 int week=Arrays.binarySearch(week_btn_resId,v.getId());
                 week+=1;

                 int idx=Collections.binarySearch(mCurWeek,week);
                 if (idx >= 0) {
                     mCurWeek.remove(idx);
                     v.setSelected(false);
                 }else {
                     v.setSelected(true);
                     mCurWeek.add(week);
                 }


                 break;
             case R.id.btn_schedule_cooler:
                 SwitchModeBtn(ACDef.FUN_MODE_COOL);
                 break;
             case R.id.btn_schedule_heater:
                 SwitchModeBtn(ACDef.FUN_MODE_HEAT);
                 break;
             case R.id.btn_schedule_dehum:
                 SwitchModeBtn(ACDef.FUN_MODE_DRY);
                 break;
             case R.id.btn_schedule_fan:
                 SwitchModeBtn(ACDef.FUN_MODE_FAN);
                 break;
             case R.id.btn_ok:
                 try {
                     SaveSchedule();
                 } catch (ParseException e) {
                     e.printStackTrace();
                 }

                 break;
             case R.id.txt_temp:
                 showNumberDialog();
                 break;
        }
    }

    class tempScheduleDate {
        Calendar startTime;
        Calendar endTime;
    }

    private Schedule CheckWeekIsExist(int week){
        int idx=-1;
        if(cmd_type == ScheduleListActivity.CMD.EDIT) {
            mSchedule = getIntent().getParcelableExtra(RoomHubManager.KEY_CMD_VALUE);
            idx=mSchedule.getIndex();
        }

        ArrayList<Schedule> schedule_lst=mACMgr.getAllSchedules(mCurUuid);

        if(schedule_lst != null){
            for(int i=0;i < schedule_lst.size();i++) {
                Schedule schedule = schedule_lst.get(i);
                if(schedule.getIndex() != idx) {
                    int[] weeks = schedule.getWeek();
                    for (int j = 0; j < weeks.length; j++) {
                        if (weeks[j] == week) {
                            return schedule;
                        }
                    }
                }
            }
        }

        return null;
    }

    private String timeFilterNext(String time){
        String[] splitTime1 = time.split(":");
        String splitTime2 = splitTime1[0].subSequence(splitTime1[0].length() - 2, splitTime1[0].length()).toString();
        time = splitTime2 +":"+splitTime1[1];
        return time;
    }

    private Calendar TransferTime(String time, int count)
    {
        Calendar cc = Calendar.getInstance();
        String[] splitTime1 = time.split(":");
        cc.set(2016, 1, 10, Integer.valueOf(splitTime1[0]), Integer.valueOf(splitTime1[1]));
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        cc.add(Calendar.DAY_OF_MONTH, count);
        return cc;
    }

    private boolean CheckTimeRange(String start_time,String end_time) {

        ArrayList<Schedule> schedule_lst=mACMgr.getAllSchedules(mCurUuid);
        SimpleDateFormat date_format = new SimpleDateFormat("HH:mm");
        ArrayList<tempScheduleDate> newScheduleList = new ArrayList<tempScheduleDate>();
        ArrayList<tempScheduleDate> oldScheduleList = new ArrayList<tempScheduleDate>();
        int tempCount1=0;
        int idx=-1;
        if(cmd_type == ScheduleListActivity.CMD.EDIT) {
            mSchedule = getIntent().getParcelableExtra(RoomHubManager.KEY_CMD_VALUE);
            idx=mSchedule.getIndex();
        }
        try {
            Date new_start_time = date_format.parse(start_time);
            Date new_end_time = date_format.parse(timeFilterNext(end_time));
            String[] splitTime1 = start_time.split(":");
            String[] splitTime2 = end_time.split(":");
            String splitTime3 = splitTime2[0].subSequence(splitTime2[0].length() - 2, splitTime2[0].length()).toString();

            int time1 = Integer.valueOf(splitTime1[0]) * 100 + Integer.valueOf(splitTime1[1]);
            int time2 = Integer.valueOf(splitTime3) * 100 + Integer.valueOf(splitTime2[1]);
            if(time1 > time2)
                tempCount1 = 1;

            int tempEight = 0;
            for(int i=0;i < mCurWeek.size();i++){
                int new_week=mCurWeek.get(i);
                if(new_week == 1)
                    tempEight = 1;
            }

            for(int i=0;i < mCurWeek.size()+tempEight;i++){
                int new_week;
                if(i == (mCurWeek.size()))
                    new_week = 8;
                else
                    new_week = mCurWeek.get(i);
                tempScheduleDate newScheduleListTemp = new tempScheduleDate();
                newScheduleListTemp.startTime = TransferTime(timeFilterNext(new_start_time.toString()),new_week);
                newScheduleListTemp.endTime = TransferTime(timeFilterNext(new_end_time.toString()),new_week+tempCount1);
                newScheduleList.add(newScheduleListTemp);
            }
        } catch (ParseException e){};

        for(int k=0;k < schedule_lst.size(); k++) {
            Schedule schedule = schedule_lst.get(k);
            if(schedule.getIndex() != idx) {
                int[] weeks = schedule.getWeek();
                int tempCount2 = 0;
                for (int j = 0; j < weeks.length; j++) {
                    try {
                        Date old_start_time = date_format.parse(schedule.getStartTime());
                        Date old_end_time = date_format.parse(schedule.getEndTime());

                        String[]splitTime1 = timeFilterNext(old_start_time.toString()).split(":");
                        String[]splitTime2 = timeFilterNext(old_end_time.toString()).split(":");
                        String splitTime3 = splitTime2[0].subSequence(splitTime2[0].length() - 2, splitTime2[0].length()).toString(); //split string "next", just get number
                        try {
                            int time1 = Integer.valueOf(splitTime1[0]) * 100 + Integer.valueOf(splitTime1[1]);
                            int time2 = Integer.valueOf(splitTime3) * 100 + Integer.valueOf(splitTime2[1]);

                            if (time1 > time2)
                                tempCount2 = 1;
                        } catch (Exception e) {
                        }

                        //Add old schedule
                        tempScheduleDate oldScheduleListTemp = new tempScheduleDate();
                        oldScheduleListTemp.startTime = TransferTime(timeFilterNext(old_start_time.toString()), weeks[j]);
                        oldScheduleListTemp.endTime = TransferTime(timeFilterNext(old_end_time.toString()), weeks[j] + tempCount2);
                        oldScheduleList.add(oldScheduleListTemp);

                        // if have the monday, need to process the next week monday
                        if(weeks[0] == 1) { // 1/18
                            tempScheduleDate nextWeekMondaySchedule = new tempScheduleDate();
                            nextWeekMondaySchedule.startTime = TransferTime(timeFilterNext(old_start_time.toString()), weeks[0]+7);
                            nextWeekMondaySchedule.endTime = TransferTime(timeFilterNext(old_end_time.toString()), weeks[0] + tempCount2+7);
                            oldScheduleList.add(nextWeekMondaySchedule);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        for(int i=0;i < newScheduleList.size();i++)
            for(int j=0;j < oldScheduleList.size();j++)
            {
            if(!(newScheduleList.get(i).startTime.compareTo(oldScheduleList.get(j).endTime) >= 0
                    || newScheduleList.get(i).endTime.compareTo(oldScheduleList.get(j).startTime) <= 0))
                return true;
        }

        return false;
    }

    private void SaveSchedule() throws ParseException {
        String str_temp=mTxtTemp.getText().toString();
        int temp=0;
        if(str_temp.length() != 0)
            temp=Integer.valueOf(str_temp);

        String start_time=mBtnStartTime.getText().toString();
        String end_time=timeFilterNext(mBtnEndTime.getText().toString());

        if(start_time.equals("-- : --")){
            Toast.makeText(mContext,getResources().getString(R.string.start_time_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        if(end_time.equals("-- : --")){
            Toast.makeText(mContext,getResources().getString(R.string.end_time_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mCurWeek.size() == 0){
            Toast.makeText(mContext,getResources().getString(R.string.week_warning), Toast.LENGTH_SHORT).show();
            return;
        }

        if(start_time.equals(end_time)) {
            Toast.makeText(mContext,getResources().getString(R.string.same_start_end_time), Toast.LENGTH_SHORT).show();
            return;
        }

        if(CheckTimeRange(start_time,end_time)){
            Toast.makeText(mContext,getResources().getString(R.string.set_time_already_exist), Toast.LENGTH_SHORT).show();
            return;
        }

        if(mCurMode == ACDef.FUN_MODE_COOL || mCurMode == ACDef.FUN_MODE_HEAT) {
            if (temp < mMinTemp || temp > mMaxTemp) {
                String str = String.format(getResources().getString(R.string.schedule_warning), mMinTemp + "~" + mMaxTemp);
                Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(Utils.isFahrenheit(this)) {
            temp = (int)Utils.toCelsius(temp);
        }

        if(cmd_type == ScheduleListActivity.CMD.ADD){
            mSchedule=new Schedule();
        }

        mSchedule.setEnable(true);
        mSchedule.setType(mCurMode);
        mSchedule.setValue(temp);
        mSchedule.setStartTime(start_time);
        mSchedule.setEndTime(end_time);
        mSchedule.setRepeat(mCurRepeat);

        int[] weeks = new int[mCurWeek.size()];
        for(int i=0;i<mCurWeek.size();i++){
            weeks[i] = mCurWeek.get(i);
        }
        mSchedule.setWeek(weeks);

        switch(cmd_type){
            case ADD:
                mACMgr.AddSchedule(mCurUuid, mSchedule);
                break;
            case EDIT:
                mACMgr.ModifySchedule(mCurUuid, mSchedule);
                break;

        }
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId() == R.id.cb_repeat)
            mCurRepeat=isChecked;
    }


    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) && (data != null)) {
            if (((ACData)data).getAssetUuid().equals(mCurUuid)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {

    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC) && (data != null)) {
            if (((ACData)data).getAssetUuid().equals(mCurUuid) && (enabled == false)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

    }

    @Override
    public void UpdateSchedule(String uuid,Schedule schedule) {
        if(schedule != null) {
            Message msg=new Message();
            msg.what=MESSAGE_UPDATE_SCHEDULE;
            Bundle bundle=new Bundle();
            bundle.putString(ACManager.KEY_UUID,uuid);
            bundle.putSerializable(ACManager.KEY_CMD_VALUE, schedule);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        if(schedule != null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_SCHEDULE,schedule));
    }

    @Override
    public void UpdateAllSchedule(String uuid,ArrayList<Schedule> schedule_lst) {
        if(schedule_lst != null){
            Message msg=new Message();
            msg.what=MESSAGE_UPDATE_ALL_SCHEDULE;
            Bundle bundle=new Bundle();
            bundle.putString(ACManager.KEY_UUID,uuid);
            bundle.putSerializable(ACManager.KEY_CMD_VALUE, schedule_lst);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void NextSchedule(NextScheduleResPack resPack) {

    }

    @Override
    public void DeleteSchedule(int idx) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_DELETE_SCHEDULE,idx));
    }

    @Override
    public void onCommandResult(String uuid,int result) {
        if(DEBUG)
            Log.d(TAG,"onCommandResult uuid="+uuid+" result="+result);
        if(result < ErrorKey.Success) {
            Toast.makeText(this,Utils.getErrorCodeString(this,result),Toast.LENGTH_SHORT).show();
        }
    }

    private void showNumberDialog(){

        final Dialog d = new Dialog(this,R.style.CustomNumberPickerDialog);
        d.setTitle(R.string.temperature);
        d.setContentView(R.layout.custom_number_picker_temp_dialog);
        Button b1 = (Button) d.findViewById(R.id.btn_cancel);
        Button b2 = (Button) d.findViewById(R.id.btn_ok);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(mMaxTemp); // max value 100
        np.setMinValue(mMinTemp);   // min value 0
        np.setWrapSelectorWheel(false);
        np.setValue(Integer.valueOf(mTxtTemp.getText().toString()));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss(); // dismiss the dialog
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                mTxtTemp.setText(String.valueOf(np.getValue()));
                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }
}
