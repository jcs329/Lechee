package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.NoticeSetting;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.PMData;
import com.quantatw.roomhub.manager.asset.manager.PMManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.AQIApi;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.key.ErrorKey;

/**
 * Created by erin on 6/2/16.
 */
public class PMAdvanceActivity extends AbstractRoomHubActivity implements View.OnClickListener,AssetChangeListener {
    private final String TAG=PMAdvanceActivity.class.getSimpleName();
    private String mCurUuid;
    private String mRoomHubUuid;
    private PMManager mPMMgr;
    private PMData mData;
    private Context mContext;

    private TextView mTxtTime;
    private ImageView mImageHigher, mImageLower;

    private int mSettingTime;
    private final int NOTICE_SETTING_TIME_MIN = 6;
    private final int NOTICE_SETTING_TIME_MAX = 60;

    private final int MESSAGE_SET_TIME = 100;
    private final int MESSAGE_COMMAND_RESULT = 200;
    private final int MESSAGE_UPDATE_PM_DATA = 300;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 400;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SET_TIME:
                    setNoticeSetting();
                    break;
                case MESSAGE_COMMAND_RESULT:
                    //updateData();
                    Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_UPDATE_PM_DATA:
                    mData=(PMData)msg.obj;
                    updateData();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm25_advance_setting);
        mContext=this;

        mTxtTime = (TextView)findViewById(R.id.txt_time);
        mImageHigher = (ImageView)findViewById(R.id.btn_time_higher);
        mImageLower = (ImageView)findViewById(R.id.btn_time_lower);
        mImageHigher.setOnClickListener(this);
        mImageLower.setOnClickListener(this);
        Button btn_ok = (Button)findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);

        mPMMgr=(PMManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.PM25);
        mRoomHubUuid=getIntent().getStringExtra(BaseAssetManager.KEY_UUID);
        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_ASSET_UUID);
        mData=mPMMgr.getPMDataByUuid(mRoomHubUuid,mCurUuid);

        if(mData == null)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPMMgr.registerAssetsChange(this);

        updateData();
        /*
        mSettingTime = mData.getNoticeSetting().getNoticeTime();
        mSettingTime = mSettingTime/60;
        Log.d(TAG,"onResume mSettingTime="+mSettingTime+",time="+mSettingTime);
        mTxtTime.setText(Integer.toString(mSettingTime));
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPMMgr != null)
            mPMMgr.unRegisterAssetsChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateData() {
        mSettingTime = mData.getNoticeSetting().getNoticeTime();
        //final int time = mSettingTime/60;
        mSettingTime = mSettingTime / 60;
        Log.d(TAG,"updateData mSettingTime="+mSettingTime+",time="+mSettingTime);
        mTxtTime.setText(Integer.toString(mSettingTime));
    }

    private void setNoticeSetting() {
        showProgressDialog("", getString(R.string.process_str));
        int[] danger_range=AQIApi.getDangerRange();
        NoticeSetting noticeSetting = new NoticeSetting(0,mSettingTime*60, danger_range[0]);
        // TODO: set notifyValue

        mPMMgr.setNoticeSetting(mRoomHubUuid,mCurUuid,noticeSetting);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_time_higher:
                if(mSettingTime < NOTICE_SETTING_TIME_MAX) {
                    /*
                    * Reset to minimum time due to the data from RoomHub by previous version
                    * might have incorrect value
                     */
                    if(mSettingTime < NOTICE_SETTING_TIME_MIN)
                        mSettingTime = NOTICE_SETTING_TIME_MIN;
                    else
                        mSettingTime++;
                    mTxtTime.setText(Integer.toString(mSettingTime));
//                    mHandler.sendEmptyMessage(MESSAGE_SET_TIME);
                }
                break;
            case R.id.btn_time_lower:
                if(mSettingTime > NOTICE_SETTING_TIME_MIN) {
                    mSettingTime--;
                    mTxtTime.setText(Integer.toString(mSettingTime));
                }
                break;
            case R.id.btn_ok:
                mHandler.sendEmptyMessage(MESSAGE_SET_TIME);
                break;
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {
        Log.d(TAG, "onCommandResult " + asset_type + " uuid=" + uuid + " result=" + result);
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25) && (uuid != null)) {
            if (uuid.equals(mCurUuid)) {
                dismissProgressDialog();
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_COMMAND_RESULT, Utils.getErrorCodeString(this, result)));
                /*
                if (result == ErrorKey.Success) {
                    finish();
                }else
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_COMMAND_RESULT, Utils.getErrorCodeString(this, result)));
                */
            }
        }
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25) && (data != null)) {
            if (((PMData)data).getAssetUuid().equals(mCurUuid))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25) && (data != null)) {
            if (((PMData)data).getAssetUuid().equals(mCurUuid))
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_PM_DATA, data));
        }
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25) && (data != null)){
            if (((PMData)data).getAssetUuid().equals(mCurUuid) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }
}
