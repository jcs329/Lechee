package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by 95011613 on 2015/10/5.
 */
public class RoomHubControllerFlipper extends BaseControllerActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener {
    private static final String TAG = RoomHubControllerFlipper.class.getSimpleName();
    private ACManager mACManager;

    private int[] mFunModeBtnIds = {R.id.btn_cooler,R.id.btn_heater,R.id.btn_dehum,R.id.btn_fan};
    private ImageView[] mFunModeBtn = new ImageView[mFunModeBtnIds.length];

    private static  TextView mTxtFunMode;
    private View mControllerTemp;
    private View mControllerDehum;
    private View mControllerFan;

    private TextView mTxtTemp;
    private ImageView mBtnLower;
    private ImageView mBtnHigher;
    private ImageView mBtnLeft;
    private ImageView mBtnRight;

    private Button mBtnSuggestTemp;
    private ImageView mBtnPower;
    private ACData mACData;

    private Button mBtnSchedule;
    private Button mBtnAdvance;

    private ImageView mIvDehum;
    private ImageView mIvFan;

    private int mCurFunMode= ACDef.FUN_MODE_COOL;
    private int mCurPowerStatus=ACDef.POWER_OFF;
    private int mCurTemp;
    private int mCurTempF;
    private int mCurTempC;
    private int mSuggestTemp;

    public static final int LOWER_TEMP  =   0;
    public static final int HIGHER_TEMP =   1;
    public static final int SUGGEST_TEMP=   2;

    private RoomHubViewFilpper viewFlipper;

    private View mCurView;
    private int[] mLimitTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_controller_flipper);

        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.AC;

        mACManager=(ACManager)mRoomHubMgr.getAssetDeviceManager(mType);

        //mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mACData=mACManager.getCurrentACDataByUuid(mCurUuid);
        //mRoomHubData=mACData.getRoomHubData();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = 200;
        params.height = 700;
        params.width = 500;
        mContext=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mACManager.registerAssetsChange(this);

        viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
        viewFlipper.setLongClickable(true);
        viewFlipper.setClickable(true);
        viewFlipper.setOnViewFlipperListener(this);

        View v=createView(mCurUuid);
        if(v != null)
            viewFlipper.addView(v, 0);
        else
            finish();

       //initLayout();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mACManager.unRegisterAssetsChange(this);
        if(viewFlipper != null)
            viewFlipper.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private View createView(String uuid) {
        if(mACData == null) return null;

        mCurUuid=uuid;
        mCurTemp=mACData.getTemperature();
        mCurFunMode=mACData.getFunctionMode();

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mCurView = (View) layoutInflater.inflate(R.layout.room_hub_controller, null);
        mCurView.setLongClickable(true);
       // mCurView.setClickable(true);
        mCurView.setFocusable(true);
        mCurView.setFocusableInTouchMode(true);
        initLayout(mCurView);
        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        return mCurView;
    }

    private void initLayout(View v){

        mControllerTemp=(View)v.findViewById(R.id.controller_temp);
        mControllerDehum=(View)v.findViewById(R.id.controller_dehum);
        mControllerFan=(View)v.findViewById(R.id.controller_fan);

        for(int i=0;i<mFunModeBtnIds.length;i++) {
            mFunModeBtn[i] = (ImageView)v.findViewById(mFunModeBtnIds[i]);
            mFunModeBtn[i].setOnClickListener(this);
        }

        mTxtFunMode=(TextView)v.findViewById(R.id.txt_fun_mode);

        mTxtTemp=(TextView)v.findViewById(R.id.txt_controller_temp);

        mTxtTemp.setText(String.valueOf(Utils.getTemp(this, Utils.getTemp(mContext,mCurTemp))));
        mCurTempF = (int) Utils.toFahrenheit(mCurTemp);
        mCurTempC = mCurTemp;
//        if(Utils.isFahrenheit(mContext))
//            mTxtTemp.setText(String.valueOf(mCurTempF));
//        else if(Utils.isCelsius(mContext))
//            mTxtTemp.setText(String.valueOf(mCurTempC));
        mBtnLower=(ImageView)v.findViewById(R.id.btn_lower);
        mBtnLower.setOnClickListener(this);
        mBtnHigher=(ImageView)v.findViewById(R.id.btn_higher);
        mBtnHigher.setOnClickListener(this);

        mBtnLeft=(ImageView)v.findViewById(R.id.controller_left_btn);
        mBtnLeft.setOnClickListener(this);
        mBtnLeft.setVisibility(View.GONE);
        mBtnRight=(ImageView)v.findViewById(R.id.controller_right_btn);
        mBtnRight.setOnClickListener(this);
        mBtnRight.setVisibility(View.GONE);

        mSuggestTemp=mACManager.getRecommendTemp(mCurUuid);
        mBtnSuggestTemp=(Button)v.findViewById(R.id.btn_suggest_temp);
        mBtnSuggestTemp.setText(String.valueOf((int) Utils.getTemp(this, mSuggestTemp)) + "°");
        mBtnSuggestTemp.setOnClickListener(this);
        mBtnPower=(ImageView)v.findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);
        mCurPowerStatus=mACData.getPowerStatus();

        mBtnSchedule = (Button) v.findViewById(R.id.btn_schedule);
        if(mACData.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE){
            mBtnSchedule.setVisibility(View.INVISIBLE);
        }else{
            mBtnSchedule.setVisibility(View.VISIBLE);
        }
        mBtnSchedule.setOnClickListener(this);
        mBtnAdvance=(Button)v.findViewById(R.id.btn_advance);
        mBtnAdvance.setOnClickListener(this);

        mIvDehum=(ImageView)v.findViewById(R.id.iv_dehum);
        mIvFan=(ImageView)v.findViewById(R.id.iv_fan);
      //  ChangeArrowBtn();
        SwitchPowerBtn();
        SwitchFunModeBtn();
    }

    private void setNotifyTempDelta(int fun_mode,int power_status,int temp){
        int notify_delta;
        if((power_status == ACDef.POWER_ON) &&
                ((fun_mode == ACDef.FUN_MODE_COOL) || (fun_mode == ACDef.FUN_MODE_HEAT))) {
            NoticeSetting notice_setting = mACData.getNoticeSetting();
            if (notice_setting != null) {
                if (notice_setting.getIsDefaultDelta() == 1) {
                    int sensor_temp = (int) mRoomHubData.getSensorTemp();

                    if (sensor_temp == ErrorKey.SENSOR_TEMPERATURE_INVALID) {
                        notify_delta = mContext.getResources().getInteger(R.integer.config_notification_temp_delta);
                    } else {
                        notify_delta = (int) Math.floor(Math.abs(sensor_temp - temp) / 2);
                        if (notify_delta < 1)
                            notify_delta = 1;
                        else if (notify_delta > 5)
                            notify_delta = 5;
                    }
                    notice_setting.setNoticeDelta(notify_delta);
                    mACData.setNoticeSetting(notice_setting);
                }
            }
        }
    }

    private boolean checkTempRange(int curTemp, int[] TempLimitRange){
        int temp = (int) Utils.getTemp(mContext, curTemp);
        int TempLimitMinRange = (int) Utils.getTemp(mContext, TempLimitRange[0]);
        int TempLimitMaxRange = (int) Utils.getTemp(mContext, TempLimitRange[1]);
        if(temp >= TempLimitMinRange && temp <= TempLimitMaxRange)
            return true;
        return false;
    }

    @Override
    public void onClick(View v) {
         boolean is_show_progress=true;
         switch (v.getId()){
            case R.id.btn_cooler:
                is_show_progress=setFunMode(ACDef.FUN_MODE_COOL);
                break;
            case R.id.btn_heater:
                is_show_progress=setFunMode(ACDef.FUN_MODE_HEAT);
                break;
            case R.id.btn_dehum:
                is_show_progress=setFunMode(ACDef.FUN_MODE_DRY);
                break;
            case R.id.btn_fan:
                is_show_progress=setFunMode(ACDef.FUN_MODE_FAN);
                break;
            case R.id.btn_power:
                if(mACData.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE){
                    is_show_progress=false;
                    ProcessToggle();
                    //mACManager.setPowerStatus(mCurUuid, ACDef.POWER_TOGGLE);
                }else{
                    if(mCurPowerStatus == ACDef.POWER_ON) {
                        setNotifyTempDelta(mACData.getFunctionMode(), mCurPowerStatus, mACData.getTemperature());
                        mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.POWER, ACDef.POWER_OFF);
                    }else {
                        mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.POWER, ACDef.POWER_ON);
                    }
                }
                break;
            case R.id.btn_lower:
                is_show_progress=setTemp(LOWER_TEMP);
                break;
            case R.id.btn_higher:
                is_show_progress=setTemp(HIGHER_TEMP);
                break;
             case R.id.btn_suggest_temp:
                 is_show_progress=setTemp(SUGGEST_TEMP);
                 break;
             case R.id.btn_schedule:
                 if (!getAccountManager().isLogin()) {
                     Utils.ShowLoginActivity(this, RoomHubMainPage.class);
                     return;
                 }else{
                     if(!mRoomHubData.IsOwner()) {
                         Toast.makeText(this, R.string.only_owner_use, Toast.LENGTH_SHORT).show();
                         return;
                     }
                 }
             case R.id.btn_advance:
                 is_show_progress=false;
                 if(mACData.getSubType() != ACDef.AC_SUBTYPE_TOGGLE_TYPE) {
                     if ((mCurPowerStatus == ACDef.POWER_OFF) && (v.getId() == R.id.btn_advance)) {
                         Toast.makeText(this, R.string.prompt_ac_power_on, Toast.LENGTH_SHORT).show();
                         return;
                     }
                 }

                 Intent intent = new Intent();
                 if (v.getId() == R.id.btn_schedule)
                     intent.setClass(this, ScheduleListActivity.class);
                 else
                     intent.setClass(this, AdvanceSettingActivity.class);
                 Bundle bundle = new Bundle();
                 //bundle.putParcelable(RoomHubManager.KEY_ROOMHUB_DATA, mData);
                 bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
                 intent.putExtras(bundle);
                 startActivity(intent);

                 break;
             case R.id.controller_left_btn:
                 is_show_progress=false;
                 if(viewFlipper!=null)
                    viewFlipper.flingToPrevious();
                 break;
             case R.id.controller_right_btn:
                 is_show_progress=false;
                 if(viewFlipper!=null)
                    viewFlipper.flingToNext();
                 break;
        }
        if(is_show_progress) {
            if(isShowing() == false)
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }
    }

    private boolean setFunMode(int fun_mode){
        int[] limit_temp=mACManager.getLimitTemp(mCurUuid, fun_mode);

        if((limit_temp[0] < 0) && (limit_temp[1] < 0)) {
            Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
            return false;
        }

        //get profile data when switch fun mode
        ACData data=mACManager.getACDataByFunMode(mCurUuid, fun_mode);
        setNotifyTempDelta(fun_mode, mACData.getPowerStatus(), data.getTemperature());

        mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.FUN_MODE, fun_mode);

        return true;
    }

    private void SwitchFunModeBtn(){
        mTxtFunMode.setText(getFunModeStr());

        int funmodbtn_idx=getCurFunModeBtnIdx();

        for(int i=0;i<mFunModeBtn.length;i++){
            if(i==funmodbtn_idx) {
                mFunModeBtn[i].setSelected(true);
            }else{
                mFunModeBtn[i].setSelected(false);
            }
        }

        if(mCurFunMode == ACDef.FUN_MODE_COOL ||
                mCurFunMode == ACDef.FUN_MODE_HEAT){
            mControllerTemp.setVisibility(View.VISIBLE);
            mControllerDehum.setVisibility(View.GONE);
            mControllerFan.setVisibility(View.GONE);
            if(mCurFunMode ==ACDef.FUN_MODE_COOL)
                getWindow().setBackgroundDrawableResource(R.drawable.background_cooler);
            else
                getWindow().setBackgroundDrawableResource(R.drawable.background_heater);
        }else if(mCurFunMode == ACDef.FUN_MODE_DRY){
            mControllerTemp.setVisibility(View.GONE);
            mControllerDehum.setVisibility(View.VISIBLE);
            mControllerFan.setVisibility(View.GONE);
            getWindow().setBackgroundDrawableResource(R.drawable.background_dehum);
        }else if(mCurFunMode == ACDef.FUN_MODE_FAN){
            mControllerTemp.setVisibility(View.GONE);
            mControllerDehum.setVisibility(View.GONE);
            mControllerFan.setVisibility(View.VISIBLE);
            getWindow().setBackgroundDrawableResource(R.drawable.background_fan);
        }
        mLimitTemp=mACManager.getLimitTemp(mCurUuid, mCurFunMode);
    }

    private String getFunModeStr(){
        String fun_mode_str="";

        switch (mCurFunMode){
            case ACDef.FUN_MODE_COOL:
                fun_mode_str=getResources().getString(R.string.cooler);
                break;
            case ACDef.FUN_MODE_HEAT:
                fun_mode_str=getResources().getString(R.string.heater);
                break;
            case ACDef.FUN_MODE_DRY:
                fun_mode_str=getResources().getString(R.string.dehumidifier);
                break;
            case ACDef.FUN_MODE_FAN:
                fun_mode_str=getResources().getString(R.string.fan);
                break;
        }
        return fun_mode_str;
    }

    private void SwitchPowerBtn(){
        boolean is_enable=true;

        mBtnPower.setSelected(true);
        if(mACData.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE){
            mBtnPower.setImageResource(R.drawable.fan_power_btn_selector);
        }else{
            if(mCurPowerStatus == ACDef.POWER_ON){
                mBtnPower.setImageResource(R.drawable.power_btn_on_selector);
//            }else if(mCurPowerStatus == ACDef.POWER_OFF) {
            }else {
                is_enable = false;
                mBtnPower.setImageResource(R.drawable.power_btn_off_selector);
            }
        }

        if(is_enable) {
            for(int i=0;i<mFunModeBtn.length;i++){
                mFunModeBtn[i].setBackground(getResources().getDrawable(R.drawable.controller_btn_selector));
            }
            if(Utils.isFahrenheit(mContext)) {
                if(mCurTemp != (int)Utils.toCelsius(mCurTempF)) {
                    mCurTempF = (int) Utils.getTemp(mContext,mCurTemp);
                    mTxtTemp.setText(String.valueOf(mCurTempF));
                }else
                    mTxtTemp.setText(String.valueOf(mCurTempF));
            }
            else if(Utils.isCelsius(mContext))
                mTxtTemp.setText(String.valueOf(mCurTemp));

//            if(Utils.isFahrenheit(mContext) && (mCurTempF != Utils.toFahrenheit(mCurTemp))) {
//                mCurTempF = (int) Utils.getTemp(mContext,mCurTemp);
//                mTxtTemp.setText(String.valueOf(mCurTempF));
//            }

            //mTxtTemp.setText(String.valueOf((int) Utils.getTemp(this, mCurTemp)));
            mBtnSuggestTemp.setText(String.valueOf((int) Utils.getTemp(this, mSuggestTemp)) + "°");
            mBtnSuggestTemp.setBackgroundResource(R.drawable.suggest_temp_btn_selector);
            mBtnSuggestTemp.setEnabled(true);
            mBtnLower.setEnabled(true);
            mBtnHigher.setEnabled(true);
            if(mCurFunMode == ACDef.FUN_MODE_DRY)
                mIvDehum.setBackground(getResources().getDrawable(R.drawable.icon_dehumidifier_big_02));
            if(mCurFunMode == ACDef.FUN_MODE_FAN)
                mIvFan.setBackground(getResources().getDrawable(R.drawable.icon_fan_big_02));
            //mBtnAdvance.setEnabled(true);
        }else {

            for(int i=0;i<mFunModeBtn.length;i++){
                mFunModeBtn[i].setBackground(getResources().getDrawable(R.drawable.funmode_btn_selector));
            }
            mTxtTemp.setText(" -- ");
            mBtnSuggestTemp.setText("");
            mBtnSuggestTemp.setBackgroundResource(R.drawable.btn_suggest_off);
            mBtnSuggestTemp.setEnabled(false);
            mBtnLower.setEnabled(false);
            mBtnHigher.setEnabled(false);
            if(mCurFunMode == ACDef.FUN_MODE_DRY)
                mIvDehum.setBackground(getResources().getDrawable(R.drawable.icon_dehumidifier_big_02_off));
            if(mCurFunMode == ACDef.FUN_MODE_FAN)
                mIvFan.setBackground(getResources().getDrawable(R.drawable.icon_fan_big_02_off));
            //mBtnAdvance.setEnabled(false);
        }
    }
    int BeforeTempF;
    private boolean setTemp(int type){
        int temp=mCurTemp;

        //focus on Limit Max and Min TEP
        if(Utils.isFahrenheit(mContext)) {
            if (mCurTempF == (int) (Utils.toFahrenheit(mLimitTemp[1]) - 1) && temp == mLimitTemp[1] && type == HIGHER_TEMP) {
                mCurTempF++;
                setNotifyTempDelta(mACData.getFunctionMode(), mACData.getPowerStatus(), temp);
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.TEMP,temp);
                return true;
            } else if (mCurTempF == (int) (Utils.toFahrenheit(mLimitTemp[0]) + 1) && temp == mLimitTemp[0] && type == LOWER_TEMP) {
                mCurTempF--;
                setNotifyTempDelta(mACData.getFunctionMode(), mACData.getPowerStatus(), temp);
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.TEMP, temp);
                return true;
            }
        }

        BeforeTempF = mCurTempF;
        int BeforeTempC = (int)Utils.toCelsius(mCurTempF);
        switch (type){
            case LOWER_TEMP:
                mCurTempF--;
                mCurTempC=(int)Utils.toCelsius(mCurTempF);
                temp--;
                break;
            case HIGHER_TEMP:
                mCurTempF++;
                mCurTempC=(int)Utils.toCelsius(mCurTempF);
                temp++;
                break;
            case SUGGEST_TEMP:
                temp=mSuggestTemp;
                mCurTempC = mSuggestTemp;
                mCurTempF = (int) Utils.toFahrenheit(mSuggestTemp);
                break;
        }

        if(checkTempRange(temp,mLimitTemp)){
            if(Utils.isFahrenheit(mContext)){
                temp = (int) Utils.toCelsius(mCurTempF);
            }

            setNotifyTempDelta(mACData.getFunctionMode(), mACData.getPowerStatus(), temp);
            mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.TEMP, temp);
            return true;
        }else{
            mCurTempF = BeforeTempF;
            //mCurTempC=(int)Utils.toCelsius(mCurTempF);
            String str = String.format(getResources().getString(R.string.schedule_warning), (int) Utils.getTemp(this, mLimitTemp[0]) + "~" + (int) Utils.getTemp(this, mLimitTemp[1]));
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private int getCurFunModeBtnIdx(){
        int idx=0;
        switch (mCurFunMode){
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

    protected void Controller_UpdateAssetData(Object asset_data){
        if(asset_data != null) {
            ACData ac_data=(ACData)asset_data;
            String uuid=ac_data.getAssetUuid();
            if(uuid.equals(mCurUuid)){
                if(!ac_data.IsIRPair())
                    finish();
                else{
                    int[] limit_temp = mACManager.getLimitTemp(mCurUuid, ac_data.getFunctionMode());

                    if ((limit_temp[0] < 0) && (limit_temp[1] < 0)) {
                        Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mACData = ac_data;
                    mCurFunMode = mACData.getFunctionMode();
                    mCurTemp = mACData.getTemperature();
                    mCurPowerStatus = mACData.getPowerStatus();

                    log(TAG, "Controller_UpdateAssetData ac mCurFunMode=" + mCurFunMode + " mCurTemp=" + mCurTemp + " mCurPowerStatus=" + mCurPowerStatus);

                    if(mACData.getSubType() == ACDef.AC_SUBTYPE_TOGGLE_TYPE)
                        mBtnSchedule.setVisibility(View.INVISIBLE);
                    else
                        mBtnSchedule.setVisibility(View.VISIBLE);
                    SwitchFunModeBtn();
                    SwitchPowerBtn();

                    mHandler.sendEmptyMessage(MESSAGE_DISMISS_PROGRESS_DIALOG);
                }
            }
        }
    }

    @Override
    public View getNextView() {
        return null;
    }

    @Override
    public View getPreviousView() {
        return null;
    }

    private void ProcessToggle() {
        if(mACData.IsRemind()){
            ShowToggleDialog();
        }else
            mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.POWER,ACDef.POWER_TOGGLE);
    }

    private void ShowToggleDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.ac_toggle_dialog);
        final CheckBox cb_remind=(CheckBox)dialog.findViewById(R.id.cb_remind);

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_ok);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cb_remind.isChecked()){
                    mACData.AddToggleNotRemind();
                }
                mACManager.setCommand(mCurUuid, AssetDef.COMMAND_TYPE.POWER,ACDef.POWER_TOGGLE);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
