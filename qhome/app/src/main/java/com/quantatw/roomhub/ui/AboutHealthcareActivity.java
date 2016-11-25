package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceUpdateType;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.sls.api.DeviceTypeConvertApi;

public class AboutHealthcareActivity extends AbstractRoomHubActivity implements View.OnClickListener,HealthDeviceChangeListener {
    private static final String TAG = "AboutActivity";
    private static boolean DEBUG=true;
    private TextView mTxtName;
    private Button mBtnRename;
    private HealthDeviceManager mHealthDeviceManager;
    private Context mContext;

    private HealthData mData;
    private String mCurUuid;
    private boolean mShow;

    private final int MESSAGE_UPDATE_DATA = 100;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_UPDATE_DATA:
                    HealthData data=(HealthData)msg.obj;
                    if(data != null && data.getUuid().equals(mCurUuid)) {
                        synchronized (mData) {
                            mData = data;
                        }
                        int type=msg.arg1;
                        if(type == HealthDeviceUpdateType.ONLINE_STATUS){
                            // TODO:
//                            if(!data.IsOnLine())
//                                finish();
                        }else if(type == HealthDeviceUpdateType.DEVICE_NAME){
                            mTxtName.setText(data.getDeviceName());
                        }
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.healthcare_devices_about);

        mContext = this;
        mHealthDeviceManager = getHealthDeviceManager();
        mHealthDeviceManager.registerHealthDeviceChangeListener(this);
        mData = getIntent().getParcelableExtra(GlobalDef.KEY_DEVICE_DATA);
        mCurUuid = mData.getUuid();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mShow = true;

        mTxtName=(TextView)findViewById(R.id.txt_dev_name);
        synchronized (mData) {
            mTxtName.setText(mData.getDeviceName());
        }

        mBtnRename=(Button) findViewById(R.id.btn_rename);
        mBtnRename.setOnClickListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mShow = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHealthDeviceManager.unregisterHealthDeviceChangeListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_rename){
            LaunchReName();
        }
    }

    private void LaunchReName(){
//        if (!mData.IsOnLine() ) {
//            Toast.makeText(this, getString(R.string.device_offline), Toast.LENGTH_SHORT).show();
//            return;
//        }

        Intent intent = new Intent(this, RenameActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(GlobalDef.KEY_DEVICE_CATEGORY, DeviceTypeConvertApi.CATEGORY.HEALTH);
        bundle.putInt(GlobalDef.KEY_DEVICE_TYPE, mData.getType());
        bundle.putString(RoomHubManager.KEY_UUID, mCurUuid);
        bundle.putString(RoomHubManager.KEY_DEV_NAME, mData.getDeviceName());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void addDeivce(HealthData device) {

    }

    @Override
    public void removeDevice(HealthData device) {
        if(device!=null) {
            if(device.getUuid().equals(mCurUuid)) {
                finish();
            }
        }
    }

    @Override
    public void updateDevice(int type, HealthData device) {
        if(device != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_DATA,type,0,device));
        }
    }
}
