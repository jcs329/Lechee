package com.quantatw.roomhub.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.AirPurifierData;
import com.quantatw.roomhub.manager.asset.manager.AirPurifierManager;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.AirPurifierDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.util.HashMap;
import java.util.Iterator;

public class AirPurifierIRActivity extends BaseControllerActivity implements View.OnClickListener {
    private static final String TAG = AirPurifierIRActivity.class.getSimpleName();

    private AirPurifierManager mAirPurifierMgr;
    private AirPurifierData mAirPurifierData;

    private View mIONLayout,mHumidityLayout;
    private View mAirDirectionLayout,mWindLayout,mSwingLayout;
    private View mSpeedMainLayout,mSpeedLayout,mSpeedSwitchLayout;

    private ImageView mBtnION,mBtnHumidity;
    private Button mBtnSwingOn,mBtnSwingOff;
    private ImageView mBtnLower,mBtnHigher,mBtnSpeed;
    private ImageView mBtnPower;

    private class FunModeInfo {

        private int keyId;
        private Button btn_view;
        private boolean is_support;

        private FunModeInfo(int keyId,Button btn_view) {
            this.keyId = keyId;
            this.btn_view = btn_view;
        }
    }

    private HashMap<Integer,FunModeInfo> mFunModeList=new HashMap<Integer,FunModeInfo>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_air_purifier_ir);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_air_purifier);
        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER;

        mAirPurifierMgr=(AirPurifierManager)mRoomHubMgr.getAssetDeviceManager(mType);
        //mCurUuid = getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mAirPurifierData=mAirPurifierMgr.getAirPurifierDataByUuid(mRoomHubUuid, mCurUuid);
        //mRoomHubData=mAirPurifierData.getRoomHubData();

        mFunModeList.put(R.id.btn_normal, new FunModeInfo(AirPurifierDef.KEY_ID_IR_NORMAL_MODE, (Button) findViewById(R.id.btn_normal)));
        mFunModeList.put(R.id.btn_sleep,new FunModeInfo(AirPurifierDef.KEY_ID_IR_SLEEP_MODE, (Button)findViewById(R.id.btn_sleep)));
        mFunModeList.put(R.id.btn_natural, new FunModeInfo(AirPurifierDef.KEY_ID_IR_NATURAL_MODE, (Button) findViewById(R.id.btn_natural)));
        mFunModeList.put(R.id.btn_special, new FunModeInfo(AirPurifierDef.KEY_ID_IR_SPECIAL_MODE, (Button) findViewById(R.id.btn_special)));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = 200;
        params.height = 700;
        params.width = 500;
        mContext=this;
        initLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAirPurifierData == null) return;

        mAirPurifierMgr.registerAssetsChange(this);

        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        UpdateLayoutData();
    }

    private void initLayout(){
        mIONLayout = findViewById(R.id.ion_layout);
        mHumidityLayout = findViewById(R.id.humidity_layout);
        mAirDirectionLayout = findViewById(R.id.air_direction_layout);
        mWindLayout = findViewById(R.id.wind_layout);
        mSwingLayout = findViewById(R.id.swing_layout);

        mSpeedMainLayout = findViewById(R.id.ll_speed);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mSpeedSwitchLayout = findViewById(R.id.speed_switch_layout);

        for (Iterator<FunModeInfo> it = mFunModeList.values().iterator(); it.hasNext(); ) {
            FunModeInfo fun_mode_info = it.next();
            fun_mode_info.btn_view.setOnClickListener(this);
            fun_mode_info.is_support=mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, fun_mode_info.keyId);
        }

        mBtnION = (ImageView) findViewById(R.id.btn_ion);
        mBtnION.setOnClickListener(this);

        mBtnHumidity = (ImageView) findViewById(R.id.btn_humidity);
        mBtnHumidity.setOnClickListener(this);

        mBtnSwingOn = (Button) findViewById(R.id.btn_swing_auto);
        mBtnSwingOn.setOnClickListener(this);

        mBtnSwingOff = (Button) findViewById(R.id.btn_swing_fix);
        mBtnSwingOff.setOnClickListener(this);

        mBtnLower = (ImageView) findViewById(R.id.btn_lower);
        mBtnLower.setOnClickListener(this);

        mBtnHigher = (ImageView) findViewById(R.id.btn_higher);
        mBtnHigher.setOnClickListener(this);

        mBtnSpeed = (ImageView) findViewById(R.id.btn_speed);
        mBtnSpeed.setOnClickListener(this);

        mBtnPower = (ImageView) findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);
    }

    private void UpdateLayoutData(){

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_ION_TOGGLE)){
            mIONLayout.setVisibility(View.VISIBLE);
        }else{
            mIONLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_HUMIDIFICATION_TOGGLE)){
            mHumidityLayout.setVisibility(View.VISIBLE);
        }else{
            mHumidityLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_ON) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_OFF)){
            mAirDirectionLayout.setVisibility(View.VISIBLE);
            mWindLayout.setVisibility(View.VISIBLE);
            mSwingLayout.setVisibility(View.GONE);
        }else{
            if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_TOGGLE)) {
                mAirDirectionLayout.setVisibility(View.VISIBLE);
                mWindLayout.setVisibility(View.GONE);
                mSwingLayout.setVisibility(View.VISIBLE);
            }else{
                mAirDirectionLayout.setVisibility(View.GONE);
            }
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_INCREASE) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_DECREASE)){
            mSpeedMainLayout.setVisibility(View.VISIBLE);
            mSpeedLayout.setVisibility(View.VISIBLE);
            mSpeedSwitchLayout.setVisibility(View.GONE);
        }else{
            if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_SWITCH)) {
                mSpeedMainLayout.setVisibility(View.VISIBLE);
                mSpeedSwitchLayout.setVisibility(View.VISIBLE);
                mSpeedLayout.setVisibility(View.GONE);
            }else {
                mSpeedMainLayout.setVisibility(View.GONE);
            }
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_POWER_ON) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_POWER_OFF)){
            mBtnPower.setVisibility(View.VISIBLE);
            if(mAirPurifierData.getPowerStatus() == AirPurifierDef.POWER_ON){
                mBtnPower.setImageResource(R.drawable.power_btn_on_selector);
            }else {
                mBtnPower.setImageResource(R.drawable.power_btn_off_selector);
            }
        }else{
            if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_IR_POWER_TOGGLE)){
                mBtnPower.setVisibility(View.VISIBLE);
                mBtnPower.setImageResource(R.drawable.fan_power_btn_selector);
            }else
                mBtnPower.setVisibility(View.INVISIBLE);
        }
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(mCurUuid.equalsIgnoreCase(((AirPurifierData)asset_data).getAssetUuid())) {
            mAirPurifierData=(AirPurifierData)asset_data;
            UpdateLayoutData();
        }
    }

    @Override
    protected void onPause() {
        mRoomHubMgr.unRegisterRoomHubChange(this);
        if(mAirPurifierMgr != null)
            mAirPurifierMgr.unRegisterAssetsChange(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress=true;
        switch (v.getId()) {
            case R.id.btn_normal:
            case R.id.btn_sleep:
            case R.id.btn_natural:
            case R.id.btn_special:
                FunModeInfo fun_mode_info=mFunModeList.get(v.getId());
                if(fun_mode_info != null) {
                    if (fun_mode_info.is_support) {
                        mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, fun_mode_info.keyId);
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.controller_warning), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
            case R.id.btn_ion:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_ION_TOGGLE);
                break;
            case R.id.btn_humidity:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_HUMIDIFICATION_TOGGLE);
                break;
            case R.id.btn_swing:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_TOGGLE);
                break;
            case R.id.btn_swing_auto:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_ON);
                break;
            case R.id.btn_swing_fix:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_SWING_OFF);
                break;
            case R.id.btn_lower:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_DECREASE);
                break;
            case R.id.btn_higher:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_INCREASE);
                break;
            case R.id.btn_speed:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_FAN_SPEED_SWITCH);
                break;
            case R.id.btn_power:
                if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_POWER_ON) ||
                        mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_POWER_OFF)) {
                    int value = mAirPurifierData.getPowerStatus();
                    int keyId = (value==AirPurifierDef.POWER_ON?AirPurifierDef.KEY_ID_POWER_OFF:AirPurifierDef.KEY_ID_POWER_ON);
                    mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,keyId);
                }else{
                    mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_IR_POWER_TOGGLE);
                }
                break;
        }
        if(is_show_progress) {
            Log.d(TAG, "showProgressDialog");
            if(isShowing() == false)
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }
    }
}
