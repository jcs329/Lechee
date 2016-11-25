package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.BulbData;
import com.quantatw.roomhub.manager.asset.manager.BulbManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

import java.util.List;

/**
 * Created by 95011613 on 2016/4/26.
 */
public class BulbController extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubViewFilpper.OnViewFlipperListener,RoomHubChangeListener,AssetChangeListener,SeekBar.OnSeekBarChangeListener {
    private static final String TAG = BulbController.class.getSimpleName();
    private static boolean DEBUG=true;
    private RoomHubManager mRoomHubMgr;
    private BulbManager mBulbManager;

    private RoomHubViewFilpper mViewFlipper;
    private TextView mTxtDevName;
    private TextView mTxtBulbName;
    private ImageView mImgGroupControl;

    private ImageView mImgBulb, mImgNext, mImgBack;
    private TextView mTxtOff;
    private LinearLayout ll_dimmer_bar;
    private SeekBar mDimmerBar;
    private ImageView mBtnPower;
    private List<BulbData> mBulbList;
    private int indexBulb = 0;
    private int[] mBulbResIds = {R.drawable.icon_lamp_20,R.drawable.icon_lamp_40,R.drawable.icon_lamp_60,R.drawable.icon_lamp_80, R.drawable.icon_lamp_100};

    private BulbData mBulbData;

    private final int MESSAGE_UPDATE_ROOMHUB_DATA       = 100;
    private final int MESSAGE_UPDATE_BULB_DATA = 101;
    private final int MESSAGE_LAUNCH_DEVICE_LIST        = 102;
    private final int MESSAGE_SHOW_PROGRESS_DIALOG      = 103;
    private final int MESSAGE_DISMISS_PROGRESS_DIALOG   = 104;
    private final int MESSAGE_SHOW_TOAST                = 105;

    private String mCurUuid;
    private Context mContext;
    private String roomhubUUID;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            log("what="+msg.what);
            switch(msg.what) {
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    Controller_UpdateRoomHubData(msg.arg1,(RoomHubData) msg.obj);
                    break;
                case MESSAGE_UPDATE_BULB_DATA:
                    Controller_UpdateBulbData((BulbData) msg.obj);
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
                    Toast.makeText(mContext,(String)msg.obj,Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.bulb_controller);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        mContext=this;
        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.controller_header_bulb, null);

        ActionBar actionBar = getActionBar();
        actionBar.setCustomView(v);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);

        mTxtDevName=(TextView)actionBar.getCustomView().findViewById(R.id.txt_dev_name);
        mTxtBulbName=(TextView)findViewById(R.id.txt_lamp_name);
        mImgGroupControl=(ImageView)actionBar.getCustomView().findViewById(R.id.img_group_control);
        mImgGroupControl.setOnClickListener(this);

        mImgBack=(ImageView)findViewById(R.id.img_back);
        mImgNext=(ImageView)findViewById(R.id.img_next);
        mImgBack.setOnClickListener(this);
        mImgNext.setOnClickListener(this);
        mImgBulb =(ImageView)findViewById(R.id.img_lamp);
        mTxtOff=(TextView)findViewById(R.id.txt_off);
        ll_dimmer_bar=(LinearLayout)findViewById(R.id.ll_dimmer_bar);
        mDimmerBar=(SeekBar)findViewById(R.id.dimmer_seekbar);
        mDimmerBar.setOnSeekBarChangeListener(this);

        mViewFlipper = (RoomHubViewFilpper) findViewById(R.id.ir_viewFlipper);
        mViewFlipper.setOnViewFlipperListener(this);
        mViewFlipper.setLongClickable(true);

        mBtnPower=(ImageView)findViewById(R.id.btn_power);
        mBtnPower.setOnClickListener(this);

        findViewById(R.id.btn_advance).setOnClickListener(this);

        mRoomHubMgr=getRoomHubManager();
        mBulbManager = (BulbManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.BULB);

        roomhubUUID = getIntent().getStringExtra(BaseAssetManager.KEY_UUID);
        mBulbList = mBulbManager.getBulbList(roomhubUUID);
        mBulbData = mBulbList.get(indexBulb);
        if(mBulbData == null){
            finish();
        }else{
            mCurUuid = mBulbData.getAssetUuid();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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
                OpenElectricMenu(findViewById(R.id.controller_menu_settings), mBulbData.getRoomHubData());
                break;
        }

//        return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRoomHubMgr.registerRoomHubChange(this);
        mBulbManager.registerAssetsChange(this);

        mRoomHubMgr.setLed(mBulbData.getRoomHubUuid(), RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);

        updateBulbList();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRoomHubMgr.unRegisterRoomHubChange(this);
        mBulbManager.unRegisterAssetsChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void updateBulbList(){

        mBulbList = mBulbManager.getBulbList(roomhubUUID);
        mBulbData = mBulbManager.getBulbDataByUuid(mCurUuid);
        indexBulb = mBulbList.indexOf(mBulbData);
        if(mBulbData == null||mBulbList.size() == 0) {
            finish();
            return;
        }else{
            mCurUuid = mBulbData.getAssetUuid();
        }
        mTxtDevName.setText(mBulbData.getRoomHubData().getName());
        mTxtBulbName.setText(mBulbData.getName());
        UpdateBulbLayout();
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress=true;
         switch (v.getId()){
             case R.id.btn_power:
                if(mBulbData.getPower() == 0)
                    mBulbManager.setPower(mCurUuid,1);
                else
                    mBulbManager.setPower(mCurUuid,0);
                break;
             case R.id.img_back:
                 is_show_progress = false;
                 previousPage();
                 break;
             case R.id.img_next:
                 is_show_progress = false;
                 nextPage();
                 break;
             case R.id.img_group_control:
                 is_show_progress = false;
                 Intent intent = new Intent();
                 intent.setClass(this, BulbGroupController.class);
                 intent.putExtra(RoomHubManager.KEY_UUID,mCurUuid);
                 startActivity(intent);
                 break;
             case R.id.btn_advance:
                 is_show_progress = false;
                 Intent intent2 = new Intent();
                 intent2.setClass(this, BulbAdvanceActivity.class);
                 intent2.putExtra(RoomHubManager.KEY_UUID,mCurUuid);
                 startActivity(intent2);
                 break;
        }
        if(is_show_progress) {
            log("showProgressDialog");
            if(!isShowing()) {
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
            }
        }
    }

    private void nextPage(){
        if(indexBulb < mBulbList.size()-1){
            indexBulb++;
        }else{
            return;
        }
        mBulbData=mBulbList.get(indexBulb);
        mCurUuid = mBulbData.getAssetUuid();
        UpdateBulbLayout();
    }
    private void previousPage(){
        if(indexBulb > 0){
            indexBulb --;
        }else{
            return;
        }
        mBulbData=mBulbList.get(indexBulb);
        mCurUuid = mBulbData.getAssetUuid();
        UpdateBulbLayout();
    }


    private void UpdateBulbLayout(){

        mTxtBulbName.setText(mBulbData.getName()+"("+(indexBulb+1)+"/"+mBulbList.size()+")");
        if(indexBulb == 0){
            mImgBack.setVisibility(View.INVISIBLE);
        }else{
            mImgBack.setVisibility(View.VISIBLE);
        }
        if (indexBulb == mBulbList.size()-1){
            mImgNext.setVisibility(View.INVISIBLE);
        }else{
            mImgNext.setVisibility(View.VISIBLE);
        }
        if (mBulbData.getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE||mBulbData.getPower() == -1){
            mImgBulb.setImageResource(R.drawable.icon_lamp_off);
            mTxtOff.setText(getString(R.string.bulb_control_offline));
            ll_dimmer_bar.setVisibility(View.INVISIBLE);
            mTxtOff.setTextColor(getResources().getColor(R.color.color_white59));

            mBtnPower.setImageResource(R.drawable.power_btn_off_selector);
        }else {
            if (mBulbData.getPower() == 0) {
                mImgBulb.setImageResource(R.drawable.icon_lamp_off);
                ll_dimmer_bar.setVisibility(View.INVISIBLE);
                mTxtOff.setText(getString(R.string.bulb_control_off));
                mTxtOff.setTextColor(getResources().getColor(R.color.color_white59));
                mBtnPower.setImageResource(R.drawable.power_btn_off_selector);
            } else {
                mBtnPower.setImageResource(R.drawable.power_btn_on_selector);
                ll_dimmer_bar.setVisibility(View.VISIBLE);

                int luminance = mBulbData.getLuminance();
                if (luminance > 100){
                    luminance = 100;
                }
                if ((luminance >= 0) && (luminance <= 20)) {
                    mImgBulb.setImageResource(mBulbResIds[0]);
                } else if ((luminance >= 21) && (luminance <= 40)) {
                    mImgBulb.setImageResource(mBulbResIds[1]);
                } else if ((luminance >= 41) && (luminance <= 60)) {
                    mImgBulb.setImageResource(mBulbResIds[2]);
                } else if ((luminance >= 61) && (luminance <= 80)) {
                    mImgBulb.setImageResource(mBulbResIds[3]);
                } else if ((luminance >= 81) && (luminance <= 100)) {
                    mImgBulb.setImageResource(mBulbResIds[4]);
                }


                mTxtOff.setText(luminance + "%");
                mTxtOff.setTextColor(getResources().getColor(R.color.color_whitea1));
                mDimmerBar.setProgress(luminance);
            }
        }
    }

    private void Controller_UpdateRoomHubData(int type,RoomHubData data){
        if(data != null) {
            String uuid=data.getUuid();
            if(uuid.equals(mBulbData.getRoomHubUuid())){
                switch(type){
                    case RoomHubManager.UPDATE_ONLINE_STATUS:
                        if(!data.IsOnLine())
                            finish();
                        break;
                    case RoomHubManager.UPDATE_ROOMHUB_NAME:
                        mTxtDevName.setText(data.getName());
                        break;
                    case RoomHubManager.UPDATE_SENSOR_DATA:
                        break;
                }
            }
        }
    }

    private void Controller_UpdateBulbData(BulbData data){
        if(data != null) {
            log("Controller_UpdateBulbData data roomHub uuid: " + data.getRoomHubUuid() + ",roomhubUUID: " + roomhubUUID);
            if (data.getRoomHubUuid().equals(roomhubUUID)) {
                updateBulbList();
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
    public void UpdateRoomHubData(int type,RoomHubData data) {
        if(data != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA, type, 0, data));
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

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int seekProgress = mDimmerBar.getProgress();

        log("seekProgress="+seekProgress);
         mBulbManager.setLuminance(mCurUuid, seekProgress+1);
    }


    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }

    @Override
    public void addDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null)) {
            if (roomhubUUID.equals(((BulbData)data).getRoomHubUuid())) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null)) {
            if (roomhubUUID.equals(((BulbData)data).getRoomHubUuid())) {
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null))
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_BULB_DATA,data));
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null)) {
            if (((BulbData)data).getAssetUuid().equals(mCurUuid) && (enabled == false)){
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
            }
        }
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

        if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) {
            log("onCommandResult uuid=" + uuid + " result=" + result);

            mHandler.sendEmptyMessage(MESSAGE_DISMISS_PROGRESS_DIALOG);
            if (uuid.equals(mCurUuid)) {
                if (result < ErrorKey.Success) {
                    mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, Utils.getErrorCodeString(this, result)));
                }
            }
        }
    }

    @Override
    public View getNextView() {
        nextPage();
        return null;
    }

    @Override
    public View getPreviousView() {
        previousPage();
        return null;
    }
}
