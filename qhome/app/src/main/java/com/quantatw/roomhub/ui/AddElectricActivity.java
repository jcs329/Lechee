package com.quantatw.roomhub.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.BLEPairDef;
import com.quantatw.roomhub.utils.RoomHubDef;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 95010915 on 2015/9/25.
 */
public class AddElectricActivity extends AbstractRoomHubActivity implements View.OnClickListener,RoomHubChangeListener,AssetListener {
    private static final String TAG = AddElectricActivity.class.getSimpleName();
    private static boolean DEBUG=true;

    private RoomHubManager mRoomHubMgr;
    private TextView mTxtDevName;
    private Context mContext;

    private GridView mContentGridView;
    private ContentAdapter mContentAdapter;
    private ArrayList<ContentItem> mContentList;

    private String mCurUuid;
    private AssetDef.ADD mAddStatus;
    private RoomHubData mData;
    private boolean mdoIRPair=false;
    private final static int MESSAGE_ADD_ELECTRIC_DEVICE       = 100;
    private final static int MESSAGE_REMOVE_ELECTRIC_DEVICE    = 101;
    private final static int MESSAGE_UPDATE_ROOMHUB_DATA       = 102;
    private final static int MESSAGE_SHOW_CMD_RESULT           = 103;

    private class ContentItem {
        int category;
        int deviceType;
        String resTitle;
        int resDrawable;
        boolean isSupport;
        int connectionType;

        ContentItem(int category,int deviceType, String title, int drawable,boolean is_support,int connection_type) {
            this.category = category;
            this.deviceType = deviceType;
            this.resTitle = title;
            this.resDrawable = drawable;
            this.isSupport=is_support;
            this.connectionType=connection_type;
        }
    }

    private class ViewHolder {
        ImageView imageView;
        TextView titleTextView;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_ADD_ELECTRIC_DEVICE:
                    //dismissProgressDialog();
                    Electric_AddDevice((AssetInfoData)msg.obj);
                    break;
                case MESSAGE_REMOVE_ELECTRIC_DEVICE:
                    break;
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    RoomHubData data=(RoomHubData)msg.obj;
                    if((data != null) && data.getUuid().equals(mCurUuid)){
                        int type=msg.arg1;
                        if(type == RoomHubManager.UPDATE_ONLINE_STATUS){
                            if(!data.IsOnLine())
                                finish();
                        }else if (type == RoomHubManager.UPDATE_ROOMHUB_NAME)
                            mTxtDevName.setText(data.getName());
                        mData=data;
                    }

                    break;
                case MESSAGE_SHOW_CMD_RESULT:
                    int retval=(int)msg.obj;
                    Toast.makeText(mContext, Utils.getErrorCodeString(mContext,retval), Toast.LENGTH_SHORT).show();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_assets);

        getWindow().setBackgroundDrawableResource(R.drawable.background);
        mContext=this;
        mRoomHubMgr=getRoomHubManager();
        mAddStatus= (AssetDef.ADD) getIntent().getExtras().getSerializable(AssetDef.ADD_STATUS);
        mCurUuid=getIntent().getExtras().getString(RoomHubManager.KEY_UUID);
        mData=mRoomHubMgr.getRoomHubDataByUuid(mCurUuid);

        mTxtDevName = (TextView) findViewById(R.id.txt_dev_name);
        if(mAddStatus == AssetDef.ADD.CONNECTION_TYPE) {
            String asset_name=getIntent().getExtras().getString(AssetDef.ASSET_NAME);
            getActionBar().setTitle(String.format(getResources().getString(R.string.connection_type_title),asset_name));
            mTxtDevName.setText(getResources().getString(R.string.asset_connection_type_string));
        }else{
            getActionBar().setTitle(getResources().getString(R.string.add_electric));
            mTxtDevName.setText(mData.getName());
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
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        mdoIRPair=false;
        if(mRoomHubMgr != null) {
            mRoomHubMgr.registerRoomHubChange(this);
            mRoomHubMgr.registerAssetListener(this);
        }
        initLayout();
        super.onResume();
    }

    private void initLayout(){

        View view = findViewById(R.id.electric_list);
        mContentGridView = (GridView)view.findViewById(R.id.assets_content);
        if(mAddStatus == AssetDef.ADD.CONNECTION_TYPE) {
            mContentList = obtainConnectionTypeContentList(getIntent().getExtras().getInt(AssetDef.ASSET_TYPE));
        }else {
            mContentList = obtainContentList();
        }
        mContentAdapter = new ContentAdapter(this, mContentList);
        mContentGridView.setAdapter(mContentAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.releaseIRPairingWakeLock();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK) {
            if(mRoomHubMgr != null) {
                String uuid = data.getStringExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID);
                int assetType = data.getIntExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, -1);
                String asset_uuid=data.getStringExtra(IRSettingDataValues.KEY_ELECTRIC_UUID);
                mRoomHubMgr.RemoveElectric(uuid,asset_uuid, assetType);
            }
        }
//        finish();
    }

    private void Electric_AddDevice(AssetInfoData data){
        configIRSetting(data.getAssetUuid(), data.getAssetType());
    }


    public void configIRSetting(String electric_uuid,int type) {
        BaseAssetManager base_asset = mRoomHubMgr.getAssetDeviceManager(type);

        base_asset.configIRSetting(mCurUuid,electric_uuid);

        Intent intent = new Intent(this,IRSettingActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IRSettingDataValues.KEY_DATA_ROOM_HUB_DEVICE_UUID, mCurUuid);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_TYPE, type);
        intent.putExtra(IRSettingDataValues.KEY_ELECTRIC_UUID, electric_uuid);
        startActivityForResult(intent, 0);

    }

    private void StartBLEPairing(int type){
        BaseAssetManager base_asset = mRoomHubMgr.getAssetDeviceManager(type);

        if(type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) {
            if (mData.getAssetCount(type) >= getResources().getInteger(R.integer.config_bulb_max_quantity)) {
                Toast.makeText(mContext, R.string.electric_reach_limit, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        base_asset.startBLEPairing(mCurUuid);
        if(type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) {
            Intent intent = new Intent(mContext, BulbBLEPairingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BLEPairDef.BLE_STATUS, BLEPairDef.STATUS.START);
            mContext.startActivity(intent);
        }
    }

    private ArrayList<ContentItem> obtainContentList() {
        ArrayList<ContentItem> list = new ArrayList<>();

        ArrayList<BaseAssetManager> asset_list = mRoomHubMgr.getAllAssetDevice();

        for (Iterator<BaseAssetManager> it = asset_list.iterator(); it.hasNext(); ) {
            BaseAssetManager base_asset = it.next();
            int asset_type = base_asset.getAssetType();
            list.add(new ContentItem(RoomHubDef.getCategory(asset_type),asset_type,base_asset.getAssetName(),
                    base_asset.getAssetIcon(),true,base_asset.getConnectionType()));
        }

        /*
        * 20160728: Bulb is not supported in App for temporary
        * set config_buld to false in config.xml
        * add fake bulb content here to show icon
         */
        list.add(new ContentItem(RoomHubDef.getCategory(DeviceTypeConvertApi.TYPE_ROOMHUB.BULB),
                DeviceTypeConvertApi.TYPE_ROOMHUB.BULB,
                getString(R.string.electric_lamp),
                R.drawable.bulb_btn_selector,
                false,
                AssetDef.CONNECTION_TYPE_BT));

        return list;
    }

    private ArrayList<ContentItem> obtainConnectionTypeContentList(int asset_type) {
        ArrayList<ContentItem> list = new ArrayList<>();

        list.add(
                new ContentItem(
                        AssetDef.CONNECTION_TYPE_IR,
                        asset_type,
                        getString(R.string.connection_type_IR),
                        R.drawable.connection_type_ir_btn_selector,
                        true,
                        AssetDef.CONNECTION_TYPE_IR));
        list.add(
                new ContentItem(
                        AssetDef.CONNECTION_TYPE_BT,
                        asset_type,
                        getString(R.string.connection_type_BT),
                        R.drawable.connection_type_bt_btn_selector,
                        true,
                        AssetDef.CONNECTION_TYPE_BT));
        list.add(
                new ContentItem(
                        AssetDef.CONNECTION_TYPE_WIFI,
                        asset_type,
                        getString(R.string.connection_type_WIFI),
                        R.drawable.connection_type_wifi_btn_selector,
                        true,
                        AssetDef.CONNECTION_TYPE_WIFI));
        return list;
    }

    private class ContentAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<ContentItem> mList;

        ContentAdapter(Context context, ArrayList<ContentItem> list) {
            mContext = context;
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.asset_grid_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.asset_image);
                viewHolder.titleTextView = (TextView)convertView.findViewById(R.id.asset_title);
                convertView.setTag(viewHolder);
            }
            else
                viewHolder = (ViewHolder)convertView.getTag();

            final ContentItem contentItem = (ContentItem)getItem(position);
            viewHolder.imageView.setBackgroundResource(contentItem.resDrawable);
            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (!mData.IsAlljoyn()){
                        Toast.makeText(mContext, R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final ContentItem contentItem = (ContentItem) getItem(position);

                    if(!contentItem.isSupport){
                        Toast.makeText(mContext,R.string.coming_soon,Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if((contentItem.deviceType == DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER) ||
                            (contentItem.deviceType == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25)){
                        if(mData.AssetIsExist(contentItem.deviceType) != null){
                            Toast.makeText(mContext,mContext.getString(R.string.electric_is_exist),Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if(contentItem.connectionType == AssetDef.CONNECTION_TYPE_BT) {
                        StartBLEPairing(contentItem.deviceType);
                    }else if(contentItem.connectionType == AssetDef.CONNECTION_TYPE_ALL) {
                        Intent intent=new Intent(mContext,AddElectricActivity.class);
                        intent.putExtra(AssetDef.ADD_STATUS, AssetDef.ADD.CONNECTION_TYPE);
                        intent.putExtra(AssetDef.ASSET_NAME, contentItem.resTitle);
                        intent.putExtra(AssetDef.ASSET_TYPE, contentItem.deviceType);
                        intent.putExtra(RoomHubManager.KEY_UUID, mCurUuid);

                        startActivity(intent);
                    }else if(contentItem.connectionType == AssetDef.CONNECTION_TYPE_WIFI) {
                        Toast.makeText(mContext,R.string.coming_soon,Toast.LENGTH_SHORT).show();
                    }else{
                        showProgressDialog("", getString(R.string.processing_str));
                        mdoIRPair=true;
                        mRoomHubMgr.AddElectric(mCurUuid, null, contentItem.deviceType,AssetDef.CONNECTION_TYPE_IR);
                    }
                }
            });
            viewHolder.titleTextView.setText(contentItem.resTitle);

            return convertView;
        }
    }

    @Override
    public void addDevice(RoomHubData data) {

    }

    @Override
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result) {
        if (data.getUuid().equals(mCurUuid) && mdoIRPair) {
            dismissProgressDialog();
            if (result < ErrorKey.Success)
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_CMD_RESULT, result));
            else
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_ELECTRIC_DEVICE, asset_info_data));
        }
    }

    @Override
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {

    }

    @Override
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {

    }

    @Override
    public void onAssetResult(String uuid, String asset_uuid,int result) {
        if(result < ErrorKey.Success) {
            dismissProgressDialog();
            if (uuid.equals(mCurUuid)) {
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_CMD_RESULT, result));
            }
        }
    }

    @Override
    public void removeDevice(RoomHubData data) {
        if(data != null){
            if(data.getUuid().equals(mCurUuid))
                finish();
        }
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(data != null) {
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA,type,0,data));
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
           finish();
        }
    }
}

