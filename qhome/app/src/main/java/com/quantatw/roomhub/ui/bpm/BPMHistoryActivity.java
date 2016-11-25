package com.quantatw.roomhub.ui.bpm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.health.bpm.BPMData;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceUpdateType;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.ui.AbstractRoomHubActivity;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

/**
 * Created by erin on 5/16/16.
 */
public class BPMHistoryActivity extends AbstractRoomHubActivity implements HealthDeviceChangeListener{
    private final String TAG=BPMHistoryActivity.class.getSimpleName();
    private FragmentTabHost mTabHost;
    private BPMHistoryFragment mCurrentFragment;
    private HealthDeviceManager mHealthDeviceManager;
    private TextView mTxtDeviceName;
    private String mCurentUuid;
    private String mUserId;
    private BPMData mBPMData;

    private final int MESSAGE_REFRESH_UI = 100;
    private final int MESSAGE_DELAY_ONLOAD = 200;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH_UI:
                    HealthData healthData = (HealthData)msg.obj;
                    mTxtDeviceName = (TextView) findViewById(R.id.bpm_name);
                    mTxtDeviceName.setText(healthData.getDeviceName());
                    break;
                case MESSAGE_DELAY_ONLOAD:
                    dismissProgressDialog();
                    mBPMData = (BPMData)mHealthDeviceManager.getShareHealthDataByUserId(
                            DeviceTypeConvertApi.TYPE_HEALTH.BPM,mUserId);
                    if(mBPMData == null) {
                        //Toast.makeText(mContext,"Can't find any records",Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"--- MESSAGE_DELAY_ONLOAD ---, still can't get BPMData!");
                        finish();
                    }
                    else {
                        initLayout();
                    }
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
        setContentView(R.layout.activity_bpm_history);

        mHealthDeviceManager = getHealthDeviceManager();
        mHealthDeviceManager.registerHealthDeviceChangeListener(this);

        if(HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
            mUserId = getIntent().getStringExtra(GlobalDef.BP_USERID_MESSAGE);

            mBPMData = (BPMData)mHealthDeviceManager.getShareHealthDataByUserId(
                    DeviceTypeConvertApi.TYPE_HEALTH.BPM,mUserId);
        }
        else {
            mCurentUuid = getIntent().getStringExtra(GlobalDef.BP_UUID_MESSAGE);
            mBPMData = (BPMData)mHealthDeviceManager.getHealthDataByUuid(mCurentUuid);
        }

        if(mBPMData == null) {
            Log.d(TAG,"BPMData is null!!!!!");
            showProgressDialog("",getString(R.string.loading));
            mHandler.sendEmptyMessageDelayed(MESSAGE_DELAY_ONLOAD,3000);
            return;
        }

        initLayout();
    }

    private void initLayout() {
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.container);

        addNewTab(BPMHistoryLatestFragment.class,getString(R.string.history_latest));
        addNewTab(BPMHistoryUsageFragment.class,getString(R.string.history_Usage));

        mTxtDeviceName = (TextView)findViewById(R.id.bpm_name);
        mTxtDeviceName.setText(mBPMData.getDeviceName());
    }

    protected void onTabSelected(BPMHistoryFragment bpmHistoryFragment) {
        mCurrentFragment = bpmHistoryFragment;
    }

    @Override
    protected void onDestroy() {
        clearFragments();
        super.onDestroy();
        getHealthDeviceManager().unregisterHealthDeviceChangeListener(this);
    }

    @Override
    public void updateDevice(int type, HealthData device) {
        if(device != null && device.getUuid().equals(mCurentUuid)) {
            if (type == HealthDeviceUpdateType.DEVICE_NAME) {
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REFRESH_UI,device));
            }
            mCurrentFragment.updateDevice(type,device);
        }
    }

    @Override
    public void addDeivce(HealthData device) {
    }

    @Override
    public void removeDevice(HealthData device) {
    }

    private void clearFragments() {
        for(int i=0;i<getSupportFragmentManager().getBackStackEntryCount();i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void addNewTab(Class<?> cls, String name) {
        Bundle bundle = new Bundle();
        if(HealthDeviceManager.NEW_BPM_SHARE_STYLE) {
            bundle.putParcelable(GlobalDef.BP_DATA_MESSAGE, mBPMData);
        }
        else
            bundle.putString(GlobalDef.BP_UUID_MESSAGE, mCurentUuid);
        mTabHost.addTab(mTabHost.newTabSpec(name)
                        .setIndicator(name),cls,bundle);
    }
}
