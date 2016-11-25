package com.quantatw.roomhub.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.quantatw.myapplication.R;

import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.FANData;
import com.quantatw.roomhub.manager.asset.manager.FANManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.FANDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

/**
 * Created by 95011613 on 2016/2/2.
 */
public class FANControllerV2 extends BaseControllerActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener {
    private static final String TAG = FANControllerV2.class.getSimpleName();

    private FANManager mFANManager;
    private FANData mFANData;

    private int[] mSwitchBtnIds = {R.id.btn_hum,R.id.btn_ion,R.id.btn_natural,R.id.btn_sleep, R.id.btn_eco};

    private Button[] mSwitchBtn = new Button[mSwitchBtnIds.length];
    private int[] mKeyId = {FANDef.KEY_ID_HUMIDIFICATION_TOGGLE,FANDef.KEY_ID_ION_TOGGLE,FANDef.KEY_ID_NATURAL_MODE,FANDef.KEY_ID_SLEEP_MODE,FANDef.KEY_ID_ESAVER_TOGGLE};
    private boolean[] mIsSupport = new boolean[mKeyId.length];

    private LinearLayout ll_swing_toggle;
    private LinearLayout ll_swing;

    private Button mBtnSwingToggle;
    private Button mBtnAuto;
    private Button mBtnFix;

    private LinearLayout ll_speed_switch;
    private LinearLayout ll_speed;

    private ImageView mBtnLower;
    private ImageView mBtnHigher;
    private ImageView mBtnSpeed;
    private ImageView mBtnPower;

    private RoomHubViewFilpper viewFlipper;

    private View mCurView;

    private boolean IsPowerToggle;
    private boolean IsFANSpeedSwitch;
    private boolean IsFANSpeed;
    private boolean IsSwingToggle;
    private int mPowerStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_controller_flipper);
        WindowManager.LayoutParams params = getWindow().getAttributes();
//        DisplayMetrics metrics = new DisplayMetrics();
        getWindow().setBackgroundDrawableResource(R.drawable.bg_fan_controller);
        mType=DeviceTypeConvertApi.TYPE_ROOMHUB.FAN;

        mFANManager=(FANManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.FAN);

        //mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mFANData=mFANManager.getFANDataByUuid(mCurUuid);
        //mRoomHubData=mFANData.getRoomHubData();
        params.x = 200;
        params.height = 700;
        params.width = 500;
        mContext=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFANManager.registerAssetsChange(this);

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
        mFANManager.unRegisterAssetsChange(this);
        if(viewFlipper != null)
            viewFlipper.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private View createView(String uuid) {

        if (DEBUG)
            Log.d(TAG, "createView uuid=" + uuid );

        if(mFANData == null) return null;

        mCurUuid=uuid;

        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mCurView = (View) layoutInflater.inflate(R.layout.fan_controller_v2, null);
        mCurView.setLongClickable(true);
        mCurView.setFocusable(true);
        mCurView.setFocusableInTouchMode(true);
        initLayout(mCurView);

        return mCurView;
    }

    private void initLayout(View v){
        for(int i=0;i<mSwitchBtnIds.length;i++) {
            mSwitchBtn[i] = (Button)v.findViewById(mSwitchBtnIds[i]);
            mSwitchBtn[i].setOnClickListener(this);
        }

        ll_swing = (LinearLayout)v.findViewById(R.id.ll_swing);
        mBtnAuto = (Button)v.findViewById(R.id.btn_swing_auto);
        mBtnAuto.setOnClickListener(this);
        mBtnFix = (Button)v.findViewById(R.id.btn_swing_fix);
        mBtnFix.setOnClickListener(this);

        ll_swing_toggle = (LinearLayout)v.findViewById(R.id.ll_swing_toggle);
        mBtnSwingToggle = (Button)v.findViewById(R.id.btn_swing_toggle);
        mBtnSwingToggle.setOnClickListener(this);

        ll_speed_switch = (LinearLayout)v.findViewById(R.id.ll_speed_switch);
        mBtnSpeed = (ImageView)v.findViewById(R.id.btn_speed);
        mBtnSpeed.setOnClickListener(this);

        ll_speed = (LinearLayout)v.findViewById(R.id.ll_speed);
        mBtnLower = (ImageView)v.findViewById(R.id.btn_lower);
        mBtnLower.setOnClickListener(this);
        mBtnHigher = (ImageView)v.findViewById(R.id.btn_higher);
        mBtnHigher.setOnClickListener(this);

        mBtnPower = (ImageView)v.findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);
        //IsPowerToggle=mFANManager.IsAbility(mCurUuid, FANDef.KEY_ID_POWER_TOGGLE);

        UpdateFANLayout();
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress=true;
         switch (v.getId()){
             case R.id.btn_hum:
             case R.id.btn_ion:
             case R.id.btn_natural:
             case R.id.btn_sleep:
             case R.id.btn_eco:
                 int idx=getIdxByResId(v.getId());
                 if(mIsSupport[idx])
                     mFANManager.setKeyId(mCurUuid,mKeyId[idx]);
                 else {
                     is_show_progress=false;
                     Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                 }
                 break;
             case R.id.btn_swing_auto:
                 if(!IsSwingToggle)
                     mFANManager.setKeyId(mCurUuid,FANDef.KEY_ID_SWING_ON);
                 break;
             case R.id.btn_swing_fix:
                 if(!IsSwingToggle)
                     mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_SWING_OFF);
                 break;
             case R.id.btn_swing_toggle:
                 if(IsSwingToggle)
                     mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_SWING_TOGGLE);
                 break;
             case R.id.btn_speed:
                if(IsFANSpeedSwitch)
                    mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_FANSPEED_SWITCH);
                break;
             case R.id.btn_lower:
                 if(IsFANSpeed)
                     mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_FANSPEED_DECREASE);
                 break;
             case R.id.btn_higher:
                 if(IsFANSpeed)
                     mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_FANSPEED_INCREASE);
                 break;
             case R.id.btn_power:
                 if(IsPowerToggle){
                     mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_POWER_TOGGLE);
                 }else{
                    if(mPowerStatus == FANDef.POWER_ON){
                        mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_POWER_OFF);
                    }else if(mPowerStatus == FANDef.POWER_OFF)
                        mFANManager.setKeyId(mCurUuid, FANDef.KEY_ID_POWER_ON);
                 }
                 break;
        }
        if(is_show_progress) {
            Log.d(TAG, "showProgressDialog");
            if(isShowing() == false)
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }
    }

    private int getIdxByResId(int res_id){

        for(int i=0;i<mSwitchBtnIds.length;i++){
            if(mSwitchBtnIds[i] == res_id)
                return i;
        }
        return 0;
    }

    private void UpdateFANLayout(){

        for(int i=0;i<mSwitchBtnIds.length;i++) {
            mIsSupport[i]=mFANManager.IsAbility(mCurUuid, mKeyId[i]);
        }
        IsSwingToggle=mFANManager.IsAbility(mCurUuid, FANDef.KEY_ID_SWING_TOGGLE);
        if(IsSwingToggle){
            ll_swing_toggle.setVisibility(View.VISIBLE);
            ll_swing.setVisibility(View.GONE);
        }else{
            ll_swing_toggle.setVisibility(View.GONE);
            if(mFANManager.IsAbility(mCurUuid, FANDef.KEY_ID_SWING_ON))
                ll_swing.setVisibility(View.VISIBLE);
            else
                ll_swing.setVisibility(View.GONE);
        }


        IsFANSpeed=mFANManager.IsAbility(mCurUuid,FANDef.KEY_ID_FANSPEED_INCREASE);
        IsFANSpeedSwitch=mFANManager.IsAbility(mCurUuid, FANDef.KEY_ID_FANSPEED_SWITCH);

        if(IsFANSpeed){
            ll_speed_switch.setVisibility(View.GONE);
            ll_speed.setVisibility(View.VISIBLE);
        }else if(IsFANSpeedSwitch){
            ll_speed_switch.setVisibility(View.VISIBLE);
            ll_speed.setVisibility(View.GONE);
        }else{
            ll_speed_switch.setVisibility(View.GONE);
            ll_speed.setVisibility(View.GONE);
        }

        IsPowerToggle=mFANManager.IsAbility(mCurUuid, FANDef.KEY_ID_POWER_TOGGLE);
        if(!IsPowerToggle)
            mPowerStatus=mFANData.getPowerStatus();
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(asset_data != null) {
            FANData fan_data=(FANData)asset_data;
            if (mCurUuid.equalsIgnoreCase(fan_data.getAssetUuid())) {
                if (!fan_data.IsIRPair())
                    finish();
                else {
                    mFANData = fan_data;
                    UpdateFANLayout();
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
}
