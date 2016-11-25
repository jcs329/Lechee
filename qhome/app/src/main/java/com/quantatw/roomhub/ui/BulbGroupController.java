package com.quantatw.roomhub.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
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
public class BulbGroupController extends AbstractRoomHubActivity implements CheckBox.OnCheckedChangeListener,View.OnClickListener,RoomHubChangeListener,AssetChangeListener,SeekBar.OnSeekBarChangeListener {
    private static final String TAG = BulbGroupController.class.getSimpleName();
    private static boolean DEBUG=true;
    private RoomHubManager mRoomHubMgr;
    private BulbManager mBulbManager;

    private TextView mTxtDevName;
//    private TextView mTxtBulbName;

    private int checkNum;
    private ListView mListVBulb;
    private BulbGroupAdapter adapter;
    private CheckBox mCheckBSelectAll;
    private LinearLayout ll_info;
    private LinearLayout ll_dimmer_bar;
    private ImageView mImgClose;
    private SeekBar mDimmerBar;
    private Button mBtnCloseBulb, mBtnOpenBulb;
    private boolean isPressList=false;
    private TextView mTxtNumSelectBulb;
    private List<BulbData> mBulbDataList;
    private BulbData mBulbData;

    private final int MESSAGE_UPDATE_ROOMHUB_DATA       = 100;
    private final int MESSAGE_UPDATE_BULB_DATA = 101;
    private final int MESSAGE_LAUNCH_DEVICE_LIST        = 102;
    private final int MESSAGE_SHOW_PROGRESS_DIALOG      = 103;
    private final int MESSAGE_DISMISS_PROGRESS_DIALOG   = 104;
    private final int MESSAGE_SHOW_TOAST                = 105;

    private String roomHubUUID;
    private String mCurUuid;
    private Context mContext;

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
        setContentView(R.layout.bulb_group_controller);
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        mContext=this;

//        mTxtBulbName=(TextView)findViewById(R.id.txt_lamp_name);
        mCheckBSelectAll=(CheckBox)findViewById(R.id.chkb_selectall);

        mListVBulb=(ListView)findViewById(R.id.list_lamp) ;
        ll_info=(LinearLayout)findViewById(R.id.ll_info);
        ll_dimmer_bar=(LinearLayout)findViewById(R.id.ll_dimmer_bar);
        mImgClose=(ImageView)findViewById(R.id.img_close);
        mImgClose.setOnClickListener(this);
        mTxtNumSelectBulb=(TextView)findViewById(R.id.txt_select_bulb_num);
        mDimmerBar=(SeekBar)findViewById(R.id.dimmer_seekbar);
        mDimmerBar.setOnSeekBarChangeListener(this);
        mBtnCloseBulb=(Button)findViewById(R.id.btn_close_lamp);
        mBtnOpenBulb=(Button)findViewById(R.id.btn_open_lamp);
        mBtnCloseBulb.setOnClickListener(this);
        mBtnOpenBulb.setOnClickListener(this);
        mRoomHubMgr=getRoomHubManager();
        mBulbManager = (BulbManager)mRoomHubMgr.getAssetDeviceManager(DeviceTypeConvertApi.TYPE_ROOMHUB.BULB);

        mCurUuid=getIntent().getStringExtra(RoomHubManager.KEY_UUID);
        mBulbData = mBulbManager.getBulbDataByUuid(mCurUuid);
        if(mBulbData == null) {
            finish();
        }else {
            roomHubUUID = mBulbData.getRoomHubUuid();
            initLayout();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mRoomHubMgr.registerRoomHubChange(this);
        mBulbManager.registerAssetsChange(this);

        mRoomHubMgr.setLed(mBulbData.getRoomHubUuid(), RoomHubDef.LED_COLOR_BLUE, RoomHubDef.LED_FLASH, 3000, 0, 1);
        mBulbDataList = mBulbManager.getBulbList(roomHubUUID);
        if (mBulbDataList.size() != 0) {
            adapter.setList(mBulbDataList);
            updateStatus();
            UpdateBulbLayout();
        }else{
            mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
        }
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

    private void initLayout(){

        mCheckBSelectAll.setOnCheckedChangeListener(this);
        mBulbDataList = mBulbManager.getBulbList(mBulbData.getRoomHubUuid());
        adapter = new BulbGroupAdapter(this,mBulbDataList);
        mListVBulb.setAdapter(adapter);
        mListVBulb.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String uuid = mBulbDataList.get(position).getAssetUuid();
                if (adapter.getStatusMap().get(uuid) == BulbGroupAdapter.STATUS_DISABLE){
                    return;
                }
                BulbGroupAdapter.ViewHolder holder = (BulbGroupAdapter.ViewHolder) view.getTag();
                holder.checkBox.toggle();

                adapter.getStatusMap().put(uuid, holder.checkBox.isChecked() ?  BulbGroupAdapter.STATUS_CHECKED: BulbGroupAdapter.STATUS_UNCHECKED);

                if (holder.checkBox.isChecked()) {
                    checkNum ++;
                    if (isAllSelected()){
                        mCheckBSelectAll.setChecked(true);
                    }
                    holder.ll.setBackgroundColor(mContext.getResources().getColor(R.color.color_list_item_bg));
                } else {
                    checkNum --;
                    mCheckBSelectAll.setOnCheckedChangeListener(null);
                    mCheckBSelectAll.setChecked(false);
                    mCheckBSelectAll.setOnCheckedChangeListener(BulbGroupController.this);
                    holder.ll.setBackgroundColor(Color.TRANSPARENT);
                }
                ll_info.setVisibility(View.GONE);
                ll_dimmer_bar.setVisibility(View.VISIBLE);
                mTxtNumSelectBulb.setText(String.format(getString(R.string.bulb_group_control_luminance_selected_number),checkNum));
                //adapter.notifyDataSetChanged();

            }
        });

        mTxtNumSelectBulb.setText(String.format(getString(R.string.bulb_group_control_luminance_selected_number),checkNum));
        mTxtDevName=(TextView)findViewById(R.id.txt_device_name);
        mTxtDevName.setText(mBulbData.getRoomHubData().getName());
//        mTxtBulbName.setText(mBulbData.getName()+"");
    }

    @Override
    public void onClick(View v) {
        boolean is_show_progress=true;
         switch (v.getId()){
             case R.id.img_close:
                 is_show_progress=false;
                 ll_info.setVisibility(View.GONE);
                 ll_dimmer_bar.setVisibility(View.VISIBLE);
                 break;
             case R.id.btn_close_lamp:
                 for (int i = 0; i < mBulbDataList.size(); i++) {
                     if (adapter.getStatusMap().get(mBulbDataList.get(i).getAssetUuid()) == BulbGroupAdapter.STATUS_CHECKED) {
                         mBulbManager.setPower(mBulbDataList.get(i).getAssetUuid(), 0);
                     }
                 }
                 adapter.notifyDataSetChanged();
                 break;
             case R.id.btn_open_lamp:
                 for (int i = 0; i < mBulbDataList.size(); i++) {
                     if (adapter.getStatusMap().get(mBulbDataList.get(i).getAssetUuid()) == BulbGroupAdapter.STATUS_CHECKED) {
                         mBulbManager.setPower(mBulbDataList.get(i).getAssetUuid(), 1);
                     }
                 }
                 adapter.notifyDataSetChanged();
                 break;
        }
        if(is_show_progress) {
            log("showProgressDialog");
            if(!isShowing())
                mHandler.sendEmptyMessage(MESSAGE_SHOW_PROGRESS_DIALOG);
        }
    }

    private void setSelectAll(boolean isSelectAll){
        checkNum = 0;
        for (int i = 0; i < mBulbDataList.size(); i++) {
            String uuid = mBulbDataList.get(i).getAssetUuid();
            if(isSelectAll) {
                if( adapter.getStatusMap().get(uuid) != BulbGroupAdapter.STATUS_DISABLE){
                    adapter.getStatusMap().put(uuid, BulbGroupAdapter.STATUS_CHECKED);
                    checkNum ++;
                }
            }else {
                if (adapter.getStatusMap().get(uuid) != BulbGroupAdapter.STATUS_DISABLE) {
                    adapter.getStatusMap().put(uuid, BulbGroupAdapter.STATUS_UNCHECKED);
                }
            }
        }
        mTxtNumSelectBulb.setText(String.format(getString(R.string.bulb_group_control_luminance_selected_number),checkNum));
        dataChanged();
    }

    private void dataChanged() {
        adapter.notifyDataSetChanged();
    }

    private void UpdateBulbLayout(){
        adapter.notifyDataSetChanged();
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
//                        mTxtDevName.setText(data.getName());
                        break;
                }
            }
        }
    }

    private void Controller_UpdateBulbData(BulbData data){
        if(data != null && roomHubUUID.equals(data.getRoomHubUuid())) {
            mBulbDataList = mBulbManager.getBulbList(roomHubUUID);
            adapter.setList(mBulbDataList);
            updateStatus();
            UpdateBulbLayout();
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
        for (int i = 0; i < mBulbDataList.size(); i++) {
            if (adapter.getStatusMap().get(mBulbDataList.get(i).getAssetUuid()) == BulbGroupAdapter.STATUS_CHECKED) {
                mBulbManager.setLuminance(mBulbDataList.get(i).getAssetUuid(), seekProgress);
            }
        }
        adapter.notifyDataSetChanged();
        log("seekProgress=" + seekProgress);
        mBulbManager.setLuminance(mCurUuid, seekProgress + 1);
    }


    private void log(String msg) {
        if(DEBUG)
            Log.d(TAG,msg);
    }

    @Override
    public void addDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null)) {
            if (roomHubUUID.equals(((BulbData)data).getRoomHubUuid())) {
                mBulbDataList = mBulbManager.getBulbList(roomHubUUID);
                adapter.setList(mBulbDataList);
                updateStatus();
                UpdateBulbLayout();
            }
        }
    }

    @Override
    public void removeDevice(int asset_type, Object data) {
        if((asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.BULB) & (data != null)) {
            if (roomHubUUID.equals(((BulbData)data).getRoomHubUuid())) {
                mBulbDataList = mBulbManager.getBulbList(roomHubUUID);
                if (mBulbDataList.size() != 0) {
                    adapter.setList(mBulbDataList);
                    updateStatus();
                    UpdateBulbLayout();
                }else{
                    mHandler.sendEmptyMessage(MESSAGE_LAUNCH_DEVICE_LIST);
                }
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

    private boolean isAllSelected(){
        for (int i = 0; i < mBulbDataList.size(); i++) {
            if (adapter.getStatusMap().get(mBulbDataList.get(i).getAssetUuid()) == BulbGroupAdapter.STATUS_UNCHECKED) {
               return false;
            }
        }
        return true;
    }

    private void updateStatus() {
        checkNum = 0;
        for (int i = 0; i < mBulbDataList.size(); i++) {
            BulbData data = mBulbDataList.get(i);
            String uuid = mBulbDataList.get(i).getAssetUuid();
            if (data.getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE) {
                adapter.getStatusMap().put(uuid, BulbGroupAdapter.STATUS_DISABLE);
            } else {
                if (adapter.getStatusMap().get(uuid) == null) {
                    adapter.getStatusMap().put(uuid, BulbGroupAdapter.STATUS_UNCHECKED);
                }else if (adapter.getStatusMap().get(uuid) == BulbGroupAdapter.STATUS_CHECKED) {
                    checkNum++;
                } else if (adapter.getStatusMap().get(uuid) == BulbGroupAdapter.STATUS_DISABLE){
                    adapter.getStatusMap().put(uuid, BulbGroupAdapter.STATUS_UNCHECKED);
                }
            }
        }
    }

    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param buttonView The compound button view whose state has changed.
     * @param isChecked  The new checked state of buttonView.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            setSelectAll(true);
        }else{
            setSelectAll(false);
        }
        ll_info.setVisibility(View.GONE);
        ll_dimmer_bar.setVisibility(View.VISIBLE);
    }
}
