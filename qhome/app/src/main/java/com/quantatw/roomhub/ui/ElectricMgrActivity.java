package com.quantatw.roomhub.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.manager.health.data.HealthData;
import com.quantatw.roomhub.manager.health.listener.HealthDeviceChangeListener;
import com.quantatw.roomhub.manager.health.manager.HealthDeviceManager;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.GlobalDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class ElectricMgrActivity extends AbstractRoomHubActivity implements
        View.OnClickListener,RoomHubChangeListener,AssetListener,AssetChangeListener,HealthDeviceChangeListener {
    private static final String TAG = "ElectricMgrActivity";
    private static boolean DEBUG=true;

    private RoomHubManager mRoomHubMgr;
    private HealthDeviceManager mHealthDeviceManager;
    private GridView mElectricGv;
    private Button mBtnAdd;
    private View contentLayout;
    private View addNewLayout;

    private int mDeviceCategory;
    private String mCurUuid;
    private ElectricAdapter mElectricAdapter;
    private RoomHubData mData;
    private Context mContext;

    public class ContentList<T> {
        private ArrayList<T> mList;
        public void setList(ArrayList<T> list) {
            this.mList = list;
        }
        public ArrayList<T> getList() {
            return this.mList;
        }
        public void addToList(T t) {
            this.mList.add(t);
        }
        public void removeFromList(T t) {
            this.mList.remove(t);
        }
        public T getItem(int position) {
            return mList.get(position);
        }
    }
    private ContentList mElectricList = new ContentList();

    private final static int MESSAGE_ADD_DEVICE         = 101;
    private final static int MESSAGE_REMOVE_DEVICE      = 102;
    private final static int MESSAGE_UPDATE_DEVICE      = 103;
    private final static int MESSAGE_UPDATE_AC_DATA     = 104;
    private final static int MESSAGE_LAUNCH_DEVICE_LIST = 105;
    private final static int MESSAGE_SHOW_CMD_RESULT    = 106;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ADD_DEVICE:
                case MESSAGE_REMOVE_DEVICE:
                case MESSAGE_UPDATE_DEVICE:
                case MESSAGE_UPDATE_AC_DATA:
                    if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH)
                        refreshList(msg.what,(HealthData)msg.obj);
                    mElectricAdapter.notifyDataSetChanged();
                    refreshUI();
                    break;
                case MESSAGE_LAUNCH_DEVICE_LIST:
                    launchDeviceList();
                    break;
                case MESSAGE_SHOW_CMD_RESULT:
                    int retval=(int)msg.obj;
                    if(retval < ErrorKey.Success){
                        Toast.makeText(mContext, Utils.getErrorCodeString(getApplicationContext(),retval), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(mContext, R.string.delete_asset_success, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.electric_list);

        mContext=this;
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        mDeviceCategory = getIntent().getExtras().getInt(GlobalDef.KEY_DEVICE_CATEGORY);

        LayoutInflater inflator = (LayoutInflater) this .getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.device_add_header, null);

        ActionBar actionBar=getActionBar();
        actionBar.setCustomView(v);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        ImageView btnAdd=(ImageView)actionBar.getCustomView().findViewById(R.id.btn_add_electric);
        btnAdd.setOnClickListener(this);

        TextView title = (TextView)v.findViewById(R.id.txt_title);
        // TODO: other category ?
        if(mDeviceCategory != DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            title.setText(Utils.getCategoryTitleString(this, mDeviceCategory));
            mHealthDeviceManager = getHealthDeviceManager();
        }
        else {
            title.setText(getResources().getString(R.string.manager_device));
            mRoomHubMgr=getRoomHubManager();
            //mACMgr=getACManager();
            //mFANMgr=getFANManager();
        }

        /*
         * acquire and held wakelock here and release in onDestroy
         * no need to consider power consumption for temporary
          */
        Utils.acquireIRPairingWakeLock(this);
    }

    @Override
    protected void onPause() {
        if(mRoomHubMgr != null){
            mRoomHubMgr.unRegisterRoomHubChange(this);
            mRoomHubMgr.unRegisterAssetListener(this);
            mRoomHubMgr.registerAssetChangeListener(this, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        }
        if(mHealthDeviceManager != null) {
            mHealthDeviceManager.unregisterHealthDeviceChangeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(mRoomHubMgr != null) {
            mRoomHubMgr.registerRoomHubChange(this);
            mRoomHubMgr.registerAssetListener(this);
            mRoomHubMgr.unRegisterAssetChangeListener(this, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);

            mCurUuid=getIntent().getExtras().getString(RoomHubManager.KEY_UUID);
            mData=mRoomHubMgr.getRoomHubDataByUuid(mCurUuid);
            mElectricList.setList(mData.getAssetList());
        }
        if(mHealthDeviceManager != null) {
            mHealthDeviceManager.registerHealthDeviceChangeListener(this);
            mElectricList.setList(mHealthDeviceManager.getAllHealthDeviceList());
        }
        initLayout();
        super.onResume();
    }

    private HealthData getHealthDataByUuid(String uuid) {
        for(int i=0;i<mElectricList.getList().size();i++) {
            HealthData healthData = (HealthData) mElectricList.getItem(i);
            if(healthData.getUuid().equals(uuid))
                return healthData;
        }
        return null;
    }

    private void refreshList(int message, HealthData healthData) {
        HealthData data = getHealthDataByUuid(healthData.getUuid());
        Log.d(TAG,"refreshList enter");
        if(data != null) {
            if(message == MESSAGE_REMOVE_DEVICE) {
                Log.d(TAG,"remove device");
                mElectricList.removeFromList(data);
            }
        }
        else {
            if(message == MESSAGE_ADD_DEVICE) {
                Log.d(TAG,"add device");
                mElectricList.addToList(healthData);
            }
        }
        Collections.sort(mElectricList.getList());
    }

    private void refreshUI() {
        if(mElectricList.getList().size() > 0) {
            contentLayout.setVisibility(View.VISIBLE);
            addNewLayout.setVisibility(View.GONE);
        }
        else {
            contentLayout.setVisibility(View.GONE);
            addNewLayout.setVisibility(View.VISIBLE);
            TextView textView = (TextView)addNewLayout.findViewById(R.id.dev_title);
            String text = getString(R.string.add_electric);
            if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH)
                text = getString(R.string.healthcare_add_device);
            textView.setText(text);
        }
    }

    private void initLayout(){
        contentLayout = findViewById(R.id.contentLayout);
        addNewLayout = findViewById(R.id.ll_add_dev);

        mBtnAdd = (Button) findViewById(R.id.btn_add_electric);
        mBtnAdd.setVisibility(View.GONE);
//        mBtnAdd.setOnClickListener(this);

        TextView title = (TextView)findViewById(R.id.electric_title);
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            title.setText(R.string.current_device);
        }

        mElectricGv =  (GridView) findViewById(R.id.electric_lst);
        mElectricAdapter =new ElectricAdapter(this,mDeviceCategory,mElectricList);
        mElectricGv.setAdapter(mElectricAdapter);

        ImageView btnAdd = (ImageView)addNewLayout.findViewById(R.id.btn_add_dev);
        btnAdd.setOnClickListener(this);
        refreshUI();
    }

    public int getDrawbleResourceByType(Object item) {
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            AssetInfoData data = (AssetInfoData) item;

            switch (data.getAssetType()) {
                case DeviceTypeConvertApi.TYPE_ROOMHUB.AC:
                    return R.drawable.btn_add_ac;
                case DeviceTypeConvertApi.TYPE_ROOMHUB.FAN:
                    return R.drawable.btn_add_fan;
                case DeviceTypeConvertApi.TYPE_ROOMHUB.PM25:
                    return R.drawable.btn_pm;
                case DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER:
                    return R.drawable.btn_ion_enable;
                case DeviceTypeConvertApi.TYPE_ROOMHUB.TV:
                    return R.drawable.btn_tv;
                case DeviceTypeConvertApi.TYPE_ROOMHUB.BULB:
                    return R.drawable.btn_lamp_off;
            }
        }
        else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            HealthData healthData = (HealthData)item;
            switch (healthData.getType()) {
                case DeviceTypeConvertApi.TYPE_HEALTH.BPM:
                    return R.drawable.btn_bp_enable;
            }
        }
        return -1;
    }

    public String getModelsByType(Object item){
        String str_models = "";
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            str_models = mContext.getResources().getString(R.string.ac_na);
            if(item != null){
                AssetInfoData data = (AssetInfoData) item;

                String brand = data.getBrandName();
                String device = data.getModelNumber();

                if(!TextUtils.isEmpty(brand) && !TextUtils.isEmpty(device))
                    str_models=brand +"/"+device;
            }
        }
        else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            HealthData healthData = (HealthData)item;
            if(!TextUtils.isEmpty(healthData.getBrandName()) &&
                    !TextUtils.isEmpty(healthData.getModelNumber()))
                str_models = healthData.getBrandName()+"/"+healthData.getModelNumber();
        }

        return str_models;
    }

    public String getDefaultUser(Object item) {
        String nickName = "";
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            HealthData healthData = (HealthData)item;
            if(healthData.getFriendData() != null) {
                nickName = healthData.getFriendData().getNickName();
                if (TextUtils.isEmpty(nickName))
                    nickName = healthData.getFriendData().getUserAccount();
            }
        }
        return nickName;
    }

    public int getConnectionType(Object item){
        int connectionType = -1;
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            AssetInfoData data = (AssetInfoData) item;
            connectionType = data.getConnectionType();
        }
        else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            HealthData healthData = (HealthData)item;
            connectionType=healthData.getConnectionType();
        }

        return connectionType;
    }

    public void DeleteElectric(int pos){
        DeleteDeviceDialog(pos);
    }

    private void DeleteDeviceDialog(final int pos){
        // final String str_uuid=uuid;
        final Dialog dialog = new Dialog(this, R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_delete_dialog);

        TextView txt_msg = (TextView) dialog.findViewById(R.id.txt_message);
        String str_msg="";
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            final AssetInfoData asset_data= (AssetInfoData) mElectricList.getItem(pos);
            BaseAssetManager base_asset = mRoomHubMgr.getAssetDeviceManager(asset_data.getAssetType());

            str_msg = String.format(getString(R.string.confirm_del_electric),base_asset.getAssetName());
        }
        else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
            final HealthData healthData = (HealthData)mElectricList.getItem(pos);
            str_msg = String.format(getString(R.string.confirm_del_electric), healthData.getDeviceName());
        }
        txt_msg.setText(str_msg);

        Button btn_no = (Button) dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_yes = (Button) dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showProgressDialog("", getString(R.string.processing_str));
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
                            final AssetInfoData asset_data= (AssetInfoData) mElectricList.getItem(pos);
                            int retval = mRoomHubMgr.RemoveElectric(mCurUuid,asset_data.getAssetUuid(), asset_data.getAssetType());
                        }
                        else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
                            final HealthData healthData = (HealthData)mElectricList.getItem(pos);
                            mHealthDeviceManager.unRegDeviceToCloud(healthData.getType(),healthData.getUuid());
                            dismissProgressDialog();
                        }
                    }
                };
                thread.start();
            }
        });
        dialog.show();
    }

    public void RePairing(int pos){
        if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            AssetInfoData asset_data = (AssetInfoData)mElectricList.getItem(pos);

            if (asset_data != null) {
                if(asset_data.getConnectionType() == AssetDef.CONNECTION_TYPE_IR)
                    configIRSetting(asset_data.getAssetUuid(), asset_data.getAssetType());
            }
        }else{
            HealthData healthData = (HealthData) mElectricList.getItem(pos);
            if(healthData != null){
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), BLEPairingDefaultUserActivity.class);
                intent.putExtra(AssetDef.ASSET_TYPE, healthData.getType());
                intent.putExtra(AssetDef.ASSET_UUID,healthData.getUuid() );
                intent.putExtra(AssetDef.ASSET_DEFAULT_USER,healthData.getFriendData().getUserId());
                startActivity(intent);
            }
        }
    }

    public void Rename(int pos){
        if(mDeviceCategory != DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
            HealthData healthData = (HealthData) mElectricList.getItem(pos);

            if (healthData != null) {
                Intent intent = new Intent(mContext, AboutHealthcareActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Bundle bundle = new Bundle();
                bundle.putParcelable(GlobalDef.KEY_DEVICE_DATA, healthData);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        }
    }

    public void configIRSetting(String electric_uuid,int type) {

        BaseAssetManager base_aset = mRoomHubMgr.getAssetDeviceManager(type);
        base_aset.configIRSetting(mCurUuid,electric_uuid);

        Intent intent = new Intent(this,IRSettingActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mCurUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, type);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID,electric_uuid);
        startActivityForResult(intent, 0);

     //   finish();
    }

    /*
    private void StartBLEPairing(int type){
        if(type == RoomHubDef.ELECTRIC_PARTICULATE_MATTER){
            getPMManager().startBLEPairing(mCurUuid);
        }
    }
    */
    /*
    private ElectricData getElectricDataByType(ElectricData data){

        synchronized (mElectricList){
            for (Iterator<ElectricData> it = (Iterator<ElectricData>) mElectricList.iterator(); it.hasNext(); ) {
                ElectricData electric = it.next();
                if(electric.getType() == data.getType())
                    return electric;
            }
        }

        return null;
    }

    private ElectricData getElectricDataByUuid(String uuid){
        if(mElectricList==null) return null;

        String data_uuid;
        for (Iterator<ElectricData> it = (Iterator<ElectricData>) mElectricList.iterator(); it.hasNext();) {
            ElectricData data = it.next();
            data_uuid=data.getElectricUuid();
            if(data_uuid!=null) {
                if (data_uuid.equals(uuid)) {
                    return data;
                }
            }
        }
        return null;
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Utils.releaseIRPairingWakeLock();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.btn_add_dev:
            case R.id.btn_add_electric:
                if(mDeviceCategory== DeviceTypeConvertApi.CATEGORY.ROOMHUB) {
                    if (!mData.IsAlljoyn())
                        Toast.makeText(this, getResources().getString(R.string.roomhub_warning_msg), Toast.LENGTH_SHORT).show();
                    else {
                        intent = new Intent(this, AddElectricActivity.class);
                        intent.putExtra(AssetDef.ADD_STATUS, AssetDef.ADD.ASSET);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(RoomHubManager.KEY_UUID, mCurUuid);
                        startActivity(intent);
                    }
                }
                else if(mDeviceCategory == DeviceTypeConvertApi.CATEGORY.HEALTH) {
                    if (!getAccountManager().isLogin()) {
                        Utils.ShowLoginActivity(this, RoomHubMainPage.class);
                        return;
                    }

                    if(Utils.isAllowToAddHealthcareDevice(mContext)) {
                        intent = new Intent(this, AddHealthcareActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }
                break;
        }
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data!=null) {
            if(data.getUuid().equals(data.getUuid()))
                mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result) {
        if(result < ErrorKey.Success) return;

        if(asset_info_data != null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_DEVICE, asset_info_data));
    }

    @Override
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REMOVE_DEVICE, asset_info_data));
    }

    @Override
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_DEVICE, asset_info_data));
    }

    @Override
    public void onAssetResult(String uuid, String asset_uuid,int result) {
        dismissProgressDialog();
        if(uuid.equals(mCurUuid)) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_CMD_RESULT, result));
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            if ((type == RoomHubManager.UPDATE_ROOMHUB_DATA)) {
                if (data.getUuid().equals(mCurUuid) && !data.IsOnLine()) {
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
        if(uuid.equals(mCurUuid) && (is_upgrade == true)){
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
    }

    @Override
    public void addDevice(int asset_type, Object data) {

    }

    @Override
    public void removeDevice(int asset_type, Object data) {

    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_AC_DATA, data));
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {

    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

    }

    @Override
    public void updateDevice(int type, HealthData device) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_DEVICE, device));
    }

    @Override
    public void addDeivce(HealthData device) {
        if(device != null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_DEVICE, device));
    }

    @Override
    public void removeDevice(HealthData device) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REMOVE_DEVICE, device));
    }
}

