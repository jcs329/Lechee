package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by cherry on 2016/06/06
 */
public class BaseControllerActivity extends AbstractRoomHubActivity implements RoomHubChangeListener,AssetChangeListener{
    private final String TAG=BaseControllerActivity.class.getSimpleName();
    protected boolean DEBUG = true;

    protected int mType;
    protected RoomHubManager mRoomHubMgr;
    protected String mCurUuid;
    protected String mRoomHubUuid;
    protected RoomHubData mRoomHubData;
    protected Context mContext;

    private TextView mTxtDevName;
    private TextView mTxtSensorTemp;
    private TextView mTxtSensorHum;

    protected final int MESSAGE_UPDATE_ROOMHUB_DATA       = 100;
    protected final int MESSAGE_UPDATE_ASSET_DATA         = 101;
    protected final int MESSAGE_LAUNCH_DEVICE_LIST        = 102;
    protected final int MESSAGE_SHOW_PROGRESS_DIALOG      = 103;
    protected final int MESSAGE_DISMISS_PROGRESS_DIALOG   = 104;
    protected final int MESSAGE_SHOW_TOAST                = 105;

    private PopupMenu menu_settings=null;

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    Controller_UpdateRoomHubData(msg.arg1, (RoomHubData) msg.obj);
                    break;
                case MESSAGE_UPDATE_ASSET_DATA:
                    Controller_UpdateAssetData(msg.obj);
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                case MESSAGE_SHOW_PROGRESS_DIALOG:
                    showProgressDialog("", getString(R.string.processing_str));
                    break;
                case MESSAGE_DISMISS_PROGRESS_DIALOG:
                    dismissProgressDialog();
                    break;
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    private void UpdateSensorData(){
        double sensor_temp=mRoomHubData.getSensorTemp();
        double sensor_hum=mRoomHubData.getSensorHumidity();

//        if(sensor_temp == ErrorKey.SENSOR_TEMPERATURE_INVALID)
//            mTxtSensorTemp.setText("--°");
//        else
//            mTxtSensorTemp.setText(String.valueOf((int) Utils.getTemp(this, sensor_temp)) + "°");
//
//        if(sensor_hum == ErrorKey.SENSOR_HUMIDITY_INVALID)
//            mTxtSensorHum.setText("--%");
//        else
//            mTxtSensorHum.setText(String.valueOf((int) sensor_hum) + "%");

    }

    private void Controller_UpdateRoomHubData(int type,RoomHubData data){
        if(data != null) {
            String uuid=data.getUuid();
            if(uuid.equals(mRoomHubData.getUuid())){
                mRoomHubData = data;
                switch(type){
                    case RoomHubManager.UPDATE_ONLINE_STATUS:
                        if(!data.IsOnLine())
                            finish();
                        break;
                    case RoomHubManager.UPDATE_ROOMHUB_NAME:
//                        mTxtDevName.setText(data.getName());
                        break;
                    case RoomHubManager.UPDATE_SENSOR_DATA:
                        UpdateSensorData();
                        break;
                }
            }
        }
    }

    protected void Controller_UpdateAssetData(Object asset_data){

    }

    protected void log(String TAG,String msg){
        if(DEBUG)
            Log.d(TAG,msg);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurUuid=getIntent().getStringExtra(BaseAssetManager.KEY_ASSET_UUID);
        mRoomHubUuid=getIntent().getStringExtra(BaseAssetManager.KEY_UUID);
        mContext=this;
        mRoomHubMgr=getRoomHubManager();
        mRoomHubData=mRoomHubMgr.getRoomHubDataByUuid(mRoomHubUuid);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.controller_header, null);

//        ActionBar actionBar = getActionBar();
//        actionBar.setCustomView(v);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setDisplayShowCustomEnabled(true);

//        mTxtDevName=(TextView)actionBar.getCustomView().findViewById(R.id.txt_dev_name);
//        mTxtSensorTemp=(TextView)actionBar.getCustomView().findViewById(R.id.txt_sensor_temp);
//        mTxtSensorHum=(TextView)actionBar.getCustomView().findViewById(R.id.txt_sensor_hum);

       // mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRoomHubMgr.registerRoomHubChange(this);

//        mTxtDevName.setText(mRoomHubData.getName());
        UpdateSensorData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRoomHubMgr.unRegisterRoomHubChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.controller_menu_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.controller_menu_settings:
                OpenElectricMenu(findViewById(R.id.controller_menu_settings),mRoomHubData);
                break;
        }

//        return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == mType) && (data != null)) {
            BaseAssetData asset_data = (BaseAssetData)data;
            if (asset_data.getAssetUuid().equals(mCurUuid))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        if(asset_type != mType)
            return;

        if(data == null) return;

        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ASSET_DATA,data));
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == mType) && (data != null)) {
            if (((BaseAssetData) data).getAssetUuid().equals(mCurUuid) && (enabled == false)) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {
        if(asset_type != mType)
            return;

        log(TAG,"onCommandResult asset_type="+asset_type+" uuid="+uuid+" result="+result);
        mHandler.sendEmptyMessage(MESSAGE_DISMISS_PROGRESS_DIALOG);
        if(uuid.equals(mCurUuid)) {
            if (result < ErrorKey.Success) {
                //dismissProgressDialog();
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, Utils.getErrorCodeString(this, result)));
            }
        }
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {

    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA, type,0, data));
        }
    }

    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {

    }
}
