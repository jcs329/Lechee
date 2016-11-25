 package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;
import com.quantatw.sls.pack.homeAppliance.HomeApplianceAbilityAc;

import java.math.BigInteger;
import java.util.Calendar;

/**
 * Created by 95011613 on 2015/11/10.
 */
public class AdvanceSettingActivity extends AbstractRoomHubActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener,AssetChangeListener{
    private static final String TAG = "AdvanceSettingActivity";
    private static boolean DEBUG=true;

    private final int MESSAGE_UPDATE_AC_DATA   = 100;
    private final int MESSAGE_LAUNCH_DEVICE_LIST =101;
    private final int MESSAGE_SHOW_PROGRESS_DIALOG      = 102;
    private final int MESSAGE_DISMISS_PROGRESS_DIALOG   = 103;
    private final int MESSAGE_SHOW_TOAST                = 104;

    private int[] mFanModeTitleResId={R.id.txt_wind_auto,R.id.txt_wind_high,R.id.txt_wind_med,R.id.txt_wind_low};
    private TextView[] mFanModeTitle = new TextView[mFanModeTitleResId.length];

    private Context mContext;
    private ACManager mACMgr;
    private ACData mData;

    private LinearLayout mTimer;
    private Button mBtnTurnOff;
    private Button mBtnClear;
    private LinearLayout mFan;
    private LinearLayout mFanToggle;
    private SeekBar mFanMode;
    private ImageView mBtnFanToggle;
    private Button mBtnAuto;
    private Button mBtnFix;
    private LinearLayout mAdvanceNotice;
    private ImageView mBtnNoticeSwitchOff;
    private LinearLayout mNoticeSetting;

    private String mCurUuid;
    /*
    private int mCurTimerOff=0;
    private int mCurFanMode;
    private int mCurSwing;
    private int mCurNoticeOnOff;
    private int mCurSubType;
    */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(DEBUG)
                Log.d(TAG, "what=" + msg.what);
            switch(msg.what) {
                case MESSAGE_UPDATE_AC_DATA:
                    UpdateAdvance((ACData)msg.obj);
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                case MESSAGE_SHOW_PROGRESS_DIALOG:
                    showProgressDialog("", getString(R.string.processing_str));
                    break;
                case MESSAGE_DISMISS_PROGRESS_DIALOG:
                    dismissProgressDialog();
                    break;
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(mContext,(String)msg.obj,Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.advance_setting);
        mContext=this;
        getWindow().setBackgroundDrawableResource(R.color.color_very_dark_blue);

        mACMgr=(ACManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //ledOnOff(mData.getUuid(),true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mACMgr.registerAssetsChange(this);

        String uuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);

        mData=mACMgr.getCurrentACDataByUuid(uuid);
        if(mData == null) {
            finish();
            return;
        }

        initLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mACMgr.unRegisterAssetsChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initLayout(){
        getActionBar().setTitle(mData.getRoomHubData().getName());

        /*

        mCurTimerOff=mData.getTimerOff();
        mCurFanMode=mData.getFan();
        mCurSwing=mData.getSwing();
        */
        mCurUuid=mData.getAssetUuid();

        mTimer=(LinearLayout)findViewById(R.id.ll_timer);

        mBtnTurnOff=(Button)findViewById(R.id.btn_turn_off);
        mBtnTurnOff.setOnClickListener(this);

        mBtnClear=(Button)findViewById(R.id.btn_clear);
        mBtnClear.setOnClickListener(this);

        mFanMode=(SeekBar)findViewById(R.id.wind_seekbar);
        mFanMode.setOnSeekBarChangeListener(this);
        mFanMode.setMax(100);
        for(int i=0;i < mFanModeTitleResId.length;i++){
            mFanModeTitle[i]=(TextView)findViewById(mFanModeTitleResId[i]);
        }

        mFan=(LinearLayout)findViewById(R.id.ll_fan);
        mFanToggle=(LinearLayout)findViewById(R.id.ll_fan_toggle);

        mBtnFanToggle=(ImageView)findViewById(R.id.btn_speed);
        mBtnFanToggle.setOnClickListener(this);

        mBtnAuto=(Button)findViewById(R.id.btn_auto);
        mBtnAuto.setOnClickListener(this);

        mBtnFix=(Button)findViewById(R.id.btn_fix);
        mBtnFix.setOnClickListener(this);

        mAdvanceNotice=(LinearLayout)findViewById(R.id.advance_notice);

        mBtnNoticeSwitchOff=(ImageView)findViewById(R.id.btn_switch_off);
        mBtnNoticeSwitchOff.setOnClickListener(this);

        mNoticeSetting=(LinearLayout)findViewById(R.id.ll_notice_setting);
        mNoticeSetting.setOnClickListener(this);

        /*
        NoticeSetting notice_setting=mData.getNoticeSetting();
        if(notice_setting != null){
            mCurNoticeOnOff=notice_setting.getSwitchOnOff();
        }
        */
        UpdateAdvanceData();
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress = true;
        boolean is_ability;
         switch (v.getId()){
             case R.id.btn_auto:
                 if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                     is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_SWING_ON);
                     if(is_ability)
                         mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_SWING_ON);
                 }else {
                     is_ability=IsSwingAbility(ACDef.AC_ABILITY_SWING_AUTO, mData.getFunctionMode());
                     if (is_ability)
                         mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.SWING,ACDef.SWING_AUTO);
                 }
                 if(!is_ability) {
                     is_show_progress=false;
                     Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                 }
                 break;
             case R.id.btn_fix:
                 if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                     is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,ACDef.WINDOW_TYPE_KEY_ID_SWING_OFF);
                     if(is_ability)
                         mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID, ACDef.WINDOW_TYPE_KEY_ID_SWING_OFF);
                 }else {
                     is_ability=IsSwingAbility(ACDef.AC_ABILITY_SWING_FIX, mData.getFunctionMode());
                     if(is_ability)
                         mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.SWING, ACDef.SWING_FIX);
                 }

                 if(!is_ability) {
                     is_show_progress=false;
                     Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                 }
                break;
             case R.id.btn_turn_off:
                 is_show_progress=false;
                 ShowNumberDialog();
                 break;
             case R.id.btn_switch_off:
                 is_show_progress=false;
                 NoticeSetting notice_setting=mData.getNoticeSetting();

                 if(notice_setting != null) {
                     if (notice_setting.getSwitchOnOff() == ACDef.POWER_ON) {
                         notice_setting.setSwitchOnOff(ACDef.POWER_OFF);
                     } else {
                         notice_setting.setSwitchOnOff(ACDef.POWER_ON);
                     }

                     mData.setNoticeSetting(notice_setting);
                 }

                 //UpdateNoticeOnOff();
                 break;
             case R.id.ll_notice_setting:
                 is_show_progress=false;

                 Intent intent = new Intent(this, NoticeSettingActivity.class);
                 intent.putExtra(RoomHubManager.KEY_UUID,mCurUuid);
                 /*
                 Bundle bundle = new Bundle();
                 bundle.putParcelable(ACManager.KEY_AC_DATA, mData);
                 intent.putExtras(bundle);
                 */
                 startActivity(intent);
                 break;
             case R.id.btn_clear:
                 mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.TIMER_ON_OFF, 0, -1);
                 break;
             case R.id.btn_speed:
                 if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE) {
                     is_ability = mACMgr.IsAbilityByKeyId(mCurUuid, ACDef.WINDOW_TYPE_KEY_ID_FAN_SWITCH);
                     if (is_ability)
                         mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.KEY_ID,ACDef.WINDOW_TYPE_KEY_ID_FAN_SWITCH);
                 }
                 break;
        }
        if(is_show_progress)
            mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
    }

    private boolean IsFANAbility(int fan_ability,int fun_mode){
        HomeApplianceAbilityAc ability=mData.getAbilityByFunMode(fun_mode);
        boolean is_ability=false;

        if(ability != null) {
            int fan = ability.getFan();
            switch(fan_ability){
                case ACDef.AC_ABILITY_FAN_AUTO:
                case ACDef.AC_ABILITY_FAN_HIGH:
                case ACDef.AC_ABILITY_FAN_MEDIUM:
                case ACDef.AC_ABILITY_FAN_LOW:
                    int is_auto= fan & fan_ability;
                    if(is_auto != 0)
                        is_ability=true;
                    break;
            }
        }

        return is_ability;
    }

    private boolean IsSwingAbility(int swing_ability,int fun_mode){
        HomeApplianceAbilityAc ability=mData.getAbilityByFunMode(fun_mode);
        boolean is_ability=false;

        if(ability != null) {
            int swing = ability.getSwing();
            switch(swing_ability){
                case ACDef.AC_ABILITY_SWING_FIX:
                case ACDef.AC_ABILITY_SWING_AUTO:
                    int is_auto= swing & swing_ability;
                    if(is_auto != 0)
                        is_ability=true;
                    break;
            }
        }

        return is_ability;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(DEBUG)
            Log.d(TAG, "onProgressChanged progress=" + progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seekProgress = mFanMode.getProgress();
        boolean is_ability = false;
        AssetDef.COMMAND_TYPE command_type;
        int command_value = 0;

        if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE)
            command_type = AssetDef.COMMAND_TYPE.KEY_ID;
        else
            command_type = AssetDef.COMMAND_TYPE.FAN;

        if(seekProgress<15){
            if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                command_value=ACDef.WINDOW_TYPE_KEY_ID_FAN_LOW;
                is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,command_value);
            }else{
                command_value=ACDef.FAN_LOW;
                is_ability=IsFANAbility(ACDef.AC_ABILITY_FAN_LOW,mData.getFunctionMode());
            }
        }else if(seekProgress>=15 && seekProgress<50){
            if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                command_value=ACDef.WINDOW_TYPE_KEY_ID_FAN_MIDDLE;
                is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,command_value);
            }else{
                command_value=ACDef.FAN_MEDIUM;
                is_ability=IsFANAbility(ACDef.AC_ABILITY_FAN_MEDIUM,mData.getFunctionMode());
            }
        }else if(seekProgress>=50 && seekProgress<80){
            if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                command_value=ACDef.WINDOW_TYPE_KEY_ID_FAN_HIGH;
                is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,command_value);
            }else{
                command_value=ACDef.FAN_HIGH;
                is_ability=IsFANAbility(ACDef.AC_ABILITY_FAN_HIGH,mData.getFunctionMode());
            }
        }else if(seekProgress>=80){
            if(mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE){
                command_value=ACDef.WINDOW_TYPE_KEY_ID_FAN_AUTO;
                is_ability=mACMgr.IsAbilityByKeyId(mCurUuid,command_value);
            }else{
                command_value=ACDef.FAN_AUTO;
                is_ability=IsFANAbility(ACDef.AC_ABILITY_FAN_AUTO,mData.getFunctionMode());
            }
        }

        if(!is_ability){
            UpdateFanMode();
            Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
            return;
        }else{
            mACMgr.setCommand(mCurUuid,command_type,command_value);
        }
        showProgressDialog("", getString(R.string.processing_str));
    }

    private void SwitchSwing(){
        if(mData.getSwing() == ACDef.SWING_FIX) {
            mBtnFix.setSelected(true);
            mBtnAuto.setSelected(false);
        }else {
            mBtnFix.setSelected(false);
            mBtnAuto.setSelected(true);
        }
    }

    private void UpdateFanMode(){
        if((mData.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE ) &&
                mACMgr.IsAbilityByKeyId(mCurUuid, ACDef.WINDOW_TYPE_KEY_ID_FAN_SWITCH)){
            mFan.setVisibility(View.GONE);
            mFanToggle.setVisibility(View.VISIBLE);
        }else{
            mFan.setVisibility(View.VISIBLE);
            mFanToggle.setVisibility(View.GONE);
        }

        if(mFan.getVisibility() == View.VISIBLE) {
            if (mData.getFan() == ACDef.FAN_AUTO)
                mFanMode.setProgress(100);
            else if (mData.getFan() == ACDef.FAN_HIGH)
                mFanMode.setProgress(66);
            else if (mData.getFan() == ACDef.FAN_MEDIUM)
                mFanMode.setProgress(33);
            else if (mData.getFan() == ACDef.FAN_LOW)
                mFanMode.setProgress(0);

            for (int i = 0; i < mFanModeTitle.length; i++) {
                if (i == mData.getFan())
                    mFanModeTitle[i].setTextColor(getResources().getColor(R.color.color_blue));
                else
                    mFanModeTitle[i].setTextColor(getResources().getColor(R.color.color_white));
            }
        }
    }
    private void UpdateAdvanceData(){
        if(mData.getSubType() == ACDef.AC_SUBTYPE_SPLIT_TYPE){
            mTimer.setVisibility(View.VISIBLE);
            mAdvanceNotice.setVisibility(View.VISIBLE);
            UpdateNoticeOnOff();

            if (mData.getTimerOff() < 0) {
                mBtnTurnOff.setText("-- : --");
            } else {
                byte[] time = Utils.intToByteArray(mData.getTimerOff());

                Calendar date = Calendar.getInstance();
                date.set(Calendar.HOUR_OF_DAY, time[1]);
                date.set(Calendar.MINUTE, time[3]);

                mBtnTurnOff.setText((String) (DateFormat.format("hh:mm aa", date.getTime())));
            }
        }else{
            mTimer.setVisibility(View.GONE);
            mAdvanceNotice.setVisibility(View.INVISIBLE);
        }

        SwitchSwing();
        UpdateFanMode();
    }

    private void UpdateAdvance(ACData data){
        if(data != null) {
            String uuid=data.getAssetUuid();
            if(uuid.equals(mCurUuid)) {
                if (!data.IsIRPair())
                    finish();
                else {
                    mData = data;
                    UpdateAdvanceData();
                    dismissProgressDialog();
                }
            }
        }
    }

    private void UpdateNoticeOnOff(){
        if(mData.getNoticeSetting().getSwitchOnOff() == ACDef.POWER_ON) {
            mBtnNoticeSwitchOff.setBackground(getResources().getDrawable(R.drawable.switch_on));
        }else {
            mBtnNoticeSwitchOff.setBackground(getResources().getDrawable(R.drawable.switch_off));
        }
    }

    private void ShowNumberDialog(){

        final Dialog d = new Dialog(this,R.style.CustomNumberPickerDialog);
        d.setTitle(R.string.set_turn_off_after_hours);
        d.setContentView(R.layout.custom_number_picker_dialog);
        Button b1 = (Button) d.findViewById(R.id.btn_cancel);
        Button b2 = (Button) d.findViewById(R.id.btn_ok);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(24); // max value 100
        np.setMinValue(1);   // min value 0
        np.setWrapSelectorWheel(false);

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
                long cur_time=System.currentTimeMillis()+(np.getValue()*60*60*1000);

                final Calendar calendar=Calendar.getInstance();
                calendar.setTimeInMillis(cur_time);
                int hourOfDay=calendar.get(Calendar.HOUR_OF_DAY);
                int minute=calendar.get(Calendar.MINUTE);

                byte time_byte[]=new byte[4];
                time_byte[0]=(byte) ((hourOfDay >> 8) & 0xFF);
                time_byte[1]=(byte) (hourOfDay & 0xFF);
                time_byte[2]=(byte) ((minute >> 8) & 0xFF);
                time_byte[3]=(byte) (minute & 0xFF);;
                int time=new BigInteger(time_byte).intValue();
                mACMgr.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.TIMER_ON_OFF, 0, time);

                d.dismiss(); // dismiss the dialog
            }
        });
        d.show();
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(data != null) {
            if (((ACData)data).getAssetUuid().equals(mData.getAssetUuid()))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(data != null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_AC_DATA, data));

    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(data != null) {
            if (((ACData)data).getAssetUuid().equals(mData.getAssetUuid()) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {
        if(asset_type != DeviceTypeConvertApi.TYPE_ROOMHUB.AC)
            return;

        if(DEBUG)
            Log.d(TAG,"onCommandResult uuid="+uuid+" result="+result);

        mHandler.sendEmptyMessage(MESSAGE_DISMISS_PROGRESS_DIALOG);
        if(uuid.equals(mCurUuid)) {
            if (result < ErrorKey.Success) {
                //dismissProgressDialog();
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, Utils.getErrorCodeString(this, result)));
            }
        }
    }
}
