package com.quantatw.roomhub.ui;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.quantatw.myapplication.R;
import com.quantatw.roomhub.manager.AccountManager;
import com.quantatw.roomhub.manager.BaseManager;
import com.quantatw.roomhub.manager.OTAManager;
import com.quantatw.roomhub.manager.ReminderData;
import com.quantatw.roomhub.manager.asset.listener.AssetChangeListener;
import com.quantatw.roomhub.manager.asset.listener.AssetListener;
import com.quantatw.roomhub.manager.asset.listener.RoomHubChangeListener;
import com.quantatw.roomhub.manager.asset.manager.ACData;
import com.quantatw.roomhub.manager.asset.manager.ACManager;
import com.quantatw.roomhub.manager.asset.manager.AssetInfoData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetData;
import com.quantatw.roomhub.manager.asset.manager.BaseAssetManager;
import com.quantatw.roomhub.manager.asset.manager.PMData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubData;
import com.quantatw.roomhub.manager.asset.manager.RoomHubManager;
import com.quantatw.roomhub.utils.ACDef;
import com.quantatw.roomhub.utils.AssetDef;
import com.quantatw.roomhub.utils.FailureCauseInfo;
import com.quantatw.roomhub.utils.RoomHubFailureCause;
import com.quantatw.roomhub.utils.SupportVersion;
import com.quantatw.roomhub.utils.Utils;
import com.quantatw.sls.api.AQIApi;
import com.quantatw.sls.api.DeviceTypeConvertApi;
import com.quantatw.sls.device.CloudDevice;
import com.quantatw.sls.key.ErrorKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by 10102098 on 2016/5/16.
 */
public class AppliancesTabFrag extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener,RoomHubChangeListener,AssetListener,AssetChangeListener {
    private static final String TAG = "AppliancesTabFrag";
    private static boolean DEBUG=true;
    private ArrayList<RoomHubData> mRoomHubDevList=null;
    private AccountManager mAccountMgr;
    private RoomHubManager mRoomHubMgr;
    /*
    private ACManager mACMgr;
    private FANManager mFANMgr;
    private TVManager mTVMgr;
    private AirPurifierManager mAirPurifierMgr;
    private PMManager mPMMgr;
    */
    private RoomHubAdapter mAdapter;

    private GridView devlist_gv;
    private LinearLayout ll_devlist;
    private LinearLayout ll_add_hub;
    private ImageView btn_add_hub;
    private View ApplianceTabView;

    private final int MESSAGE_ADD_DEVICE            = 100;
    private final int MESSAGE_REMOVE_DEVICE         = 101;
    private final int MESSAGE_UPDATE_ROOMHUB_DATA   = 102;
    private final int MESSAGE_LEAVE_HUB_ERROR       = 103;
    private final int MESSAGE_ADD_ELECTRIC_DEVICE   = 104;
    private final int MESSAGE_REMOVE_ELECTRIC_DEVICE= 105;
    private final int MESSAGE_UPDATE_ELECTRIC_DEVICE= 106;
    private final int MESSAGE_UPDATE_UPGRADE_STATUS = 107;
    //private final int MESSAGE_UPDATE_AC_DATA        = 108;
    private final int MESSAGE_UPDATE_ASSET_DATA        = 108;
    private final int MESSAGE_SHOW_TOAST            = 109;
    private final int MESSAGE_MONITOR_CLOSE_DEVICE  = 110;

    private final int MESSAGE_CHECK_DEVICE_IN = 200;
    private final int MESSAGE_SHOW_PROMPT_DIALOG = 300;

    protected static final int ROOMHUB_MENU1  = 0;
    protected static final int ROOMHUB_MENU2  = 1;
    protected static final int ROOMHUB_MENU3  = 2;
    protected static final int ROOMHUB_MENU4  = 3;

    private static final int AIR_QUALITY_GOOD   =   0;
    private static final int AIR_QUALITY_NORMAL =   1;
    private static final int AIR_QUALITY_DANGER =   2;

    private final String KEY_IS_UPGRADE = "is_upgrade";

    private int mCloseAllACTotalCheckCount = 10;
    private static final int CLOSE_ALL_AC_TIMER = 3000;

    private long exitTime = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG,"message what="+msg.what);
            switch(msg.what) {
                case MESSAGE_ADD_DEVICE:
                    ChangeDeviceList(RoomHubData.ACTION.ADD_DEVICE, (RoomHubData) msg.obj);
                    break;
                case MESSAGE_REMOVE_DEVICE:
                    ChangeDeviceList(RoomHubData.ACTION.REMOVE_DEVICE, (RoomHubData) msg.obj);
                    break;
                case MESSAGE_UPDATE_ROOMHUB_DATA:
                    UpdateItem(msg.arg1, (RoomHubData) msg.obj);
                    break;
                case MESSAGE_LEAVE_HUB_ERROR:
                    Toast.makeText(getActivity().getApplicationContext(), Utils.getErrorCodeString(getActivity().getApplicationContext(),(int)msg.obj), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_CHECK_DEVICE_IN:
                    if(mRoomHubDevList.size() <= 0)
                        sendReminderBroadcast();
                    break;
                case MESSAGE_ADD_ELECTRIC_DEVICE:
                    Electric_AddDevice((AssetInfoData) msg.getData().getParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(BaseAssetManager.KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_REMOVE_ELECTRIC_DEVICE:
                    Electric_RemoveDevice((AssetInfoData) msg.getData().getParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(BaseAssetManager.KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_ELECTRIC_DEVICE:
                    Electric_UpdateDevice((AssetInfoData) msg.getData().getParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA), (RoomHubData) msg.getData().getParcelable(BaseAssetManager.KEY_ROOMHUB_DATA));
                    break;
                case MESSAGE_UPDATE_UPGRADE_STATUS:
                    UpdateUpgradeStatus(msg.getData().getString(RoomHubManager.KEY_UUID), msg.getData().getBoolean(KEY_IS_UPGRADE));
                    break;
                /*
                case MESSAGE_UPDATE_AC_DATA:
                    ChangeACData((ACData) msg.obj);
                    break;
                */
                case MESSAGE_UPDATE_ASSET_DATA:
                    ChangeAssetData(msg.arg1,msg.obj);
                    break;
                case MESSAGE_SHOW_PROMPT_DIALOG:
                    promptRestoreMobileDialog();
                    break;
                case MESSAGE_SHOW_TOAST:
                    Toast.makeText(getActivity(), (int)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_MONITOR_CLOSE_DEVICE:
                    checkResult();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ApplianceTabView = inflater.inflate(R.layout.frag_appliances_tab, container, false);
        return ApplianceTabView;
    }

    @Override
    public void UpdateAssetData(int asset_type, Object data) {
        Log.d(TAG, "UpdateAssetData asset_type="+asset_type);
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ASSET_DATA, asset_type, 0, data));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void addDevice(RoomHubData data) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_ADD_DEVICE, data));
    }

    @Override
    public void removeDevice(RoomHubData data) {
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_REMOVE_DEVICE, data));
    }

    @Override
    public void UpdateRoomHubData(int type, RoomHubData data) {
        if(DEBUG)
            Log.d(TAG,"UpdateRoomHubData type="+type+" uuid="+data.getUuid());
        if(data != null)
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA,type,0 ,data));
        /*
        if((type==RoomHubManager.UPDATE_SENSOR_DATA)){
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_SENSOR_DATA, data));
        }else if((type==RoomHubManager.UPDATE_ROOMHUB_DATA)){
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_DATA, data));
        }else if((type==RoomHubManager.UPDATE_ROOMHUB_NAME)){
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ROOMHUB_NAME, data));
        }
        */
    }

    @Override
    public void UpdateDeviceShareUser(CloudDevice device) {

    }

    @Override
    public void UpgradeStatus(String uuid, boolean is_upgrade) {
        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_UPGRADE_STATUS;
        Bundle bundle=new Bundle();
        bundle.putString(RoomHubManager.KEY_UUID,uuid);
        bundle.putBoolean(KEY_IS_UPGRADE, is_upgrade);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void ChangeDeviceList(RoomHubData.ACTION action,RoomHubData data){
        if(data!=null) {
            RoomHubData old_data = getRoomHubDataByUuid(data.getUuid());
            if (old_data != null) {
                if (action == RoomHubData.ACTION.REMOVE_DEVICE) {
                    if (DEBUG)
                        Log.d(TAG, "ChangeDeviceList remove device uuid=" + data.getUuid());
                    mRoomHubDevList.remove(old_data);
                }
            } else {
                if (action == RoomHubData.ACTION.ADD_DEVICE) {
                    if (DEBUG)
                        Log.d(TAG, "ChangeDeviceList add device uuid=" + data.getUuid());
                    mRoomHubDevList.add(data);
                }
            }
            UpdateLayout();
            Collections.sort(mRoomHubDevList);

            mAdapter.notifyDataSetChanged();
        }
    }
    private RoomHubData getRoomHubDataByUuid(String uuid){
        if(mRoomHubDevList==null) return null;

        String data_uuid;
        for (Iterator<RoomHubData> it = mRoomHubDevList.iterator(); it.hasNext();) {
            RoomHubData data = it.next();
            data_uuid=data.getUuid();
            if(data_uuid!=null) {
                if (data_uuid.equals(uuid)) {
                    return data;
                }
            }
        }
        return null;
    }

    private int getIdxByUuid(String Uuid){
        if(mRoomHubDevList==null) return -1;

        for(int i=0;i<mRoomHubDevList.size();i++){
            if(Uuid.equals(mRoomHubDevList.get(i).getUuid())){
                return i;
            }
        }
        return -1;
    }

    private View getCurViewByUuid(String uuid){
        int first_visible=devlist_gv.getFirstVisiblePosition();
        int last_visible=devlist_gv.getLastVisiblePosition();
        int idx=getIdxByUuid(uuid);

        if(DEBUG)
            Log.d(TAG,"getCurViewByUuid uuid="+uuid+" idx="+idx+" first_visible="+first_visible+" last_visible="+last_visible);

        if(idx < 0) return null;

        if((idx >= first_visible) && (idx <= last_visible)) {
            int pos = idx - first_visible;

            return devlist_gv.getChildAt(pos);
        }

        return null;
    }

    private void UpdateItem(int type, final RoomHubData data){
        if(data == null) return;

        View v=getCurViewByUuid(data.getUuid());
        if(v != null){
            switch (type) {
                case RoomHubManager.UPDATE_ONLINE_STATUS: {
                    //ImageView btn_funmode_icon = (ImageView) v.findViewById(R.id.mode_icon);
                    ImageView btn_funmode_icon = (ImageView) v.findViewById(R.id.mode_icon2);
                    LinearLayout ll_msg = (LinearLayout) v.findViewById(R.id.ll_msg);
                    TextView tv_temp = (TextView) v.findViewById(R.id.txt_sensor_temp);
                    TextView tv_hum = (TextView) v.findViewById(R.id.txt_sensor_hum);

                    if (DEBUG)
                        Log.d(TAG, "UpdateItem UPDATE_ONLINE_STATUS uuid="+data.getUuid()+" IsUpgrade=" + data.IsUpgrade() + " IsOnLine=" + data.IsOnLine());

                    if (!data.IsUpgrade()) {
                        if (!data.IsOnLine()) {
                            LinearLayout ll_electric = (LinearLayout) v.findViewById(R.id.ll_electric);
                            LinearLayout ll_add_electric = (LinearLayout) v.findViewById(R.id.ll_add_electric);
                            TextView txt_msg = (TextView) v.findViewById(R.id.txt_msg);

                            tv_temp.setText("--°");
                            tv_hum.setText("--%");

                            //btn_funmode_icon.setBackground(getResources().getDrawable(R.drawable.icon_disconnected));
                            ll_electric.setVisibility(View.GONE);
                            ll_add_electric.setVisibility(View.GONE);
                            ll_msg.setVisibility(View.VISIBLE);
                            txt_msg.setText(R.string.device_offline);
                            btn_funmode_icon.setBackground(getResources().getDrawable(R.drawable.icon_disconnected));
                        } else {
                            //btn_funmode_icon.setBackground(null);
                            ll_msg.setVisibility(View.GONE);

                            UpdateSensorData(v, data);

                            UpdateElectricItem(v, data);

                        }

                    }

                    break;
                }
                case RoomHubManager.UPDATE_SENSOR_DATA: {
                    if(data.IsOnLine()) {
                        UpdateSensorData(v,data);
                    }
                    break;
                }
                /*
                case RoomHubManager.UPDATE_ROOMHUB_NAME:
                    TextView tv_dev_name = (TextView) v.findViewById(R.id.txt_devname);
                    tv_dev_name.setText(data.getName());
                    break;
                */
            }
        }
        int idx=getIdxByUuid(data.getUuid());
        if(idx >= 0) {
            mRoomHubDevList.set(idx, data);
            if (type == RoomHubManager.UPDATE_ROOMHUB_NAME) {
                Collections.sort(mRoomHubDevList);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private void UpdateElectricItem(View v, final RoomHubData data){
        //mAdapter.UpdateElectricBtnItem(v, data, getIdxByUuid(data.getUuid()));
        mAdapter.UpdateElectricBtnList(v, data, getIdxByUuid(data.getUuid()));
    }

    private void UpdateSensorData(View v,RoomHubData data){
        double sensor_temp=data.getSensorTemp();
        double sensor_hum=data.getSensorHumidity();

        if (DEBUG)
            Log.d(TAG, "UpdateSensorData temp=" + sensor_temp + " hum=" + sensor_hum);

        TextView tv_temp = (TextView) v.findViewById(R.id.txt_sensor_temp);
        TextView tv_hum = (TextView) v.findViewById(R.id.txt_sensor_hum);

        if(sensor_temp == ErrorKey.SENSOR_TEMPERATURE_INVALID)
            tv_temp.setText("--°");
        else
            tv_temp.setText(String.valueOf((int) Utils.getTemp(getActivity(), sensor_temp)) + "°");

        if(sensor_hum == ErrorKey.SENSOR_HUMIDITY_INVALID)
            tv_hum.setText("--%");
        else
            tv_hum.setText(String.valueOf((int) sensor_hum) + "%");

    }

    private void sendReminderBroadcast() {
        ReminderData reminderData = new ReminderData();
        reminderData.setSenderId(BaseManager.ROOMHUB_MANAGER);
        reminderData.setMessageId(RoomHubFailureCause.ID.H60Failure_Phone_004);
        reminderData.setSimpleMessage(getString(R.string.fail_msg_phone_004));
        FailureCauseInfo failureCauseInfo = reminderData.getFailureCauseInfo(getActivity());

        FailureCauseInfo.ButtonAction okButton = new FailureCauseInfo.ButtonAction();
        okButton.setButtonType(FailureCauseInfo.FailureButton.BUTTON_OK);
        okButton.setLaunchActionType(FailureCauseInfo.LaunchActionType.DO_NOTHING);
        failureCauseInfo.setActionButton1Message(okButton);

        FailureCauseInfo.ButtonAction wifiButton = new FailureCauseInfo.ButtonAction();
        wifiButton.setCustomButtonLabel(getString(R.string.fail_action_wifi));
        wifiButton.setLaunchActionType(FailureCauseInfo.LaunchActionType.LAUNCH_WIFI);
        failureCauseInfo.setActionButton2Message(wifiButton);

        FailureCauseInfo.ButtonAction activeButton = new FailureCauseInfo.ButtonAction();
        activeButton.setCustomButtonLabel(getActivity().getString(R.string.fail_action_mobile));
        activeButton.setLaunchActionType(FailureCauseInfo.LaunchActionType.LAUNCH_MOBILE_NETWORK);
        failureCauseInfo.setActionButton3Message(activeButton);

        Utils.sendReminderMessage(getActivity(), reminderData);
    }

    private void Electric_AddDevice(AssetInfoData asset_data, RoomHubData data){
        Log.d(TAG,"Electric_AddDevice uuid=" + data.getUuid());

        RoomHubData roomhub_data= getRoomHubDataByUuid(data.getUuid());
        if(roomhub_data != null){

            UpdateLayout();

            mAdapter.notifyDataSetChanged();
        }
    }

    private void Electric_RemoveDevice(AssetInfoData asset_data, RoomHubData data){
        Log.d(TAG,"Electric_RemoveDevice uuid=" + data.getUuid());
        RoomHubData roomhub_data=getRoomHubDataByUuid(data.getUuid());
        if(roomhub_data != null){
            UpdateLayout();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void Electric_UpdateDevice(AssetInfoData asset_data, RoomHubData data){
        Log.d(TAG,"Electric_UpdateDevice uuid=" + data.getUuid());
        RoomHubData roomhub_data=getRoomHubDataByUuid(data.getUuid());
        if(roomhub_data != null){
            UpdateLayout();
            mAdapter.notifyDataSetChanged();
        }
    }
    private void UpdateUpgradeStatus(String uuid,boolean is_upgrade){
        View v=getCurViewByUuid(uuid);
        if(v != null){
            //ImageView btn_funmode_icon = (ImageView) v.findViewById(R.id.mode_icon);
            ImageView btn_funmode_icon = (ImageView) v.findViewById(R.id.mode_icon2);
            LinearLayout ll_electric = (LinearLayout) v.findViewById(R.id.ll_electric);
            LinearLayout ll_add_electric = (LinearLayout) v.findViewById(R.id.ll_add_electric);
            LinearLayout ll_msg = (LinearLayout) v.findViewById(R.id.ll_msg);
            TextView txt_msg=(TextView) v.findViewById(R.id.txt_msg);
            if(DEBUG)
                Log.d(TAG,"UpdateUpgradeStatus uuid="+uuid+" is_upgradde="+is_upgrade);
            if(is_upgrade) {
                //btn_funmode_icon.setBackground(getResources().getDrawable(R.drawable.icon_renew));
                ll_electric.setVisibility(View.GONE);
                ll_add_electric.setVisibility(View.GONE);
                ll_msg.setVisibility(View.VISIBLE);
                btn_funmode_icon.setBackground(getResources().getDrawable(R.drawable.icon_renew));
                txt_msg.setText(R.string.device_upgrade);
            }else {
                RoomHubData data=getRoomHubDataByUuid(uuid);
                if(data.IsOnLine()) {
                    //btn_funmode_icon.setBackground(null);
                    ll_msg.setVisibility(View.GONE);
                    UpdateElectricItem(v, data);
                }
            }
        }

    }

    private void ChangeAssetData(int asset_type,Object data){
        String uuid = null;

        if(data == null) return;

        AssetInfoData asset_data=(AssetInfoData)data;
        String roomhub_uuid=asset_data.getRoomHubUuid();
        Log.d(TAG, "ChangeAssetData asset_type=" + asset_type + " roomhuUuid=" + roomhub_uuid + " asset_uuid=" + asset_data.getAssetUuid());
        RoomHubData roomhub_data=mRoomHubMgr.getRoomHubDataByUuid(roomhub_uuid);
        if(roomhub_data == null) return;
        Log.d(TAG, "ChangeAssetData asset_type=" + asset_type + " roomhuUuid=" + roomhub_uuid + " asset_uuid=" + asset_data.getAssetUuid());
        View v=getCurViewByUuid(roomhub_uuid);
        if(v != null){

            LinearLayout ll_electric_list=(LinearLayout)v.findViewById(R.id.ll_electric_list);
            int child_cnt=ll_electric_list.getChildCount();
            if(child_cnt > 0){
                ArrayList<AssetInfoData> asset_list=roomhub_data.getAssetListNoSameType();

                int idx=asset_list.indexOf(asset_data);
                Log.d(TAG, "ChangeAssetData idx=" +idx);
                if(idx < child_cnt) {
                    View item_view = ll_electric_list.getChildAt(idx);
                    if (item_view != null) {
                        RoomHubAdapter.ElectricBtnViewHolder btnViewHolder = (RoomHubAdapter.ElectricBtnViewHolder) item_view.getTag();
                        btnViewHolder.ConnStatus.setBackground(null);
                        btnViewHolder.pmStatus.setVisibility(View.INVISIBLE);
                        btnViewHolder.pmStatusTxt.setVisibility(View.INVISIBLE);
                        if(asset_data.getOnlineStatus() == AssetDef.ONLINE_STATUS_OFFLINE){
                            btnViewHolder.ConnStatus.setVisibility(View.VISIBLE);
                            btnViewHolder.ConnStatus.setBackground(getActivity().getResources().getDrawable(R.drawable.icon_bt_status_off));
                        } else {  //online
                            btnViewHolder.ConnStatus.setVisibility(View.INVISIBLE);
                            if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.PM25) {
                                btnViewHolder.pmStatus.setVisibility(View.VISIBLE);
                                btnViewHolder.pmStatusTxt.setVisibility(View.VISIBLE);

                                Log.d(TAG, "ChangeAssetData ChangeAssetData value=" + ((PMData) asset_data).getValue());
                                AQIApi.AQI_CATEGORY catetory=AQIApi.getAQICategoryByPM25Value(((PMData) asset_data).getValue());
                                //int pmAirQuality = getPMStatus(0, RoomHubDef.ELECTRIC_PARTICULATE_MATTER, null);
                                if (AQIApi.AQI_CATEGORY.GOOD == catetory) {
                                    btnViewHolder.pmStatus.setBackground(getActivity().getResources().getDrawable(R.drawable.lable_status_good));
                                    btnViewHolder.pmStatusTxt.setText(R.string.air_quality_good);
                                } else if (AQIApi.AQI_CATEGORY.NORMAL == catetory) {
                                    btnViewHolder.pmStatus.setBackground(getActivity().getResources().getDrawable(R.drawable.lable_status_normal));
                                    btnViewHolder.pmStatusTxt.setText(R.string.air_quality_normal);
                                } else if (AQIApi.AQI_CATEGORY.DANGER == catetory) {
                                    btnViewHolder.pmStatus.setBackground(getActivity().getResources().getDrawable(R.drawable.lable_status_bad));
                                    btnViewHolder.pmStatusTxt.setText(R.string.air_quality_danger);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void promptRestoreMobileDialog() {
        final Dialog dialog = new Dialog(getActivity(),R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.onboarding_done_restore_mobile));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(getString(R.string.enable_mobile_data));
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setPromptDisableMobileData(getActivity(), false);
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(
                        new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity"));
                startActivity(intent);
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setText(getString(R.string.continue_use_wifi));
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void checkResult(){
        ArrayList<String> failAsset=new ArrayList<String>();
        if(mRoomHubDevList.size() > 0) {
            for (int i = 0; i < mRoomHubDevList.size(); i++) {
                RoomHubData data = mRoomHubDevList.get(i);
                if (data != null) {
                    ArrayList<AssetInfoData> asset_list=data.getAssetList();

                    if(asset_list != null) {
                        for (Iterator<AssetInfoData> it = asset_list.iterator(); it.hasNext(); ) {
                            AssetInfoData asset_data = it.next();
                            if(asset_data.getAssetType() == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
                                ACData ac_data = (ACData)asset_data;
                                if ((ac_data != null) && (ac_data.getSubType() == ACDef.AC_SUBTYPE_SPLIT_TYPE)) {
                                    if (ac_data.getPowerStatus() == ACDef.POWER_ON) {
                                        if(DEBUG)
                                            Log.d(TAG,"checkResult fail asset uuid="+asset_data.getAssetUuid());
                                        failAsset.add(data.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(failAsset.size() == 0){
            mHandler.removeMessages(MESSAGE_MONITOR_CLOSE_DEVICE);
            ((RoomHubMainPage)getActivity()).dismissProgressDialog();
            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_SHOW_TOAST, R.string.close_all_device_msg));
        }else{
            mCloseAllACTotalCheckCount--;
            if(mCloseAllACTotalCheckCount == 0) {
                mHandler.removeMessages(MESSAGE_MONITOR_CLOSE_DEVICE);
                ((RoomHubMainPage)getActivity()).dismissProgressDialog();
                ShowCloseAllDeviceFailDialog(failAsset);
            }else{
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_MONITOR_CLOSE_DEVICE),CLOSE_ALL_AC_TIMER);
            }
        }
    }

    private void ShowCloseAllDeviceFailDialog(ArrayList<String> failAsset){
        if(failAsset.size() <=0) return;

        final Dialog dialog = new Dialog(getActivity(),R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getActivity().getResources().getString(R.string.close_ac_fail_msg));

        String str_msg="";
        for(int i=0;i<failAsset.size();i++){
            str_msg+=failAsset.get(i)+"\n";
        }

        if(DEBUG)
            Log.d(TAG,"ShowCloseAllDeviceFailDialog str_msg="+str_msg);

        TextView txt_msg1=(TextView)dialog.findViewById(R.id.txt_message1);
        txt_msg1.setVisibility(View.VISIBLE);
        txt_msg1.setText(str_msg);
        txt_msg1.setMovementMethod(ScrollingMovementMethod.getInstance());

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.ok);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.show();
    }

    @Override
    public void onCommandResult(int asset_type, String uuid, int result) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
        RoomHubData data = getRoomHubDataByIdx(position);
        if(data != null) {
            if(data.IsUpgrade()) {
                if(data.IsOwner())
                    launchOTAActivity(data.getUuid());
                else
                    Toast.makeText(getActivity(), R.string.device_upgrade_not_operate, Toast.LENGTH_SHORT).show();
                return;
            }

            if(data.IsOnLine()==true) {
                showProgressDialog("", getString(R.string.processing_str));
                Intent intent = new Intent();
                intent.setClass(this, RoomHubControllerFlipper.class);
                intent.putExtra(RoomHubManager.KEY_UUID,data.getUuid());

                startActivity(intent);
            }else
                Toast.makeText(getActivity(), R.string.device_offline_not_operate, Toast.LENGTH_SHORT).show();
        }
        */
    }

    @Override
    public void removeDevice(int asset_type, Object data) {
    }

    @Override
    public void onAssetResult(String uuid, String asset_uuid,int result) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_hub:
                ((RoomHubMainPage)getActivity()).addHub();
                break;
        }
    }

    @Override
    public void UpdatePageStatus(int asset_type, boolean enabled, Object data) {

    }

    @Override
    public void addDevice(int asset_type, Object data) {
        Log.d(TAG,"addDevice asset_type="+asset_type+" asset_uuid="+((AssetInfoData)data).getAssetUuid());
        mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_UPDATE_ASSET_DATA,asset_type,0, data));
    }

    @Override
    public void onResume() {
        super.onResume();

        mAccountMgr=((RoomHubMainPage)getActivity()).getAccountManager();
        mRoomHubMgr=((RoomHubMainPage)getActivity()).getRoomHubManager();
        mRoomHubMgr.registerRoomHubChange(this);
        mRoomHubMgr.registerAssetListener(this);

        for(BaseAssetManager asset_mgr:mRoomHubMgr.getAllAssetDevice()){
            Log.d(TAG,"registerAssetChangeListener asset_type="+asset_mgr.getAssetType());
            mRoomHubMgr.registerAssetChangeListener(this, asset_mgr.getAssetType());
        }

        mRoomHubDevList=mRoomHubMgr.getRoomHubDataList(mAccountMgr.isLogin());

        if(DEBUG)
            Log.d(TAG, "onResume dev_list count=" + mRoomHubDevList.size());

        mAdapter=new RoomHubAdapter(getActivity(), mRoomHubDevList);
        devlist_gv= (GridView) ApplianceTabView.findViewById(R.id.roomhub_devlist);
        devlist_gv.setAdapter(mAdapter);

        ll_devlist=(LinearLayout) ApplianceTabView.findViewById(R.id.ll_devlist);
        ll_add_hub=(LinearLayout) ApplianceTabView.findViewById(R.id.ll_add_hub);

        UpdateLayout();

        btn_add_hub=(ImageView)ApplianceTabView.findViewById(R.id.btn_add_hub);
        btn_add_hub.setOnClickListener(this);
        //  devlist_gv.setOnItemClickListener(this);

        if(mRoomHubDevList.size() <= 0) {
            Toast.makeText(getActivity(), R.string.no_device_message, Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(MESSAGE_CHECK_DEVICE_IN,2*60*1000);   // 2mins
        }

        UpdateLayout();
    }

    @Override
    public void onPause() {
        super.onPause();

        mRoomHubMgr.unRegisterRoomHubChange(this);
        mRoomHubMgr.unRegisterAssetListener(this);
        mRoomHubMgr.unRegisterAssetChangeListener(this, DeviceTypeConvertApi.TYPE_ROOMHUB.AC);
        /*
        mACMgr.unRegisterAssetsChange(this);
        mFANMgr.unRegisterAssetsChange(this);
        mTVMgr.unRegisterAssetsChange(this);
        mAirPurifierMgr.unRegisterAssetsChange(this);
        mPMMgr.unRegisterAssetsChange(this);
        */
        if(mRoomHubDevList!=null)
            mRoomHubDevList.clear();

        mHandler.removeMessages(MESSAGE_CHECK_DEVICE_IN);
    }

    @Override
    public void onStop() {
        ((RoomHubMainPage)getActivity()).dismissProgressDialog();
        super.onStop();
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        Log.d(TAG, "onKeyDown keyCode=" + keyCode + " action=" + event.getAction());
//        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
//            Log.d(TAG,"onKeyDown cur_time="+System.currentTimeMillis()+" exitTime="+exitTime);
//            if((System.currentTimeMillis()-exitTime) > 2000){
//                Toast.makeText(getActivity(), R.string.exit_device, Toast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            }
//            else{
//                // finish();
//                ((RoomHubApplication)getActivity().getApplication()).onTerminate();
//                System.exit(0);
//            }
//
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == getActivity().RESULT_OK && requestCode == IRSettingDataValues.REQUEST_CODE_IR_CONFIG_DONE) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void CloseAllDevices(){
        if(mRoomHubDevList != null){
            ((RoomHubMainPage)getActivity()).showProgressDialog("", getString(R.string.processing_str));
            Thread thread = new Thread() {
                @Override
                public void run() {
                    if(mRoomHubDevList.size() > 0) {
                        mCloseAllACTotalCheckCount = 10;
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_MONITOR_CLOSE_DEVICE),CLOSE_ALL_AC_TIMER);
                        for (Iterator<RoomHubData> it = mRoomHubDevList.iterator(); it.hasNext(); ) {
                            RoomHubData data = it.next();
                            if (data != null) {
                                if (data.IsOnLine() && !data.IsUpgrade())
                                    CloseElectricList(data.getAssetList());
                            }
                        }
                    }
                }
            };
            thread.start();
        }
    }

    private void CloseElectricList(ArrayList<AssetInfoData> asset_list){
        if(asset_list !=null){
            for (Iterator<AssetInfoData> it = asset_list.iterator(); it.hasNext(); ) {
                BaseAssetData asset_data = (BaseAssetData)it.next();
                int asset_type = asset_data.getAssetType();
                if(asset_type == DeviceTypeConvertApi.TYPE_ROOMHUB.AC){
                    if((asset_data != null) && (asset_data.getSubType() == ACDef.AC_SUBTYPE_SPLIT_TYPE)) {
                        //  if(ac_data.getPowerStatus() == ACDef.POWER_ON)
                        ACManager ac_mgr = (ACManager)mRoomHubMgr.getAssetDeviceManager(asset_type);
                        ac_mgr.setCommand(asset_data.getAssetUuid(), AssetDef.COMMAND_TYPE.POWER, ACDef.POWER_OFF);
                        SystemClock.sleep(500);
                    }
                }
            }
        }
    }

    public void OpenRoomHubListMenu(int index,View v){
        final RoomHubData data = getRoomHubDataByIdx(index);
        if(data != null) {
            String[] menu_array;
            final boolean isLogin=mAccountMgr.isLogin();
            final boolean isOwner=data.IsOwner();
            final boolean isFriend=data.IsFriend();

            if(data.IsUpgrade()) {
                if(isOwner)
                    launchOTAActivity(data.getUuid());
                else
                    Toast.makeText(getActivity(), R.string.device_upgrade_not_operate, Toast.LENGTH_SHORT).show();
                return;
            }

            if(isLogin){
                if(isOwner) {
                    menu_array = getResources().getStringArray(R.array.roomhub_menu_login_owner);
                }else {
                    if(isFriend){
                        menu_array = getResources().getStringArray(R.array.roomhub_menu_login_not_owner);
                    }else{
                        Toast.makeText(getActivity(), R.string.insufficient_permissions, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }else {
                if(isOwner) {
                    menu_array=getResources().getStringArray(R.array.roomhub_menu);
                }else{
                    Toast.makeText(getActivity(), R.string.insufficient_permissions, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            final RoomHubMenuDialog menu = new RoomHubMenuDialog(getActivity(), data, menu_array);

            menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (isLogin) {
                        if (isOwner)
                            OwnerItemClick(position, data);
                        else {
                            if (isFriend)
                                NoOwnerItemClick(position, data);
                        }
                    } else {
                        if (isOwner)
                            NoLoginItemClick(position, data);
                    }

                    menu.dismiss();
                }
            });
        }
    }

    private void NoLoginItemClick(int pos,RoomHubData data){
        switch (pos) {
            case ROOMHUB_MENU1: // about this hub
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                intent.putExtra(RoomHubManager.KEY_UUID, data.getUuid());
                /*
                bundle = new Bundle();
                bundle.putParcelable(RoomHubManager.KEY_ROOMHUB_DATA, data);
                intent.putExtras(bundle);
                */
                getActivity().startActivity(intent);
                break;
        }
    }

    private void OwnerItemClick(int pos,RoomHubData data){
        Intent intent;
        Bundle bundle;
        switch (pos) {
            case ROOMHUB_MENU1: //
                Log.d(TAG, "RoomHubMenuDialog onItemClick uuid=" + data.getUuid());
                if(!data.IsAlljoyn()){
                    Toast.makeText(getActivity(), R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(data.IsOnLine()==true) {
//                    if(data.checkVersion(data.getVersion(),"1.1.06")) {
                    if(SupportVersion.RoomHubVer.isValid(data.getVersion())) {
                        intent = new Intent(getActivity(), ElectricMgrActivity.class);
                        bundle = new Bundle();
                        bundle.putString(RoomHubManager.KEY_UUID, data.getUuid());

                        intent.putExtras(bundle);

                        getActivity().startActivity(intent);
                    }else
                        FirmwareTooOldDialog(data.getUuid());
                }else{
                    Toast.makeText(getActivity(), R.string.device_offline_not_operate, Toast.LENGTH_SHORT).show();
                }
                break;
            case ROOMHUB_MENU2: // share hub
                intent = new Intent(getActivity(), RoomHubShareHubActivity.class);
                bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID, data.getUuid());
                bundle.putString(RoomHubManager.KEY_DEV_NAME, data.getName());

                intent.putExtras(bundle);

                getActivity().startActivity(intent);
                break;
            case ROOMHUB_MENU3: // about this hub
                intent = new Intent(getActivity(), AboutActivity.class);
                intent.putExtra(RoomHubManager.KEY_UUID, data.getUuid());
                /*
                bundle = new Bundle();
                bundle.putParcelable(RoomHubManager.KEY_ROOMHUB_DATA, data);
                intent.putExtras(bundle);
                */
                getActivity().startActivity(intent);
                break;
            /*
            case ROOMHUB_MENU4:
                CloseAllElectric(data.getUuid());
                break;
            */
        }
    }

    private void FirmwareTooOldDialog(final String uuid){
        final Dialog dialog = new Dialog(getActivity(),R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.firmware_version_not_support));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setText(R.string.ok);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), OTAActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(OTAManager.OTA_DEVICE_UUID, uuid);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setVisibility(View.GONE);

        dialog.show();
    }

    private void NoOwnerItemClick(int pos,RoomHubData data){
        switch (pos) {
            /*
            case ROOMHUB_MENU1: //Select IR
                if(mRoomHubMgr.IsAlljoyn(data.getUuid()))
                    ((RoomHubMainPageNew) mContext).configIRSetting(data.getUuid());
                else
                    Toast.makeText(mContext, R.string.roomhub_warning_msg, Toast.LENGTH_SHORT).show();
                break;
            */
            case ROOMHUB_MENU1: // share hub info
                Intent intent = new Intent(getActivity(), ShareHubInfoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID, data.getUuid());
                bundle.putString("owner_name", data.getOwnerName());
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
                break;
            case ROOMHUB_MENU2: // leave hub
                LeaveDeviceDialog(data.getUuid());

                //Log.d(TAG, "NoOwnerItemClick ret=" + ret);
                break;
        }
    }

    private void LeaveDeviceDialog(String uuid){
        final String str_uuid=uuid;

        final Dialog dialog = new Dialog(getActivity(),R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.leave_device));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ((RoomHubMainPage) getActivity()).showProgressDialog("", getString(R.string.processing_str));
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        int ret = mAccountMgr.DeleteDeviceUser(str_uuid, mAccountMgr.getUserId());
                        Log.d(TAG, "LeaveDeviceDialog ret=" + ret);
                        ((RoomHubMainPage) getActivity()).dismissProgressDialog();
                        if (ret != ErrorKey.Success) {
                            mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_LEAVE_HUB_ERROR, ret));
                        }
                    }
                };
                thread.start();
            }
        });


        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void launchOTAActivity(String uuid) {
        Intent intent = new Intent(getActivity(), OTAActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putString(OTAManager.OTA_DEVICE_UUID, uuid);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    private RoomHubData getRoomHubDataByIdx(int idx) {
        if(mRoomHubDevList==null) return null;

        for(int i=0;i<mRoomHubDevList.size();i++){
            if(idx == i){
                return mRoomHubDevList.get(i);
            }
        }
        return null;
    }

    /*
        public int getPMStatus(int pos, int type, RoomHubData roomhub_data) {
            RoomHubData data;
            if (roomhub_data == null)
                data = mRoomHubDevList.get(pos);
            else
                data = roomhub_data;

            ElectricData electric_data=mRoomHubMgr.getElectricDataByType(data.getUuid(),type);
            if(electric_data != null) {
                String uuid = electric_data.getElectricUuid();
                if (type == RoomHubDef.ELECTRIC_PARTICULATE_MATTER) {
                    PMData pm_data = ((RoomHubMainPage) getActivity()).getPMManager().getPMDataByUuid(uuid);

                    if(pm_data == null)
                        return -1;
                    AQIApi.AQI_CATEGORY catetory=AQIApi.getAQICategoryByPM25Value(pm_data.getValue());
                    if (catetory == AQIApi.AQI_CATEGORY.DANGER)
                        return AIR_QUALITY_DANGER;
                    else if (catetory == AQIApi.AQI_CATEGORY.NORMAL)
                        return AIR_QUALITY_NORMAL;
                    else
                        return AIR_QUALITY_GOOD;
                }
            }
            return -1;
        }
        */
    public void LaunchElectricActivity(int pos, int type, RoomHubData roomhub_data){
        RoomHubData data;
        if(roomhub_data == null)
            data=mRoomHubDevList.get(pos);
        else
            data=roomhub_data;

        if(data != null) {
            if(data.IsUpgrade()) {
                if(data.IsOwner())
                    launchOTAActivity(data.getUuid());
                else
                    Toast.makeText(getActivity(), R.string.device_upgrade_not_operate, Toast.LENGTH_SHORT).show();
                return;
            }

            if(data.IsOnLine()==true) {
                boolean is_data_sync=true;
                boolean is_pair=true;

                //BaseAssetManager base_asset = mRoomHubMgr.getAssetDeviceManager(type);
                BaseAssetData asset_data = (BaseAssetData)data.AssetIsExist(type);
                if(asset_data != null){
                    is_data_sync = asset_data.IsReady();
                    is_pair = asset_data.IsIRPair();


                    if(!is_data_sync){
                        Toast.makeText(getActivity(), R.string.data_sync_msg, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //Bulb no need check pair
                    if(!is_pair&&type != DeviceTypeConvertApi.TYPE_ROOMHUB.BULB){
                        if (!data.IsAlljoyn() || !data.IsOwner()) {
                            Toast.makeText(getActivity(), R.string.device_not_ir_pairing, Toast.LENGTH_SHORT).show();
                        } else {
                            doIRPairingDialog(data);
                        }
                        return;
                    }

                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(BaseAssetManager.KEY_UUID, data.getUuid());
                    intent.putExtra(BaseAssetManager.KEY_ASSET_UUID, asset_data.getAssetUuid());

                    switch(type){
                        case DeviceTypeConvertApi.TYPE_ROOMHUB.AC:
                            if(asset_data.getSubType() == ACDef.AC_SUBTYPE_WINDOW_TYPE)
                                intent.setClass(getActivity(), WindowTypeACController.class);
                            else
                                intent.setClass(getActivity(), RoomHubControllerFlipper.class);
                            break;
                        case DeviceTypeConvertApi.TYPE_ROOMHUB.FAN:
                            intent.setClass(getActivity(), FANControllerV2.class);
                            break;

                        case DeviceTypeConvertApi.TYPE_ROOMHUB.PM25:
                            intent.setClass(getActivity(), PMActivity.class);
                            break;
                        case DeviceTypeConvertApi.TYPE_ROOMHUB.AIR_PURIFIER:
                            if(asset_data.getConnectionType() == AssetDef.CONNECTION_TYPE_BT)
                                intent.setClass(getActivity(), AirPurifierBTActivity.class);
                            else
                                intent.setClass(getActivity(), AirPurifierIRActivity.class);
                            break;
                        case DeviceTypeConvertApi.TYPE_ROOMHUB.TV:
                            intent.setClass(getContext(), TVController.class);
                            break;
                        case DeviceTypeConvertApi.TYPE_ROOMHUB.BULB:
                            intent.setClass(getContext(), BulbController.class);
                            break;
                    }

                    startActivity(intent);
                }
            }else
                Toast.makeText(getActivity(), R.string.device_offline_not_operate, Toast.LENGTH_SHORT).show();
        }
    }

    private void doIRPairingDialog(final RoomHubData data){
        final Dialog dialog = new Dialog(getActivity(),R.style.CustomDialog);

        dialog.setContentView(R.layout.custom_dialog);

        TextView txt_msg=(TextView)dialog.findViewById(R.id.txt_message);
        txt_msg.setText(getString(R.string.device_not_ir_pairing_owner));

        Button btn_yes = (Button)dialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), ElectricMgrActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(RoomHubManager.KEY_UUID, data.getUuid());

                intent.putExtras(bundle);

                getActivity().startActivity(intent);
            }
        });


        Button btn_no = (Button)dialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void LaunchAddElectric(int pos,RoomHubData roomhub_data){
        RoomHubData data;
        if(roomhub_data == null)
            data=mRoomHubDevList.get(pos);
        else
            data=roomhub_data;

        if(data != null) {
            if (data.IsUpgrade()) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.device_upgrade_not_operate), Toast.LENGTH_SHORT).show();
            } else if (!data.IsOnLine()) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.device_offline_not_operate), Toast.LENGTH_SHORT).show();
            } else if ((!data.IsOwner()) && (!data.IsFriend())) {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.insufficient_permissions), Toast.LENGTH_SHORT).show();
            } else if (!data.IsAlljoyn()){
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.roomhub_warning_msg), Toast.LENGTH_SHORT).show();
            } else {
//                if(data.checkVersion(data.getVersion(),"1.1.06")) {
                if(SupportVersion.RoomHubVer.isValid(data.getVersion())) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), AddElectricActivity.class);
                    intent.putExtra(AssetDef.ADD_STATUS, AssetDef.ADD.ASSET);
                    intent.putExtra(RoomHubManager.KEY_UUID, data.getUuid());

                    startActivity(intent);
                }else{
                    FirmwareTooOldDialog(data.getUuid());
                }
            }
        }
    }

    private void UpdateLayout(){
        if(mRoomHubDevList.size() > 0) {
            ll_devlist.setVisibility(View.VISIBLE);
            ll_add_hub.setVisibility(View.GONE);
        }else {
            ll_devlist.setVisibility(View.GONE);
            ll_add_hub.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addAssetDevice(AssetInfoData asset_info_data, RoomHubData data, int result) {
        if(result < ErrorKey.Success) return;

        Message msg=new Message();
        msg.what=MESSAGE_ADD_ELECTRIC_DEVICE;
        Bundle bundle=new Bundle();
        bundle.putParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA,asset_info_data);
        bundle.putParcelable(BaseAssetManager.KEY_ROOMHUB_DATA,data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public void removeAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        Message msg=new Message();
        msg.what=MESSAGE_REMOVE_ELECTRIC_DEVICE;
        Bundle bundle=new Bundle();
        bundle.putParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA,asset_info_data);
        bundle.putParcelable(BaseAssetManager.KEY_ROOMHUB_DATA,data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public void updateAssetDevice(AssetInfoData asset_info_data, RoomHubData data) {
        Message msg=new Message();
        msg.what=MESSAGE_UPDATE_ELECTRIC_DEVICE;
        Bundle bundle=new Bundle();
        bundle.putParcelable(BaseAssetManager.KEY_ASSET_INFO_DATA,asset_info_data);
        bundle.putParcelable(BaseAssetManager.KEY_ROOMHUB_DATA,data);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
}
