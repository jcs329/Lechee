package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quantatw.myapplication.R;

import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.PMData;
import com.quantatw.roomhub.manager.asset.manager.PMManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.sls.api.AQIApi;
import com.quantatw.sls.api.DeviceTypeConvertApi;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PMActivity extends BaseControllerActivity implements View.OnClickListener {
    private static final String TAG = "PMActivity";
    private static boolean DEBUG=true;

    private PMManager mPMMgr;
    private PMData mData;
    private static final int KEY_ID_REFRESH     =   31;

    private static final int AIR_QUALITY_GOOD   =   0;
    private static final int AIR_QUALITY_NORMAL =   1;
    private static final int AIR_QUALITY_DANGER =   2;

    private LinearLayout ll_aq;
//    private ImageView mIvbattery;
    private TextView mTxtAirQuality;
    private TextView mTxtUnit;
    private TextView mTxtUpdateTime;
    private ImageView mBtnReload;

    private int[] aq_str_resId = {R.string.air_quality_good,R.string.air_quality_normal,R.string.air_quality_danger};
    private int[] aq_bg_resId = {R.drawable.bg_aq_good,R.drawable.bg_aq_normal,R.drawable.bg_aq_danger};
    private int[] battery_resId = {R.drawable.bettery_lv_1,R.drawable.bettery_lv_2,R.drawable.bettery_lv_3,R.drawable.bettery_lv_4,R.drawable.bettery_lv_5};
    private int[] img_aq_resId = {R.drawable.img_aq_good,R.drawable.img_aq_normal,R.drawable.img_aq_danger};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault_Light_DialogWhenLarge_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pm25_activity);

        mType= DeviceTypeConvertApi.TYPE_ROOMHUB.PM25;

        mPMMgr=(PMManager)mRoomHubMgr.getAssetDeviceManager(mType);
        mData=mPMMgr.getPMDataByUuid(mRoomHubUuid,mCurUuid);

        if(mData == null)
            finish();

      //  mRoomHubData=mData.getRoomHubData();

        ll_aq=(LinearLayout)findViewById(R.id.ll_aq);
//        mIvbattery=(ImageView)findViewById(R.id.iv_battery);
        mTxtAirQuality=(TextView)findViewById(R.id.txt_air_quality);
        mTxtUnit=(TextView)findViewById(R.id.txt_unit);
        mTxtUpdateTime=(TextView)findViewById(R.id.txt_update_time);
        mBtnReload=(ImageView)findViewById(R.id.btn_reload);
        mBtnReload.setOnClickListener(this);
        WindowManager.LayoutParams params = getWindow().getAttributes();

        params.x = 200;
        params.height = 700;
        params.width = 500;
        mContext=this;
//        Button btnAdvance = (Button)findViewById(R.id.btn_advance);
//        btnAdvance.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mPMMgr.registerAssetsChange(this);

        mRoomHubMgr.setLed(mRoomHubUuid, RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        UpdateAirQuality();
    }

    @Override
    protected void onPause() {
        if(mPMMgr != null)
            mPMMgr.unRegisterAssetsChange(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_reload:
                showProgressDialog("", getString(R.string.processing_str));
                mPMMgr.setKeyId(mRoomHubUuid,mCurUuid,KEY_ID_REFRESH);
                break;
            case R.id.btn_advance:
                Intent intent = new Intent(this, PMAdvanceActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(BaseAssetManager.KEY_UUID, mRoomHubUuid);
                intent.putExtra(BaseAssetManager.KEY_ASSET_UUID,mCurUuid);
                startActivity(intent);
                break;
        }
    }

    private void UpdateAirQuality(){
        if(mData == null) return;

        /*
        if(mData.getAdapter() == 1){
            mIvbattery.setBackgroundResource(R.drawable.usb_status);
        }else {
            int capacity = ((mData.getCapacity() >= 1) && (mData.getCapacity() <= battery_resId.length)) ? mData.getCapacity() : 1;
            mIvbattery.setBackgroundResource(battery_resId[capacity - 1]);
            if (capacity == 1) {
                showLowBatterDialog();
            }
        }
        */
        int level=getAirQualityLevel();
        getWindow().setBackgroundDrawableResource(aq_bg_resId[level]);

        ll_aq.setBackgroundResource(img_aq_resId[level]);

        mTxtAirQuality.setText(aq_str_resId[level]);
        mTxtUnit.setText(String.valueOf(mData.getValue())+" μg/m3");

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        mTxtUpdateTime.setText(format.format(new Date(mData.getUpdateTime())));
    }

    protected void Controller_UpdateAssetData(Object asset_data){
        if(mCurUuid.equalsIgnoreCase(((PMData)asset_data).getAssetUuid())) {
            mData = (PMData) asset_data;
            UpdateAirQuality();
        }
    }

    private void showLowBatterDialog() {
        final Dialog dialog = new Dialog(this,R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.pm25_low_batter_alert));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.show();
    }

    public int getAirQualityLevel(){
        AQIApi.AQI_CATEGORY catetory=AQIApi.getAQICategoryByPM25Value(mData.getValue());

        if(catetory == AQIApi.AQI_CATEGORY.DANGER)
            return AIR_QUALITY_DANGER;
        else if(catetory == AQIApi.AQI_CATEGORY.NORMAL)
            return AIR_QUALITY_NORMAL;
        else
            return AIR_QUALITY_GOOD;
    }
}
