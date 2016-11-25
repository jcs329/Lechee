package com.quantatw.roomhub.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

public class RenameActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener,HealthDeviceChangeListener {
    private static final String TAG = "RenameActivity";
    private TextView txt_dev_name;
    private RoomHubManager mRoomHubMgr;
    private HealthDeviceManager mHealthDeviceManager;
    private Button btn_ok;
    private int mDeviceCategory = DeviceTypeConvertApi.CATEGORY.ROOMHUB;
    private int mDeviceType;
    private String mCurUuid;
    private HealthData mHealthData;
    private Context mContext;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 100;
    private final int MESSAGE_SHOW_TOAST         = 101;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    //finish();
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
        setContentView(R.layout.modify_devname);
        mContext=this;

        mDeviceCategory = getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_CATEGORY);
        mDeviceType = getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_TYPE);
        mCurUuid=getIntent().getExtras().getString(RoomHubManager.KEY_UUID);

        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            mHealthDeviceManager = getHealthDeviceManager();
            mHealthDeviceManager.registerHealthDeviceChangeListener(this);
            mHealthData = mHealthDeviceManager.getHealthDataByUuid(mCurUuid);
        }
        else {
            mRoomHubMgr = getRoomHubManager();
            mRoomHubMgr.registerRoomHubChange(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String dev_name=getIntent().getExtras().getString(RoomHubManager.KEY_DEV_NAME);

        txt_dev_name=(TextView)findViewById(R.id.new_dev_name);
        txt_dev_name.setText(dev_name);

        btn_ok=(Button)findViewById(R.id.rename_ok);
        btn_ok.setOnClickListener(this);
        Log.d(TAG, "onResume uuid=" + mCurUuid + " dev_name=" + dev_name);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(mRoomHubMgr != null)
            mRoomHubMgr.unRegisterRoomHubChange(this);
        if(mHealthDeviceManager != null)
            mHealthDeviceManager.unregisterHealthDeviceChangeListener(this);
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        final String dev_name=txt_dev_name.getText().toString();

        showProgressDialog("", getString(R.string.processing_str));

        Thread thread = new Thread() {
            @Override
            public void run() {
                int retval = 0;
                if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH)
                    retval = mHealthData.rename(dev_name);
                else
                    retval=mRoomHubMgr.modifiedDeviceName(mCurUuid, dev_name);
            Log.d(TAG,"modify device name retval="+retval);
            dismissProgressDialog();
            if(retval < ErrorKey.Success)
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, Utils.getErrorCodeString(mContext,retval)));
            else
                finish();
            }
        };
        thread.start();
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data != null) {
            if (data.getUuid().equals(mCurUuid)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if (data.getUuid().equals(mCurUuid) && (data.IsUpgrade() || !data.IsOnLine())) {
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
            }
        }
    }
    /*
    @Override
    public void UpdateRoomHubDeviceSeq(MicroLocationData locationData) {

    }
    */
    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        if (uuid.equals(mCurUuid) && (is_upgrade == true)) {
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void updateDevice(int type, HealthData device) {

    }

    @Override
    public void addDeivce(HealthData device) {

    }

    @Override
    public void removeDevice(HealthData device) {
        if(device != null) {
            if (device.getUuid().equals(mCurUuid)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }
}
