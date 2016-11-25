package com.quantatw.roomhub.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.quantatw.myapplication.R;

import com.quantatw.roomhub.manager.asset.manager.AirPurifierData;
import com.quantatw.roomhub.manager.asset.manager.AirPurifierManager;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.AirPurifierDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.AQIApi;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by erin on 3/9/16.
 * Updated by cherry on 2016/06/06
 */
public class AirPurifierBTActivity extends BaseControllerActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener{
    private final String TAG=AirPurifierBTActivity.class.getSimpleName();
    private boolean DEBUG = true;

    private AirPurifierManager mAirPurifierMgr;
    private AirPurifierData mAirPurifierData;
    private RoomHubViewFilpper viewFlipper;

    private View mCurView;
    private View mMainLayout, mOffLayout;
    private View mAutoLayout, mTimeMainLayout,mWindMainLayout;
    private View mIONLayout,mWindLayout,mUVLayout;

    private TextView mUpdateTimeTextView;
    private Button mAirQualityBtn;

    private TextView mTimeSetTextView;
    private ImageView mTimeSetImageView, mAutoImageView, mUvImageView, mIONImageView,mWindImageView;
    private ImageView mFanSpeedImageView, mPowerImageView;

    private static final int AIR_QUALITY_GOOD   =   0;
    private static final int AIR_QUALITY_NORMAL =   1;
    private static final int AIR_QUALITY_DANGER =   2;

    private int[] mFanLevelImageRes =
                    {R.drawable.lv_auto_windspeed,
                    R.drawable.lv_1_windspeed,
                    R.drawable.lv_2_windspeed,
                    R.drawable.lv_3_windspeed,
                    R.drawable.lv_4_windspeed,
                    R.drawable.lv_5_windspeed,
                    R.drawable.lv_6_windspeed,
                    R.drawable.lv_7_windspeed,
                    R.drawable.lv_8_windspeed,
                    R.drawable.lv_9_windspeed};

    private void getViews(View v) {
        mMainLayout = v.findViewById(R.id.air_main_layout);
        mOffLayout = v.findViewById(R.id.air_off_layout);
        mAutoLayout = v.findViewById(R.id.auto_layout);
        mTimeMainLayout = v.findViewById(R.id.time_main_layout);
        mWindMainLayout = v.findViewById(R.id.wind_main_layout);
        mIONLayout = v.findViewById(R.id.ion_layout);
        mWindLayout = v.findViewById(R.id.wind_layout);
        mUVLayout = v.findViewById(R.id.uv_layout);

        mUpdateTimeTextView = (TextView) v.findViewById(R.id.txt_update_time);
        mAirQualityBtn = (Button) v.findViewById(R.id.img_air_quality);

        mAutoImageView = (ImageView)v.findViewById(R.id.air_auto_set);
        mAutoImageView.setOnClickListener(this);
        mTimeSetTextView = (TextView)v.findViewById(R.id.air_time_text);
        mTimeSetImageView = (ImageView)v.findViewById(R.id.air_time_set);
        mTimeSetImageView.setOnClickListener(this);

        mFanSpeedImageView = (ImageView)v.findViewById(R.id.air_fan_speed);

        mIONImageView = (ImageView)v.findViewById(R.id.air_ion_set);
        mIONImageView.setOnClickListener(this);
        mWindImageView = (ImageView)v.findViewById(R.id.air_wind_set);
        mWindImageView.setOnClickListener(this);
        mUvImageView = (ImageView)v.findViewById(R.id.air_uv_set);
        mUvImageView.setOnClickListener(this);

        mPowerImageView = (ImageView)v.findViewById(R.id.btn_power);
        mPowerImageView.setOnClickListener(this);

        Button schedule_btn = (Button) v.findViewById(R.id.btn_schedule);
        schedule_btn.setVisibility(View.INVISIBLE);

        Button advance_btnw = (Button) v.findViewById(R.id.btn_advance);
        advance_btnw.setVisibility(View.INVISIBLE);
    }

    private View createView(String uuid) {
        log(TAG, "createView uuid=" + uuid );

        if(mAirPurifierData == null) return null;

        mCurUuid=uuid;

        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        mCurView = (View) layoutInflater.inflate(R.layout.activity_air_purifier_bt, null);
//        mCurView.setLongClickable(true);
        mCurView.setFocusable(true);
        mCurView.setFocusableInTouchMode(true);

        updateViews(mCurView);

        return mCurView;
    }

    private void updateViews(View v) {
        getViews(v);
        refreshLayout(mAirPurifierData);
    }

    private void refreshLayout(AirPurifierData airPurifierData) {
        View view = viewFlipper.getCurrentView();
        getViews(view == null ? mCurView : view);

        int power=airPurifierData.getPowerStatus();
        mPowerImageView.setSelected(true);
        if(power == AirPurifierDef.POWER_ON){
            mPowerImageView.setImageResource(R.drawable.power_btn_on_selector);
        }else {
            mPowerImageView.setImageResource(R.drawable.power_btn_off_selector);
        }

        if(power == 0) { //OFF
            mOffLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.GONE);
            //mPowerImageView.setSelected(false);
            mPowerImageView.setOnClickListener(this);
            return;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        mUpdateTimeTextView.setText(format.format(new Date(airPurifierData.getUpdateTime())));

        // ON:
        mOffLayout.setVisibility(View.GONE);
        mMainLayout.setVisibility(View.VISIBLE);

        AQIApi.AQI_CATEGORY aqi_category= AQIApi.getAQICategoryByPM25Value(mAirPurifierData.getQuality());
        if(aqi_category == AQIApi.AQI_CATEGORY.DANGER) {
            mAirQualityBtn.setBackgroundResource(R.drawable.bg_aq_info_bad);
            mAirQualityBtn.setText(R.string.air_quality_danger);
        }else if(aqi_category == AQIApi.AQI_CATEGORY.NORMAL){
            mAirQualityBtn.setBackgroundResource(R.drawable.bg_aq_info_normal);
            mAirQualityBtn.setText(R.string.air_quality_normal);
        }else{
            mAirQualityBtn.setBackgroundResource(R.drawable.bg_aq_info_good);
            mAirQualityBtn.setText(R.string.air_quality_good);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_AUTO_ON) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_AUTO_OFF)){
            mAutoLayout.setVisibility(View.VISIBLE);

            if(airPurifierData.getAutoFan() == AirPurifierDef.AUTO_OFF)
                mAutoImageView.setSelected(false);
            else
                mAutoImageView.setSelected(true);
                //mAutoImageView.setSelected(airPurifierData.getAutoFan() == AirPurifierDef.AUTO_OFF ? false : true);
        }else{
            mAutoLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_TIMER)){
            mTimeMainLayout.setVisibility(View.VISIBLE);
            if(airPurifierData.getClock() > 0) {
                mTimeSetTextView.setText(String.valueOf(airPurifierData.getClock()));
                //mTimeSetImageView.setSelected(true);
            }
            else {
                mTimeSetTextView.setText("--");
                //mTimeSetImageView.setSelected(false);
            }
        }else{
            mTimeMainLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_ANION_ON) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_ANION_OFF)) {
            mIONLayout.setVisibility(View.VISIBLE);

            mIONImageView.setSelected(airPurifierData.getAnion() == AirPurifierDef.ANION_OFF ? false : true);
        }else{
            mIONLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_FAN_SPEED)){
            mWindMainLayout.setVisibility(View.VISIBLE);
            mWindLayout.setVisibility(View.VISIBLE);
            if(airPurifierData.getSpeed() >= mFanLevelImageRes.length )
                mFanSpeedImageView.setBackgroundResource(mFanLevelImageRes[0]);
            else
                mFanSpeedImageView.setBackgroundResource(mFanLevelImageRes[airPurifierData.getSpeed()]);
        }else{
            mWindMainLayout.setVisibility(View.GONE);
            mWindLayout.setVisibility(View.GONE);
        }

        if(mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid, AirPurifierDef.KEY_ID_UV_ON) ||
                mAirPurifierMgr.IsAbility(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_UV_OFF)){
            mUVLayout.setVisibility(View.VISIBLE);

            mUvImageView.setSelected(airPurifierData.getUv() == AirPurifierDef.UV_OFF ? false : true);
        }else{
            mUVLayout.setVisibility(View.GONE);
        }


    //    mPowerImageView.setSelected(airPurifierData.getPowerStatus() == AirPurifierDef.POWER_OFF ? false : true);
    }

    private void updateAirPurifierData(AirPurifierData airPurifierData) {
        refreshLayout(airPurifierData);
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(mCurUuid.equalsIgnoreCase(((AirPurifierData)asset_data).getAssetUuid())) {
            updateAirPurifierData((AirPurifierData)asset_data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.room_hub_controller_flipper);

        getWindow().setBackgroundDrawableResource(R.drawable.bg_air_purifier);

        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER;

        mAirPurifierMgr=(AirPurifierManager)mRoomHubMgr.getAssetDeviceManager(mType);
        //mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mAirPurifierData=mAirPurifierMgr.getAirPurifierDataByUuid(mRoomHubUuid,mCurUuid);
        //mRoomHubData=mAirPurifierData.getRoomHubData();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.x = 200;
        params.height = 700;
        params.width = 500;
        mContext=this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewFlipper = (RoomHubViewFilpper) findViewById(R.id.body_flipper);
        viewFlipper.setLongClickable(true);
        viewFlipper.setClickable(true);
        viewFlipper.setOnViewFlipperListener(this);

        mAirPurifierMgr.registerAssetsChange(this);

        View v=createView(mCurUuid);
        if(v != null)
            viewFlipper.addView(v, 0);
        else
            finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAirPurifierMgr.unRegisterAssetsChange(this);

        if(viewFlipper != null)
            viewFlipper.removeAllViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress=true;
        int value = 0;
        int keyId = 0;

        switch (v.getId()) {
            case R.id.air_auto_set:
                value = mAirPurifierData.getAutoFan();
                keyId = (value==AirPurifierDef.AUTO_OFF?AirPurifierDef.KEY_ID_AUTO_ON:AirPurifierDef.KEY_ID_AUTO_OFF);
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,keyId);
                break;
            case R.id.air_time_set:
//                value = mAirPurifierData.getClock()+1;
//                if(value == AirPurifierDef.TIMER_MAX)
//                    value = AirPurifierDef.TIMER_MIN;
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_TIMER);
                break;
            case R.id.air_ion_set:
                value = mAirPurifierData.getAnion();
                keyId = (value==AirPurifierDef.ANION_ON?AirPurifierDef.KEY_ID_ANION_OFF:AirPurifierDef.KEY_ID_ANION_ON);
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,keyId);
                break;
            case R.id.air_wind_set:
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,AirPurifierDef.KEY_ID_FAN_SPEED);
                break;
            case R.id.air_uv_set:
                value = mAirPurifierData.getUv();
                keyId = (value==AirPurifierDef.UV_ON?AirPurifierDef.KEY_ID_UV_OFF:AirPurifierDef.KEY_ID_UV_ON);
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,keyId);
                break;
            case R.id.btn_power:
                value = mAirPurifierData.getPowerStatus();
                keyId = (value==AirPurifierDef.POWER_ON?AirPurifierDef.KEY_ID_POWER_OFF:AirPurifierDef.KEY_ID_POWER_ON);
                mAirPurifierMgr.setKeyId(mRoomHubUuid,mCurUuid,keyId);
                break;
            default:
                break;
        }

        if(is_show_progress) {
            log(TAG, "showProgressDialog");
            if(isShowing() == false)
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }

    }

    @Override
    public View getPreviousView() {
        return null;
    }

    @Override
    public View getNextView() {
        return null;
    }
}
