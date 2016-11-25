package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.BulbData;
import com.quantatw.roomhub.manager.asset.manager.BulbManager;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;

public class BulbAdvanceActivity extends AbstractRoomHubActivity implements View.OnClickListener,AssetChangeListener {
    private final String TAG=BulbAdvanceActivity.class.getSimpleName();
    private String mCurUuid;
    private BulbManager bulbManager;
    private BulbData mData;
    private Context mContext;
    private TextView nameTextView;

    private final int MESSAGE_COMMAND_RESULT = 200;
    private final int MESSAGE_UPDATE_DATA = 300;
    private final int MESSAGE_LAUNCH_DEVICE_LIST = 400;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_COMMAND_RESULT:
                    //updateData();
                    Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_UPDATE_DATA:
                    mData=(BulbData)msg.obj;
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
        setContentView(R.layout.bulb_advance);
        mContext=this;
        findViewById(R.id.btn_rename).setOnClickListener(this);
        nameTextView = (TextView)findViewById(R.id.txt_dev_name);
        bulbManager = (BulbManager)getRoomHubManager().getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.BULB);

        mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_UUID);
        mData= bulbManager.getBulbDataByUuid(mCurUuid);

        if(mData == null)
            finish();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bulbManager.registerAssetsChange(this);

        updateData();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(bulbManager != null)
            bulbManager.unRegisterAssetsChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateData() {
        nameTextView.setText(mData.getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rename:
                Intent intent = new Intent();
                intent.setClass(this, BulbRenameActivity.class);
                intent.putExtra(BaseAssetManager.KEY_UUID,mCurUuid);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {
        Log.d(TAG, "onCommandResult " + asset_type + " uuid=" + uuid + " result=" + result);
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) && (uuid != null)) {
            if (uuid.equals(mCurUuid)) {
                dismissProgressDialog();
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_COMMAND_RESULT, Utils.getErrorCodeString(this, result)));
            }
        }
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) && (data != null)) {
            if (((BulbData)data).getAssetUuid().equals(mCurUuid))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) && (data != null)) {
            if (((BulbData)data).getAssetUuid().equals(mCurUuid))
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_DATA, data));
        }
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) && (data != null)){
            if (((BulbData)data).getAssetUuid().equals(mCurUuid) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }
}
